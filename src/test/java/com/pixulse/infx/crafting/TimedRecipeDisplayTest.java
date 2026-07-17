package com.pixulse.infx.crafting;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import org.junit.jupiter.api.Test;

class TimedRecipeDisplayTest {
    private static final Recipe.CommonInfo COMMON_INFO = new Recipe.CommonInfo(true);
    private static final CraftingRecipe.CraftingBookInfo BOOK_INFO =
            new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, "");

    @Test
    void shapedRecipesAreHiddenFromTheVanillaRecipeBook() {
        var delegate = new ShapedRecipe(COMMON_INFO, BOOK_INFO, null, null) {
            @Override
            public List<RecipeDisplay> display() {
                return Collections.singletonList(null);
            }
        };

        var recipe = new TimedShapedRecipe(BenchTier.HAND, 25.0F, delegate);

        assertTrue(recipe.display().isEmpty());
    }

    @Test
    void shapelessRecipesAreHiddenFromTheVanillaRecipeBook() {
        var delegate = new ShapelessRecipe(
                COMMON_INFO, BOOK_INFO, (ItemStackTemplate) null, List.of()) {
            @Override
            public List<RecipeDisplay> display() {
                return Collections.singletonList(null);
            }
        };

        var recipe = new TimedShapelessRecipe(BenchTier.HAND, 25.0F, delegate);

        assertTrue(recipe.display().isEmpty());
    }
}
