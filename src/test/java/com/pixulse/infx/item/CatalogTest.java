package com.pixulse.infx.item;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.registry.tag.ModTags;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class CatalogTest {
    private static Catalog catalog() {
        return ModItems.catalog();
    }

    private static List<String> goldenPaths() throws IOException, URISyntaxException {
        return Files.readAllLines(
                Path.of(CatalogTest.class.getResource("/r196/catalog-paths.txt").toURI()), UTF_8);
    }

    @Test
    void catalogMatchesTheIndependentGoldenManifest() throws Exception {
        List<String> actual = catalog().entries().stream().map(Catalog.Entry::path).toList();
        assertEquals(goldenPaths(), actual);
        assertEquals(234, actual.size());
        assertEquals(234, new HashSet<>(actual).size());
        assertEquals(30, catalog().rawEntries().size());
        assertEquals(204, catalog().equipmentEntries().size());
    }

    @Test
    void allEquipmentIsModOwnedButExactVanillaRawCurrencyIsReused() {
        assertTrue(catalog().equipmentEntries().stream()
                .allMatch(entry -> entry.id().getNamespace().equals("infx")));
        assertSame(Items.COPPER_NUGGET, catalog().reusedRaw("copper_nugget"));
        assertSame(Items.GOLD_NUGGET, catalog().reusedRaw("gold_nugget"));
        assertSame(Items.IRON_NUGGET, catalog().reusedRaw("iron_nugget"));
        assertSame(Items.COPPER_INGOT, catalog().reusedRaw("copper_ingot"));
        assertSame(Items.GOLD_INGOT, catalog().reusedRaw("gold_ingot"));
        assertSame(Items.IRON_INGOT, catalog().reusedRaw("iron_ingot"));
        assertThrows(IllegalArgumentException.class, () -> catalog().reusedRaw("silver_ingot"));
    }

    @Test
    void aliasesPreserveCatalogRegistryIds() {
        assertSame(ModItems.FLINT_CHIP, catalog().raw("flint_chip").holder());
        assertSame(ModItems.SINEW, catalog().raw("sinew").holder());
        assertSame(ModItems.OBSIDIAN_SHARD, catalog().raw("obsidian_shard").holder());
        assertSame(ModItems.EMERALD_SHARD, catalog().raw("emerald_shard").holder());
        assertSame(ModItems.SILVER_NUGGET, catalog().raw("silver_nugget").holder());
        assertSame(ModItems.MITHRIL_NUGGET, catalog().raw("mithril_nugget").holder());
        assertSame(ModItems.ADAMANTIUM_NUGGET, catalog().raw("adamantium_nugget").holder());
        assertSame(
                ModItems.FLINT_HATCHET,
                catalog().equipment(MiteMaterial.FLINT, EquipmentType.HATCHET).holder());
        assertSame(
                ModItems.FLINT_SHOVEL,
                catalog().equipment(MiteMaterial.FLINT, EquipmentType.SHOVEL).holder());
        assertSame(
                ModItems.FLINT_AXE,
                catalog().equipment(MiteMaterial.FLINT, EquipmentType.AXE).holder());
        assertSame(
                ModItems.COPPER_PICKAXE,
                catalog().equipment(MiteMaterial.COPPER, EquipmentType.PICKAXE).holder());
        assertSame(
                ModItems.COPPER_SHOVEL,
                catalog().equipment(MiteMaterial.COPPER, EquipmentType.SHOVEL).holder());
        assertSame(
                ModItems.COPPER_AXE,
                catalog().equipment(MiteMaterial.COPPER, EquipmentType.AXE).holder());
        assertSame(
                ModItems.COPPER_HOE,
                catalog().equipment(MiteMaterial.COPPER, EquipmentType.HOE).holder());
        assertSame(
                ModItems.COPPER_SWORD,
                catalog().equipment(MiteMaterial.COPPER, EquipmentType.SWORD).holder());
        assertSame(
                ModItems.IRON_PICKAXE,
                catalog().equipment(MiteMaterial.IRON, EquipmentType.PICKAXE).holder());
        assertSame(
                ModItems.IRON_SHOVEL,
                catalog().equipment(MiteMaterial.IRON, EquipmentType.SHOVEL).holder());
        assertSame(
                ModItems.IRON_AXE,
                catalog().equipment(MiteMaterial.IRON, EquipmentType.AXE).holder());
        assertSame(
                ModItems.IRON_HOE,
                catalog().equipment(MiteMaterial.IRON, EquipmentType.HOE).holder());
        assertSame(
                ModItems.IRON_SWORD,
                catalog().equipment(MiteMaterial.IRON, EquipmentType.SWORD).holder());
    }

    @Test
    void missingLookupsFailWithTheRequestedIdentity() {
        IllegalArgumentException raw =
                assertThrows(IllegalArgumentException.class, () -> catalog().raw("iron_coin"));
        assertTrue(raw.getMessage().contains("iron_coin"));
        IllegalArgumentException equipment = assertThrows(
                IllegalArgumentException.class,
                () -> catalog().equipment(MiteMaterial.WOOD, EquipmentType.PICKAXE));
        assertTrue(equipment.getMessage().contains("wood_pickaxe"));
        assertFalse(catalog().entries().stream().anyMatch(entry -> entry.path().contains("diamond_helmet")));
        assertFalse(catalog().entries().stream().anyMatch(entry -> entry.path().endsWith("_frags")));
    }

    @Test
    void specialtyFactoriesAreNotCollapsedToPlainItems() {
        assertEquals(
                MiteShearsItem.class,
                catalog().equipment(MiteMaterial.COPPER, EquipmentType.SHEARS).itemClass());
        assertEquals(
                MiteFishingRodItem.class,
                catalog().equipment(MiteMaterial.FLINT, EquipmentType.FISHING_ROD).itemClass());
        assertEquals(
                ToolItem.class,
                catalog().equipment(MiteMaterial.COPPER, EquipmentType.PICKAXE).itemClass());
    }

    @Test
    void projectileFactoriesRetainCatalogIdentity() {
        assertEquals(
                MiteBowItem.class,
                catalog().equipment(MiteMaterial.WOOD, EquipmentType.BOW).itemClass());
        assertEquals(
                MiteArrowItem.class,
                catalog().equipment(MiteMaterial.ADAMANTIUM, EquipmentType.ARROW).itemClass());
    }

    @Test
    void orderedViewsAreStableForDataGenerationAndCreativeTabs() {
        assertEquals("flint_chip", catalog().rawEntries().getFirst().path());
        assertEquals("adamantium_coin", catalog().rawEntries().getLast().path());
        assertEquals("leather_helmet", catalog().equipmentEntries().getFirst().path());
        assertEquals("adamantium_horse_armor", catalog().equipmentEntries().getLast().path());
        assertEquals(
                "repair_materials/rusted_iron",
                ModTags.Items.repairMaterial(MiteMaterial.RUSTED_IRON).location().getPath());
        assertEquals(
                "equipment/war_hammer",
                ModTags.Items.equipmentType(EquipmentType.WAR_HAMMER).location().getPath());
    }

    @Test
    void everyDefinitionHasTwoNamesAndApprovedTerminology() {
        for (Catalog.Entry entry : catalog().entries()) {
            assertFalse(entry.englishName().isBlank(), entry.path());
            assertFalse(entry.chineseName().isBlank(), entry.path());
        }
        assertEquals(
                "InfiniteX Copper Pickaxe",
                catalog().equipment(MiteMaterial.COPPER, EquipmentType.PICKAXE).englishName());
        assertEquals(
                "InfiniteX 铜镐",
                catalog().equipment(MiteMaterial.COPPER, EquipmentType.PICKAXE).chineseName());
        assertEquals(
                "Ancient Metal War Hammer",
                catalog().equipment(MiteMaterial.ANCIENT_METAL, EquipmentType.WAR_HAMMER).englishName());
        assertEquals(
                "远古金属锁链胸甲",
                catalog().equipment(MiteMaterial.ANCIENT_METAL, EquipmentType.CHAINMAIL_CHESTPLATE).chineseName());
        assertEquals(
                "Gold Horse Armor",
                catalog().equipment(MiteMaterial.GOLD, EquipmentType.HORSE_ARMOR).englishName());
        assertEquals("Bow", catalog().equipment(MiteMaterial.WOOD, EquipmentType.BOW).englishName());
        assertEquals(
                "Fishing Rod",
                catalog().equipment(MiteMaterial.ADAMANTIUM, EquipmentType.FISHING_ROD).englishName());
        assertEquals(
                "钓鱼竿",
                catalog().equipment(MiteMaterial.FLINT, EquipmentType.FISHING_ROD).chineseName());
    }
}
