package com.pixulse.infx.survival;

import com.pixulse.infx.registry.ModItems;
import java.util.Set;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Nutrient metadata kept separate from modern {@code FoodProperties}. */
public final class R196FoodProfiles {
    private static final Set<Item> RAW_MEAT = Set.of(
            Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.MUTTON, Items.RABBIT, Items.COD, Items.SALMON);
    private static final Set<Item> COOKED_MEAT = Set.of(
            Items.COOKED_BEEF,
            Items.COOKED_PORKCHOP,
            Items.COOKED_CHICKEN,
            Items.COOKED_MUTTON,
            Items.COOKED_RABBIT,
            Items.COOKED_COD,
            Items.COOKED_SALMON);
    private static final Set<Item> VEGETABLES = Set.of(
            Items.CARROT,
            Items.POTATO,
            Items.BAKED_POTATO,
            Items.BEETROOT,
            Items.MELON_SLICE,
            Items.SWEET_BERRIES,
            Items.GLOW_BERRIES,
            Items.APPLE);
    private static final Set<Item> SWEETS = Set.of(
            Items.SUGAR,
            Items.COOKIE,
            Items.CAKE,
            Items.PUMPKIN_PIE,
            Items.HONEY_BOTTLE,
            Items.GOLDEN_APPLE,
            Items.ENCHANTED_GOLDEN_APPLE);

    private R196FoodProfiles() {}

    public static R196FoodProfile forStack(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.EGG) return new R196FoodProfile(1, 3, 12_000, 0, 2_000, 0);
        if (RAW_MEAT.contains(item)) return new R196FoodProfile(1, 3, 10_000, 0, 4_000, 0);
        if (COOKED_MEAT.contains(item)) return new R196FoodProfile(3, 6, 22_000, 0, 8_000, 0);
        if (VEGETABLES.contains(item)) return new R196FoodProfile(2, 4, 0, 18_000, 1_000, 2_000);
        if (SWEETS.contains(item)) return new R196FoodProfile(4, 5, 0, 1_000, 2_000, 18_000);
        if (item == Items.BROWN_MUSHROOM) return new R196FoodProfile(1, 2, 1_000, 8_000, 0, 0);
        if (item == Items.RED_MUSHROOM) return new R196FoodProfile(1, 2, 1_000, 6_000, 0, 0);
        if (item == Items.WHEAT_SEEDS || item == Items.PUMPKIN_SEEDS || item == Items.MELON_SEEDS
                || item == Items.BEETROOT_SEEDS || item == Items.NETHER_WART) {
            return new R196FoodProfile(0.5, 1, 2_000, 3_000, 1_000, 0);
        }
        if (item == ModItems.DOUGH.get()) return new R196FoodProfile(1, 2, 2_000, 1_000, 0, 4_000);
        if (item == ModItems.SALAD.get()) return new R196FoodProfile(3, 7, 2_000, 34_000, 4_000, 0);
        if (item == ModItems.BLUEBERRIES.get()) return new R196FoodProfile(1, 3, 0, 14_000, 0, 5_000);
        if (item == ModItems.BLUEBERRY_PORRIDGE.get()) return new R196FoodProfile(4, 8, 5_000, 20_000, 2_000, 8_000);
        if (item == ModItems.MILK_BOWL.get()) return new R196FoodProfile(3, 5, 14_000, 0, 12_000, 4_000);
        if (item == ModItems.CEREAL_PORRIDGE.get()) return new R196FoodProfile(4, 8, 8_000, 8_000, 3_000, 6_000);
        if (item == ModItems.CHOCOLATE.get()) return new R196FoodProfile(4, 5, 2_000, 0, 8_000, 24_000);
        if (item == ModItems.PUMPKIN_SOUP.get()) return new R196FoodProfile(3, 8, 2_000, 28_000, 2_000, 2_000);
        if (item == ModItems.CREAM_OF_MUSHROOM_SOUP.get()) return new R196FoodProfile(4, 9, 8_000, 18_000, 9_000, 3_000);
        if (item == ModItems.ONION.get()) return new R196FoodProfile(1, 3, 0, 16_000, 0, 1_000);
        if (item == ModItems.VEGETABLE_SOUP.get()) return new R196FoodProfile(4, 9, 2_000, 36_000, 2_000, 0);
        if (item == ModItems.CREAM_OF_VEGETABLE_SOUP.get()) return new R196FoodProfile(5, 10, 8_000, 38_000, 9_000, 3_000);
        if (item == ModItems.CHICKEN_SOUP.get()) return new R196FoodProfile(5, 10, 24_000, 12_000, 7_000, 0);
        if (item == ModItems.BEEF_STEW.get()) return new R196FoodProfile(6, 12, 30_000, 10_000, 10_000, 0);
        if (item == ModItems.ORANGE.get()) return new R196FoodProfile(2, 5, 0, 20_000, 0, 7_000);
        if (item == ModItems.FRUIT_ICE.get()) return new R196FoodProfile(3, 5, 0, 10_000, 0, 18_000);
        if (item == ModItems.CHEESE.get()) return new R196FoodProfile(4, 7, 20_000, 0, 20_000, 1_000);
        if (item == ModItems.MASHED_POTATO.get()) return new R196FoodProfile(4, 9, 4_000, 12_000, 6_000, 3_000);
        if (item == ModItems.ICE_CREAM.get()) return new R196FoodProfile(4, 7, 8_000, 0, 10_000, 22_000);
        if (item == ModItems.BANANA.get()) return new R196FoodProfile(2, 5, 1_000, 18_000, 0, 8_000);
        if (item == ModItems.WORM.get()) return new R196FoodProfile(1, 2, 6_000, 0, 2_000, 0);
        if (item == ModItems.COOKED_WORM.get()) return new R196FoodProfile(2, 4, 10_000, 0, 3_000, 0);
        if (item == Items.BREAD) return new R196FoodProfile(3, 7, 5_000, 2_000, 1_000, 5_000);
        if (item == Items.MUSHROOM_STEW || item == Items.RABBIT_STEW || item == Items.BEETROOT_SOUP) {
            return new R196FoodProfile(4, 9, 8_000, 24_000, 5_000, 0);
        }
        return R196FoodProfile.EMPTY;
    }
}
