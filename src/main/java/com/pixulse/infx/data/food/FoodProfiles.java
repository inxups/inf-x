package com.pixulse.infx.data.food;

import com.pixulse.infx.item.MiteBucketItem;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * R196 food values transcribed from Item, ItemMeat, ItemSeedFood and BlockCake. Modern-only food
 * items use the nearest R196 counterpart rather than a new independent nutrition model.
 */
public final class FoodProfiles {
    private static final FoodProfile APPLE = mite(2, 1, 1_000, false, false, true);
    private static final FoodProfile MUSHROOM_STEW = mite(2, 4, 0, false, false, false);
    private static final FoodProfile BREAD = mite(8, 2, 0, false, false, false);
    private static final FoodProfile RAW_PORK = mite(4, 4, 0, true, false, false);
    private static final FoodProfile COOKED_PORK = mite(8, 8, 0, true, false, false);
    private static final FoodProfile GOLDEN_APPLE = mite(2, 1, 1_000, false, false, true, 0, true);
    private static final FoodProfile EGG = mite(1, 3, 0, true, false, false);
    private static final FoodProfile RAW_COD = mite(3, 3, 0, true, true, false);
    private static final FoodProfile COOKED_COD = mite(6, 6, 0, true, true, false);
    private static final FoodProfile RAW_SALMON = mite(5, 5, 0, true, true, false);
    private static final FoodProfile COOKED_SALMON = mite(10, 10, 0, true, true, false);
    private static final FoodProfile SUGAR = mite(1, 0, 1_000, false, false, false);
    private static final FoodProfile COOKIE = mite(3, 1, 250, false, false, false);
    private static final FoodProfile MELON = mite(1, 1, 1_000, false, false, true);
    private static final FoodProfile RAW_BEEF = mite(5, 5, 0, true, false, false);
    private static final FoodProfile COOKED_BEEF = mite(10, 10, 0, true, false, false);
    private static final FoodProfile RAW_CHICKEN = mite(3, 3, 0, true, false, false);
    private static final FoodProfile COOKED_CHICKEN = mite(6, 6, 0, true, false, false);
    private static final FoodProfile ROTTEN_FLESH = mite(2, 1, 0, true, false, false);
    private static final FoodProfile SPIDER_EYE = mite(0, 1, 0, true, false, false);
    private static final FoodProfile CARROT = mite(1, 2, 0, false, false, true);
    private static final FoodProfile POTATO = mite(3, 1, 0, false, false, false);
    private static final FoodProfile BAKED_POTATO = mite(6, 2, 0, false, false, false);
    private static final FoodProfile POISONOUS_POTATO = mite(2, 0, 0, false, false, false);
    private static final FoodProfile PUMPKIN_PIE = mite(10, 6, 1_000, true, false, true);
    private static final FoodProfile MUSHROOM = mite(1, 1, 0, false, false, false);
    private static final FoodProfile DRIED_KELP = mite(0, 1, 0, false, false, false);
    private static final FoodProfile WHEAT_SEEDS = mite(1, 0, 0, false, true, false, 2_000, false);
    private static final FoodProfile PUMPKIN_SEEDS = mite(1, 2, 0, false, true, false);
    private static final FoodProfile MELON_SEEDS = mite(1, 1, 0, false, true, false);
    private static final FoodProfile NETHER_WART = mite(1, 1, 0, false, false, false);
    private static final FoodProfile MILK_BOWL = mite(0, 1, 0, true, false, false, 0, true);
    private static final FoodProfile MILK_BUCKET = mite(0, 4, 0, true, false, false, 0, true);
    private static final FoodProfile RAW_LAMB = mite(3, 3, 0, true, false, false);
    private static final FoodProfile COOKED_LAMB = mite(6, 6, 0, true, false, false);
    private static final FoodProfile CHEESE = mite(3, 3, 0, true, false, false);
    private static final FoodProfile DOUGH = mite(6, 2, 0, false, false, false);
    private static final FoodProfile CHOCOLATE = mite(3, 3, 1_000, false, false, false);
    private static final FoodProfile ONION = mite(1, 1, 0, false, false, true);
    private static final FoodProfile BEEF_STEW = mite(16, 16, 0, true, false, true);
    private static final FoodProfile CHICKEN_SOUP = mite(10, 10, 0, true, false, true);
    private static final FoodProfile VEGETABLE_SOUP = mite(6, 6, 0, false, false, true);
    private static final FoodProfile ICE_CREAM = mite(5, 4, 1_000, true, false, false);
    private static final FoodProfile SALAD = mite(1, 1, 0, false, false, true);
    private static final FoodProfile CREAM_OF_MUSHROOM_SOUP = mite(3, 5, 0, true, false, false);
    private static final FoodProfile CREAM_OF_VEGETABLE_SOUP = mite(7, 7, 0, true, false, true);
    private static final FoodProfile PUMPKIN_SOUP = mite(1, 2, 0, false, false, true);
    private static final FoodProfile ORANGE = mite(2, 1, 1_000, false, false, true);
    private static final FoodProfile MASHED_POTATO = mite(12, 8, 0, true, false, false);
    private static final FoodProfile FRUIT_ICE = mite(4, 2, 2_000, false, false, true);
    private static final FoodProfile BLUEBERRIES = mite(1, 1, 1_000, false, false, true);
    private static final FoodProfile BLUEBERRY_PORRIDGE = mite(4, 2, 2_000, false, false, true);
    private static final FoodProfile CEREAL_PORRIDGE = mite(4, 2, 1_000, true, false, false);
    private static final FoodProfile WORM = mite(0, 1, 0, true, false, false);
    private static final FoodProfile COOKED_WORM = mite(1, 1, 0, true, false, false);
    private static final FoodProfile CAKE_SLICE = mite(2, 2, 1_000 / 6, true, false, false);

    private FoodProfiles() {}

    public static FoodProfile cakeSlice() {
        return CAKE_SLICE;
    }

    public static FoodProfile forStack(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof MiteBucketItem bucket && bucket.contents() == MiteBucketItem.Contents.MILK) {
            return MILK_BUCKET;
        }
        if (item == Items.MILK_BUCKET) return MILK_BUCKET;

        if (item == Items.APPLE) return APPLE;
        if (item == Items.MUSHROOM_STEW || item == Items.RABBIT_STEW || item == Items.BEETROOT_SOUP) {
            return MUSHROOM_STEW;
        }
        if (item == Items.BREAD) return BREAD;
        if (item == Items.PORKCHOP) return RAW_PORK;
        if (item == Items.COOKED_PORKCHOP) return COOKED_PORK;
        if (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE) return GOLDEN_APPLE;
        if (item == Items.EGG) return EGG;
        if (item == Items.COD) return RAW_COD;
        if (item == Items.COOKED_COD) return COOKED_COD;
        if (item == Items.SALMON) return RAW_SALMON;
        if (item == Items.COOKED_SALMON) return COOKED_SALMON;
        if (item == Items.SUGAR) return SUGAR;
        if (item == Items.CAKE) return CAKE_SLICE;
        if (item == Items.COOKIE) return COOKIE;
        if (item == Items.MELON_SLICE) return MELON;
        if (item == Items.BEEF) return RAW_BEEF;
        if (item == Items.COOKED_BEEF) return COOKED_BEEF;
        if (item == Items.CHICKEN) return RAW_CHICKEN;
        if (item == Items.COOKED_CHICKEN) return COOKED_CHICKEN;
        if (item == Items.ROTTEN_FLESH) return ROTTEN_FLESH;
        if (item == Items.SPIDER_EYE) return SPIDER_EYE;
        if (item == Items.CARROT || item == Items.GOLDEN_CARROT || item == Items.BEETROOT) return CARROT;
        if (item == Items.POTATO) return POTATO;
        if (item == Items.BAKED_POTATO) return BAKED_POTATO;
        if (item == Items.POISONOUS_POTATO) return POISONOUS_POTATO;
        if (item == Items.PUMPKIN_PIE) return PUMPKIN_PIE;
        if (item == Items.BROWN_MUSHROOM || item == Items.RED_MUSHROOM) return MUSHROOM;
        if (item == Items.WHEAT_SEEDS) return WHEAT_SEEDS;
        if (item == Items.PUMPKIN_SEEDS) return PUMPKIN_SEEDS;
        if (item == Items.MELON_SEEDS) return MELON_SEEDS;
        if (item == Items.BEETROOT_SEEDS) return MELON_SEEDS;
        if (item == Items.NETHER_WART) return NETHER_WART;
        if (item == Items.MUTTON || item == Items.RABBIT) return RAW_LAMB;
        if (item == Items.COOKED_MUTTON || item == Items.COOKED_RABBIT) return COOKED_LAMB;
        if (item == Items.TROPICAL_FISH || item == Items.PUFFERFISH) return RAW_COD;
        if (item == Items.SWEET_BERRIES || item == Items.GLOW_BERRIES) return BLUEBERRIES;
        if (item == Items.DRIED_KELP) return DRIED_KELP;
        if (item == Items.CHORUS_FRUIT) return ORANGE;
        if (item == Items.HONEY_BOTTLE) return SUGAR;

        if (item == InfXItems.DOUGH.get()) return DOUGH;
        if (item == InfXItems.SALAD.get()) return SALAD;
        if (item == InfXItems.BLUEBERRIES.get()) return BLUEBERRIES;
        if (item == InfXItems.BLUEBERRY_PORRIDGE.get()) return BLUEBERRY_PORRIDGE;
        if (item == InfXItems.MILK_BOWL.get()) return MILK_BOWL;
        if (item == InfXItems.CEREAL_PORRIDGE.get()) return CEREAL_PORRIDGE;
        if (item == InfXItems.CHOCOLATE.get()) return CHOCOLATE;
        if (item == InfXItems.PUMPKIN_SOUP.get()) return PUMPKIN_SOUP;
        if (item == InfXItems.CREAM_OF_MUSHROOM_SOUP.get()) return CREAM_OF_MUSHROOM_SOUP;
        if (item == InfXItems.ONION.get()) return ONION;
        if (item == InfXItems.VEGETABLE_SOUP.get()) return VEGETABLE_SOUP;
        if (item == InfXItems.CREAM_OF_VEGETABLE_SOUP.get()) return CREAM_OF_VEGETABLE_SOUP;
        if (item == InfXItems.CHICKEN_SOUP.get()) return CHICKEN_SOUP;
        if (item == InfXItems.BEEF_STEW.get()) return BEEF_STEW;
        if (item == InfXItems.ORANGE.get() || item == InfXItems.BANANA.get()) return ORANGE;
        if (item == InfXItems.FRUIT_ICE.get()) return FRUIT_ICE;
        if (item == InfXItems.CHEESE.get()) return CHEESE;
        if (item == InfXItems.MASHED_POTATO.get()) return MASHED_POTATO;
        if (item == InfXItems.ICE_CREAM.get()) return ICE_CREAM;
        if (item == InfXItems.WORM.get()) return WORM;
        if (item == InfXItems.COOKED_WORM.get()) return COOKED_WORM;
        return FoodProfile.EMPTY;
    }

    private static FoodProfile mite(
            double satiation,
            double nutrition,
            int sugarContent,
            boolean hasProtein,
            boolean hasEssentialFats,
            boolean hasPhytonutrients) {
        return FoodProfile.mite(
                satiation,
                nutrition,
                sugarContent,
                hasProtein,
                hasEssentialFats,
                hasPhytonutrients);
    }

    private static FoodProfile mite(
            double satiation,
            double nutrition,
            int sugarContent,
            boolean hasProtein,
            boolean hasEssentialFats,
            boolean hasPhytonutrients,
            int extraEssentialFats,
            boolean alwaysEdible) {
        return FoodProfile.mite(
                satiation,
                nutrition,
                sugarContent,
                hasProtein,
                hasEssentialFats,
                hasPhytonutrients,
                extraEssentialFats,
                alwaysEdible);
    }
}
