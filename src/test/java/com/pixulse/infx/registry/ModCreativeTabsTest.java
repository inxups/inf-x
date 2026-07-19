package com.pixulse.infx.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class ModCreativeTabsTest {
    @Test
    void categoriesCoverEveryRegisteredItemExactlyOnce() {
        List<Identifier> categorized = Arrays.stream(ModCreativeTabs.Category.values())
                .flatMap(category -> ModCreativeTabs.items(category).stream())
                .map(item -> item.getId())
                .toList();
        Set<Identifier> uniqueCategorized = new HashSet<>(categorized);
        Set<Identifier> registered = ModItems.ITEMS.getEntries().stream()
                .map(item -> item.getId())
                .collect(Collectors.toSet());

        assertEquals(348, registered.size());
        assertEquals(registered, uniqueCategorized);
        assertEquals(categorized.size(), uniqueCategorized.size(), "creative item appears in multiple categories");
    }

    @Test
    void categorySizesMatchTheCreativeInventoryDesign() {
        Map<ModCreativeTabs.Category, Integer> expected = Map.of(
                ModCreativeTabs.Category.BLOCKS, 50,
                ModCreativeTabs.Category.INGREDIENTS, 31,
                ModCreativeTabs.Category.FOOD_AND_CONSUMABLES, 24,
                ModCreativeTabs.Category.TOOLS_AND_UTILITIES, 135,
                ModCreativeTabs.Category.COMBAT_AND_EQUIPMENT, 108);

        expected.forEach((category, size) ->
                assertEquals(size, ModCreativeTabs.items(category).size(), category.name()));
    }

    @Test
    void everyBlockExceptThePortalHasOneCreativeBlockItem() {
        Identifier portal = ModBlocks.UNDERWORLD_PORTAL.getId();
        Set<Identifier> expectedBlockItems = ModBlocks.BLOCKS.getEntries().stream()
                .map(block -> block.getId())
                .filter(id -> !id.equals(portal))
                .collect(Collectors.toSet());
        Set<Identifier> creativeBlocks = ModCreativeTabs.items(ModCreativeTabs.Category.BLOCKS).stream()
                .map(item -> item.getId())
                .collect(Collectors.toSet());
        Set<Identifier> registeredItems = ModItems.ITEMS.getEntries().stream()
                .map(item -> item.getId())
                .collect(Collectors.toSet());

        assertEquals(51, ModBlocks.BLOCKS.getEntries().size());
        assertEquals(expectedBlockItems, creativeBlocks);
        assertFalse(registeredItems.contains(portal), "Underworld portal must remain without a BlockItem");
        assertTrue(ModItems.WORLD_BLOCKS.stream().allMatch(item -> creativeBlocks.contains(item.getId())));
    }
}
