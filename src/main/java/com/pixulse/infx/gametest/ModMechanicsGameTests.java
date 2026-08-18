package com.pixulse.infx.gametest;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.data.food.FoodProfile;
import com.pixulse.infx.data.food.FoodProfiles;
import com.pixulse.infx.entity.GelatinousSphere;
import com.pixulse.infx.entity.InfxBrickProjectile;
import com.pixulse.infx.entity.InfxSheep;
import com.pixulse.infx.entity.InfxWolf;
import com.pixulse.infx.item.InfxBucketItem;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.registry.InfXItems;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Batch-2 mechanics: explosion drops, cauldron vessels, brick throwing, colored eggs, wool acid. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class ModMechanicsGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, InfiniteX.MOD_ID);
    private static final List<String> NAMES = List.of(
            "infx_explosion_drops",
            "infx_explosion_ore_shards",
            "infx_cauldron_vessels",
            "infx_dire_wolf_sit",
            "infx_dire_wolf_sit_mid_follow",
            "infx_dire_wolf_sit_ignores_generic_rules",
            "infx_brick_throw",
            "infx_colored_eggs",
            "infx_leaves_projectile_pass",
            "infx_leaves_item_pass",
            "infx_sphere_sheep",
            "infx_moon_brightness",
            "infx_monster_cap");

    static {
        FUNCTIONS.register("infx_explosion_drops", () -> ModMechanicsGameTests::explosionDrops);
        FUNCTIONS.register("infx_explosion_ore_shards", () -> ModMechanicsGameTests::explosionOreShards);
        FUNCTIONS.register("infx_cauldron_vessels", () -> ModMechanicsGameTests::cauldronVessels);
        FUNCTIONS.register("infx_dire_wolf_sit", () -> ModMechanicsGameTests::direWolfSit);
        FUNCTIONS.register("infx_dire_wolf_sit_mid_follow", () -> ModMechanicsGameTests::direWolfSitMidFollow);
        FUNCTIONS.register("infx_dire_wolf_sit_ignores_generic_rules", () -> ModMechanicsGameTests::direWolfSitIgnoresGenericMonsterRules);
        FUNCTIONS.register("infx_brick_throw", () -> ModMechanicsGameTests::brickThrow);
        FUNCTIONS.register("infx_colored_eggs", () -> ModMechanicsGameTests::coloredEggs);
        FUNCTIONS.register("infx_leaves_projectile_pass", () -> ModMechanicsGameTests::leavesProjectilePass);
        FUNCTIONS.register("infx_leaves_item_pass", () -> ModMechanicsGameTests::leavesItemPass);
        FUNCTIONS.register("infx_sphere_sheep", () -> ModMechanicsGameTests::sphereSheep);
        FUNCTIONS.register("infx_moon_brightness", () -> ModMechanicsGameTests::moonBrightness);
        FUNCTIONS.register("infx_monster_cap", () -> ModMechanicsGameTests::monsterCap);
    }

    private ModMechanicsGameTests() {}

    public static void register(IEventBus modBus) {
        FUNCTIONS.register(modBus);
    }

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                InfiniteX.id("infx_mechanics"), new TestEnvironmentDefinition.AllOf());
        for (String name : NAMES) {
            ResourceKey<Consumer<GameTestHelper>> function =
                    ResourceKey.create(Registries.TEST_FUNCTION, InfiniteX.id(name));
            event.registerTest(
                    function.identifier(),
                    new FunctionGameTestInstance(
                            function,
                            new TestData<>(
                                    environment,
                                    Identifier.withDefaultNamespace("empty"),
                                    800,
                                    0,
                                    true,
                                    net.minecraft.world.level.block.Rotation.NONE)));
        }
    }

    /** InfX Block#dropBlockAsEntityItem: exploded wool, brick and stone drop their exploded forms. */
    private static void explosionDrops(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos wool = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos brick = helper.absolutePos(new BlockPos(3, 2, 2));
        BlockPos stone = helper.absolutePos(new BlockPos(4, 2, 2));
        level.setBlock(wool, Blocks.RED_WOOL.defaultBlockState(), 3);
        level.setBlock(brick, Blocks.BRICKS.defaultBlockState(), 3);
        level.setBlock(stone, Blocks.STONE.defaultBlockState(), 3);
        List<BlockPos> affected = new java.util.ArrayList<>(List.of(wool, brick, stone));
        ServerExplosion explosion = new ServerExplosion(
                level,
                null,
                null,
                null,
                Vec3.atCenterOf(helper.absolutePos(new BlockPos(3, 2, 2))),
                3.0F,
                false,
                Explosion.BlockInteraction.DESTROY);
        NeoForge.EVENT_BUS.post(new ExplosionEvent.Detonate(level, explosion, List.of(), affected));
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(
                    affected.isEmpty(),
                    "converted blocks must be removed from the affected list");
            helper.assertTrue(level.getBlockState(wool).isAir(), "wool must be destroyed by the explosion");
            helper.assertTrue(level.getBlockState(brick).isAir(), "bricks must be destroyed by the explosion");
            helper.assertTrue(level.getBlockState(stone).isAir(), "stone must be destroyed by the explosion");
            AABB search = new AABB(Vec3.atCenterOf(wool), Vec3.atCenterOf(stone)).inflate(6.0);
            List<ItemStack> stacks = level.getEntitiesOfClass(ItemEntity.class, search).stream()
                    .map(ItemEntity::getItem)
                    .toList();
            helper.assertTrue(
                    stacks.stream().anyMatch(stack -> stack.is(Items.STRING)),
                    "wool must explode into string");
            helper.assertTrue(
                    stacks.stream().anyMatch(stack -> stack.is(Items.BRICK)),
                    "bricks must explode into a brick");
            helper.assertTrue(
                    stacks.stream().anyMatch(stack -> stack.is(Items.COBBLESTONE)),
                    "stone must explode into cobblestone");
            helper.succeed();
        });
    }

    /** InfX BlockOre#dropBlockAsEntityItem: exploded diamond/emerald ore (incl. deepslate) drop 1 shard. */
    private static void explosionOreShards(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos diamondOre = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos deepslateDiamondOre = helper.absolutePos(new BlockPos(3, 2, 2));
        BlockPos emeraldOre = helper.absolutePos(new BlockPos(4, 2, 2));
        BlockPos deepslateEmeraldOre = helper.absolutePos(new BlockPos(5, 2, 2));
        level.setBlock(diamondOre, Blocks.DIAMOND_ORE.defaultBlockState(), 3);
        level.setBlock(deepslateDiamondOre, Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState(), 3);
        level.setBlock(emeraldOre, Blocks.EMERALD_ORE.defaultBlockState(), 3);
        level.setBlock(deepslateEmeraldOre, Blocks.DEEPSLATE_EMERALD_ORE.defaultBlockState(), 3);
        List<BlockPos> affected =
                new java.util.ArrayList<>(List.of(diamondOre, deepslateDiamondOre, emeraldOre, deepslateEmeraldOre));
        ServerExplosion explosion = new ServerExplosion(
                level,
                null,
                null,
                null,
                Vec3.atCenterOf(helper.absolutePos(new BlockPos(3, 2, 2))),
                3.0F,
                false,
                Explosion.BlockInteraction.DESTROY);
        NeoForge.EVENT_BUS.post(new ExplosionEvent.Detonate(level, explosion, List.of(), affected));
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(
                    affected.isEmpty(),
                    "converted ore blocks must be removed from the affected list");
            helper.assertTrue(
                    level.getBlockState(diamondOre).isAir(), "diamond ore must be destroyed by the explosion");
            helper.assertTrue(
                    level.getBlockState(deepslateDiamondOre).isAir(),
                    "deepslate diamond ore must be destroyed by the explosion");
            helper.assertTrue(
                    level.getBlockState(emeraldOre).isAir(), "emerald ore must be destroyed by the explosion");
            helper.assertTrue(
                    level.getBlockState(deepslateEmeraldOre).isAir(),
                    "deepslate emerald ore must be destroyed by the explosion");
            AABB search = new AABB(Vec3.atCenterOf(diamondOre), Vec3.atCenterOf(deepslateEmeraldOre)).inflate(10.0);
            List<ItemStack> stacks = level.getEntitiesOfClass(ItemEntity.class, search).stream()
                    .map(ItemEntity::getItem)
                    .toList();
            helper.assertTrue(
                    stacks.stream().anyMatch(stack -> stack.is(InfXItems.DIAMOND_SHARD.get())),
                    "exploded diamond ore must drop diamond shards");
            helper.assertTrue(
                    stacks.stream().noneMatch(stack -> stack.is(Items.DIAMOND)),
                    "exploded diamond ore must not drop whole diamonds");
            helper.assertTrue(
                    stacks.stream().anyMatch(stack -> stack.is(InfXItems.EMERALD_SHARD.get())),
                    "exploded emerald ore must drop emerald shards");
            helper.assertTrue(
                    stacks.stream().noneMatch(stack -> stack.is(Items.EMERALD)),
                    "exploded emerald ore must not drop whole emeralds");
            helper.succeed();
        });
    }

    /** InfX: a tamed dire wolf ordered to sit stays put and never follows or teleports to its owner. */
    private static void direWolfSit(GameTestHelper helper) {
        ServerPlayer player = ModCompletionGameTests.createPlayer(helper);
        ServerLevel level = helper.getLevel();
        // Floor the wolf's spawn cell so it lands and grounds before the sit goal can run.
        for (BlockPos floor : List.of(new BlockPos(3, 1, 2), new BlockPos(3, 0, 2))) {
            level.setBlock(helper.absolutePos(floor), Blocks.STONE.defaultBlockState(), 3);
        }
        InfxWolf wolf = helper.spawn(InfXEntityTypes.DIRE_WOLF.get(), new BlockPos(3, 2, 2));
        wolf.setTame(true, true);
        wolf.setOwner(player);
        // Right-click with an empty hand, exactly like the player toggling the sit pose.
        wolf.mobInteract(player, InteractionHand.MAIN_HAND);
        Vec3 start = wolf.position();
        // Move the owner beyond the follow start distance (10) and teleport distance (12).
        player.snapTo(player.getX() + 24.0, player.getY(), player.getZ() + 24.0, 0.0F, 0.0F);
        helper.runAfterDelay(60, () -> {
            helper.assertTrue(
                    wolf.isOrderedToSit(), "the dire wolf must remain ordered to sit");
            helper.assertTrue(
                    wolf.isInSittingPose(), "the dire wolf must remain in the sitting pose");
            helper.assertTrue(
                    wolf.distanceToSqr(start) < 0.5,
                    "a sitting dire wolf must not follow or teleport to its owner");
            ModCompletionGameTests.removePlayer(player);
            helper.succeed();
        });
    }

    /** InfX: sitting while mid-follow holds; walking away after a sit toggle must not resume the chase. */
    private static void direWolfSitMidFollow(GameTestHelper helper) {
        ServerPlayer player = ModCompletionGameTests.createPlayer(helper);
        ServerLevel level = helper.getLevel();
        for (BlockPos floor : List.of(new BlockPos(3, 1, 2), new BlockPos(3, 0, 2))) {
            level.setBlock(helper.absolutePos(floor), Blocks.STONE.defaultBlockState(), 3);
        }
        InfxWolf wolf = helper.spawn(InfXEntityTypes.DIRE_WOLF.get(), new BlockPos(3, 2, 2));
        wolf.setTame(true, true);
        wolf.setOwner(player);
        // Owner far away so the follow goal is eligible, then right-click to sit.
        player.snapTo(player.getX() + 24.0, player.getY(), player.getZ() + 24.0, 0.0F, 0.0F);
        wolf.mobInteract(player, InteractionHand.MAIN_HAND);
        Vec3 start = wolf.position();
        helper.runAfterDelay(60, () -> {
            helper.assertTrue(
                    wolf.isOrderedToSit(), "the mid-follow right-click must order the wolf to sit");
            helper.assertTrue(
                    wolf.isInSittingPose(), "the mid-follow right-click must pose the wolf");
            helper.assertTrue(
                    wolf.distanceToSqr(start) < 0.5,
                    "a wolf sat down mid-follow must not keep following");
            ModCompletionGameTests.removePlayer(player);
            helper.succeed();
        });
    }

    /**
     * MonsterEvents must not treat dire wolves as generic Enemy monsters: a sitting tamed
     * dire wolf must not target the lit player or path after targets or the player's light.
     */
    private static void direWolfSitIgnoresGenericMonsterRules(GameTestHelper helper) {
        ServerPlayer player = ModCompletionGameTests.createPlayer(helper);
        ServerLevel level = helper.getLevel();
        // Floor the wolf cell and the lamp cell; bright lamp beside the player.
        for (BlockPos floor : List.of(new BlockPos(3, 1, 2), new BlockPos(2, 1, 1))) {
            level.setBlock(helper.absolutePos(floor), Blocks.STONE.defaultBlockState(), 3);
        }
        level.setBlock(helper.absolutePos(new BlockPos(2, 2, 1)), Blocks.SEA_LANTERN.defaultBlockState(), 3);
        InfxWolf wolf = helper.spawn(InfXEntityTypes.DIRE_WOLF.get(), new BlockPos(3, 2, 2));
        wolf.setTame(true, true);
        wolf.setOwner(player);
        wolf.setOrderedToSit(true);
        // The empty test platform does not always resolve the fall collision, leaving the
        // spawn airborne; the sit goal refuses to start until the mob is grounded.
        wolf.setOnGround(true);
        Vec3 start = wolf.position();
        helper.runAfterDelay(100, () -> {
            helper.assertTrue(
                    !(wolf.getTarget() instanceof Player),
                    "a sitting tamed dire wolf must not target the lit player");
            helper.assertTrue(
                    wolf.isInSittingPose(), "the dire wolf must remain sitting");
            helper.assertTrue(
                    wolf.distanceToSqr(start) < 2.0,
                    "a sitting tamed dire wolf must not path after targets or light");
            ModCompletionGameTests.removePlayer(player);
            helper.succeed();
        });
    }

    /** InfX BlockCauldron ItemVessel branch: buckets move three levels, bowls one. */
    private static void cauldronVessels(GameTestHelper helper) {
        ServerPlayer player = ModCompletionGameTests.createPlayer(helper);
        ServerLevel level = helper.getLevel();
        BlockPos cauldron = helper.absolutePos(new BlockPos(2, 2, 2));
        Item emptyIron = InfXItems.bucket(InfxMaterial.IRON, InfxBucketItem.Contents.EMPTY).value();
        Item waterIron = InfXItems.bucket(InfxMaterial.IRON, InfxBucketItem.Contents.WATER).value();

        level.setBlock(
                cauldron,
                Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3),
                3);
        CauldronInteractions.WATER.get(new ItemStack(emptyIron))
                .interact(level.getBlockState(cauldron), level, cauldron, player, InteractionHand.MAIN_HAND, new ItemStack(emptyIron));
        helper.assertTrue(
                player.getMainHandItem().is(waterIron),
                "an empty bucket must fill from a full water cauldron");
        helper.assertTrue(level.getBlockState(cauldron).is(Blocks.CAULDRON), "the cauldron empties into the bucket");

        CauldronInteractions.EMPTY.get(player.getMainHandItem())
                .interact(level.getBlockState(cauldron), level, cauldron, player, InteractionHand.MAIN_HAND, player.getMainHandItem());
        BlockState refilled = level.getBlockState(cauldron);
        helper.assertTrue(
                refilled.is(Blocks.WATER_CAULDRON) && refilled.getValue(LayeredCauldronBlock.LEVEL) == 3,
                "a water bucket must refill the empty cauldron to three levels");
        helper.assertTrue(player.getMainHandItem().is(emptyIron), "the bucket comes back empty");

        level.setBlock(
                cauldron,
                Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 1),
                3);
        CauldronInteractions.WATER.get(new ItemStack(Items.BOWL))
                .interact(level.getBlockState(cauldron), level, cauldron, player, InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL));
        helper.assertTrue(player.getMainHandItem().is(InfXItems.WATER_BOWL.value()), "an empty bowl takes one cauldron level");
        helper.assertTrue(level.getBlockState(cauldron).is(Blocks.CAULDRON), "one bowl empties the last cauldron level");

        CauldronInteractions.EMPTY.get(player.getMainHandItem())
                .interact(level.getBlockState(cauldron), level, cauldron, player, InteractionHand.MAIN_HAND, player.getMainHandItem());
        BlockState afterBowl = level.getBlockState(cauldron);
        helper.assertTrue(
                afterBowl.is(Blocks.WATER_CAULDRON) && afterBowl.getValue(LayeredCauldronBlock.LEVEL) == 1,
                "a water bowl refills one cauldron level");
        helper.assertTrue(player.getMainHandItem().is(Items.BOWL), "the bowl comes back empty");

        // Lava: an empty bucket takes lava from a lava cauldron; a lava bucket refills it.
        Item lavaIron = InfXItems.bucket(InfxMaterial.IRON, InfxBucketItem.Contents.LAVA).value();
        level.setBlock(cauldron, Blocks.LAVA_CAULDRON.defaultBlockState(), 3);
        CauldronInteractions.LAVA.get(new ItemStack(emptyIron))
                .interact(level.getBlockState(cauldron), level, cauldron, player, InteractionHand.MAIN_HAND, new ItemStack(emptyIron));
        helper.assertTrue(
                player.getMainHandItem().is(lavaIron),
                "an empty bucket must fill from a lava cauldron");
        helper.assertTrue(level.getBlockState(cauldron).is(Blocks.CAULDRON), "the cauldron empties into the lava bucket");

        CauldronInteractions.EMPTY.get(player.getMainHandItem())
                .interact(level.getBlockState(cauldron), level, cauldron, player, InteractionHand.MAIN_HAND, player.getMainHandItem());
        helper.assertTrue(
                level.getBlockState(cauldron).is(Blocks.LAVA_CAULDRON),
                "a lava bucket must refill the empty cauldron");
        helper.assertTrue(player.getMainHandItem().is(emptyIron), "the lava bucket comes back empty");

        ModCompletionGameTests.removePlayer(player);
        helper.succeed();
    }

    /** InfX ItemBrick#onItemRightClick: brick throws a projectile and consumes one. */
    private static void brickThrow(GameTestHelper helper) {
        ServerPlayer player = ModCompletionGameTests.createPlayer(helper);
        ServerLevel level = helper.getLevel();
        ItemStack brick = new ItemStack(Items.BRICK, 5);
        player.setItemInHand(InteractionHand.MAIN_HAND, brick);
        player.gameMode.useItem(player, level, brick, InteractionHand.MAIN_HAND);
        helper.assertTrue(brick.getCount() == 4, "throwing a brick must consume one");
        helper.assertTrue(
                level.getEntitiesOfClass(
                                InfxBrickProjectile.class, new AABB(player.blockPosition()).inflate(8.0))
                        .stream()
                        .anyMatch(projectile -> projectile.getOwner() == player),
                "throwing a brick must spawn the brick projectile");
        ModCompletionGameTests.removePlayer(player);
        helper.succeed();
    }

    /** 26.2 blue and brown eggs follow the INFX egg profile. */
    private static void coloredEggs(GameTestHelper helper) {
        ServerPlayer player = ModCompletionGameTests.createPlayer(helper);
        FoodProfile egg = FoodProfiles.forStack(Items.EGG.getDefaultInstance());
        for (Item eggItem : List.of(Items.BLUE_EGG, Items.BROWN_EGG)) {
            ItemStack stack = eggItem.getDefaultInstance();
            helper.assertTrue(stack.get(DataComponents.CONSUMABLE) != null, eggItem + " must be consumable");
            helper.assertTrue(stack.get(DataComponents.FOOD) != null, eggItem + " must have a food value");
            FoodProfile profile = FoodProfiles.forStack(stack);
            helper.assertTrue(
                    profile.satiation() == egg.satiation()
                            && profile.nutrition() == egg.nutrition()
                            && profile.protein() == egg.protein(),
                    eggItem + " must use the egg's InfX food profile");
        }
        ModCompletionGameTests.removePlayer(player);
        helper.succeed();
    }

    /** INFX leaves: arrows pass straight through the leaves wall and stop in the stone wall. */
    private static void leavesProjectilePass(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // Self-contained floor (the test template itself is empty).
        for (int x = 0; x <= 11; x++) {
            for (int z = 1; z <= 3; z++) {
                level.setBlock(helper.absolutePos(new BlockPos(x, 1, z)), Blocks.STONE.defaultBlockState(), 3);
            }
        }
        // Leaves wall, then a stone wall further along +X as the control.
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        BlockPos leaf1 = helper.absolutePos(new BlockPos(4, 2, 2));
        BlockPos leaf2 = helper.absolutePos(new BlockPos(5, 2, 2));
        BlockPos stone1 = helper.absolutePos(new BlockPos(9, 2, 2));
        BlockPos stone2 = helper.absolutePos(new BlockPos(10, 2, 2));
        level.setBlock(leaf1, leaves, 3);
        level.setBlock(leaf2, leaves, 3);
        level.setBlock(stone1, Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(stone2, Blocks.STONE.defaultBlockState(), 3);
        // Force-load the chunks so BlockCollisions (which silently skips unloaded chunks) sees every block.
        level.getChunk(helper.absolutePos(new BlockPos(0, 1, 1)));
        level.getChunk(helper.absolutePos(new BlockPos(11, 1, 3)));

        // Entity-aware collision shapes: leaves are empty for an arrow, stone is not, and
        // non-entity queries keep the normal full leaves shape.
        Arrow probe = new Arrow(level, 0, 0, 0, Items.ARROW.getDefaultInstance(), null);
        helper.assertTrue(
                level.getBlockState(leaf1).getCollisionShape(level, leaf1, CollisionContext.of(probe)).isEmpty(),
                "leaves must not collide with an arrow");
        helper.assertTrue(
                !level.getBlockState(stone1).getCollisionShape(level, stone1, CollisionContext.of(probe)).isEmpty(),
                "stone must still collide with an arrow");
        helper.assertTrue(
                !level.getBlockState(leaf1).getCollisionShape(level, leaf1, CollisionContext.empty()).isEmpty(),
                "leaves must keep their normal collision shape for non-entity queries");

        // Deterministic flight: tick the arrow manually so the test does not depend on chunk
        // ticking. It must pass both leaves blocks and stop inside the stone wall.
        Vec3 spawn = Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 2, 2)));
        Arrow arrow = new Arrow(level, 0, 0, 0, Items.ARROW.getDefaultInstance(), null);
        arrow.snapTo(spawn.x, spawn.y, spawn.z, 0.0F, 0.0F);
        arrow.shoot(1.0, 0.0, 0.0, 3.0F, 0.0F);
        for (int i = 0; i < 40; i++) {
            arrow.tick();
        }
        int x = arrow.blockPosition().getX();
        helper.assertTrue(
                x >= stone1.getX() - 1 && x <= stone2.getX(),
                "the arrow must pass through the leaves wall and stop in the stone wall, but its block X is " + x);
        helper.succeed();
    }

    /** INFX leaves: dropped items fall through the leaves pad and rest on the floor below. */
    private static void leavesItemPass(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // Full-width floor so a drifting item can never fall out of the test area. The leaves
        // pad sits at rel x 5..8: the other test in this batch places its walls at rel x 3..4
        // (from this origin), so no foreign stone block can sit under the pad.
        for (int x = 0; x <= 15; x++) {
            for (int z = 0; z <= 5; z++) {
                level.setBlock(helper.absolutePos(new BlockPos(x, 1, z)), Blocks.STONE.defaultBlockState(), 3);
            }
        }
        // 4x4 leaves pad, wider than the small random horizontal drift item entities pick up
        // while falling, so the item stays over it until it has passed through.
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int x = 5; x <= 8; x++) {
            for (int z = 1; z <= 4; z++) {
                level.setBlock(helper.absolutePos(new BlockPos(x, 3, z)), leaves, 3);
            }
        }
        BlockPos leafPad = helper.absolutePos(new BlockPos(6, 3, 2));
        BlockPos stonePad = helper.absolutePos(new BlockPos(12, 3, 2));
        level.setBlock(stonePad, Blocks.STONE.defaultBlockState(), 3);
        // Force-load the chunks so BlockCollisions (which silently skips unloaded chunks) sees every block.
        level.getChunk(helper.absolutePos(new BlockPos(0, 1, 0)));
        level.getChunk(helper.absolutePos(new BlockPos(15, 1, 5)));

        // Entity-aware collision shapes: leaves are empty for a dropped item, stone is not.
        ItemEntity probe = new ItemEntity(level, 0, 0, 0, Items.APPLE.getDefaultInstance());
        helper.assertTrue(
                level.getBlockState(leafPad).getCollisionShape(level, leafPad, CollisionContext.of(probe)).isEmpty(),
                "leaves must not collide with a dropped item");
        helper.assertTrue(
                !level.getBlockState(stonePad).getCollisionShape(level, stonePad, CollisionContext.of(probe)).isEmpty(),
                "stone must still collide with a dropped item");

        // Deterministic fall: tick the item manually so the test does not depend on chunk ticking.
        Vec3 overLeaves = Vec3.atCenterOf(helper.absolutePos(new BlockPos(6, 5, 2)));
        ItemEntity item = new ItemEntity(level, 0, 0, 0, Items.APPLE.getDefaultInstance());
        item.snapTo(overLeaves.x, overLeaves.y, overLeaves.z, 0.0F, 0.0F);
        for (int i = 0; i < 120; i++) {
            item.tick();
        }
        double itemY = item.getY();
        helper.assertTrue(
                itemY < leafPad.getY(),
                "the item must fall through the leaves pad and reach the floor, but it rests at y=" + itemY);
        helper.succeed();
    }

    /** InfX EntitySheep: a gelatinous sphere hit instantly corrodes the wool. */
    private static void sphereSheep(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var sheep = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_SHEEP.get(), new BlockPos(2, 2, 2));
        GelatinousSphere sphere = new GelatinousSphere(
                level,
                sheep.getX(),
                sheep.getY(),
                sheep.getZ(),
                new ItemStack(InfXItems.BLACK_GELATINOUS_SPHERE.get()));
        level.addFreshEntity(sphere);
        helper.assertTrue(
                sheep.hurtServer(level, level.damageSources().thrown(sphere, null), 1.0F),
                "the sphere hit must damage the sheep");
        helper.assertTrue(sheep.isSheared(), "the black sphere must corrode the sheep's wool");
        helper.succeed();
    }

    /** MITE moon brightness feeds regional difficulty: blood 0.6, ordinary full moon 1.25. */
    private static void moonBrightness(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var overworldClock = level.registryAccess().get(WorldClocks.OVERWORLD).orElseThrow();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        level.clockManager().setTotalTicks(overworldClock, 744_000L); // blood moon day 32
        helper.assertTrue(
                Math.abs(level.getMoonBrightness(pos) - 0.6F) < 1.0E-5F,
                "blood moons must dim the regional-difficulty moon brightness to 0.6");
        level.clockManager().setTotalTicks(overworldClock, 168_000L); // full moon day 8
        helper.assertTrue(
                Math.abs(level.getMoonBrightness(pos) - 1.25F) < 1.0E-5F,
                "ordinary full moons must use phase factor × 0.5 + 0.75 = 1.25");
        helper.succeed();
    }

    /** MITE caps hostile mobs at 50 per player instead of vanilla's 70. */
    private static void monsterCap(GameTestHelper helper) {
        helper.assertTrue(
                MobCategory.MONSTER.getMaxInstancesPerChunk() == 50,
                "MONSTER must cap at 50 per player");
        helper.assertTrue(
                MobCategory.CREATURE.getMaxInstancesPerChunk() == 10,
                "passive categories must keep their vanilla cap");
        helper.succeed();
    }
}
