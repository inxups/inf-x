package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.pixulse.infx.item.material.InfxMaterial;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EquipmentKeyTest {
    private static EquipmentKey key(InfxMaterial material, EquipmentType type) {
        return new EquipmentKey(material, type);
    }

    @Test
    void toolFormulasCoverLowMiddleAndHighTiers() {
        assertEquals(400, key(InfxMaterial.FLINT, EquipmentType.HATCHET).durability());
        assertEquals(9_600, key(InfxMaterial.IRON, EquipmentType.PICKAXE).durability());
        assertEquals(512_000, key(InfxMaterial.ADAMANTIUM, EquipmentType.WAR_HAMMER).durability());
        assertEquals(2.5F, key(InfxMaterial.FLINT, EquipmentType.HATCHET).miningSpeed());
        assertEquals(8.0F, key(InfxMaterial.IRON, EquipmentType.PICKAXE).miningSpeed());
        assertEquals(9.0F, key(InfxMaterial.ADAMANTIUM, EquipmentType.WAR_HAMMER).miningSpeed());
        assertEquals(3.0F, key(InfxMaterial.FLINT, EquipmentType.HATCHET).meleeDamage());
        assertEquals(6.0F, key(InfxMaterial.IRON, EquipmentType.PICKAXE).meleeDamage());
        assertEquals(8.0F, key(InfxMaterial.ADAMANTIUM, EquipmentType.WAR_HAMMER).meleeDamage());
    }

    @Test
    void wearAndReachMatchR196TypeProfiles() {
        assertEquals(4.0F / 3.0F, EquipmentType.HATCHET.blockDecay());
        assertEquals(.4F, EquipmentType.MATTOCK.blockDecay());
        assertEquals(2.0F / 3.0F, EquipmentType.WAR_HAMMER.blockDecay());
        assertEquals(133, key(InfxMaterial.FLINT, EquipmentType.HATCHET).attackWear());
        assertEquals(75, key(InfxMaterial.COPPER, EquipmentType.BATTLE_AXE).attackWear());
        assertEquals(66, key(InfxMaterial.IRON, EquipmentType.WAR_HAMMER).attackWear());
        assertEquals(1.0F, EquipmentType.SCYTHE.reachBonus());
        assertEquals(.25F, EquipmentType.KNIFE.reachBonus());
    }

    @Test
    void modernAttackSpeedAnalogsAreLiteral() {
        Map<EquipmentType, Float> expected = Map.ofEntries(
                Map.entry(EquipmentType.PICKAXE, -2.8F),
                Map.entry(EquipmentType.SHOVEL, -3.0F),
                Map.entry(EquipmentType.MATTOCK, -3.0F),
                Map.entry(EquipmentType.HATCHET, -3.2F),
                Map.entry(EquipmentType.AXE, -3.1F),
                Map.entry(EquipmentType.BATTLE_AXE, -3.1F),
                Map.entry(EquipmentType.WAR_HAMMER, -3.4F),
                Map.entry(EquipmentType.CLUB, -3.4F),
                Map.entry(EquipmentType.CUDGEL, -3.4F),
                Map.entry(EquipmentType.HOE, -1.0F),
                Map.entry(EquipmentType.SCYTHE, -1.0F),
                Map.entry(EquipmentType.SWORD, -2.4F),
                Map.entry(EquipmentType.DAGGER, -2.4F),
                Map.entry(EquipmentType.KNIFE, -2.4F));
        expected.forEach((type, speed) -> assertEquals(speed, type.attackSpeedModifier(), type.path()));
        assertFalse(EquipmentType.SHEARS.hasAttackSpeedModifier());
        assertFalse(EquipmentType.BOW.hasAttackSpeedModifier());
    }

    @Test
    void bowArrowAndFishingValuesMatchR196() {
        assertEquals(32, key(InfxMaterial.WOOD, EquipmentType.BOW).durability());
        assertEquals(64, key(InfxMaterial.ANCIENT_METAL, EquipmentType.BOW).durability());
        assertEquals(128, key(InfxMaterial.MITHRIL, EquipmentType.BOW).durability());
        assertEquals(3, key(InfxMaterial.FLINT, EquipmentType.FISHING_ROD).durability());
        assertEquals(16, key(InfxMaterial.IRON, EquipmentType.FISHING_ROD).durability());
        assertEquals(512, key(InfxMaterial.ADAMANTIUM, EquipmentType.FISHING_ROD).durability());
        assertEquals(1.0, key(InfxMaterial.FLINT, EquipmentType.ARROW).arrowBaseDamage());
        assertEquals(2.5, key(InfxMaterial.IRON, EquipmentType.ARROW).arrowBaseDamage());
        assertEquals(3.5, key(InfxMaterial.ADAMANTIUM, EquipmentType.ARROW).arrowBaseDamage());
    }

    @Test
    void armorDurabilityAndFractionalProtectionMatchR196() {
        EquipmentKey copperHelmet = key(InfxMaterial.COPPER, EquipmentType.HELMET);
        EquipmentKey copperChainHelmet = key(InfxMaterial.COPPER, EquipmentType.CHAINMAIL_HELMET);
        assertEquals(40, copperHelmet.durability());
        assertEquals(20, copperChainHelmet.durability());
        assertEquals(35.0F / 24.0F, copperHelmet.armorProtection());
        assertEquals(25.0F / 24.0F, copperChainHelmet.armorProtection());

        float plateSum = EquipmentType.platePieces().stream()
                .map(type -> key(InfxMaterial.MITHRIL, type))
                .map(EquipmentKey::armorProtection)
                .reduce(0.0F, Float::sum);
        float chainSum = EquipmentType.chainPieces().stream()
                .map(type -> key(InfxMaterial.MITHRIL, type))
                .map(EquipmentKey::armorProtection)
                .reduce(0.0F, Float::sum);
        assertEquals(9.0F, plateSum, 1.0E-6F);
        assertEquals(7.0F, chainSum, 1.0E-6F);
        assertEquals(7.0F, key(InfxMaterial.ADAMANTIUM, EquipmentType.HORSE_ARMOR).armorProtection());
    }

    @Test
    void approvedNameExceptionsAndEquipmentAssetsAreStable() {
        assertEquals("Copper Pickaxe", key(InfxMaterial.COPPER, EquipmentType.PICKAXE).englishName());
        assertEquals("铜镐", key(InfxMaterial.COPPER, EquipmentType.PICKAXE).chineseName());
        assertEquals("Bow", key(InfxMaterial.WOOD, EquipmentType.BOW).englishName());
        assertEquals("短木棒", key(InfxMaterial.WOOD, EquipmentType.CUDGEL).chineseName());
        assertEquals("木棒", key(InfxMaterial.WOOD, EquipmentType.CLUB).chineseName());
        assertEquals("Fishing Rod", key(InfxMaterial.ADAMANTIUM, EquipmentType.FISHING_ROD).englishName());
        assertEquals("Gold Horse Armor", key(InfxMaterial.GOLD, EquipmentType.HORSE_ARMOR).englishName());
        assertEquals("infx:mithril_chainmail",
                key(InfxMaterial.MITHRIL, EquipmentType.CHAINMAIL_CHESTPLATE)
                        .equipmentAsset().identifier().toString());
    }

    @Test
    void ordinaryToolBehaviorsAreMappedWithoutVanillaConstructorMutation() {
        assertEquals(InfxUseAction.AXE, EquipmentType.HATCHET.useAction());
        assertEquals(InfxUseAction.AXE, EquipmentType.AXE.useAction());
        assertEquals(InfxUseAction.AXE, EquipmentType.BATTLE_AXE.useAction());
        assertEquals(InfxUseAction.SHOVEL, EquipmentType.SHOVEL.useAction());
        assertEquals(InfxUseAction.HOE, EquipmentType.HOE.useAction());
        assertEquals(InfxUseAction.MATTOCK, EquipmentType.MATTOCK.useAction());
        assertEquals(133, key(InfxMaterial.FLINT, EquipmentType.HATCHET).damageForBreaking(1.0F));
        assertEquals(20, key(InfxMaterial.COPPER, EquipmentType.MATTOCK).damageForBreaking(.5F));
        assertEquals(0, key(InfxMaterial.IRON, EquipmentType.PICKAXE).damageForBreaking(0.0F));
    }
}
