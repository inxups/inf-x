package com.pixulse.infx.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class InfXRecipeRemaindersTest {
    private static final Recipe.CommonInfo COMMON_INFO = new Recipe.CommonInfo(true);
    private static final CraftingRecipe.CraftingBookInfo BOOK_INFO =
            new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, "");

    @BeforeAll
    static void bindTemplateItems() {
        // ItemStackTemplate#create builds stacks from the item's built-in
        // registry holder; JUnit does not bootstrap the component registry,
        // so the holders used by the templates must be bound explicitly.
        for (Item item : List.of(
                Items.BOWL,
                Items.BUCKET,
                Items.MILK_BUCKET,
                Items.MUSHROOM_STEW,
                Items.DIAMOND,
                Items.CAKE,
                Items.BREAD,
                Items.WHEAT,
                Items.APPLE,
                Items.SUGAR)) {
            if (!item.builtInRegistryHolder().areComponentsBound()) {
                item.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
            }
        }
    }

    private static ItemStack liquidBowlStack() {
        return new ItemStack(
                Holder.direct(
                        Items.MUSHROOM_STEW,
                        DataComponentMap.builder()
                                .set(DataComponents.USE_REMAINDER, new UseRemainder(new ItemStackTemplate(Items.BOWL)))
                                .build()),
                1);
    }

    private static ItemStack plainStack(Item item) {
        return new ItemStack(Holder.direct(item, DataComponentMap.EMPTY), 1);
    }

    @Test
    void shapelessReturnsTheEmptyBowlOfEveryConsumedLiquidBowl() {
        InfXShapelessRecipe recipe = new InfXShapelessRecipe(
                COMMON_INFO,
                BOOK_INFO,
                new ItemStackTemplate(Items.DIAMOND),
                List.of(
                        Ingredient.of(Items.MUSHROOM_STEW),
                        Ingredient.of(Items.MUSHROOM_STEW),
                        Ingredient.of(Items.MUSHROOM_STEW),
                        Ingredient.of(Items.MUSHROOM_STEW)));

        NonNullList<ItemStack> remaining = recipe.getRemainingItems(
                CraftingInput.of(2, 2, List.of(liquidBowlStack(), liquidBowlStack(), liquidBowlStack(), liquidBowlStack())));

        assertEquals(4, remaining.size());
        for (ItemStack remainder : remaining) {
            assertEquals(1, remainder.getCount());
            assertEquals(Items.BOWL, remainder.getItem(), "each consumed liquid bowl returns an empty bowl");
        }
    }

    @Test
    void shapelessFallsBackToTheCraftRemainderOfVesselsWithoutUseRemainder() {
        InfXShapelessRecipe recipe = new InfXShapelessRecipe(
                COMMON_INFO,
                BOOK_INFO,
                new ItemStackTemplate(Items.CAKE),
                List.of(Ingredient.of(Items.MILK_BUCKET)));

        NonNullList<ItemStack> remaining =
                recipe.getRemainingItems(CraftingInput.of(1, 1, List.of(plainStack(Items.MILK_BUCKET))));

        assertEquals(Items.BUCKET, remaining.getFirst().getItem());
    }

    @Test
    void shapelessLeavesOrdinaryIngredientsAlone() {
        InfXShapelessRecipe recipe = new InfXShapelessRecipe(
                COMMON_INFO,
                BOOK_INFO,
                new ItemStackTemplate(Items.BREAD),
                List.of(Ingredient.of(Items.WHEAT), Ingredient.of(Items.APPLE)));

        NonNullList<ItemStack> remaining = recipe.getRemainingItems(
                CraftingInput.of(1, 2, List.of(plainStack(Items.WHEAT), plainStack(Items.APPLE))));

        assertTrue(remaining.stream().allMatch(ItemStack::isEmpty));
    }

    @Test
    void shapedReturnsTheEmptyBowlOfAConsumedLiquidBowl() {
        InfXShapedRecipe recipe = new InfXShapedRecipe(
                COMMON_INFO,
                BOOK_INFO,
                ShapedRecipePattern.of(
                        Map.of('M', Ingredient.of(Items.MUSHROOM_STEW), 'F', Ingredient.of(Items.SUGAR)),
                        List.of("FM")),
                new ItemStackTemplate(Items.CAKE));

        NonNullList<ItemStack> remaining = recipe.getRemainingItems(
                CraftingInput.of(2, 1, List.of(plainStack(Items.SUGAR), liquidBowlStack())));

        assertTrue(remaining.getFirst().isEmpty());
        assertEquals(Items.BOWL, remaining.get(1).getItem());
    }

    @Test
    void mapCodecsDecodeTheGeneratedRecipeJson() {
        RegistryOps<JsonElement> registryOps =
                RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        InfXShapelessRecipe cheese = InfXShapelessRecipe.MAP_CODEC
                .codec()
                .parse(registryOps, JsonParser.parseString("""
                        {
                          "category": "misc",
                          "ingredients": [
                            "minecraft:bowl",
                            "minecraft:bowl",
                            "minecraft:bowl",
                            "minecraft:bowl"
                          ],
                          "result": {
                            "id": "minecraft:bread"
                          }
                        }
                        """))
                .getOrThrow();
        assertEquals(1, cheese.result().count());

        InfXShapedRecipe cake = InfXShapedRecipe.MAP_CODEC
                .codec()
                .parse(registryOps, JsonParser.parseString("""
                        {
                          "category": "misc",
                          "key": {
                            "M": "minecraft:milk_bucket",
                            "F": "minecraft:sugar",
                            "S": "minecraft:sugar",
                            "E": "minecraft:egg"
                          },
                          "pattern": [
                            "FS",
                            "EM"
                          ],
                          "result": {
                            "id": "minecraft:cake"
                          }
                        }
                        """))
                .getOrThrow();
        assertEquals(2, cake.getHeight());
        assertEquals(2, cake.getWidth());
    }
}
