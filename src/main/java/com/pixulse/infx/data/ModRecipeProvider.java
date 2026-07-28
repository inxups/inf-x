package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.recipe.BenchTier;
import com.pixulse.infx.recipe.TimedShapedRecipe;
import com.pixulse.infx.recipe.TimedShapelessRecipe;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.MiteBucketItem;
import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.registry.tag.InfXItemTags;

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

final class ModRecipeProvider extends RecipeProvider {
    private static final float STICK_DIFFICULTY = 25.0F;

    private ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        addShaped(
                "sugar_from_sugar_cane",
                BenchTier.HAND,
                800.0F,
                CraftingBookCategory.MISC,
                "",
                Items.SUGAR,
                1,
                Map.of('C', Ingredient.of(Items.SUGAR_CANE)),
                List.of("C"));
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
        addShaped(
                "pumpkin_pie",
                BenchTier.FLINT,
                350.0F,
                CraftingBookCategory.MISC,
                "",
                Items.PUMPKIN_PIE,
                1,
                Map.of('P', Ingredient.of(Items.PUMPKIN), 'F', Ingredient.of(InfXItems.FLOUR),
                        'S', Ingredient.of(Items.SUGAR), 'E', Ingredient.of(Items.EGG)),
                List.of("PF", "SE"));
        addShaped(
                "cake",
                BenchTier.FLINT,
                600.0F,
                CraftingBookCategory.MISC,
                "",
                Items.CAKE,
                1,
                Map.of(
                        'M',
                        ingredient(InfXItemTags.MILK_BUCKETS),
                        'F',
                        Ingredient.of(InfXItems.FLOUR),
                        'S',
                        Ingredient.of(Items.SUGAR),
                        'E',
                        Ingredient.of(Items.EGG)),
                List.of("FS", "EM"));
        addShaped(
                "cake_from_milk_bowl",
                BenchTier.FLINT,
                600.0F,
                CraftingBookCategory.MISC,
                "",
                Items.CAKE,
                1,
                Map.of('M', Ingredient.of(InfXItems.MILK_BOWL), 'F', Ingredient.of(InfXItems.FLOUR),
                        'S', Ingredient.of(Items.SUGAR), 'E', Ingredient.of(Items.EGG)),
                List.of("FS", "EM"));
        addShaped(
                "golden_apple",
                BenchTier.HAND,
                500.0F,
                CraftingBookCategory.MISC,
                "",
                Items.GOLDEN_APPLE,
                1,
                Map.of('G', Ingredient.of(Items.GOLD_NUGGET), 'A', Ingredient.of(Items.APPLE)),
                List.of("GGG", "GAG", "GGG"));
        addShaped(
                "mushroom_stew",
                BenchTier.HAND,
                150.0F,
                CraftingBookCategory.MISC,
                "",
                Items.MUSHROOM_STEW,
                1,
                Map.of('B', Ingredient.of(Items.BROWN_MUSHROOM), 'R', Ingredient.of(Items.RED_MUSHROOM),
                        'W', Ingredient.of(InfXItems.WATER_BOWL)),
                List.of("RB", "W "));
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
        addShaped(
                "diamond_enchanting_table",
                BenchTier.MITHRIL,
                6_400.0F,
                CraftingBookCategory.BUILDING,
                "",
                InfXBlocks.DIAMOND_ENCHANTING_TABLE,
                1,
                Map.of('B', Ingredient.of(Items.BOOK), 'G', Ingredient.of(Items.DIAMOND),
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
        addMiteBlockRecipes();

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
                "large_clay_oven",
                BenchTier.FLINT,
                640.0F,
                CraftingBookCategory.BUILDING,
                "",
                InfXBlocks.LARGE_CLAY_OVEN,
                1,
                Map.of('C', Ingredient.of(Blocks.CLAY)),
                List.of("CCC", "C C", "CCC"));
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
                        Ingredient.of(Blocks.SANDSTONE),
                        RecipeCategory.BUILDING_BLOCKS,
                        CookingBookCategory.BLOCKS,
                        Blocks.GLASS,
                        0.1F,
                        200)
                .unlockedBy("has_sandstone", has(Blocks.SANDSTONE))
                .save(output, recipeKey("sandstone_to_glass"));
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(InfXBlocks.SILVER_ORE),
                        RecipeCategory.MISC,
                        CookingBookCategory.MISC,
                        InfXItems.SILVER_INGOT,
                        0.7F,
                        200)
                .unlockedBy("has_silver_ore", has(InfXBlocks.SILVER_ORE))
                .save(output, recipeKey("silver_ingot_from_smelting_silver_ore"));
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(InfXBlocks.MITHRIL_ORE),
                        RecipeCategory.MISC,
                        CookingBookCategory.MISC,
                        InfXItems.MITHRIL_INGOT,
                        0.7F,
                        200)
                .unlockedBy("has_mithril_ore", has(InfXBlocks.MITHRIL_ORE))
                .save(output, recipeKey("mithril_ingot_from_smelting_mithril_ore"));
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(InfXBlocks.ADAMANTIUM_ORE),
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
                equipment(MiteMaterial.OBSIDIAN, EquipmentType.HATCHET),
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
                equipment(MiteMaterial.OBSIDIAN, EquipmentType.SHOVEL),
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
                equipment(MiteMaterial.OBSIDIAN, EquipmentType.AXE),
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
                equipment(MiteMaterial.WOOD, EquipmentType.CUDGEL),
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
                equipment(MiteMaterial.WOOD, EquipmentType.CLUB),
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
                equipment(MiteMaterial.FLINT, EquipmentType.KNIFE),
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
                equipment(MiteMaterial.OBSIDIAN, EquipmentType.KNIFE),
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
                equipment(MiteMaterial.WOOD, EquipmentType.BOW),
                1,
                Map.of(
                        'S', Ingredient.of(Items.STICK),
                        'B', ingredient(InfXItemTags.BINDINGS)),
                List.of(" SB", "S B", " SB"));
        addMetalBow(
                "ancient_metal",
                MiteMaterial.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                1750.0F,
                InfXItems.ANCIENT_METAL_INGOT);
        addMetalBow(
                "mithril",
                MiteMaterial.MITHRIL,
                BenchTier.MITHRIL,
                6550.0F,
                InfXItems.MITHRIL_INGOT);
        addShaped(
                "flint_workbench",
                BenchTier.HAND,
                270.0F,
                CraftingBookCategory.BUILDING,
                "",
                InfXBlocks.FLINT_WORKBENCH,
                1,
                Map.of(
                        'F', Ingredient.of(Items.FLINT),
                        'B', ingredient(InfXItemTags.BINDINGS),
                        'S', Ingredient.of(Items.STICK),
                        'L', ingredient(ItemTags.LOGS)),
                List.of("FB", "SL"));
        addShaped(
                "obsidian_workbench",
                BenchTier.HAND,
                410.0F,
                CraftingBookCategory.BUILDING,
                "",
                InfXBlocks.OBSIDIAN_WORKBENCH,
                1,
                Map.of(
                        'O', Ingredient.of(Items.OBSIDIAN),
                        'B', ingredient(InfXItemTags.BINDINGS),
                        'S', Ingredient.of(Items.STICK),
                        'L', ingredient(ItemTags.LOGS)),
                List.of("OB", "SL"));
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
        addMetalConversions("copper", 400.0F, Items.COPPER_NUGGET, Items.COPPER_INGOT);
        addMetalConversions("silver", 400.0F, InfXItems.SILVER_NUGGET, InfXItems.SILVER_INGOT);
        addMetalConversions("gold", 400.0F, Items.GOLD_NUGGET, Items.GOLD_INGOT);
        addMetalConversions("iron", 800.0F, Items.IRON_NUGGET, Items.IRON_INGOT);
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

        addPlanks("acacia", Items.ACACIA_PLANKS, ItemTags.ACACIA_LOGS, 4);
        addPlanks("bamboo", Items.BAMBOO_PLANKS, ItemTags.BAMBOO_BLOCKS, 2);
        addPlanks("birch", Items.BIRCH_PLANKS, ItemTags.BIRCH_LOGS, 4);
        addPlanks("cherry", Items.CHERRY_PLANKS, ItemTags.CHERRY_LOGS, 4);
        addPlanks("crimson", Items.CRIMSON_PLANKS, ItemTags.CRIMSON_STEMS, 4);
        addPlanks("dark_oak", Items.DARK_OAK_PLANKS, ItemTags.DARK_OAK_LOGS, 4);
        addPlanks("jungle", Items.JUNGLE_PLANKS, ItemTags.JUNGLE_LOGS, 4);
        addPlanks("mangrove", Items.MANGROVE_PLANKS, ItemTags.MANGROVE_LOGS, 4);
        addPlanks("oak", Items.OAK_PLANKS, ItemTags.OAK_LOGS, 4);
        addPlanks("pale_oak", Items.PALE_OAK_PLANKS, ItemTags.PALE_OAK_LOGS, 4);
        addPlanks("spruce", Items.SPRUCE_PLANKS, ItemTags.SPRUCE_LOGS, 4);
        addPlanks("warped", Items.WARPED_PLANKS, ItemTags.WARPED_STEMS, 4);

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
        addShaped(
                "cobblestone_furnace",
                BenchTier.COPPER,
                800.0F,
                CraftingBookCategory.BUILDING,
                "",
                Blocks.FURNACE,
                1,
                Map.of('C', Ingredient.of(Blocks.COBBLESTONE)),
                List.of("CCC", "C C", "CCC"));
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
        addCatalogCoreMetalTools("silver", MiteMaterial.SILVER, BenchTier.COPPER, 400.0F, InfXItems.SILVER_INGOT);
        addCatalogCoreMetalTools("gold", MiteMaterial.GOLD, BenchTier.COPPER, 400.0F, Items.GOLD_INGOT);
        addCatalogCoreMetalTools(
                "ancient_metal",
                MiteMaterial.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                1600.0F,
                InfXItems.ANCIENT_METAL_INGOT);
        addCatalogCoreMetalTools(
                "mithril", MiteMaterial.MITHRIL, BenchTier.MITHRIL, 6400.0F, InfXItems.MITHRIL_INGOT);
        addCatalogCoreMetalTools(
                "adamantium",
                MiteMaterial.ADAMANTIUM,
                BenchTier.ADAMANTIUM,
                25600.0F,
                InfXItems.ADAMANTIUM_INGOT);
        addCatalogSpecialMetalTools(
                "copper", MiteMaterial.COPPER, BenchTier.COPPER, 400.0F, Items.COPPER_INGOT);
        addCatalogSpecialMetalTools(
                "silver", MiteMaterial.SILVER, BenchTier.COPPER, 400.0F, InfXItems.SILVER_INGOT);
        addCatalogSpecialMetalTools(
                "gold", MiteMaterial.GOLD, BenchTier.COPPER, 400.0F, Items.GOLD_INGOT);
        addCatalogSpecialMetalTools(
                "iron", MiteMaterial.IRON, BenchTier.IRON, 800.0F, Items.IRON_INGOT);
        addCatalogSpecialMetalTools(
                "ancient_metal",
                MiteMaterial.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                1600.0F,
                InfXItems.ANCIENT_METAL_INGOT);
        addCatalogSpecialMetalTools(
                "mithril", MiteMaterial.MITHRIL, BenchTier.MITHRIL, 6400.0F, InfXItems.MITHRIL_INGOT);
        addCatalogSpecialMetalTools(
                "adamantium",
                MiteMaterial.ADAMANTIUM,
                BenchTier.ADAMANTIUM,
                25600.0F,
                InfXItems.ADAMANTIUM_INGOT);
        addMetalDagger("copper", MiteMaterial.COPPER, BenchTier.COPPER, 425.0F, Items.COPPER_INGOT);
        addMetalDagger("silver", MiteMaterial.SILVER, BenchTier.COPPER, 425.0F, InfXItems.SILVER_INGOT);
        addMetalDagger("gold", MiteMaterial.GOLD, BenchTier.COPPER, 425.0F, Items.GOLD_INGOT);
        addMetalDagger("iron", MiteMaterial.IRON, BenchTier.IRON, 825.0F, Items.IRON_INGOT);
        addMetalDagger(
                "ancient_metal",
                MiteMaterial.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                1625.0F,
                InfXItems.ANCIENT_METAL_INGOT);
        addMetalDagger(
                "mithril", MiteMaterial.MITHRIL, BenchTier.MITHRIL, 6425.0F, InfXItems.MITHRIL_INGOT);
        addMetalDagger(
                "adamantium",
                MiteMaterial.ADAMANTIUM,
                BenchTier.ADAMANTIUM,
                25625.0F,
                InfXItems.ADAMANTIUM_INGOT);

        addArrow("flint", MiteMaterial.FLINT, BenchTier.FLINT, 75.0F, InfXItems.FLINT_CHIP);
        addArrow(
                "obsidian",
                MiteMaterial.OBSIDIAN,
                BenchTier.FLINT,
                200.0F / 9.0F + 50.0F,
                InfXItems.OBSIDIAN_SHARD);
        addArrow("copper", MiteMaterial.COPPER, BenchTier.COPPER, 400.0F / 9.0F + 50.0F, Items.COPPER_NUGGET);
        addArrow(
                "silver",
                MiteMaterial.SILVER,
                BenchTier.COPPER,
                400.0F / 9.0F + 50.0F,
                InfXItems.SILVER_NUGGET);
        addArrow("gold", MiteMaterial.GOLD, BenchTier.COPPER, 400.0F / 9.0F + 50.0F, Items.GOLD_NUGGET);
        addArrow("iron", MiteMaterial.IRON, BenchTier.IRON, 800.0F / 9.0F + 50.0F, Items.IRON_NUGGET);
        addArrow(
                "ancient_metal",
                MiteMaterial.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                1600.0F / 9.0F + 50.0F,
                InfXItems.catalog().raw("ancient_metal_nugget").holder());
        addArrow(
                "mithril",
                MiteMaterial.MITHRIL,
                BenchTier.MITHRIL,
                6400.0F / 9.0F + 50.0F,
                InfXItems.MITHRIL_NUGGET);
        addArrow(
                "adamantium",
                MiteMaterial.ADAMANTIUM,
                BenchTier.ADAMANTIUM,
                25600.0F / 9.0F + 50.0F,
                InfXItems.ADAMANTIUM_NUGGET);

        addArmorSet("leather", MiteMaterial.LEATHER, BenchTier.FLINT, 100.0F, Items.LEATHER, false);
        addMetalArmorSets(
                "copper", MiteMaterial.COPPER, BenchTier.COPPER, 400.0F, Items.COPPER_INGOT);
        addMetalArmorSets(
                "silver", MiteMaterial.SILVER, BenchTier.COPPER, 400.0F, InfXItems.SILVER_INGOT);
        addMetalArmorSets(
                "gold", MiteMaterial.GOLD, BenchTier.COPPER, 400.0F, Items.GOLD_INGOT);
        addArmorSet(
                "rusted_iron",
                MiteMaterial.RUSTED_IRON,
                BenchTier.COPPER,
                400.0F * 4.0F / 9.0F,
                raw("rusted_iron_chain"),
                true);
        addMetalArmorSets(
                "iron", MiteMaterial.IRON, BenchTier.IRON, 800.0F, Items.IRON_INGOT);
        addMetalArmorSets(
                "ancient_metal",
                MiteMaterial.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                1600.0F,
                InfXItems.ANCIENT_METAL_INGOT);
        addMetalArmorSets(
                "mithril", MiteMaterial.MITHRIL, BenchTier.MITHRIL, 6400.0F, InfXItems.MITHRIL_INGOT);
        addMetalArmorSets(
                "adamantium",
                MiteMaterial.ADAMANTIUM,
                BenchTier.ADAMANTIUM,
                25600.0F,
                InfXItems.ADAMANTIUM_INGOT);
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

    private void addR196Buckets() {
        for (MiteMaterial material : InfXItems.BUCKET_MATERIALS) {
            addShaped(
                    material.path() + "_bucket",
                    bucketBench(material),
                    bucketIngotDifficulty(material) * 3.0F,
                    CraftingBookCategory.MISC,
                    "",
                    InfXItems.bucket(material, MiteBucketItem.Contents.EMPTY),
                    1,
                    Map.of('I', Ingredient.of(bucketIngot(material))),
                    List.of("I I", " I "));
            addShapeless(
                    material.path() + "_bucket_from_stone_bucket",
                    BenchTier.HAND,
                    100.0F,
                    CraftingBookCategory.MISC,
                    "",
                    InfXItems.bucket(material, MiteBucketItem.Contents.EMPTY),
                    1,
                    List.of(Ingredient.of(InfXItems.bucket(material, MiteBucketItem.Contents.STONE))));
        }
    }

    private static ItemLike bucketIngot(MiteMaterial material) {
        return switch (material) {
            case COPPER -> Items.COPPER_INGOT;
            case SILVER -> InfXItems.SILVER_INGOT;
            case GOLD -> Items.GOLD_INGOT;
            case IRON -> Items.IRON_INGOT;
            case ANCIENT_METAL -> InfXItems.ANCIENT_METAL_INGOT;
            case MITHRIL -> InfXItems.MITHRIL_INGOT;
            case ADAMANTIUM -> InfXItems.ADAMANTIUM_INGOT;
            default -> throw new IllegalArgumentException("No R196 bucket ingot for " + material);
        };
    }

    private static BenchTier bucketBench(MiteMaterial material) {
        return switch (material) {
            case COPPER, SILVER, GOLD -> BenchTier.COPPER;
            case IRON -> BenchTier.IRON;
            case ANCIENT_METAL -> BenchTier.ANCIENT_METAL;
            case MITHRIL -> BenchTier.MITHRIL;
            case ADAMANTIUM -> BenchTier.ADAMANTIUM;
            default -> throw new IllegalArgumentException("No R196 bucket bench for " + material);
        };
    }

    private static float bucketIngotDifficulty(MiteMaterial material) {
        return switch (material) {
            case COPPER, SILVER, GOLD -> 400.0F;
            case IRON -> 800.0F;
            case ANCIENT_METAL -> 1_600.0F;
            case MITHRIL -> 6_400.0F;
            case ADAMANTIUM -> 25_600.0F;
            default -> throw new IllegalArgumentException("No R196 bucket difficulty for " + material);
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
            MiteMaterial equipmentMaterial,
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
            MiteMaterial equipmentMaterial,
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

    private void addMiteBlockRecipes() {
        addShaped(
                "stone_from_cobblestone",
                BenchTier.HAND,
                200.0F,
                CraftingBookCategory.BUILDING,
                "",
                Blocks.STONE,
                2,
                Map.of('C', Ingredient.of(Blocks.COBBLESTONE)),
                List.of("CC", "CC"));
        addShaped(
                "stone_bricks",
                BenchTier.HAND,
                200.0F,
                CraftingBookCategory.BUILDING,
                "",
                Blocks.STONE_BRICKS,
                2,
                Map.of('S', Ingredient.of(Blocks.STONE)),
                List.of("SS", "SS"));
        addShaped(
                "compass",
                BenchTier.FLINT,
                400.0F,
                CraftingBookCategory.MISC,
                "",
                Items.COMPASS,
                1,
                Map.of('N', Ingredient.of(Items.IRON_NUGGET), 'R', Ingredient.of(Items.REDSTONE)),
                List.of("NNN", "NRN", "NNN"));
        addShaped(
                "clock",
                BenchTier.FLINT,
                400.0F,
                CraftingBookCategory.MISC,
                "",
                Items.CLOCK,
                1,
                Map.of('N', Ingredient.of(Items.GOLD_NUGGET), 'R', Ingredient.of(Items.REDSTONE)),
                List.of("NNN", "NRN", "NNN"));
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
        addShaped(
                "glass_pane",
                BenchTier.HAND,
                200.0F,
                CraftingBookCategory.BUILDING,
                "",
                Blocks.GLASS_PANE,
                6,
                Map.of('G', Ingredient.of(Blocks.GLASS)),
                List.of("G"));
        addShaped(
                "bricks",
                BenchTier.FLINT,
                800.0F,
                CraftingBookCategory.BUILDING,
                "",
                Blocks.BRICKS,
                2,
                Map.of('B', Ingredient.of(Items.BRICK), 'S', Ingredient.of(Blocks.SAND)),
                List.of("BBB", "BSB", "BBB"));
        addShaped(
                "snow",
                BenchTier.HAND,
                25.0F,
                CraftingBookCategory.BUILDING,
                "",
                Blocks.SNOW,
                1,
                Map.of('S', Ingredient.of(Items.SNOWBALL)),
                List.of("S"));
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
        addShaped(
                "snow_block",
                BenchTier.HAND,
                200.0F,
                CraftingBookCategory.BUILDING,
                "",
                Blocks.SNOW_BLOCK,
                1,
                Map.of('S', Ingredient.of(InfXBlocks.SNOW_SLAB)),
                List.of("S", "S"));
        addShaped(
                "oak_sign",
                BenchTier.FLINT,
                50.0F,
                CraftingBookCategory.BUILDING,
                "",
                Items.OAK_SIGN,
                1,
                Map.of('W', ingredient(ItemTags.WOODEN_SLABS), 'S', Ingredient.of(Items.STICK)),
                List.of("W", "S"));
        addShaped(
                "oak_fence",
                BenchTier.FLINT,
                150.0F,
                CraftingBookCategory.BUILDING,
                "",
                Items.OAK_FENCE,
                2,
                Map.of('S', Ingredient.of(Items.STICK)),
                List.of("SSS", "SSS"));
        addShaped(
                "ladder",
                BenchTier.FLINT,
                175.0F,
                CraftingBookCategory.BUILDING,
                "",
                Items.LADDER,
                2,
                Map.of('S', Ingredient.of(Items.STICK)),
                List.of("S S", "S S", "S S"));
        addShaped(
                "nether_bricks",
                BenchTier.FLINT,
                800.0F,
                CraftingBookCategory.BUILDING,
                "",
                Blocks.NETHER_BRICKS,
                2,
                Map.of('B', Ingredient.of(Items.NETHER_BRICK), 'S', Ingredient.of(Blocks.SOUL_SAND)),
                List.of("BBB", "BSB", "BBB"));
        addShaped(
                "saddle",
                BenchTier.FLINT,
                600.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                Items.SADDLE,
                4,
                Map.of('L', Ingredient.of(Items.LEATHER), 'N', Ingredient.of(Items.IRON_NUGGET)),
                List.of("LLL", "L L", "N N"));
    }

    private void addPlanks(String wood, ItemLike result, TagKey<Item> logs, int count) {
        addShapeless(
                wood + "_planks",
                BenchTier.FLINT,
                120.0F,
                CraftingBookCategory.BUILDING,
                "planks",
                result,
                count,
                List.of(ingredient(logs)));
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
            MiteMaterial equipmentMaterial,
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
            MiteMaterial equipmentMaterial,
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
            MiteMaterial equipmentMaterial,
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
            MiteMaterial equipmentMaterial,
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
            MiteMaterial equipmentMaterial,
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

    private static ItemLike equipment(MiteMaterial material, EquipmentType type) {
        return InfXItems.catalog().equipment(material, type).holder();
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
                MiteMaterial.COPPER,
                BenchTier.COPPER,
                Items.COPPER_INGOT,
                Blocks.COPPER_BLOCK,
                12_400.0F);
        addMetalAnvil(MiteMaterial.SILVER, BenchTier.SILVER, InfXItems.SILVER_INGOT, InfXBlocks.SILVER_BLOCK, 12_400.0F);
        addMetalAnvil(MiteMaterial.GOLD, BenchTier.GOLD, Items.GOLD_INGOT, Blocks.GOLD_BLOCK, 12_400.0F);
        addMetalAnvil(MiteMaterial.IRON, BenchTier.IRON, Items.IRON_INGOT, Blocks.IRON_BLOCK, 24_800.0F);
        addMetalAnvil(
                MiteMaterial.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                InfXItems.ANCIENT_METAL_INGOT,
                InfXBlocks.ANCIENT_METAL_BLOCK,
                49_600.0F);
        addMetalAnvil(
                MiteMaterial.MITHRIL,
                BenchTier.MITHRIL,
                InfXItems.MITHRIL_INGOT,
                InfXBlocks.MITHRIL_BLOCK,
                198_400.0F);
        addMetalAnvil(
                MiteMaterial.ADAMANTIUM,
                BenchTier.ADAMANTIUM,
                InfXItems.ADAMANTIUM_INGOT,
                InfXBlocks.ADAMANTIUM_BLOCK,
                793_600.0F);
    }

    private void addMetalAnvil(
            MiteMaterial material,
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
                List.of("BBB", "I I", "I I"));
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
        ShapedRecipe delegate = new ShapedRecipe(
                new Recipe.CommonInfo(true),
                new CraftingRecipe.CraftingBookInfo(category, group),
                ShapedRecipePattern.of(key, pattern),
                new ItemStackTemplate(result.asItem(), count));
        output.accept(recipeKey(name), new TimedShapedRecipe(requiredBench, difficulty, delegate), null);
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
        ShapelessRecipe delegate = new ShapelessRecipe(
                new Recipe.CommonInfo(true),
                new CraftingRecipe.CraftingBookInfo(category, group),
                new ItemStackTemplate(result.asItem(), count),
                ingredients);
        output.accept(recipeKey(name), new TimedShapelessRecipe(requiredBench, difficulty, delegate), null);
    }

    private Ingredient ingredient(TagKey<Item> tag) {
        return Ingredient.of(items.getOrThrow(tag));
    }

    private static ResourceKey<Recipe<?>> recipeKey(String path) {
        return ResourceKey.create(Registries.RECIPE, InfiniteX.id(path));
    }

    static final class Runner extends RecipeProvider.Runner {
        Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "InfiniteX recipes";
        }
    }
}
