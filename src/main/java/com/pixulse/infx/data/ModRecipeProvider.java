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
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;

final class ModRecipeProvider extends RecipeProvider {
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

        addShaped(
                "copper_pickaxe",
                BenchTier.COPPER,
                1250.0F,
                CraftingBookCategory.EQUIPMENT,
                "",
                ModItems.COPPER_PICKAXE,
                1,
                Map.of(
                        'I', Ingredient.of(Items.COPPER_INGOT),
                        'S', Ingredient.of(Items.STICK)),
                List.of("III", " S ", " S "));
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
