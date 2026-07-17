package com.pixulse.infx.item;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.material.R196RawItem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class R196EquipmentTypeTest {
    private static final Set<R196Material> METALS = EnumSet.of(
            R196Material.COPPER,
            R196Material.SILVER,
            R196Material.GOLD,
            R196Material.RUSTED_IRON,
            R196Material.IRON,
            R196Material.ANCIENT_METAL,
            R196Material.MITHRIL,
            R196Material.ADAMANTIUM);

    @Test
    void matrixHasExactCategoryCounts() {
        Map<R196EquipmentCategory, Long> counts = R196EquipmentKey.all().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        key -> key.type().category(), java.util.stream.Collectors.counting()));
        assertEquals(96L, counts.get(R196EquipmentCategory.TOOL));
        assertEquals(33L, counts.get(R196EquipmentCategory.WEAPON));
        assertEquals(36L, counts.get(R196EquipmentCategory.PLATE_ARMOR));
        assertEquals(32L, counts.get(R196EquipmentCategory.CHAIN_ARMOR));
        assertEquals(7L, counts.get(R196EquipmentCategory.HORSE_ARMOR));
        assertEquals(204, R196EquipmentKey.all().size());
    }

    @Test
    void goldenCatalogMatchesRawAndEquipmentDefinitionOrder() throws Exception {
        List<String> golden = Files.readAllLines(
                Path.of(R196EquipmentTypeTest.class.getResource("/r196/catalog-paths.txt").toURI()), UTF_8);
        List<String> actual = Stream.concat(
                        Arrays.stream(R196RawItem.values()).map(R196RawItem::path),
                        R196EquipmentKey.all().stream().map(R196EquipmentKey::path))
                .toList();
        assertEquals(golden, actual);
    }

    @Test
    void representativeAllowedSetsMatchTheApprovedMatrix() {
        assertEquals(METALS, R196EquipmentType.PICKAXE.allowedMaterials());
        assertEquals(
                EnumSet.of(R196Material.FLINT, R196Material.OBSIDIAN),
                R196EquipmentType.KNIFE.allowedMaterials());
        assertEquals(
                EnumSet.of(R196Material.WOOD, R196Material.ANCIENT_METAL, R196Material.MITHRIL),
                R196EquipmentType.BOW.allowedMaterials());
        assertEquals(
                EnumSet.of(
                        R196Material.LEATHER,
                        R196Material.COPPER,
                        R196Material.SILVER,
                        R196Material.GOLD,
                        R196Material.RUSTED_IRON,
                        R196Material.IRON,
                        R196Material.ANCIENT_METAL,
                        R196Material.MITHRIL,
                        R196Material.ADAMANTIUM),
                R196EquipmentType.HELMET.allowedMaterials());
        assertFalse(R196EquipmentType.HORSE_ARMOR.allows(R196Material.RUSTED_IRON));
        assertFalse(R196EquipmentType.FISHING_ROD.allows(R196Material.RUSTED_IRON));
    }

    @Test
    void illegalKeysFailWithTheOffendingCombination() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> new R196EquipmentKey(R196Material.WOOD, R196EquipmentType.PICKAXE));
        assertTrue(error.getMessage().contains("wood_pickaxe"));
    }

    @Test
    void excludedArtifactsCannotBeRepresented() {
        Set<String> paths = R196EquipmentKey.all().stream()
                .map(R196EquipmentKey::path)
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
        assertEquals(R196EquipmentType.FactoryKind.SHEARS, R196EquipmentType.SHEARS.factoryKind());
        assertEquals(R196EquipmentType.FactoryKind.FISHING_ROD, R196EquipmentType.FISHING_ROD.factoryKind());
        assertEquals(R196EquipmentType.FactoryKind.BOW, R196EquipmentType.BOW.factoryKind());
        assertEquals(R196EquipmentType.FactoryKind.ARROW, R196EquipmentType.ARROW.factoryKind());
        assertEquals(R196EquipmentType.FactoryKind.PLAIN, R196EquipmentType.HELMET.factoryKind());
        assertEquals(R196EquipmentType.FactoryKind.ORDINARY, R196EquipmentType.PICKAXE.factoryKind());
    }
}
