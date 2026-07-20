package com.pixulse.infx.crafting;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

/**
 * Client-side JEI view of a normal 26.2 crafting recipe with its inferred
 * MITE profile.  This wrapper is never registered or used for server recipe
 * matching; the original recipe holder ID remains authoritative.
 */
public record InferredTimedCraftingRecipe(CraftingProfile profile, CraftingRecipe delegate)
        implements TimedCraftingRecipe {
    public InferredTimedCraftingRecipe {
        if (profile == null) {
            throw new NullPointerException("profile");
        }
        if (delegate == null) {
            throw new NullPointerException("delegate");
        }
    }

    public static InferredTimedCraftingRecipe of(CraftingRecipe recipe) {
        return new InferredTimedCraftingRecipe(MiteCraftingRules.displayProfile(recipe), recipe);
    }

    @Override
    public BenchTier requiredBench() {
        return profile.requiredBench();
    }

    @Override
    public float difficulty() {
        return profile.difficulty();
    }

    @Override
    public boolean materialGated() {
        return profile.materialGated();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return delegate.matches(input, level);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return delegate.assemble(input);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return delegate.getRemainingItems(input);
    }

    @Override
    public boolean isSpecial() {
        return delegate.isSpecial();
    }

    @Override
    public boolean showNotification() {
        return delegate.showNotification();
    }

    @Override
    public String group() {
        return delegate.group();
    }

    @Override
    public RecipeSerializer<? extends Recipe<CraftingInput>> getSerializer() {
        return delegate.getSerializer();
    }

    @Override
    public PlacementInfo placementInfo() {
        return delegate.placementInfo();
    }

    @Override
    public List<RecipeDisplay> display() {
        return delegate.display();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return delegate.recipeBookCategory();
    }
}
