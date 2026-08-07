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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
            "infx_cauldron_vessels",
            "infx_dire_wolf_sit",
            "infx_dire_wolf_sit_mid_follow",
            "infx_dire_wolf_sit_ignores_generic_rules",
            "infx_brick_throw",
            "infx_colored_eggs",
            "infx_sphere_sheep");

    static {
        FUNCTIONS.register("infx_explosion_drops", () -> ModMechanicsGameTests::explosionDrops);
        FUNCTIONS.register("infx_cauldron_vessels", () -> ModMechanicsGameTests::cauldronVessels);
        FUNCTIONS.register("infx_dire_wolf_sit", () -> ModMechanicsGameTests::direWolfSit);
        FUNCTIONS.register("infx_dire_wolf_sit_mid_follow", () -> ModMechanicsGameTests::direWolfSitMidFollow);
        FUNCTIONS.register("infx_dire_wolf_sit_ignores_generic_rules", () -> ModMechanicsGameTests::direWolfSitIgnoresGenericMonsterRules);
        FUNCTIONS.register("infx_brick_throw", () -> ModMechanicsGameTests::brickThrow);
        FUNCTIONS.register("infx_colored_eggs", () -> ModMechanicsGameTests::coloredEggs);
        FUNCTIONS.register("infx_sphere_sheep", () -> ModMechanicsGameTests::sphereSheep);
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
}
