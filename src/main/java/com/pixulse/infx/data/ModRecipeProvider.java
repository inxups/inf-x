package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.crafting.BenchTier;
import com.pixulse.infx.crafting.TimedShapedRecipe;
import com.pixulse.infx.crafting.TimedShapelessRecipe;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.item.R196BucketItem;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.tag.ModTags;
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
import net.minecraft.world.item.alchemy.PotionIds;
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
        addShapeless(
                "flour",
                BenchTier.HAND,
                100.0F,
                CraftingBookCategory.MISC,
                "",
                ModItems.FLOUR,
                1,
                List.of(Ingredient.of(Items.WHEAT)));
        addShapeless(
                "water_bowl",
                BenchTier.FLINT,
                25.0F,
                CraftingBookCategory.MISC,
                "",
                ModItems.WATER_BOWL,
                4,
                List.of(Ingredient.of(Items.WATER_BUCKET), Ingredient.of(Items.BOWL), Ingredient.of(Items.BOWL),
                        Ingredient.of(Items.BOWL), Ingredient.of(Items.BOWL)));
        addShapeless(
                "dough",
                BenchTier.HAND,
                150.0F,
                CraftingBookCategory.MISC,
                "",
                ModItems.DOUGH,
                1,
                List.of(Ingredient.of(ModItems.FLOUR), Ingredient.of(ModItems.WATER_BOWL)));
        addShapeless(
                "salad",
                BenchTier.HAND,
                175.0F,
                CraftingBookCategory.MISC,
                "",
                ModItems.SALAD,
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
                ModItems.BLUEBERRY_PORRIDGE,
                1,
                List.of(Ingredient.of(Items.WHEAT_SEEDS), Ingredient.of(ModItems.BLUEBERRIES),
                        Ingredient.of(Items.SUGAR), Ingredient.of(ModItems.WATER_BOWL)));
        addShapeless(
                "milk_bowl",
                BenchTier.FLINT,
                75.0F,
                CraftingBookCategory.MISC,
                "",
                ModItems.MILK_BOWL,
                4,
                List.of(Ingredient.of(Items.MILK_BUCKET), Ingredient.of(Items.BOWL), Ingredient.of(Items.BOWL),
                        Ingredient.of(Items.BOWL), Ingredient.of(Items.BOWL)));
        addShapeless(
                "cereal_porridge",
                BenchTier.HAND,
                200.0F,
                CraftingBookCategory.MISC,
                "",
                ModItems.CEREAL_PORRIDGE,
                1,
                List.of(Ingredient.of(ModItems.MILK_BOWL), Ingredient.of(Items.WHEAT), Ingredient.of(Items.SUGAR)));
        addShapeless(
                "chocolate",
                BenchTier.HAND,
                100.0F,
                CraftingBookCategory.MISC,
                "",
                ModItems.CHOCOLATE,
                1,
                List.of(Ingredient.of(Items.COCOA_BEANS), Ingredient.of(Items.SUGAR)));
        addShapeless(
                "pumpkin_soup",
                BenchTier.HAND,
                175.0F,
                CraftingBookCategory.MISC,
                "",
                ModItems.PUMPKIN_SOUP,
                1,
                List.of(Ingredient.of(Items.PUMPKIN), Ingredient.of(ModItems.WATER_BOWL)));
        addShapeless(
                "cream_of_mushroom_soup",
                BenchTier.HAND,
                225.0F,
                CraftingBookCategory.MISC,
                "",
                ModItems.CREAM_OF_MUSHROOM_SOUP,
                1,
                List.of(Ingredient.of(ModItems.MILK_BOWL), Ingredient.of(Items.BROWN_MUSHROOM),
                        Ingredient.of(Items.BROWN_MUSHROOM)));
        addShapeless(
                "vegetable_soup",
                BenchTier.HAND,
                225.0F,
                CraftingBookCategory.MISC,
                "",
                ModItems.VEGETABLE_SOUP,
                1,
                List.of(Ingredient.of(Items.CARROT), Ingredient.of(Items.POTATO),
                        Ingredient.of(ModItems.ONION), Ingredient.of(ModItems.WATER_BOWL)));
        addShapeless(
                "cream_of_vegetable_soup",
                BenchTier.HAND,
                250.0F,
                CraftingBookCategory.MISC,
                "",
                ModItems.CREAM_OF_VEGETABLE_SOUP,
                1,
                List.of(Ingredient.of(ModItems.MILK_BOWL), Ingredient.of(Items.CARROT), Ingredient.of(Items.POTATO),
                        Ingredient.of(ModItems.ONION)));
        addShapeless(
                "chicken_soup",
                BenchTier.HAND,
                275.0F,
                CraftingBookCategory.MISC,
                "",
                ModItems.CHICKEN_SOUP,
                1,
                List.of(Ingredient.of(Items.COOKED_CHICKEN), Ingredient.of(Items.CARROT),
                        Ingredient.of(ModItems.ONION), Ingredient.of(ModItems.WATER_BOWL)));
        addShapeless(
                "beef_stew",
                BenchTier.HAND,
                300.0F,
                CraftingBookCategory.MISC,
                "",
                ModItems.BEEF_STEW,
                1,
                List.of(Ingredient.of(Items.COOKED_BEEF), Ingredient.of(Items.BROWN_MUSHROOM),
                        Ingredient.of(Items.POTATO), Ingredient.of(ModItems.WATER_BOWL)));
        addShapeless(
                "mashed_potato",
                BenchTier.HAND,
                175.0F,
                CraftingBookCategory.MISC,
                "",
                ModItems.MASHED_POTATO,
                1,
                List.of(Ingredient.of(ModItems.MILK_BOWL), Ingredient.of(Items.BAKED_POTATO),
                        Ingredient.of(ModItems.CHEESE)));
        addShapeless(
                "cheese",
                BenchTier.HAND,
                6_400.0F,
                CraftingBookCategory.MISC,
                "",
                ModItems.CHEESE,
                1,
                List.of(Ingredient.of(ModItems.MILK_BOWL), Ingredient.of(ModItems.MILK_BOWL),
                        Ingredient.of(ModItems.MILK_BOWL), Ingredient.of(ModItems.MILK_BOWL)));
        addShapeless(
                "fruit_ice",
                BenchTier.HAND,
                150.0F,
                CraftingBookCategory.MISC,
                "",
                ModItems.FRUIT_ICE,
                1,
                List.of(Ingredient.of(ModItems.ORANGE), Ingredient.of(Items.SUGAR),
                        Ingredient.of(Items.SNOWBALL), Ingredient.of(Items.BOWL)));
        addShapeless(
                "ice_cream",
                BenchTier.HAND,
                200.0F,
                CraftingBookCategory.MISC,
                "",
                ModItems.ICE_CREAM,
                1,
                List.of(Ingredient.of(ModItems.CHOCOLATE), Ingredient.of(ModItems.MILK_BOWL),
                        Ingredient.of(Items.SNOWBALL)));
        addShaped(
                "pumpkin_pie",
                BenchTier.FLINT,
                350.0F,
                CraftingBookCategory.MISC,
                "",
                Items.PUMPKIN_PIE,
                1,
                Map.of('P', Ingredient.of(Items.PUMPKIN), 'F', Ingredient.of(ModItems.FLOUR),
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
                Map.of('M', Ingredient.of(Items.MILK_BUCKET), 'F', Ingredient.of(ModItems.FLOUR),
                        'S', Ingredient.of(Items.SUGAR), 'E', Ingredient.of(Items.EGG)),
                List.of("MMM", "SES", "FFF"));
        addShapeless(
                "cake_from_milk_bowl",
                BenchTier.HAND,
                600.0F,
                CraftingBookCategory.MISC,
                "",
                Items.CAKE,
                1,
                List.of(Ingredient.of(ModItems.FLOUR), Ingredient.of(Items.SUGAR),
                        Ingredient.of(Items.EGG), Ingredient.of(ModItems.MILK_BOWL)));
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
        addShapeless(
                "mushroom_stew_with_water",
                BenchTier.HAND,
                150.0F,
                CraftingBookCategory.MISC,
                "",
                Items.MUSHROOM_STEW,
                1,
                List.of(Ingredient.of(Items.BROWN_MUSHROOM), Ingredient.of(Items.RED_MUSHROOM),
                        Ingredient.of(ModItems.WATER_BOWL)));
        addShapeless(
                "bottle_of_disenchanting",
                BenchTier.HAND,
                100.0F,
                CraftingBookCategory.MISC,
                "",
                ModItems.BOTTLE_OF_DISENCHANTING,
                1,
                List.of(
                        DataComponentIngredient.of(
                                DataComponents.POTION_CONTENTS,
                                new PotionContents(registries.lookupOrThrow(Registries.POTION).getOrThrow(PotionIds.WATER)),
                                Items.POTION),
                        Ingredient.of(Items.NETHER_WART),
                        Ingredient.of(Items.CHARCOAL)));
        addR196Buckets();
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModItems.DOUGH),
                        RecipeCategory.FOOD,
                        CookingBookCategory.FOOD,
                        Items.BREAD,
                        1.0F,
                        200)
                .unlockedBy("has_dough", has(ModItems.DOUGH))
                .save(output, recipeKey("bread_from_dough"));
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModItems.WORM),
                        RecipeCategory.FOOD,
                        CookingBookCategory.FOOD,
                        ModItems.COOKED_WORM,
                        0.2F,
                        160)
                .unlockedBy("has_worm", has(ModItems.WORM))
                .save(output, recipeKey("cooked_worm"));
        addShaped(
                "emerald_enchanting_table",
                BenchTier.IRON,
                3_200.0F,
                CraftingBookCategory.BUILDING,
                "",
                ModBlocks.EMERALD_ENCHANTING_TABLE,
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
                ModBlocks.DIAMOND_ENCHANTING_TABLE,
                1,
                Map.of('B', Ingredient.of(Items.BOOK), 'G', Ingredient.of(Items.DIAMOND),
                        'O', Ingredient.of(Blocks.OBSIDIAN)),
                List.of(" B ", "GOG", "OOO"));
        addSafe("copper_safe", BenchTier.COPPER, ModBlocks.COPPER_SAFE, Items.COPPER_INGOT, 3_600.0F);
        addSafe("silver_safe", BenchTier.COPPER, ModBlocks.SILVER_SAFE, ModItems.SILVER_INGOT, 3_600.0F);
        addSafe("gold_safe", BenchTier.GOLD, ModBlocks.GOLD_SAFE, Items.GOLD_INGOT, 3_600.0F);
        addSafe("iron_safe", BenchTier.IRON, ModBlocks.IRON_SAFE, Items.IRON_INGOT, 7_200.0F);
        addSafe("ancient_metal_safe", BenchTier.ANCIENT_METAL, ModBlocks.ANCIENT_METAL_SAFE, ModItems.ANCIENT_METAL_INGOT, 14_400.0F);
        addSafe("mithril_safe", BenchTier.MITHRIL, ModBlocks.MITHRIL_SAFE, ModItems.MITHRIL_INGOT, 28_800.0F);
        addSafe("adamantium_safe", BenchTier.ADAMANTIUM, ModBlocks.ADAMANTIUM_SAFE, ModItems.ADAMANTIUM_INGOT, 57_600.0F);
        addShapeless(
                "flint_to_flint_chips",
                BenchTier.HAND,
                100.0F,
                CraftingBookCategory.MISC,
                "",
                ModItems.FLINT_CHIP,
                4,
                List.of(Ingredient.of(Items.FLINT)));
        addShapeless(
                "leather_to_sinew",
                BenchTier.HAND,
                50.0F,
                CraftingBookCategory.MISC,
                "",
                ModItems.SINEW,
                4,
                List.of(Ingredient.of(Items.LEATHER)));
        addShardRecipes("diamond", raw("diamond_shard"), Items.DIAMOND, 1600.0F);
        addShardRecipes("nether_quartz", raw("nether_quartz_shard"), Items.QUARTZ, 900.0F);
        addShardRecipes("glass", raw("glass_shard"), Blocks.GLASS_PANE, 200.0F);

        addShaped(
                "clay_furnace",
                BenchTier.HAND,
                320.0F,
                CraftingBookCategory.BUILDING,
                "",
                ModBlocks.CLAY_FURNACE,
                1,
                Map.of('C', Ingredient.of(Blocks.CLAY)),
                List.of("CC", "CC"));
        addShaped(
                "large_clay_oven",
                BenchTier.FLINT,
                640.0F,
                CraftingBookCategory.BUILDING,
                "",
                ModBlocks.LARGE_CLAY_OVEN,
                1,
                Map.of('C', Ingredient.of(Blocks.CLAY)),
                List.of("CCC", "C C", "CCC"));
        addShaped(
                "sandstone_furnace",
                BenchTier.FLINT,
                640.0F,
                CraftingBookCategory.BUILDING,
                "",
                ModBlocks.SANDSTONE_FURNACE,
                1,
                Map.of('S', Ingredient.of(Blocks.SANDSTONE)),
                List.of("SSS", "S S", "SSS"));
        addShaped(
                "hardened_clay_furnace",
                BenchTier.FLINT,
                1440.0F,
                CraftingBookCategory.BUILDING,
                "",
                ModBlocks.HARDENED_CLAY_FURNACE,
                1,
                Map.of('T', Ingredient.of(Blocks.TERRACOTTA)),
                List.of("TTT", "T T", "TTT"));
        addShaped(
                "obsidian_furnace",
                BenchTier.FLINT,
                1920.0F,
                CraftingBookCategory.BUILDING,
                "",
                ModBlocks.OBSIDIAN_FURNACE,
                1,
                Map.of('O', Ingredient.of(Blocks.OBSIDIAN)),
                List.of("OOO", "O O", "OOO"));
        addShaped(
                "netherrack_furnace",
                BenchTier.FLINT,
                1280.0F,
                CraftingBookCategory.BUILDING,
                "",
                ModBlocks.NETHERRACK_FURNACE,
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
                        Ingredient.of(ModBlocks.SILVER_ORE),
                        RecipeCategory.MISC,
                        CookingBookCategory.MISC,
                        ModItems.SILVER_INGOT,
                        0.7F,
                        200)
                .unlockedBy("has_silver_ore", has(ModBlocks.SILVER_ORE))
                .save(output, recipeKey("silver_ingot_from_smelting_silver_ore"));
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModBlocks.MITHRIL_ORE),
                        RecipeCategory.MISC,
                        CookingBookCategory.MISC,
                        ModItems.MITHRIL_INGOT,
                        0.7F,
                        200)
                .unlockedBy("has_mithril_ore", has(ModBlocks.MITHRIL_ORE))
                .save(output, recipeKey("mithril_ingot_from_smelting_mithril_ore"));
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModBlocks.ADAMANTIUM_ORE),
                        RecipeCategory.MISC,
                        CookingBookCategory.MISC,
                        ModItems.ADAMANTIUM_INGOT,
                        0.7F,
                        200)
                .unlockedBy("has_adamantium_ore", has(ModBlocks.ADAMANTIUM_ORE))
                .save(output, recipeKey("adamantium_ingot_from_smelting_adamantium_ore"));

        addShaped(
                "flint_hatchet",
                BenchTier.HAND,
                150.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                ModItems.FLINT_HATCHET,
                1,
                Map.of(
                        'F', Ingredient.of(Items.FLINT),
                        'S', Ingredient.of(Items.STICK),
                        'B', ingredient(ModTags.Items.BINDINGS)),
                List.of("FS", "BS"));
        addShaped(
                "flint_shovel",
                BenchTier.FLINT,
                150.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                ModItems.FLINT_SHOVEL,
                1,
                Map.of(
                        'F', Ingredient.of(Items.FLINT),
                        'S', Ingredient.of(Items.STICK),
                        'B', ingredient(ModTags.Items.BINDINGS)),
                List.of("F ", "S ", "SB"));
        addShaped(
                "flint_axe",
                BenchTier.FLINT,
                375.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                ModItems.FLINT_AXE,
                1,
                Map.of(
                        'F', Ingredient.of(Items.FLINT),
                        'S', Ingredient.of(Items.STICK),
                        'B', ingredient(ModTags.Items.BINDINGS)),
                List.of("FF", "FS", "BS"));
        addShaped(
                "obsidian_hatchet",
                BenchTier.HAND,
                315.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(R196Material.OBSIDIAN, R196EquipmentType.HATCHET),
                1,
                Map.of(
                        'O', Ingredient.of(Items.OBSIDIAN),
                        'S', Ingredient.of(Items.STICK),
                        'B', ingredient(ModTags.Items.BINDINGS)),
                List.of("OS", "BS"));
        addShaped(
                "obsidian_shovel",
                BenchTier.FLINT,
                315.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(R196Material.OBSIDIAN, R196EquipmentType.SHOVEL),
                1,
                Map.of(
                        'O', Ingredient.of(Items.OBSIDIAN),
                        'S', Ingredient.of(Items.STICK),
                        'B', ingredient(ModTags.Items.BINDINGS)),
                List.of("O ", "S ", "SB"));
        addShaped(
                "obsidian_axe",
                BenchTier.FLINT,
                795.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(R196Material.OBSIDIAN, R196EquipmentType.AXE),
                1,
                Map.of(
                        'O', Ingredient.of(Items.OBSIDIAN),
                        'S', Ingredient.of(Items.STICK),
                        'B', ingredient(ModTags.Items.BINDINGS)),
                List.of("OO", "OS", "BS"));
        addShaped(
                "wood_cudgel",
                BenchTier.HAND,
                105.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(R196Material.WOOD, R196EquipmentType.CUDGEL),
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
                equipment(R196Material.WOOD, R196EquipmentType.CLUB),
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
                equipment(R196Material.FLINT, R196EquipmentType.KNIFE),
                1,
                Map.of(
                        'F', Ingredient.of(Items.FLINT),
                        'S', Ingredient.of(Items.STICK),
                        'B', ingredient(ModTags.Items.BINDINGS)),
                List.of("FB", "S "));
        addShaped(
                "obsidian_knife",
                BenchTier.HAND,
                290.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(R196Material.OBSIDIAN, R196EquipmentType.KNIFE),
                1,
                Map.of(
                        'O', Ingredient.of(Items.OBSIDIAN),
                        'S', Ingredient.of(Items.STICK),
                        'B', ingredient(ModTags.Items.BINDINGS)),
                List.of("OB", "S "));
        addShaped(
                "wood_bow",
                BenchTier.FLINT,
                150.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(R196Material.WOOD, R196EquipmentType.BOW),
                1,
                Map.of(
                        'S', Ingredient.of(Items.STICK),
                        'B', ingredient(ModTags.Items.BINDINGS)),
                List.of(" SB", "S B", " SB"));
        addMetalBow(
                "ancient_metal",
                R196Material.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                1750.0F,
                ModItems.ANCIENT_METAL_INGOT);
        addMetalBow(
                "mithril",
                R196Material.MITHRIL,
                BenchTier.MITHRIL,
                6550.0F,
                ModItems.MITHRIL_INGOT);
        addShaped(
                "flint_workbench",
                BenchTier.HAND,
                270.0F,
                CraftingBookCategory.BUILDING,
                "",
                ModBlocks.FLINT_WORKBENCH,
                1,
                Map.of(
                        'F', Ingredient.of(Items.FLINT),
                        'B', ingredient(ModTags.Items.BINDINGS),
                        'S', Ingredient.of(Items.STICK),
                        'L', ingredient(ItemTags.LOGS)),
                List.of("FB", "SL"));
        addShaped(
                "obsidian_workbench",
                BenchTier.HAND,
                410.0F,
                CraftingBookCategory.BUILDING,
                "",
                ModBlocks.OBSIDIAN_WORKBENCH,
                1,
                Map.of(
                        'O', Ingredient.of(Items.OBSIDIAN),
                        'B', ingredient(ModTags.Items.BINDINGS),
                        'S', Ingredient.of(Items.STICK),
                        'L', ingredient(ItemTags.LOGS)),
                List.of("OB", "SL"));
        addMetalWorkbench("copper", BenchTier.FLINT, 605.0F, Items.COPPER_INGOT, ModBlocks.COPPER_WORKBENCH);
        addMetalWorkbench("silver", BenchTier.FLINT, 605.0F, ModItems.SILVER_INGOT, ModBlocks.SILVER_WORKBENCH);
        addMetalWorkbench("gold", BenchTier.FLINT, 605.0F, Items.GOLD_INGOT, ModBlocks.GOLD_WORKBENCH);
        addMetalWorkbench("iron", BenchTier.COPPER, 1005.0F, Items.IRON_INGOT, ModBlocks.IRON_WORKBENCH);
        addMetalWorkbench(
                "ancient_metal",
                BenchTier.IRON,
                1805.0F,
                ModItems.ANCIENT_METAL_INGOT,
                ModBlocks.ANCIENT_METAL_WORKBENCH);
        addMetalWorkbench(
                "mithril", BenchTier.ANCIENT_METAL, 6605.0F, ModItems.MITHRIL_INGOT, ModBlocks.MITHRIL_WORKBENCH);
        addMetalWorkbench(
                "adamantium",
                BenchTier.MITHRIL,
                25805.0F,
                ModItems.ADAMANTIUM_INGOT,
                ModBlocks.ADAMANTIUM_WORKBENCH);
        addMetalStorageRecipes("silver", ModItems.SILVER_INGOT, ModBlocks.SILVER_BLOCK, 3_600.0F);
        addMetalStorageRecipes(
                "ancient_metal", ModItems.ANCIENT_METAL_INGOT, ModBlocks.ANCIENT_METAL_BLOCK, 14_400.0F);
        addMetalStorageRecipes("mithril", ModItems.MITHRIL_INGOT, ModBlocks.MITHRIL_BLOCK, 57_600.0F);
        addMetalStorageRecipes(
                "adamantium", ModItems.ADAMANTIUM_INGOT, ModBlocks.ADAMANTIUM_BLOCK, 230_400.0F);
        addShaped(
                "mithril_rune_stone",
                BenchTier.MITHRIL,
                3_200.0F,
                CraftingBookCategory.BUILDING,
                "",
                ModBlocks.MITHRIL_RUNE_STONE,
                1,
                Map.of(
                        'N', Ingredient.of(ModItems.MITHRIL_NUGGET),
                        'O', Ingredient.of(Blocks.OBSIDIAN)),
                List.of(" N ", "NON", " N "));
        addShaped(
                "adamantium_rune_stone",
                BenchTier.ADAMANTIUM,
                12_800.0F,
                CraftingBookCategory.BUILDING,
                "",
                ModBlocks.ADAMANTIUM_RUNE_STONE,
                1,
                Map.of(
                        'N', Ingredient.of(ModItems.ADAMANTIUM_NUGGET),
                        'O', Ingredient.of(Blocks.OBSIDIAN)),
                List.of(" N ", "NON", " N "));
        addMetalAnvilRecipes();
        addMetalConversions("copper", 400.0F, Items.COPPER_NUGGET, Items.COPPER_INGOT);
        addMetalConversions("silver", 400.0F, ModItems.SILVER_NUGGET, ModItems.SILVER_INGOT);
        addMetalConversions("gold", 400.0F, Items.GOLD_NUGGET, Items.GOLD_INGOT);
        addMetalConversions("iron", 800.0F, Items.IRON_NUGGET, Items.IRON_INGOT);
        addMetalConversions(
                "ancient_metal",
                1600.0F,
                ModItems.catalog().raw("ancient_metal_nugget").holder(),
                ModItems.ANCIENT_METAL_INGOT);
        addMetalConversions("mithril", 6400.0F, ModItems.MITHRIL_NUGGET, ModItems.MITHRIL_INGOT);
        addMetalConversions(
                "adamantium",
                25600.0F,
                ModItems.ADAMANTIUM_NUGGET,
                ModItems.ADAMANTIUM_INGOT);
        addChainConversions(
                "copper", BenchTier.COPPER, 400.0F, Items.COPPER_NUGGET);
        addChainConversions(
                "silver", BenchTier.COPPER, 400.0F, ModItems.SILVER_NUGGET);
        addChainConversions(
                "gold", BenchTier.COPPER, 400.0F, Items.GOLD_NUGGET);
        addChainConversions(
                "iron", BenchTier.IRON, 800.0F, Items.IRON_NUGGET);
        addChainConversions(
                "ancient_metal",
                BenchTier.ANCIENT_METAL,
                1600.0F,
                ModItems.catalog().raw("ancient_metal_nugget").holder());
        addChainConversions(
                "mithril", BenchTier.MITHRIL, 6400.0F, ModItems.MITHRIL_NUGGET);
        addChainConversions(
                "adamantium",
                BenchTier.ADAMANTIUM,
                25600.0F,
                ModItems.ADAMANTIUM_NUGGET);

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
                ModItems.COPPER_PICKAXE,
                ModItems.COPPER_SHOVEL,
                ModItems.COPPER_AXE,
                ModItems.COPPER_HOE,
                ModItems.COPPER_SWORD);
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
                ModItems.IRON_PICKAXE,
                ModItems.IRON_SHOVEL,
                ModItems.IRON_AXE,
                ModItems.IRON_HOE,
                ModItems.IRON_SWORD);
        addCatalogCoreMetalTools("silver", R196Material.SILVER, BenchTier.COPPER, 400.0F, ModItems.SILVER_INGOT);
        addCatalogCoreMetalTools("gold", R196Material.GOLD, BenchTier.COPPER, 400.0F, Items.GOLD_INGOT);
        addCatalogCoreMetalTools(
                "ancient_metal",
                R196Material.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                1600.0F,
                ModItems.ANCIENT_METAL_INGOT);
        addCatalogCoreMetalTools(
                "mithril", R196Material.MITHRIL, BenchTier.MITHRIL, 6400.0F, ModItems.MITHRIL_INGOT);
        addCatalogCoreMetalTools(
                "adamantium",
                R196Material.ADAMANTIUM,
                BenchTier.ADAMANTIUM,
                25600.0F,
                ModItems.ADAMANTIUM_INGOT);
        addCatalogSpecialMetalTools(
                "copper", R196Material.COPPER, BenchTier.COPPER, 400.0F, Items.COPPER_INGOT);
        addCatalogSpecialMetalTools(
                "silver", R196Material.SILVER, BenchTier.COPPER, 400.0F, ModItems.SILVER_INGOT);
        addCatalogSpecialMetalTools(
                "gold", R196Material.GOLD, BenchTier.COPPER, 400.0F, Items.GOLD_INGOT);
        addCatalogSpecialMetalTools(
                "iron", R196Material.IRON, BenchTier.IRON, 800.0F, Items.IRON_INGOT);
        addCatalogSpecialMetalTools(
                "ancient_metal",
                R196Material.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                1600.0F,
                ModItems.ANCIENT_METAL_INGOT);
        addCatalogSpecialMetalTools(
                "mithril", R196Material.MITHRIL, BenchTier.MITHRIL, 6400.0F, ModItems.MITHRIL_INGOT);
        addCatalogSpecialMetalTools(
                "adamantium",
                R196Material.ADAMANTIUM,
                BenchTier.ADAMANTIUM,
                25600.0F,
                ModItems.ADAMANTIUM_INGOT);
        addMetalDagger("copper", R196Material.COPPER, BenchTier.COPPER, 425.0F, Items.COPPER_INGOT);
        addMetalDagger("silver", R196Material.SILVER, BenchTier.COPPER, 425.0F, ModItems.SILVER_INGOT);
        addMetalDagger("gold", R196Material.GOLD, BenchTier.COPPER, 425.0F, Items.GOLD_INGOT);
        addMetalDagger("iron", R196Material.IRON, BenchTier.IRON, 825.0F, Items.IRON_INGOT);
        addMetalDagger(
                "ancient_metal",
                R196Material.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                1625.0F,
                ModItems.ANCIENT_METAL_INGOT);
        addMetalDagger(
                "mithril", R196Material.MITHRIL, BenchTier.MITHRIL, 6425.0F, ModItems.MITHRIL_INGOT);
        addMetalDagger(
                "adamantium",
                R196Material.ADAMANTIUM,
                BenchTier.ADAMANTIUM,
                25625.0F,
                ModItems.ADAMANTIUM_INGOT);

        addArrow("flint", R196Material.FLINT, BenchTier.FLINT, 75.0F, ModItems.FLINT_CHIP);
        addArrow(
                "obsidian",
                R196Material.OBSIDIAN,
                BenchTier.FLINT,
                200.0F / 9.0F + 50.0F,
                ModItems.OBSIDIAN_SHARD);
        addArrow("copper", R196Material.COPPER, BenchTier.COPPER, 400.0F / 9.0F + 50.0F, Items.COPPER_NUGGET);
        addArrow(
                "silver",
                R196Material.SILVER,
                BenchTier.COPPER,
                400.0F / 9.0F + 50.0F,
                ModItems.SILVER_NUGGET);
        addArrow("gold", R196Material.GOLD, BenchTier.COPPER, 400.0F / 9.0F + 50.0F, Items.GOLD_NUGGET);
        addArrow("iron", R196Material.IRON, BenchTier.IRON, 800.0F / 9.0F + 50.0F, Items.IRON_NUGGET);
        addArrow(
                "ancient_metal",
                R196Material.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                1600.0F / 9.0F + 50.0F,
                ModItems.catalog().raw("ancient_metal_nugget").holder());
        addArrow(
                "mithril",
                R196Material.MITHRIL,
                BenchTier.MITHRIL,
                6400.0F / 9.0F + 50.0F,
                ModItems.MITHRIL_NUGGET);
        addArrow(
                "adamantium",
                R196Material.ADAMANTIUM,
                BenchTier.ADAMANTIUM,
                25600.0F / 9.0F + 50.0F,
                ModItems.ADAMANTIUM_NUGGET);

        addArmorSet("leather", R196Material.LEATHER, BenchTier.FLINT, 100.0F, Items.LEATHER, false);
        addMetalArmorSets(
                "copper", R196Material.COPPER, BenchTier.COPPER, 400.0F, Items.COPPER_INGOT);
        addMetalArmorSets(
                "silver", R196Material.SILVER, BenchTier.COPPER, 400.0F, ModItems.SILVER_INGOT);
        addMetalArmorSets(
                "gold", R196Material.GOLD, BenchTier.COPPER, 400.0F, Items.GOLD_INGOT);
        addArmorSet(
                "rusted_iron",
                R196Material.RUSTED_IRON,
                BenchTier.COPPER,
                400.0F * 4.0F / 9.0F,
                raw("rusted_iron_chain"),
                true);
        addMetalArmorSets(
                "iron", R196Material.IRON, BenchTier.IRON, 800.0F, Items.IRON_INGOT);
        addMetalArmorSets(
                "ancient_metal",
                R196Material.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                1600.0F,
                ModItems.ANCIENT_METAL_INGOT);
        addMetalArmorSets(
                "mithril", R196Material.MITHRIL, BenchTier.MITHRIL, 6400.0F, ModItems.MITHRIL_INGOT);
        addMetalArmorSets(
                "adamantium",
                R196Material.ADAMANTIUM,
                BenchTier.ADAMANTIUM,
                25600.0F,
                ModItems.ADAMANTIUM_INGOT);
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
        for (R196Material material : ModItems.BUCKET_MATERIALS) {
            addShaped(
                    material.path() + "_bucket",
                    bucketBench(material),
                    bucketIngotDifficulty(material) * 3.0F,
                    CraftingBookCategory.MISC,
                    "",
                    ModItems.bucket(material, R196BucketItem.Contents.EMPTY),
                    1,
                    Map.of('I', Ingredient.of(bucketIngot(material))),
                    List.of("I I", " I "));
            addShapeless(
                    material.path() + "_bucket_from_stone_bucket",
                    BenchTier.HAND,
                    100.0F,
                    CraftingBookCategory.MISC,
                    "",
                    ModItems.bucket(material, R196BucketItem.Contents.EMPTY),
                    1,
                    List.of(Ingredient.of(ModItems.bucket(material, R196BucketItem.Contents.STONE))));
        }
    }

    private static ItemLike bucketIngot(R196Material material) {
        return switch (material) {
            case COPPER -> Items.COPPER_INGOT;
            case SILVER -> ModItems.SILVER_INGOT;
            case GOLD -> Items.GOLD_INGOT;
            case IRON -> Items.IRON_INGOT;
            case ANCIENT_METAL -> ModItems.ANCIENT_METAL_INGOT;
            case MITHRIL -> ModItems.MITHRIL_INGOT;
            case ADAMANTIUM -> ModItems.ADAMANTIUM_INGOT;
            default -> throw new IllegalArgumentException("No R196 bucket ingot for " + material);
        };
    }

    private static BenchTier bucketBench(R196Material material) {
        return switch (material) {
            case COPPER, SILVER, GOLD -> BenchTier.COPPER;
            case IRON -> BenchTier.IRON;
            case ANCIENT_METAL -> BenchTier.ANCIENT_METAL;
            case MITHRIL -> BenchTier.MITHRIL;
            case ADAMANTIUM -> BenchTier.ADAMANTIUM;
            default -> throw new IllegalArgumentException("No R196 bucket bench for " + material);
        };
    }

    private static float bucketIngotDifficulty(R196Material material) {
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
            R196Material equipmentMaterial,
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
            R196Material equipmentMaterial,
            BenchTier requiredBench,
            float componentDifficulty,
            ItemLike component,
            boolean chainmail) {
        List<R196EquipmentType> pieces = chainmail
                ? R196EquipmentType.chainPieces()
                : R196EquipmentType.platePieces();
        for (R196EquipmentType piece : pieces) {
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

    private static List<String> armorPattern(R196EquipmentType piece) {
        return switch (piece) {
            case HELMET, CHAINMAIL_HELMET -> List.of("CCC", "C C");
            case CHESTPLATE, CHAINMAIL_CHESTPLATE -> List.of("C C", "CCC", "CCC");
            case LEGGINGS, CHAINMAIL_LEGGINGS -> List.of("CCC", "C C", "C C");
            case BOOTS, CHAINMAIL_BOOTS -> List.of("C C", "C C");
            default -> throw new IllegalArgumentException("Not an armor piece: " + piece);
        };
    }

    private static ItemLike raw(String path) {
        return ModItems.catalog().raw(path).holder();
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
            R196Material equipmentMaterial,
            BenchTier requiredBench,
            float ingotDifficulty,
            ItemLike ingot) {
        addCoreMetalTools(
                material,
                requiredBench,
                ingotDifficulty,
                ingot,
                equipment(equipmentMaterial, R196EquipmentType.PICKAXE),
                equipment(equipmentMaterial, R196EquipmentType.SHOVEL),
                equipment(equipmentMaterial, R196EquipmentType.AXE),
                equipment(equipmentMaterial, R196EquipmentType.HOE),
                equipment(equipmentMaterial, R196EquipmentType.SWORD));
    }

    private void addCatalogSpecialMetalTools(
            String material,
            R196Material equipmentMaterial,
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
                equipment(equipmentMaterial, R196EquipmentType.MATTOCK),
                1,
                key,
                List.of("III", " SI", " S "));
        addShaped(
                material + "_battle_axe",
                requiredBench,
                ingotDifficulty * 4.0F + STICK_DIFFICULTY * 2.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(equipmentMaterial, R196EquipmentType.BATTLE_AXE),
                1,
                key,
                List.of("I I", "ISI", " S "));
        addShaped(
                material + "_war_hammer",
                requiredBench,
                ingotDifficulty * 5.0F + STICK_DIFFICULTY * 2.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(equipmentMaterial, R196EquipmentType.WAR_HAMMER),
                1,
                key,
                List.of("III", "ISI", " S "));
        addShaped(
                material + "_scythe",
                requiredBench,
                ingotDifficulty * 2.0F + STICK_DIFFICULTY * 3.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(equipmentMaterial, R196EquipmentType.SCYTHE),
                1,
                key,
                List.of("SI ", "S I", "S  "));
        addShaped(
                material + "_hatchet",
                requiredBench,
                ingotDifficulty + STICK_DIFFICULTY * 2.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(equipmentMaterial, R196EquipmentType.HATCHET),
                1,
                key,
                List.of("SI", "S "));
        addShaped(
                material + "_shears",
                requiredBench,
                ingotDifficulty * 2.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(equipmentMaterial, R196EquipmentType.SHEARS),
                1,
                Map.of('I', Ingredient.of(ingot)),
                List.of(" I", "I "));
    }

    private void addMetalDagger(
            String material,
            R196Material equipmentMaterial,
            BenchTier requiredBench,
            float difficulty,
            ItemLike ingot) {
        addShaped(
                material + "_dagger",
                requiredBench,
                difficulty,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(equipmentMaterial, R196EquipmentType.DAGGER),
                1,
                Map.of(
                        'I', Ingredient.of(ingot),
                        'S', Ingredient.of(Items.STICK)),
                List.of("I", "S"));
    }

    private void addMetalBow(
            String material,
            R196Material equipmentMaterial,
            BenchTier requiredBench,
            float difficulty,
            ItemLike ingot) {
        addShaped(
                material + "_bow",
                requiredBench,
                difficulty,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(equipmentMaterial, R196EquipmentType.BOW),
                1,
                Map.of(
                        'I', Ingredient.of(ingot),
                        'S', Ingredient.of(Items.STICK),
                        'B', Ingredient.of(Items.STRING)),
                List.of(" SB", "SIB", " SB"));
    }

    private void addArrow(
            String material,
            R196Material equipmentMaterial,
            BenchTier requiredBench,
            float difficulty,
            ItemLike arrowhead) {
        addShaped(
                material + "_arrow",
                requiredBench,
                difficulty,
                CraftingBookCategory.EQUIPMENT,
                "",
                equipment(equipmentMaterial, R196EquipmentType.ARROW),
                1,
                Map.of(
                        'H', Ingredient.of(arrowhead),
                        'S', Ingredient.of(Items.STICK),
                        'F', Ingredient.of(Items.FEATHER)),
                List.of("H", "S", "F"));
    }

    private static ItemLike equipment(R196Material material, R196EquipmentType type) {
        return ModItems.catalog().equipment(material, type).holder();
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
                R196Material.COPPER,
                BenchTier.COPPER,
                Items.COPPER_INGOT,
                Blocks.COPPER_BLOCK.weathering().unaffected(),
                12_400.0F);
        addMetalAnvil(R196Material.SILVER, BenchTier.SILVER, ModItems.SILVER_INGOT, ModBlocks.SILVER_BLOCK, 12_400.0F);
        addMetalAnvil(R196Material.GOLD, BenchTier.GOLD, Items.GOLD_INGOT, Blocks.GOLD_BLOCK, 12_400.0F);
        addMetalAnvil(R196Material.IRON, BenchTier.IRON, Items.IRON_INGOT, Blocks.IRON_BLOCK, 24_800.0F);
        addMetalAnvil(
                R196Material.ANCIENT_METAL,
                BenchTier.ANCIENT_METAL,
                ModItems.ANCIENT_METAL_INGOT,
                ModBlocks.ANCIENT_METAL_BLOCK,
                49_600.0F);
        addMetalAnvil(
                R196Material.MITHRIL,
                BenchTier.MITHRIL,
                ModItems.MITHRIL_INGOT,
                ModBlocks.MITHRIL_BLOCK,
                198_400.0F);
        addMetalAnvil(
                R196Material.ADAMANTIUM,
                BenchTier.ADAMANTIUM,
                ModItems.ADAMANTIUM_INGOT,
                ModBlocks.ADAMANTIUM_BLOCK,
                793_600.0F);
    }

    private void addMetalAnvil(
            R196Material material,
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
                ModBlocks.metalAnvil(material),
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
