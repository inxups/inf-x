package com.pixulse.infx.integration.jei;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.crafting.BenchTier;
import com.pixulse.infx.crafting.TimedCraftingRecipe;

import mezz.jei.api.recipe.types.IRecipeHolderType;

public final class TimedCraftingJeiTypes {
    public static final IRecipeHolderType<TimedCraftingRecipe> HAND =
            IRecipeHolderType.create(InfiniteX.id("hand_crafting"));
    public static final IRecipeHolderType<TimedCraftingRecipe> FLINT =
            IRecipeHolderType.create(InfiniteX.id("flint_workbench"));
    public static final IRecipeHolderType<TimedCraftingRecipe> COPPER =
            IRecipeHolderType.create(InfiniteX.id("copper_workbench"));

    private TimedCraftingJeiTypes() {}

    public static IRecipeHolderType<TimedCraftingRecipe> forBench(BenchTier benchTier) {
        return switch (benchTier) {
            case HAND -> HAND;
            case FLINT -> FLINT;
            case COPPER -> COPPER;
        };
    }
}
