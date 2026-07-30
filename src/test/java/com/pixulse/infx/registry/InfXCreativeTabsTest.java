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
import net.neoforged.neoforge.registries.DeferredHolder;
import org.junit.jupiter.api.Test;

class InfXCreativeTabsTest {
    @Test
    void categoriesCoverEveryRegisteredItemExactlyOnce() {
        List<Identifier> categorized = Arrays.stream(InfXCreativeTabs.Category.values())
                .flatMap(category -> InfXCreativeTabs.items(category).stream())
                .map(DeferredHolder::getId)
                .toList();
        Set<Identifier> uniqueCategorized = new HashSet<>(categorized);
        Set<Identifier> registered = InfXItems.ITEMS.getEntries().stream()
                .map(DeferredHolder::getId)
                .collect(Collectors.toSet());

        // 400 baseline + 42 mob buckets + 7 powder-snow buckets + 4 fish spawn eggs, clay golem and bat eggs.
        assertEquals(455, registered.size());
        assertEquals(registered, uniqueCategorized);
        assertEquals(categorized.size(), uniqueCategorized.size(), "creative item appears in multiple categories");
    }

    @Test
    void categorySizesMatchTheCreativeInventoryDesign() {
        Map<InfXCreativeTabs.Category, Integer> expected = Map.of(
                InfXCreativeTabs.Category.BLOCKS, 50,
                InfXCreativeTabs.Category.INGREDIENTS, 31,
                InfXCreativeTabs.Category.FOOD_AND_CONSUMABLES, 24,
                InfXCreativeTabs.Category.TOOLS_AND_UTILITIES, 184,
                InfXCreativeTabs.Category.COMBAT_AND_EQUIPMENT, 113,
                InfXCreativeTabs.Category.SPAWN_EGGS, 53);

        expected.forEach((category, size) ->
                assertEquals(size, InfXCreativeTabs.items(category).size(), category.name()));
    }

    @Test
    void animalReplacementSpawnEggsAreInTheSpawnEggsCategory() {
        Set<String> eggPaths = InfXCreativeTabs.items(InfXCreativeTabs.Category.SPAWN_EGGS).stream()
                .map(item -> item.getId().getPath())
                .collect(Collectors.toSet());
        for (String animal : List.of(
                "infx_cow", "infx_chicken", "infx_sheep", "infx_pig", "infx_horse", "infx_ocelot", "infx_wolf")) {
            assertTrue(eggPaths.contains(animal + "_spawn_egg"), animal);
        }
    }

    @Test
    void everyPlayerObtainableBlockHasOneCreativeBlockItem() {
        Identifier underworldPortal = InfXBlocks.UNDERWORLD_PORTAL.getId();
        Identifier netherPortal = InfXBlocks.NETHER_PORTAL.getId();
        Identifier returnSpawnPortal = InfXBlocks.RETURN_SPAWN_PORTAL.getId();
        Identifier infestedNetherrack = InfXBlocks.INFESTED_NETHERRACK.getId();
        Set<Identifier> unobtainableBlocks = Set.of(
                underworldPortal,
                netherPortal,
                returnSpawnPortal,
                infestedNetherrack,
                InfXBlocks.INFX_WHEAT.getId(),
                InfXBlocks.INFX_CARROTS.getId(),
                InfXBlocks.INFX_POTATOES.getId(),
                InfXBlocks.INFX_BEETROOTS.getId());
        Set<Identifier> expectedBlockItems = InfXBlocks.BLOCKS.getEntries().stream()
                .map(DeferredHolder::getId)
                .filter(id -> !unobtainableBlocks.contains(id))
                .collect(Collectors.toSet());
        Set<Identifier> creativeBlocks = InfXCreativeTabs.items(InfXCreativeTabs.Category.BLOCKS).stream()
                .map(DeferredHolder::getId)
                .collect(Collectors.toSet());
        Set<Identifier> registeredItems = InfXItems.ITEMS.getEntries().stream()
                .map(DeferredHolder::getId)
                .collect(Collectors.toSet());

        assertEquals(58, InfXBlocks.BLOCKS.getEntries().size());
        assertEquals(expectedBlockItems, creativeBlocks);
        assertFalse(registeredItems.contains(underworldPortal), "Underworld portal must remain without a BlockItem");
        assertFalse(registeredItems.contains(netherPortal), "Nether portal must remain without a BlockItem");
        assertFalse(registeredItems.contains(returnSpawnPortal), "Return-spawn portal must remain without a BlockItem");
        assertFalse(registeredItems.contains(infestedNetherrack), "infested netherrack must remain worldgen-only");
        for (Identifier crop : Set.of(
                InfXBlocks.INFX_WHEAT.getId(),
                InfXBlocks.INFX_CARROTS.getId(),
                InfXBlocks.INFX_POTATOES.getId(),
                InfXBlocks.INFX_BEETROOTS.getId())) {
            assertFalse(registeredItems.contains(crop), crop + " must remain seed-placed only");
        }
        assertTrue(InfXItems.WORLD_BLOCKS.stream().allMatch(item -> creativeBlocks.contains(item.getId())));
    }
}
