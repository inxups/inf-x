package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.crafting.TimedCraftingRecipe;
import com.pixulse.infx.crafting.TimedShapedRecipe;
import com.pixulse.infx.crafting.TimedShapelessRecipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {
    private static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, InfiniteX.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, InfiniteX.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<TimedCraftingRecipe>> CRAFTING =
            TYPES.register("crafting", () -> RecipeType.simple(InfiniteX.id("crafting")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TimedShapedRecipe>> CRAFTING_SHAPED =
            SERIALIZERS.register("crafting_shaped", () -> TimedShapedRecipe.SERIALIZER);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TimedShapelessRecipe>> CRAFTING_SHAPELESS =
            SERIALIZERS.register("crafting_shapeless", () -> TimedShapelessRecipe.SERIALIZER);

    private ModRecipes() {}

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        SERIALIZERS.register(modBus);
    }
}
