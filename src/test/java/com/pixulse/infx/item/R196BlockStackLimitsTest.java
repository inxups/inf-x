package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class R196BlockStackLimitsTest {
    @Test
    void restoresEveryExplicitMiteStackTier() {
        assertLimit(1, Items.FURNACE, Items.ANVIL, Items.OAK_DOOR, Items.BED.red());
        assertLimit(4, Items.STONE, Items.CHEST, Items.CRAFTING_TABLE, Items.DIAMOND_BLOCK);
        assertLimit(
                8,
                Items.OAK_PLANKS,
                Items.WOOL.white(),
                Items.CARPET.white(),
                Items.STONE_PRESSURE_PLATE,
                Items.OAK_FENCE,
                Items.LADDER,
                Items.MELON,
                Items.PUMPKIN,
                Items.RAIL,
                Items.STONE_SLAB,
                Items.VINE,
                Items.COBBLESTONE_WALL,
                Items.CAKE);
        assertLimit(
                16,
                Items.GLASS_PANE,
                Items.IRON_BARS,
                Items.OAK_SAPLING,
                Items.TORCH,
                Items.REDSTONE_TORCH,
                Items.OAK_SIGN,
                Items.PLAYER_HEAD,
                Items.SUGAR_CANE,
                Items.REPEATER,
                Items.COMPARATOR,
                Items.BREWING_STAND,
                Items.FLOWER_POT);
        assertLimit(
                32,
                Items.DANDELION,
                Items.BROWN_MUSHROOM,
                Items.SHORT_GRASS,
                Items.LILY_PAD,
                Items.SNOW);
    }

    @Test
    void extrapolatesModernBlocksFromTheNearestMiteFamily() {
        assertLimit(1, Items.BLAST_FURNACE, Items.SMOKER, Items.CHERRY_DOOR);
        assertLimit(4, Items.CRAFTER, Items.CHERRY_STAIRS);
        assertLimit(8, Items.CHERRY_PLANKS, Items.CHERRY_SLAB, Items.CHERRY_FENCE);
        assertLimit(16, Items.CHERRY_SAPLING, Items.CHERRY_SIGN, Items.SOUL_TORCH);
        assertLimit(32, Items.OPEN_EYEBLOSSOM);
    }

    @Test
    void keepsStricterModernSafetyLimits() {
        assertLimitWithCurrent(1, 1, Items.SHULKER_BOX, Items.DYED_SHULKER_BOX.white());
    }

    @Test
    void excludesItemsThatPlantBlocksButAreNotBlockInventoryObjects() {
        assertNotTargeted(Items.WHEAT_SEEDS, Items.CARROT, Items.NETHER_WART, Items.REDSTONE, Items.APPLE);
    }

    @Test
    void coversEveryVanillaBlockInventoryObject() {
        Set<Identifier> uncovered = new HashSet<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (!(item instanceof BlockItem blockItem)) continue;
            Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
            Identifier blockId = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
            if (!itemId.getNamespace().equals("minecraft") || !itemId.equals(blockId)) continue;
            OptionalInt limit = R196BlockStackLimits.limit(item, 64);
            if (limit.isEmpty()) {
                uncovered.add(itemId);
            } else {
                assertTrue(Set.of(1, 4, 8, 16, 32).contains(limit.getAsInt()), itemId.toString());
            }
        }
        assertEquals(Set.of(Identifier.withDefaultNamespace("nether_wart")), uncovered);
    }

    private static void assertLimit(int expected, Item... items) {
        for (Item item : items) {
            assertEquals(
                    OptionalInt.of(expected),
                    R196BlockStackLimits.limit(item, 64),
                    () -> BuiltInRegistries.ITEM.getKey(item).toString());
        }
    }

    private static void assertLimitWithCurrent(int expected, int current, Item... items) {
        for (Item item : items) {
            assertEquals(
                    OptionalInt.of(expected),
                    R196BlockStackLimits.limit(item, current),
                    () -> BuiltInRegistries.ITEM.getKey(item).toString());
        }
    }

    private static void assertNotTargeted(Item... items) {
        for (Item item : items) {
            assertEquals(
                    OptionalInt.empty(),
                    R196BlockStackLimits.limit(item, 64),
                    () -> BuiltInRegistries.ITEM.getKey(item).toString());
        }
    }
}
