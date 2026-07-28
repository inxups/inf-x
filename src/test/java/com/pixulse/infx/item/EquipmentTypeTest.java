package com.pixulse.infx.item;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.item.material.RawItem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class EquipmentTypeTest {
    private static final Set<MiteMaterial> METALS = EnumSet.of(
            MiteMaterial.COPPER,
            MiteMaterial.SILVER,
            MiteMaterial.GOLD,
            MiteMaterial.RUSTED_IRON,
            MiteMaterial.IRON,
            MiteMaterial.ANCIENT_METAL,
            MiteMaterial.MITHRIL,
            MiteMaterial.ADAMANTIUM);

    @Test
    void matrixHasExactCategoryCounts() {
        Map<EquipmentCategory, Long> counts = EquipmentKey.all().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        key -> key.type().category(), java.util.stream.Collectors.counting()));
        assertEquals(96L, counts.get(EquipmentCategory.TOOL));
        assertEquals(33L, counts.get(EquipmentCategory.WEAPON));
        assertEquals(36L, counts.get(EquipmentCategory.PLATE_ARMOR));
        assertEquals(32L, counts.get(EquipmentCategory.CHAIN_ARMOR));
        assertEquals(7L, counts.get(EquipmentCategory.HORSE_ARMOR));
        assertEquals(204, EquipmentKey.all().size());
    }

    @Test
    void goldenCatalogMatchesRawAndEquipmentDefinitionOrder() throws Exception {
        List<String> golden = Files.readAllLines(
                Path.of(EquipmentTypeTest.class.getResource("/r196/catalog-paths.txt").toURI()), UTF_8);
        List<String> actual = Stream.concat(
                        Arrays.stream(RawItem.values()).map(RawItem::path),
                        EquipmentKey.all().stream().map(EquipmentKey::path))
                .toList();
        assertEquals(golden, actual);
    }

    @Test
    void representativeAllowedSetsMatchTheApprovedMatrix() {
        assertEquals(METALS, EquipmentType.PICKAXE.allowedMaterials());
        assertEquals(
                EnumSet.of(MiteMaterial.FLINT, MiteMaterial.OBSIDIAN),
                EquipmentType.KNIFE.allowedMaterials());
        assertEquals(
                EnumSet.of(MiteMaterial.WOOD, MiteMaterial.ANCIENT_METAL, MiteMaterial.MITHRIL),
                EquipmentType.BOW.allowedMaterials());
        assertEquals(
                EnumSet.of(
                        MiteMaterial.LEATHER,
                        MiteMaterial.COPPER,
                        MiteMaterial.SILVER,
                        MiteMaterial.GOLD,
                        MiteMaterial.RUSTED_IRON,
                        MiteMaterial.IRON,
                        MiteMaterial.ANCIENT_METAL,
                        MiteMaterial.MITHRIL,
                        MiteMaterial.ADAMANTIUM),
                EquipmentType.HELMET.allowedMaterials());
        assertFalse(EquipmentType.HORSE_ARMOR.allows(MiteMaterial.RUSTED_IRON));
        assertFalse(EquipmentType.FISHING_ROD.allows(MiteMaterial.RUSTED_IRON));
    }

    @Test
    void illegalKeysFailWithTheOffendingCombination() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> new EquipmentKey(MiteMaterial.WOOD, EquipmentType.PICKAXE));
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
        assertEquals(204, paths.size());
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
}
