package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalInt;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class InfXVanillaItemStackLimitsTest {
    @Test
    void restoresVanillaMiteStackTiers() {
        assertLimit(1, Items.WRITTEN_BOOK);
        assertLimit(4, Items.MUSHROOM_STEW, Items.RABBIT_STEW, Items.BEETROOT_SOUP, Items.SUSPICIOUS_STEW);
        assertLimit(
                8,
                Items.BUCKET,
                Items.GLASS_BOTTLE,
                Items.BRICK,
                Items.NETHER_BRICK,
                Items.RESIN_BRICK,
                Items.IRON_INGOT,
                Items.COPPER_INGOT,
                Items.GOLD_INGOT,
                Items.NETHERITE_INGOT,
                Items.PUMPKIN_PIE);
        assertLimit(
                16,
                Items.APPLE,
                Items.ARROW,
                Items.SPECTRAL_ARROW,
                Items.TIPPED_ARROW,
                Items.BOWL,
                Items.BOOK,
                Items.MAP,
                Items.FILLED_MAP,
                Items.EXPERIENCE_BOTTLE,
                Items.ENDER_EYE);
        assertLimit(32, Items.DIAMOND, Items.EMERALD);
        assertLimit(
                64,
                Items.WHEAT_SEEDS,
                Items.PUMPKIN_SEEDS,
                Items.MELON_SEEDS,
                Items.TORCHFLOWER_SEEDS,
                Items.PITCHER_POD,
                Items.BEETROOT_SEEDS,
                Items.NETHER_WART,
                Items.PAPER,
                Items.GOLD_NUGGET,
                Items.IRON_NUGGET,
                Items.AMETHYST_SHARD,
                Items.PRISMARINE_SHARD,
                Items.ECHO_SHARD,
                Items.DISC_FRAGMENT_5);
    }

    @Test
    void coversVanillaNonBlockInventoryObjectsAndNonIdentityBlockItems() {
        for (Item item : BuiltInRegistries.ITEM) {
            var itemId = BuiltInRegistries.ITEM.getKey(item);
            if (!itemId.getNamespace().equals("minecraft")
                    || item == Items.AIR
                    || item == Items.STICK
                    || item == Items.BONE) {
                continue;
            }
            if (item instanceof BlockItem blockItem
                    && itemId.equals(BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()))
                    && item != Items.NETHER_WART) {
                continue;
            }
            OptionalInt limit = InfXVanillaItemStackLimits.limit(item, 64);
            assertTrue(limit.isPresent(), itemId.toString());
            assertTrue(Set.of(1, 4, 8, 16, 32, 64).contains(limit.getAsInt()), itemId.toString());
        }
    }

    @Test
    void preservesExistingStricterLimitsExceptFilledBowls() {
        assertEquals(OptionalInt.of(1), InfXVanillaItemStackLimits.limit(Items.WRITABLE_BOOK, 1));
        assertEquals(OptionalInt.of(1), InfXVanillaItemStackLimits.limit(Items.POTION, 1));
        assertEquals(OptionalInt.of(1), InfXVanillaItemStackLimits.limit(Items.MUSIC_DISC_13, 1));
        assertEquals(OptionalInt.of(4), InfXVanillaItemStackLimits.limit(Items.MUSHROOM_STEW, 1));
        assertEquals(OptionalInt.of(8), InfXVanillaItemStackLimits.limit(Items.BUCKET, 16));
    }

    private static void assertLimit(int expected, Item... items) {
        for (Item item : items) {
            assertEquals(
                    OptionalInt.of(expected),
                    InfXVanillaItemStackLimits.limit(item, 64),
                    () -> BuiltInRegistries.ITEM.getKey(item).toString());
        }
    }
}
