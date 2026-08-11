package com.pixulse.infx.datagen;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.InfxBucketItem;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.recipe.BenchTier;
import com.pixulse.infx.recipe.RecipeRule;
import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.registry.tag.InfXItemTags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.DyeRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import org.jspecify.annotations.NonNull;

final class ModRecipeProvider extends RecipeProvider {
    private static final float STICK_DIFFICULTY = 25.0F;

    /** Crafting rules emitted alongside the recipes, keyed by rule/recipe ID. */
    private final Map<ResourceKey<Recipe<?>>, RecipeRule> recipeRules = new HashMap<>();

    ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    Map<ResourceKey<Recipe<?>>, RecipeRule> recipeRules() {
        return recipeRules;
    }

    @Override
    protected void buildRecipes() {
        addInfXVanillaDoorOverrides();

        // Flour is an INFX ingredient; the vanilla sugar and bone-meal recipes
        // are restored from the vanilla pack and need no replacement here.
        addShaped(
                "flour",
                BenchTier.HAND,
                100.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.FLOUR,
                1,
                Map.of('W', Ingredient.of(Items.WHEAT)),
                List.of("WWW"));
        addShapeless(
                "water_bowl",
                BenchTier.FLINT,
                25.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.WATER_BOWL,
                4,
                List.of(
                        ingredient(InfXItemTags.WATER_BUCKETS),
                        Ingredient.of(Items.BOWL),
                        Ingredient.of(Items.BOWL),
                        Ingredient.of(Items.BOWL),
                        Ingredient.of(Items.BOWL)));
        addShapeless(
                "dough",
                BenchTier.HAND,
                150.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.DOUGH,
                1,
                List.of(Ingredient.of(InfXItems.FLOUR), Ingredient.of(InfXItems.WATER_BOWL)));
        addShaped(
                "dough_from_water_bucket",
                BenchTier.HAND,
                600.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.DOUGH,
                4,
                Map.of('F', Ingredient.of(InfXItems.FLOUR), 'W', ingredient(InfXItemTags.WATER_BUCKETS)),
                List.of("F F", " W ", "F F"));
        addShapeless(
                "salad",
                BenchTier.HAND,
                175.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.SALAD,
                1,
                List.of(
                        Ingredient.of(Items.BOWL),
                        Ingredient.of(Items.DANDELION),
                        Ingredient.of(Items.DANDELION),
                        Ingredient.of(Items.DANDELION)));
        addShapeless(
                "blueberry_porridge",
                BenchTier.HAND,
                175.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.BLUEBERRY_PORRIDGE,
                1,
                List.of(Ingredient.of(Items.WHEAT_SEEDS), Ingredient.of(InfXItems.BLUEBERRIES),
                        Ingredient.of(Items.SUGAR), Ingredient.of(InfXItems.WATER_BOWL)));
        addShapeless(
                "milk_bowl",
                BenchTier.FLINT,
                75.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.MILK_BOWL,
                4,
                List.of(
                        ingredient(InfXItemTags.MILK_BUCKETS),
                        Ingredient.of(Items.BOWL),
                        Ingredient.of(Items.BOWL),
                        Ingredient.of(Items.BOWL),
                        Ingredient.of(Items.BOWL)));
        addShapeless(
                "cereal_porridge",
                BenchTier.HAND,
                200.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.CEREAL_PORRIDGE,
                1,
                List.of(Ingredient.of(InfXItems.MILK_BOWL), Ingredient.of(Items.WHEAT), Ingredient.of(Items.SUGAR)));
        addShapeless(
                "chocolate",
                BenchTier.HAND,
                100.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.CHOCOLATE,
                1,
                List.of(Ingredient.of(Items.COCOA_BEANS), Ingredient.of(Items.SUGAR)));
        addShapeless(
                "pumpkin_soup",
                BenchTier.HAND,
                175.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.PUMPKIN_SOUP,
                1,
                List.of(Ingredient.of(Items.PUMPKIN), Ingredient.of(InfXItems.WATER_BOWL)));
        addShapeless(
                "cream_of_mushroom_soup",
                BenchTier.HAND,
                225.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.CREAM_OF_MUSHROOM_SOUP,
                1,
                List.of(Ingredient.of(InfXItems.MILK_BOWL), Ingredient.of(Items.BROWN_MUSHROOM),
                        Ingredient.of(Items.BROWN_MUSHROOM)));
        addShapeless(
                "vegetable_soup",
                BenchTier.HAND,
                225.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.VEGETABLE_SOUP,
                1,
                List.of(Ingredient.of(Items.CARROT), Ingredient.of(Items.POTATO),
                        Ingredient.of(InfXItems.ONION), Ingredient.of(InfXItems.WATER_BOWL)));
        addShapeless(
                "cream_of_vegetable_soup",
                BenchTier.HAND,
                250.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.CREAM_OF_VEGETABLE_SOUP,
                1,
                List.of(Ingredient.of(InfXItems.MILK_BOWL), Ingredient.of(Items.CARROT), Ingredient.of(Items.POTATO),
                        Ingredient.of(InfXItems.ONION)));
        addShapeless(
                "chicken_soup",
                BenchTier.HAND,
                275.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.CHICKEN_SOUP,
                1,
                List.of(Ingredient.of(Items.COOKED_CHICKEN), Ingredient.of(Items.CARROT),
                        Ingredient.of(InfXItems.ONION), Ingredient.of(InfXItems.WATER_BOWL)));
        addShapeless(
                "beef_stew",
                BenchTier.HAND,
                300.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.BEEF_STEW,
                1,
                List.of(Ingredient.of(Items.COOKED_BEEF), Ingredient.of(Items.BROWN_MUSHROOM),
                        Ingredient.of(Items.POTATO), Ingredient.of(InfXItems.WATER_BOWL)));
        addShapeless(
                "mashed_potato",
                BenchTier.HAND,
                175.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.MASHED_POTATO,
                1,
                List.of(Ingredient.of(InfXItems.MILK_BOWL), Ingredient.of(Items.BAKED_POTATO),
                        Ingredient.of(InfXItems.CHEESE)));
        addShapeless(
                "cheese",
                BenchTier.HAND,
                6_400.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.CHEESE,
                1,
                List.of(Ingredient.of(InfXItems.MILK_BOWL), Ingredient.of(InfXItems.MILK_BOWL),
                        Ingredient.of(InfXItems.MILK_BOWL), Ingredient.of(InfXItems.MILK_BOWL)));
        addShapeless(
                "fruit_ice",
                BenchTier.HAND,
                150.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.FRUIT_ICE,
                1,
                List.of(Ingredient.of(InfXItems.ORANGE), Ingredient.of(Items.SUGAR),
                        Ingredient.of(Items.SNOWBALL), Ingredient.of(Items.BOWL)));
        addShapeless(
                "ice_cream",
                BenchTier.HAND,
                200.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.ICE_CREAM,
                1,
                List.of(Ingredient.of(InfXItems.CHOCOLATE), Ingredient.of(InfXItems.MILK_BOWL),
                        Ingredient.of(Items.SNOWBALL)));
        // InfX recipe: golden apple + bottle o' enchanting makes an enchanted golden apple.
        addShapeless(
                "enchanted_golden_apple",
                BenchTier.HAND,
                600.0F,
                CraftingBookCategory.MISC,
                "",
                Items.ENCHANTED_GOLDEN_APPLE,
                1,
                List.of(
                        Ingredient.of(Items.GOLDEN_APPLE),
                        Ingredient.of(Items.EXPERIENCE_BOTTLE)));
        addShapeless(
                "bottle_of_disenchanting",
                BenchTier.HAND,
                100.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.BOTTLE_OF_DISENCHANTING,
                1,
                List.of(
                        DataComponentIngredient.of(
                                DataComponents.POTION_CONTENTS,
                                new PotionContents(Potions.WATER),
                                Items.POTION),
                        Ingredient.of(Items.NETHER_WART),
                        Ingredient.of(Items.CHARCOAL)));
        addR196Buckets();
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(InfXItems.DOUGH),
                        RecipeCategory.FOOD,
                        CookingBookCategory.FOOD,
                        Items.BREAD,
                        1.0F,
                        200)
                .unlockedBy("has_dough", has(InfXItems.DOUGH))
                .save(output, recipeKey("bread_from_dough"));
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(InfXItems.WORM),
                        RecipeCategory.FOOD,
                        CookingBookCategory.FOOD,
                        InfXItems.COOKED_WORM,
                        0.2F,
                        160)
                .unlockedBy("has_worm", has(InfXItems.WORM))
                .save(output, recipeKey("cooked_worm"));
        // InfX: baking potatoes in a furnace grants no experience; override the vanilla
        // recipe with the same inputs and time but a zero experience reward.
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(Items.POTATO),
                        RecipeCategory.FOOD,
                        CookingBookCategory.FOOD,
                        Items.BAKED_POTATO,
                        0.0F,
                        200)
                .unlockedBy("has_potato", has(Items.POTATO))
                .save(output, vanillaRecipeKey("baked_potato"));
        addShaped(
                "emerald_enchanting_table",
                BenchTier.IRON,
                3_200.0F,
                CraftingBookCategory.BUILDING,
                "",
                InfXBlocks.EMERALD_ENCHANTING_TABLE,
                1,
                Map.of('B', Ingredient.of(Items.BOOK), 'G', Ingredient.of(Items.EMERALD),
                        'O', Ingredient.of(Blocks.OBSIDIAN)),
                List.of(" B ", "GOG", "OOO"));
        // InfX: the diamond table is the mithril-tier enchanting station, so it is forged
        // from mithril rather than diamonds; the mithril bench gate matches the material.
        addShaped(
                "diamond_enchanting_table",
                BenchTier.MITHRIL,
                6_400.0F,
                CraftingBookCategory.BUILDING,
                "",
                InfXBlocks.DIAMOND_ENCHANTING_TABLE,
                1,
                Map.of('B', Ingredient.of(Items.BOOK), 'G', Ingredient.of(InfXItems.MITHRIL_INGOT),
                        'O', Ingredient.of(Blocks.OBSIDIAN)),
                List.of(" B ", "GOG", "OOO"));
        addSafe("copper_safe", BenchTier.COPPER, InfXBlocks.COPPER_SAFE, Items.COPPER_INGOT, 3_600.0F);
        addSafe("silver_safe", BenchTier.COPPER, InfXBlocks.SILVER_SAFE, InfXItems.SILVER_INGOT, 3_600.0F);
        addSafe("gold_safe", BenchTier.GOLD, InfXBlocks.GOLD_SAFE, Items.GOLD_INGOT, 3_600.0F);
        addSafe("iron_safe", BenchTier.IRON, InfXBlocks.IRON_SAFE, Items.IRON_INGOT, 7_200.0F);
        addSafe("ancient_metal_safe", BenchTier.ANCIENT_METAL, InfXBlocks.ANCIENT_METAL_SAFE, InfXItems.ANCIENT_METAL_INGOT, 14_400.0F);
        addSafe("mithril_safe", BenchTier.MITHRIL, InfXBlocks.MITHRIL_SAFE, InfXItems.MITHRIL_INGOT, 28_800.0F);
        addSafe("adamantium_safe", BenchTier.ADAMANTIUM, InfXBlocks.ADAMANTIUM_SAFE, InfXItems.ADAMANTIUM_INGOT, 57_600.0F);
        addShapeless(
                "flint_to_flint_chips",
                BenchTier.HAND,
                100.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.FLINT_CHIP,
                4,
                List.of(Ingredient.of(Items.FLINT)));
        addShaped(
                "flint_from_flint_chips",
                BenchTier.HAND,
                100.0F,
                CraftingBookCategory.MISC,
                "",
                Items.FLINT,
                1,
                Map.of('F', Ingredient.of(InfXItems.FLINT_CHIP)),
                List.of("FF", "FF"));
        addShapeless(
                "leather_to_sinew",
                BenchTier.HAND,
                50.0F,
                CraftingBookCategory.MISC,
                "",
                InfXItems.SINEW,
                4,
                List.of(Ingredient.of(Items.LEATHER)));
        addShardRecipes("obsidian", raw("obsidian_shard"), Blocks.OBSIDIAN, 200.0F);
        addShardRecipes("diamond", raw("diamond_shard"), Items.DIAMOND, 1600.0F);
        addShardRecipes("nether_quartz", raw("nether_quartz_shard"), Items.QUARTZ, 900.0F);
        addShardRecipes("glass", raw("glass_shard"), Blocks.GLASS_PANE, 200.0F);
        addBlockRecipes();

        addShaped(
                "clay_furnace",
                BenchTier.HAND,
                320.0F,
                CraftingBookCategory.BUILDING,
                "",
                InfXBlocks.CLAY_FURNACE,
                1,
                Map.of('C', Ingredient.of(Blocks.CLAY)),
                List.of("CC", "CC"));
        addShaped(
                "sandstone_furnace",
                BenchTier.FLINT,
                640.0F,
                CraftingBookCategory.BUILDING,
                "",
                InfXBlocks.SANDSTONE_FURNACE,
                1,
                Map.of('S', Ingredient.of(Blocks.SANDSTONE)),
                List.of("SSS", "S S", "SSS"));
        addShaped(
                "hardened_clay_furnace",
                BenchTier.FLINT,
                1440.0F,
                CraftingBookCategory.BUILDING,
                "",
                InfXBlocks.HARDENED_CLAY_FURNACE,
                1,
                Map.of('T', Ingredient.of(Blocks.TERRACOTTA)),
                List.of("TTT", "T T", "TTT"));
        addShaped(
                "obsidian_furnace",
                BenchTier.FLINT,
                1920.0F,
                CraftingBookCategory.BUILDING,
                "",
                InfXBlocks.OBSIDIAN_FURNACE,
                1,
                Map.of('O', Ingredient.of(Blocks.OBSIDIAN)),
                List.of("OOO", "O O", "OOO"));
        addShaped(
                "netherrack_furnace",
                BenchTier.FLINT,
                1280.0F,
                CraftingBookCategory.BUILDING,
                "",
                InfXBlocks.NETHERRACK_FURNACE,
                1,
                Map.of('N', Ingredient.of(Blocks.NETHERRACK)),
                List.of("NNN", "N N", "NNN"));
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(Blocks.SAND),
                        RecipeCategory.BUILDING_BLOCKS,
                        CookingBookCategory.BLOCKS,
                        Blocks.SANDSTONE,
                        0.1F,
                        200)
                .unlockedBy("has_sand", has(Blocks.SAND))
                .save(output, recipeKey("sand_batch"));
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(Blocks.RED_SAND),
                        RecipeCategory.BUILDING_BLOCKS,
                        CookingBookCategory.BLOCKS,
                        Blocks.RED_SANDSTONE,
                        0.1F,
                        200)
                .unlockedBy("has_red_sand", has(Blocks.RED_SAND))
                .save(output, recipeKey("red_sand_batch"));
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(InfXBlocks.SILVER_ORE, InfXBlocks.DEEPSLATE_SILVER_ORE),
                        RecipeCategory.MISC,
                        CookingBookCategory.MISC,
                        InfXItems.SILVER_INGOT,
                        0.7F,
                        200)
                .unlockedBy("has_silver_ore", has(InfXBlocks.SILVER_ORE))
                .save(output, recipeKey("silver_ingot_from_smelting_silver_ore"));
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(InfXBlocks.MITHRIL_ORE, InfXBlocks.DEEPSLATE_MITHRIL_ORE),
                        RecipeCategory.MISC,
                        CookingBookCategory.MISC,
                        InfXItems.MITHRIL_INGOT,
                        0.7F,
                        200)
                .unlockedBy("has_mithril_ore", has(InfXBlocks.MITHRIL_ORE))
                .save(output, recipeKey("mithril_ingot_from_smelting_mithril_ore"));
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(InfXBlocks.ADAMANTIUM_ORE, InfXBlocks.DEEPSLATE_ADAMANTIUM_ORE),
                        RecipeCategory.MISC,
                        CookingBookCategory.MISC,
                        InfXItems.ADAMANTIUM_INGOT,
                        0.7F,
                        200)
                .unlockedBy("has_adamantium_ore", has(InfXBlocks.ADAMANTIUM_ORE))
                .save(output, recipeKey("adamantium_ingot_from_smelting_adamantium_ore"));

        addShaped(
                "flint_hatchet",
                BenchTier.HAND,
                150.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                InfXItems.FLINT_HATCHET,
                1,
                Map.of(
                        'F', Ingredient.of(Items.FLINT),
                        'S', Ingredient.of(Items.STICK),
                        'B', ingredient(InfXItemTags.BINDINGS)),
                List.of("FS", "BS"));
        addShaped(
                "flint_shovel",
                BenchTier.FLINT,
                150.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                InfXItems.FLINT_SHOVEL,
                1,
                Map.of(
                        'F', Ingredient.of(Items.FLINT),
                        'S', Ingredient.of(Items.STICK),
                        'B', ingredient(InfXItemTags.BINDINGS)),
                List.of("F ", "S ", "SB"));
        addShaped(
                "flint_axe",
                BenchTier.FLINT,
                375.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                InfXItems.FLINT_AXE,
                1,
                Map.of(
                        'F', Ingredient.of(Items.FLINT),
                        'S', Ingredient.of(Items.STICK),
                        'B', ingredient(InfXItemTags.BINDINGS)),
                List.of("FF", "FS", "BS"));
        addShaped(
                "obsidian_hatchet",
                BenchTier.HAND,
                315.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(InfxMaterial.OBSIDIAN, EquipmentType.HATCHET),
                1,
                Map.of(
                        'O', Ingredient.of(Items.OBSIDIAN),
                        'S', Ingredient.of(Items.STICK),
                        'B', ingredient(InfXItemTags.BINDINGS)),
                List.of("OS", "BS"));
        addShaped(
                "obsidian_shovel",
                BenchTier.FLINT,
                315.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(InfxMaterial.OBSIDIAN, EquipmentType.SHOVEL),
                1,
                Map.of(
                        'O', Ingredient.of(Items.OBSIDIAN),
                        'S', Ingredient.of(Items.STICK),
                        'B', ingredient(InfXItemTags.BINDINGS)),
                List.of("O ", "S ", "SB"));
        addShaped(
                "obsidian_axe",
                BenchTier.FLINT,
                795.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(InfxMaterial.OBSIDIAN, EquipmentType.AXE),
                1,
                Map.of(
                        'O', Ingredient.of(Items.OBSIDIAN),
                        'S', Ingredient.of(Items.STICK),
                        'B', ingredient(InfXItemTags.BINDINGS)),
                List.of("OO", "OS", "BS"));
        addShaped(
                "wood_cudgel",
                BenchTier.HAND,
                105.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(InfxMaterial.WOOD, EquipmentType.CUDGEL),
                1,
                Map.of(
                        'P', ingredient(ItemTags.PLANKS),
                        'S', Ingredient.of(Items.STICK)),
                List.of("P", "S"));
        addShaped(
                "wood_club",
                BenchTier.FLINT,
                185.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(InfxMaterial.WOOD, EquipmentType.CLUB),
                1,
                Map.of(
                        'P', ingredient(ItemTags.PLANKS),
                        'S', Ingredient.of(Items.STICK)),
                List.of("P", "P", "S"));
        addShaped(
                "flint_knife",
                BenchTier.HAND,
                150.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(InfxMaterial.FLINT, EquipmentType.KNIFE),
                1,
                Map.of(
                        'F', Ingredient.of(Items.FLINT),
                        'S', Ingredient.of(Items.STICK),
                        'B', ingredient(InfXItemTags.BINDINGS)),
                List.of("FB", "S "));
        addShaped(
                "obsidian_knife",
                BenchTier.HAND,
                290.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(InfxMaterial.OBSIDIAN, EquipmentType.KNIFE),
                1,
                Map.of(
                        'O', Ingredient.of(Items.OBSIDIAN),
                        'S', Ingredient.of(Items.STICK),
                        'B', ingredient(InfXItemTags.BINDINGS)),
                List.of("OB", "S "));
        addShaped(
                "wood_bow",
                BenchTier.FLINT,
                150.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(InfxMaterial.WOOD, EquipmentType.BOW),
                1,
                Map.of(
                        'S', Ingredient.of(Items.STICK),
                        'B', ingredient(InfXItemTags.BINDINGS)),
                List.of(" SB", "S B", " SB"));
        addMetalBow(
                "ancient_metal",
                InfxMaterial.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                1750.0F,
                InfXItems.ANCIENT_METAL_INGOT);
        addMetalBow(
                "mithril",
                InfxMaterial.MITHRIL,
                BenchTier.MITHRIL,
                6550.0F,
                InfXItems.MITHRIL_INGOT);
        addStrippedLogWorkbenchRecipes();
        addMetalWorkbench("copper", BenchTier.FLINT, 605.0F, Items.COPPER_INGOT, InfXBlocks.COPPER_WORKBENCH);
        addMetalWorkbench("silver", BenchTier.FLINT, 605.0F, InfXItems.SILVER_INGOT, InfXBlocks.SILVER_WORKBENCH);
        addMetalWorkbench("gold", BenchTier.FLINT, 605.0F, Items.GOLD_INGOT, InfXBlocks.GOLD_WORKBENCH);
        addMetalWorkbench("iron", BenchTier.COPPER, 1005.0F, Items.IRON_INGOT, InfXBlocks.IRON_WORKBENCH);
        addMetalWorkbench(
                "ancient_metal",
                BenchTier.IRON,
                1805.0F,
                InfXItems.ANCIENT_METAL_INGOT,
                InfXBlocks.ANCIENT_METAL_WORKBENCH);
        addMetalWorkbench(
                "mithril", BenchTier.ANCIENT_METAL, 6605.0F, InfXItems.MITHRIL_INGOT, InfXBlocks.MITHRIL_WORKBENCH);
        addMetalWorkbench(
                "adamantium",
                BenchTier.MITHRIL,
                25805.0F,
                InfXItems.ADAMANTIUM_INGOT,
                InfXBlocks.ADAMANTIUM_WORKBENCH);
        addMetalStorageRecipes("silver", InfXItems.SILVER_INGOT, InfXBlocks.SILVER_BLOCK, 3_600.0F);
        addMetalStorageRecipes(
                "ancient_metal", InfXItems.ANCIENT_METAL_INGOT, InfXBlocks.ANCIENT_METAL_BLOCK, 14_400.0F);
        addMetalStorageRecipes("mithril", InfXItems.MITHRIL_INGOT, InfXBlocks.MITHRIL_BLOCK, 57_600.0F);
        addMetalStorageRecipes(
                "adamantium", InfXItems.ADAMANTIUM_INGOT, InfXBlocks.ADAMANTIUM_BLOCK, 230_400.0F);
        addShaped(
                "mithril_rune_stone",
                BenchTier.MITHRIL,
                3_200.0F,
                CraftingBookCategory.BUILDING,
                "",
                InfXBlocks.MITHRIL_RUNE_STONE,
                1,
                Map.of(
                        'N', Ingredient.of(InfXItems.MITHRIL_NUGGET),
                        'O', Ingredient.of(Blocks.OBSIDIAN)),
                List.of(" N ", "NON", " N "));
        addShaped(
                "adamantium_rune_stone",
                BenchTier.ADAMANTIUM,
                12_800.0F,
                CraftingBookCategory.BUILDING,
                "",
                InfXBlocks.ADAMANTIUM_RUNE_STONE,
                1,
                Map.of(
                        'N', Ingredient.of(InfXItems.ADAMANTIUM_NUGGET),
                        'O', Ingredient.of(Blocks.OBSIDIAN)),
                List.of(" N ", "NON", " N "));
        addMetalAnvilRecipes();
        addMetalConversions("silver", 400.0F, InfXItems.SILVER_NUGGET, InfXItems.SILVER_INGOT);
        addMetalConversions(
                "ancient_metal",
                1600.0F,
                InfXItems.catalog().raw("ancient_metal_nugget").holder(),
                InfXItems.ANCIENT_METAL_INGOT);
        addMetalConversions("mithril", 6400.0F, InfXItems.MITHRIL_NUGGET, InfXItems.MITHRIL_INGOT);
        addMetalConversions(
                "adamantium",
                25600.0F,
                InfXItems.ADAMANTIUM_NUGGET,
                InfXItems.ADAMANTIUM_INGOT);
        addCoinConversions("copper", raw("copper_coin"), Items.COPPER_NUGGET);
        addCoinConversions("silver", raw("silver_coin"), InfXItems.SILVER_NUGGET);
        addCoinConversions("gold", raw("gold_coin"), Items.GOLD_NUGGET);
        addCoinConversions("ancient_metal", raw("ancient_metal_coin"),
                InfXItems.catalog().raw("ancient_metal_nugget").holder());
        addCoinConversions("mithril", raw("mithril_coin"), InfXItems.MITHRIL_NUGGET);
        addCoinConversions("adamantium", raw("adamantium_coin"), InfXItems.ADAMANTIUM_NUGGET);
        addChainConversions(
                "copper", BenchTier.COPPER, 400.0F, Items.COPPER_NUGGET);
        addChainConversions(
                "silver", BenchTier.COPPER, 400.0F, InfXItems.SILVER_NUGGET);
        addChainConversions(
                "gold", BenchTier.COPPER, 400.0F, Items.GOLD_NUGGET);
        addChainConversions(
                "iron", BenchTier.IRON, 800.0F, Items.IRON_NUGGET);
        addChainConversions(
                "ancient_metal",
                BenchTier.ANCIENT_METAL,
                1600.0F,
                InfXItems.catalog().raw("ancient_metal_nugget").holder());
        addChainConversions(
                "mithril", BenchTier.MITHRIL, 6400.0F, InfXItems.MITHRIL_NUGGET);
        addChainConversions(
                "adamantium",
                BenchTier.ADAMANTIUM,
                25600.0F,
                InfXItems.ADAMANTIUM_NUGGET);


        addCoreMetalTools(
                "copper",
                BenchTier.COPPER,
                400.0F,
                Items.COPPER_INGOT,
                InfXItems.COPPER_PICKAXE,
                InfXItems.COPPER_SHOVEL,
                InfXItems.COPPER_AXE,
                InfXItems.COPPER_HOE,
                InfXItems.COPPER_SWORD);
        addCoreMetalTools(
                "iron",
                BenchTier.IRON,
                800.0F,
                Items.IRON_INGOT,
                InfXItems.IRON_PICKAXE,
                InfXItems.IRON_SHOVEL,
                InfXItems.IRON_AXE,
                InfXItems.IRON_HOE,
                InfXItems.IRON_SWORD);
        addCatalogCoreMetalTools("silver", InfxMaterial.SILVER, BenchTier.COPPER, 400.0F, InfXItems.SILVER_INGOT);
        addCatalogCoreMetalTools("gold", InfxMaterial.GOLD, BenchTier.COPPER, 400.0F, Items.GOLD_INGOT);
        addCatalogCoreMetalTools(
                "ancient_metal",
                InfxMaterial.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                1600.0F,
                InfXItems.ANCIENT_METAL_INGOT);
        addCatalogCoreMetalTools(
                "mithril", InfxMaterial.MITHRIL, BenchTier.MITHRIL, 6400.0F, InfXItems.MITHRIL_INGOT);
        addCatalogCoreMetalTools(
                "adamantium",
                InfxMaterial.ADAMANTIUM,
                BenchTier.ADAMANTIUM,
                25600.0F,
                InfXItems.ADAMANTIUM_INGOT);
        addCatalogSpecialMetalTools(
                "copper", InfxMaterial.COPPER, BenchTier.COPPER, 400.0F, Items.COPPER_INGOT);
        addCatalogSpecialMetalTools(
                "silver", InfxMaterial.SILVER, BenchTier.COPPER, 400.0F, InfXItems.SILVER_INGOT);
        addCatalogSpecialMetalTools(
                "gold", InfxMaterial.GOLD, BenchTier.COPPER, 400.0F, Items.GOLD_INGOT);
        addCatalogSpecialMetalTools(
                "iron", InfxMaterial.IRON, BenchTier.IRON, 800.0F, Items.IRON_INGOT);
        addCatalogSpecialMetalTools(
                "ancient_metal",
                InfxMaterial.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                1600.0F,
                InfXItems.ANCIENT_METAL_INGOT);
        addCatalogSpecialMetalTools(
                "mithril", InfxMaterial.MITHRIL, BenchTier.MITHRIL, 6400.0F, InfXItems.MITHRIL_INGOT);
        addCatalogSpecialMetalTools(
                "adamantium",
                InfxMaterial.ADAMANTIUM,
                BenchTier.ADAMANTIUM,
                25600.0F,
                InfXItems.ADAMANTIUM_INGOT);
        addMetalDagger("copper", InfxMaterial.COPPER, BenchTier.COPPER, 425.0F, Items.COPPER_INGOT);
        addMetalDagger("silver", InfxMaterial.SILVER, BenchTier.COPPER, 425.0F, InfXItems.SILVER_INGOT);
        addMetalDagger("gold", InfxMaterial.GOLD, BenchTier.COPPER, 425.0F, Items.GOLD_INGOT);
        addMetalDagger("iron", InfxMaterial.IRON, BenchTier.IRON, 825.0F, Items.IRON_INGOT);
        addMetalDagger(
                "ancient_metal",
                InfxMaterial.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                1625.0F,
                InfXItems.ANCIENT_METAL_INGOT);
        addMetalDagger(
                "mithril", InfxMaterial.MITHRIL, BenchTier.MITHRIL, 6425.0F, InfXItems.MITHRIL_INGOT);
        addMetalDagger(
                "adamantium",
                InfxMaterial.ADAMANTIUM,
                BenchTier.ADAMANTIUM,
                25625.0F,
                InfXItems.ADAMANTIUM_INGOT);

        addArrow("flint", InfxMaterial.FLINT, BenchTier.FLINT, 75.0F, InfXItems.FLINT_CHIP);
        addArrow(
                "obsidian",
                InfxMaterial.OBSIDIAN,
                BenchTier.FLINT,
                200.0F / 9.0F + 50.0F,
                InfXItems.OBSIDIAN_SHARD);
        addArrow("copper", InfxMaterial.COPPER, BenchTier.COPPER, 400.0F / 9.0F + 50.0F, Items.COPPER_NUGGET);
        addArrow(
                "silver",
                InfxMaterial.SILVER,
                BenchTier.COPPER,
                400.0F / 9.0F + 50.0F,
                InfXItems.SILVER_NUGGET);
        addArrow("gold", InfxMaterial.GOLD, BenchTier.COPPER, 400.0F / 9.0F + 50.0F, Items.GOLD_NUGGET);
        addArrow("iron", InfxMaterial.IRON, BenchTier.IRON, 800.0F / 9.0F + 50.0F, Items.IRON_NUGGET);
        addArrow(
                "ancient_metal",
                InfxMaterial.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                1600.0F / 9.0F + 50.0F,
                InfXItems.catalog().raw("ancient_metal_nugget").holder());
        addArrow(
                "mithril",
                InfxMaterial.MITHRIL,
                BenchTier.MITHRIL,
                6400.0F / 9.0F + 50.0F,
                InfXItems.MITHRIL_NUGGET);
        addArrow(
                "adamantium",
                InfxMaterial.ADAMANTIUM,
                BenchTier.ADAMANTIUM,
                25600.0F / 9.0F + 50.0F,
                InfXItems.ADAMANTIUM_NUGGET);

        addArrowDismantling("flint", InfxMaterial.FLINT, InfXItems.FLINT_CHIP);
        addArrowDismantling("obsidian", InfxMaterial.OBSIDIAN, InfXItems.OBSIDIAN_SHARD);
        addArrowDismantling("copper", InfxMaterial.COPPER, Items.COPPER_NUGGET);
        addArrowDismantling("silver", InfxMaterial.SILVER, InfXItems.SILVER_NUGGET);
        addArrowDismantling("gold", InfxMaterial.GOLD, Items.GOLD_NUGGET);
        addArrowDismantling("iron", InfxMaterial.IRON, Items.IRON_NUGGET);
        addArrowDismantling(
                "ancient_metal",
                InfxMaterial.ANCIENT_METAL,
                InfXItems.catalog().raw("ancient_metal_nugget").holder());
        addArrowDismantling("mithril", InfxMaterial.MITHRIL, InfXItems.MITHRIL_NUGGET);
        addArrowDismantling("adamantium", InfxMaterial.ADAMANTIUM, InfXItems.ADAMANTIUM_NUGGET);

        addFishingRod("flint", InfxMaterial.FLINT, BenchTier.FLINT, 75.0F, InfXItems.FLINT_CHIP);
        addFishingRod(
                "obsidian",
                InfxMaterial.OBSIDIAN,
                BenchTier.FLINT,
                200.0F / 9.0F + 50.0F,
                InfXItems.OBSIDIAN_SHARD);
        addFishingRod("copper", InfxMaterial.COPPER, BenchTier.COPPER, 400.0F / 9.0F + 50.0F, Items.COPPER_NUGGET);
        addFishingRod(
                "silver",
                InfxMaterial.SILVER,
                BenchTier.COPPER,
                400.0F / 9.0F + 50.0F,
                InfXItems.SILVER_NUGGET);
        addFishingRod("gold", InfxMaterial.GOLD, BenchTier.COPPER, 400.0F / 9.0F + 50.0F, Items.GOLD_NUGGET);
        addFishingRod("iron", InfxMaterial.IRON, BenchTier.IRON, 800.0F / 9.0F + 50.0F, Items.IRON_NUGGET);
        addFishingRod(
                "ancient_metal",
                InfxMaterial.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                1600.0F / 9.0F + 50.0F,
                InfXItems.catalog().raw("ancient_metal_nugget").holder());
        addFishingRod(
                "mithril",
                InfxMaterial.MITHRIL,
                BenchTier.MITHRIL,
                6400.0F / 9.0F + 50.0F,
                InfXItems.MITHRIL_NUGGET);
        addFishingRod(
                "adamantium",
                InfxMaterial.ADAMANTIUM,
                BenchTier.ADAMANTIUM,
                25600.0F / 9.0F + 50.0F,
                InfXItems.ADAMANTIUM_NUGGET);

        // InfX: every hook material has a carrot on a stick, craftable from its rod and back again.
        for (InfxMaterial hookMaterial : InfXItems.FISHING_HOOK_MATERIALS) {
            addShapeless(
                    hookMaterial.path() + "_carrot_on_a_stick",
                    BenchTier.HAND,
                    40.0F,
                    CraftingBookCategory.EQUIPMENT,
                    "",
                    InfXItems.CARROT_ON_A_STICKS.get(hookMaterial).get(),
                    1,
                    List.of(
                            Ingredient.of(equipment(hookMaterial, EquipmentType.FISHING_ROD)),
                            Ingredient.of(Items.CARROT)));
            addShapeless(
                    hookMaterial.path() + "_carrot_on_a_stick_dismantling",
                    BenchTier.HAND,
                    40.0F,
                    CraftingBookCategory.EQUIPMENT,
                    "",
                    equipment(hookMaterial, EquipmentType.FISHING_ROD),
                    1,
                    List.of(Ingredient.of(InfXItems.CARROT_ON_A_STICKS.get(hookMaterial).get())));
        }

        // InfX warped-fungus-on-a-stick variants follow the same rod-and-back pattern.
        for (InfxMaterial hookMaterial : InfXItems.FISHING_HOOK_MATERIALS) {
            addShapeless(
                    hookMaterial.path() + "_warped_fungus_on_a_stick",
                    BenchTier.HAND,
                    40.0F,
                    CraftingBookCategory.EQUIPMENT,
                    "",
                    InfXItems.WARPED_FUNGUS_ON_A_STICKS.get(hookMaterial).get(),
                    1,
                    List.of(
                            Ingredient.of(equipment(hookMaterial, EquipmentType.FISHING_ROD)),
                            Ingredient.of(Items.WARPED_FUNGUS)));
            addShapeless(
                    hookMaterial.path() + "_warped_fungus_on_a_stick_dismantling",
                    BenchTier.HAND,
                    40.0F,
                    CraftingBookCategory.EQUIPMENT,
                    "",
                    equipment(hookMaterial, EquipmentType.FISHING_ROD),
                    1,
                    List.of(Ingredient.of(InfXItems.WARPED_FUNGUS_ON_A_STICKS.get(hookMaterial).get())));
        }

        addArmorSet("leather", InfxMaterial.LEATHER, BenchTier.FLINT, 100.0F, Items.LEATHER, false);
        for (EquipmentType piece : EquipmentType.platePieces()) {
            addDyeRecipe("leather_" + piece.path() + "_dyed", equipment(InfxMaterial.LEATHER, piece));
        }
        addMetalArmorSets(
                "copper", InfxMaterial.COPPER, BenchTier.COPPER, 400.0F, Items.COPPER_INGOT);
        addMetalArmorSets(
                "silver", InfxMaterial.SILVER, BenchTier.COPPER, 400.0F, InfXItems.SILVER_INGOT);
        addMetalArmorSets(
                "gold", InfxMaterial.GOLD, BenchTier.COPPER, 400.0F, Items.GOLD_INGOT);
        addArmorSet(
                "rusted_iron",
                InfxMaterial.RUSTED_IRON,
                BenchTier.COPPER,
                400.0F * 4.0F / 9.0F,
                raw("rusted_iron_chain"),
                true);
        addMetalArmorSets(
                "iron", InfxMaterial.IRON, BenchTier.IRON, 800.0F, Items.IRON_INGOT);
        addMetalArmorSets(
                "ancient_metal",
                InfxMaterial.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                1600.0F,
                InfXItems.ANCIENT_METAL_INGOT);
        addMetalArmorSets(
                "mithril", InfxMaterial.MITHRIL, BenchTier.MITHRIL, 6400.0F, InfXItems.MITHRIL_INGOT);
        addMetalArmorSets(
                "adamantium",
                InfxMaterial.ADAMANTIUM,
                BenchTier.ADAMANTIUM,
                25600.0F,
                InfXItems.ADAMANTIUM_INGOT);
    }

    private void addStrippedLogWorkbenchRecipes() {
        for (InfXBlocks.StrippedLogWorkbenchSet workbench : InfXBlocks.STRIPPED_LOG_WORKBENCHES) {
            String prefix = "stripped_" + workbench.wood();
            addShaped(
                    prefix + "_flint_workbench",
                    BenchTier.HAND,
                    270.0F,
                    CraftingBookCategory.BUILDING,
                    "",
                    workbench.flint(),
                    1,
                    Map.of(
                            'F', Ingredient.of(Items.FLINT),
                            'B', ingredient(InfXItemTags.BINDINGS),
                            'S', Ingredient.of(Items.STICK),
                            'L', Ingredient.of(workbench.strippedLog())),
                    List.of("FB", "SL"));
            addShaped(
                    prefix + "_obsidian_workbench",
                    BenchTier.HAND,
                    410.0F,
                    CraftingBookCategory.BUILDING,
                    "",
                    workbench.obsidian(),
                    1,
                    Map.of(
                            'O', Ingredient.of(Items.OBSIDIAN),
                            'B', ingredient(InfXItemTags.BINDINGS),
                            'S', Ingredient.of(Items.STICK),
                            'L', Ingredient.of(workbench.strippedLog())),
                    List.of("OB", "SL"));
        }
    }

    private void addMetalConversions(
            String material,
            float ingotDifficulty,
            ItemLike nugget,
            ItemLike ingot) {
        addShaped(
                material + "_ingot_from_nuggets",
                BenchTier.FLINT,
                ingotDifficulty,
                CraftingBookCategory.MISC,
                "",
                ingot,
                1,
                Map.of('N', Ingredient.of(nugget)),
                List.of("NNN", "NNN", "NNN"));
        addShapeless(
                material + "_nuggets_from_ingot",
                BenchTier.HAND,
                ingotDifficulty,
                CraftingBookCategory.MISC,
                "",
                nugget,
                9,
                List.of(Ingredient.of(ingot)));
    }

    private void addCoinConversions(String material, ItemLike coin, ItemLike nugget) {
        addShapeless(
                material + "_nugget_from_coin",
                BenchTier.HAND,
                25.0F,
                CraftingBookCategory.MISC,
                "",
                nugget,
                1,
                List.of(Ingredient.of(coin)));
        addShapeless(
                material + "_coin_from_nugget",
                BenchTier.HAND,
                100.0F,
                CraftingBookCategory.MISC,
                "",
                coin,
                1,
                List.of(Ingredient.of(nugget)));
    }

    private void addR196Buckets() {
        for (InfxMaterial material : InfXItems.BUCKET_MATERIALS) {
            addShaped(
                    material.path() + "_bucket",
                    bucketBench(material),
                    bucketIngotDifficulty(material) * 3.0F,
                    CraftingBookCategory.MISC,
                    "",
                    InfXItems.bucket(material, InfxBucketItem.Contents.EMPTY),
                    1,
                    Map.of('I', Ingredient.of(bucketIngot(material))),
                    List.of("I I", " I "));
            addShapeless(
                    material.path() + "_bucket_from_stone_bucket",
                    BenchTier.HAND,
                    100.0F,
                    CraftingBookCategory.MISC,
                    "",
                    InfXItems.bucket(material, InfxBucketItem.Contents.EMPTY),
                    1,
                    List.of(Ingredient.of(InfXItems.bucket(material, InfxBucketItem.Contents.STONE))));
        }
    }

    private static ItemLike bucketIngot(InfxMaterial material) {
        return switch (material) {
            case COPPER -> Items.COPPER_INGOT;
            case SILVER -> InfXItems.SILVER_INGOT;
            case GOLD -> Items.GOLD_INGOT;
            case IRON -> Items.IRON_INGOT;
            case ANCIENT_METAL -> InfXItems.ANCIENT_METAL_INGOT;
            case MITHRIL -> InfXItems.MITHRIL_INGOT;
            case ADAMANTIUM -> InfXItems.ADAMANTIUM_INGOT;
            default -> throw new IllegalArgumentException("No INFX bucket ingot for " + material);
        };
    }

    private static BenchTier bucketBench(InfxMaterial material) {
        return switch (material) {
            case COPPER, SILVER, GOLD -> BenchTier.COPPER;
            case IRON -> BenchTier.IRON;
            case ANCIENT_METAL -> BenchTier.ANCIENT_METAL;
            case MITHRIL -> BenchTier.MITHRIL;
            case ADAMANTIUM -> BenchTier.ADAMANTIUM;
            default -> throw new IllegalArgumentException("No INFX bucket bench for " + material);
        };
    }

    private static float bucketIngotDifficulty(InfxMaterial material) {
        return switch (material) {
            case COPPER, SILVER, GOLD -> 400.0F;
            case IRON -> 800.0F;
            case ANCIENT_METAL -> 1_600.0F;
            case MITHRIL -> 6_400.0F;
            case ADAMANTIUM -> 25_600.0F;
            default -> throw new IllegalArgumentException("No INFX bucket difficulty for " + material);
        };
    }

    private void addChainConversions(
            String material,
            BenchTier requiredBench,
            float ingotDifficulty,
            ItemLike nugget) {
        float chainDifficulty = ingotDifficulty * 4.0F / 9.0F;
        ItemLike chain = raw(material + "_chain");
        addShaped(
                material + "_chain_from_nuggets",
                requiredBench,
                chainDifficulty,
                CraftingBookCategory.MISC,
                "",
                chain,
                1,
                Map.of('N', Ingredient.of(nugget)),
                List.of(" N ", "N N", " N "));
        addShaped(
                material + "_nuggets_from_chain",
                BenchTier.HAND,
                chainDifficulty,
                CraftingBookCategory.MISC,
                "",
                nugget,
                4,
                Map.of('C', Ingredient.of(chain)),
                List.of("C"));
    }

    private void addMetalArmorSets(
            String material,
            InfxMaterial equipmentMaterial,
            BenchTier requiredBench,
            float ingotDifficulty,
            ItemLike ingot) {
        addArmorSet(material, equipmentMaterial, requiredBench, ingotDifficulty, ingot, false);
        addArmorSet(
                material,
                equipmentMaterial,
                requiredBench,
                ingotDifficulty * 4.0F / 9.0F,
                raw(material + "_chain"),
                true);
    }

    private void addArmorSet(
            String material,
            InfxMaterial equipmentMaterial,
            BenchTier requiredBench,
            float componentDifficulty,
            ItemLike component,
            boolean chainmail) {
        List<EquipmentType> pieces = chainmail
                ? EquipmentType.chainPieces()
                : EquipmentType.platePieces();
        for (EquipmentType piece : pieces) {
            addShaped(
                    material + "_" + piece.path(),
                    requiredBench,
                    componentDifficulty * piece.durabilityComponents(),
                    CraftingBookCategory.EQUIPMENT,
                    "",
                    equipment(equipmentMaterial, piece),
                    1,
                    Map.of('C', Ingredient.of(component)),
                    armorPattern(piece));
        }
    }

    private static List<String> armorPattern(EquipmentType piece) {
        return switch (piece) {
            case HELMET, CHAINMAIL_HELMET -> List.of("CCC", "C C");
            case CHESTPLATE, CHAINMAIL_CHESTPLATE -> List.of("C C", "CCC", "CCC");
            case LEGGINGS, CHAINMAIL_LEGGINGS -> List.of("CCC", "C C", "C C");
            case BOOTS, CHAINMAIL_BOOTS -> List.of("C C", "C C");
            default -> throw new IllegalArgumentException("Not an armor piece: " + piece);
        };
    }

    private static ItemLike raw(String path) {
        return InfXItems.catalog().raw(path).holder();
    }

    private void addShardRecipes(String name, ItemLike shard, ItemLike whole, float difficulty) {
        addShaped(
                name + "_from_shards",
                BenchTier.FLINT,
                difficulty,
                CraftingBookCategory.MISC,
                "",
                whole,
                1,
                Map.of('S', Ingredient.of(shard)),
                List.of("SSS", "SSS", "SSS"));
        addShapeless(
                name + "_to_shards",
                BenchTier.HAND,
                difficulty,
                CraftingBookCategory.MISC,
                "",
                shard,
                9,
                List.of(Ingredient.of(whole)));
    }

    private void addBlockRecipes() {
        addShaped(
                "snow_slab",
                BenchTier.HAND,
                100.0F,
                CraftingBookCategory.BUILDING,
                "",
                InfXBlocks.SNOW_SLAB,
                1,
                Map.of('S', Ingredient.of(Items.SNOWBALL)),
                List.of("SS", "SS"));
        // Keep the INFX iron-nugget alternative alongside the restored
        // vanilla flint-and-steel recipe, which uses a full iron ingot.
        addShaped(
                "flint_and_steel",
                BenchTier.FLINT,
                100.0F,
                CraftingBookCategory.MISC,
                "",
                Items.FLINT_AND_STEEL,
                1,
                Map.of('N', Ingredient.of(Items.IRON_NUGGET), 'F', Ingredient.of(Items.FLINT)),
                List.of("N ", " F"));
        // InfX CraftingManager: two leads per silk/string or sinew leash, knotted with an
        // INFX gelatinous sphere (the vanilla slime ball no longer drops in the INFX world).
        // The restored vanilla lead recipe needs a slime ball and therefore stays inert.
        addShaped(
                "lead",
                BenchTier.HAND,
                150.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                Items.LEAD,
                2,
                Map.of('~', Ingredient.of(Items.STRING), 'O', ingredient(InfXItemTags.GELATINOUS_SPHERES)),
                List.of("~~ ", "~O ", "  ~"));
        addShaped(
                "lead_from_sinew",
                BenchTier.HAND,
                150.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                Items.LEAD,
                2,
                Map.of('~', Ingredient.of(InfXItems.SINEW), 'O', ingredient(InfXItemTags.GELATINOUS_SPHERES)),
                List.of("~~ ", "~O ", "  ~"));
    }

    private void addCoreMetalTools(
            String material,
            BenchTier requiredBench,
            float ingotDifficulty,
            ItemLike ingot,
            ItemLike pickaxe,
            ItemLike shovel,
            ItemLike axe,
            ItemLike hoe,
            ItemLike sword) {
        Map<Character, Ingredient> key = Map.of(
                'I', Ingredient.of(ingot),
                'S', Ingredient.of(Items.STICK));
        addShaped(
                material + "_pickaxe",
                requiredBench,
                ingotDifficulty * 3.0F + STICK_DIFFICULTY * 2.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                pickaxe,
                1,
                key,
                List.of("III", " S ", " S "));
        addShaped(
                material + "_shovel",
                requiredBench,
                ingotDifficulty + STICK_DIFFICULTY * 2.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                shovel,
                1,
                key,
                List.of("I", "S", "S"));
        addShaped(
                material + "_axe",
                requiredBench,
                ingotDifficulty * 3.0F + STICK_DIFFICULTY * 2.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                axe,
                1,
                key,
                List.of("II", "IS", " S"));
        addShaped(
                material + "_hoe",
                requiredBench,
                ingotDifficulty * 2.0F + STICK_DIFFICULTY * 2.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                hoe,
                1,
                key,
                List.of("II", " S", " S"));
        addShaped(
                material + "_sword",
                requiredBench,
                ingotDifficulty * 2.0F + STICK_DIFFICULTY,
                CraftingBookCategory.EQUIPMENT,
                "",
                sword,
                1,
                key,
                List.of("I", "I", "S"));
    }

    private void addCatalogCoreMetalTools(
            String material,
            InfxMaterial equipmentMaterial,
            BenchTier requiredBench,
            float ingotDifficulty,
            ItemLike ingot) {
        addCoreMetalTools(
                material,
                requiredBench,
                ingotDifficulty,
                ingot,
                equipment(equipmentMaterial, EquipmentType.PICKAXE),
                equipment(equipmentMaterial, EquipmentType.SHOVEL),
                equipment(equipmentMaterial, EquipmentType.AXE),
                equipment(equipmentMaterial, EquipmentType.HOE),
                equipment(equipmentMaterial, EquipmentType.SWORD));
    }

    private void addCatalogSpecialMetalTools(
            String material,
            InfxMaterial equipmentMaterial,
            BenchTier requiredBench,
            float ingotDifficulty,
            ItemLike ingot) {
        Map<Character, Ingredient> key = Map.of(
                'I', Ingredient.of(ingot),
                'S', Ingredient.of(Items.STICK));
        addShaped(
                material + "_mattock",
                requiredBench,
                ingotDifficulty * 4.0F + STICK_DIFFICULTY * 2.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(equipmentMaterial, EquipmentType.MATTOCK),
                1,
                key,
                List.of("III", " SI", " S "));
        addShaped(
                material + "_battle_axe",
                requiredBench,
                ingotDifficulty * 4.0F + STICK_DIFFICULTY * 2.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(equipmentMaterial, EquipmentType.BATTLE_AXE),
                1,
                key,
                List.of("I I", "ISI", " S "));
        addShaped(
                material + "_war_hammer",
                requiredBench,
                ingotDifficulty * 5.0F + STICK_DIFFICULTY * 2.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(equipmentMaterial, EquipmentType.WAR_HAMMER),
                1,
                key,
                List.of("III", "ISI", " S "));
        addShaped(
                material + "_scythe",
                requiredBench,
                ingotDifficulty * 2.0F + STICK_DIFFICULTY * 3.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(equipmentMaterial, EquipmentType.SCYTHE),
                1,
                key,
                List.of("SI ", "S I", "S  "));
        addShaped(
                material + "_hatchet",
                requiredBench,
                ingotDifficulty + STICK_DIFFICULTY * 2.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(equipmentMaterial, EquipmentType.HATCHET),
                1,
                key,
                List.of("SI", "S "));
        addShaped(
                material + "_shears",
                requiredBench,
                ingotDifficulty * 2.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(equipmentMaterial, EquipmentType.SHEARS),
                1,
                Map.of('I', Ingredient.of(ingot)),
                List.of(" I", "I "));
    }

    private void addMetalDagger(
            String material,
            InfxMaterial equipmentMaterial,
            BenchTier requiredBench,
            float difficulty,
            ItemLike ingot) {
        addShaped(
                material + "_dagger",
                requiredBench,
                difficulty,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(equipmentMaterial, EquipmentType.DAGGER),
                1,
                Map.of(
                        'I', Ingredient.of(ingot),
                        'S', Ingredient.of(Items.STICK)),
                List.of("I", "S"));
    }

    private void addMetalBow(
            String material,
            InfxMaterial equipmentMaterial,
            BenchTier requiredBench,
            float difficulty,
            ItemLike ingot) {
        addShaped(
                material + "_bow",
                requiredBench,
                difficulty,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(equipmentMaterial, EquipmentType.BOW),
                1,
                Map.of(
                        'I', Ingredient.of(ingot),
                        'S', Ingredient.of(Items.STICK),
                        'B', Ingredient.of(Items.STRING)),
                List.of(" SB", "SIB", " SB"));
    }

    private void addArrow(
            String material,
            InfxMaterial equipmentMaterial,
            BenchTier requiredBench,
            float difficulty,
            ItemLike arrowhead) {
        addShaped(
                material + "_arrow",
                requiredBench,
                difficulty,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(equipmentMaterial, EquipmentType.ARROW),
                1,
                Map.of(
                        'H', Ingredient.of(arrowhead),
                        'S', Ingredient.of(Items.STICK),
                        'F', Ingredient.of(Items.FEATHER)),
                List.of("H", "S", "F"));
    }

    /** InfX fishing rods: hook material, two sticks and two silk strands in the rod shape. */
    private void addFishingRod(
            String material,
            InfxMaterial equipmentMaterial,
            BenchTier requiredBench,
            float difficulty,
            ItemLike hookMaterial) {
        addShaped(
                material + "_fishing_rod",
                requiredBench,
                difficulty,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(equipmentMaterial, EquipmentType.FISHING_ROD),
                1,
                Map.of(
                        '?', Ingredient.of(hookMaterial),
                        '/', Ingredient.of(Items.STICK),
                        '|', Ingredient.of(Items.STRING)),
                List.of("  /", " /|", "/?|"));
    }

    private static ItemLike equipment(InfxMaterial material, EquipmentType type) {
        return InfXItems.catalog().equipment(material, type).holder();
    }

    /** InfX dismantles one arrow back into its single arrowhead material. */
    private void addArrowDismantling(String material, InfxMaterial arrowMaterial, ItemLike arrowhead) {
        addShapeless(
                material + "_arrow_dismantling",
                BenchTier.HAND,
                25.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                arrowhead,
                1,
                List.of(Ingredient.of(equipment(arrowMaterial, EquipmentType.ARROW))));
    }

    private void addMetalWorkbench(
            String material, BenchTier requiredBench, float difficulty, ItemLike ingot, ItemLike result) {
        addShaped(
                material + "_workbench",
                requiredBench,
                difficulty,
                CraftingBookCategory.BUILDING,
                "",
                result,
                1,
                Map.of(
                        'I', Ingredient.of(ingot),
                        'L', Ingredient.of(Items.LEATHER),
                        'S', Ingredient.of(Items.STICK),
                        'P', ingredient(ItemTags.PLANKS)),
                List.of("IL", "SP"));
    }

    private void addMetalStorageRecipes(
            String name, ItemLike ingot, ItemLike block, float difficulty) {
        addShaped(
                name + "_block",
                BenchTier.FLINT,
                difficulty,
                CraftingBookCategory.BUILDING,
                "",
                block,
                1,
                Map.of('I', Ingredient.of(ingot)),
                List.of("III", "III", "III"));
        addShapeless(
                name + "_block_to_ingots",
                BenchTier.HAND,
                difficulty,
                CraftingBookCategory.MISC,
                "",
                ingot,
                9,
                List.of(Ingredient.of(block)));
    }

    private void addMetalAnvilRecipes() {
        addMetalAnvil(
                InfxMaterial.COPPER,
                BenchTier.COPPER,
                Items.COPPER_INGOT,
                Blocks.COPPER_BLOCK,
                12_400.0F);
        addMetalAnvil(InfxMaterial.SILVER, BenchTier.SILVER, InfXItems.SILVER_INGOT, InfXBlocks.SILVER_BLOCK, 12_400.0F);
        addMetalAnvil(InfxMaterial.GOLD, BenchTier.GOLD, Items.GOLD_INGOT, Blocks.GOLD_BLOCK, 12_400.0F);
        addMetalAnvil(InfxMaterial.IRON, BenchTier.IRON, Items.IRON_INGOT, Blocks.IRON_BLOCK, 24_800.0F);
        addMetalAnvil(
                InfxMaterial.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                InfXItems.ANCIENT_METAL_INGOT,
                InfXBlocks.ANCIENT_METAL_BLOCK,
                49_600.0F);
        addMetalAnvil(
                InfxMaterial.MITHRIL,
                BenchTier.MITHRIL,
                InfXItems.MITHRIL_INGOT,
                InfXBlocks.MITHRIL_BLOCK,
                198_400.0F);
        addMetalAnvil(
                InfxMaterial.ADAMANTIUM,
                BenchTier.ADAMANTIUM,
                InfXItems.ADAMANTIUM_INGOT,
                InfXBlocks.ADAMANTIUM_BLOCK,
                793_600.0F);
    }

    private void addMetalAnvil(
            InfxMaterial material,
            BenchTier bench,
            ItemLike ingot,
            ItemLike storageBlock,
            float difficulty) {
        addShaped(
                material.path() + "_anvil",
                bench,
                difficulty,
                CraftingBookCategory.BUILDING,
                "",
                InfXBlocks.metalAnvil(material),
                1,
                Map.of('B', Ingredient.of(storageBlock), 'I', Ingredient.of(ingot)),
                // InfX anvils: a full storage-block top, one centered ingot, and a full ingot base.
                List.of("BBB", " I ", "III"));
    }

    private void addSafe(
            String name,
            BenchTier bench,
            ItemLike result,
            ItemLike ingot,
            float difficulty) {
        addShaped(
                name,
                bench,
                difficulty,
                CraftingBookCategory.BUILDING,
                "",
                result,
                1,
                Map.of('M', Ingredient.of(ingot), 'C', Ingredient.of(Blocks.CHEST)),
                List.of("MMM", "MCM", "MMM"));
    }

    private void addInfXVanillaDoorOverrides() {
        addInfXVanillaDoorOverride("acacia_door", Items.ACACIA_PLANKS, Items.ACACIA_DOOR, "wooden_door");
        addInfXVanillaDoorOverride("bamboo_door", Items.BAMBOO_PLANKS, Items.BAMBOO_DOOR, "wooden_door");
        addInfXVanillaDoorOverride("birch_door", Items.BIRCH_PLANKS, Items.BIRCH_DOOR, "wooden_door");
        addInfXVanillaDoorOverride("cherry_door", Items.CHERRY_PLANKS, Items.CHERRY_DOOR, "wooden_door");
        addInfXVanillaDoorOverride("copper_door", Items.COPPER_INGOT, Items.COPPER_DOOR, "");
        addInfXVanillaDoorOverride("crimson_door", Items.CRIMSON_PLANKS, Items.CRIMSON_DOOR, "wooden_door");
        addInfXVanillaDoorOverride("dark_oak_door", Items.DARK_OAK_PLANKS, Items.DARK_OAK_DOOR, "wooden_door");
        addInfXVanillaDoorOverride("iron_door", Items.IRON_INGOT, Items.IRON_DOOR, "");
        addInfXVanillaDoorOverride("jungle_door", Items.JUNGLE_PLANKS, Items.JUNGLE_DOOR, "wooden_door");
        addInfXVanillaDoorOverride("mangrove_door", Items.MANGROVE_PLANKS, Items.MANGROVE_DOOR, "wooden_door");
        addInfXVanillaDoorOverride("oak_door", Items.OAK_PLANKS, Items.OAK_DOOR, "wooden_door");
        addInfXVanillaDoorOverride("pale_oak_door", Items.PALE_OAK_PLANKS, Items.PALE_OAK_DOOR, "wooden_door");
        addInfXVanillaDoorOverride("spruce_door", Items.SPRUCE_PLANKS, Items.SPRUCE_DOOR, "wooden_door");
        addInfXVanillaDoorOverride("warped_door", Items.WARPED_PLANKS, Items.WARPED_DOOR, "wooden_door");
    }

    private void addInfXVanillaDoorOverride(String name, ItemLike material, ItemLike result, String group) {
        ShapedRecipe recipe = new ShapedRecipe(
                new Recipe.CommonInfo(true),
                new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.REDSTONE, group),
                ShapedRecipePattern.of(Map.of('#', Ingredient.of(material)), List.of("##", "##", "##")),
                new ItemStackTemplate(result.asItem(), 1));
        output.accept(vanillaRecipeKey(name), recipe, null);
    }

    private void addShaped(
            String name,
            BenchTier requiredBench,
            float difficulty,
            CraftingBookCategory category,
            String group,
            ItemLike result,
            int count,
            Map<Character, Ingredient> key,
            List<String> pattern) {
        ShapedRecipe recipe = new ShapedRecipe(
                new Recipe.CommonInfo(true),
                new CraftingRecipe.CraftingBookInfo(category, group),
                ShapedRecipePattern.of(key, pattern),
                new ItemStackTemplate(result.asItem(), count));
        ResourceKey<Recipe<?>> key2 = recipeKey(name);
        output.accept(key2, recipe, null);
        recipeRules.put(key2, RecipeRule.of(key2, key2.identifier(), difficulty, requiredBench));
    }

    /**
     * Vanilla-style armor dyeing ({@code crafting_dye}) for InfX leather
     * armor. Dyeing is a trivial 2x2 hand craft: the armor piece and the dye
     * contribute 25 difficulty each.
     */
    private void addDyeRecipe(String name, ItemLike target) {
        DyeRecipe recipe = new DyeRecipe(
                new Recipe.CommonInfo(true),
                new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, "dyed_armor"),
                Ingredient.of(target),
                ingredient(ItemTags.DYES),
                new ItemStackTemplate(target.asItem(), 1));
        ResourceKey<Recipe<?>> key = recipeKey(name);
        output.accept(key, recipe, null);
        recipeRules.put(key, RecipeRule.of(key, key.identifier(), 50.0F, BenchTier.HAND));
    }

    private void addShapeless(
            String name,
            BenchTier requiredBench,
            float difficulty,
            CraftingBookCategory category,
            String group,
            ItemLike result,
            int count,
            List<Ingredient> ingredients) {
        ShapelessRecipe recipe = new ShapelessRecipe(
                new Recipe.CommonInfo(true),
                new CraftingRecipe.CraftingBookInfo(category, group),
                new ItemStackTemplate(result.asItem(), count),
                ingredients);
        ResourceKey<Recipe<?>> key2 = recipeKey(name);
        output.accept(key2, recipe, null);
        recipeRules.put(key2, RecipeRule.of(key2, key2.identifier(), difficulty, requiredBench));
    }

    private Ingredient ingredient(TagKey<Item> tag) {
        return Ingredient.of(items.getOrThrow(tag));
    }

    private static ResourceKey<Recipe<?>> recipeKey(String path) {
        return ResourceKey.create(Registries.RECIPE, InfiniteX.id(path));
    }

    private static ResourceKey<Recipe<?>> vanillaRecipeKey(String path) {
        return ResourceKey.create(Registries.RECIPE, net.minecraft.resources.Identifier.withDefaultNamespace(path));
    }

    static final class Runner extends RecipeProvider.Runner {
        Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected @NonNull RecipeProvider createRecipeProvider(HolderLookup.@NonNull Provider registries, @NonNull RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public @NonNull String getName() {
            return "InfiniteX recipes";
        }
    }
}
