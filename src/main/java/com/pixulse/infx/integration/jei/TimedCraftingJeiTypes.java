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
    public static final IRecipeHolderType<TimedCraftingRecipe> IRON =
            IRecipeHolderType.create(InfiniteX.id("iron_workbench"));
    public static final IRecipeHolderType<TimedCraftingRecipe> ANCIENT_METAL =
            IRecipeHolderType.create(InfiniteX.id("ancient_metal_workbench"));
    public static final IRecipeHolderType<TimedCraftingRecipe> MITHRIL =
            IRecipeHolderType.create(InfiniteX.id("mithril_workbench"));
    public static final IRecipeHolderType<TimedCraftingRecipe> ADAMANTIUM =
            IRecipeHolderType.create(InfiniteX.id("adamantium_workbench"));
    public static final IRecipeHolderType<TimedCraftingRecipe> OBSIDIAN =
            IRecipeHolderType.create(InfiniteX.id("obsidian_workbench"));

    private TimedCraftingJeiTypes() {}

    public static IRecipeHolderType<TimedCraftingRecipe> forBench(BenchTier benchTier) {
        return switch (benchTier) {
            case HAND -> HAND;
            case FLINT -> FLINT;
            case COPPER, SILVER, GOLD -> COPPER;
            case IRON -> IRON;
            case ANCIENT_METAL -> ANCIENT_METAL;
            case MITHRIL -> MITHRIL;
            case ADAMANTIUM -> ADAMANTIUM;
            case OBSIDIAN -> OBSIDIAN;
        };
    }
}
