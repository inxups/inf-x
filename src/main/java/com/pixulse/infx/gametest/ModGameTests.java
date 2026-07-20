package com.pixulse.infx.gametest;

import com.mojang.authlib.GameProfile;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.TieredWorkbenchBlock;
import com.pixulse.infx.block.entity.R196FurnaceBlockEntity;
import com.pixulse.infx.crafting.BenchTier;
import com.pixulse.infx.crafting.CraftingProfile;
import com.pixulse.infx.crafting.MiteCraftingRules;
import com.pixulse.infx.crafting.TimedCraftingEngine;
import com.pixulse.infx.crafting.TimedCraftingMenu;
import com.pixulse.infx.furnace.FurnaceHeatAccess;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.menu.TimedWorkbenchMenu;
import com.pixulse.infx.progression.R196Experience;
import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.registry.ModRecipes;
import com.pixulse.infx.server.ExtremeDifficulty;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.ChatFormatting;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
    private static final BlockPos WORK_POS = new BlockPos(1, 1, 1);
    private static final BlockPos FURNACE_POS = new BlockPos(3, 1, 1);
    private static final AtomicInteger PLAYER_SEQUENCE = new AtomicInteger();
    private static final List<String> DISABLED_VANILLA_RECIPES = List.of(
            "acacia_planks",
            "arrow",
            "bamboo_planks",
            "birch_planks",
            "bow",
            "bundle",
            "cherry_planks",
            "copper_axe",
            "copper_boots",
            "copper_chestplate",
            "copper_helmet",
            "copper_hoe",
            "copper_ingot",
            "copper_ingot_from_blasting_copper_ore",
            "copper_ingot_from_blasting_deepslate_copper_ore",
            "copper_ingot_from_blasting_raw_copper",
            "copper_ingot_from_nuggets",
            "copper_ingot_from_smelting_copper_ore",
            "copper_ingot_from_smelting_deepslate_copper_ore",
            "copper_ingot_from_smelting_raw_copper",
            "copper_ingot_from_waxed_copper_block",
            "copper_leggings",
            "copper_nugget",
            "copper_nugget_from_blasting",
            "copper_nugget_from_smelting",
            "copper_pickaxe",
            "copper_shovel",
            "copper_spear",
            "copper_sword",
            "crafter",
            "crafting_table",
            "crimson_planks",
            "dark_oak_planks",
            "diamond_spear",
            "furnace",
            "golden_spear",
            "glass",
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
            "iron_ingot_from_blasting_deepslate_iron_ore",
            "iron_ingot_from_blasting_iron_ore",
            "iron_ingot_from_blasting_raw_iron",
            "iron_ingot_from_nuggets",
            "iron_axe",
            "iron_boots",
            "iron_chestplate",
            "iron_helmet",
            "iron_hoe",
            "iron_leggings",
            "iron_pickaxe",
            "iron_shovel",
            "iron_spear",
            "iron_sword",
            "iron_nugget",
            "jungle_planks",
            "leather_boots",
            "leather_chestplate",
            "leather_helmet",
            "leather_leggings",
            "mangrove_planks",
            "netherite_spear_smithing",
            "netherite_block",
            "netherite_ingot",
            "netherite_ingot_from_netherite_block",
            "netherite_scrap",
            "netherite_scrap_from_blasting",
            "netherite_upgrade_smithing_template",
            "oak_planks",
            "pale_oak_planks",
            "raw_copper",
            "raw_copper_block",
            "sandstone",
            "shears",
            "smooth_sandstone",
            "spruce_planks",
            "stone_axe",
            "stone_hoe",
            "stone_pickaxe",
            "stone_shovel",
            "stone_spear",
            "stone_sword",
            "warped_planks",
            "wooden_axe",
            "wooden_hoe",
            "wooden_pickaxe",
            "wooden_shovel",
            "wooden_spear",
            "wooden_sword");
    private static final List<String> WEAPON_RECIPES = List.of(
            "wood_cudgel",
            "wood_club",
            "flint_knife",
            "obsidian_knife",
            "wood_bow",
            "ancient_metal_bow",
            "mithril_bow");
    private static final List<String> ARROW_MATERIALS = List.of(
            "flint", "obsidian", "copper", "silver", "gold", "iron", "ancient_metal", "mithril", "adamantium");
    private static final List<String> PLATE_ARMOR_MATERIALS = List.of(
            "leather", "copper", "silver", "gold", "iron", "ancient_metal", "mithril", "adamantium");
    private static final List<String> CHAIN_ARMOR_MATERIALS = List.of(
            "copper", "silver", "gold", "rusted_iron", "iron", "ancient_metal", "mithril", "adamantium");
    private static final List<String> PLATE_ARMOR_PIECES = List.of("helmet", "chestplate", "leggings", "boots");
    private static final List<String> CHAIN_ARMOR_PIECES =
            List.of("chainmail_helmet", "chainmail_chestplate", "chainmail_leggings", "chainmail_boots");
    private static final List<String> CORE_TOOL_RECIPES = List.of(
            "flint_axe",
            "copper_pickaxe",
            "copper_shovel",
            "copper_axe",
            "copper_hoe",
            "copper_sword",
            "iron_pickaxe",
            "iron_shovel",
            "iron_axe",
            "iron_hoe",
            "iron_sword",
            "silver_pickaxe",
            "silver_shovel",
            "silver_axe",
            "silver_hoe",
            "silver_sword",
            "gold_pickaxe",
            "gold_shovel",
            "gold_axe",
            "gold_hoe",
            "gold_sword",
            "ancient_metal_pickaxe",
            "ancient_metal_shovel",
            "ancient_metal_axe",
            "ancient_metal_hoe",
            "ancient_metal_sword",
            "mithril_pickaxe",
            "mithril_shovel",
            "mithril_axe",
            "mithril_hoe",
            "mithril_sword",
            "adamantium_pickaxe",
            "adamantium_shovel",
            "adamantium_axe",
            "adamantium_hoe",
            "adamantium_sword");
    private static final List<String> METAL_MATERIALS = List.of(
            "copper", "silver", "gold", "iron", "ancient_metal", "mithril", "adamantium");
    private static final List<String> SPECIAL_TOOL_TYPES =
            List.of("mattock", "battle_axe", "war_hammer", "scythe", "hatchet", "shears");

    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, InfiniteX.MOD_ID);

    private static final ResourceKey<Consumer<GameTestHelper>> HARVEST_RESTRICTIONS =
            functionKey("harvest_restrictions");
    private static final ResourceKey<Consumer<GameTestHelper>> BENCH_HIERARCHY =
            functionKey("bench_hierarchy");
    private static final ResourceKey<Consumer<GameTestHelper>> TIMED_CRAFTING =
            functionKey("timed_crafting");
    private static final ResourceKey<Consumer<GameTestHelper>> VANILLA_TIMED_CRAFTING =
            functionKey("vanilla_timed_crafting");
    private static final ResourceKey<Consumer<GameTestHelper>> VANILLA_CRAFTING_MENU =
            functionKey("vanilla_crafting_menu");
    private static final ResourceKey<Consumer<GameTestHelper>> CRAFTING_PROFILES =
            functionKey("crafting_profiles");
    private static final ResourceKey<Consumer<GameTestHelper>> TIMED_RESETS =
            functionKey("timed_resets");
    private static final ResourceKey<Consumer<GameTestHelper>> FULL_INVENTORY_DROP =
            functionKey("full_inventory_drop");
    private static final ResourceKey<Consumer<GameTestHelper>> RECIPE_BOUNDARIES =
            functionKey("recipe_boundaries");
    private static final ResourceKey<Consumer<GameTestHelper>> COPPER_LOOP =
            functionKey("copper_loop");
    private static final ResourceKey<Consumer<GameTestHelper>> IRON_LOOP =
            functionKey("iron_loop");
    private static final ResourceKey<Consumer<GameTestHelper>> CORE_TOOL_RECIPES_TEST =
            functionKey("core_tool_recipes");
    private static final ResourceKey<Consumer<GameTestHelper>> ADVANCED_CORE_TOOL_RECIPES =
            functionKey("advanced_core_tool_recipes");
    private static final ResourceKey<Consumer<GameTestHelper>> SPECIAL_TOOL_RECIPES =
            functionKey("special_tool_recipes");
    private static final ResourceKey<Consumer<GameTestHelper>> WEAPON_RECIPES_TEST =
            functionKey("weapon_recipes");
    private static final ResourceKey<Consumer<GameTestHelper>> ARMOR_RECIPES_TEST =
            functionKey("armor_recipes");
    private static final ResourceKey<Consumer<GameTestHelper>> FURNACE_HEAT_RULES =
            functionKey("furnace_heat_rules");
    private static final ResourceKey<Consumer<GameTestHelper>> FURNACE_TIER_RULES =
            functionKey("furnace_tier_rules");
    private static final ResourceKey<Consumer<GameTestHelper>> ADVANCED_FURNACE_RULES =
            functionKey("advanced_furnace_rules");
    private static final ResourceKey<Consumer<GameTestHelper>> EXTREME_DIFFICULTY =
            functionKey("extreme_difficulty");

    static {
        TEST_FUNCTIONS.register("harvest_restrictions", () -> ModGameTests::harvestRestrictions);
        TEST_FUNCTIONS.register("bench_hierarchy", () -> ModGameTests::benchHierarchy);
        TEST_FUNCTIONS.register("timed_crafting", () -> ModGameTests::timedCrafting);
        TEST_FUNCTIONS.register("vanilla_timed_crafting", () -> ModGameTests::vanillaTimedCrafting);
        TEST_FUNCTIONS.register("vanilla_crafting_menu", () -> ModGameTests::vanillaCraftingMenu);
        TEST_FUNCTIONS.register("crafting_profiles", () -> ModGameTests::craftingProfiles);
        TEST_FUNCTIONS.register("timed_resets", () -> ModGameTests::timedResets);
        TEST_FUNCTIONS.register("full_inventory_drop", () -> ModGameTests::fullInventoryDrop);
        TEST_FUNCTIONS.register("recipe_boundaries", () -> ModGameTests::recipeBoundaries);
        TEST_FUNCTIONS.register("copper_loop", () -> ModGameTests::copperLoop);
        TEST_FUNCTIONS.register("iron_loop", () -> ModGameTests::ironLoop);
        TEST_FUNCTIONS.register("core_tool_recipes", () -> ModGameTests::coreToolRecipes);
        TEST_FUNCTIONS.register("advanced_core_tool_recipes", () -> ModGameTests::advancedCoreToolRecipes);
        TEST_FUNCTIONS.register("special_tool_recipes", () -> ModGameTests::specialToolRecipes);
        TEST_FUNCTIONS.register("weapon_recipes", () -> ModGameTests::weaponRecipes);
        TEST_FUNCTIONS.register("armor_recipes", () -> ModGameTests::armorRecipes);
        TEST_FUNCTIONS.register("furnace_heat_rules", () -> ModGameTests::furnaceHeatRules);
        TEST_FUNCTIONS.register("furnace_tier_rules", () -> ModGameTests::furnaceTierRules);
        TEST_FUNCTIONS.register("advanced_furnace_rules", () -> ModGameTests::advancedFurnaceRules);
        TEST_FUNCTIONS.register("extreme_difficulty", () -> ModGameTests::extremeDifficulty);
    }

    private ModGameTests() {}

    public static void register(IEventBus modBus) {
        TEST_FUNCTIONS.register(modBus);
        modBus.addListener(ModGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                InfiniteX.id("m1"), new TestEnvironmentDefinition.AllOf());
        registerTest(event, HARVEST_RESTRICTIONS, environment, 40);
        registerTest(event, BENCH_HIERARCHY, environment, 80);
        registerTest(event, TIMED_CRAFTING, environment, 200);
        registerTest(event, VANILLA_TIMED_CRAFTING, environment, 120);
        registerTest(event, VANILLA_CRAFTING_MENU, environment, 120);
        registerTest(event, CRAFTING_PROFILES, environment, 80);
        registerTest(event, TIMED_RESETS, environment, 120);
        registerTest(event, FULL_INVENTORY_DROP, environment, 100);
        registerTest(event, RECIPE_BOUNDARIES, environment, 40);
        registerTest(event, COPPER_LOOP, environment, 400);
        registerTest(event, IRON_LOOP, environment, 700);
        registerTest(event, CORE_TOOL_RECIPES_TEST, environment, 240);
        registerTest(event, ADVANCED_CORE_TOOL_RECIPES, environment, 80);
        registerTest(event, SPECIAL_TOOL_RECIPES, environment, 80);
        registerTest(event, WEAPON_RECIPES_TEST, environment, 80);
        registerTest(event, ARMOR_RECIPES_TEST, environment, 120);
        registerTest(event, FURNACE_HEAT_RULES, environment, 600);
        registerTest(event, FURNACE_TIER_RULES, environment, 900);
        registerTest(event, ADVANCED_FURNACE_RULES, environment, 600);
        registerTest(event, EXTREME_DIFFICULTY, environment, 40);
    }

    private static void registerTest(
            RegisterGameTestsEvent event,
            ResourceKey<Consumer<GameTestHelper>> function,
            Holder<TestEnvironmentDefinition<?>> environment,
            int maxTicks) {
        TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
                environment,
                Identifier.withDefaultNamespace("empty"),
                maxTicks,
                0,
                true,
                Rotation.NONE);
        event.registerTest(function.identifier(), new FunctionGameTestInstance(function, data));
    }

    private static void harvestRestrictions(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        BlockPos absolutePos = helper.absolutePos(WORK_POS);

        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.setBlock(WORK_POS, Blocks.OAK_LOG);
        helper.assertFalse(player.gameMode.destroyBlock(absolutePos), "empty hand must not break logs");
        helper.assertTrue(helper.getBlockState(WORK_POS).is(Blocks.OAK_LOG), "cancelled log break must keep the block");

        player.setItemInHand(InteractionHand.MAIN_HAND, ModItems.FLINT_HATCHET.get().getDefaultInstance());
        helper.assertTrue(player.gameMode.destroyBlock(absolutePos), "flint hatchet must break logs");
        helper.assertTrue(helper.getBlockState(WORK_POS).isAir(), "allowed log break must remove the block");

        helper.setBlock(WORK_POS, Blocks.STONE);
        helper.assertFalse(player.gameMode.destroyBlock(absolutePos), "flint tier must not break pickaxe blocks");
        helper.assertTrue(helper.getBlockState(WORK_POS).is(Blocks.STONE), "cancelled stone break must keep the block");

        player.setItemInHand(InteractionHand.MAIN_HAND, ModItems.COPPER_PICKAXE.get().getDefaultInstance());
        helper.assertTrue(player.gameMode.destroyBlock(absolutePos), "copper pickaxe must break stone");

        helper.setBlock(WORK_POS, ModBlocks.SILVER_ORE.get());
        helper.assertTrue(player.gameMode.destroyBlock(absolutePos), "copper pickaxe must break silver ore");
        helper.assertTrue(
                helper.getBlockState(WORK_POS).isAir(),
                "a harvested silver ore block must be removed");

        player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.setBlock(WORK_POS, Blocks.OAK_LOG);
        helper.assertTrue(player.gameMode.destroyBlock(absolutePos), "creative players must bypass harvest restrictions");

        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        helper.setBlock(WORK_POS, ModBlocks.FLINT_WORKBENCH.get());
        helper.assertTrue(player.gameMode.destroyBlock(absolutePos), "workbenches must be recoverable with an empty hand");

        removePlayer(player);
        helper.succeed();
    }

    private static void extremeDifficulty(GameTestHelper helper) {
        Difficulty extreme = ExtremeDifficulty.value();
        Component displayName = extreme.getDisplayName();
        Component info = extreme.getInfo();
        ByteBuf networkBuffer = Unpooled.buffer();
        Difficulty decodedDifficulty;
        try {
            Difficulty.STREAM_CODEC.encode(networkBuffer, extreme);
            decodedDifficulty = Difficulty.STREAM_CODEC.decode(networkBuffer);
        } finally {
            networkBuffer.release();
        }
        var worldData = helper.getLevel().getServer().getWorldData();
        var difficultyCommand = helper.getLevel()
                .getServer()
                .getCommands()
                .getDispatcher()
                .getRoot()
                .getChild("difficulty");

        helper.assertTrue(Difficulty.values().length == 5, "Difficulty.values must contain five values");
        helper.assertTrue(extreme != Difficulty.HARD, "Extreme must be independent from vanilla Hard");
        helper.assertTrue(extreme.ordinal() == 4 && extreme.getId() == 4, "Extreme must use ordinal and ID 4");
        helper.assertTrue(Difficulty.valueOf("EXTREME") == extreme, "Enum.valueOf must resolve Extreme");
        helper.assertTrue(Difficulty.byName("extreme") == extreme, "the difficulty codec must resolve extreme");
        helper.assertTrue(decodedDifficulty == extreme, "the network codec must resolve Extreme");
        helper.assertTrue(displayName.getString().equals("extreme"), "Extreme must use a literal lowercase name");
        helper.assertTrue(info.getString().equals("extreme"), "Extreme info must not require a translation key");
        helper.assertTrue(
                TextColor.fromLegacyFormat(ChatFormatting.RED).equals(displayName.getStyle().getColor()),
                "Extreme display text must be red");
        helper.assertTrue(
                TextColor.fromLegacyFormat(ChatFormatting.RED).equals(info.getStyle().getColor()),
                "Extreme info text must be red");
        helper.assertTrue(worldData.getDifficulty() == extreme, "the server must start on Extreme");
        helper.assertTrue(worldData.isDifficultyLocked(), "the server must lock Extreme");
        helper.assertTrue(
                difficultyCommand != null && difficultyCommand.getChild("extreme") != null,
                "/difficulty extreme must be registered by the vanilla difficulty command");
        helper.succeed();
    }

    private static void benchHierarchy(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);

        TimedCraftingMenu hand = (TimedCraftingMenu) player.inventoryMenu;
        player.containerMenu = player.inventoryMenu;
        hand.infx$craftingContainer().setItem(0, Items.LEATHER.getDefaultInstance());
        helper.assertTrue(TimedCraftingEngine.refreshResult(hand, player, true), "hand recipe must resolve in the 2x2 grid");
        assertResult(helper, hand, ModItems.SINEW.get(), "hand recipe result");

        clearGrid(hand.infx$craftingContainer());
        helper.setBlock(WORK_POS, ModBlocks.FLINT_WORKBENCH.get());
        helper.setBlock(WORK_POS.above(), Blocks.STONE);
        helper.assertTrue(
                TieredWorkbenchBlock.isObstructed(helper.getLevel(), helper.absolutePos(WORK_POS)),
                "a full block above the workbench must obstruct it");
        helper.setBlock(WORK_POS.above(), Blocks.AIR);
        helper.assertFalse(
                TieredWorkbenchBlock.isObstructed(helper.getLevel(), helper.absolutePos(WORK_POS)),
                "clearing the block above must clear the obstruction");
        TimedWorkbenchMenu flint = workbenchMenu(player, helper, BenchTier.FLINT, ModBlocks.FLINT_WORKBENCH.get(), 1);
        player.containerMenu = flint;
        fillCopperPickaxe(flint.infx$craftingContainer());
        helper.assertFalse(TimedCraftingEngine.refreshResult(flint, player, true), "flint bench must reject copper-tier recipes");

        clearGrid(flint.infx$craftingContainer());
        flint.infx$craftingContainer().setItem(0, Items.LEATHER.getDefaultInstance());
        helper.assertTrue(TimedCraftingEngine.refreshResult(flint, player, true), "flint bench must accept hand recipes");
        assertResult(helper, flint, ModItems.SINEW.get(), "flint bench lower-tier result");

        flint.removed(player);
        helper.setBlock(WORK_POS, ModBlocks.COPPER_WORKBENCH.get());
        TimedWorkbenchMenu copper = workbenchMenu(player, helper, BenchTier.COPPER, ModBlocks.COPPER_WORKBENCH.get(), 2);
        player.containerMenu = copper;
        copper.infx$craftingContainer().setItem(0, Items.LEATHER.getDefaultInstance());
        helper.assertTrue(TimedCraftingEngine.refreshResult(copper, player, true), "copper bench must accept hand recipes");

        clearGrid(copper.infx$craftingContainer());
        fillCopperPickaxe(copper.infx$craftingContainer());
        helper.assertTrue(TimedCraftingEngine.refreshResult(copper, player, true), "copper bench must accept copper recipes");
        assertResult(helper, copper, ModItems.COPPER_PICKAXE.get(), "copper bench result");

        removePlayer(player);
        helper.succeed();
    }

    private static void timedCrafting(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        helper.onEachTick(player::doTick);
        TimedCraftingMenu menu = (TimedCraftingMenu) player.inventoryMenu;
        player.containerMenu = player.inventoryMenu;
        menu.infx$craftingContainer().setItem(0, Items.LEATHER.getDefaultInstance());
        helper.assertTrue(TimedCraftingEngine.refreshResult(menu, player, true), "leather recipe must have a timed preview");

        player.inventoryMenu.clicked(0, 1, ContainerInput.PICKUP, player);
        helper.assertTrue(player.inventoryMenu.getCarried().isEmpty(), "right click must not take a timed result");
        helper.assertTrue(menu.infx$craftingContainer().getItem(0).getCount() == 1, "right click must not consume timed inputs");
        helper.assertFalse(menu.infx$craftingState().isRunning(), "right click must not start timed crafting");

        player.inventoryMenu.clicked(0, 0, ContainerInput.PICKUP, player);
        helper.assertTrue(menu.infx$craftingState().isRunning(), "left click must start timed crafting");
        helper.assertTrue(countItem(player.getInventory(), ModItems.SINEW.get()) == 0, "result click must not craft immediately");
        helper.assertTrue(menu.infx$craftingContainer().getItem(0).getCount() == 1, "input must remain until completion");

        int[] pausedProgress = new int[1];
        helper.startSequence()
                .thenExecuteAfter(10, () -> {
                    helper.assertTrue(countItem(player.getInventory(), ModItems.SINEW.get()) == 0, "result must still be delayed");
                    player.getFoodData().setFoodLevel(0);
                    pausedProgress[0] = menu.infx$craftingState().progressTicks();
                })
                .thenExecuteAfter(10, () -> {
                    helper.assertTrue(
                            menu.infx$craftingState().progressTicks() == pausedProgress[0],
                            "zero hunger must pause without losing progress");
                    player.getFoodData().setFoodLevel(20);
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(player.getInventory(), ModItems.SINEW.get()) == 4,
                        "completion must place four sinew in the inventory"))
                .thenExecute(() -> {
                    helper.assertTrue(menu.infx$craftingContainer().getItem(0).isEmpty(), "completion must consume leather");
                    helper.assertFalse(menu.infx$craftingState().isRunning(), "exhausted ingredients must stop repetition");
                    removePlayer(player);
                })
                .thenSucceed();
    }

    private static void vanillaTimedCrafting(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        helper.onEachTick(player::doTick);
        player.containerMenu = player.inventoryMenu;
        TimedCraftingMenu menu = (TimedCraftingMenu) player.inventoryMenu;
        CraftingContainer grid = menu.infx$craftingContainer();
        grid.setItem(0, Items.COAL.getDefaultInstance());
        grid.setItem(2, Items.STICK.getDefaultInstance());

        helper.assertTrue(
                TimedCraftingEngine.refreshResult(menu, player, true),
                "an enabled vanilla torch recipe must resolve in the timed hand grid");
        assertResult(helper, menu, Items.TORCH, "vanilla torch timed preview");
        player.inventoryMenu.clicked(0, 0, ContainerInput.PICKUP, player);
        helper.assertTrue(menu.infx$craftingState().isRunning(), "vanilla recipe must start a timed craft");
        helper.assertTrue(countItem(player.getInventory(), Items.TORCH) == 0, "vanilla result must not be immediate");

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(player.getInventory(), Items.TORCH) == 4,
                        "the timed path must eventually produce the vanilla torch result"))
                .thenExecute(() -> {
                    helper.assertTrue(grid.getItem(0).isEmpty(), "vanilla completion must consume coal");
                    helper.assertTrue(grid.getItem(2).isEmpty(), "vanilla completion must consume the stick");
                    removePlayer(player);
                })
                .thenSucceed();
    }

    private static void vanillaCraftingMenu(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        helper.onEachTick(player::doTick);
        helper.setBlock(WORK_POS, Blocks.CRAFTING_TABLE);
        CraftingMenu vanilla = new CraftingMenu(
                31,
                player.getInventory(),
                ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(WORK_POS)));
        helper.assertTrue(vanilla instanceof TimedCraftingMenu, "the vanilla crafting menu must receive the timed mixin");
        player.containerMenu = vanilla;
        TimedCraftingMenu timed = (TimedCraftingMenu) vanilla;
        CraftingContainer grid = timed.infx$craftingContainer();

        // Place a 1x2 recipe away from the top-left corner. This verifies that
        // completion maps the trimmed CraftingInput back to the original grid.
        grid.setItem(4, Items.COAL.getDefaultInstance());
        grid.setItem(7, Items.STICK.getDefaultInstance());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(timed, player, true),
                "the vanilla 3x3 menu must resolve enabled vanilla recipes through the timed engine");
        assertResult(helper, timed, Items.TORCH, "vanilla crafting-table timed preview");
        vanilla.clicked(0, 0, ContainerInput.PICKUP, player);
        helper.assertTrue(timed.infx$craftingState().isRunning(), "crafting-table result must start a timer");

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(player.getInventory(), Items.TORCH) == 4,
                        "the vanilla crafting-table timer must complete"))
                .thenExecute(() -> {
                    helper.assertTrue(grid.getItem(4).isEmpty(), "offset coal slot must be consumed");
                    helper.assertTrue(grid.getItem(7).isEmpty(), "offset stick slot must be consumed");
                    removePlayer(player);
                })
                .thenSucceed();
    }

    private static void craftingProfiles(GameTestHelper helper) {
        assertDifficulty(helper, Items.STICK, 25.0F);
        assertDifficulty(helper, Items.EXPERIENCE_BOTTLE, 25.0F);
        assertDifficulty(helper, Items.ARROW, 40.0F);
        assertDifficulty(helper, Items.LEATHER, 100.0F);
        assertDifficulty(helper, Items.FLINT, 100.0F);
        assertDifficulty(helper, Items.OBSIDIAN, 240.0F);
        assertDifficulty(helper, Items.COAL_BLOCK, 120.0F);
        assertDifficulty(helper, Items.LAPIS_BLOCK, 300.0F);
        assertDifficulty(helper, Items.COPPER_INGOT, 400.0F);
        assertDifficulty(helper, Items.IRON_INGOT, 800.0F);
        assertDifficulty(helper, Items.EMERALD, 800.0F);
        assertDifficulty(helper, Items.QUARTZ, 900.0F);
        assertDifficulty(helper, Items.DIAMOND, 1600.0F);
        assertDifficulty(helper, Items.IRON_BLOCK, 7200.0F);
        assertDifficulty(helper, Items.DIAMOND_BLOCK, 14_400.0F);
        assertDifficulty(helper, Items.QUARTZ_BLOCK, 3600.0F);
        assertDifficulty(helper, Items.AMETHYST_SHARD, 25.0F);
        assertDifficulty(
                helper,
                ModItems.catalog().raw("rusted_iron_chain").holder().get(),
                400.0F * 4.0F / 9.0F);

        var recipeMap = helper.getLevel().recipeAccess().recipeMap();
        var vanillaRecipes = recipeMap.byType(RecipeType.CRAFTING);
        helper.assertTrue(!vanillaRecipes.isEmpty(), "the server must load enabled 26.2 crafting recipes");
        for (var holder : vanillaRecipes) {
            try {
                CraftingProfile profile = MiteCraftingRules.displayProfile(holder.value());
                helper.assertTrue(
                        Float.isFinite(profile.difficulty()) && profile.difficulty() > 0.0F,
                        holder.id().identifier() + " must have a finite positive inferred difficulty");
                helper.assertTrue(
                        BenchTier.ADAMANTIUM.supports(profile.requiredBench()),
                        holder.id().identifier() + " must map to a supported workbench tier");
            } catch (RuntimeException error) {
                helper.fail("failed to infer crafting profile for " + holder.id().identifier() + ": " + error);
            }
        }

        var explicitRecipes = recipeMap.byType(ModRecipes.CRAFTING.get());
        helper.assertTrue(!explicitRecipes.isEmpty(), "InfiniteX explicit R196 crafting recipes must be loaded");
        for (var holder : explicitRecipes) {
            CraftingProfile profile = CraftingProfile.explicit(
                    holder.value().requiredBench(), holder.value().difficulty());
            helper.assertTrue(
                    Float.isFinite(profile.difficulty()) && profile.difficulty() > 0.0F,
                    holder.id().identifier() + " must retain a finite positive explicit difficulty");
            helper.assertTrue(
                    BenchTier.ADAMANTIUM.supports(profile.requiredBench()),
                    holder.id().identifier() + " must retain a supported explicit workbench tier");
        }
        helper.succeed();
    }

    private static void fullInventoryDrop(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        helper.onEachTick(player::doTick);
        grantMaximumExperience(player);
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            player.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        }

        TimedCraftingMenu menu = (TimedCraftingMenu) player.inventoryMenu;
        player.containerMenu = player.inventoryMenu;
        menu.infx$craftingContainer().setItem(0, Items.LEATHER.getDefaultInstance());
        helper.assertTrue(TimedCraftingEngine.refreshResult(menu, player, true), "full inventory test recipe must resolve");
        player.inventoryMenu.clicked(0, 0, ContainerInput.PICKUP, player);

        helper.startSequence()
                .thenWaitUntil(() -> {
                    var nearbyItems = helper.getLevel().getEntities(
                            EntityTypes.ITEM, player.getBoundingBox().inflate(4.0), entity -> entity.isAlive());
                    helper.assertTrue(
                            nearbyItems.stream().anyMatch(entity -> entity.getItem().is(ModItems.SINEW.get())),
                            "a full inventory must drop the crafted result at the player; progress="
                                    + menu.infx$craftingState().progressTicks()
                                    + "/"
                                    + menu.infx$craftingState().requiredTicks()
                                    + ", running="
                                    + menu.infx$craftingState().isRunning()
                                    + ", inventorySinew="
                                    + countItem(player.getInventory(), ModItems.SINEW.get())
                                    + ", nearby="
                                    + nearbyItems.stream().map(entity -> entity.getItem().toString()).toList());
                })
                .thenExecute(() -> {
                    helper.assertTrue(countItem(player.getInventory(), ModItems.SINEW.get()) == 0, "full inventory must not retain the result");
                    helper.assertTrue(menu.infx$craftingContainer().getItem(0).isEmpty(), "dropped crafting must still consume its input");
                    removePlayer(player);
                })
                .thenSucceed();
    }

    private static void timedResets(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        TimedCraftingMenu menu = (TimedCraftingMenu) player.inventoryMenu;
        player.containerMenu = player.inventoryMenu;
        menu.infx$craftingContainer().setItem(0, Items.LEATHER.getDefaultInstance());
        TimedCraftingEngine.refreshResult(menu, player, true);
        player.inventoryMenu.clicked(0, 0, ContainerInput.QUICK_MOVE, player);
        helper.assertTrue(menu.infx$craftingState().isRunning(), "shift-click must start timed crafting");

        helper.startSequence()
                .thenExecuteAfter(5, () -> {
                    menu.infx$craftingContainer().setItem(0, Items.FLINT.getDefaultInstance());
                    TimedCraftingEngine.refreshResult(menu, player, true);
                    helper.assertFalse(menu.infx$craftingState().isRunning(), "changing recipe must reset crafting");
                    helper.assertTrue(menu.infx$craftingState().progressTicks() == 0, "recipe change must clear progress");
                    assertResult(helper, menu, ModItems.FLINT_CHIP.get(), "changed recipe preview");
                    player.inventoryMenu.clicked(0, 0, ContainerInput.QUICK_MOVE, player);
                    helper.assertTrue(menu.infx$craftingState().isRunning(), "shift-click must restart the changed recipe");
                })
                .thenExecuteAfter(5, () -> {
                    player.closeContainer();
                    helper.assertFalse(menu.infx$craftingState().isRunning(), "closing the menu must reset crafting");
                    helper.assertTrue(menu.infx$craftingState().requiredTicks() == 0, "closing must clear required ticks");
                    removePlayer(player);
                })
                .thenSucceed();
    }

    private static void recipeBoundaries(GameTestHelper helper) {
        var recipes = helper.getLevel().recipeAccess().recipeMap();
        for (String path : DISABLED_VANILLA_RECIPES) {
            var loaded = recipes.byKey(recipeKey("minecraft", path));
            if (loaded != null) {
                List<String> sources = helper.getLevel()
                        .getServer()
                        .getResourceManager()
                        .getResourceStack(Identifier.fromNamespaceAndPath("minecraft", "recipe/" + path + ".json"))
                        .stream()
                        .map(resource -> resource.sourcePackId())
                        .toList();
                helper.fail("minecraft:" + path + " must be disabled; loaded "
                        + loaded.value().getClass().getName() + " from resource stack " + sources);
            }
        }
        helper.assertTrue(recipes.byKey(recipeKey("infx", "oak_planks")) != null, "InfiniteX plank recipe must exist");
        for (String material : METAL_MATERIALS) {
            helper.assertTrue(
                    recipes.byKey(recipeKey("infx", material + "_ingot_from_nuggets")) != null,
                    "InfiniteX ingot conversion must exist: " + material);
            helper.assertTrue(
                    recipes.byKey(recipeKey("infx", material + "_nuggets_from_ingot")) != null,
                    "InfiniteX nugget conversion must exist: " + material);
        }
        for (String path : CORE_TOOL_RECIPES) {
            helper.assertTrue(
                    recipes.byKey(recipeKey("infx", path)) != null,
                    "InfiniteX core tool recipe must exist: " + path);
        }
        for (String material : METAL_MATERIALS) {
            for (String type : SPECIAL_TOOL_TYPES) {
                String path = material + "_" + type;
                helper.assertTrue(
                        recipes.byKey(recipeKey("infx", path)) != null,
                        "InfiniteX special tool recipe must exist: " + path);
            }
        }
        for (String path : List.of("obsidian_hatchet", "obsidian_shovel", "obsidian_axe")) {
            helper.assertTrue(
                    recipes.byKey(recipeKey("infx", path)) != null,
                    "InfiniteX obsidian tool recipe must exist: " + path);
        }
        for (String path : WEAPON_RECIPES) {
            helper.assertTrue(
                    recipes.byKey(recipeKey("infx", path)) != null,
                    "InfiniteX weapon recipe must exist: " + path);
        }
        for (String material : METAL_MATERIALS) {
            String path = material + "_dagger";
            helper.assertTrue(
                    recipes.byKey(recipeKey("infx", path)) != null,
                    "InfiniteX dagger recipe must exist: " + path);
        }
        for (String material : ARROW_MATERIALS) {
            String path = material + "_arrow";
            helper.assertTrue(
                    recipes.byKey(recipeKey("infx", path)) != null,
                    "InfiniteX arrow recipe must exist: " + path);
        }
        for (String material : METAL_MATERIALS) {
            for (String conversion : List.of("chain_from_nuggets", "nuggets_from_chain")) {
                String path = material + "_" + conversion;
                helper.assertTrue(
                        recipes.byKey(recipeKey("infx", path)) != null,
                        "InfiniteX chain conversion must exist: " + path);
            }
        }
        for (String material : PLATE_ARMOR_MATERIALS) {
            for (String piece : PLATE_ARMOR_PIECES) {
                String path = material + "_" + piece;
                helper.assertTrue(
                        recipes.byKey(recipeKey("infx", path)) != null,
                        "InfiniteX plate armor recipe must exist: " + path);
            }
        }
        for (String material : CHAIN_ARMOR_MATERIALS) {
            for (String piece : CHAIN_ARMOR_PIECES) {
                String path = material + "_" + piece;
                helper.assertTrue(
                        recipes.byKey(recipeKey("infx", path)) != null,
                        "InfiniteX chain armor recipe must exist: " + path);
            }
        }
        helper.assertTrue(recipes.byKey(recipeKey("infx", "flint_shovel")) != null, "InfiniteX flint shovel recipe must exist");
        helper.assertTrue(recipes.byKey(recipeKey("infx", "cobblestone_furnace")) != null, "InfiniteX furnace recipe must exist");
        helper.assertTrue(
                recipes.byKey(recipeKey("minecraft", "iron_ingot_from_smelting_raw_iron")) != null,
                "raw iron must retain its furnace recipe");
        helper.succeed();
    }

    private static void copperLoop(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        helper.onEachTick(player::doTick);
        grantMaximumExperience(player);
        helper.setBlock(WORK_POS, ModBlocks.FLINT_WORKBENCH.get());

        TimedWorkbenchMenu flint = workbenchMenu(player, helper, BenchTier.FLINT, ModBlocks.FLINT_WORKBENCH.get(), 1);
        player.containerMenu = flint;
        CraftingContainer grid = flint.infx$craftingContainer();
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            grid.setItem(slot, new ItemStack(Items.COPPER_NUGGET, 4));
        }
        helper.assertTrue(TimedCraftingEngine.refreshResult(flint, player, true), "36 nuggets must match the ingot recipe");
        flint.clicked(0, 0, ContainerInput.PICKUP, player);

        TimedWorkbenchMenu[] copperMenu = new TimedWorkbenchMenu[1];
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(player.getInventory(), Items.COPPER_INGOT) == 4,
                        "36 nuggets must continuously craft into exactly four ingots; actual="
                                + countItem(player.getInventory(), Items.COPPER_INGOT)
                                + ", remainingNuggets=" + countGridItem(grid, Items.COPPER_NUGGET)
                                + ", progress=" + flint.infx$craftingState().progressTicks()
                                + "/" + flint.infx$craftingState().requiredTicks()
                                + ", running=" + flint.infx$craftingState().isRunning()
                                + ", food=" + player.getFoodData().getFoodLevel()))
                .thenExecute(() -> {
                    helper.assertTrue(countGridItem(grid, Items.COPPER_NUGGET) == 0, "all 36 nuggets must be consumed");
                    ItemStack ingot = takeItem(helper, player.getInventory(), Items.COPPER_INGOT, 1);
                    grid.setItem(0, ingot);
                    grid.setItem(1, Items.LEATHER.getDefaultInstance());
                    grid.setItem(3, Items.STICK.getDefaultInstance());
                    grid.setItem(4, Blocks.OAK_PLANKS.asItem().getDefaultInstance());
                    helper.assertTrue(TimedCraftingEngine.refreshResult(flint, player, true), "four loop ingots must unlock the copper bench recipe");
                    flint.clicked(0, 0, ContainerInput.PICKUP, player);
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(player.getInventory(), ModItems.COPPER_WORKBENCH.get()) == 1,
                        "one ingot must craft the copper workbench"))
                .thenExecute(() -> {
                    takeItem(helper, player.getInventory(), ModItems.COPPER_WORKBENCH.get(), 1);
                    player.closeContainer();
                    helper.setBlock(WORK_POS, ModBlocks.COPPER_WORKBENCH.get());

                    TimedWorkbenchMenu copper = workbenchMenu(
                            player, helper, BenchTier.COPPER, ModBlocks.COPPER_WORKBENCH.get(), 2);
                    copperMenu[0] = copper;
                    player.containerMenu = copper;
                    ItemStack ingots = takeItem(helper, player.getInventory(), Items.COPPER_INGOT, 3);
                    copper.infx$craftingContainer().setItem(0, ingots.split(1));
                    copper.infx$craftingContainer().setItem(1, ingots.split(1));
                    copper.infx$craftingContainer().setItem(2, ingots.split(1));
                    copper.infx$craftingContainer().setItem(4, Items.STICK.getDefaultInstance());
                    copper.infx$craftingContainer().setItem(7, Items.STICK.getDefaultInstance());
                    helper.assertTrue(TimedCraftingEngine.refreshResult(copper, player, true), "remaining three ingots must match the copper pickaxe");
                    copper.clicked(0, 0, ContainerInput.PICKUP, player);
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(player.getInventory(), ModItems.COPPER_PICKAXE.get()) == 1,
                        "copper workbench must finish the InfiniteX copper pickaxe"))
                .thenExecute(() -> {
                    helper.assertTrue(countItem(player.getInventory(), Items.COPPER_INGOT) == 0, "the loop must use exactly four ingots");
                    helper.assertTrue(countGridItem(copperMenu[0].infx$craftingContainer(), Items.COPPER_INGOT) == 0, "pickaxe must consume three ingots");
                    removePlayer(player);
                })
                .thenSucceed();
    }

    private static void ironLoop(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        helper.onEachTick(player::doTick);
        grantMaximumExperience(player);
        helper.setBlock(WORK_POS, ModBlocks.COPPER_WORKBENCH.get());

        TimedWorkbenchMenu copper = workbenchMenu(
                player, helper, BenchTier.COPPER, ModBlocks.COPPER_WORKBENCH.get(), 3);
        player.containerMenu = copper;
        CraftingContainer grid = copper.infx$craftingContainer();
        fillFurnace(grid);
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(copper, player, true),
                "eight cobblestone must match the copper-tier furnace recipe");
        copper.clicked(0, 0, ContainerInput.PICKUP, player);

        FurnaceBlockEntity[] furnace = new FurnaceBlockEntity[1];
        TimedWorkbenchMenu[] ironMenu = new TimedWorkbenchMenu[1];
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(player.getInventory(), Items.FURNACE) == 1,
                        "copper workbench must finish the cobblestone furnace"))
                .thenExecute(() -> {
                    takeItem(helper, player.getInventory(), Items.FURNACE, 1);
                    helper.setBlock(FURNACE_POS, Blocks.FURNACE);
                    furnace[0] = helper.getBlockEntity(FURNACE_POS, FurnaceBlockEntity.class);
                    furnace[0].setItem(0, Items.RAW_IRON.getDefaultInstance());
                    furnace[0].setItem(1, Items.COAL.getDefaultInstance());
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        furnace[0].getItem(2).is(Items.IRON_INGOT),
                        "the crafted furnace must smelt raw iron"))
                .thenExecute(() -> {
                    FurnaceResultSlot resultSlot = new FurnaceResultSlot(player, furnace[0], 2, 0, 0);
                    ItemStack smelted = resultSlot.remove(1);
                    helper.assertTrue(smelted.is(Items.IRON_INGOT), "furnace output must be an iron ingot");
                    resultSlot.onTake(player, smelted);
                    var acquireIron = helper.getLevel()
                            .getServer()
                            .getAdvancements()
                            .get(InfiniteX.id("progression/acquire_iron"));
                    helper.assertTrue(acquireIron != null, "acquire iron advancement must be loaded");
                    helper.assertTrue(
                            player.getAdvancements().getOrStartProgress(acquireIron).isDone(),
                            "taking furnace output must grant acquire iron");
                    player.getInventory().add(smelted);
                    player.getInventory().add(new ItemStack(Items.IRON_INGOT, 3));

                    clearGrid(grid);
                    grid.setItem(0, takeItem(helper, player.getInventory(), Items.IRON_INGOT, 1));
                    grid.setItem(1, Items.LEATHER.getDefaultInstance());
                    grid.setItem(3, Items.STICK.getDefaultInstance());
                    grid.setItem(4, Blocks.OAK_PLANKS.asItem().getDefaultInstance());
                    helper.assertTrue(
                            TimedCraftingEngine.refreshResult(copper, player, true),
                            "the first iron ingot must unlock the iron workbench");
                    copper.clicked(0, 0, ContainerInput.PICKUP, player);
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(player.getInventory(), ModItems.IRON_WORKBENCH.get()) == 1,
                        "copper workbench must finish the iron workbench"))
                .thenExecute(() -> {
                    takeItem(helper, player.getInventory(), ModItems.IRON_WORKBENCH.get(), 1);
                    player.closeContainer();
                    helper.setBlock(WORK_POS, ModBlocks.IRON_WORKBENCH.get());

                    TimedWorkbenchMenu iron = workbenchMenu(
                            player, helper, BenchTier.IRON, ModBlocks.IRON_WORKBENCH.get(), 4);
                    ironMenu[0] = iron;
                    player.containerMenu = iron;
                    CraftingContainer ironGrid = iron.infx$craftingContainer();
                    ItemStack ingots = takeItem(helper, player.getInventory(), Items.IRON_INGOT, 3);
                    ironGrid.setItem(0, ingots.split(1));
                    ironGrid.setItem(1, ingots.split(1));
                    ironGrid.setItem(2, ingots.split(1));
                    ironGrid.setItem(4, Items.STICK.getDefaultInstance());
                    ironGrid.setItem(7, Items.STICK.getDefaultInstance());
                    helper.assertTrue(
                            TimedCraftingEngine.refreshResult(iron, player, true),
                            "three iron ingots must match the iron pickaxe recipe");
                    iron.clicked(0, 0, ContainerInput.PICKUP, player);
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(player.getInventory(), ModItems.IRON_PICKAXE.get()) == 1,
                        "iron workbench must finish the InfiniteX iron pickaxe"))
                .thenExecute(() -> {
                    helper.assertTrue(
                            countGridItem(ironMenu[0].infx$craftingContainer(), Items.IRON_INGOT) == 0,
                            "iron pickaxe must consume three ingots");
                    removePlayer(player);
                })
                .thenSucceed();
    }

    private static void coreToolRecipes(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        helper.onEachTick(player::doTick);
        grantMaximumExperience(player);
        helper.setBlock(WORK_POS, ModBlocks.FLINT_WORKBENCH.get());

        TimedWorkbenchMenu flint = workbenchMenu(
                player, helper, BenchTier.FLINT, ModBlocks.FLINT_WORKBENCH.get(), 5);
        player.containerMenu = flint;
        fillFlintAxe(flint.infx$craftingContainer());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(flint, player, true),
                "three flint, two sticks, and binding must match the flint axe recipe");
        assertResult(helper, flint, ModItems.FLINT_AXE.get(), "flint axe preview");
        flint.clicked(0, 0, ContainerInput.PICKUP, player);

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(player.getInventory(), ModItems.FLINT_AXE.get()) == 1,
                        "flint workbench must finish the flint axe"))
                .thenExecute(() -> {
                    assertAdvancementDone(helper, player, "build_axe", "crafting a full axe must grant Lumberjack");
                    takeItem(helper, player.getInventory(), ModItems.FLINT_AXE.get(), 1);
                    player.closeContainer();
                    helper.setBlock(WORK_POS, ModBlocks.COPPER_WORKBENCH.get());

                    TimedWorkbenchMenu copper = workbenchMenu(
                            player, helper, BenchTier.COPPER, ModBlocks.COPPER_WORKBENCH.get(), 6);
                    player.containerMenu = copper;
                    fillMetalHoe(copper.infx$craftingContainer(), Items.COPPER_INGOT);
                    helper.assertTrue(
                            TimedCraftingEngine.refreshResult(copper, player, true),
                            "two copper ingots and two sticks must match the copper hoe recipe");
                    assertResult(helper, copper, ModItems.COPPER_HOE.get(), "copper hoe preview");
                    copper.clicked(0, 0, ContainerInput.PICKUP, player);
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(player.getInventory(), ModItems.COPPER_HOE.get()) == 1,
                        "copper workbench must finish the copper hoe"))
                .thenExecute(() -> {
                    assertAdvancementDone(helper, player, "build_hoe", "crafting a hoe must grant Time to Farm");
                    takeItem(helper, player.getInventory(), ModItems.COPPER_HOE.get(), 1);
                    player.closeContainer();
                    helper.setBlock(WORK_POS, ModBlocks.IRON_WORKBENCH.get());

                    TimedWorkbenchMenu iron = workbenchMenu(
                            player, helper, BenchTier.IRON, ModBlocks.IRON_WORKBENCH.get(), 7);
                    player.containerMenu = iron;
                    fillMetalSword(iron.infx$craftingContainer(), Items.IRON_INGOT);
                    helper.assertTrue(
                            TimedCraftingEngine.refreshResult(iron, player, true),
                            "two iron ingots and one stick must match the iron sword recipe");
                    assertResult(helper, iron, ModItems.IRON_SWORD.get(), "iron sword preview");
                    iron.clicked(0, 0, ContainerInput.PICKUP, player);
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(player.getInventory(), ModItems.IRON_SWORD.get()) == 1,
                        "iron workbench must finish the InfiniteX iron sword"))
                .thenExecute(() -> removePlayer(player))
                .thenSucceed();
    }

    private static void advancedCoreToolRecipes(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        grantMaximumExperience(player);

        TimedCraftingMenu hand = (TimedCraftingMenu) player.inventoryMenu;
        player.containerMenu = player.inventoryMenu;
        hand.infx$craftingContainer().setItem(0, ModItems.ADAMANTIUM_INGOT.toStack());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(hand, player, true),
                "metal ingots must split into nuggets in the hand crafting grid");
        assertResult(helper, hand, ModItems.ADAMANTIUM_NUGGET.get(), "adamantium nugget preview");
        clearGrid(hand.infx$craftingContainer());

        helper.setBlock(WORK_POS, ModBlocks.COPPER_WORKBENCH.get());
        TimedWorkbenchMenu copper = workbenchMenu(
                player, helper, BenchTier.COPPER, ModBlocks.COPPER_WORKBENCH.get(), 8);
        player.containerMenu = copper;
        fillMetalSword(copper.infx$craftingContainer(), ModItems.SILVER_INGOT.get());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(copper, player, true),
                "copper-tier workbenches must accept silver core tools");
        assertResult(
                helper,
                copper,
                equipment(R196Material.SILVER, R196EquipmentType.SWORD),
                "silver sword preview");
        clearGrid(copper.infx$craftingContainer());
        fillMetalHoe(copper.infx$craftingContainer(), Items.GOLD_INGOT);
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(copper, player, true),
                "copper-tier workbenches must accept gold core tools");
        assertResult(
                helper,
                copper,
                equipment(R196Material.GOLD, R196EquipmentType.HOE),
                "gold hoe preview");

        player.closeContainer();
        helper.setBlock(WORK_POS, ModBlocks.ANCIENT_METAL_WORKBENCH.get());
        TimedWorkbenchMenu ancientMetal = workbenchMenu(
                player, helper, BenchTier.ANCIENT_METAL, ModBlocks.ANCIENT_METAL_WORKBENCH.get(), 9);
        player.containerMenu = ancientMetal;
        fillMetalSword(ancientMetal.infx$craftingContainer(), ModItems.MITHRIL_INGOT.get());
        helper.assertFalse(
                TimedCraftingEngine.refreshResult(ancientMetal, player, true),
                "ancient metal workbenches must reject mithril core tools");

        player.closeContainer();
        helper.setBlock(WORK_POS, ModBlocks.MITHRIL_WORKBENCH.get());
        TimedWorkbenchMenu mithril = workbenchMenu(
                player, helper, BenchTier.MITHRIL, ModBlocks.MITHRIL_WORKBENCH.get(), 10);
        player.containerMenu = mithril;
        fillMetalSword(mithril.infx$craftingContainer(), ModItems.MITHRIL_INGOT.get());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(mithril, player, true),
                "mithril workbenches must accept mithril core tools");
        assertResult(
                helper,
                mithril,
                equipment(R196Material.MITHRIL, R196EquipmentType.SWORD),
                "mithril sword preview");
        clearGrid(mithril.infx$craftingContainer());
        fillMetalSword(mithril.infx$craftingContainer(), ModItems.ADAMANTIUM_INGOT.get());
        helper.assertFalse(
                TimedCraftingEngine.refreshResult(mithril, player, true),
                "mithril workbenches must reject adamantium core tools");

        player.closeContainer();
        helper.setBlock(WORK_POS, ModBlocks.ADAMANTIUM_WORKBENCH.get());
        TimedWorkbenchMenu adamantium = workbenchMenu(
                player, helper, BenchTier.ADAMANTIUM, ModBlocks.ADAMANTIUM_WORKBENCH.get(), 11);
        player.containerMenu = adamantium;
        fillMetalSword(adamantium.infx$craftingContainer(), ModItems.ADAMANTIUM_INGOT.get());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(adamantium, player, true),
                "adamantium workbenches must accept adamantium core tools");
        assertResult(
                helper,
                adamantium,
                equipment(R196Material.ADAMANTIUM, R196EquipmentType.SWORD),
                "adamantium sword preview");

        removePlayer(player);
        helper.succeed();
    }

    private static void specialToolRecipes(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        grantMaximumExperience(player);

        TimedCraftingMenu hand = (TimedCraftingMenu) player.inventoryMenu;
        player.containerMenu = player.inventoryMenu;
        hand.infx$craftingContainer().setItem(0, Items.OBSIDIAN.getDefaultInstance());
        hand.infx$craftingContainer().setItem(1, Items.STICK.getDefaultInstance());
        hand.infx$craftingContainer().setItem(2, ModItems.SINEW.toStack());
        hand.infx$craftingContainer().setItem(3, Items.STICK.getDefaultInstance());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(hand, player, true),
                "obsidian hatchets must resolve in the hand crafting grid");
        assertResult(
                helper,
                hand,
                equipment(R196Material.OBSIDIAN, R196EquipmentType.HATCHET),
                "obsidian hatchet preview");
        clearGrid(hand.infx$craftingContainer());

        helper.setBlock(WORK_POS, ModBlocks.FLINT_WORKBENCH.get());
        TimedWorkbenchMenu flint = workbenchMenu(
                player, helper, BenchTier.FLINT, ModBlocks.FLINT_WORKBENCH.get(), 12);
        player.containerMenu = flint;
        CraftingContainer flintGrid = flint.infx$craftingContainer();
        flintGrid.setItem(0, Items.OBSIDIAN.getDefaultInstance());
        flintGrid.setItem(1, Items.OBSIDIAN.getDefaultInstance());
        flintGrid.setItem(3, Items.OBSIDIAN.getDefaultInstance());
        flintGrid.setItem(4, Items.STICK.getDefaultInstance());
        flintGrid.setItem(6, ModItems.SINEW.toStack());
        flintGrid.setItem(7, Items.STICK.getDefaultInstance());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(flint, player, true),
                "obsidian axes must resolve on a flint workbench");
        assertResult(
                helper,
                flint,
                equipment(R196Material.OBSIDIAN, R196EquipmentType.AXE),
                "obsidian axe preview");

        player.closeContainer();
        helper.setBlock(WORK_POS, ModBlocks.COPPER_WORKBENCH.get());
        TimedWorkbenchMenu copper = workbenchMenu(
                player, helper, BenchTier.COPPER, ModBlocks.COPPER_WORKBENCH.get(), 13);
        player.containerMenu = copper;
        CraftingContainer copperGrid = copper.infx$craftingContainer();
        copperGrid.setItem(1, ModItems.SILVER_INGOT.toStack());
        copperGrid.setItem(3, ModItems.SILVER_INGOT.toStack());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(copper, player, true),
                "copper-tier workbenches must accept silver shears");
        assertResult(
                helper,
                copper,
                equipment(R196Material.SILVER, R196EquipmentType.SHEARS),
                "silver shears preview");

        player.closeContainer();
        helper.setBlock(WORK_POS, ModBlocks.IRON_WORKBENCH.get());
        TimedWorkbenchMenu iron = workbenchMenu(
                player, helper, BenchTier.IRON, ModBlocks.IRON_WORKBENCH.get(), 14);
        player.containerMenu = iron;
        fillWarHammer(iron.infx$craftingContainer(), ModItems.MITHRIL_INGOT.get());
        helper.assertFalse(
                TimedCraftingEngine.refreshResult(iron, player, true),
                "iron workbenches must reject mithril war hammers");

        player.closeContainer();
        helper.setBlock(WORK_POS, ModBlocks.MITHRIL_WORKBENCH.get());
        TimedWorkbenchMenu mithril = workbenchMenu(
                player, helper, BenchTier.MITHRIL, ModBlocks.MITHRIL_WORKBENCH.get(), 15);
        player.containerMenu = mithril;
        fillWarHammer(mithril.infx$craftingContainer(), ModItems.MITHRIL_INGOT.get());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(mithril, player, true),
                "mithril workbenches must accept mithril war hammers");
        assertResult(
                helper,
                mithril,
                equipment(R196Material.MITHRIL, R196EquipmentType.WAR_HAMMER),
                "mithril war hammer preview");

        player.closeContainer();
        helper.setBlock(WORK_POS, ModBlocks.ADAMANTIUM_WORKBENCH.get());
        TimedWorkbenchMenu adamantium = workbenchMenu(
                player, helper, BenchTier.ADAMANTIUM, ModBlocks.ADAMANTIUM_WORKBENCH.get(), 16);
        player.containerMenu = adamantium;
        CraftingContainer adamantiumGrid = adamantium.infx$craftingContainer();
        adamantiumGrid.setItem(0, Items.STICK.getDefaultInstance());
        adamantiumGrid.setItem(1, ModItems.ADAMANTIUM_INGOT.toStack());
        adamantiumGrid.setItem(3, Items.STICK.getDefaultInstance());
        adamantiumGrid.setItem(5, ModItems.ADAMANTIUM_INGOT.toStack());
        adamantiumGrid.setItem(6, Items.STICK.getDefaultInstance());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(adamantium, player, true),
                "adamantium workbenches must accept adamantium scythes");
        assertResult(
                helper,
                adamantium,
                equipment(R196Material.ADAMANTIUM, R196EquipmentType.SCYTHE),
                "adamantium scythe preview");

        removePlayer(player);
        helper.succeed();
    }

    private static void weaponRecipes(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        grantMaximumExperience(player);

        TimedCraftingMenu hand = (TimedCraftingMenu) player.inventoryMenu;
        player.containerMenu = player.inventoryMenu;
        CraftingContainer handGrid = hand.infx$craftingContainer();
        handGrid.setItem(0, Items.FLINT.getDefaultInstance());
        handGrid.setItem(1, ModItems.SINEW.toStack());
        handGrid.setItem(2, Items.STICK.getDefaultInstance());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(hand, player, true),
                "flint knives must resolve in the hand crafting grid");
        assertResult(
                helper,
                hand,
                equipment(R196Material.FLINT, R196EquipmentType.KNIFE),
                "flint knife preview");

        player.closeContainer();
        helper.setBlock(WORK_POS, ModBlocks.FLINT_WORKBENCH.get());
        TimedWorkbenchMenu flint = workbenchMenu(
                player, helper, BenchTier.FLINT, ModBlocks.FLINT_WORKBENCH.get(), 17);
        player.containerMenu = flint;
        CraftingContainer flintGrid = flint.infx$craftingContainer();
        for (int slot : List.of(1, 3, 7)) {
            flintGrid.setItem(slot, Items.STICK.getDefaultInstance());
        }
        for (int slot : List.of(2, 5, 8)) {
            flintGrid.setItem(slot, ModItems.SINEW.toStack());
        }
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(flint, player, true),
                "wood bows must resolve on a flint workbench");
        assertResult(
                helper,
                flint,
                equipment(R196Material.WOOD, R196EquipmentType.BOW),
                "wood bow preview");

        player.closeContainer();
        helper.setBlock(WORK_POS, ModBlocks.COPPER_WORKBENCH.get());
        TimedWorkbenchMenu copper = workbenchMenu(
                player, helper, BenchTier.COPPER, ModBlocks.COPPER_WORKBENCH.get(), 18);
        player.containerMenu = copper;
        CraftingContainer copperGrid = copper.infx$craftingContainer();
        copperGrid.setItem(0, Items.GOLD_INGOT.getDefaultInstance());
        copperGrid.setItem(3, Items.STICK.getDefaultInstance());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(copper, player, true),
                "copper-tier workbenches must accept gold daggers");
        assertResult(
                helper,
                copper,
                equipment(R196Material.GOLD, R196EquipmentType.DAGGER),
                "gold dagger preview");

        player.closeContainer();
        helper.setBlock(WORK_POS, ModBlocks.IRON_WORKBENCH.get());
        TimedWorkbenchMenu iron = workbenchMenu(
                player, helper, BenchTier.IRON, ModBlocks.IRON_WORKBENCH.get(), 19);
        player.containerMenu = iron;
        CraftingContainer ironGrid = iron.infx$craftingContainer();
        ironGrid.setItem(0, ModItems.MITHRIL_NUGGET.toStack());
        ironGrid.setItem(3, Items.STICK.getDefaultInstance());
        ironGrid.setItem(6, Items.FEATHER.getDefaultInstance());
        helper.assertFalse(
                TimedCraftingEngine.refreshResult(iron, player, true),
                "iron workbenches must reject mithril arrows");

        player.closeContainer();
        helper.setBlock(WORK_POS, ModBlocks.MITHRIL_WORKBENCH.get());
        TimedWorkbenchMenu mithril = workbenchMenu(
                player, helper, BenchTier.MITHRIL, ModBlocks.MITHRIL_WORKBENCH.get(), 20);
        player.containerMenu = mithril;
        CraftingContainer mithrilGrid = mithril.infx$craftingContainer();
        mithrilGrid.setItem(0, ModItems.MITHRIL_NUGGET.toStack());
        mithrilGrid.setItem(3, Items.STICK.getDefaultInstance());
        mithrilGrid.setItem(6, Items.FEATHER.getDefaultInstance());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(mithril, player, true),
                "mithril workbenches must accept mithril arrows");
        assertResult(
                helper,
                mithril,
                equipment(R196Material.MITHRIL, R196EquipmentType.ARROW),
                "mithril arrow preview");

        removePlayer(player);
        helper.succeed();
    }

    private static void armorRecipes(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        helper.onEachTick(player::doTick);
        grantMaximumExperience(player);

        helper.setBlock(WORK_POS, ModBlocks.FLINT_WORKBENCH.get());
        TimedWorkbenchMenu flint = workbenchMenu(
                player, helper, BenchTier.FLINT, ModBlocks.FLINT_WORKBENCH.get(), 21);
        player.containerMenu = flint;
        CraftingContainer flintGrid = flint.infx$craftingContainer();
        for (int slot : List.of(0, 1, 2, 3, 5)) {
            flintGrid.setItem(slot, Items.LEATHER.getDefaultInstance());
        }
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(flint, player, true),
                "leather helmets must resolve on a flint workbench");
        assertResult(
                helper,
                flint,
                equipment(R196Material.LEATHER, R196EquipmentType.HELMET),
                "leather helmet preview");
        clearGrid(flintGrid);
        for (int slot : List.of(1, 3, 5, 7)) {
            flintGrid.setItem(slot, Items.COPPER_NUGGET.getDefaultInstance());
        }
        helper.assertFalse(
                TimedCraftingEngine.refreshResult(flint, player, true),
                "flint workbenches must reject copper chain crafting");

        player.closeContainer();
        helper.setBlock(WORK_POS, ModBlocks.COPPER_WORKBENCH.get());
        TimedWorkbenchMenu copper = workbenchMenu(
                player, helper, BenchTier.COPPER, ModBlocks.COPPER_WORKBENCH.get(), 22);
        player.containerMenu = copper;
        CraftingContainer copperGrid = copper.infx$craftingContainer();
        for (int slot : List.of(1, 3, 5, 7)) {
            copperGrid.setItem(slot, Items.COPPER_NUGGET.getDefaultInstance());
        }
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(copper, player, true),
                "copper workbenches must accept copper chain crafting");
        assertResult(
                helper,
                copper,
                ModItems.catalog().raw("copper_chain").holder().get(),
                "copper chain preview");
        clearGrid(copperGrid);
        for (int slot : List.of(0, 2, 3, 5)) {
            copperGrid.setItem(slot, ModItems.catalog().raw("copper_chain").holder().get().getDefaultInstance());
        }
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(copper, player, true),
                "copper chainmail boots must resolve on a copper workbench");
        assertResult(
                helper,
                copper,
                equipment(R196Material.COPPER, R196EquipmentType.CHAINMAIL_BOOTS),
                "copper chainmail boots preview");

        player.closeContainer();
        helper.setBlock(WORK_POS, ModBlocks.IRON_WORKBENCH.get());
        TimedWorkbenchMenu iron = workbenchMenu(
                player, helper, BenchTier.IRON, ModBlocks.IRON_WORKBENCH.get(), 23);
        player.containerMenu = iron;
        CraftingContainer ironGrid = iron.infx$craftingContainer();
        for (int slot : List.of(0, 1, 2, 3, 5)) {
            ironGrid.setItem(slot, ModItems.MITHRIL_INGOT.toStack());
        }
        helper.assertFalse(
                TimedCraftingEngine.refreshResult(iron, player, true),
                "iron workbenches must reject mithril plate armor");

        player.closeContainer();
        helper.setBlock(WORK_POS, ModBlocks.MITHRIL_WORKBENCH.get());
        TimedWorkbenchMenu mithril = workbenchMenu(
                player, helper, BenchTier.MITHRIL, ModBlocks.MITHRIL_WORKBENCH.get(), 24);
        player.containerMenu = mithril;
        CraftingContainer mithrilGrid = mithril.infx$craftingContainer();
        for (int slot : List.of(0, 1, 2, 3, 5)) {
            mithrilGrid.setItem(slot, ModItems.MITHRIL_INGOT.toStack());
        }
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(mithril, player, true),
                "mithril workbenches must accept mithril plate armor");
        assertResult(
                helper,
                mithril,
                equipment(R196Material.MITHRIL, R196EquipmentType.HELMET),
                "mithril helmet preview");
        player.closeContainer();

        player.setItemSlot(
                EquipmentSlot.CHEST,
                equipment(R196Material.LEATHER, R196EquipmentType.CHESTPLATE).getDefaultInstance());
        helper.startSequence()
                .thenWaitUntil(() -> assertAdvancementDone(
                        helper, player, "wear_leather", "wearing leather armor must grant Suiting Up"))
                .thenExecute(() -> {
                    player.setItemSlot(
                            EquipmentSlot.HEAD,
                            equipment(R196Material.COPPER, R196EquipmentType.HELMET).getDefaultInstance());
                    player.setItemSlot(
                            EquipmentSlot.CHEST,
                            equipment(R196Material.GOLD, R196EquipmentType.CHESTPLATE).getDefaultInstance());
                    player.setItemSlot(
                            EquipmentSlot.LEGS,
                            equipment(R196Material.IRON, R196EquipmentType.LEGGINGS).getDefaultInstance());
                    player.setItemSlot(
                            EquipmentSlot.FEET,
                            equipment(R196Material.MITHRIL, R196EquipmentType.BOOTS).getDefaultInstance());
                })
                .thenWaitUntil(() -> assertAdvancementDone(
                        helper,
                        player,
                        "wear_all_plate_armor",
                        "a mixed full metal plate set must grant Tin Can"))
                .thenExecute(() -> {
                    player.setItemSlot(
                            EquipmentSlot.HEAD,
                            equipment(R196Material.ADAMANTIUM, R196EquipmentType.HELMET).getDefaultInstance());
                    player.setItemSlot(
                            EquipmentSlot.CHEST,
                            equipment(R196Material.ADAMANTIUM, R196EquipmentType.CHESTPLATE).getDefaultInstance());
                    player.setItemSlot(
                            EquipmentSlot.LEGS,
                            equipment(R196Material.ADAMANTIUM, R196EquipmentType.LEGGINGS).getDefaultInstance());
                    player.setItemSlot(
                            EquipmentSlot.FEET,
                            equipment(R196Material.ADAMANTIUM, R196EquipmentType.BOOTS).getDefaultInstance());
                })
                .thenWaitUntil(() -> assertAdvancementDone(
                        helper,
                        player,
                        "wear_all_adamantium_plate_armor",
                        "a full adamantium plate set must grant Juggernaut"))
                .thenExecute(() -> removePlayer(player))
                .thenSucceed();
    }

    private static void furnaceHeatRules(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        helper.onEachTick(player::doTick);
        var furnaceState = Blocks.FURNACE.defaultBlockState()
                .setValue(AbstractFurnaceBlock.FACING, Direction.NORTH);
        helper.setBlock(FURNACE_POS, furnaceState);
        FurnaceBlockEntity[] furnace = {
            helper.getBlockEntity(FURNACE_POS, FurnaceBlockEntity.class)
        };
        furnace[0].setItem(0, Items.RAW_IRON.getDefaultInstance());
        furnace[0].setItem(1, Items.CHARCOAL.getDefaultInstance());

        helper.startSequence()
                .thenExecuteAfter(40, () -> {
                    helper.assertTrue(
                            furnace[0].getItem(2).isEmpty(),
                            "heat-1 charcoal must not smelt heat-2 raw iron");
                    helper.assertTrue(
                            furnace[0].getItem(1).is(Items.CHARCOAL),
                            "insufficient fuel must not be consumed");
                    helper.assertFalse(
                            helper.getBlockState(FURNACE_POS).getValue(AbstractFurnaceBlock.LIT),
                            "insufficient fuel must not light the furnace");
                    furnace[0].setItem(1, Items.LAVA_BUCKET.getDefaultInstance());
                })
                .thenExecuteAfter(5, () -> {
                    helper.assertTrue(
                            furnace[0].getItem(1).is(Items.LAVA_BUCKET),
                            "heat-3 lava must exceed the cobblestone furnace capacity");
                    helper.assertFalse(
                            helper.getBlockState(FURNACE_POS).getValue(AbstractFurnaceBlock.LIT),
                            "overheated fuel must not light the furnace");
                    furnace[0].setItem(1, Items.COAL.getDefaultInstance());
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        furnace[0].getItem(2).is(Items.IRON_INGOT),
                        "heat-2 coal must smelt raw iron"))
                .thenExecute(() -> {
                    furnace[0].setItem(2, ItemStack.EMPTY);
                    furnace[0].setItem(0, ModItems.SILVER_ORE.toStack());
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        furnace[0].getItem(2).is(ModItems.SILVER_INGOT),
                        "heat-2 coal must smelt silver ore"))
                .thenExecute(() -> {
                    helper.setBlock(FURNACE_POS, Blocks.AIR);
                    helper.setBlock(FURNACE_POS, furnaceState);
                    furnace[0] = helper.getBlockEntity(FURNACE_POS, FurnaceBlockEntity.class);
                    furnace[0].setItem(0, Items.CHICKEN.getDefaultInstance());
                    furnace[0].setItem(1, Items.CHARCOAL.getDefaultInstance());
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        helper.getBlockState(FURNACE_POS).getValue(AbstractFurnaceBlock.LIT),
                        "heat-1 charcoal must cook food"))
                .thenExecute(() -> helper.setBlock(FURNACE_POS.north(), Blocks.STONE))
                .thenExecuteAfter(2, () -> {
                    helper.assertFalse(
                            helper.getBlockState(FURNACE_POS).getValue(AbstractFurnaceBlock.LIT),
                            "a solid block in front must extinguish the furnace");
                    helper.assertTrue(
                            furnace[0].getItem(2).isEmpty(),
                            "an obstructed furnace must not finish cooking");

                    BlockPos absolutePos = helper.absolutePos(FURNACE_POS);
                    BlockHitResult hit = new BlockHitResult(
                            Vec3.atCenterOf(absolutePos), Direction.UP, absolutePos, false);
                    InteractionResult interaction = player.gameMode.useItemOn(
                            player,
                            helper.getLevel(),
                            player.getItemInHand(InteractionHand.MAIN_HAND),
                            InteractionHand.MAIN_HAND,
                            hit);
                    helper.assertTrue(
                            interaction == InteractionResult.FAIL,
                            "an obstructed furnace must reject opening");
                    helper.assertTrue(
                            player.containerMenu == player.inventoryMenu,
                            "an obstructed furnace must not open a menu");
                    removePlayer(player);
                })
                .thenSucceed();
    }

    private static void furnaceTierRules(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        helper.onEachTick(player::doTick);
        var clayState = ModBlocks.CLAY_FURNACE.get()
                .defaultBlockState()
                .setValue(AbstractFurnaceBlock.FACING, Direction.NORTH);
        helper.setBlock(FURNACE_POS, clayState);
        R196FurnaceBlockEntity[] furnace = {
            helper.getBlockEntity(FURNACE_POS, R196FurnaceBlockEntity.class)
        };
        ItemStack sandBatch = new ItemStack(Items.SAND, 4);
        helper.assertFalse(
                furnace[0].canPlaceItem(0, sandBatch),
                "the clay oven must reject large sand input");
        helper.assertFalse(
                furnace[0].canPlaceItem(1, Items.OAK_LOG.getDefaultInstance()),
                "the clay oven must reject large wood fuel");
        helper.assertTrue(
                furnace[0].canPlaceItem(0, Items.CHICKEN.getDefaultInstance()),
                "the clay oven must accept small food input");
        helper.assertTrue(
                furnace[0].canPlaceItem(1, Items.CHARCOAL.getDefaultInstance()),
                "the clay oven must accept small heat-1 fuel");

        player.openMenu(furnace[0]);
        helper.assertFalse(
                player.containerMenu.getSlot(0).mayPlace(sandBatch),
                "the clay oven input slot must reject large items");
        helper.assertFalse(
                player.containerMenu.getSlot(1).mayPlace(Items.OAK_LOG.getDefaultInstance()),
                "the clay oven fuel slot must reject large items");
        helper.assertTrue(
                player.containerMenu.getSlot(1).mayPlace(Items.CHARCOAL.getDefaultInstance()),
                "the clay oven fuel slot must accept charcoal");
        player.closeContainer();

        furnace[0].setItem(0, Items.CHICKEN.getDefaultInstance());
        furnace[0].setItem(1, Items.CHARCOAL.getDefaultInstance());
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(
                        furnace[0].getItem(2).is(Items.COOKED_CHICKEN),
                        "the clay oven must cook small food with heat 1"))
                .thenExecute(() -> {
                    helper.setBlock(FURNACE_POS, Blocks.AIR);
                    var sandstoneState = ModBlocks.SANDSTONE_FURNACE.get()
                            .defaultBlockState()
                            .setValue(AbstractFurnaceBlock.FACING, Direction.NORTH);
                    helper.setBlock(FURNACE_POS, sandstoneState);
                    furnace[0] = helper.getBlockEntity(FURNACE_POS, R196FurnaceBlockEntity.class);

                    player.openMenu(furnace[0]);
                    helper.assertTrue(
                            player.containerMenu.getSlot(0).mayPlace(sandBatch),
                            "the sandstone oven must accept large sand input");
                    helper.assertFalse(
                            player.containerMenu.getSlot(1).mayPlace(Items.COAL.getDefaultInstance()),
                            "the heat-1 sandstone oven must reject coal");
                    helper.assertTrue(
                            player.containerMenu.getSlot(1).mayPlace(Items.CHARCOAL.getDefaultInstance()),
                            "the sandstone oven must accept charcoal");
                    player.closeContainer();

                    furnace[0].setItem(0, new ItemStack(Items.SAND, 4));
                    furnace[0].setItem(1, Items.COAL.getDefaultInstance());
                })
                .thenExecuteAfter(20, () -> {
                    helper.assertTrue(
                            furnace[0].getItem(2).isEmpty(),
                            "coal must not run in the heat-1 sandstone oven");
                    helper.assertTrue(
                            furnace[0].getItem(1).is(Items.COAL),
                            "rejected coal must not be consumed");
                    furnace[0].setItem(1, Items.CHARCOAL.getDefaultInstance());
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        furnace[0].getItem(2).is(Items.SANDSTONE),
                        "four sand at heat 1 must produce one sandstone"))
                .thenExecute(() -> {
                    helper.assertTrue(
                            furnace[0].getItem(0).isEmpty(),
                            "a completed sand batch must consume all four sand");
                    helper.setBlock(FURNACE_POS, Blocks.AIR);
                    helper.setBlock(
                            FURNACE_POS,
                            Blocks.FURNACE.defaultBlockState()
                                    .setValue(AbstractFurnaceBlock.FACING, Direction.NORTH));
                    FurnaceBlockEntity cobblestone =
                            helper.getBlockEntity(FURNACE_POS, FurnaceBlockEntity.class);
                    cobblestone.setItem(0, new ItemStack(Items.SAND, 3));
                    cobblestone.setItem(1, Items.COAL.getDefaultInstance());
                })
                .thenExecuteAfter(40, () -> {
                    FurnaceBlockEntity cobblestone =
                            helper.getBlockEntity(FURNACE_POS, FurnaceBlockEntity.class);
                    helper.assertTrue(
                            cobblestone.getItem(2).isEmpty(),
                            "fewer than four sand must not start a batch");
                    helper.assertTrue(
                            cobblestone.getItem(1).is(Items.COAL),
                            "an incomplete sand batch must not consume fuel");
                    cobblestone.setItem(0, new ItemStack(Items.SAND, 4));
                })
                .thenWaitUntil(() -> {
                    FurnaceBlockEntity cobblestone =
                            helper.getBlockEntity(FURNACE_POS, FurnaceBlockEntity.class);
                    helper.assertTrue(
                            cobblestone.getItem(2).is(Items.GLASS),
                            "four sand at heat 2 must produce one glass");
                })
                .thenExecute(() -> {
                    FurnaceBlockEntity cobblestone =
                            helper.getBlockEntity(FURNACE_POS, FurnaceBlockEntity.class);
                    helper.assertTrue(
                            cobblestone.getItem(0).isEmpty(),
                            "the heat-2 glass batch must consume four sand");
                    removePlayer(player);
                })
                .thenSucceed();
    }

    private static void advancedFurnaceRules(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        helper.onEachTick(player::doTick);
        helper.setBlock(
                FURNACE_POS,
                ModBlocks.LARGE_CLAY_OVEN.get()
                        .defaultBlockState()
                        .setValue(AbstractFurnaceBlock.FACING, Direction.NORTH));
        R196FurnaceBlockEntity[] furnace = {
            helper.getBlockEntity(FURNACE_POS, R196FurnaceBlockEntity.class)
        };

        helper.assertTrue(
                furnace[0].canPlaceItem(0, new ItemStack(Items.SAND, 4)),
                "the large clay oven must accept full blocks");
        helper.assertTrue(
                furnace[0].canPlaceItem(1, Items.OAK_LOG.getDefaultInstance()),
                "the large clay oven must accept large heat-1 fuel");
        helper.assertFalse(
                furnace[0].canPlaceItem(1, Items.COAL.getDefaultInstance()),
                "the large clay oven must reject heat-2 coal");

        player.openMenu(furnace[0]);
        helper.assertTrue(
                player.containerMenu.getSlot(0).mayPlace(new ItemStack(Items.SAND, 4)),
                "the large clay oven menu must accept full blocks");
        helper.assertFalse(
                player.containerMenu.getSlot(1).mayPlace(Items.COAL.getDefaultInstance()),
                "the large clay oven menu must enforce its heat-1 ceiling");
        player.closeContainer();

        helper.setBlock(FURNACE_POS, Blocks.AIR);
        helper.setBlock(
                FURNACE_POS,
                ModBlocks.OBSIDIAN_FURNACE.get()
                        .defaultBlockState()
                        .setValue(AbstractFurnaceBlock.FACING, Direction.NORTH));
        furnace[0] = helper.getBlockEntity(FURNACE_POS, R196FurnaceBlockEntity.class);
        helper.assertTrue(
                furnace[0].canPlaceItem(1, Items.LAVA_BUCKET.getDefaultInstance()),
                "the obsidian furnace must accept heat-3 lava");
        helper.assertFalse(
                furnace[0].canPlaceItem(1, Items.BLAZE_ROD.getDefaultInstance()),
                "the obsidian furnace must reject heat-4 blaze rods");
        furnace[0].setItem(0, ModItems.MITHRIL_ORE.toStack());
        furnace[0].setItem(1, Items.LAVA_BUCKET.getDefaultInstance());

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(
                        ((FurnaceHeatAccess) (Object) furnace[0]).infx$currentHeat() == 3,
                        "the obsidian furnace must burn lava at heat 3"))
                .thenWaitUntil(() -> helper.assertTrue(
                        furnace[0].getItem(2).is(ModItems.MITHRIL_INGOT),
                        "heat-3 lava must smelt mithril ore in the obsidian furnace"))
                .thenExecute(() -> {
                    helper.setBlock(FURNACE_POS, Blocks.AIR);
                    helper.setBlock(
                            FURNACE_POS,
                            ModBlocks.NETHERRACK_FURNACE.get()
                                    .defaultBlockState()
                                    .setValue(AbstractFurnaceBlock.FACING, Direction.NORTH));
                    furnace[0] = helper.getBlockEntity(FURNACE_POS, R196FurnaceBlockEntity.class);
                    helper.assertTrue(
                            furnace[0].canPlaceItem(1, Items.BLAZE_ROD.getDefaultInstance()),
                            "the netherrack furnace must accept heat-4 blaze rods");
                    furnace[0].setItem(0, ModItems.ADAMANTIUM_ORE.toStack());
                    furnace[0].setItem(1, Items.LAVA_BUCKET.getDefaultInstance());
                })
                .thenExecuteAfter(40, () -> {
                    helper.assertTrue(
                            furnace[0].getItem(0).is(ModItems.ADAMANTIUM_ORE),
                            "heat-3 lava must not start heat-4 adamantium ore");
                    helper.assertTrue(
                            furnace[0].getItem(1).is(Items.LAVA_BUCKET),
                            "insufficient heat must not consume the lava bucket");
                    furnace[0].setItem(1, Items.BLAZE_ROD.getDefaultInstance());
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        ((FurnaceHeatAccess) (Object) furnace[0]).infx$currentHeat() == 4,
                        "the netherrack furnace must burn blaze rods at heat 4"))
                .thenWaitUntil(() -> helper.assertTrue(
                        furnace[0].getItem(2).is(ModItems.ADAMANTIUM_INGOT),
                        "heat-4 blaze fuel must smelt adamantium ore in the netherrack furnace"))
                .thenExecute(() -> removePlayer(player))
                .thenSucceed();
    }

    private static ServerPlayer createPlayer(GameTestHelper helper) {
        String name = "infx-test-" + PLAYER_SEQUENCE.incrementAndGet();
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
        ServerPlayer player = new ServerPlayer(
                helper.getLevel().getServer(), helper.getLevel(), profile, cookie.clientInformation());
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(20);
        Vec3 position = helper.absoluteVec(Vec3.atBottomCenterOf(WORK_POS.above()));
        player.snapTo(position.x, position.y, position.z, 0.0F, 0.0F);
        return player;
    }

    private static void removePlayer(ServerPlayer player) {
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }
        player.level().getServer().getPlayerList().remove(player);
    }

    private static void grantMaximumExperience(ServerPlayer player) {
        R196Experience.setTotal(player, R196Experience.XP_AT_DISPLAY_CAP);
    }

    private static TimedWorkbenchMenu workbenchMenu(
            ServerPlayer player,
            GameTestHelper helper,
            BenchTier tier,
            Block block,
            int containerId) {
        return TimedWorkbenchMenu.server(
                containerId,
                player.getInventory(),
                tier,
                ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(WORK_POS)),
                block);
    }

    private static void assertResult(GameTestHelper helper, TimedCraftingMenu menu, Item item, String description) {
        helper.assertTrue(menu.infx$resultContainer().getItem(0).is(item), description);
    }

    private static void assertDifficulty(GameTestHelper helper, Item item, float expected) {
        float actual = MiteCraftingRules.componentDifficulty(item.getDefaultInstance());
        helper.assertTrue(
                Math.abs(actual - expected) < 0.001F,
                item + " component difficulty must be " + expected + ", actual=" + actual);
    }

    private static void assertAdvancementDone(
            GameTestHelper helper, ServerPlayer player, String path, String description) {
        var advancement = helper.getLevel()
                .getServer()
                .getAdvancements()
                .get(InfiniteX.id("progression/" + path));
        helper.assertTrue(advancement != null, path + " advancement must be loaded");
        helper.assertTrue(
                player.getAdvancements().getOrStartProgress(advancement).isDone(),
                description);
    }

    private static void fillCopperPickaxe(CraftingContainer grid) {
        grid.setItem(0, Items.COPPER_INGOT.getDefaultInstance());
        grid.setItem(1, Items.COPPER_INGOT.getDefaultInstance());
        grid.setItem(2, Items.COPPER_INGOT.getDefaultInstance());
        grid.setItem(4, Items.STICK.getDefaultInstance());
        grid.setItem(7, Items.STICK.getDefaultInstance());
    }

    private static void fillFlintAxe(CraftingContainer grid) {
        grid.setItem(0, Items.FLINT.getDefaultInstance());
        grid.setItem(1, Items.FLINT.getDefaultInstance());
        grid.setItem(3, Items.FLINT.getDefaultInstance());
        grid.setItem(4, Items.STICK.getDefaultInstance());
        grid.setItem(6, ModItems.SINEW.get().getDefaultInstance());
        grid.setItem(7, Items.STICK.getDefaultInstance());
    }

    private static void fillMetalHoe(CraftingContainer grid, Item ingot) {
        grid.setItem(0, ingot.getDefaultInstance());
        grid.setItem(1, ingot.getDefaultInstance());
        grid.setItem(4, Items.STICK.getDefaultInstance());
        grid.setItem(7, Items.STICK.getDefaultInstance());
    }

    private static void fillMetalSword(CraftingContainer grid, Item ingot) {
        grid.setItem(0, ingot.getDefaultInstance());
        grid.setItem(3, ingot.getDefaultInstance());
        grid.setItem(6, Items.STICK.getDefaultInstance());
    }

    private static void fillWarHammer(CraftingContainer grid, Item ingot) {
        for (int slot : List.of(0, 1, 2, 3, 5)) {
            grid.setItem(slot, ingot.getDefaultInstance());
        }
        grid.setItem(4, Items.STICK.getDefaultInstance());
        grid.setItem(7, Items.STICK.getDefaultInstance());
    }

    private static void fillFurnace(CraftingContainer grid) {
        for (int slot : List.of(0, 1, 2, 3, 5, 6, 7, 8)) {
            grid.setItem(slot, Blocks.COBBLESTONE.asItem().getDefaultInstance());
        }
    }

    private static void clearGrid(CraftingContainer grid) {
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            grid.setItem(slot, ItemStack.EMPTY);
        }
    }

    private static Item equipment(R196Material material, R196EquipmentType type) {
        return ModItems.catalog().equipment(material, type).holder().get();
    }

    private static int countItem(Inventory inventory, Item item) {
        return inventory.getNonEquipmentItems().stream()
                .filter(stack -> stack.is(item))
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    private static int countGridItem(CraftingContainer grid, Item item) {
        int count = 0;
        for (ItemStack stack : grid.getItems()) {
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static ItemStack takeItem(GameTestHelper helper, Inventory inventory, Item item, int count) {
        int remaining = count;
        for (ItemStack stack : inventory.getNonEquipmentItems()) {
            if (!stack.is(item)) {
                continue;
            }
            int removed = Math.min(remaining, stack.getCount());
            stack.shrink(removed);
            remaining -= removed;
            if (remaining == 0) {
                break;
            }
        }
        helper.assertTrue(remaining == 0, "expected inventory material was missing: " + item);
        return new ItemStack(item, count);
    }

    private static ResourceKey<Consumer<GameTestHelper>> functionKey(String path) {
        return ResourceKey.create(Registries.TEST_FUNCTION, InfiniteX.id(path));
    }

    private static ResourceKey<Recipe<?>> recipeKey(String namespace, String path) {
        return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(namespace, path));
    }
}
