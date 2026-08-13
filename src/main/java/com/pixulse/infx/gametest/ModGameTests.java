package com.pixulse.infx.gametest;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.mojang.authlib.GameProfile;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.TieredWorkbenchBlock;
import com.pixulse.infx.block.entity.InfxFurnaceBlockEntity;
import com.pixulse.infx.recipe.BenchTier;
import com.pixulse.infx.recipe.CraftingProfile;
import com.pixulse.infx.recipe.InfxCraftingRules;
import com.pixulse.infx.recipe.RecipeRules;
import com.pixulse.infx.recipe.TimedCraftingEngine;
import com.pixulse.infx.recipe.TimedCraftingMenu;
import com.pixulse.infx.item.equipment.QualitySystem;
import com.pixulse.infx.item.InfxBucketItem;
import com.pixulse.infx.data.furnace.FurnaceHeatAccess;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.item.material.Quality;
import com.pixulse.infx.screen.menu.TimedWorkbenchMenu;
import com.pixulse.infx.player.Experience;
import com.pixulse.infx.registry.InfXAttachments;
import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.registry.InfXDataComponents;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.event.server.ExtremeDifficulty;
import com.pixulse.infx.data.food.SurvivalData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.ChatFormatting;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.damagesource.IScalingFunction;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class ModGameTests {
    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger("infx.furnace_click_experience");
    private static final BlockPos WORK_POS = new BlockPos(1, 1, 1);
    private static final BlockPos FURNACE_POS = new BlockPos(3, 1, 1);
    private static final AtomicInteger PLAYER_SEQUENCE = new AtomicInteger();
    private static final List<String> DISABLED_NETHERITE_SMITHING = List.of(
            "netherite_axe_smithing",
            "netherite_hoe_smithing",
            "netherite_pickaxe_smithing",
            "netherite_shovel_smithing",
            "netherite_sword_smithing",
            "netherite_spear_smithing",
            "netherite_helmet_smithing",
            "netherite_chestplate_smithing",
            "netherite_leggings_smithing",
            "netherite_boots_smithing",
            "netherite_nautilus_armor_smithing",
            "netherite_horse_armor_smithing");
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

    private static final ResourceKey<Consumer<GameTestHelper>> BENCH_HIERARCHY =
            functionKey("bench_hierarchy");
    private static final ResourceKey<Consumer<GameTestHelper>> FLINT_WORKBENCH_HAND_CRAFTING =
            functionKey("flint_workbench_hand_crafting");
    private static final ResourceKey<Consumer<GameTestHelper>> TIMED_CRAFTING =
            functionKey("timed_crafting");
    private static final ResourceKey<Consumer<GameTestHelper>> COIN_CRAFTING =
            functionKey("coin_crafting");
    private static final ResourceKey<Consumer<GameTestHelper>> LEATHER_DYEING =
            functionKey("leather_dyeing");
    private static final ResourceKey<Consumer<GameTestHelper>> VANILLA_RECIPE_POLICY =
            functionKey("vanilla_recipe_policy");
    private static final ResourceKey<Consumer<GameTestHelper>> VANILLA_CRAFTING_MENU =
            functionKey("vanilla_crafting_menu");
    private static final ResourceKey<Consumer<GameTestHelper>> VANILLA_DOOR_RECIPES =
            functionKey("vanilla_door_recipes");
    private static final ResourceKey<Consumer<GameTestHelper>> VANILLA_OVERFLOW_RECIPES =
            functionKey("vanilla_overflow_recipes");
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
    private static final ResourceKey<Consumer<GameTestHelper>> FURNACE_CLICK_EXPERIENCE =
            functionKey("furnace_click_experience");
    private static final ResourceKey<Consumer<GameTestHelper>> FURNACE_TIER_RULES =
            functionKey("furnace_tier_rules");
    private static final ResourceKey<Consumer<GameTestHelper>> ADVANCED_FURNACE_RULES =
            functionKey("advanced_furnace_rules");
    private static final ResourceKey<Consumer<GameTestHelper>> EXTREME_DIFFICULTY =
            functionKey("extreme_difficulty");
    private static final ResourceKey<Consumer<GameTestHelper>> HOT_FLOOR =
            functionKey("hot_floor");
    private static final ResourceKey<Consumer<GameTestHelper>> BLOCK_HARDNESS =
            functionKey("block_hardness");

    static {
        TEST_FUNCTIONS.register("bench_hierarchy", () -> ModGameTests::benchHierarchy);
        TEST_FUNCTIONS.register(
                "flint_workbench_hand_crafting", () -> ModGameTests::flintWorkbenchHandCrafting);
        TEST_FUNCTIONS.register("timed_crafting", () -> ModGameTests::timedCrafting);
        TEST_FUNCTIONS.register("coin_crafting", () -> ModGameTests::coinCrafting);
        TEST_FUNCTIONS.register("leather_dyeing", () -> ModGameTests::leatherDyeing);
        TEST_FUNCTIONS.register("vanilla_recipe_policy", () -> ModGameTests::vanillaRecipePolicy);
        TEST_FUNCTIONS.register("vanilla_crafting_menu", () -> ModGameTests::vanillaCraftingMenu);
        TEST_FUNCTIONS.register("vanilla_door_recipes", () -> ModGameTests::vanillaDoorRecipes);
        TEST_FUNCTIONS.register("vanilla_overflow_recipes", () -> ModGameTests::vanillaOverflowRecipes);
        TEST_FUNCTIONS.register("crafting_profiles", () -> ModGameTests::craftingProfiles);
        TEST_FUNCTIONS.register("timed_resets", () -> ModGameTests::timedResets);
        TEST_FUNCTIONS.register("full_inventory_drop", () -> ModGameTests::fullInventoryDrop);
        TEST_FUNCTIONS.register("recipe_boundaries", () -> ModGameTests::recipeBoundaries);
        TEST_FUNCTIONS.register("vanilla_recipe_collisions", () -> ModGameTests::vanillaRecipeCollisions);
        TEST_FUNCTIONS.register("copper_loop", () -> ModGameTests::copperLoop);
        TEST_FUNCTIONS.register("iron_loop", () -> ModGameTests::ironLoop);
        TEST_FUNCTIONS.register("core_tool_recipes", () -> ModGameTests::coreToolRecipes);
        TEST_FUNCTIONS.register("advanced_core_tool_recipes", () -> ModGameTests::advancedCoreToolRecipes);
        TEST_FUNCTIONS.register("special_tool_recipes", () -> ModGameTests::specialToolRecipes);
        TEST_FUNCTIONS.register("weapon_recipes", () -> ModGameTests::weaponRecipes);
        TEST_FUNCTIONS.register("armor_recipes", () -> ModGameTests::armorRecipes);
        TEST_FUNCTIONS.register("furnace_heat_rules", () -> ModGameTests::furnaceHeatRules);
        TEST_FUNCTIONS.register("furnace_click_experience", () -> ModGameTests::furnaceClickExperience);
        TEST_FUNCTIONS.register("furnace_tier_rules", () -> ModGameTests::furnaceTierRules);
        TEST_FUNCTIONS.register("advanced_furnace_rules", () -> ModGameTests::advancedFurnaceRules);
        TEST_FUNCTIONS.register("extreme_difficulty", () -> ModGameTests::extremeDifficulty);
        TEST_FUNCTIONS.register("hot_floor", () -> ModGameTests::hotFloor);
        TEST_FUNCTIONS.register("block_hardness", () -> ModGameTests::blockHardness);
    }

    private ModGameTests() {}

    public static void register(IEventBus modBus) {
        TEST_FUNCTIONS.register(modBus);
    }

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                InfiniteX.id("m1"), new TestEnvironmentDefinition.AllOf());
        registerTest(event, BENCH_HIERARCHY, environment, 80);
        registerTest(event, FLINT_WORKBENCH_HAND_CRAFTING, environment, 300);
        registerTest(event, TIMED_CRAFTING, environment, 200);
        registerTest(event, COIN_CRAFTING, environment, 200);
        registerTest(event, LEATHER_DYEING, environment, 80);
        registerTest(event, VANILLA_RECIPE_POLICY, environment, 40);
        registerTest(event, VANILLA_CRAFTING_MENU, environment, 200);
        registerTest(event, VANILLA_DOOR_RECIPES, environment, 300);
        registerTest(event, VANILLA_OVERFLOW_RECIPES, environment, 600);
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
        registerTest(event, FURNACE_CLICK_EXPERIENCE, environment, 1000);
        registerTest(event, FURNACE_TIER_RULES, environment, 900);
        registerTest(event, ADVANCED_FURNACE_RULES, environment, 600);
        registerTest(event, EXTREME_DIFFICULTY, environment, 40);
        registerTest(event, HOT_FLOOR, environment, 40);
        registerTest(event, BLOCK_HARDNESS, environment, 40);
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
        Difficulty gameplayDifficulty = helper.getLevel().getDifficulty();
        DifficultyInstance regionalDifficulty = helper.getLevel().getCurrentDifficultyAt(helper.absolutePos(WORK_POS));
        DifficultyInstance directlyConstructedExtremeDifficulty = new DifficultyInstance(extreme, 72_000L, 0L, 0.0F);
        DifficultyInstance directlyConstructedHardDifficulty = new DifficultyInstance(Difficulty.HARD, 72_000L, 0L, 0.0F);
        var difficultyCommand = helper.getLevel()
                .getServer()
                .getCommands()
                .getDispatcher()
                .getRoot()
                .getChild("difficulty");
        ServerPlayer player = createPlayer(helper);
        var attacker = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 2, 3));
        float scaledMobDamage = IScalingFunction.DEFAULT.scaleDamage(
                helper.getLevel().damageSources().mobAttack(attacker), player, 4.0F, extreme);
        float scaledExplosionDamage = IScalingFunction.DEFAULT.scaleDamage(
                helper.getLevel().damageSources().explosion(null), player, 4.0F, extreme);

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
        helper.assertTrue(gameplayDifficulty == Difficulty.HARD, "Extreme gameplay values must resolve to Hard");
        helper.assertTrue(regionalDifficulty.getDifficulty() == Difficulty.HARD,
                "regional difficulty must use Hard as its base value");
        helper.assertTrue(directlyConstructedExtremeDifficulty.getDifficulty() == Difficulty.HARD,
                "directly constructed regional difficulty must use Hard as its base value");
        helper.assertTrue(
                Math.abs(directlyConstructedExtremeDifficulty.getEffectiveDifficulty()
                                - directlyConstructedHardDifficulty.getEffectiveDifficulty())
                        < 0.001F,
                "directly constructed regional difficulty must use Hard's effective value");
        helper.assertTrue(Math.abs(scaledMobDamage - 6.0F) < 0.001F,
                "mob damage must use Hard's 1.5x difficulty scaling");
        helper.assertTrue(Math.abs(scaledExplosionDamage - 6.0F) < 0.001F,
                "explosion damage must use Hard's 1.5x difficulty scaling");
        helper.assertTrue(
                difficultyCommand != null && difficultyCommand.getChild("extreme") != null,
                "/difficulty extreme must be registered by the vanilla difficulty command");
        removePlayer(player);
        helper.succeed();
    }

    private static void hotFloor(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        try {
            BlockPos floorPos = helper.absolutePos(WORK_POS);
            for (Block floor : List.of(InfXBlocks.MANTLE.get(), InfXBlocks.CORE.get())) {
                helper.setBlock(WORK_POS, floor);
                player.clearFire();
                floor.stepOn(helper.getLevel(), floorPos, floor.defaultBlockState(), player);
                String floorName = BuiltInRegistries.BLOCK.getKey(floor).getPath();
                helper.assertTrue(player.isOnFire(), floorName + " must ignite entities that step on it");
                helper.assertTrue(
                        player.getRemainingFireTicks() >= 160,
                        floorName + " must apply at least eight seconds of fire");
            }
        } finally {
            removePlayer(player);
        }
        helper.succeed();
    }

    private static void blockHardness(GameTestHelper helper) {
        for (Block block : List.of(Blocks.COBWEB, Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN)) {
            float expected = 8.0F;
            float defaultDestroyTime = block.defaultDestroyTime();
            float destroySpeed = block.defaultBlockState()
                    .getDestroySpeed(helper.getLevel(), helper.absolutePos(WORK_POS));
            String blockName = BuiltInRegistries.BLOCK.getKey(block).getPath();
            helper.assertTrue(
                    defaultDestroyTime == expected,
                    blockName + " default hardness must match InfX obsidian hardness: " + defaultDestroyTime);
            helper.assertTrue(
                    destroySpeed == expected,
                    blockName + " runtime destroy speed must match InfX obsidian hardness: " + destroySpeed);
        }
        helper.succeed();
    }

    private static void benchHierarchy(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);

        TimedCraftingMenu hand = (TimedCraftingMenu) player.inventoryMenu;
        player.containerMenu = player.inventoryMenu;
        hand.infx$craftingContainer().setItem(0, Items.LEATHER.getDefaultInstance());
        helper.assertTrue(TimedCraftingEngine.refreshResult(hand, player, true), "hand recipe must resolve in the 2x2 grid");
        assertResult(helper, hand, InfXItems.SINEW.get(), "hand recipe result");

        clearGrid(hand.infx$craftingContainer());
        helper.setBlock(WORK_POS, flintWorkbench());
        helper.setBlock(WORK_POS.above(), Blocks.STONE);
        helper.assertTrue(
                TieredWorkbenchBlock.isObstructed(helper.getLevel(), helper.absolutePos(WORK_POS)),
                "a full block above the workbench must obstruct it");
        helper.setBlock(WORK_POS.above(), Blocks.AIR);
        helper.assertFalse(
                TieredWorkbenchBlock.isObstructed(helper.getLevel(), helper.absolutePos(WORK_POS)),
                "clearing the block above must clear the obstruction");
        TimedWorkbenchMenu flint = workbenchMenu(player, helper, BenchTier.FLINT, flintWorkbench(), 1);
        player.containerMenu = flint;
        fillCopperPickaxe(flint.infx$craftingContainer());
        helper.assertFalse(TimedCraftingEngine.refreshResult(flint, player, true), "flint bench must reject copper-tier recipes");

        clearGrid(flint.infx$craftingContainer());
        flint.infx$craftingContainer().setItem(0, Items.LEATHER.getDefaultInstance());
        helper.assertTrue(TimedCraftingEngine.refreshResult(flint, player, true), "flint bench must accept hand recipes");
        assertResult(helper, flint, InfXItems.SINEW.get(), "flint bench lower-tier result");

        flint.removed(player);
        helper.setBlock(WORK_POS, InfXBlocks.COPPER_WORKBENCH.get());
        TimedWorkbenchMenu copper = workbenchMenu(player, helper, BenchTier.COPPER, InfXBlocks.COPPER_WORKBENCH.get(), 2);
        player.containerMenu = copper;
        copper.infx$craftingContainer().setItem(0, Items.LEATHER.getDefaultInstance());
        helper.assertTrue(TimedCraftingEngine.refreshResult(copper, player, true), "copper bench must accept hand recipes");

        clearGrid(copper.infx$craftingContainer());
        fillCopperPickaxe(copper.infx$craftingContainer());
        helper.assertTrue(TimedCraftingEngine.refreshResult(copper, player, true), "copper bench must accept copper recipes");
        assertResult(helper, copper, InfXItems.COPPER_PICKAXE.get(), "copper bench result");

        removePlayer(player);
        helper.succeed();
    }

    private static void flintWorkbenchHandCrafting(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        helper.onEachTick(player::doTick);
        InfXBlocks.StrippedLogWorkbenchSet workbench = InfXBlocks.STRIPPED_LOG_WORKBENCHES.getFirst();
        String recipeName = "stripped_" + workbench.wood() + "_flint_workbench";
        RecipeHolder<?> untypedHolder = helper.getLevel()
                .recipeAccess()
                .recipeMap()
                .byKey(recipeKey("infx", recipeName));
        helper.assertTrue(untypedHolder != null, recipeName + " recipe must be loaded");
        RecipeHolder<CraftingRecipe> holder = (RecipeHolder<CraftingRecipe>) untypedHolder;
        CraftingProfile inferred = InfxCraftingRules.displayProfile(holder.value());
        CraftingProfile ruled = RecipeRules.serverDisplayProfile(holder);
        helper.assertTrue(
                inferred.requiredBench() == BenchTier.FLINT,
                "the flint workbench fallback must infer FLINT from its output name");
        helper.assertTrue(
                ruled.requiredBench() == BenchTier.HAND,
                "the explicit recipe rule must keep the flint workbench craftable by hand");
        helper.assertTrue(
                Math.abs(ruled.difficulty() - 270.0F) < 0.001F,
                "the flint workbench rule must retain difficulty 270");

        TimedCraftingMenu hand = (TimedCraftingMenu) player.inventoryMenu;
        player.containerMenu = player.inventoryMenu;
        CraftingContainer grid = hand.infx$craftingContainer();
        grid.setItem(0, Items.FLINT.getDefaultInstance());
        grid.setItem(1, InfXItems.SINEW.get().getDefaultInstance());
        grid.setItem(2, Items.STICK.getDefaultInstance());
        grid.setItem(3, workbench.strippedLog().asItem().getDefaultInstance());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(hand, player, true),
                "the 2x2 inventory grid must resolve the flint workbench recipe");
        assertResult(
                helper,
                hand,
                workbench.flint().get().asItem(),
                "flint workbench hand-crafting preview");

        player.inventoryMenu.clicked(0, 0, ContainerInput.PICKUP, player);
        helper.assertTrue(hand.infx$craftingState().isRunning(), "the flint workbench craft must start its timer");
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(player.getInventory(), workbench.flint().get().asItem()) == 1,
                        "the flint workbench hand craft must complete"))
                .thenExecute(() -> {
                    helper.assertTrue(
                            grid.getItems().stream().allMatch(ItemStack::isEmpty),
                            "the completed flint workbench craft must consume all four inputs");
                    helper.assertFalse(
                            hand.infx$craftingState().isRunning(),
                            "the exhausted flint workbench recipe must stop repeating");
                    removePlayer(player);
                })
                .thenSucceed();
    }

    private static void timedCrafting(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        SurvivalData initialSurvival = player.getData(InfXAttachments.SURVIVAL);
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
        helper.assertTrue(countItem(player.getInventory(), InfXItems.SINEW.get()) == 0, "result click must not craft immediately");
        helper.assertTrue(menu.infx$craftingContainer().getItem(0).getCount() == 1, "input must remain until completion");
        EmbeddedChannel playerChannel = (EmbeddedChannel) player.connection.getConnection().channel();
        while (playerChannel.readOutbound() != null) {}

        int[] pausedProgress = new int[1];
        helper.startSequence()
                .thenExecuteAfter(10, () -> {
                    helper.assertTrue(countItem(player.getInventory(), InfXItems.SINEW.get()) == 0, "result must still be delayed");
                    player.setData(
                            InfXAttachments.SURVIVAL,
                            new SurvivalData(
                                    0.0D,
                                    0.0D,
                                    initialSurvival.protein(),
                                    initialSurvival.phytonutrients(),
                                    initialSurvival.essentialFats(),
                                    initialSurvival.insulinResponse(),
                                    initialSurvival.recoveryProgress(),
                                    initialSurvival.hungerProgress(),
                                    initialSurvival.nutritionHungerProgress(),
                                    initialSurvival.starvationProgress()));
                    pausedProgress[0] = menu.infx$craftingState().progressTicks();
                })
                .thenExecuteAfter(10, () -> {
                    helper.assertTrue(
                            menu.infx$craftingState().progressTicks() == pausedProgress[0],
                            "zero hunger must pause without losing progress");
                    player.setData(InfXAttachments.SURVIVAL, initialSurvival);
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(player.getInventory(), InfXItems.SINEW.get()) == 4,
                        "completion must place four sinew in the inventory"))
                .thenExecute(() -> {
                    playerChannel.runPendingTasks();
                    playerChannel.flushOutbound();
                    boolean receivedInventoryUpdate = false;
                    Object outbound;
                    while ((outbound = playerChannel.readOutbound()) != null) {
                        if (containsInventoryUpdate(outbound, InfXItems.SINEW.get(), 4)) {
                            receivedInventoryUpdate = true;
                        }
                    }
                    helper.assertTrue(
                            receivedInventoryUpdate,
                            "completion must synchronize the crafted result to the client inventory");
                    helper.assertTrue(menu.infx$craftingContainer().getItem(0).isEmpty(), "completion must consume leather");
                    helper.assertFalse(menu.infx$craftingState().isRunning(), "exhausted ingredients must stop repetition");
                    removePlayer(player);
                })
                .thenSucceed();
    }

    private static void leatherDyeing(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        TimedCraftingMenu menu = (TimedCraftingMenu) player.inventoryMenu;
        player.containerMenu = player.inventoryMenu;

        Item chestplate = equipment(InfxMaterial.LEATHER, EquipmentType.CHESTPLATE);
        ItemStack undyed = chestplate.getDefaultInstance();
        helper.assertFalse(undyed.has(DataComponents.DYED_COLOR), "fresh leather armor must be undyed");
        helper.assertTrue(
                undyed.is(ItemTags.CAULDRON_CAN_REMOVE_DYE),
                "leather armor must be washable in a water cauldron");

        menu.infx$craftingContainer().setItem(0, chestplate.getDefaultInstance());
        menu.infx$craftingContainer().setItem(1, Items.RED_DYE.getDefaultInstance());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(menu, player, true),
                "leather dye recipe must resolve in the 2x2 grid");
        ItemStack preview = menu.infx$resultContainer().getItem(0);
        helper.assertTrue(preview.is(chestplate), "dye preview must keep the leather armor piece");
        DyedItemColor expected = DyedItemColor.applyDyes((DyedItemColor) null, List.of(DyeColor.RED));
        helper.assertTrue(
                expected.equals(preview.get(DataComponents.DYED_COLOR)),
                "dye preview must carry the blended dye color");

        // Re-dyeing an already dyed piece blends with its current color.
        menu.infx$craftingContainer().setItem(0, preview.copy());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(menu, player, true), "re-dyeing a dyed piece must resolve");
        ItemStack reDyed = menu.infx$resultContainer().getItem(0);
        helper.assertTrue(
                DyedItemColor.applyDyes(expected, List.of(DyeColor.RED))
                        .equals(reDyed.get(DataComponents.DYED_COLOR)),
                "re-dyeing must blend with the existing color");

        removePlayer(player);
        helper.succeed();
    }

    private static void coinCrafting(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        helper.onEachTick(player::doTick);
        TimedCraftingMenu menu = (TimedCraftingMenu) player.inventoryMenu;
        player.containerMenu = player.inventoryMenu;
        int[] coinExperienceBefore = new int[1];
        menu.infx$craftingContainer().setItem(
                0, InfXItems.catalog().raw("copper_coin").holder().toStack());
        int experienceBefore = player.totalExperience;
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(menu, player, true),
                "a copper coin must produce a copper nugget preview");
        helper.assertTrue(
                menu.infx$resultContainer().getItem(0).is(Items.COPPER_NUGGET),
                "the coin recipe must produce a copper nugget");
        helper.assertTrue(menu.infx$experienceCost() == 0, "breaking a coin into a nugget must show no XP cost");
        player.inventoryMenu.clicked(0, 0, ContainerInput.PICKUP, player);
        helper.assertTrue(menu.infx$craftingState().isRunning(), "coin crafting must start its timer");

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(player.getInventory(), Items.COPPER_NUGGET) == 1,
                        "crafting a copper coin must produce one copper nugget"))
                .thenExecute(() -> {
                    helper.assertTrue(
                            player.totalExperience == experienceBefore + 5,
                            "crafting a copper coin must return its five XP");
                    helper.assertTrue(
                            menu.infx$craftingContainer().getItem(0).isEmpty(),
                            "coin crafting must consume one coin");
                    Item copperCoin = InfXItems.catalog().raw("copper_coin").holder().value();
                    menu.infx$craftingContainer().setItem(0, Items.COPPER_NUGGET.getDefaultInstance());
                    helper.assertTrue(
                            TimedCraftingEngine.refreshResult(menu, player, true),
                            "a copper nugget must produce a copper coin preview");
                    helper.assertTrue(
                            menu.infx$resultContainer().getItem(0).is(copperCoin),
                            "the nugget recipe must produce a copper coin");
                    helper.assertTrue(menu.infx$experienceCost() == 5, "coin crafting must expose its XP cost");
                    coinExperienceBefore[0] = player.totalExperience;
                    player.inventoryMenu.clicked(0, 0, ContainerInput.PICKUP, player);
                    helper.assertTrue(menu.infx$craftingState().isRunning(), "coin production must start its timer");
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(
                                player.getInventory(),
                                InfXItems.catalog().raw("copper_coin").holder().value()) == 1,
                        "crafting a nugget into a coin must produce one coin"))
                .thenExecute(() -> {
                    helper.assertTrue(
                            player.totalExperience == coinExperienceBefore[0] - 5,
                            "crafting a copper coin must deduct its five XP");
                    removePlayer(player);
                })
                .thenSucceed();
    }

    private static boolean containsInventoryUpdate(Object outbound, Item item, int count) {
        if (outbound instanceof ClientboundSetPlayerInventoryPacket packet) {
            return packet.contents().is(item) && packet.contents().getCount() == count;
        }
        if (outbound instanceof BundlePacket<?> bundle) {
            for (Object subPacket : bundle.subPackets()) {
                if (containsInventoryUpdate(subPacket, item, count)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void vanillaRecipePolicy(GameTestHelper helper) {
        var recipeMap = helper.getLevel().recipeAccess().recipeMap();
        List<String> vanillaCrafting = recipeMap.byType(RecipeType.CRAFTING).stream()
                .map(holder -> holder.id().identifier())
                .filter(id -> "minecraft".equals(id.getNamespace()))
                .map(Identifier::toString)
                .sorted()
                .toList();
        helper.assertTrue(
                vanillaCrafting.size() >= 1_000,
                "the unrelated vanilla crafting set must remain loaded; found only " + vanillaCrafting.size());
        for (String path : List.of(
                "oak_planks",
                "stick",
                "spyglass",
                "turtle_helmet",
                "wolf_armor",
                "leather_horse_armor")) {
            helper.assertTrue(
                    recipeMap.byKey(recipeKey("minecraft", path)) != null,
                    "minecraft:" + path + " must remain available");
        }
        for (String path : List.of(
                "iron_pickaxe",
                "bow",
                "repair_item",
                "shield_decoration",
                "tipped_arrow",
                "leather_helmet",
                "leather_horse_armor_dyed",
                "wolf_armor_dyed")) {
            helper.assertTrue(
                    recipeMap.byKey(recipeKey("minecraft", path)) == null,
                    "minecraft:" + path + " must be disabled");
        }
        for (String path : DISABLED_NETHERITE_SMITHING) {
            helper.assertTrue(
                    recipeMap.byKey(recipeKey("minecraft", path)) == null,
                    "minecraft:" + path + " must be disabled");
        }
        helper.assertTrue(
                recipeMap.byKey(recipeKey("infx", "flint_knife")) != null,
                "INFX crafting recipes must remain loaded");
        helper.assertTrue(
                recipeMap.byType(RecipeType.SMELTING).stream()
                        .anyMatch(holder -> holder.id().identifier().getNamespace().equals("minecraft")),
                "vanilla non-crafting recipes must remain loaded");
        helper.succeed();
    }

    private static void vanillaCraftingMenu(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        helper.onEachTick(player::doTick);
        int qualityCost = QualitySystem.experienceCost(150.0F, Quality.FINE);
        int[] experienceBeforeQualityCraft = new int[1];
        helper.setBlock(WORK_POS, Blocks.CRAFTING_TABLE);
        player.openMenu(helper.getBlockState(WORK_POS)
                .getMenuProvider(helper.getLevel(), helper.absolutePos(WORK_POS)));
        helper.assertTrue(
                player.containerMenu instanceof CraftingMenu,
                "opening a vanilla crafting table must create its standard crafting menu");
        CraftingMenu vanilla = (CraftingMenu) player.containerMenu;
        helper.assertTrue(vanilla instanceof TimedCraftingMenu, "the vanilla crafting menu must receive the timed mixin");
        TimedCraftingMenu timed = (TimedCraftingMenu) vanilla;
        CraftingContainer grid = timed.infx$craftingContainer();

        // Use a restored vanilla recipe (oak log -> oak planks) away from the
        // top-left corner so the vanilla menu mixin is exercised through the
        // timed engine with a real vanilla recipe.
        grid.setItem(4, Items.OAK_LOG.getDefaultInstance());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(timed, player, true),
                "the vanilla 3x3 menu must resolve restored vanilla recipes through the timed engine");
        assertResult(helper, timed, Items.OAK_PLANKS, "vanilla plank timed preview");
        vanilla.clicked(0, 0, ContainerInput.PICKUP, player);
        helper.assertTrue(timed.infx$craftingState().isRunning(), "crafting-table result must start a timer");

        helper.startSequence()
                .thenExecuteAfter(5, () -> {
                    helper.assertTrue(
                            player.containerMenu == vanilla,
                            "the vanilla crafting-table menu must remain open while its timer runs");
                    helper.assertTrue(
                            vanilla.stillValid(player),
                            "the crafting-table context must remain valid while its timer runs");
                    helper.assertTrue(
                            timed.infx$craftingState().isRunning(),
                            "the vanilla crafting-table timer must remain active before completion");
                    helper.assertTrue(
                            timed.infx$craftingState().progressTicks() > 0,
                            "the vanilla crafting-table timer must receive player ticks");
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(player.getInventory(), Items.OAK_PLANKS) == 4,
                        "the vanilla crafting-table timer must complete"))
                .thenExecute(() -> {
                    helper.assertTrue(grid.getItem(4).isEmpty(), "offset log slot must be consumed");
                    grantMaximumExperience(player);
                    grid.setItem(0, Items.FLINT.getDefaultInstance());
                    grid.setItem(1, Items.STRING.getDefaultInstance());
                    grid.setItem(3, Items.STICK.getDefaultInstance());
                    helper.assertTrue(
                            TimedCraftingEngine.refreshResult(timed, player, true),
                            "the crafting table must preview the flint knife");
                    vanilla.clicked(0, 1, ContainerInput.PICKUP, player);
                    ItemStack qualityPreview = timed.infx$resultContainer().getItem(0);
                    helper.assertTrue(
                            qualityPreview.is(equipment(InfxMaterial.FLINT, EquipmentType.KNIFE)),
                            "right-click must keep the tool crafting result");
                    helper.assertTrue(
                            qualityPreview.get(InfXDataComponents.QUALITY.get()) == Quality.FINE,
                            "enough experience must select Fine Quality on a crafting table");
                    helper.assertTrue(vanilla.getCarried().isEmpty(), "right-click must not take the quality preview");
                    experienceBeforeQualityCraft[0] = player.totalExperience;
                    vanilla.clicked(0, 0, ContainerInput.PICKUP, player);
                    helper.assertTrue(timed.infx$craftingState().isRunning(), "quality tool crafting must start its timer");
                })
                .thenWaitUntil(() -> {
                    ItemStack craftedKnife = player.getInventory().getNonEquipmentItems().stream()
                            .filter(stack -> stack.is(equipment(InfxMaterial.FLINT, EquipmentType.KNIFE)))
                            .findFirst()
                            .orElse(ItemStack.EMPTY);
                    helper.assertTrue(
                            craftedKnife.get(InfXDataComponents.QUALITY.get()) == Quality.FINE,
                            "the completed tool must retain the selected quality");
                    helper.assertTrue(
                            player.totalExperience == experienceBeforeQualityCraft[0] - qualityCost,
                            "quality crafting must deduct its exact experience cost");
                })
                .thenExecute(() -> {
                    Experience.setTotal(player, -20);
                    clearGrid(grid);
                    grid.setItem(0, Items.FLINT.getDefaultInstance());
                    grid.setItem(1, Items.STRING.getDefaultInstance());
                    grid.setItem(3, Items.STICK.getDefaultInstance());
                    timed.infx$setSelectedQualityCode(QualitySystem.toCode(Quality.FINE));
                    helper.assertTrue(
                            TimedCraftingEngine.refreshResult(timed, player, true),
                            "negative-level players must still receive a tool preview");
                    Quality negativeQuality = timed.infx$resultContainer().getItem(0)
                            .get(InfXDataComponents.QUALITY.get());
                    helper.assertTrue(
                            negativeQuality == Quality.WRETCHED || negativeQuality == Quality.POOR,
                            "negative levels must force Wretched or Poor quality");
                    helper.assertTrue(timed.infx$experienceCost() == 0, "forced bad quality must cost no XP");

                    Experience.setTotal(player, 1_000);
                    timed.infx$setSelectedQualityCode(QualitySystem.toCode(Quality.POOR));
                    helper.assertTrue(
                            TimedCraftingEngine.refreshResult(timed, player, true),
                            "positive-level players must keep a normal tool preview");
                    helper.assertTrue(
                            timed.infx$resultContainer().getItem(0).get(InfXDataComponents.QUALITY.get()) == null,
                            "positive-level players without the witch curse cannot craft Poor tools");
                    removePlayer(player);
                })
                .thenSucceed();
    }

    private static void vanillaDoorRecipes(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        helper.onEachTick(player::doTick);
        grantMaximumExperience(player);
        helper.setBlock(WORK_POS, InfXBlocks.ADAMANTIUM_WORKBENCH.get());
        TimedWorkbenchMenu menu = workbenchMenu(
                player,
                helper,
                BenchTier.ADAMANTIUM,
                InfXBlocks.ADAMANTIUM_WORKBENCH.get(),
                9);
        player.containerMenu = menu;
        CraftingContainer grid = menu.infx$craftingContainer();

        Map<Item, Item> doors = Map.ofEntries(
                Map.entry(Items.ACACIA_PLANKS, Items.ACACIA_DOOR),
                Map.entry(Items.BAMBOO_PLANKS, Items.BAMBOO_DOOR),
                Map.entry(Items.BIRCH_PLANKS, Items.BIRCH_DOOR),
                Map.entry(Items.CHERRY_PLANKS, Items.CHERRY_DOOR),
                Map.entry(Items.COPPER_INGOT, Items.COPPER_DOOR),
                Map.entry(Items.CRIMSON_PLANKS, Items.CRIMSON_DOOR),
                Map.entry(Items.DARK_OAK_PLANKS, Items.DARK_OAK_DOOR),
                Map.entry(Items.IRON_INGOT, Items.IRON_DOOR),
                Map.entry(Items.JUNGLE_PLANKS, Items.JUNGLE_DOOR),
                Map.entry(Items.MANGROVE_PLANKS, Items.MANGROVE_DOOR),
                Map.entry(Items.OAK_PLANKS, Items.OAK_DOOR),
                Map.entry(Items.PALE_OAK_PLANKS, Items.PALE_OAK_DOOR),
                Map.entry(Items.SPRUCE_PLANKS, Items.SPRUCE_DOOR),
                Map.entry(Items.WARPED_PLANKS, Items.WARPED_DOOR));
        for (var entry : doors.entrySet()) {
            clearGrid(grid);
            fillDoor(grid, entry.getKey());
            helper.assertTrue(
                    TimedCraftingEngine.refreshResult(menu, player, true),
                    entry.getValue() + " recipe must resolve");
            ItemStack preview = menu.infx$resultContainer().getItem(0);
            helper.assertTrue(
                    preview.is(entry.getValue()) && preview.getCount() == 1,
                    entry.getValue() + " recipe must preview one door; actual=" + preview);
        }

        clearGrid(grid);
        fillDoor(grid, Items.OAK_PLANKS);
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(menu, player, true),
                "oak door recipe must resolve before timed completion");
        menu.clicked(0, 0, ContainerInput.PICKUP, player);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(player.getInventory(), Items.OAK_DOOR) == 1,
                        "the overridden vanilla door recipe must finish with one door"))
                .thenExecute(() -> {
                    helper.assertTrue(
                            countGridItem(grid, Items.OAK_PLANKS) == 0,
                            "door crafting must consume all six planks");
                    removePlayer(player);
                })
                .thenSucceed();
    }

    private static void vanillaOverflowRecipes(GameTestHelper helper) {
        var recipes = helper.getLevel().recipeAccess().recipeMap().byType(RecipeType.CRAFTING);
        int overflowRecipes = 0;
        for (RecipeHolder<CraftingRecipe> holder : recipes) {
            if (!holder.id().identifier().getNamespace().equals("minecraft")) {
                continue;
            }
            if (holder.value().display().stream().anyMatch(ModGameTests::hasOverflowResult)) {
                overflowRecipes++;
            }
        }
        helper.assertTrue(
                overflowRecipes == 23,
                "all 23 remaining vanilla overflow recipe groups must be discoverable; found " + overflowRecipes);

        ServerPlayer player = createPlayer(helper);
        helper.onEachTick(player::doTick);
        grantMaximumExperience(player);
        helper.setBlock(WORK_POS, InfXBlocks.ADAMANTIUM_WORKBENCH.get());
        TimedWorkbenchMenu menu = workbenchMenu(
                player,
                helper,
                BenchTier.ADAMANTIUM,
                InfXBlocks.ADAMANTIUM_WORKBENCH.get(),
                11);
        player.containerMenu = menu;
        CraftingContainer grid = menu.infx$craftingContainer();

        clearGrid(grid);
        fillSpyglass(grid);
        assertDirectPreview(helper, menu, player, Items.SPYGLASS, 1, "spyglass");

        clearGrid(grid);
        fillRail(grid);
        assertOverflowPreview(helper, menu, player, Items.RAIL, 16, "rail");

        clearGrid(grid);
        fillScaffolding(grid);
        assertOverflowPreview(helper, menu, player, Items.SCAFFOLDING, 6, "scaffolding");

        clearGrid(grid);
        fillShelf(grid);
        assertDirectPreview(helper, menu, player, Items.OAK_SHELF, 6, "oak shelf");

        clearGrid(grid);
        fillConcretePowder(grid);
        assertDirectPreview(helper, menu, player, Items.WHITE_CONCRETE_POWDER, 8, "white concrete powder");

        clearGrid(grid);
        fillStainedGlass(grid);
        assertDirectPreview(helper, menu, player, Items.WHITE_STAINED_GLASS, 8, "white stained glass");

        clearGrid(grid);
        fillStainedTerracotta(grid);
        assertOverflowPreview(helper, menu, player, Items.WHITE_TERRACOTTA, 8, "white stained terracotta");

        clearGrid(grid);
        grid.setItem(0, Items.COPPER_BLOCK.getDefaultInstance());
        assertOverflowPreview(helper, menu, player, Items.COPPER_INGOT, 9, "copper block to ingots");

        clearGrid(grid);
        fillRail(grid);
        helper.assertTrue(TimedCraftingEngine.refreshResult(menu, player, true), "rail completion must resolve");
        menu.clicked(0, 0, ContainerInput.PICKUP, player);
        ServerPlayer[] fullInventoryPlayer = new ServerPlayer[1];
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(player.getInventory(), Items.RAIL) == 16,
                        "rail completion must retain the original 16-item yield"))
                .thenExecute(() -> {
                    int railStacks = 0;
                    for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
                        if (stack.is(Items.RAIL)) {
                            railStacks++;
                            helper.assertTrue(
                                    stack.getCount() <= stack.getMaxStackSize(),
                                    "every delivered rail stack must respect the InfX maximum");
                        }
                    }
                    helper.assertTrue(railStacks == 2, "16 rails must arrive as two legal stacks");
                    removePlayer(player);
                })
                .thenExecute(() -> {
                    ServerPlayer full = createPlayer(helper);
                    fullInventoryPlayer[0] = full;
                    helper.onEachTick(full::doTick);
                    grantMaximumExperience(full);
                    for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
                        int fillerCount = Items.COBBLESTONE.getDefaultInstance().getMaxStackSize();
                        full.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, fillerCount));
                    }
                    TimedWorkbenchMenu dropMenu = workbenchMenu(
                            full,
                            helper,
                            BenchTier.ADAMANTIUM,
                            InfXBlocks.ADAMANTIUM_WORKBENCH.get(),
                            12);
                    full.containerMenu = dropMenu;
                    fillRail(dropMenu.infx$craftingContainer());
                    helper.assertTrue(
                            TimedCraftingEngine.refreshResult(dropMenu, full, true),
                            "full inventory rail recipe must resolve");
                    dropMenu.clicked(0, 0, ContainerInput.PICKUP, full);
                })
                .thenWaitUntil(() -> {
                    ServerPlayer full = fullInventoryPlayer[0];
                    var nearbyItems = helper.getLevel().getEntities(
                            EntityType.ITEM, full.getBoundingBox().inflate(4.0), Entity::isAlive);
                    helper.assertTrue(
                            nearbyItems.stream().anyMatch(entity -> entity.getItem().is(Items.RAIL)),
                            "a full inventory must drop every overflow rail stack");
                    for (var entity : nearbyItems) {
                        if (entity.getItem().is(Items.RAIL)) {
                            helper.assertTrue(
                                    entity.getItem().getCount() <= entity.getItem().getMaxStackSize(),
                                    "dropped overflow rails must remain legal stacks");
                        }
                    }
                })
                .thenExecute(() -> removePlayer(fullInventoryPlayer[0]))
                .thenSucceed();
    }

    private static boolean hasOverflowResult(RecipeDisplay display) {
        if (display.result() instanceof SlotDisplay.ItemStackSlotDisplay itemStackDisplay) {
            ItemStackTemplate template = itemStackDisplay.stack();
            ItemStack single = template.apply(1, net.minecraft.core.component.DataComponentPatch.EMPTY);
            return !single.isEmpty() && template.count() > single.getMaxStackSize();
        }
        return false;
    }

    private static void assertOverflowPreview(
            GameTestHelper helper,
            TimedCraftingMenu menu,
            ServerPlayer player,
            Item expected,
            int logicalCount,
            String description) {
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(menu, player, true),
                description + " must resolve through the timed engine");
        ItemStack preview = menu.infx$resultContainer().getItem(0);
        helper.assertTrue(preview.is(expected), description + " must produce " + expected + "; actual=" + preview);
        helper.assertTrue(
                preview.getCount() == preview.getMaxStackSize(),
                description + " must cap the physical preview at the item maximum; actual=" + preview);
        helper.assertTrue(
                menu.infx$logicalResultCount() == logicalCount,
                description + " must retain the logical vanilla count " + logicalCount
                        + "; actual=" + menu.infx$logicalResultCount());
        helper.assertTrue(
                preview.getCount() <= preview.getMaxStackSize(),
                description + " must never expose an oversized ItemStack");
    }

    private static void assertDirectPreview(
            GameTestHelper helper,
            TimedCraftingMenu menu,
            ServerPlayer player,
            Item expected,
            int count,
            String description) {
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(menu, player, true),
                description + " must resolve through the timed engine");
        ItemStack preview = menu.infx$resultContainer().getItem(0);
        helper.assertTrue(preview.is(expected), description + " must produce " + expected + "; actual=" + preview);
        helper.assertTrue(
                preview.getCount() == count,
                description + " must preserve its vanilla result count; actual=" + preview);
        helper.assertTrue(
                menu.infx$logicalResultCount() == count,
                description + " must retain the vanilla logical result count " + count
                        + "; actual=" + menu.infx$logicalResultCount());
        helper.assertTrue(
                preview.getCount() <= preview.getMaxStackSize(),
                description + " must expose a legal ItemStack");
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
                InfXItems.catalog().raw("rusted_iron_chain").holder().get(),
                400.0F * 4.0F / 9.0F);

        var recipeMap = helper.getLevel().recipeAccess().recipeMap();
        var loadedCraftingRecipes = recipeMap.byType(RecipeType.CRAFTING);
        helper.assertTrue(
                loadedCraftingRecipes.stream()
                        .anyMatch(holder -> holder.id().identifier().getNamespace().equals("minecraft")),
                "vanilla crafting recipes must be restored for inference");
        for (var holder : loadedCraftingRecipes) {
            try {
                CraftingProfile profile = RecipeRules.serverDisplayProfile(holder);
                helper.assertTrue(
                        Float.isFinite(profile.difficulty()) && profile.difficulty() > 0.0F,
                        holder.id().identifier() + " must have a finite positive difficulty");
                helper.assertTrue(
                        BenchTier.ADAMANTIUM.supports(profile.requiredBench()),
                        holder.id().identifier() + " must map to a supported workbench tier");
            } catch (RuntimeException error) {
                helper.fail("failed to resolve crafting profile for " + holder.id().identifier() + ": " + error);
            }
        }

        // Explicit recipe rules override the inferred values for INFX recipes.
        var copperPickaxe = recipeMap.byKey(recipeKey("infx", "copper_pickaxe"));
        helper.assertTrue(copperPickaxe != null, "the INFX copper pickaxe recipe must be loaded");
        CraftingProfile ruleProfile = RecipeRules.serverDisplayProfile(
                (RecipeHolder<CraftingRecipe>) copperPickaxe);
        helper.assertTrue(ruleProfile.requiredBench() == BenchTier.COPPER,
                "recipe rules must raise the copper pickaxe to the copper bench");
        helper.assertTrue(
                Math.abs(ruleProfile.difficulty() - 1250.0F) < 0.001F,
                "recipe rules must keep the copper pickaxe difficulty at 1250");
        var leatherToSinew = recipeMap.byKey(recipeKey("infx", "leather_to_sinew"));
        helper.assertTrue(leatherToSinew != null, "the INFX sinew recipe must be loaded");
        helper.assertTrue(
                RecipeRules.serverDisplayProfile((RecipeHolder<CraftingRecipe>) leatherToSinew).requiredBench()
                        == BenchTier.HAND,
                "recipe rules must keep hand-tier recipes on the hand tier");

        // InfX: baking potatoes in a furnace grants no experience; the vanilla
        // smelting recipe is overridden with a zero reward.
        var bakedPotato = recipeMap.byKey(recipeKey("minecraft", "baked_potato"));
        helper.assertTrue(bakedPotato != null, "the baked potato smelting recipe must be loaded");
        helper.assertTrue(
                ((SmeltingRecipe) bakedPotato.value()).experience() == 0.0F,
                "baking potatoes must not grant furnace experience");
        helper.succeed();
    }

    /**
     * Restored vanilla recipes share grids with INFX recipes for items that
     * have both (buckets, armor, anvils, chains, snow). The INFX recipe must
     * win such ties because it carries an explicit recipe rule.
     */
    private static void vanillaRecipeCollisions(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        helper.setBlock(WORK_POS, flintWorkbench());
        TimedWorkbenchMenu flint = workbenchMenu(player, helper, BenchTier.FLINT, flintWorkbench(), 1);
        player.containerMenu = flint;

        CraftingContainer grid = flint.infx$craftingContainer();
        grid.setItem(0, Items.SNOWBALL.getDefaultInstance());
        grid.setItem(1, Items.SNOWBALL.getDefaultInstance());
        grid.setItem(3, Items.SNOWBALL.getDefaultInstance());
        grid.setItem(4, Items.SNOWBALL.getDefaultInstance());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(flint, player, true),
                "four snowballs must resolve to the ruled INFX snow slab");
        assertResult(helper, flint, InfXBlocks.SNOW_SLAB.get().asItem(), "ruled snow slab must beat vanilla snow block");

        clearGrid(grid);
        grid.setItem(0, Items.IRON_INGOT.getDefaultInstance());
        grid.setItem(2, Items.IRON_INGOT.getDefaultInstance());
        grid.setItem(4, Items.IRON_INGOT.getDefaultInstance());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(flint, player, true),
                "three iron ingots must resolve to the ruled INFX iron bucket");
        assertResult(
                helper,
                flint,
                InfXItems.bucket(InfxMaterial.IRON, InfxBucketItem.Contents.EMPTY).asItem(),
                "ruled INFX bucket must beat the vanilla bucket");

        flint.removed(player);
        removePlayer(player);
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
                            EntityType.ITEM, player.getBoundingBox().inflate(4.0), Entity::isAlive);
                    helper.assertTrue(
                            nearbyItems.stream().anyMatch(entity -> entity.getItem().is(InfXItems.SINEW.get())),
                            "a full inventory must drop the crafted result at the player; progress="
                                    + menu.infx$craftingState().progressTicks()
                                    + "/"
                                    + menu.infx$craftingState().requiredTicks()
                                    + ", running="
                                    + menu.infx$craftingState().isRunning()
                                    + ", inventorySinew="
                                    + countItem(player.getInventory(), InfXItems.SINEW.get())
                                    + ", nearby="
                                    + nearbyItems.stream().map(entity -> entity.getItem().toString()).toList());
                })
                .thenExecute(() -> {
                    helper.assertTrue(countItem(player.getInventory(), InfXItems.SINEW.get()) == 0, "full inventory must not retain the result");
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
                    assertResult(helper, menu, InfXItems.FLINT_CHIP.get(), "changed recipe preview");
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
        helper.assertTrue(
                recipes.byKey(recipeKey("minecraft", "crafting_table")) != null,
                "the vanilla crafting table recipe must be restored");
        helper.assertTrue(
                recipes.byKey(recipeKey("minecraft", "oak_planks")) != null,
                "the vanilla oak planks recipe must be restored");
        helper.assertTrue(
                recipes.byKey(recipeKey("minecraft", "stick")) != null,
                "the vanilla stick recipe must be restored");
        helper.assertTrue(
                recipes.byKey(recipeKey("infx", "oak_planks")) == null,
                "the INFX oak planks duplicate must be gone");
        for (String material : List.of("silver", "ancient_metal", "mithril", "adamantium")) {
            helper.assertTrue(
                    recipes.byKey(recipeKey("infx", material + "_ingot_from_nuggets")) != null,
                    "InfiniteX ingot conversion must exist: " + material);
            helper.assertTrue(
                    recipes.byKey(recipeKey("infx", material + "_nuggets_from_ingot")) != null,
                    "InfiniteX nugget conversion must exist: " + material);
        }
        for (String material : List.of("copper", "gold", "iron")) {
            helper.assertTrue(
                    recipes.byKey(recipeKey("minecraft", material + "_ingot_from_nuggets")) != null,
                    "vanilla ingot conversion must be restored: " + material);
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
        for (String material : ARROW_MATERIALS) {
            String path = material + "_arrow_dismantling";
            helper.assertTrue(
                    recipes.byKey(recipeKey("infx", path)) != null,
                    "InfiniteX arrow dismantling recipe must exist: " + path);
        }
        for (String material : ARROW_MATERIALS) {
            for (String path : List.of(
                    material + "_fishing_rod",
                    material + "_carrot_on_a_stick",
                    material + "_carrot_on_a_stick_dismantling")) {
                helper.assertTrue(
                        recipes.byKey(recipeKey("infx", path)) != null,
                        "InfiniteX fishing recipe must exist: " + path);
            }
        }
        for (String material : METAL_MATERIALS) {
            var recipe = recipes.byKey(recipeKey("infx", material + "_anvil"));
            helper.assertTrue(recipe != null, "InfiniteX anvil recipe must exist: " + material);
            if (recipe != null && recipe.value() instanceof ShapedRecipe shaped) {
                List<Optional<net.minecraft.world.item.crafting.Ingredient>> grid =
                        shaped.pattern.ingredients();
                helper.assertTrue(
                        grid.size() == 9
                                && grid.get(3).isEmpty()
                                && grid.get(4).isPresent()
                                && grid.get(5).isEmpty()
                                && grid.get(6).isPresent()
                                && grid.get(7).isPresent()
                                && grid.get(8).isPresent(),
                        "InfX anvil shape must place one centered ingot above a full ingot base: "
                                + material);
            }
        }
        helper.assertTrue(
                recipes.byKey(recipeKey("infx", "glass_bottle")) == null,
                "the INFX glass bottle duplicate must be gone (vanilla restored)");
        for (String removed : List.of("sandstone_to_glass", "red_sandstone_to_glass")) {
            helper.assertTrue(
                    recipes.byKey(recipeKey("infx", removed)) == null,
                    "Sandstone must not smelt into glass: " + removed);
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
        helper.assertTrue(
                recipes.byKey(recipeKey("minecraft", "furnace")) != null,
                "the vanilla furnace recipe must be restored");
        helper.assertTrue(
                recipes.byKey(recipeKey("infx", "cobblestone_furnace")) == null,
                "the INFX cobblestone furnace duplicate must be gone");
        helper.assertTrue(
                recipes.byKey(recipeKey("minecraft", "iron_ingot_from_smelting_raw_iron")) != null,
                "raw iron must retain its furnace recipe");
        helper.succeed();
    }

    private static void copperLoop(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        helper.onEachTick(player::doTick);
        grantMaximumExperience(player);
        helper.setBlock(WORK_POS, flintWorkbench());

        TimedWorkbenchMenu flint = workbenchMenu(player, helper, BenchTier.FLINT, flintWorkbench(), 1);
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
                        countItem(player.getInventory(), InfXItems.COPPER_WORKBENCH.get()) == 1,
                        "one ingot must craft the copper workbench"))
                .thenExecute(() -> {
                    takeItem(helper, player.getInventory(), InfXItems.COPPER_WORKBENCH.get(), 1);
                    player.closeContainer();
                    helper.setBlock(WORK_POS, InfXBlocks.COPPER_WORKBENCH.get());

                    TimedWorkbenchMenu copper = workbenchMenu(
                            player, helper, BenchTier.COPPER, InfXBlocks.COPPER_WORKBENCH.get(), 2);
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
                        countItem(player.getInventory(), InfXItems.COPPER_PICKAXE.get()) == 1,
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
        helper.setBlock(WORK_POS, InfXBlocks.COPPER_WORKBENCH.get());

        TimedWorkbenchMenu copper = workbenchMenu(
                player, helper, BenchTier.COPPER, InfXBlocks.COPPER_WORKBENCH.get(), 3);
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
                    var ironAge = helper.getLevel()
                            .getServer()
                            .getAdvancements()
                            .get(InfiniteX.id("progression/iron_age"));
                    helper.assertTrue(ironAge != null, "iron age advancement must be loaded");
                    helper.assertTrue(
                            player.getAdvancements().getOrStartProgress(ironAge).isDone(),
                            "taking furnace output must grant iron age");
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
                        countItem(player.getInventory(), InfXItems.IRON_WORKBENCH.get()) == 1,
                        "copper workbench must finish the iron workbench"))
                .thenExecute(() -> {
                    takeItem(helper, player.getInventory(), InfXItems.IRON_WORKBENCH.get(), 1);
                    player.closeContainer();
                    helper.setBlock(WORK_POS, InfXBlocks.IRON_WORKBENCH.get());

                    TimedWorkbenchMenu iron = workbenchMenu(
                            player, helper, BenchTier.IRON, InfXBlocks.IRON_WORKBENCH.get(), 4);
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
                        countItem(player.getInventory(), InfXItems.IRON_PICKAXE.get()) == 1,
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
        helper.setBlock(WORK_POS, flintWorkbench());

        TimedWorkbenchMenu flint = workbenchMenu(
                player, helper, BenchTier.FLINT, flintWorkbench(), 5);
        player.containerMenu = flint;
        fillFlintAxe(flint.infx$craftingContainer());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(flint, player, true),
                "three flint, two sticks, and binding must match the flint axe recipe");
        assertResult(helper, flint, InfXItems.FLINT_AXE.get(), "flint axe preview");
        flint.clicked(0, 0, ContainerInput.PICKUP, player);

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(player.getInventory(), InfXItems.FLINT_AXE.get()) == 1,
                        "flint workbench must finish the flint axe"))
                .thenExecute(() -> {
                    assertAdvancementDone(helper, player, "flint_kit", "crafting a flint axe must grant Flint Kit");
                    takeItem(helper, player.getInventory(), InfXItems.FLINT_AXE.get(), 1);
                    player.closeContainer();
                    helper.setBlock(WORK_POS, InfXBlocks.COPPER_WORKBENCH.get());

                    TimedWorkbenchMenu copper = workbenchMenu(
                            player, helper, BenchTier.COPPER, InfXBlocks.COPPER_WORKBENCH.get(), 6);
                    player.containerMenu = copper;
                    fillMetalHoe(copper.infx$craftingContainer(), Items.COPPER_INGOT);
                    helper.assertTrue(
                            TimedCraftingEngine.refreshResult(copper, player, true),
                            "two copper ingots and two sticks must match the copper hoe recipe");
                    assertResult(helper, copper, InfXItems.COPPER_HOE.get(), "copper hoe preview");
                    copper.clicked(0, 0, ContainerInput.PICKUP, player);
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(player.getInventory(), InfXItems.COPPER_HOE.get()) == 1,
                        "copper workbench must finish the copper hoe"))
                .thenExecute(() -> {
                    assertAdvancementDone(helper, player, "farming", "crafting a hoe must grant Time to Farm");
                    takeItem(helper, player.getInventory(), InfXItems.COPPER_HOE.get(), 1);
                    player.closeContainer();
                    helper.setBlock(WORK_POS, InfXBlocks.IRON_WORKBENCH.get());

                    TimedWorkbenchMenu iron = workbenchMenu(
                            player, helper, BenchTier.IRON, InfXBlocks.IRON_WORKBENCH.get(), 7);
                    player.containerMenu = iron;
                    fillMetalSword(iron.infx$craftingContainer(), Items.IRON_INGOT);
                    helper.assertTrue(
                            TimedCraftingEngine.refreshResult(iron, player, true),
                            "two iron ingots and one stick must match the iron sword recipe");
                    assertResult(helper, iron, InfXItems.IRON_SWORD.get(), "iron sword preview");
                    iron.clicked(0, 0, ContainerInput.PICKUP, player);
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        countItem(player.getInventory(), InfXItems.IRON_SWORD.get()) == 1,
                        "iron workbench must finish the InfiniteX iron sword"))
                .thenExecute(() -> removePlayer(player))
                .thenSucceed();
    }

    private static void advancedCoreToolRecipes(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        grantMaximumExperience(player);

        TimedCraftingMenu hand = (TimedCraftingMenu) player.inventoryMenu;
        player.containerMenu = player.inventoryMenu;
        hand.infx$craftingContainer().setItem(0, InfXItems.ADAMANTIUM_INGOT.toStack());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(hand, player, true),
                "metal ingots must split into nuggets in the hand crafting grid");
        assertResult(helper, hand, InfXItems.ADAMANTIUM_NUGGET.get(), "adamantium nugget preview");
        clearGrid(hand.infx$craftingContainer());

        helper.setBlock(WORK_POS, InfXBlocks.COPPER_WORKBENCH.get());
        TimedWorkbenchMenu copper = workbenchMenu(
                player, helper, BenchTier.COPPER, InfXBlocks.COPPER_WORKBENCH.get(), 8);
        player.containerMenu = copper;
        fillMetalSword(copper.infx$craftingContainer(), InfXItems.SILVER_INGOT.get());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(copper, player, true),
                "copper-tier workbenches must accept silver core tools");
        assertResult(
                helper,
                copper,
                equipment(InfxMaterial.SILVER, EquipmentType.SWORD),
                "silver sword preview");
        clearGrid(copper.infx$craftingContainer());
        fillMetalHoe(copper.infx$craftingContainer(), Items.GOLD_INGOT);
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(copper, player, true),
                "copper-tier workbenches must accept gold core tools");
        assertResult(
                helper,
                copper,
                equipment(InfxMaterial.GOLD, EquipmentType.HOE),
                "gold hoe preview");

        player.closeContainer();
        helper.setBlock(WORK_POS, InfXBlocks.ANCIENT_METAL_WORKBENCH.get());
        TimedWorkbenchMenu ancientMetal = workbenchMenu(
                player, helper, BenchTier.ANCIENT_METAL, InfXBlocks.ANCIENT_METAL_WORKBENCH.get(), 9);
        player.containerMenu = ancientMetal;
        fillMetalSword(ancientMetal.infx$craftingContainer(), InfXItems.MITHRIL_INGOT.get());
        helper.assertFalse(
                TimedCraftingEngine.refreshResult(ancientMetal, player, true),
                "ancient metal workbenches must reject mithril core tools");

        player.closeContainer();
        helper.setBlock(WORK_POS, InfXBlocks.MITHRIL_WORKBENCH.get());
        TimedWorkbenchMenu mithril = workbenchMenu(
                player, helper, BenchTier.MITHRIL, InfXBlocks.MITHRIL_WORKBENCH.get(), 10);
        player.containerMenu = mithril;
        fillMetalSword(mithril.infx$craftingContainer(), InfXItems.MITHRIL_INGOT.get());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(mithril, player, true),
                "mithril workbenches must accept mithril core tools");
        assertResult(
                helper,
                mithril,
                equipment(InfxMaterial.MITHRIL, EquipmentType.SWORD),
                "mithril sword preview");
        clearGrid(mithril.infx$craftingContainer());
        fillMetalSword(mithril.infx$craftingContainer(), InfXItems.ADAMANTIUM_INGOT.get());
        helper.assertFalse(
                TimedCraftingEngine.refreshResult(mithril, player, true),
                "mithril workbenches must reject adamantium core tools");

        player.closeContainer();
        helper.setBlock(WORK_POS, InfXBlocks.ADAMANTIUM_WORKBENCH.get());
        TimedWorkbenchMenu adamantium = workbenchMenu(
                player, helper, BenchTier.ADAMANTIUM, InfXBlocks.ADAMANTIUM_WORKBENCH.get(), 11);
        player.containerMenu = adamantium;
        fillMetalSword(adamantium.infx$craftingContainer(), InfXItems.ADAMANTIUM_INGOT.get());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(adamantium, player, true),
                "adamantium workbenches must accept adamantium core tools");
        assertResult(
                helper,
                adamantium,
                equipment(InfxMaterial.ADAMANTIUM, EquipmentType.SWORD),
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
        hand.infx$craftingContainer().setItem(2, InfXItems.SINEW.toStack());
        hand.infx$craftingContainer().setItem(3, Items.STICK.getDefaultInstance());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(hand, player, true),
                "obsidian hatchets must resolve in the hand crafting grid");
        assertResult(
                helper,
                hand,
                equipment(InfxMaterial.OBSIDIAN, EquipmentType.HATCHET),
                "obsidian hatchet preview");
        clearGrid(hand.infx$craftingContainer());

        helper.setBlock(WORK_POS, flintWorkbench());
        TimedWorkbenchMenu flint = workbenchMenu(
                player, helper, BenchTier.FLINT, flintWorkbench(), 12);
        player.containerMenu = flint;
        CraftingContainer flintGrid = flint.infx$craftingContainer();
        flintGrid.setItem(0, Items.OBSIDIAN.getDefaultInstance());
        flintGrid.setItem(1, Items.OBSIDIAN.getDefaultInstance());
        flintGrid.setItem(3, Items.OBSIDIAN.getDefaultInstance());
        flintGrid.setItem(4, Items.STICK.getDefaultInstance());
        flintGrid.setItem(6, InfXItems.SINEW.toStack());
        flintGrid.setItem(7, Items.STICK.getDefaultInstance());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(flint, player, true),
                "obsidian axes must resolve on a flint workbench");
        assertResult(
                helper,
                flint,
                equipment(InfxMaterial.OBSIDIAN, EquipmentType.AXE),
                "obsidian axe preview");

        player.closeContainer();
        helper.setBlock(WORK_POS, InfXBlocks.COPPER_WORKBENCH.get());
        TimedWorkbenchMenu copper = workbenchMenu(
                player, helper, BenchTier.COPPER, InfXBlocks.COPPER_WORKBENCH.get(), 13);
        player.containerMenu = copper;
        CraftingContainer copperGrid = copper.infx$craftingContainer();
        copperGrid.setItem(1, InfXItems.SILVER_INGOT.toStack());
        copperGrid.setItem(3, InfXItems.SILVER_INGOT.toStack());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(copper, player, true),
                "copper-tier workbenches must accept silver shears");
        assertResult(
                helper,
                copper,
                equipment(InfxMaterial.SILVER, EquipmentType.SHEARS),
                "silver shears preview");

        player.closeContainer();
        helper.setBlock(WORK_POS, InfXBlocks.IRON_WORKBENCH.get());
        TimedWorkbenchMenu iron = workbenchMenu(
                player, helper, BenchTier.IRON, InfXBlocks.IRON_WORKBENCH.get(), 14);
        player.containerMenu = iron;
        fillWarHammer(iron.infx$craftingContainer(), InfXItems.MITHRIL_INGOT.get());
        helper.assertFalse(
                TimedCraftingEngine.refreshResult(iron, player, true),
                "iron workbenches must reject mithril war hammers");

        player.closeContainer();
        helper.setBlock(WORK_POS, InfXBlocks.MITHRIL_WORKBENCH.get());
        TimedWorkbenchMenu mithril = workbenchMenu(
                player, helper, BenchTier.MITHRIL, InfXBlocks.MITHRIL_WORKBENCH.get(), 15);
        player.containerMenu = mithril;
        fillWarHammer(mithril.infx$craftingContainer(), InfXItems.MITHRIL_INGOT.get());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(mithril, player, true),
                "mithril workbenches must accept mithril war hammers");
        assertResult(
                helper,
                mithril,
                equipment(InfxMaterial.MITHRIL, EquipmentType.WAR_HAMMER),
                "mithril war hammer preview");

        player.closeContainer();
        helper.setBlock(WORK_POS, InfXBlocks.ADAMANTIUM_WORKBENCH.get());
        TimedWorkbenchMenu adamantium = workbenchMenu(
                player, helper, BenchTier.ADAMANTIUM, InfXBlocks.ADAMANTIUM_WORKBENCH.get(), 16);
        player.containerMenu = adamantium;
        CraftingContainer adamantiumGrid = adamantium.infx$craftingContainer();
        adamantiumGrid.setItem(0, Items.STICK.getDefaultInstance());
        adamantiumGrid.setItem(1, InfXItems.ADAMANTIUM_INGOT.toStack());
        adamantiumGrid.setItem(3, Items.STICK.getDefaultInstance());
        adamantiumGrid.setItem(5, InfXItems.ADAMANTIUM_INGOT.toStack());
        adamantiumGrid.setItem(6, Items.STICK.getDefaultInstance());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(adamantium, player, true),
                "adamantium workbenches must accept adamantium scythes");
        assertResult(
                helper,
                adamantium,
                equipment(InfxMaterial.ADAMANTIUM, EquipmentType.SCYTHE),
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
        handGrid.setItem(1, InfXItems.SINEW.toStack());
        handGrid.setItem(2, Items.STICK.getDefaultInstance());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(hand, player, true),
                "flint knives must resolve in the hand crafting grid");
        assertResult(
                helper,
                hand,
                equipment(InfxMaterial.FLINT, EquipmentType.KNIFE),
                "flint knife preview");

        player.closeContainer();
        helper.setBlock(WORK_POS, flintWorkbench());
        TimedWorkbenchMenu flint = workbenchMenu(
                player, helper, BenchTier.FLINT, flintWorkbench(), 17);
        player.containerMenu = flint;
        CraftingContainer flintGrid = flint.infx$craftingContainer();
        for (int slot : List.of(1, 3, 7)) {
            flintGrid.setItem(slot, Items.STICK.getDefaultInstance());
        }
        for (int slot : List.of(2, 5, 8)) {
            flintGrid.setItem(slot, InfXItems.SINEW.toStack());
        }
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(flint, player, true),
                "wood bows must resolve on a flint workbench");
        assertResult(
                helper,
                flint,
                equipment(InfxMaterial.WOOD, EquipmentType.BOW),
                "wood bow preview");

        player.closeContainer();
        helper.setBlock(WORK_POS, InfXBlocks.COPPER_WORKBENCH.get());
        TimedWorkbenchMenu copper = workbenchMenu(
                player, helper, BenchTier.COPPER, InfXBlocks.COPPER_WORKBENCH.get(), 18);
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
                equipment(InfxMaterial.GOLD, EquipmentType.DAGGER),
                "gold dagger preview");

        player.closeContainer();
        helper.setBlock(WORK_POS, InfXBlocks.IRON_WORKBENCH.get());
        TimedWorkbenchMenu iron = workbenchMenu(
                player, helper, BenchTier.IRON, InfXBlocks.IRON_WORKBENCH.get(), 19);
        player.containerMenu = iron;
        CraftingContainer ironGrid = iron.infx$craftingContainer();
        ironGrid.setItem(0, InfXItems.MITHRIL_NUGGET.toStack());
        ironGrid.setItem(3, Items.STICK.getDefaultInstance());
        ironGrid.setItem(6, Items.FEATHER.getDefaultInstance());
        helper.assertFalse(
                TimedCraftingEngine.refreshResult(iron, player, true),
                "iron workbenches must reject mithril arrows");

        player.closeContainer();
        helper.setBlock(WORK_POS, InfXBlocks.MITHRIL_WORKBENCH.get());
        TimedWorkbenchMenu mithril = workbenchMenu(
                player, helper, BenchTier.MITHRIL, InfXBlocks.MITHRIL_WORKBENCH.get(), 20);
        player.containerMenu = mithril;
        CraftingContainer mithrilGrid = mithril.infx$craftingContainer();
        mithrilGrid.setItem(0, InfXItems.MITHRIL_NUGGET.toStack());
        mithrilGrid.setItem(3, Items.STICK.getDefaultInstance());
        mithrilGrid.setItem(6, Items.FEATHER.getDefaultInstance());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(mithril, player, true),
                "mithril workbenches must accept mithril arrows");
        assertResult(
                helper,
                mithril,
                equipment(InfxMaterial.MITHRIL, EquipmentType.ARROW),
                "mithril arrow preview");

        removePlayer(player);
        helper.succeed();
    }

    private static void armorRecipes(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        helper.onEachTick(player::doTick);
        grantMaximumExperience(player);

        helper.setBlock(WORK_POS, flintWorkbench());
        TimedWorkbenchMenu flint = workbenchMenu(
                player, helper, BenchTier.FLINT, flintWorkbench(), 21);
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
                equipment(InfxMaterial.LEATHER, EquipmentType.HELMET),
                "leather helmet preview");
        clearGrid(flintGrid);
        for (int slot : List.of(1, 3, 5, 7)) {
            flintGrid.setItem(slot, Items.COPPER_NUGGET.getDefaultInstance());
        }
        helper.assertFalse(
                TimedCraftingEngine.refreshResult(flint, player, true),
                "flint workbenches must reject copper chain crafting");

        player.closeContainer();
        helper.setBlock(WORK_POS, InfXBlocks.COPPER_WORKBENCH.get());
        TimedWorkbenchMenu copper = workbenchMenu(
                player, helper, BenchTier.COPPER, InfXBlocks.COPPER_WORKBENCH.get(), 22);
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
                InfXItems.catalog().raw("copper_chain").holder().get(),
                "copper chain preview");
        clearGrid(copperGrid);
        for (int slot : List.of(0, 2, 3, 5)) {
            copperGrid.setItem(slot, InfXItems.catalog().raw("copper_chain").holder().get().getDefaultInstance());
        }
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(copper, player, true),
                "copper chainmail boots must resolve on a copper workbench");
        assertResult(
                helper,
                copper,
                equipment(InfxMaterial.COPPER, EquipmentType.CHAINMAIL_BOOTS),
                "copper chainmail boots preview");

        player.closeContainer();
        helper.setBlock(WORK_POS, InfXBlocks.IRON_WORKBENCH.get());
        TimedWorkbenchMenu iron = workbenchMenu(
                player, helper, BenchTier.IRON, InfXBlocks.IRON_WORKBENCH.get(), 23);
        player.containerMenu = iron;
        CraftingContainer ironGrid = iron.infx$craftingContainer();
        for (int slot : List.of(0, 1, 2, 3, 5)) {
            ironGrid.setItem(slot, InfXItems.MITHRIL_INGOT.toStack());
        }
        helper.assertFalse(
                TimedCraftingEngine.refreshResult(iron, player, true),
                "iron workbenches must reject mithril plate armor");

        player.closeContainer();
        helper.setBlock(WORK_POS, InfXBlocks.MITHRIL_WORKBENCH.get());
        TimedWorkbenchMenu mithril = workbenchMenu(
                player, helper, BenchTier.MITHRIL, InfXBlocks.MITHRIL_WORKBENCH.get(), 24);
        player.containerMenu = mithril;
        CraftingContainer mithrilGrid = mithril.infx$craftingContainer();
        for (int slot : List.of(0, 1, 2, 3, 5)) {
            mithrilGrid.setItem(slot, InfXItems.MITHRIL_INGOT.toStack());
        }
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(mithril, player, true),
                "mithril workbenches must accept mithril plate armor");
        assertResult(
                helper,
                mithril,
                equipment(InfxMaterial.MITHRIL, EquipmentType.HELMET),
                "mithril helmet preview");
        player.closeContainer();

        player.setItemSlot(
                EquipmentSlot.HEAD,
                equipment(InfxMaterial.LEATHER, EquipmentType.HELMET).getDefaultInstance());
        player.setItemSlot(
                EquipmentSlot.CHEST,
                equipment(InfxMaterial.LEATHER, EquipmentType.CHESTPLATE).getDefaultInstance());
        player.setItemSlot(
                EquipmentSlot.LEGS,
                equipment(InfxMaterial.LEATHER, EquipmentType.LEGGINGS).getDefaultInstance());
        player.setItemSlot(
                EquipmentSlot.FEET,
                equipment(InfxMaterial.LEATHER, EquipmentType.BOOTS).getDefaultInstance());
        helper.startSequence()
                .thenWaitUntil(() -> assertAdvancementDone(
                        helper, player, "leather_armor", "wearing leather armor must grant Suiting Up"))
                .thenExecute(() -> {
                    player.setItemSlot(
                            EquipmentSlot.HEAD,
                            equipment(InfxMaterial.COPPER, EquipmentType.HELMET).getDefaultInstance());
                    player.setItemSlot(
                            EquipmentSlot.CHEST,
                            equipment(InfxMaterial.GOLD, EquipmentType.CHESTPLATE).getDefaultInstance());
                    player.setItemSlot(
                            EquipmentSlot.LEGS,
                            equipment(InfxMaterial.IRON, EquipmentType.LEGGINGS).getDefaultInstance());
                    player.setItemSlot(
                            EquipmentSlot.FEET,
                            equipment(InfxMaterial.MITHRIL, EquipmentType.BOOTS).getDefaultInstance());
                })
                .thenWaitUntil(() -> assertAdvancementDone(
                        helper,
                        player,
                        "metal_armor",
                        "a mixed full metal set must grant Metal Shell"))
                .thenExecute(() -> {
                    player.setItemSlot(
                            EquipmentSlot.HEAD,
                            equipment(InfxMaterial.ADAMANTIUM, EquipmentType.HELMET).getDefaultInstance());
                    player.setItemSlot(
                            EquipmentSlot.CHEST,
                            equipment(InfxMaterial.ADAMANTIUM, EquipmentType.CHESTPLATE).getDefaultInstance());
                    player.setItemSlot(
                            EquipmentSlot.LEGS,
                            equipment(InfxMaterial.ADAMANTIUM, EquipmentType.LEGGINGS).getDefaultInstance());
                    player.setItemSlot(
                            EquipmentSlot.FEET,
                            equipment(InfxMaterial.ADAMANTIUM, EquipmentType.BOOTS).getDefaultInstance());
                })
                .thenWaitUntil(() -> assertAdvancementDone(
                        helper,
                        player,
                        "adamantium_armor",
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
                    furnace[0].setItem(0, InfXItems.SILVER_ORE.toStack());
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        furnace[0].getItem(2).is(InfXItems.SILVER_INGOT),
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

    private static void furnaceClickExperience(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        helper.onEachTick(player::doTick);
        for (int x = 0; x <= 1; x++) {
            for (int z = 1; z <= 2; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
        BlockState vanillaState = Blocks.FURNACE
                .defaultBlockState()
                .setValue(AbstractFurnaceBlock.FACING, Direction.NORTH);
        BlockState obsidianState = InfXBlocks.OBSIDIAN_FURNACE.get()
                .defaultBlockState()
                .setValue(AbstractFurnaceBlock.FACING, Direction.NORTH);
        BlockPos vanillaClickPos = new BlockPos(2, 1, 1);
        BlockPos vanillaShiftPos = new BlockPos(3, 1, 1);
        BlockPos infxClickPos = new BlockPos(2, 1, 2);
        BlockPos infxShiftPos = new BlockPos(4, 1, 1);
        BlockState vanillaSouth = Blocks.FURNACE
                .defaultBlockState()
                .setValue(AbstractFurnaceBlock.FACING, Direction.SOUTH);
        BlockState obsidianEast = InfXBlocks.OBSIDIAN_FURNACE.get()
                .defaultBlockState()
                .setValue(AbstractFurnaceBlock.FACING, Direction.EAST);
        helper.setBlock(vanillaClickPos, vanillaState);
        helper.setBlock(vanillaShiftPos, vanillaSouth);
        helper.setBlock(infxClickPos, obsidianEast);
        helper.setBlock(infxShiftPos, obsidianState);
        FurnaceBlockEntity[] vanillaClick = {
            helper.getBlockEntity(vanillaClickPos, FurnaceBlockEntity.class)
        };
        FurnaceBlockEntity[] vanillaShift = {
            helper.getBlockEntity(vanillaShiftPos, FurnaceBlockEntity.class)
        };
        InfxFurnaceBlockEntity[] infxClick = {
            helper.getBlockEntity(infxClickPos, InfxFurnaceBlockEntity.class)
        };
        InfxFurnaceBlockEntity[] infxShift = {
            helper.getBlockEntity(infxShiftPos, InfxFurnaceBlockEntity.class)
        };
        for (FurnaceBlockEntity furnace : new FurnaceBlockEntity[] {vanillaClick[0], vanillaShift[0]}) {
            furnace.setItem(0, new ItemStack(Items.PORKCHOP, 3));
            furnace.setItem(1, new ItemStack(Items.COAL, 2));
        }
        for (InfxFurnaceBlockEntity furnace : new InfxFurnaceBlockEntity[] {infxClick[0], infxShift[0]}) {
            furnace.setItem(0, new ItemStack(Items.PORKCHOP, 3));
            furnace.setItem(1, new ItemStack(Items.LAVA_BUCKET));
        }

        int[] xpBeforeClick = {0};
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(
                        allCooked(vanillaClick[0])
                                && allCooked(vanillaShift[0])
                                && allCooked(infxClick[0])
                                && allCooked(infxShift[0]),
                        "all four furnaces must finish cooking three porkchops: "
                                + describe(vanillaClickPos, vanillaClick[0]) + "; "
                                + describe(vanillaShiftPos, vanillaShift[0]) + "; "
                                + describe(infxClickPos, infxClick[0]) + "; "
                                + describe(infxShiftPos, infxShift[0])))
                .thenExecute(() -> {
                    player.openMenu(vanillaClick[0]);
                    xpBeforeClick[0] = player.totalExperience;
                    player.containerMenu.clicked(2, 0, ContainerInput.PICKUP, player);
                    ItemStack carried = player.containerMenu.getCarried();
                    helper.assertTrue(
                            carried.is(Items.COOKED_PORKCHOP) && carried.getCount() == 3,
                            "left-click must pick up the whole cooked stack, carried=" + carried);
                    helper.assertTrue(
                            player.containerMenu.getSlot(2).getItem().isEmpty(),
                            "left-click must empty the result slot");
                    LOGGER.info("vanilla left-click: orbs={} xpBefore={}",
                            orbCount(helper, vanillaClickPos), xpBeforeClick[0]);
                    player.getInventory().add(carried);
                    player.containerMenu.setCarried(ItemStack.EMPTY);
                    player.closeContainer();
                })
                .thenExecuteAfter(10, () -> helper.assertTrue(
                        player.totalExperience > xpBeforeClick[0],
                        "left-click on the vanilla furnace must award experience, xp="
                                + player.totalExperience + " alive=" + player.isAlive()))
                .thenExecute(() -> {
                    player.openMenu(vanillaShift[0]);
                    xpBeforeClick[0] = player.totalExperience;
                    player.containerMenu.clicked(2, 0, ContainerInput.QUICK_MOVE, player);
                    helper.assertTrue(
                            countItem(player.getInventory(), Items.COOKED_PORKCHOP) >= 3,
                            "shift-click must move the cooked porkchops into the inventory");
                    helper.assertTrue(
                            player.containerMenu.getSlot(2).getItem().isEmpty(),
                            "shift-click must empty the result slot");
                    LOGGER.info("vanilla shift-click: orbs={} xpBefore={}",
                            orbCount(helper, vanillaShiftPos), xpBeforeClick[0]);
                    player.closeContainer();
                })
                .thenExecuteAfter(10, () -> helper.assertTrue(
                        player.totalExperience > xpBeforeClick[0],
                        "shift-click on the vanilla furnace must award experience, xp="
                                + player.totalExperience + " alive=" + player.isAlive()))
                .thenExecute(() -> {
                    player.openMenu(infxClick[0]);
                    xpBeforeClick[0] = player.totalExperience;
                    player.containerMenu.clicked(2, 0, ContainerInput.PICKUP, player);
                    ItemStack carried = player.containerMenu.getCarried();
                    helper.assertTrue(
                            carried.is(Items.COOKED_PORKCHOP) && carried.getCount() == 3,
                            "left-click must pick up the whole cooked stack from the InfX furnace, carried=" + carried);
                    helper.assertTrue(
                            player.containerMenu.getSlot(2).getItem().isEmpty(),
                            "left-click must empty the InfX result slot");
                    LOGGER.info("infx left-click: orbs={} xpBefore={}",
                            orbCount(helper, infxClickPos), xpBeforeClick[0]);
                    player.getInventory().add(carried);
                    player.containerMenu.setCarried(ItemStack.EMPTY);
                    player.closeContainer();
                })
                .thenExecuteAfter(10, () -> helper.assertTrue(
                        player.totalExperience > xpBeforeClick[0],
                        "left-click on the InfX furnace must award experience, xp="
                                + player.totalExperience + " alive=" + player.isAlive()))
                .thenExecute(() -> {
                    player.openMenu(infxShift[0]);
                    xpBeforeClick[0] = player.totalExperience;
                    player.containerMenu.clicked(2, 0, ContainerInput.QUICK_MOVE, player);
                    helper.assertTrue(
                            countItem(player.getInventory(), Items.COOKED_PORKCHOP) >= 6,
                            "shift-click must move the cooked porkchops from the InfX furnace into the inventory");
                    helper.assertTrue(
                            player.containerMenu.getSlot(2).getItem().isEmpty(),
                            "shift-click must empty the InfX result slot");
                    LOGGER.info("infx shift-click: orbs={} xpBefore={}",
                            orbCount(helper, infxShiftPos), xpBeforeClick[0]);
                    player.closeContainer();
                })
                .thenExecuteAfter(10, () -> helper.assertTrue(
                        player.totalExperience > xpBeforeClick[0],
                        "shift-click on the InfX furnace must award experience, xp="
                                + player.totalExperience + " alive=" + player.isAlive()))
                .thenExecuteAfter(20, () -> removePlayer(player))
                .thenSucceed();
    }

    private static boolean allCooked(AbstractFurnaceBlockEntity furnace) {
        return furnace.getItem(2).is(Items.COOKED_PORKCHOP) && furnace.getItem(2).getCount() == 3;
    }

    private static String describe(BlockPos pos, AbstractFurnaceBlockEntity furnace) {
        String heat = "max=vanilla";
        if (furnace instanceof FurnaceHeatAccess access) {
            heat = "heat=" + access.infx$currentHeat() + " lit=" + access.infx$litTimeRemaining();
        }
        return pos + " " + heat + " fuel=" + furnace.getItem(1) + " in=" + furnace.getItem(0).getCount()
                + " out=" + furnace.getItem(2);
    }

    private static int orbCount(GameTestHelper helper, BlockPos pos) {
        return helper.getLevel()
                .getEntitiesOfClass(ExperienceOrb.class, new AABB(helper.absolutePos(pos)).inflate(8.0))
                .size();
    }

    private static void furnaceTierRules(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        helper.onEachTick(player::doTick);
        var clayState = InfXBlocks.CLAY_FURNACE.get()
                .defaultBlockState()
                .setValue(AbstractFurnaceBlock.FACING, Direction.NORTH);
        helper.setBlock(FURNACE_POS, clayState);
        InfxFurnaceBlockEntity[] furnace = {
            helper.getBlockEntity(FURNACE_POS, InfxFurnaceBlockEntity.class)
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
                    var sandstoneState = InfXBlocks.SANDSTONE_FURNACE.get()
                            .defaultBlockState()
                            .setValue(AbstractFurnaceBlock.FACING, Direction.NORTH);
                    helper.setBlock(FURNACE_POS, sandstoneState);
                    furnace[0] = helper.getBlockEntity(FURNACE_POS, InfxFurnaceBlockEntity.class);

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
                InfXBlocks.OBSIDIAN_FURNACE.get()
                        .defaultBlockState()
                        .setValue(AbstractFurnaceBlock.FACING, Direction.NORTH));
        InfxFurnaceBlockEntity[] furnace = {
            helper.getBlockEntity(FURNACE_POS, InfxFurnaceBlockEntity.class)
        };
        helper.assertTrue(
                furnace[0].canPlaceItem(1, Items.LAVA_BUCKET.getDefaultInstance()),
                "the obsidian furnace must accept heat-3 lava");
        helper.assertFalse(
                furnace[0].canPlaceItem(1, Items.BLAZE_ROD.getDefaultInstance()),
                "the obsidian furnace must reject heat-4 blaze rods");
        furnace[0].setItem(0, InfXItems.MITHRIL_ORE.toStack());
        furnace[0].setItem(1, Items.LAVA_BUCKET.getDefaultInstance());

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(
                        ((FurnaceHeatAccess) (Object) furnace[0]).infx$currentHeat() == 3,
                        "the obsidian furnace must burn lava at heat 3"))
                .thenWaitUntil(() -> helper.assertTrue(
                        furnace[0].getItem(2).is(InfXItems.MITHRIL_INGOT),
                        "heat-3 lava must smelt mithril ore in the obsidian furnace"))
                .thenExecute(() -> {
                    helper.setBlock(FURNACE_POS, Blocks.AIR);
                    helper.setBlock(
                            FURNACE_POS,
                            InfXBlocks.NETHERRACK_FURNACE.get()
                                    .defaultBlockState()
                                    .setValue(AbstractFurnaceBlock.FACING, Direction.NORTH));
                    furnace[0] = helper.getBlockEntity(FURNACE_POS, InfxFurnaceBlockEntity.class);
                    helper.assertTrue(
                            furnace[0].canPlaceItem(1, Items.BLAZE_ROD.getDefaultInstance()),
                            "the netherrack furnace must accept heat-4 blaze rods");
                    furnace[0].setItem(0, InfXItems.ADAMANTIUM_ORE.toStack());
                    furnace[0].setItem(1, Items.LAVA_BUCKET.getDefaultInstance());
                })
                .thenExecuteAfter(40, () -> {
                    helper.assertTrue(
                            furnace[0].getItem(0).is(InfXItems.ADAMANTIUM_ORE),
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
                        furnace[0].getItem(2).is(InfXItems.ADAMANTIUM_INGOT),
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
        Experience.setTotal(player, Experience.XP_AT_DISPLAY_CAP);
    }

    private static Block flintWorkbench() {
        return InfXBlocks.STRIPPED_LOG_WORKBENCHES.getFirst().flint().get();
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
        float actual = InfxCraftingRules.componentDifficulty(item.getDefaultInstance());
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
        grid.setItem(6, InfXItems.SINEW.get().getDefaultInstance());
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

    private static void fillDoor(CraftingContainer grid, Item material) {
        for (int slot : List.of(0, 1, 3, 4, 6, 7)) {
            grid.setItem(slot, material.getDefaultInstance());
        }
    }

    private static void fillRail(CraftingContainer grid) {
        for (int slot : List.of(0, 2, 3, 5, 6, 8)) {
            grid.setItem(slot, Items.IRON_INGOT.getDefaultInstance());
        }
        grid.setItem(4, Items.STICK.getDefaultInstance());
    }

    private static void fillScaffolding(CraftingContainer grid) {
        for (int slot : List.of(0, 2, 3, 5, 6, 8)) {
            grid.setItem(slot, Items.BAMBOO.getDefaultInstance());
        }
        grid.setItem(1, Items.STRING.getDefaultInstance());
    }

    private static void fillSpyglass(CraftingContainer grid) {
        grid.setItem(1, Items.AMETHYST_SHARD.getDefaultInstance());
        grid.setItem(4, Items.COPPER_INGOT.getDefaultInstance());
        grid.setItem(7, Items.COPPER_INGOT.getDefaultInstance());
    }

    private static void fillShelf(CraftingContainer grid) {
        for (int slot : List.of(0, 1, 2, 6, 7, 8)) {
            grid.setItem(slot, Items.STRIPPED_OAK_LOG.getDefaultInstance());
        }
    }

    private static void fillConcretePowder(CraftingContainer grid) {
        grid.setItem(0, Items.WHITE_DYE.getDefaultInstance());
        for (int slot : List.of(1, 2, 3, 4)) {
            grid.setItem(slot, Items.SAND.getDefaultInstance());
        }
        for (int slot : List.of(5, 6, 7, 8)) {
            grid.setItem(slot, Items.GRAVEL.getDefaultInstance());
        }
    }

    private static void fillStainedGlass(CraftingContainer grid) {
        for (int slot : List.of(0, 1, 2, 3, 5, 6, 7, 8)) {
            grid.setItem(slot, Items.GLASS.getDefaultInstance());
        }
        grid.setItem(4, Items.WHITE_DYE.getDefaultInstance());
    }

    private static void fillStainedTerracotta(CraftingContainer grid) {
        for (int slot : List.of(0, 1, 2, 3, 5, 6, 7, 8)) {
            grid.setItem(slot, Items.TERRACOTTA.getDefaultInstance());
        }
        grid.setItem(4, Items.WHITE_DYE.getDefaultInstance());
    }

    private static void clearGrid(CraftingContainer grid) {
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            grid.setItem(slot, ItemStack.EMPTY);
        }
    }

    private static Item equipment(InfxMaterial material, EquipmentType type) {
        return InfXItems.catalog().equipment(material, type).holder().get();
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
