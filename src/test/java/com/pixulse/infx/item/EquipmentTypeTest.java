package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.item.material.RawItem;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EquipmentTypeTest {
    private static final Set<InfxMaterial> METALS = EnumSet.of(
            InfxMaterial.COPPER,
            InfxMaterial.SILVER,
            InfxMaterial.GOLD,
            InfxMaterial.RUSTED_IRON,
            InfxMaterial.IRON,
            InfxMaterial.ANCIENT_METAL,
            InfxMaterial.MITHRIL,
            InfxMaterial.ADAMANTIUM);

    @Test
    void representativeAllowedSetsMatchTheApprovedMatrix() {
        assertEquals(METALS, EquipmentType.PICKAXE.allowedMaterials());
        assertEquals(
                EnumSet.of(InfxMaterial.FLINT, InfxMaterial.OBSIDIAN),
                EquipmentType.KNIFE.allowedMaterials());
        assertEquals(
                EnumSet.of(InfxMaterial.WOOD, InfxMaterial.ANCIENT_METAL, InfxMaterial.MITHRIL),
                EquipmentType.BOW.allowedMaterials());
        assertEquals(
                EnumSet.of(
                        InfxMaterial.LEATHER,
                        InfxMaterial.COPPER,
                        InfxMaterial.SILVER,
                        InfxMaterial.GOLD,
                        InfxMaterial.RUSTED_IRON,
                        InfxMaterial.IRON,
                        InfxMaterial.ANCIENT_METAL,
                        InfxMaterial.MITHRIL,
                        InfxMaterial.ADAMANTIUM),
                EquipmentType.HELMET.allowedMaterials());
        assertFalse(EquipmentType.HORSE_ARMOR.allows(InfxMaterial.RUSTED_IRON));
        assertFalse(EquipmentType.FISHING_ROD.allows(InfxMaterial.RUSTED_IRON));
    }

    @Test
    void illegalKeysFailWithTheOffendingCombination() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> new EquipmentKey(InfxMaterial.WOOD, EquipmentType.PICKAXE));
        assertTrue(error.getMessage().contains("wood_pickaxe"));
    }

    @Test
    void excludedArtifactsCannotBeRepresented() {
        Set<String> paths = EquipmentKey.all().stream()
                .map(EquipmentKey::path)
                .collect(java.util.stream.Collectors.toSet());
        assertFalse(paths.contains("iron_knife"));
        assertFalse(paths.contains("stone_dagger"));
        assertFalse(paths.contains("chip_flint_knife"));
        assertFalse(paths.stream().anyMatch(path -> path.startsWith("diamond_")));
        assertFalse(paths.stream().anyMatch(path -> path.contains("carrot_on_a_stick")));
    }

    @Test
    void specialtyFactoriesAreDeclaredByType() {
        assertEquals(EquipmentType.FactoryKind.SHEARS, EquipmentType.SHEARS.factoryKind());
        assertEquals(EquipmentType.FactoryKind.FISHING_ROD, EquipmentType.FISHING_ROD.factoryKind());
        assertEquals(EquipmentType.FactoryKind.BOW, EquipmentType.BOW.factoryKind());
        assertEquals(EquipmentType.FactoryKind.ARROW, EquipmentType.ARROW.factoryKind());
        assertEquals(EquipmentType.FactoryKind.PLAIN, EquipmentType.HELMET.factoryKind());
        assertEquals(EquipmentType.FactoryKind.ORDINARY, EquipmentType.PICKAXE.factoryKind());
    }

    @Test
    void reachModesMatchTheApprovedToolWeaponAndHybridMatrix() {
        assertEquals(
                Set.of(
                        EquipmentType.PICKAXE,
                        EquipmentType.SHOVEL,
                        EquipmentType.HOE,
                        EquipmentType.MATTOCK,
                        EquipmentType.SHEARS),
                typesWithReachMode(ReachMode.INTERACTION));
        assertEquals(
                Set.of(
                        EquipmentType.CUDGEL,
                        EquipmentType.CLUB,
                        EquipmentType.KNIFE,
                        EquipmentType.SWORD,
                        EquipmentType.DAGGER),
                typesWithReachMode(ReachMode.MELEE));
        assertEquals(
                Set.of(
                        EquipmentType.HATCHET,
                        EquipmentType.AXE,
                        EquipmentType.BATTLE_AXE,
                        EquipmentType.WAR_HAMMER,
                        EquipmentType.SCYTHE),
                typesWithReachMode(ReachMode.BOTH));
        assertEquals(
                Set.of(
                        EquipmentType.FISHING_ROD,
                        EquipmentType.BOW,
                        EquipmentType.ARROW,
                        EquipmentType.HELMET,
                        EquipmentType.CHESTPLATE,
                        EquipmentType.LEGGINGS,
                        EquipmentType.BOOTS,
                        EquipmentType.CHAINMAIL_HELMET,
                        EquipmentType.CHAINMAIL_CHESTPLATE,
                        EquipmentType.CHAINMAIL_LEGGINGS,
                        EquipmentType.CHAINMAIL_BOOTS,
                        EquipmentType.HORSE_ARMOR),
                typesWithReachMode(ReachMode.NONE));
    }

    private static Set<EquipmentType> typesWithReachMode(ReachMode mode) {
        return Arrays.stream(EquipmentType.values())
                .filter(type -> type.reachMode() == mode)
                .collect(java.util.stream.Collectors.toSet());
    }
}
