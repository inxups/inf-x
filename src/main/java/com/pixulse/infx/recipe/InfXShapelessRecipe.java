package com.pixulse.infx.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;

/**
 * A shapeless recipe that returns every consumed ingredient's empty
 * container (use/craft remainder) when the result does not include it,
 * e.g. 4 milk bowls crafting into cheese leave 4 empty bowls behind.
 */
public class InfXShapelessRecipe extends ShapelessRecipe {
    public static final MapCodec<InfXShapelessRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
                    CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(o -> o.bookInfo),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.result),
                    Codec.lazyInitialized(
                                    () -> Ingredient.CODEC.listOf(
                                            1, ShapedRecipePattern.getMaxWidth() * ShapedRecipePattern.getMaxHeight()))
                            .fieldOf("ingredients")
                            .forGetter(o -> o.ingredients))
                    .apply(i, InfXShapelessRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, InfXShapelessRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC,
            o -> o.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC,
            o -> o.bookInfo,
            ItemStackTemplate.STREAM_CODEC,
            o -> o.result,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),
            o -> o.ingredients,
            InfXShapelessRecipe::new);
    public static final RecipeSerializer<InfXShapelessRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final ItemStackTemplate result;
    private final List<Ingredient> ingredients;

    public InfXShapelessRecipe(
            Recipe.CommonInfo commonInfo,
            CraftingRecipe.CraftingBookInfo bookInfo,
            ItemStackTemplate result,
            List<Ingredient> ingredients) {
        super(commonInfo, bookInfo, result, ingredients);
        this.result = result;
        this.ingredients = ingredients;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        return (RecipeSerializer<ShapelessRecipe>) (RecipeSerializer<?>) SERIALIZER;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return InfXRecipeRemainders.forInput(input);
    }
}
