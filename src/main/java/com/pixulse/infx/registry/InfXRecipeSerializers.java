package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.recipe.InfXRepairRecipe;
import com.pixulse.infx.recipe.InfXShapedRecipe;
import com.pixulse.infx.recipe.InfXShapelessRecipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class InfXRecipeSerializers {
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, InfiniteX.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<InfXShapelessRecipe>> SHAPELESS_RETURNING =
            RECIPE_SERIALIZERS.register("shapeless_returning", () -> InfXShapelessRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<InfXShapedRecipe>> SHAPED_RETURNING =
            RECIPE_SERIALIZERS.register("shaped_returning", () -> InfXShapedRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<InfXRepairRecipe>> REPAIR =
            RECIPE_SERIALIZERS.register("repair", () -> InfXRepairRecipe.SERIALIZER);

    private InfXRecipeSerializers() {}

    public static void register(IEventBus modBus) {
        RECIPE_SERIALIZERS.register(modBus);
    }
}
