package com.pixulse.infx.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jspecify.annotations.NonNull;

/**
 * Disables vanilla recipes whose serializer cannot be stripped by
 * {@code VanillaCraftingRecipeRemoval} (which only touches the crafting
 * family). Only the netherite weapon/tool smithing upgrades remain disabled;
 * everything else has been restored to vanilla.
 */
final class DisabledVanillaRecipesProvider implements DataProvider {
    private static final List<String> DISABLED_RECIPES = List.of(
            "netherite_axe_smithing",
            "netherite_hoe_smithing",
            "netherite_pickaxe_smithing",
            "netherite_shovel_smithing",
            "netherite_sword_smithing",
            "netherite_spear_smithing");

    private final Path recipesDirectory;

    DisabledVanillaRecipesProvider(PackOutput output) {
        this.recipesDirectory = output
                .getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve("minecraft")
                .resolve("recipe");
    }

    @Override
    public @NonNull CompletableFuture<?> run(@NonNull CachedOutput cache) {
        return CompletableFuture.allOf(DISABLED_RECIPES.stream()
                .map(recipe -> DataProvider.saveStable(
                        cache, disabledRecipe(), recipesDirectory.resolve(recipe + ".json")))
                .toArray(CompletableFuture[]::new));
    }

    private static JsonObject disabledRecipe() {
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "neoforge:never");
        JsonArray conditions = new JsonArray();
        conditions.add(condition);
        JsonObject recipe = new JsonObject();
        recipe.add("neoforge:conditions", conditions);
        return recipe;
    }

    @Override
    public @NonNull String getName() {
        return "Disabled vanilla weapon/tool smithing recipes";
    }
}
