package com.pixulse.infx.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public interface TimedCraftingRecipe extends Recipe<CraftingInput> {
    BenchTier requiredBench();

    float difficulty();

    CraftingRecipe delegate();

    default NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return CraftingRecipe.defaultCraftingReminder(input);
    }

    @Override
    default RecipeType<TimedCraftingRecipe> getType() {
        return com.pixulse.infx.registry.ModRecipes.CRAFTING.get();
    }
}
