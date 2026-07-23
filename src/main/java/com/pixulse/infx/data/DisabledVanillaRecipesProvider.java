package com.pixulse.infx.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

final class DisabledVanillaRecipesProvider implements DataProvider {
    private static final List<String> BASE_DISABLED_RECIPES = List.of(
            "acacia_planks",
            "arrow",
            "bamboo_planks",
            "birch_planks",
            "bow",
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
            "netherite_axe_smithing",
            "netherite_boots_smithing",
            "netherite_chestplate_smithing",
            "netherite_helmet_smithing",
            "netherite_hoe_smithing",
            "netherite_leggings_smithing",
            "netherite_pickaxe_smithing",
            "netherite_shovel_smithing",
            "netherite_sword_smithing",
            "diamond_axe",
            "diamond_boots",
            "diamond_chestplate",
            "diamond_helmet",
            "diamond_hoe",
            "diamond_leggings",
            "diamond_pickaxe",
            "diamond_shovel",
            "diamond_spear",
            "diamond_sword",
            "shield",
            "blast_furnace",
            "smoker",
            "smithing_table",
            "stonecutter",
            "spyglass",
            "mace",
            "bricks",
            "chiseled_stone_bricks",
            "clock",
            "compass",
            "flint_and_steel",
            "glass_pane",
            "ladder",
            "melon",
            "nether_bricks",
            "oak_fence",
            "oak_sign",
            "saddle",
            "snow",
            "snow_block",
            "stone",
            "stone_bricks",
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
            "golden_axe",
            "golden_boots",
            "golden_chestplate",
            "golden_helmet",
            "golden_hoe",
            "golden_leggings",
            "golden_pickaxe",
            "golden_shovel",
            "golden_sword",
            "iron_ingot_from_nuggets",
            "iron_nugget",
            "iron_axe",
            "iron_boots",
            "iron_chestplate",
            "iron_helmet",
            "iron_hoe",
            "iron_leggings",
            "iron_pickaxe",
            "iron_shovel",
            "iron_sword",
            "leather_boots",
            "leather_chestplate",
            "leather_helmet",
            "leather_leggings",
            "shears",
            "crafting_table",
            "furnace",
            "bread",
            "cake",
            "enchanting_table",
            "glass",
            "golden_apple",
            "mushroom_stew",
            "pumpkin_pie",
            "sandstone",
            "smooth_sandstone");
    private static final List<String> MODERN_BYPASS_RECIPES = List.of(
            "black_bundle",
            "blue_bundle",
            "brown_bundle",
            "bundle",
            "copper_bars",
            "copper_block",
            "copper_bulb",
            "copper_chain",
            "copper_chest",
            "copper_door",
            "copper_grate",
            "copper_ingot",
            "copper_ingot_from_waxed_copper_block",
            "copper_lantern",
            "copper_torch",
            "copper_trapdoor",
            "crafter",
            "cyan_bundle",
            "gray_bundle",
            "green_bundle",
            "light_blue_bundle",
            "light_gray_bundle",
            "lime_bundle",
            "magenta_bundle",
            "netherite_block",
            "netherite_horse_armor_smithing",
            "netherite_ingot",
            "netherite_ingot_from_netherite_block",
            "netherite_nautilus_armor_smithing",
            "netherite_scrap",
            "netherite_scrap_from_blasting",
            "netherite_upgrade_smithing_template",
            "orange_bundle",
            "pink_bundle",
            "purple_bundle",
            "raw_copper",
            "raw_copper_block",
            "red_bundle",
            "white_bundle",
            "yellow_bundle");
    private static final List<String> DISABLED_RECIPES = Stream.concat(
                    BASE_DISABLED_RECIPES.stream(), MODERN_BYPASS_RECIPES.stream())
            .distinct()
            .toList();

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
