package com.pixulse.infx.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

final class DisabledVanillaRecipesProvider implements DataProvider {
    private static final List<String> DISABLED_RECIPES = List.of(
            "acacia_planks",
            "bamboo_planks",
            "birch_planks",
            "cherry_planks",
            "crimson_planks",
            "dark_oak_planks",
            "jungle_planks",
            "mangrove_planks",
            "oak_planks",
            "pale_oak_planks",
            "spruce_planks",
            "warped_planks",
            "wooden_axe",
            "wooden_hoe",
            "wooden_pickaxe",
            "wooden_shovel",
            "wooden_spear",
            "wooden_sword",
            "stone_axe",
            "stone_hoe",
            "stone_pickaxe",
            "stone_shovel",
            "stone_spear",
            "stone_sword",
            "copper_axe",
            "copper_hoe",
            "copper_pickaxe",
            "copper_shovel",
            "copper_spear",
            "copper_sword",
            "copper_helmet",
            "copper_chestplate",
            "copper_leggings",
            "copper_boots",
            "iron_spear",
            "golden_spear",
            "diamond_spear",
            "netherite_spear_smithing",
            "copper_ingot_from_blasting_copper_ore",
            "copper_ingot_from_blasting_deepslate_copper_ore",
            "copper_ingot_from_blasting_raw_copper",
            "copper_ingot_from_smelting_copper_ore",
            "copper_ingot_from_smelting_deepslate_copper_ore",
            "copper_ingot_from_smelting_raw_copper",
            "copper_nugget_from_blasting",
            "copper_nugget_from_smelting",
            "copper_nugget",
            "copper_ingot_from_nuggets",
            "iron_ingot_from_blasting_deepslate_iron_ore",
            "iron_ingot_from_blasting_iron_ore",
            "iron_ingot_from_blasting_raw_iron",
            "gold_ingot_from_nuggets",
            "gold_nugget",
            "iron_ingot_from_nuggets",
            "iron_nugget",
            "iron_axe",
            "iron_hoe",
            "iron_pickaxe",
            "iron_shovel",
            "iron_sword",
            "golden_axe",
            "golden_hoe",
            "golden_pickaxe",
            "golden_shovel",
            "golden_sword",
            "crafting_table",
            "furnace",
            "glass",
            "sandstone",
            "smooth_sandstone");

    private final Path recipesDirectory;

    DisabledVanillaRecipesProvider(PackOutput output) {
        this.recipesDirectory = output
                .getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve("minecraft")
                .resolve("recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
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
    public String getName() {
        return "Disabled vanilla recipes";
    }
}
