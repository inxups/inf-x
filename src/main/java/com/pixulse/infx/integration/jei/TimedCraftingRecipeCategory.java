package com.pixulse.infx.integration.jei;

import java.util.List;

import com.pixulse.infx.crafting.BenchTier;
import com.pixulse.infx.crafting.TimedCraftingRecipe;

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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.level.ItemLike;

final class TimedCraftingRecipeCategory
        extends AbstractRecipeCategory<RecipeHolder<TimedCraftingRecipe>> {
    private static final int WIDTH = 116;
    private static final int HEIGHT = 66;

    private final ICraftingGridHelper craftingGridHelper;
    private final IDrawableStatic arrow;

    TimedCraftingRecipeCategory(IGuiHelper guiHelper, BenchTier benchTier, ItemLike icon) {
        super(
                TimedCraftingJeiTypes.forBench(benchTier),
                Component.translatable("jei.infx.category." + benchTier.serializedName()),
                guiHelper.createDrawableItemLike(icon),
                WIDTH,
                HEIGHT);
        craftingGridHelper = guiHelper.createCraftingGridHelper();
        arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            RecipeHolder<TimedCraftingRecipe> recipeHolder,
            IFocusGroup focuses) {
        RecipeDisplay display = getDisplay(recipeHolder.value());
        craftingGridHelper.createAndSetOutputs(builder, display.result());

        if (display instanceof ShapedCraftingRecipeDisplay shaped) {
            craftingGridHelper.createAndSetIngredientsFromDisplays(
                    builder, shaped.ingredients(), shaped.width(), shaped.height());
        } else if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
            craftingGridHelper.createAndSetIngredientsFromDisplays(
                    builder, shapeless.ingredients(), 0, 0);
        }
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder,
            RecipeHolder<TimedCraftingRecipe> recipeHolder,
            IFocusGroup focuses) {
        String difficulty = formatDifficulty(recipeHolder.value().difficulty());
        builder.addText(Component.translatable("jei.infx.difficulty", difficulty), WIDTH, 9)
                .setColor(0xFF808080)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setPosition(0, 56);
    }

    @Override
    public void draw(
            RecipeHolder<TimedCraftingRecipe> recipeHolder,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphicsExtractor graphics,
            double mouseX,
            double mouseY) {
        arrow.draw(graphics, 61, 19);
    }

    @Override
    public boolean isHandled(RecipeHolder<TimedCraftingRecipe> recipeHolder) {
        List<RecipeDisplay> displays = recipeHolder.value().delegate().display();
        if (displays.isEmpty()) {
            return false;
        }
        RecipeDisplay display = displays.getFirst();
        return display instanceof ShapedCraftingRecipeDisplay
                || display instanceof ShapelessCraftingRecipeDisplay;
    }

    private static RecipeDisplay getDisplay(TimedCraftingRecipe recipe) {
        return recipe.delegate().display().getFirst();
    }

    private static String formatDifficulty(float difficulty) {
        if (difficulty == Math.rint(difficulty)) {
            return Integer.toString((int) difficulty);
        }
        return Float.toString(difficulty);
    }
}
