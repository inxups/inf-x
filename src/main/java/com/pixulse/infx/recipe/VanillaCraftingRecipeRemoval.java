package com.pixulse.infx.recipe;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pixulse.infx.InfiniteX;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.ModifyRecipeJsonsEvent;

/**
 * Removes every vanilla crafting recipe before recipe deserialization.
 *
 * <p>All recipes in the {@code minecraft} namespace whose serializer belongs
 * to the crafting family ({@code crafting_shaped}, {@code crafting_shapeless},
 * {@code crafting_special_*}, {@code crafting_transmute}, ...) are stripped,
 * so vanilla items are no longer craftable and InfiniteX items take over the
 * crafting grids. InfiniteX recipes live in the {@code infx} namespace and are
 * unaffected even though they use the standard {@code minecraft:crafting_*}
 * serializers; their difficulty and workbench tier come from
 * {@link RecipeRules} overrides or {@link InfxCraftingRules} inference.</p>
 *
 * <p>Non-crafting vanilla recipes (smelting, blasting, smoking, campfire
 * cooking, stonecutting, smithing) are not touched; only the netherite
 * weapon/tool smithing upgrades stay disabled through the generated
 * {@code neoforge:never} override files.</p>
 */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class VanillaCraftingRecipeRemoval {
    private static final String MINECRAFT_NAMESPACE = "minecraft";

    /** Prefix shared by every vanilla crafting-family serializer. */
    private static final String CRAFTING_SERIALIZER_PREFIX = "crafting_";

    private VanillaCraftingRecipeRemoval() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void removeRecipes(ModifyRecipeJsonsEvent event) {
        int removed = removeVanillaCraftingRecipes(event.getRecipeJsons());
        InfiniteX.LOGGER.info("Removed {} vanilla crafting recipes", removed);
    }

    static int removeVanillaCraftingRecipes(Map<Identifier, JsonElement> recipes) {
        int initialSize = recipes.size();
        recipes.entrySet().removeIf(entry -> isVanillaCraftingRecipe(entry.getKey(), entry.getValue()));
        return initialSize - recipes.size();
    }

    static boolean isVanillaCraftingRecipe(Identifier recipeId, JsonElement recipeJson) {
        if (!MINECRAFT_NAMESPACE.equals(recipeId.getNamespace()) || !recipeJson.isJsonObject()) {
            return false;
        }
        JsonObject object = recipeJson.getAsJsonObject();
        JsonElement typeElement = object.get("type");
        if (typeElement == null || !typeElement.isJsonPrimitive()
                || !typeElement.getAsJsonPrimitive().isString()) {
            return false;
        }
        Identifier type = Identifier.tryParse(typeElement.getAsString());
        if (type == null || !MINECRAFT_NAMESPACE.equals(type.getNamespace())) {
            return false;
        }
        return type.getPath().startsWith(CRAFTING_SERIALIZER_PREFIX);
    }
}
