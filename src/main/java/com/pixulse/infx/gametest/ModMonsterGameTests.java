package com.pixulse.infx.gametest;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.entity.R196Mob;
import com.pixulse.infx.entity.R196MonsterTactics;
import com.pixulse.infx.entity.R196MonsterEvents;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.registry.ModEntityTypes;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Runtime coverage for roster construction and vanilla natural-spawn replacement. */
public final class ModMonsterGameTests {
    private static final String ROSTER = "r196_monster_roster";
    private static final String REPLACEMENT = "r196_monster_replacement";
    private static final String BEHAVIORS = "r196_monster_behaviors";
    private static final String TACTICS = "r196_monster_tactics";
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, InfiniteX.MOD_ID);

    static {
        FUNCTIONS.register(ROSTER, () -> ModMonsterGameTests::roster);
        FUNCTIONS.register(REPLACEMENT, () -> ModMonsterGameTests::replacement);
        FUNCTIONS.register(BEHAVIORS, () -> ModMonsterGameTests::behaviors);
        FUNCTIONS.register(TACTICS, () -> ModMonsterGameTests::tactics);
    }

    private ModMonsterGameTests() {}

    public static void register(IEventBus modBus) {
        FUNCTIONS.register(modBus);
        modBus.addListener(ModMonsterGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                InfiniteX.id("r196_monsters"), new TestEnvironmentDefinition.AllOf());
        for (String name : List.of(ROSTER, REPLACEMENT, BEHAVIORS, TACTICS)) {
            ResourceKey<Consumer<GameTestHelper>> function =
                    ResourceKey.create(Registries.TEST_FUNCTION, InfiniteX.id(name));
            event.registerTest(
                    function.identifier(),
                    new FunctionGameTestInstance(
                            function,
                            new TestData<>(
                                    environment,
                                    Identifier.withDefaultNamespace("empty"),
                                    200,
                                    0,
                                    true,
                                    Rotation.NONE)));
        }
    }

    private static void roster(GameTestHelper helper) {
        for (var holder : ModEntityTypes.ALL) {
            var entity = holder.get().create(helper.getLevel(), EntitySpawnReason.COMMAND);
            helper.assertTrue(entity instanceof R196Mob, holder.getId() + " must implement R196Mob");
            helper.assertTrue(
                    entity instanceof LivingEntity living && living.getMaxHealth() > 0.0F,
                    holder.getId() + " must have a registered positive max-health attribute");
            entity.discard();
        }
        helper.assertTrue(
                BuiltInRegistries.ENTITY_TYPE
                        .wrapAsHolder(ModEntityTypes.R196_ZOMBIE.get())
                        .is(EntityTypeTags.UNDEAD),
                "replacement zombies must retain vanilla undead semantics");
        helper.assertTrue(
                BuiltInRegistries.ENTITY_TYPE
                        .wrapAsHolder(ModEntityTypes.R196_SKELETON.get())
                        .is(EntityTypeTags.SKELETONS),
                "replacement skeletons must retain the vanilla skeleton family tag");
        helper.assertTrue(
                BuiltInRegistries.ENTITY_TYPE
                        .wrapAsHolder(ModEntityTypes.PHASE_SPIDER.get())
                        .is(EntityTypeTags.ARTHROPOD),
                "R196 spiders must retain arthropod semantics");
        helper.assertTrue(
                BuiltInRegistries.ENTITY_TYPE
                        .wrapAsHolder(ModEntityTypes.R196_SQUID.get())
                        .is(EntityTypeTags.AQUATIC),
                "replacement squid must retain aquatic semantics");
        helper.succeed();
    }

    private static void replacement(GameTestHelper helper) {
        BlockPos naturalPos = new BlockPos(2, 2, 2);
        BlockPos explicitPos = new BlockPos(5, 2, 2);
        helper.spawn(EntityTypes.ZOMBIE, naturalPos, EntitySpawnReason.NATURAL);
        var explicit = EntityTypes.ZOMBIE.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        Vec3 explicitLocation = helper.absoluteVec(Vec3.atBottomCenterOf(explicitPos));
        explicit.snapTo(explicitLocation.x, explicitLocation.y, explicitLocation.z, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(explicit);
        helper.assertEntityPresent(EntityTypes.ZOMBIE, explicitPos);
        helper.succeedWhen(() -> {
            helper.assertEntityNotPresent(EntityTypes.ZOMBIE, naturalPos);
            helper.assertEntityPresent(ModEntityTypes.R196_ZOMBIE.get(), naturalPos, 2.0);
            helper.assertEntityPresent(EntityTypes.ZOMBIE, explicitPos);
        });
    }

    private static void behaviors(GameTestHelper helper) {
        var level = helper.getLevel();
        var skeleton = helper.spawnWithNoFreeWill(ModEntityTypes.R196_SKELETON.get(), new BlockPos(1, 2, 1));
        var vanillaSkeleton = helper.spawnWithNoFreeWill(EntityTypes.SKELETON, new BlockPos(2, 2, 1));
        Arrow arrow = EntityTypes.ARROW.create(level, EntitySpawnReason.COMMAND);
        float before = skeleton.getHealth();
        helper.assertTrue(
                !skeleton.hurtServer(level, level.damageSources().arrow(arrow, vanillaSkeleton), 4.0F),
                "R196 skeletons must reject arrows fired by another skeleton");
        helper.assertTrue(skeleton.getHealth() == before, "skeleton arrows must not reduce R196 skeleton health");

        var blaze = helper.spawnWithNoFreeWill(ModEntityTypes.R196_BLAZE.get(), new BlockPos(3, 2, 1));
        before = blaze.getHealth();
        helper.assertTrue(
                !blaze.hurtServer(level, level.damageSources().mobAttack(vanillaSkeleton), 4.0F),
                "mundane unenchanted attacks must not hurt the R196 blaze");
        helper.assertTrue(blaze.getHealth() == before, "rejected blaze damage must not change health");
        Snowball snowball = new Snowball(level, vanillaSkeleton, Items.SNOWBALL.getDefaultInstance());
        helper.assertTrue(
                blaze.hurtServer(level, level.damageSources().thrown(snowball, vanillaSkeleton), 3.0F),
                "snowballs must hurt the R196 blaze");
        helper.assertTrue(blaze.getHealth() == before - 3.0F, "snowballs must deal three damage to the R196 blaze");

        var infernal = helper.spawnWithNoFreeWill(ModEntityTypes.INFERNAL_CREEPER.get(), new BlockPos(1, 2, 4));
        var cow = helper.spawnWithNoFreeWill(EntityTypes.COW, new BlockPos(9, 2, 4));
        before = cow.getHealth();
        level.explode(
                infernal,
                infernal.getX(),
                infernal.getY(),
                infernal.getZ(),
                3.0F,
                Level.ExplosionInteraction.MOB);
        helper.assertTrue(cow.getHealth() < before, "infernal creeper explosions must use the amplified six-block radius");
        helper.succeed();
    }

    private static void tactics(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = ModR196CompletionGameTests.createPlayer(helper);
        Vec3 playerPos = helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(7, 2, 7)));
        player.snapTo(playerPos.x, playerPos.y, playerPos.z, 0.0F, 0.0F);

        var leader = helper.spawn(
                ModEntityTypes.R196_ZOMBIE.get(), new BlockPos(2, 2, 2));
        var ally = helper.spawn(
                ModEntityTypes.R196_SKELETON.get(), new BlockPos(3, 2, 2));
        leader.setTarget(player);
        R196MonsterEvents.propagateTarget(level, leader, player);
        helper.assertTrue(ally.getTarget() == player, "R196 monsters must share a newly acquired player target");
        leader.setTarget(null);
        ally.setTarget(null);
        level.gameEvent(GameEvent.BLOCK_DESTROY, player.position(), GameEvent.Context.of(player));
        helper.assertTrue(leader.getTarget() == player, "player block noise must attract nearby monsters");
        helper.assertTrue(ally.getTarget() == player, "activity attraction applies across hostile families");

        var digger = helper.spawnWithNoFreeWill(
                ModEntityTypes.R196_ZOMBIE.get(), new BlockPos(2, 2, 7));
        digger.setItemSlot(
                net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                ModItems.catalog()
                        .equipment(R196Material.IRON, R196EquipmentType.PICKAXE)
                        .holder()
                        .toStack());
        digger.setTarget(player);
        BlockPos wall = new BlockPos(4, 3, 7);
        helper.setBlock(wall, Blocks.STONE);
        for (int attempt = 0; attempt < 30 && !helper.getBlockState(wall).isAir(); attempt++) {
            R196MonsterTactics.tryDig(level, digger);
        }
        helper.assertTrue(helper.getBlockState(wall).isAir(), "tool-equipped blocked monster must mine through stone");
        ModR196CompletionGameTests.removePlayer(player);
        helper.succeed();
    }
}
