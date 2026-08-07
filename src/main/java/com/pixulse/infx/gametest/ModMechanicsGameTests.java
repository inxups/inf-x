package com.pixulse.infx.gametest;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.data.food.FoodProfile;
import com.pixulse.infx.data.food.FoodProfiles;
import com.pixulse.infx.entity.GelatinousSphere;
import com.pixulse.infx.entity.InfxBrickProjectile;
import com.pixulse.infx.entity.InfxSheep;
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
            "infx_brick_throw",
            "infx_colored_eggs",
            "infx_sphere_sheep");

    static {
        FUNCTIONS.register("infx_explosion_drops", () -> ModMechanicsGameTests::explosionDrops);
        FUNCTIONS.register("infx_cauldron_vessels", () -> ModMechanicsGameTests::cauldronVessels);
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
