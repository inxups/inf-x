package com.pixulse.infx.item;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.registry.tag.InfXItemTags;

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
        return InfXItems.catalog();
    }

    private static List<String> goldenPaths() throws IOException, URISyntaxException {
        return Files.readAllLines(
                Path.of(CatalogTest.class.getResource("/infx/catalog-paths.txt").toURI()), UTF_8);
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
        assertSame(InfXItems.FLINT_CHIP, catalog().raw("flint_chip").holder());
        assertSame(InfXItems.SINEW, catalog().raw("sinew").holder());
        assertSame(InfXItems.OBSIDIAN_SHARD, catalog().raw("obsidian_shard").holder());
        assertSame(InfXItems.EMERALD_SHARD, catalog().raw("emerald_shard").holder());
        assertSame(InfXItems.SILVER_NUGGET, catalog().raw("silver_nugget").holder());
        assertSame(InfXItems.MITHRIL_NUGGET, catalog().raw("mithril_nugget").holder());
        assertSame(InfXItems.ADAMANTIUM_NUGGET, catalog().raw("adamantium_nugget").holder());
        assertSame(
                InfXItems.FLINT_HATCHET,
                catalog().equipment(InfxMaterial.FLINT, EquipmentType.HATCHET).holder());
        assertSame(
                InfXItems.FLINT_SHOVEL,
                catalog().equipment(InfxMaterial.FLINT, EquipmentType.SHOVEL).holder());
        assertSame(
                InfXItems.FLINT_AXE,
                catalog().equipment(InfxMaterial.FLINT, EquipmentType.AXE).holder());
        assertSame(
                InfXItems.COPPER_PICKAXE,
                catalog().equipment(InfxMaterial.COPPER, EquipmentType.PICKAXE).holder());
        assertSame(
                InfXItems.COPPER_SHOVEL,
                catalog().equipment(InfxMaterial.COPPER, EquipmentType.SHOVEL).holder());
        assertSame(
                InfXItems.COPPER_AXE,
                catalog().equipment(InfxMaterial.COPPER, EquipmentType.AXE).holder());
        assertSame(
                InfXItems.COPPER_HOE,
                catalog().equipment(InfxMaterial.COPPER, EquipmentType.HOE).holder());
        assertSame(
                InfXItems.COPPER_SWORD,
                catalog().equipment(InfxMaterial.COPPER, EquipmentType.SWORD).holder());
        assertSame(
                InfXItems.IRON_PICKAXE,
                catalog().equipment(InfxMaterial.IRON, EquipmentType.PICKAXE).holder());
        assertSame(
                InfXItems.IRON_SHOVEL,
                catalog().equipment(InfxMaterial.IRON, EquipmentType.SHOVEL).holder());
        assertSame(
                InfXItems.IRON_AXE,
                catalog().equipment(InfxMaterial.IRON, EquipmentType.AXE).holder());
        assertSame(
                InfXItems.IRON_HOE,
                catalog().equipment(InfxMaterial.IRON, EquipmentType.HOE).holder());
        assertSame(
                InfXItems.IRON_SWORD,
                catalog().equipment(InfxMaterial.IRON, EquipmentType.SWORD).holder());
    }

    @Test
    void missingLookupsFailWithTheRequestedIdentity() {
        IllegalArgumentException raw =
                assertThrows(IllegalArgumentException.class, () -> catalog().raw("iron_coin"));
        assertTrue(raw.getMessage().contains("iron_coin"));
        IllegalArgumentException equipment = assertThrows(
                IllegalArgumentException.class,
                () -> catalog().equipment(InfxMaterial.WOOD, EquipmentType.PICKAXE));
        assertTrue(equipment.getMessage().contains("wood_pickaxe"));
        assertFalse(catalog().entries().stream().anyMatch(entry -> entry.path().contains("diamond_helmet")));
        assertFalse(catalog().entries().stream().anyMatch(entry -> entry.path().endsWith("_frags")));
    }

    @Test
    void specialtyFactoriesAreNotCollapsedToPlainItems() {
        assertEquals(
                InfxShearsItem.class,
                catalog().equipment(InfxMaterial.COPPER, EquipmentType.SHEARS).itemClass());
        assertEquals(
                InfxFishingRodItem.class,
                catalog().equipment(InfxMaterial.FLINT, EquipmentType.FISHING_ROD).itemClass());
        assertEquals(
                ToolItem.class,
                catalog().equipment(InfxMaterial.COPPER, EquipmentType.PICKAXE).itemClass());
    }

    @Test
    void projectileFactoriesRetainCatalogIdentity() {
        assertEquals(
                InfxBowItem.class,
                catalog().equipment(InfxMaterial.WOOD, EquipmentType.BOW).itemClass());
        assertEquals(
                InfxArrowItem.class,
                catalog().equipment(InfxMaterial.ADAMANTIUM, EquipmentType.ARROW).itemClass());
    }

    @Test
    void orderedViewsAreStableForDataGenerationAndCreativeTabs() {
        assertEquals("flint_chip", catalog().rawEntries().getFirst().path());
        assertEquals("adamantium_coin", catalog().rawEntries().getLast().path());
        assertEquals("leather_helmet", catalog().equipmentEntries().getFirst().path());
        assertEquals("adamantium_horse_armor", catalog().equipmentEntries().getLast().path());
        assertEquals(
                "repair_materials/rusted_iron",
                InfXItemTags.repairMaterial(InfxMaterial.RUSTED_IRON).location().getPath());
        assertEquals(
                "equipment/war_hammer",
                InfXItemTags.equipmentType(EquipmentType.WAR_HAMMER).location().getPath());
    }

    @Test
    void everyDefinitionHasTwoNamesAndApprovedTerminology() {
        for (Catalog.Entry entry : catalog().entries()) {
            assertFalse(entry.englishName().isBlank(), entry.path());
            assertFalse(entry.chineseName().isBlank(), entry.path());
        }
        assertEquals(
                "Copper Pickaxe",
                catalog().equipment(InfxMaterial.COPPER, EquipmentType.PICKAXE).englishName());
        assertEquals(
                "铜镐",
                catalog().equipment(InfxMaterial.COPPER, EquipmentType.PICKAXE).chineseName());
        assertEquals(
                "Ancient Metal War Hammer",
                catalog().equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.WAR_HAMMER).englishName());
        assertEquals(
                "远古金属锁链胸甲",
                catalog().equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.CHAINMAIL_CHESTPLATE).chineseName());
        assertEquals(
                "Gold Horse Armor",
                catalog().equipment(InfxMaterial.GOLD, EquipmentType.HORSE_ARMOR).englishName());
        assertEquals("Bow", catalog().equipment(InfxMaterial.WOOD, EquipmentType.BOW).englishName());
        assertEquals(
                "Fishing Rod",
                catalog().equipment(InfxMaterial.ADAMANTIUM, EquipmentType.FISHING_ROD).englishName());
        assertEquals(
                "钓鱼竿",
                catalog().equipment(InfxMaterial.FLINT, EquipmentType.FISHING_ROD).chineseName());
    }
}
