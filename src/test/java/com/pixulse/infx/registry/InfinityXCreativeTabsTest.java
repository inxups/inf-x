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

class InfinityXCreativeTabsTest {
    @Test
    void categoriesCoverEveryRegisteredItemExactlyOnce() {
        List<Identifier> categorized = Arrays.stream(InfinityXCreativeTabs.Category.values())
                .flatMap(category -> InfinityXCreativeTabs.items(category).stream())
                .map(item -> item.getId())
                .toList();
        Set<Identifier> uniqueCategorized = new HashSet<>(categorized);
        Set<Identifier> registered = InfinityXItems.ITEMS.getEntries().stream()
                .map(item -> item.getId())
                .collect(Collectors.toSet());

        // 395 baseline + 42 mob buckets + 7 powder-snow buckets + 4 fish spawn eggs + clay golem egg.
        assertEquals(449, registered.size());
        assertEquals(registered, uniqueCategorized);
        assertEquals(categorized.size(), uniqueCategorized.size(), "creative item appears in multiple categories");
    }

    @Test
    void categorySizesMatchTheCreativeInventoryDesign() {
        Map<InfinityXCreativeTabs.Category, Integer> expected = Map.of(
                InfinityXCreativeTabs.Category.BLOCKS, 45,
                InfinityXCreativeTabs.Category.INGREDIENTS, 31,
                InfinityXCreativeTabs.Category.FOOD_AND_CONSUMABLES, 24,
                InfinityXCreativeTabs.Category.TOOLS_AND_UTILITIES, 184,
                InfinityXCreativeTabs.Category.COMBAT_AND_EQUIPMENT, 113,
                InfinityXCreativeTabs.Category.SPAWN_EGGS, 52);

        expected.forEach((category, size) ->
                assertEquals(size, InfinityXCreativeTabs.items(category).size(), category.name()));
    }

    @Test
    void animalReplacementSpawnEggsAreInTheSpawnEggsCategory() {
        Set<String> eggPaths = InfinityXCreativeTabs.items(InfinityXCreativeTabs.Category.SPAWN_EGGS).stream()
                .map(item -> item.getId().getPath())
                .collect(Collectors.toSet());
        for (String animal : List.of(
                "r196_cow", "r196_chicken", "r196_sheep", "r196_pig", "r196_horse", "r196_ocelot", "r196_wolf")) {
            assertTrue(eggPaths.contains(animal + "_spawn_egg"), animal);
        }
    }

    @Test
    void everyPlayerObtainableBlockHasOneCreativeBlockItem() {
        Identifier underworldPortal = InfinityXBlocks.UNDERWORLD_PORTAL.getId();
        Identifier netherPortal = InfinityXBlocks.NETHER_PORTAL.getId();
        Identifier returnSpawnPortal = InfinityXBlocks.RETURN_SPAWN_PORTAL.getId();
        Identifier infestedNetherrack = InfinityXBlocks.INFESTED_NETHERRACK.getId();
        Set<Identifier> worldgenOnly = Set.of(
                underworldPortal, netherPortal, returnSpawnPortal, infestedNetherrack);
        Set<Identifier> expectedBlockItems = InfinityXBlocks.BLOCKS.getEntries().stream()
                .map(block -> block.getId())
                .filter(id -> !worldgenOnly.contains(id))
                .collect(Collectors.toSet());
        Set<Identifier> creativeBlocks = InfinityXCreativeTabs.items(InfinityXCreativeTabs.Category.BLOCKS).stream()
                .map(item -> item.getId())
                .collect(Collectors.toSet());
        Set<Identifier> registeredItems = InfinityXItems.ITEMS.getEntries().stream()
                .map(item -> item.getId())
                .collect(Collectors.toSet());

        assertEquals(49, InfinityXBlocks.BLOCKS.getEntries().size());
        assertEquals(expectedBlockItems, creativeBlocks);
        assertFalse(registeredItems.contains(underworldPortal), "Underworld portal must remain without a BlockItem");
        assertFalse(registeredItems.contains(netherPortal), "Nether portal must remain without a BlockItem");
        assertFalse(registeredItems.contains(returnSpawnPortal), "Return-spawn portal must remain without a BlockItem");
        assertFalse(registeredItems.contains(infestedNetherrack), "infested netherrack must remain worldgen-only");
        assertTrue(InfinityXItems.WORLD_BLOCKS.stream().allMatch(item -> creativeBlocks.contains(item.getId())));
    }
}
