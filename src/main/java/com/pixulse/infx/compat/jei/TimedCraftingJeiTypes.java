package com.pixulse.infx.compat.jei;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.recipe.BenchTier;

import mezz.jei.api.recipe.types.IRecipeHolderType;
import net.minecraft.world.item.crafting.CraftingRecipe;

public final class TimedCraftingJeiTypes {
    public static final IRecipeHolderType<CraftingRecipe> HAND = IRecipeHolderType.create(InfiniteX.id("hand_crafting"));
    public static final IRecipeHolderType<CraftingRecipe> FLINT = IRecipeHolderType.create(InfiniteX.id("flint_workbench"));
    public static final IRecipeHolderType<CraftingRecipe> COPPER = IRecipeHolderType.create(InfiniteX.id("copper_workbench"));
    public static final IRecipeHolderType<CraftingRecipe> IRON = IRecipeHolderType.create(InfiniteX.id("iron_workbench"));
    public static final IRecipeHolderType<CraftingRecipe> ANCIENT_METAL = IRecipeHolderType.create(InfiniteX.id("ancient_metal_workbench"));
    public static final IRecipeHolderType<CraftingRecipe> MITHRIL = IRecipeHolderType.create(InfiniteX.id("mithril_workbench"));
    public static final IRecipeHolderType<CraftingRecipe> ADAMANTIUM = IRecipeHolderType.create(InfiniteX.id("adamantium_workbench"));
    public static final IRecipeHolderType<CraftingRecipe> OBSIDIAN = IRecipeHolderType.create(InfiniteX.id("obsidian_workbench"));

    private TimedCraftingJeiTypes() {
    }

    public static IRecipeHolderType<CraftingRecipe> forBench(BenchTier benchTier) {
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
