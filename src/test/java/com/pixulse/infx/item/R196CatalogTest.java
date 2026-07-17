package com.pixulse.infx.item;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.tag.ModTags;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class R196CatalogTest {
    private static R196Catalog catalog() {
        return ModItems.catalog();
    }

    private static List<String> goldenPaths() throws IOException, URISyntaxException {
        return Files.readAllLines(
                Path.of(R196CatalogTest.class.getResource("/r196/catalog-paths.txt").toURI()), UTF_8);
    }

    @Test
    void catalogMatchesTheIndependentGoldenManifest() throws Exception {
        List<String> actual = catalog().entries().stream().map(R196Catalog.Entry::path).toList();
        assertEquals(goldenPaths(), actual);
        assertEquals(237, actual.size());
        assertEquals(237, new HashSet<>(actual).size());
        assertEquals(33, catalog().rawEntries().size());
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
    void aliasesPreserveTheNineExistingRegistryIds() {
        assertSame(ModItems.FLINT_CHIP, catalog().raw("flint_chip").holder());
        assertSame(ModItems.SINEW, catalog().raw("sinew").holder());
        assertSame(ModItems.OBSIDIAN_SHARD, catalog().raw("obsidian_shard").holder());
        assertSame(ModItems.EMERALD_SHARD, catalog().raw("emerald_shard").holder());
        assertSame(ModItems.SILVER_NUGGET, catalog().raw("silver_nugget").holder());
        assertSame(ModItems.MITHRIL_NUGGET, catalog().raw("mithril_nugget").holder());
        assertSame(ModItems.ADAMANTIUM_NUGGET, catalog().raw("adamantium_nugget").holder());
        assertSame(
                ModItems.FLINT_HATCHET,
                catalog().equipment(R196Material.FLINT, R196EquipmentType.HATCHET).holder());
        assertSame(
                ModItems.COPPER_PICKAXE,
                catalog().equipment(R196Material.COPPER, R196EquipmentType.PICKAXE).holder());
    }

    @Test
    void missingLookupsFailWithTheRequestedIdentity() {
        IllegalArgumentException raw =
                assertThrows(IllegalArgumentException.class, () -> catalog().raw("iron_coin"));
        assertTrue(raw.getMessage().contains("iron_coin"));
        IllegalArgumentException equipment = assertThrows(
                IllegalArgumentException.class,
                () -> catalog().equipment(R196Material.WOOD, R196EquipmentType.PICKAXE));
        assertTrue(equipment.getMessage().contains("wood_pickaxe"));
        assertFalse(catalog().entries().stream().anyMatch(entry -> entry.path().contains("diamond_helmet")));
    }

    @Test
    void specialtyFactoriesAreNotCollapsedToPlainItems() {
        assertEquals(
                R196ShearsItem.class,
                catalog().equipment(R196Material.COPPER, R196EquipmentType.SHEARS).itemClass());
        assertEquals(
                R196FishingRodItem.class,
                catalog().equipment(R196Material.FLINT, R196EquipmentType.FISHING_ROD).itemClass());
        assertEquals(
                R196ToolItem.class,
                catalog().equipment(R196Material.COPPER, R196EquipmentType.PICKAXE).itemClass());
    }

    @Test
    void projectileFactoriesRetainCatalogIdentity() {
        assertEquals(
                R196BowItem.class,
                catalog().equipment(R196Material.WOOD, R196EquipmentType.BOW).itemClass());
        assertEquals(
                R196ArrowItem.class,
                catalog().equipment(R196Material.ADAMANTIUM, R196EquipmentType.ARROW).itemClass());
    }

    @Test
    void orderedViewsAreStableForDataGenerationAndCreativeTabs() {
        assertEquals("flint_chip", catalog().rawEntries().getFirst().path());
        assertEquals("netherspawn_frags", catalog().rawEntries().getLast().path());
        assertEquals("leather_helmet", catalog().equipmentEntries().getFirst().path());
        assertEquals("adamantium_horse_armor", catalog().equipmentEntries().getLast().path());
        assertEquals(
                "repair_materials/rusted_iron",
                ModTags.Items.repairMaterial(R196Material.RUSTED_IRON).location().getPath());
        assertEquals(
                "equipment/war_hammer",
                ModTags.Items.equipmentType(R196EquipmentType.WAR_HAMMER).location().getPath());
    }

    @Test
    void everyDefinitionHasTwoNamesAndApprovedTerminology() {
        for (R196Catalog.Entry entry : catalog().entries()) {
            assertFalse(entry.englishName().isBlank(), entry.path());
            assertFalse(entry.chineseName().isBlank(), entry.path());
        }
        assertEquals(
                "InfiniteX Copper Pickaxe",
                catalog().equipment(R196Material.COPPER, R196EquipmentType.PICKAXE).englishName());
        assertEquals(
                "InfiniteX 铜镐",
                catalog().equipment(R196Material.COPPER, R196EquipmentType.PICKAXE).chineseName());
        assertEquals(
                "Ancient Metal War Hammer",
                catalog().equipment(R196Material.ANCIENT_METAL, R196EquipmentType.WAR_HAMMER).englishName());
        assertEquals(
                "远古金属锁链胸甲",
                catalog().equipment(R196Material.ANCIENT_METAL, R196EquipmentType.CHAINMAIL_CHESTPLATE).chineseName());
        assertEquals(
                "Gold Horse Armor",
                catalog().equipment(R196Material.GOLD, R196EquipmentType.HORSE_ARMOR).englishName());
        assertEquals("Bow", catalog().equipment(R196Material.WOOD, R196EquipmentType.BOW).englishName());
        assertEquals(
                "Fishing Rod",
                catalog().equipment(R196Material.ADAMANTIUM, R196EquipmentType.FISHING_ROD).englishName());
        assertEquals(
                "钓鱼竿",
                catalog().equipment(R196Material.FLINT, R196EquipmentType.FISHING_ROD).chineseName());
    }
}
