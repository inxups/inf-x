package com.pixulse.infx.data.food;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Resolves food metadata from reloadable {@code data/<namespace>/food_profiles} rules. */
public final class FoodProfiles {
    private FoodProfiles() {}

    public static FoodProfile cakeSlice() {
        return FoodProfileRules.profile(Items.CAKE.getDefaultInstance()).orElse(FoodProfile.EMPTY);
    }

    public static FoodProfile forStack(ItemStack stack) {
        return FoodProfileRules.profile(stack).orElse(FoodProfile.EMPTY);
    }
}
