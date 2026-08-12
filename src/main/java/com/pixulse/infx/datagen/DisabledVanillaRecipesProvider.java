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
 * Generates data-pack overrides that disable selected vanilla recipes
 * replaced by InfiniteX progression.
 *
 * <p>The overrides use NeoForge's {@code neoforge:never} condition instead of
 * copying vanilla recipe JSON. This keeps the list explicit for Minecraft
 * 26.1.2 while allowing all unrelated vanilla recipes to remain available.</p>
 */
final class DisabledVanillaRecipesProvider implements DataProvider {
    private static final List<String> DISABLED_RECIPES = List.of(
            // Wooden, stone, copper, iron, gold and diamond tools and weapons.
            "wooden_sword",
            "wooden_spear",
            "wooden_axe",
            "wooden_pickaxe",
            "wooden_shovel",
            "wooden_hoe",
            "stone_sword",
            "stone_spear",
            "stone_axe",
            "stone_pickaxe",
            "stone_shovel",
            "stone_hoe",
            "copper_sword",
            "copper_spear",
            "copper_axe",
            "copper_pickaxe",
            "copper_shovel",
            "copper_hoe",
            "iron_sword",
            "iron_spear",
            "iron_axe",
            "iron_pickaxe",
            "iron_shovel",
            "iron_hoe",
            "golden_sword",
            "golden_spear",
            "golden_axe",
            "golden_pickaxe",
            "golden_shovel",
            "golden_hoe",
            "diamond_sword",
            "diamond_spear",
            "diamond_axe",
            "diamond_pickaxe",
            "diamond_shovel",
            "diamond_hoe",
            // Other vanilla tools, weapons and projectiles replaced by InfX.
            "bow",
            "arrow",
            "spectral_arrow",
            "crossbow",
            "mace",
            "shield",
            "shears",
            "fishing_rod",
            "flint_and_steel",
            "brush",
            "spyglass",
            "carrot_on_a_stick",
            "warped_fungus_on_a_stick",
            // Vanilla special crafting recipes that operate on the disabled gear.
            "repair_item",
            "shield_decoration",
            "tipped_arrow",
            // Vanilla food and utility recipes replaced by InfX ingredients and shapes.
            "cake",
            "cookie",
            "bread",
            "golden_apple",
            "compass",
            "clock",
            "bricks",
            "nether_bricks",
            // Leather, copper, iron, gold and diamond player armor.
            "leather_helmet",
            "leather_chestplate",
            "leather_leggings",
            "leather_boots",
            "copper_helmet",
            "copper_chestplate",
            "copper_leggings",
            "copper_boots",
            "iron_helmet",
            "iron_chestplate",
            "iron_leggings",
            "iron_boots",
            "golden_helmet",
            "golden_chestplate",
            "golden_leggings",
            "golden_boots",
            "diamond_helmet",
            "diamond_chestplate",
            "diamond_leggings",
            "diamond_boots",
            // Vanilla armor dyeing recipes.
            "leather_helmet_dyed",
            "leather_chestplate_dyed",
            "leather_leggings_dyed",
            "leather_boots_dyed",
            "leather_horse_armor_dyed",
            "wolf_armor_dyed",
            // Netherite tool, weapon and armor upgrades are smithing recipes.
            "netherite_sword_smithing",
            "netherite_spear_smithing",
            "netherite_axe_smithing",
            "netherite_pickaxe_smithing",
            "netherite_hoe_smithing",
            "netherite_shovel_smithing",
            "netherite_helmet_smithing",
            "netherite_chestplate_smithing",
            "netherite_leggings_smithing",
            "netherite_boots_smithing",
            "netherite_nautilus_armor_smithing",
            "netherite_horse_armor_smithing");

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
        return "Disabled vanilla 26.1 recipes";
    }
}
