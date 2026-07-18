package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.crafting.BenchTier;
import com.pixulse.infx.crafting.TimedShapedRecipe;
import com.pixulse.infx.crafting.TimedShapelessRecipe;
import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.tag.ModTags;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

final class ModRecipeProvider extends RecipeProvider {
    private static final float STICK_DIFFICULTY = 25.0F;

    private ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
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
        addShaped(
                "copper_ingot_from_nuggets",
                BenchTier.FLINT,
                400.0F,
                CraftingBookCategory.MISC,
                "",
                Items.COPPER_INGOT,
                1,
                Map.of('N', Ingredient.of(Items.COPPER_NUGGET)),
                List.of("NNN", "NNN", "NNN"));
        addShapeless(
                "copper_nuggets_from_ingot",
                BenchTier.FLINT,
                400.0F,
                CraftingBookCategory.MISC,
                "",
                Items.COPPER_NUGGET,
                9,
                List.of(Ingredient.of(Items.COPPER_INGOT)));

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
