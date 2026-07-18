package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.crafting.BenchTier;
import com.pixulse.infx.crafting.TimedShapedRecipe;
import com.pixulse.infx.crafting.TimedShapelessRecipe;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;
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
