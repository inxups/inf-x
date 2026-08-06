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

        // 397 baseline + 22 stripped-log workbenches + 42 mob buckets + 7 powder-snow buckets
        // + 5 additional spawn eggs + 9 carrot-on-a-stick + 9 warped-fungus-on-a-stick items.
        assertEquals(493, registered.size());
        assertEquals(registered, uniqueCategorized);
        assertEquals(categorized.size(), uniqueCategorized.size(), "creative item appears in multiple categories");
    }

    @Test
    void categorySizesMatchTheCreativeInventoryDesign() {
        Map<InfXCreativeTabs.Category, Integer> expected = Map.of(
                InfXCreativeTabs.Category.BLOCKS, 69,
                InfXCreativeTabs.Category.INGREDIENTS, 31,
                InfXCreativeTabs.Category.FOOD_AND_CONSUMABLES, 24,
                InfXCreativeTabs.Category.TOOLS_AND_UTILITIES, 202,
                InfXCreativeTabs.Category.COMBAT_AND_EQUIPMENT, 113,
                InfXCreativeTabs.Category.SPAWN_EGGS, 54);

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
                InfXBlocks.INFX_BEETROOTS.getId(),
                // The onion crop has no separate block item: the onion item itself is its seed
                // and is listed in the food tab, exactly like MITE.
                InfXBlocks.INFX_ONION.getId());
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

        assertEquals(78, InfXBlocks.BLOCKS.getEntries().size());
        assertFalse(InfXBlocks.BLOCKS.getEntries().stream()
                .anyMatch(block -> Set.of("flint_workbench", "obsidian_workbench").contains(block.getId().getPath())));
        assertFalse(InfXItems.ITEMS.getEntries().stream()
                .anyMatch(item -> Set.of("flint_workbench", "obsidian_workbench").contains(item.getId().getPath())));
        assertEquals(expectedBlockItems, creativeBlocks);
        assertFalse(registeredItems.contains(underworldPortal), "Underworld portal must remain without a BlockItem");
        assertFalse(registeredItems.contains(netherPortal), "Nether portal must remain without a BlockItem");
        assertFalse(registeredItems.contains(returnSpawnPortal), "Return-spawn portal must remain without a BlockItem");
        assertFalse(registeredItems.contains(infestedNetherrack), "infested netherrack must remain worldgen-only");
        for (Identifier crop : Set.of(
                InfXBlocks.INFX_WHEAT.getId(),
                InfXBlocks.INFX_CARROTS.getId(),
                InfXBlocks.INFX_POTATOES.getId(),
                InfXBlocks.INFX_BEETROOTS.getId(),
                InfXBlocks.INFX_ONION.getId())) {
            assertFalse(registeredItems.contains(crop), crop + " must remain seed-placed only");
        }
        assertTrue(InfXItems.WORLD_BLOCKS.stream().allMatch(item -> creativeBlocks.contains(item.getId())));
    }
}
