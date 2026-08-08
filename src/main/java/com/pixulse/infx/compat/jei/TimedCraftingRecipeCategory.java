package com.pixulse.infx.compat.jei;

import java.util.List;

import com.pixulse.infx.recipe.BenchTier;
import com.pixulse.infx.recipe.RecipeRules;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.NonNull;

/**
 * JEI category for one workbench tier. Every entry is a normal crafting
 * recipe; its INFX difficulty and required bench are resolved through
 * {@link RecipeRules} (with {@code InfxCraftingRules} inference as fallback).
 */
final class TimedCraftingRecipeCategory
        extends AbstractRecipeCategory<RecipeHolder<CraftingRecipe>> {
    private static final int WIDTH = 116;
    private static final int HEIGHT = 76;

    private final ICraftingGridHelper craftingGridHelper;
    private final IDrawableStatic arrow;
    private final BenchTier benchTier;

    TimedCraftingRecipeCategory(IGuiHelper guiHelper, BenchTier benchTier, ItemLike icon) {
        super(
                TimedCraftingJeiTypes.forBench(benchTier),
                Component.translatable("jei.infx.category." + benchTier.serializedName()),
                guiHelper.createDrawableItemLike(icon),
                WIDTH,
                HEIGHT);
        this.benchTier = benchTier;
        craftingGridHelper = guiHelper.createCraftingGridHelper();
        arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public void setRecipe(
            @NonNull IRecipeLayoutBuilder builder,
            RecipeHolder<CraftingRecipe> recipeHolder,
            @NonNull IFocusGroup focuses) {
        RecipeDisplay display = getDisplay(recipeHolder);
        craftingGridHelper.createAndSetOutputs(builder, display.result());

        if (display instanceof ShapedCraftingRecipeDisplay shaped)
            craftingGridHelper.createAndSetIngredientsFromDisplays(builder, shaped.ingredients(), shaped.width(), shaped.height());
        else if (display instanceof ShapelessCraftingRecipeDisplay shapeless)
            craftingGridHelper.createAndSetIngredientsFromDisplays(builder, shapeless.ingredients(), 0, 0);
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder,
            RecipeHolder<CraftingRecipe> recipeHolder,
            @NonNull IFocusGroup focuses) {
        String difficulty = formatDifficulty(RecipeRules.displayProfile(recipeHolder).difficulty());
        builder.addText(Component.translatable("jei.infx.difficulty", difficulty), WIDTH, 9)
                .setColor(0xFF808080)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setPosition(0, 55);
        builder.addText(
                        Component.translatable(
                                "jei.infx.required_bench",
                                Component.translatable("jei.infx.category." + benchTier.serializedName())),
                        WIDTH,
                        9)
                .setColor(0xFF808080)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setPosition(0, 65);
    }

    @Override
    public void draw(
            RecipeHolder<CraftingRecipe> recipeHolder,
            @NonNull IRecipeSlotsView recipeSlotsView,
            @NonNull GuiGraphicsExtractor graphics,
            double mouseX,
            double mouseY) {
        arrow.draw(graphics, 61, 19);
    }

    @Override
    public boolean isHandled(RecipeHolder<CraftingRecipe> recipeHolder) {
        List<RecipeDisplay> displays = recipeHolder.value().display();
        if (displays.isEmpty()) {
            return false;
        }
        RecipeDisplay display = displays.getFirst();
        return display instanceof ShapedCraftingRecipeDisplay
                || display instanceof ShapelessCraftingRecipeDisplay;
    }

    private static RecipeDisplay getDisplay(RecipeHolder<CraftingRecipe> recipeHolder) {
        return recipeHolder.value().display().getFirst();
    }

    private static String formatDifficulty(float difficulty) {
        if (difficulty == Math.rint(difficulty)) {
            return Integer.toString((int) difficulty);
        }
        return Float.toString(difficulty);
    }
}
