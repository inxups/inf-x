package com.pixulse.infx.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

public record TimedShapedRecipe(BenchTier requiredBench, float difficulty, ShapedRecipe delegate)
        implements TimedCraftingRecipe {
    public static final MapCodec<TimedShapedRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                            TimedRecipeCodecs.BENCH_TIER
                                    .fieldOf("required_bench")
                                    .forGetter(TimedShapedRecipe::requiredBench),
                            ExtraCodecs.POSITIVE_FLOAT
                                    .fieldOf("difficulty")
                                    .forGetter(TimedShapedRecipe::difficulty),
                            ShapedRecipe.MAP_CODEC.forGetter(TimedShapedRecipe::delegate))
                    .apply(instance, TimedShapedRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TimedShapedRecipe> STREAM_CODEC = StreamCodec.composite(
            TimedRecipeCodecs.BENCH_TIER_STREAM,
            TimedShapedRecipe::requiredBench,
            ByteBufCodecs.FLOAT,
            TimedShapedRecipe::difficulty,
            ShapedRecipe.STREAM_CODEC,
            TimedShapedRecipe::delegate,
            TimedShapedRecipe::new);

    public static final RecipeSerializer<TimedShapedRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return delegate.matches(input, level);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return delegate.assemble(input);
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
    public RecipeSerializer<TimedShapedRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public PlacementInfo placementInfo() {
        return delegate.placementInfo();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return delegate.recipeBookCategory();
    }
}
