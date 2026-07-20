package com.pixulse.infx.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pixulse.infx.InfiniteX;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.ModifyRecipeJsonsEvent;

/** Removes Minecraft's built-in crafting recipes before recipe deserialization. */
public final class VanillaCraftingRecipeRemoval {
    private static final String MINECRAFT_NAMESPACE = "minecraft";
    private static final String CRAFTING_SERIALIZER_PREFIX = "crafting_";

    private VanillaCraftingRecipeRemoval() {}

    public static void register(IEventBus gameBus) {
        // Run after ordinary recipe integrations so a later listener cannot
        // accidentally restore a minecraft-namespaced crafting JSON.
        gameBus.addListener(EventPriority.LOWEST, VanillaCraftingRecipeRemoval::removeRecipes);
    }

    private static void removeRecipes(ModifyRecipeJsonsEvent event) {
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
        return type != null
                && MINECRAFT_NAMESPACE.equals(type.getNamespace())
                && type.getPath().startsWith(CRAFTING_SERIALIZER_PREFIX);
    }
}
