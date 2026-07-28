package com.pixulse.infx.gametest;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.effect.curse.CurseData;
import com.pixulse.infx.entity.*;
import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.world.RiverBiomes;
import com.pixulse.infx.world.Underworld;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Runtime coverage for roster construction and vanilla natural-spawn replacement. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class ModMonsterGameTests {
    private static final float DAMAGE_EPSILON = 0.001F;
    private static final String ROSTER = "r196_monster_roster";
    private static final String ATTRIBUTES = "r196_monster_attributes";
    private static final String REPLACEMENT = "r196_monster_replacement";
    private static final String BEHAVIORS = "r196_monster_behaviors";
    private static final String NETHERSPAWN = "r196_netherspawn_mechanics";
    private static final String ENDERMAN = "r196_enderman";
    private static final String WITCH_CURSE = "r196_witch_curse";
    private static final String TACTICS = "r196_monster_tactics";
    private static final String SPAWNS = "r196_spawn_tables";
    private static final String ATTACK_RANGES = "r196_attack_ranges";
    private static final String RANGED_ATTACK_RANGES = "r196_ranged_attack_ranges";
    private static final String EXPLOSION_RANGES = "r196_explosion_ranges";
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, InfiniteX.MOD_ID);

    static {
        FUNCTIONS.register(ROSTER, () -> ModMonsterGameTests::roster);
        FUNCTIONS.register(ATTRIBUTES, () -> ModMonsterGameTests::attributes);
        FUNCTIONS.register(REPLACEMENT, () -> ModMonsterGameTests::replacement);
        FUNCTIONS.register(BEHAVIORS, () -> ModMonsterGameTests::behaviors);
        FUNCTIONS.register(NETHERSPAWN, () -> ModMonsterGameTests::netherspawnMechanics);
        FUNCTIONS.register(ENDERMAN, () -> ModMonsterGameTests::enderman);
        FUNCTIONS.register(WITCH_CURSE, () -> ModMonsterGameTests::witchCurse);
        FUNCTIONS.register(TACTICS, () -> ModMonsterGameTests::tactics);
        FUNCTIONS.register(SPAWNS, () -> ModMonsterGameTests::spawnTables);
        FUNCTIONS.register(ATTACK_RANGES, () -> ModMonsterGameTests::attackRanges);
        FUNCTIONS.register(RANGED_ATTACK_RANGES, () -> ModMonsterGameTests::rangedAttackRanges);
        FUNCTIONS.register(EXPLOSION_RANGES, () -> ModMonsterGameTests::explosionRanges);
    }

    private ModMonsterGameTests() {}

    public static void register(IEventBus modBus) {
        FUNCTIONS.register(modBus);
    }

    @SubscribeEvent

    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                InfiniteX.id("r196_monsters"), new TestEnvironmentDefinition.AllOf());
        Holder<TestEnvironmentDefinition<?>> rangedEnvironment = event.registerEnvironment(
                InfiniteX.id("r196_ranged_combat"), new TestEnvironmentDefinition.AllOf());
        for (String name : List.of(
                ROSTER,
                ATTRIBUTES,
                REPLACEMENT,
                BEHAVIORS,
                NETHERSPAWN,
                ENDERMAN,
                WITCH_CURSE,
                TACTICS,
                SPAWNS,
                ATTACK_RANGES,
                RANGED_ATTACK_RANGES,
                EXPLOSION_RANGES)) {
            ResourceKey<Consumer<GameTestHelper>> function =
                    ResourceKey.create(Registries.TEST_FUNCTION, InfiniteX.id(name));
            event.registerTest(
                    function.identifier(),
                    new FunctionGameTestInstance(
                            function,
                            new TestData<>(
                                    name.equals(RANGED_ATTACK_RANGES) ? rangedEnvironment : environment,
                                    Identifier.withDefaultNamespace("empty"),
                                    name.equals(RANGED_ATTACK_RANGES) ? 500 : name.equals(WITCH_CURSE) ? 400 : 200,
                                    0,
                                    true,
                                    Rotation.NONE)));
        }
    }

    private static void roster(GameTestHelper helper) {
        var passiveReplacements = Set.of(
                InfXEntityTypes.R196_COW,
                InfXEntityTypes.R196_CHICKEN,
                InfXEntityTypes.R196_SHEEP,
                InfXEntityTypes.R196_PIG,
                InfXEntityTypes.R196_HORSE,
                InfXEntityTypes.R196_OCELOT,
                InfXEntityTypes.R196_WOLF);
        for (var holder : InfXEntityTypes.ALL) {
            var entity = holder.get().create(helper.getLevel(), EntitySpawnReason.COMMAND);
            if (!passiveReplacements.contains(holder)) {
                helper.assertTrue(entity instanceof MiteMob, holder.getId() + " must implement MiteMob");
            }
            helper.assertTrue(
                    entity instanceof LivingEntity living && living.getMaxHealth() > 0.0F,
                    holder.getId() + " must have a registered positive max-health attribute");
            entity.discard();
        }
        helper.assertTrue(
                BuiltInRegistries.ENTITY_TYPE
                        .wrapAsHolder(InfXEntityTypes.R196_ZOMBIE.get())
                        .is(EntityTypeTags.UNDEAD),
                "replacement zombies must retain vanilla undead semantics");
        helper.assertTrue(
                BuiltInRegistries.ENTITY_TYPE
                        .wrapAsHolder(InfXEntityTypes.R196_SKELETON.get())
                        .is(EntityTypeTags.SKELETONS),
                "replacement skeletons must retain the vanilla skeleton family tag");
        helper.assertTrue(
                BuiltInRegistries.ENTITY_TYPE
                        .wrapAsHolder(InfXEntityTypes.PHASE_SPIDER.get())
                        .is(EntityTypeTags.ARTHROPOD),
                "R196 spiders must retain arthropod semantics");
        helper.assertTrue(
                BuiltInRegistries.ENTITY_TYPE
                        .wrapAsHolder(InfXEntityTypes.R196_SQUID.get())
                        .is(EntityTypeTags.AQUATIC),
                "replacement squid must retain aquatic semantics");
        helper.assertTrue(
                InfXEntityTypes.R196_SQUID.get().getCategory() == MobCategory.WATER_CREATURE
                        && InfXEntityTypes.R196_SQUID.get().isAllowedInPeaceful(),
                "replacement squid must use the peaceful water-creature cap");
        for (var type : List.of(
                InfXEntityTypes.R196_COD,
                InfXEntityTypes.R196_SALMON,
                InfXEntityTypes.R196_PUFFERFISH,
                InfXEntityTypes.R196_TROPICAL_FISH)) {
            helper.assertTrue(
                    BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type.get()).is(EntityTypeTags.AQUATIC)
                            && BuiltInRegistries.ENTITY_TYPE
                                    .wrapAsHolder(type.get())
                                    .is(EntityTypeTags.AXOLOTL_HUNT_TARGETS)
                            && BuiltInRegistries.ENTITY_TYPE
                                    .wrapAsHolder(type.get())
                                    .is(EntityTypeTags.NOT_SCARY_FOR_PUFFERFISH)
                            && type.get().getCategory() == MobCategory.WATER_AMBIENT
                            && type.get().isAllowedInPeaceful(),
                    type.getId() + " must retain vanilla aquatic interaction semantics");
        }
        helper.assertTrue(
                InfXEntityTypes.VAMPIRE_BAT.get().getCategory() == MobCategory.AMBIENT
                        && InfXEntityTypes.NIGHTWING.get().getCategory() == MobCategory.AMBIENT,
                "hostile cave bats must retain the R196 ambient spawn pool");
        helper.assertTrue(
                InfXEntityTypes.DIRE_WOLF.get().getCategory() == MobCategory.CREATURE
                        && InfXEntityTypes.DIRE_WOLF.get().isAllowedInPeaceful(),
                "dire wolves must use animal spawning rather than the hostile cap");
        helper.succeed();
    }

    private static void attributes(GameTestHelper helper) {
        int zombieX = 1;
        for (var type : List.of(
                InfXEntityTypes.R196_ZOMBIE,
                InfXEntityTypes.INVISIBLE_STALKER,
                InfXEntityTypes.GHOUL,
                InfXEntityTypes.SHADOW,
                InfXEntityTypes.WIGHT,
                InfXEntityTypes.REVENANT)) {
            var zombie = helper.spawnWithNoFreeWill(type.get(), new BlockPos(zombieX++, 2, 1));
            helper.assertTrue(
                    zombie.getAttributeBaseValue(Attributes.ARMOR) == 0.0D,
                    type.getId() + " must not inherit modern zombie armor");
        }
        var stalker = helper.spawnWithNoFreeWill(InfXEntityTypes.INVISIBLE_STALKER.get(), new BlockPos(10, 2, 1));
        helper.assertTrue(
                stalker.canBreakDoors() && !stalker.isInvisible() && !stalker.canPickUpLoot(),
                "invisible stalkers must break doors without inheriting vanilla invisibility or zombie loot pickup");
        var piglin = helper.spawn(InfXEntityTypes.R196_ZOMBIFIED_PIGLIN.get(), new BlockPos(8, 2, 1));
        helper.assertTrue(
                piglin.getAttributeBaseValue(Attributes.ARMOR) == 0.0D,
                "R196 zombified piglins must not inherit modern zombie armor");
        helper.assertTrue(
                piglin.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == 0.23D,
                "R196 zombified piglins must use the normalized modern base movement speed");
        helper.assertTrue(
                !piglin.canBreakDoors() && !piglin.canPickUpLoot(),
                "R196 zombified piglins must not inherit modern zombie door breaking or item pickup");
        var piglinTarget = ModCompletionGameTests.createPlayer(helper);
        piglin.setTarget(piglinTarget);
        // The replacement adjusts its modifier from customServerAiStep, which is
        // invoked by the real entity tick rather than by the client-side aiStep hook.
        piglin.tick();
        helper.assertTrue(
                Math.abs(piglin.getAttributeValue(Attributes.MOVEMENT_SPEED) - 0.28D) < DAMAGE_EPSILON,
                "R196 zombified piglins must use the normalized +0.05 chase-speed bonus");
        var enderman = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_ENDERMAN.get(), new BlockPos(7, 2, 1));
        helper.assertTrue(
                enderman.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == 0.30D,
                "R196 endermen must retain their 0.30 standing movement speed");
        enderman.setTarget(piglinTarget);
        helper.assertTrue(
                Math.abs(enderman.getAttributeValue(Attributes.MOVEMENT_SPEED) - 6.50D) < DAMAGE_EPSILON,
                "R196 endermen must retain MITE's 6.50 chase-speed total");
        enderman.setTarget(null);
        helper.assertTrue(
                Math.abs(enderman.getAttributeValue(Attributes.MOVEMENT_SPEED) - 0.30D) < DAMAGE_EPSILON,
                "R196 endermen must remove the chase-speed modifier without a target");
        piglin.setTarget(null);
        ModCompletionGameTests.removePlayer(piglinTarget);
        piglin.tick();
        helper.assertTrue(
                Math.abs(piglin.getAttributeValue(Attributes.MOVEMENT_SPEED) - 0.23D) < DAMAGE_EPSILON,
                "R196 zombified piglins must remove the chase-speed modifier without a target");
        var blaze = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_BLAZE.get(), new BlockPos(9, 2, 1));
        helper.assertTrue(
                Math.abs(blaze.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) - 0.23D) < DAMAGE_EPSILON,
                "R196 blazes must retain the modern baseline movement speed");

        int spiderX = 1;
        for (var type : List.of(
                InfXEntityTypes.R196_SPIDER,
                InfXEntityTypes.R196_CAVE_SPIDER,
                InfXEntityTypes.BLACK_WIDOW_SPIDER,
                InfXEntityTypes.DEMON_SPIDER,
                InfXEntityTypes.WOOD_SPIDER,
                InfXEntityTypes.PHASE_SPIDER)) {
            var spider = helper.spawnWithNoFreeWill(type.get(), new BlockPos(spiderX++, 2, 2));
            double expectedSpeed = type == InfXEntityTypes.R196_SPIDER
                            || type == InfXEntityTypes.R196_CAVE_SPIDER
                            || type == InfXEntityTypes.DEMON_SPIDER
                    ? 0.375D
                    : 0.30D;
            helper.assertTrue(
                    spider.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == expectedSpeed,
                    type.getId() + " must retain its normalized arachnid movement speed");
        }

        int silverfishX = 1;
        for (var type : List.of(
                InfXEntityTypes.NETHERSPAWN,
                InfXEntityTypes.COPPERSPINE,
                InfXEntityTypes.HOARY_SILVERFISH)) {
            var silverfish = helper.spawnWithNoFreeWill(type.get(), new BlockPos(silverfishX++, 2, 3));
            helper.assertTrue(
                    silverfish.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == 0.25D,
                    type.getId() + " must retain the modern silverfish movement baseline");
        }

        int slimeX = 1;
        for (var type : List.of(
                InfXEntityTypes.R196_SLIME,
                InfXEntityTypes.JELLY,
                InfXEntityTypes.BLOB,
                InfXEntityTypes.OOZE,
                InfXEntityTypes.PUDDING)) {
            var slime = helper.spawnWithNoFreeWill(type.get(), new BlockPos(slimeX++, 2, 4));
            for (int size : List.of(1, 2, 4)) {
                slime.setSize(size, true);
                double expectedSpeed = slime.variant() == MiteSlime.Variant.OOZE
                        ? 0.05D
                        : 0.20D + 0.10D * slime.getSize();
                helper.assertTrue(
                        slime.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == expectedSpeed,
                        type.getId() + " must retain its expected movement speed after size " + slime.getSize());
            }
        }
        var magmaCube = helper.spawnWithNoFreeWill(InfXEntityTypes.MAGMA_CUBE.get(), new BlockPos(7, 2, 4));
        for (int size : List.of(1, 4)) {
            magmaCube.setSize(size, true);
            helper.assertTrue(
                    magmaCube.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == 0.20D,
                    "R196 magma cubes must retain 0.20 movement speed after size " + size);
            helper.assertTrue(
                    magmaCube.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) == size * 2.0D,
                    "R196 magma cubes must retain two damage per size");
            helper.assertTrue(
                    magmaCube.getAttributeBaseValue(Attributes.ARMOR) == size * 2.0D,
                    "R196 magma cubes must retain two defense per size");
        }

        var level = helper.getLevel();
        ItemEntity magicOwner = new ItemEntity(level, 0.0, 0.0, 0.0, Items.SNOWBALL.getDefaultInstance());
        Snowball magicProjectile = new Snowball(level, 0.0, 0.0, 0.0, Items.SNOWBALL.getDefaultInstance());
        var directMagic = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_WITCH.get(), new BlockPos(1, 2, 8));
        float before = directMagic.getHealth();
        helper.assertTrue(
                directMagic.hurtServer(level, level.damageSources().magic(), 20.0F),
                "direct magic must damage the R196 witch");
        helper.assertTrue(
                Math.abs(directMagic.getHealth() - (before - 20.0F)) < DAMAGE_EPSILON,
                "direct magic must not receive the R196 witch's indirect-magic defense");

        var indirectNonMagic = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_WITCH.get(), new BlockPos(4, 2, 8));
        before = indirectNonMagic.getHealth();
        helper.assertTrue(
                indirectNonMagic.hurtServer(level, level.damageSources().thrown(magicProjectile, magicOwner), 20.0F),
                "indirect non-magic damage must damage the R196 witch");
        helper.assertTrue(
                Math.abs(indirectNonMagic.getHealth() - (before - 20.0F)) < DAMAGE_EPSILON,
                "indirect non-magic damage must not receive the R196 witch's defense");

        var indirectMagic = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_WITCH.get(), new BlockPos(7, 2, 8));
        before = indirectMagic.getHealth();
        helper.assertTrue(
                indirectMagic.hurtServer(level, level.damageSources().indirectMagic(magicProjectile, magicOwner), 20.0F),
                "indirect magic must damage the R196 witch");
        helper.assertTrue(
                Math.abs(indirectMagic.getHealth() - (before - 10.0F)) < DAMAGE_EPSILON,
                "indirect magic must receive exactly ten points of R196 witch defense");
        helper.succeed();
    }

    private static void replacement(GameTestHelper helper) {
        BlockPos naturalPos = new BlockPos(2, 2, 2);
        BlockPos explicitPos = new BlockPos(5, 2, 2);
        BlockPos explicitWitchPos = new BlockPos(5, 2, 5);
        BlockPos convertedVillagerPos = new BlockPos(8, 2, 5);
        helper.spawn(EntityType.ZOMBIE, naturalPos.getX(), naturalPos.getY(), naturalPos.getZ(), EntitySpawnReason.NATURAL);
        BlockPos triggeredPos = new BlockPos(8, 2, 2);
        helper.setBlock(triggeredPos.east(), Blocks.COPPER_ORE);
        helper.spawn(EntityType.SILVERFISH, triggeredPos.getX(), triggeredPos.getY(), triggeredPos.getZ(), EntitySpawnReason.TRIGGERED);
        var explicit = EntityType.ZOMBIE.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        Vec3 explicitLocation = helper.absoluteVec(Vec3.atBottomCenterOf(explicitPos));
        explicit.snapTo(explicitLocation.x, explicitLocation.y, explicitLocation.z, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(explicit);
        var explicitWitch = EntityType.WITCH.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        Vec3 explicitWitchLocation = helper.absoluteVec(Vec3.atBottomCenterOf(explicitWitchPos));
        explicitWitch.snapTo(
                explicitWitchLocation.x, explicitWitchLocation.y, explicitWitchLocation.z, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(explicitWitch);
        var converter = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_ZOMBIE.get(), convertedVillagerPos.south());
        var villager = helper.spawn(EntityType.VILLAGER, convertedVillagerPos);
        helper.assertTrue(
                converter.convertVillagerToZombieVillager(helper.getLevel(), villager),
                "R196 zombies must convert villagers into the R196 zombie type");
        helper.startSequence()
                // Replacement insertion is deliberately scheduled after the
                // vanilla join event, so the test must not inspect tick-zero's
                // pre-transaction entity index.
                .thenWaitUntil(() -> {
                    helper.assertEntityPresent(InfXEntityTypes.R196_ZOMBIE.get(), naturalPos, 2.0D);
                    helper.assertEntityPresent(EntityType.ZOMBIE, explicitPos);
                    helper.assertEntityPresent(InfXEntityTypes.COPPERSPINE.get(), triggeredPos, 2.0D);
                    helper.assertEntityPresent(InfXEntityTypes.R196_WITCH.get(), explicitWitchPos, 2.0D);
                    helper.assertEntityPresent(InfXEntityTypes.R196_ZOMBIE.get(), convertedVillagerPos, 2.0D);
                })
                .thenExecute(() -> {
                    helper.assertEntityNotPresent(EntityType.ZOMBIE, naturalPos);
                    helper.assertEntityNotPresent(EntityType.SILVERFISH, triggeredPos);
                    helper.assertEntityNotPresent(EntityType.WITCH, explicitWitchPos);
                    helper.assertEntityNotPresent(EntityType.ZOMBIE_VILLAGER, convertedVillagerPos);
                    Vec3 replacementPosition = helper.absoluteVec(Vec3.atBottomCenterOf(naturalPos));
                    var replacement = helper.getLevel()
                            .getEntitiesOfClass(
                                    MiteZombie.class,
                                    new AABB(replacementPosition, replacementPosition).inflate(2.0D),
                                    entity -> entity.getType() == InfXEntityTypes.R196_ZOMBIE.get())
                            .getFirst();
                    helper.assertTrue(
                            replacement.getAttributeBaseValue(Attributes.FOLLOW_RANGE) == 40.0D,
                            "replacement initialization must retain the R196 follow range");
                    helper.assertTrue(
                            replacement.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) == 5.0D,
                            "replacement initialization must retain the R196 attack damage; actual="
                                    + replacement.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
                    Vec3 convertedVillagerPosition = helper.absoluteVec(Vec3.atBottomCenterOf(convertedVillagerPos));
                    helper.assertTrue(
                            helper.getLevel()
                                    .getEntitiesOfClass(
                                            MiteZombie.class,
                                            new AABB(convertedVillagerPosition, convertedVillagerPosition).inflate(2.0D),
                                            MiteZombie::isVillagerZombie)
                                    .size()
                                    == 1,
                            "villager conversions must retain the R196 villager-zombie marker");
                })
                .thenSucceed();
    }

    private static void spawnTables(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        MobSpawnSettings plains = biomes.getOrThrow(Biomes.PLAINS).value().getMobSettings();
        helper.assertTrue(
                Set.copyOf(spawnTypes(plains, MobCategory.CREATURE)).equals(Set.of(
                        InfXEntityTypes.R196_SHEEP.get(),
                        InfXEntityTypes.R196_PIG.get(),
                        InfXEntityTypes.R196_CHICKEN.get(),
                        InfXEntityTypes.R196_COW.get(),
                        InfXEntityTypes.R196_HORSE.get())),
                "plains must use only the R196 livestock and horse table");
        helper.assertTrue(
                !spawnTypes(plains, MobCategory.MONSTER).contains(EntityType.DROWNED)
                        && spawnTypes(plains, MobCategory.MONSTER).contains(InfXEntityTypes.GHOUL.get()),
                "Overworld monster tables must replace modern biome additions");
        for (var type : List.of(
                InfXEntityTypes.R196_COW,
                InfXEntityTypes.R196_CHICKEN,
                InfXEntityTypes.R196_SHEEP,
                InfXEntityTypes.R196_PIG,
                InfXEntityTypes.R196_HORSE,
                InfXEntityTypes.R196_WOLF)) {
            helper.assertTrue(
                    SpawnPlacements.getPlacementType(type.get()) == SpawnPlacementTypes.ON_GROUND
                            && SpawnPlacements.getHeightmapType(type.get())
                                    == Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    type.getId() + " must retain vanilla ground spawn placement restrictions");
        }
        helper.assertTrue(
                SpawnPlacements.getPlacementType(InfXEntityTypes.R196_OCELOT.get())
                                == SpawnPlacementTypes.ON_GROUND
                        && SpawnPlacements.getHeightmapType(InfXEntityTypes.R196_OCELOT.get())
                                == Heightmap.Types.MOTION_BLOCKING,
                "R196 ocelots must retain vanilla ocelot spawn placement restrictions");
        for (var type : List.of(
                InfXEntityTypes.R196_COD,
                InfXEntityTypes.R196_SALMON,
                InfXEntityTypes.R196_PUFFERFISH,
                InfXEntityTypes.R196_TROPICAL_FISH)) {
            helper.assertTrue(
                    SpawnPlacements.getPlacementType(type.get()) == SpawnPlacementTypes.IN_WATER
                            && SpawnPlacements.getHeightmapType(type.get())
                                    == Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    type.getId() + " must retain vanilla water spawn placement restrictions");
        }

        MobSpawnSettings ocean = biomes.getOrThrow(Biomes.OCEAN).value().getMobSettings();
        helper.assertTrue(
                spawnTypes(ocean, MobCategory.WATER_AMBIENT).equals(List.of(InfXEntityTypes.R196_COD.get())),
                "normal oceans must spawn only InfiniteX cod");
        MobSpawnSettings coldOcean = biomes.getOrThrow(Biomes.COLD_OCEAN).value().getMobSettings();
        helper.assertTrue(
                Set.copyOf(spawnTypes(coldOcean, MobCategory.WATER_AMBIENT)).equals(Set.of(
                        InfXEntityTypes.R196_COD.get(), InfXEntityTypes.R196_SALMON.get())),
                "cold oceans must replace cod and salmon with InfiniteX fish");
        MobSpawnSettings lukewarmOcean = biomes.getOrThrow(Biomes.LUKEWARM_OCEAN).value().getMobSettings();
        helper.assertTrue(
                Set.copyOf(spawnTypes(lukewarmOcean, MobCategory.WATER_AMBIENT)).equals(Set.of(
                        InfXEntityTypes.R196_COD.get(),
                        InfXEntityTypes.R196_PUFFERFISH.get(),
                        InfXEntityTypes.R196_TROPICAL_FISH.get())),
                "lukewarm oceans must use InfiniteX cod, pufferfish and tropical fish");
        MobSpawnSettings warmOcean = biomes.getOrThrow(Biomes.WARM_OCEAN).value().getMobSettings();
        helper.assertTrue(
                Set.copyOf(spawnTypes(warmOcean, MobCategory.WATER_AMBIENT)).equals(Set.of(
                        InfXEntityTypes.R196_PUFFERFISH.get(), InfXEntityTypes.R196_TROPICAL_FISH.get())),
                "warm oceans must use InfiniteX pufferfish and tropical fish");
        MobSpawnSettings river = biomes.getOrThrow(Biomes.RIVER).value().getMobSettings();
        helper.assertTrue(
                spawnTypes(river, MobCategory.WATER_AMBIENT).equals(List.of(InfXEntityTypes.R196_SALMON.get())),
                "rivers must use InfiniteX salmon");
        MobSpawnSettings lushCaves = biomes.getOrThrow(Biomes.LUSH_CAVES).value().getMobSettings();
        helper.assertTrue(
                spawnTypes(lushCaves, MobCategory.WATER_AMBIENT)
                        .equals(List.of(InfXEntityTypes.R196_TROPICAL_FISH.get())),
                "lush caves must use InfiniteX tropical fish");

        MobSpawnSettings mushroom = biomes.getOrThrow(Biomes.MUSHROOM_FIELDS).value().getMobSettings();
        helper.assertTrue(
                spawnTypes(mushroom, MobCategory.MONSTER).isEmpty()
                        && spawnTypes(mushroom, MobCategory.CREATURE).isEmpty()
                        && spawnTypes(mushroom, MobCategory.WATER_CREATURE).isEmpty(),
                "mushroom fields must remain free of monsters, animals and squid");
        helper.assertTrue(
                Set.copyOf(spawnTypes(mushroom, MobCategory.AMBIENT)).equals(Set.of(
                        EntityType.BAT,
                        InfXEntityTypes.VAMPIRE_BAT.get(),
                        InfXEntityTypes.NIGHTWING.get())),
                "mushroom fields retain the inherited R196 cave-bat pool");

        MobSpawnSettings jungle = biomes.getOrThrow(Biomes.JUNGLE).value().getMobSettings();
        helper.assertTrue(
                spawnTypes(jungle, MobCategory.MONSTER).contains(InfXEntityTypes.BLACK_WIDOW_SPIDER.get()),
                "jungle biomes must include black widow spiders");
        helper.assertTrue(
                spawnTypes(jungle, MobCategory.CREATURE).stream()
                                .filter(type -> type == InfXEntityTypes.R196_CHICKEN.get())
                                .count()
                        == 2,
                "jungle biomes must retain the additional R196 chicken entry");

        MobSpawnSettings jungleRiver = biomes.getOrThrow(RiverBiomes.JUNGLE_RIVER).value().getMobSettings();
        helper.assertTrue(
                spawnTypes(jungleRiver, MobCategory.CREATURE).isEmpty()
                        && !spawnTypes(jungleRiver, MobCategory.MONSTER)
                                .contains(InfXEntityTypes.BLACK_WIDOW_SPIDER.get()),
                "jungle rivers must use river rather than jungle animal overrides");

        MobSpawnSettings nether = biomes.getOrThrow(Biomes.NETHER_WASTES).value().getMobSettings();
        helper.assertTrue(
                Set.copyOf(spawnTypes(nether, MobCategory.MONSTER)).equals(Set.of(
                        EntityType.GHAST,
                        EntityType.ZOMBIFIED_PIGLIN,
                        EntityType.MAGMA_CUBE,
                        InfXEntityTypes.EARTH_ELEMENTAL.get()))
                        && spawnTypes(nether, MobCategory.CREATURE).isEmpty(),
                "Nether biomes must use the exact four-entry R196 pool");

        MobSpawnSettings end = biomes.getOrThrow(Biomes.END_HIGHLANDS).value().getMobSettings();
        helper.assertTrue(
                Set.copyOf(spawnTypes(end, MobCategory.MONSTER)).equals(Set.of(
                        EntityType.ENDERMAN, InfXEntityTypes.EARTH_ELEMENTAL.get())),
                "End biomes must use only endermen and earth elementals");

        MobSpawnSettings underworld = biomes.getOrThrow(Underworld.BIOME).value().getMobSettings();
        helper.assertTrue(
                spawnTypes(underworld, MobCategory.WATER_CREATURE).equals(List.of(EntityType.SQUID))
                        && spawnTypes(underworld, MobCategory.CREATURE).isEmpty(),
                "Underworld must retain aquatic spawning without blue-moon livestock");
        helper.assertTrue(
                spawnTypes(underworld, MobCategory.MONSTER).contains(InfXEntityTypes.CLAY_GOLEM.get()),
                "Underworld must retain MITE's clay-golem spawn entry");
        helper.succeed();
    }

    private static List<net.minecraft.world.entity.EntityType<?>> spawnTypes(
            MobSpawnSettings settings, MobCategory category) {
        return settings.getMobs(category).unwrap().stream()
                .<net.minecraft.world.entity.EntityType<?>>map(entry -> entry.value().type())
                .toList();
    }

    private static void assertEarthForm(
            GameTestHelper helper,
            EarthElemental elemental,
            net.minecraft.world.level.block.state.BlockState ground,
            boolean heated,
            EarthElemental.Form expected) {
        elemental.initializeMiteForm(ground, heated);
        helper.assertTrue(
                elemental.form() == expected && elemental.isMagma() == expected.isMagmaForm(),
                "earth elemental form must match " + expected);
    }

    private static void behaviors(GameTestHelper helper) {
        var level = helper.getLevel();
        var skeleton = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_SKELETON.get(), new BlockPos(1, 2, 1));
        var vanillaSkeleton = helper.spawnWithNoFreeWill(EntityType.SKELETON, new BlockPos(2, 2, 1));
        Arrow arrow = EntityType.ARROW.create(level, EntitySpawnReason.COMMAND);
        float before = skeleton.getHealth();
        helper.assertTrue(
                !skeleton.hurtServer(level, level.damageSources().arrow(arrow, vanillaSkeleton), 4.0F),
                "R196 skeletons must reject arrows fired by another skeleton");
        helper.assertTrue(skeleton.getHealth() == before, "skeleton arrows must not reduce R196 skeleton health");

        var blaze = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_BLAZE.get(), new BlockPos(3, 2, 1));
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

        var player = ModCompletionGameTests.createPlayer(helper);
        before = blaze.getHealth();
        player.setItemInHand(
                net.minecraft.world.InteractionHand.MAIN_HAND, Items.IRON_SWORD.getDefaultInstance());
        player.igniteForSeconds(8.0F);
        helper.assertTrue(
                !blaze.hurtServer(level, level.damageSources().playerAttack(player), 4.0F),
                "burning players still cannot hurt blazes with unenchanted weapons");
        helper.assertTrue(blaze.getHealth() == before, "burning unenchanted hits must not change blaze health");

        blaze.invulnerableTime = 0;
        var enchantedSword = Items.IRON_SWORD.getDefaultInstance();
        var registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        enchantedSword.enchant(
                registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING), 1);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, enchantedSword);
        player.igniteForSeconds(8.0F);
        helper.assertTrue(
                blaze.hurtServer(level, level.damageSources().playerAttack(player), 4.0F),
                "burning players must still hurt blazes with non-fire enchanted weapons");
        helper.assertTrue(
                blaze.getHealth() < before,
                "non-fire enchanted hits must deal damage while the attacker is burning");

        var magma = helper.spawnWithNoFreeWill(InfXEntityTypes.MAGMA_CUBE.get(), new BlockPos(4, 2, 1));
        before = magma.getHealth();
        player.setItemInHand(
                net.minecraft.world.InteractionHand.MAIN_HAND, Items.IRON_SWORD.getDefaultInstance());
        helper.assertTrue(
                !magma.hurtServer(level, level.damageSources().playerAttack(player), 4.0F),
                "swords must not hurt the R196 magma cube");
        helper.assertTrue(magma.getHealth() == before, "sword hits must not change magma cube health");
        player.setItemInHand(
                net.minecraft.world.InteractionHand.MAIN_HAND, InfXItems.IRON_PICKAXE.toStack());
        helper.assertTrue(
                magma.hurtServer(level, level.damageSources().playerAttack(player), 4.0F),
                "pickaxes must hurt the R196 magma cube");
        helper.assertTrue(magma.getHealth() == before - 4.0F, "pickaxe hits must deal magma cube damage");

        magma.invulnerableTime = 0;
        before = magma.getHealth();
        player.setItemInHand(
                net.minecraft.world.InteractionHand.MAIN_HAND,
                InfXItems.catalog()
                        .equipment(MiteMaterial.IRON, EquipmentType.WAR_HAMMER)
                        .holder()
                        .toStack());
        helper.assertTrue(
                magma.hurtServer(level, level.damageSources().playerAttack(player), 4.0F),
                "war hammers must hurt the R196 magma cube");
        helper.assertTrue(magma.getHealth() == before - 4.0F, "war hammer hits must deal magma cube damage");

        var earth = helper.spawnWithNoFreeWill(InfXEntityTypes.EARTH_ELEMENTAL.get(), new BlockPos(5, 2, 1));
        helper.assertTrue(
                earth.fireImmune()
                        && earth.getMaxSpawnClusterSize() == 1
                        && earth.getNavigation().getNodeEvaluator().canOpenDoors(),
                "earth elementals must be fire/lava immune, spawn singly and path to break doors");
        before = earth.getHealth();
        player.setItemInHand(
                net.minecraft.world.InteractionHand.MAIN_HAND, Items.IRON_SWORD.getDefaultInstance());
        helper.assertTrue(
                !earth.hurtServer(level, level.damageSources().playerAttack(player), 4.0F),
                "swords must not hurt the earth elemental");
        helper.assertTrue(earth.getHealth() == before, "sword hits must not change earth elemental health");
        player.setItemInHand(
                net.minecraft.world.InteractionHand.MAIN_HAND, InfXItems.IRON_PICKAXE.toStack());
        helper.assertTrue(
                earth.hurtServer(level, level.damageSources().playerAttack(player), 4.0F),
                "pickaxes must hurt the earth elemental");
        helper.assertTrue(earth.getHealth() < before, "pickaxe hits must deal earth elemental damage");

        assertEarthForm(helper, earth, Blocks.STONE.defaultBlockState(), false, EarthElemental.Form.STONE_NORMAL);
        assertEarthForm(helper, earth, Blocks.STONE.defaultBlockState(), true, EarthElemental.Form.STONE_MAGMA);
        assertEarthForm(helper, earth, Blocks.OBSIDIAN.defaultBlockState(), false, EarthElemental.Form.OBSIDIAN_NORMAL);
        assertEarthForm(helper, earth, Blocks.OBSIDIAN.defaultBlockState(), true, EarthElemental.Form.OBSIDIAN_MAGMA);
        assertEarthForm(helper, earth, Blocks.NETHERRACK.defaultBlockState(), false, EarthElemental.Form.NETHERRACK_NORMAL);
        assertEarthForm(helper, earth, Blocks.NETHERRACK.defaultBlockState(), true, EarthElemental.Form.NETHERRACK_MAGMA);
        assertEarthForm(helper, earth, Blocks.END_STONE.defaultBlockState(), false, EarthElemental.Form.END_STONE_NORMAL);
        assertEarthForm(helper, earth, Blocks.END_STONE.defaultBlockState(), true, EarthElemental.Form.END_STONE_MAGMA);
        helper.assertTrue(earth.quench(level), "water-bucket quenching must cool molten mineral bodies");
        helper.assertTrue(
                earth.form() == EarthElemental.Form.END_STONE_NORMAL && earth.heat() == 0,
                "quenching must restore the matching normal mineral form");

        earth.initializeMiteForm(Blocks.NETHERRACK.defaultBlockState(), true);
        float earthHealth = earth.getHealth();
        Snowball earthSnowball = new Snowball(level, player, Items.SNOWBALL.getDefaultInstance());
        helper.assertTrue(
                !earth.hurtServer(level, level.damageSources().thrown(earthSnowball, player), 1.0F)
                        && earth.form() == EarthElemental.Form.NETHERRACK_NORMAL
                        && earth.getHealth() == earthHealth,
                "snowballs must quench mineral bodies without dealing damage");

        var clay = helper.spawnWithNoFreeWill(InfXEntityTypes.CLAY_GOLEM.get(), new BlockPos(8, 2, 1));
        clay.initializeMiteForm(Blocks.CLAY.defaultBlockState(), false);
        helper.assertTrue(
                clay.form() == EarthElemental.Form.CLAY_NORMAL && !clay.isMagma()
                        && clay.doorBreakTicks(true) == 480 && clay.fireImmune()
                        && clay.getMaxSpawnClusterSize() == 1,
                "normal clay golems must retain their non-magma body and fourfold door-break speed");
        float clayHealth = clay.getHealth();
        Snowball claySnowball = new Snowball(level, player, Items.SNOWBALL.getDefaultInstance());
        helper.assertTrue(
                clay.hurtServer(level, level.damageSources().thrown(claySnowball, player), 1.0F)
                        && clay.form() == EarthElemental.Form.CLAY_NORMAL
                        && clay.getHealth() < clayHealth,
                "normal clay must take ordinary snowball damage instead of quenching");
        clay.invulnerableTime = 0;
        clay.convertToMagma();
        helper.assertTrue(
                clay.form() == EarthElemental.Form.CLAY_HARDENED && !clay.isMagma()
                        && clay.doorBreakTicks(true) == 320 && !clay.quench(level),
                "heated clay must harden permanently without entering a magma state");

        var fire = helper.spawnWithNoFreeWill(InfXEntityTypes.FIRE_ELEMENTAL.get(), new BlockPos(6, 2, 1));
        before = fire.getHealth();
        player.setItemInHand(
                net.minecraft.world.InteractionHand.MAIN_HAND, Items.IRON_SWORD.getDefaultInstance());
        helper.assertTrue(
                !fire.hurtServer(level, level.damageSources().playerAttack(player), 4.0F),
                "unenchanted swords must not hurt the fire elemental");
        helper.assertTrue(fire.getHealth() == before, "rejected fire elemental damage must not change health");
        Snowball fireSnowball = new Snowball(level, player, Items.SNOWBALL.getDefaultInstance());
        helper.assertTrue(
                fire.hurtServer(level, level.damageSources().thrown(fireSnowball, player), 3.0F),
                "snowballs must hurt the fire elemental");
        helper.assertTrue(
                fire.getHealth() == before - 3.0F, "snowballs must deal three damage to the fire elemental");
        fire.invulnerableTime = 0;
        before = fire.getHealth();
        helper.assertFalse(
                fire.hurtServer(level, level.damageSources().generic(), 2.0F),
                "unowned generic damage must not bypass the fire-elemental vulnerability gate");
        helper.assertTrue(
                fire.getHealth() == before,
                "rejected unowned generic damage must not change fire-elemental health");
        before = fire.getHealth();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, enchantedSword);
        helper.assertTrue(
                fire.hurtServer(level, level.damageSources().playerAttack(player), 4.0F),
                "non-fire enchanted weapons must hurt the fire elemental");
        helper.assertTrue(
                fire.getHealth() < before, "non-fire enchanted hits must deal fire elemental damage");
        helper.assertTrue(fire.isOnFire(), "fire elementals must always render as burning");
        helper.assertTrue(
                !fire.isSensitiveToWater(),
                "fire elementals must not stack the modern per-tick water damage on MITE's own drain");

        var enderman = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_ENDERMAN.get(), new BlockPos(8, 2, 1));
        Arrow endermanArrow = EntityType.ARROW.create(level, EntitySpawnReason.COMMAND);
        before = enderman.getHealth();
        helper.assertTrue(
                enderman.hurtServer(level, level.damageSources().arrow(endermanArrow, player), 4.0F),
                "R196 endermen must take projectile damage");
        helper.assertTrue(
                enderman.getHealth() < before,
                "R196 projectile hits must reduce enderman health");
        helper.assertTrue(
                enderman.getTarget() == player,
                "R196 projectile hits must keep the living shooter as the enderman target");
        enderman.invulnerableTime = 0;
        Arrow dispenserArrow = EntityType.ARROW.create(level, EntitySpawnReason.COMMAND);
        before = enderman.getHealth();
        helper.assertTrue(
                enderman.hurtServer(level, level.damageSources().arrow(dispenserArrow, null), 3.0F),
                "unowned projectile damage must not fall back to vanilla enderman immunity");
        helper.assertTrue(
                enderman.getHealth() < before,
                "unowned projectiles must still damage R196 endermen");
        enderman.invulnerableTime = 0;
        enderman.setTarget(player);
        Snowball indirectMagic = new Snowball(level, player, Items.SNOWBALL.getDefaultInstance());
        helper.assertTrue(
                enderman.hurtServer(level, level.damageSources().indirectMagic(indirectMagic, player), 2.0F),
                "R196 endermen must take non-projectile indirect damage");
        helper.assertTrue(
                enderman.getTarget() == null && enderman.getLastHurtByMob() == null,
                "non-projectile indirect damage must make R196 endermen blink and drop aggression");

        var sharingSource = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_ZOMBIE.get(), new BlockPos(14, 2, 12));
        var neutralEnderman = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_ENDERMAN.get(), new BlockPos(12, 2, 12));
        MonsterEvents.propagateTarget(level, sharingSource, player);
        helper.assertTrue(
                neutralEnderman.getTarget() == null,
                "shared monster targets must not override R196 enderman neutrality");
        helper.assertTrue(
                MonsterEvents.propagateTarget(level, neutralEnderman, player) == 0,
                "R196 endermen must not propagate their own targets to nearby monsters");
        neutralEnderman.setTarget(player);
        helper.assertFalse(
                MonsterTactics.tryDig(level, neutralEnderman),
                "R196 endermen must never receive generic pursuit block digging");
        ModCompletionGameTests.removePlayer(player);

        BlockPos squidPos = new BlockPos(3, 2, 7);
        helper.setBlock(squidPos, Blocks.WATER);
        helper.setBlock(squidPos.east(2), Blocks.WATER);
        var squid = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_SQUID.get(), squidPos);
        var squidPrey = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_COW.get(), squidPos.east(2));
        squid.aiStep();
        helper.assertFalse(
                squidPrey.hasEffect(MobEffects.SLOWNESS),
                "R196 squid must not slow an animal before their hitboxes collide");
        squidPrey.snapTo(squid.getX(), squid.getY(), squid.getZ(), 0.0F, 0.0F);
        for (int tick = 0; tick < 3 && !squidPrey.hasEffect(MobEffects.SLOWNESS); tick++) {
            squid.tick();
            squidPrey.snapTo(squid.getX(), squid.getY(), squid.getZ(), 0.0F, 0.0F);
        }
        helper.assertTrue(
                squidPrey.hasEffect(MobEffects.SLOWNESS),
                "R196 squid must slow land animals on a real collision");
        var preyBoat = helper.spawn(EntityType.OAK_BOAT, squidPos);
        squidPrey.startRiding(preyBoat, true, false);
        for (int hit = 0; hit < 5; hit++) {
            // A natural collision pushes the squid away from the boat. Reposition it for the
            // next independent collision while still exercising LivingEntity#doPush.
            squid.setDeltaMovement(Vec3.ZERO);
            squid.snapTo(preyBoat.getX(), preyBoat.getY(), preyBoat.getZ(), 0.0F, 0.0F);
            squid.tick();
        }
        helper.assertFalse(
                preyBoat.isRemoved(),
                "R196 squid must not destroy a pursued boat before six collisions");
        squid.setDeltaMovement(Vec3.ZERO);
        squid.snapTo(preyBoat.getX(), preyBoat.getY(), preyBoat.getZ(), 0.0F, 0.0F);
        squid.tick();
        helper.assertTrue(
                preyBoat.isRemoved(),
                "R196 squid must destroy a boat on its sixth collision while pursuing its passenger");

        BlockPos infernalStone = new BlockPos(1, 2, 3);
        helper.setBlock(infernalStone, Blocks.STONE);
        var infernal = helper.spawnWithNoFreeWill(InfXEntityTypes.INFERNAL_CREEPER.get(), new BlockPos(1, 2, 4));
        var cow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(9, 2, 4));
        var infernalTarget = ModCompletionGameTests.createPlayer(helper);
        var infernalSwell = helper.spawn(InfXEntityTypes.INFERNAL_CREEPER.get(), new BlockPos(6, 2, 1));
        infernalSwell.setTarget(infernalTarget);
        helper.startSequence()
                // Explosions query the level's entity index. Waiting for both
                // entities prevents a tick-zero explosion from observing an
                // empty index while spawn registration is still pending.
                .thenWaitUntil(() -> {
                    helper.assertEntityPresent(InfXEntityTypes.INFERNAL_CREEPER.get(), new BlockPos(1, 2, 4), 2.0D);
                    helper.assertEntityPresent(EntityType.COW, new BlockPos(9, 2, 4), 2.0D);
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        infernalSwell.getSwellDir() > 0,
                        "infernal creepers must begin swelling from MITE's expanded no-path range"))
                .thenExecute(() -> {
                    float cowHealthBefore = cow.getHealth();
                    level.explode(
                            infernal,
                            infernal.getX(),
                            infernal.getY(),
                            infernal.getZ(),
                            3.0F,
                            Level.ExplosionInteraction.MOB);
                    helper.assertTrue(
                            cow.getHealth() < cowHealthBefore,
                            "infernal creeper explosions must use the amplified six-block radius");
                    helper.assertTrue(
                            helper.getBlockState(infernalStone).isAir(),
                            "infernal creeper explosions must break stone");
                })
                .thenExecute(() -> ModCompletionGameTests.removePlayer(infernalTarget))
                .thenSucceed();
    }

    private static void attackRanges(GameTestHelper helper) {
        var skeleton = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_SKELETON.get(), new BlockPos(2, 80, 2));
        var skeletonTarget = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(4, 80, 2));
        skeleton.setItemSlot(
                net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                InfXItems.catalog()
                        .equipment(MiteMaterial.IRON, EquipmentType.SWORD)
                        .holder()
                        .toStack());
        assertMeleeBoundary(helper, skeleton, skeletonTarget, 1.949, 1.951, "tool-equipped skeleton");

        var revenant = helper.spawnWithNoFreeWill(InfXEntityTypes.REVENANT.get(), new BlockPos(2, 80, 4));
        var revenantTarget = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(4, 80, 4));
        revenant.setItemSlot(
                net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                InfXItems.catalog()
                        .equipment(MiteMaterial.IRON, EquipmentType.SWORD)
                        .holder()
                        .toStack());
        assertMeleeBoundary(helper, revenant, revenantTarget, 1.949, 1.951, "tool-equipped revenant");

        var earth = helper.spawnWithNoFreeWill(InfXEntityTypes.EARTH_ELEMENTAL.get(), new BlockPos(2, 80, 6));
        var earthTarget = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(4, 80, 6));
        assertMeleeBoundary(helper, earth, earthTarget, 2.0, 2.001, "earth elemental");

        var spider = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_SPIDER.get(), new BlockPos(2, 80, 8));
        var spiderTarget = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(4, 80, 8));
        assertMeleeBoundary(helper, spider, spiderTarget, 1.749, 1.75, "spider");

        var pigman = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_ZOMBIFIED_PIGLIN.get(), new BlockPos(2, 80, 10));
        var pigmanTarget = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(4, 80, 10));
        pigman.setItemSlot(
                net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                InfXItems.catalog()
                        .equipment(MiteMaterial.GOLD, EquipmentType.SWORD)
                        .holder()
                        .toStack());
        assertMeleeBoundary(helper, pigman, pigmanTarget, 1.749, 1.75, "tool-equipped zombie pigman");

        var silverfish = helper.spawnWithNoFreeWill(InfXEntityTypes.COPPERSPINE.get(), new BlockPos(2, 80, 12));
        var silverfishTarget = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(4, 80, 12));
        assertMeleeBoundary(helper, silverfish, silverfishTarget, 1.199, 1.201, "silverfish");

        var wolf = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_WOLF.get(), new BlockPos(2, 80, 14));
        var wolfTarget = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(4, 80, 14));
        double wolfReach = Math.sqrt(
                Math.pow(wolf.getBbWidth() * 1.75, 2.0) + wolfTarget.getBbWidth());
        assertMeleeBoundary(helper, wolf, wolfTarget, wolfReach - 0.001, wolfReach + 0.001, "ordinary wolf");

        List.of(
                        skeleton,
                        skeletonTarget,
                        revenant,
                        revenantTarget,
                        earth,
                        earthTarget,
                        spider,
                        spiderTarget,
                        pigman,
                        pigmanTarget,
                        silverfish,
                        silverfishTarget,
                        wolf,
                        wolfTarget)
                .forEach(net.minecraft.world.entity.Entity::discard);

        // Ooze pursuit only moves toward the target; the collision callback dispatches damage.
        var ooze = helper.spawn(InfXEntityTypes.OOZE.get(), new BlockPos(2, 80, 2));
        ooze.setSize(2, true);
        ooze.setNoGravity(true);
        ooze.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
        var oozeTarget = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(4, 80, 2));
        oozeTarget.setNoGravity(true);
        placeAtDistance(helper, ooze, oozeTarget, new BlockPos(2, 80, 2), 1.6);
        ooze.setTarget(oozeTarget);
        float oozeTargetHealth = oozeTarget.getHealth();
        for (int tick = 0; tick < 25; tick++) {
            ooze.tick();
        }
        helper.assertTrue(
                oozeTarget.getHealth() == oozeTargetHealth,
                "gray ooze melee pursuit must not deal damage before collision");
        oozeTarget.snapTo(ooze.getX(), ooze.getY(), ooze.getZ(), 0.0F, 0.0F);
        oozeTarget.tick();
        helper.assertTrue(
                oozeTarget.getHealth() < oozeTargetHealth,
                "gray ooze collision callback must still deal damage");
        ooze.discard();
        oozeTarget.discard();

        for (var batType : List.of(
                InfXEntityTypes.VAMPIRE_BAT, InfXEntityTypes.NIGHTWING, InfXEntityTypes.GIANT_VAMPIRE_BAT)) {
            helper.setBlock(new BlockPos(2, 86, 2), Blocks.STONE);
            var bat = helper.spawn(batType.get(), new BlockPos(2, 84, 2));
            bat.setNoGravity(true);
            var prey = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(4, 84, 2));
            prey.setNoGravity(true);
            float health = prey.getHealth();
            helper.assertFalse(bat.hasMiteAttackContact(prey), batType.getId() + " must reject ranged contact");
            bat.tick();
            helper.assertTrue(prey.getHealth() == health, batType.getId() + " must not attack before contact");
            prey.snapTo(bat.getX(), bat.getY(), bat.getZ(), 0.0F, 0.0F);
            helper.assertTrue(bat.hasMiteAttackContact(prey), batType.getId() + " must accept half-box contact");
            bat.tick();
            if (batType != InfXEntityTypes.NIGHTWING) {
                helper.assertTrue(prey.getHealth() < health, batType.getId() + " must attack after half-box contact");
            }
            bat.discard();
            prey.discard();
        }

        BlockPos squidPos = new BlockPos(2, 84, 2);
        helper.setBlock(squidPos, Blocks.WATER);
        helper.setBlock(squidPos.east(2), Blocks.WATER);
        var squid = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_SQUID.get(), squidPos);
        var squidPrey = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_COW.get(), squidPos.east(2));
        squid.aiStep();
        helper.assertFalse(squidPrey.hasEffect(MobEffects.SLOWNESS), "squid must not slow animals at range");
        squidPrey.snapTo(squid.getX(), squid.getY(), squid.getZ(), 0.0F, 0.0F);
        squid.tick();
        helper.assertTrue(squidPrey.hasEffect(MobEffects.SLOWNESS), "squid must slow animals on collision");
        helper.succeed();
    }

    private static void assertMeleeBoundary(
            GameTestHelper helper,
            Mob attacker,
            LivingEntity target,
            double insideDistance,
            double outsideDistance,
            String description) {
        BlockPos origin = helper.relativePos(attacker.blockPosition());
        placeAtDistance(helper, attacker, target, origin, insideDistance);
        helper.assertTrue(
                attacker.isWithinMeleeAttackRange(target), description + " must hit on the R196 inner boundary");
        placeAtDistance(helper, attacker, target, origin, outsideDistance);
        helper.assertFalse(
                attacker.isWithinMeleeAttackRange(target), description + " must not hit beyond the R196 boundary");
    }

    private static void placeAtDistance(
            GameTestHelper helper, Mob attacker, LivingEntity target, BlockPos origin, double distance) {
        Vec3 absolute = helper.absoluteVec(Vec3.atBottomCenterOf(origin));
        attacker.snapTo(absolute.x, absolute.y, absolute.z, 0.0F, 0.0F);
        target.snapTo(absolute.x + distance, absolute.y, absolute.z, 0.0F, 0.0F);
    }

    private static void rangedAttackRanges(GameTestHelper helper) {
        var level = helper.getLevel();
        ServerPlayer player = ModCompletionGameTests.createPlayer(helper);
        player.setNoGravity(true);
        boolean[] skeletonCompletedDraw = {false};
        boolean[] skeletonWasUsing = {false};
        int[] skeletonMaxUseTicks = {0};
        boolean[] witchThrew = {false};
        MiteWitch[] witchRef = {null};

        var skeleton = helper.spawn(InfXEntityTypes.R196_SKELETON.get(), new BlockPos(3, 2, 3));
        skeleton.setNoGravity(true);
        skeleton.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
        skeleton.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, Items.BOW.getDefaultInstance());
        skeleton.reassessWeaponGoal();
        placePlayerAtDistance(helper, skeleton, player, new BlockPos(3, 2, 3), 30.5);
        skeleton.setTarget(player);

        helper.startSequence()
                .thenExecuteFor(80, () -> {
                    skeleton.setTarget(player);
                    helper.assertFalse(
                            skeleton.isUsingItem(), "skeletons must not begin drawing beyond 30 blocks");
                    helper.assertTrue(
                            level.getEntitiesOfClass(
                                            AbstractArrow.class,
                                            skeleton.getBoundingBox().inflate(40.0),
                                            arrow -> arrow.getOwner() == skeleton)
                                    .isEmpty(),
                            "skeletons must not begin or release a shot beyond 30 blocks");
                })
                .thenExecute(() -> placePlayerAtDistance(helper, skeleton, player, new BlockPos(3, 2, 3), 29.0))
                .thenExecuteFor(90, () -> {
                    skeleton.setTarget(player);
                    boolean using = skeleton.isUsingItem();
                    skeletonMaxUseTicks[0] = Math.max(skeletonMaxUseTicks[0], skeleton.getTicksUsingItem());
                    if (skeletonWasUsing[0] && !using) {
                        skeletonCompletedDraw[0] = true;
                    }
                    if (!using && skeletonMaxUseTicks[0] >= 15) {
                        skeletonCompletedDraw[0] = true;
                    }
                    skeletonWasUsing[0] = using;
                })
                .thenExecute(() -> helper.assertTrue(
                        skeletonCompletedDraw[0],
                        "skeletons must resume firing after entering 30 blocks"
                                + "; target=" + (skeleton.getTarget() == player)
                                + ", noAi=" + skeleton.isNoAi()
                                + ", using=" + skeleton.isUsingItem()
                                + ", bow=" + skeleton.getMainHandItem().is(Items.BOW)
                                + ", visible=" + skeleton.getSensing().hasLineOfSight(player)
                                + ", distance=" + Math.sqrt(skeleton.distanceToSqr(player))))
                .thenExecute(() -> {
                    level.getEntitiesOfClass(AbstractArrow.class, skeleton.getBoundingBox().inflate(40.0))
                            .forEach(AbstractArrow::discard);
                    skeleton.discard();
                })
                .thenExecute(() -> {
                    var witch = helper.spawn(InfXEntityTypes.R196_WITCH.get(), new BlockPos(3, 2, 3));
                    witch.setNoGravity(true);
                    witch.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
                    placePlayerAtDistance(helper, witch, player, new BlockPos(3, 2, 3), 20.5);
                    witch.setTarget(player);
                    witchRef[0] = witch;
                })
                .thenExecuteFor(80, () -> {
                    var witch = witchRef[0];
                    witch.setTarget(player);
                    helper.assertTrue(
                            level.getEntitiesOfClass(
                                            ThrownSplashPotion.class,
                                            witch.getBoundingBox().inflate(32.0),
                                            potion -> potion.getOwner() == witch)
                                    .isEmpty(),
                            "witches must not begin or release a potion beyond 20 blocks");
                })
                .thenExecute(() -> {
                    var witch = witchRef[0];
                    placePlayerAtDistance(helper, witch, player, new BlockPos(3, 2, 3), 19.0);
                })
                .thenExecuteFor(140, () -> {
                    var witch = witchRef[0];
                    witch.setTarget(player);
                    if (!level.getEntitiesOfClass(ThrownSplashPotion.class, witch.getBoundingBox().inflate(32.0))
                                    .isEmpty()) {
                        witchThrew[0] = true;
                    }
                })
                .thenExecute(() -> helper.assertTrue(
                        witchThrew[0], "witches must resume throwing after entering 20 blocks"))
                .thenExecute(() -> {
                    witchRef[0].discard();
                    ModCompletionGameTests.removePlayer(player);
                })
                .thenSucceed();
    }

    private static void placePlayerAtDistance(
            GameTestHelper helper, Mob attacker, ServerPlayer player, BlockPos origin, double distance) {
        Vec3 absolute = helper.absoluteVec(Vec3.atBottomCenterOf(origin));
        attacker.snapTo(absolute.x, absolute.y, absolute.z, 0.0F, 0.0F);
        player.snapTo(absolute.x + distance, absolute.y, absolute.z, 0.0F, 0.0F);
    }

    private static void explosionRanges(GameTestHelper helper) {
        var level = helper.getLevel();

        var ordinary = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_CREEPER.get(), new BlockPos(4, 10, 4));
        var ordinaryInside = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(8, 10, 4));
        var ordinaryOutside = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(9, 10, 4));
        placeExplosionTargets(helper, ordinary, ordinaryInside, ordinaryOutside, new BlockPos(4, 10, 4), 4.3, 4.5);

        var infernal = helper.spawnWithNoFreeWill(InfXEntityTypes.INFERNAL_CREEPER.get(), new BlockPos(25, 10, 4));
        var infernalInside = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(33, 10, 4));
        var infernalOutside = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(34, 10, 4));
        placeExplosionTargets(helper, infernal, infernalInside, infernalOutside, new BlockPos(25, 10, 4), 8.7, 8.9);

        var netherspawn = helper.spawnWithNoFreeWill(InfXEntityTypes.NETHERSPAWN.get(), new BlockPos(4, 10, 25));
        var netherspawnInside = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(8, 10, 25));
        var netherspawnOutside = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(9, 10, 25));
        placeExplosionTargets(
                helper, netherspawn, netherspawnInside, netherspawnOutside, new BlockPos(4, 10, 25), 3.9, 4.1);

        var ghast = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_GHAST.get(), new BlockPos(25, 10, 25));
        var fireball = new LargeFireball(level, ghast, Vec3.ZERO, 1);
        var fireballInside = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(29, 10, 25));
        var fireballOutside = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(30, 10, 25));
        Vec3 fireballCenter = helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(25, 10, 25)));
        fireball.snapTo(fireballCenter.x, fireballCenter.y, fireballCenter.z, 0.0F, 0.0F);
        fireballInside.snapTo(fireballCenter.x + 3.9, fireballCenter.y, fireballCenter.z, 0.0F, 0.0F);
        fireballOutside.snapTo(fireballCenter.x - 4.1, fireballCenter.y, fireballCenter.z, 0.0F, 0.0F);

        var vanilla = helper.spawnWithNoFreeWill(EntityType.CREEPER, new BlockPos(15, 10, 15));
        var vanillaInside = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(21, 10, 15));
        var vanillaOutside = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(22, 10, 15));
        placeExplosionTargets(helper, vanilla, vanillaInside, vanillaOutside, new BlockPos(15, 10, 15), 5.9, 6.1);

        helper.startSequence()
                .thenExecuteAfter(1, () -> {
                    assertExplosionBoundary(helper, ordinary, ordinaryInside, ordinaryOutside, 3.0F, "ordinary R196 creeper");
                    assertExplosionBoundary(helper, infernal, infernalInside, infernalOutside, 6.0F, "infernal creeper");
                    assertExplosionBoundary(helper, netherspawn, netherspawnInside, netherspawnOutside, 1.0F, "netherspawn");
                    assertExplosionBoundary(helper, fireball, fireballInside, fireballOutside, 1.0F, "R196 ghast fireball");
                    assertExplosionBoundary(helper, vanilla, vanillaInside, vanillaOutside, 3.0F, "non-R196 explosion");
                })
                .thenSucceed();
    }

    private static void placeExplosionTargets(
            GameTestHelper helper,
            Mob source,
            LivingEntity inside,
            LivingEntity outside,
            BlockPos origin,
            double insideDistance,
            double outsideDistance) {
        Vec3 absolute = helper.absoluteVec(Vec3.atBottomCenterOf(origin));
        source.snapTo(absolute.x, absolute.y, absolute.z, 0.0F, 0.0F);
        source.setNoGravity(true);
        inside.snapTo(absolute.x + insideDistance, absolute.y, absolute.z, 0.0F, 0.0F);
        inside.setNoGravity(true);
        outside.snapTo(absolute.x - outsideDistance, absolute.y, absolute.z, 0.0F, 0.0F);
        outside.setNoGravity(true);
    }

    private static void assertExplosionBoundary(
            GameTestHelper helper,
            net.minecraft.world.entity.Entity source,
            LivingEntity inside,
            LivingEntity outside,
            float blockRadius,
            String description) {
        float insideHealth = inside.getHealth();
        float outsideHealth = outside.getHealth();
        helper.getLevel().explode(
                source, source.getX(), source.getY(), source.getZ(), blockRadius, Level.ExplosionInteraction.MOB);
        helper.assertTrue(inside.getHealth() < insideHealth, description + " must damage inside its entity radius");
        helper.assertTrue(outside.getHealth() == outsideHealth, description + " must not damage outside its entity radius");
    }

    private static void enderman(GameTestHelper helper) {
        var level = helper.getLevel();
        MiteEnderman enderman = helper.spawn(InfXEntityTypes.R196_ENDERMAN.get(), new BlockPos(3, 2, 3));
        ItemEntity pearl = new ItemEntity(
                level, enderman.getX() + 1.0, enderman.getY(), enderman.getZ(), Items.ENDER_PEARL.getDefaultInstance());
        pearl.setNoPickUpDelay();
        level.addFreshEntity(pearl);

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertEntityPresent(InfXEntityTypes.R196_ENDERMAN.get(), new BlockPos(3, 2, 3), 2.0D))
                .thenWaitUntil(() -> helper.assertTrue(
                        pearl.isRemoved(),
                        "R196 endermen must collect nearby dropped ender pearls"))
                .thenExecute(() -> helper.assertTrue(
                        enderman.requiresCustomPersistence(),
                        "R196 endermen carrying valuables must not despawn"))
                .thenSucceed();
    }

    private static void witchCurse(GameTestHelper helper) {
        var level = helper.getLevel();
        MiteWitch witch = helper.spawn(InfXEntityTypes.R196_WITCH.get(), new BlockPos(7, 2, 7));
        // MITE only excludes a creative/disable-damage player. A survival player with the
        // entity-level Invulnerable flag must still receive a pending witch curse.
        ServerPlayer curseDeliveryProbe = ModCompletionGameTests.createPlayer(helper);
        curseDeliveryProbe.setInvulnerable(true);

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(
                        CurseData.get(level.getServer())
                                .entry(curseDeliveryProbe.getUUID())
                                .isPresent(),
                        "R196 witches must create a pending curse for a survival player marked Invulnerable"))
                .thenExecute(() -> {
                    witch.discard();
                    CurseData.get(level.getServer()).remove(curseDeliveryProbe.getUUID());
                    ModCompletionGameTests.removePlayer(curseDeliveryProbe);
                })
                .thenSucceed();
    }

    private static void netherspawnMechanics(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = ModCompletionGameTests.createPlayer(helper);
        var snowballTarget = helper.spawnWithNoFreeWill(InfXEntityTypes.NETHERSPAWN.get(), new BlockPos(2, 2, 2));
        var waterTarget = helper.spawnWithNoFreeWill(InfXEntityTypes.NETHERSPAWN.get(), new BlockPos(5, 2, 2));
        var terrainSource = helper.spawnWithNoFreeWill(InfXEntityTypes.NETHERSPAWN.get(), new BlockPos(8, 2, 2));
        BlockPos waterSafetyProbe = new BlockPos(6, 2, 2);
        BlockPos netherrack = new BlockPos(9, 2, 2);
        BlockPos quartz = new BlockPos(7, 2, 2);
        BlockPos netherGold = new BlockPos(8, 2, 3);
        BlockPos gold = new BlockPos(8, 2, 1);
        BlockPos deepslateGold = new BlockPos(8, 3, 2);
        helper.setBlock(netherrack, Blocks.NETHERRACK);
        helper.setBlock(waterSafetyProbe, Blocks.TORCH);
        helper.setBlock(quartz, Blocks.NETHER_QUARTZ_ORE);
        helper.setBlock(netherGold, Blocks.NETHER_GOLD_ORE);
        helper.setBlock(gold, Blocks.GOLD_ORE);
        helper.setBlock(deepslateGold, Blocks.DEEPSLATE_GOLD_ORE);
        Snowball snowball = new Snowball(level, player, Items.SNOWBALL.getDefaultInstance());

        helper.startSequence()
                .thenWaitUntil(() -> {
                    helper.assertEntityPresent(InfXEntityTypes.NETHERSPAWN.get(), new BlockPos(2, 2, 2), 2.0D);
                    helper.assertEntityPresent(InfXEntityTypes.NETHERSPAWN.get(), new BlockPos(5, 2, 2), 2.0D);
                    helper.assertEntityPresent(InfXEntityTypes.NETHERSPAWN.get(), new BlockPos(8, 2, 2), 2.0D);
                })
                .thenExecute(() -> {
                    float healthBefore = snowballTarget.getHealth();
                    helper.assertTrue(
                            snowballTarget.hurtServer(level, level.damageSources().thrown(snowball, player), 0.0F),
                            "Netherspawn must accept snowball damage");
                    helper.assertTrue(
                            Math.abs(snowballTarget.getHealth() - (healthBefore - 2.0F)) < DAMAGE_EPSILON,
                            "MITE snowballs must deal two damage to netherspawn");
                    waterTarget.hurtServer(level, level.damageSources().drown(), 8.0F);
                    helper.assertFalse(waterTarget.isAlive(), "drowning-source water damage must kill netherspawn safely");
                    level.explode(
                            terrainSource,
                            terrainSource.getX(),
                            terrainSource.getY(),
                            terrainSource.getZ(),
                            4.0F,
                            true,
                            Level.ExplosionInteraction.MOB);
                })
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(
                            helper.getBlockState(waterSafetyProbe).is(Blocks.TORCH),
                            "drowning-source water kills must not trigger a netherspawn explosion");
                    helper.assertTrue(helper.getBlockState(netherrack).is(Blocks.NETHERRACK), "netherspawn must preserve netherrack");
                    helper.assertTrue(helper.getBlockState(quartz).is(Blocks.NETHER_QUARTZ_ORE), "netherspawn must preserve quartz ore");
                    helper.assertTrue(helper.getBlockState(netherGold).is(Blocks.NETHER_GOLD_ORE), "netherspawn must preserve nether gold ore");
                    helper.assertTrue(helper.getBlockState(gold).is(Blocks.GOLD_ORE), "netherspawn must preserve gold ore");
                    helper.assertTrue(
                            helper.getBlockState(deepslateGold).is(Blocks.DEEPSLATE_GOLD_ORE),
                            "netherspawn must preserve the modern gold-ore variant");
                    ModCompletionGameTests.removePlayer(player);
                })
                .thenSucceed();
    }

    private static void tactics(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = ModCompletionGameTests.createPlayer(helper);
        Vec3 playerPos = helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(7, 2, 7)));
        player.snapTo(playerPos.x, playerPos.y, playerPos.z, 0.0F, 0.0F);

        var leader = helper.spawn(
                InfXEntityTypes.R196_ZOMBIE.get(), new BlockPos(2, 2, 2));
        var ally = helper.spawn(
                InfXEntityTypes.R196_SKELETON.get(), new BlockPos(3, 2, 2));
        var piglin = helper.spawnWithNoFreeWill(
                InfXEntityTypes.R196_ZOMBIFIED_PIGLIN.get(), new BlockPos(4, 2, 2));
        leader.setTarget(player);
        MonsterEvents.propagateTarget(level, leader, player);
        helper.assertTrue(ally.getTarget() == player, "R196 monsters must share a newly acquired player target");
        helper.assertTrue(
                piglin.getTarget() == null,
                "R196 zombified piglins must retain their own close-range target rules");
        leader.setTarget(null);
        ally.setTarget(null);
        level.gameEvent(GameEvent.BLOCK_DESTROY, player.position(), GameEvent.Context.of(player));
        helper.assertTrue(leader.getTarget() == player, "player block noise must attract nearby monsters");
        helper.assertTrue(ally.getTarget() == player, "activity attraction applies across hostile families");
        helper.assertTrue(
                piglin.getTarget() == null,
                "player activity must not bypass R196 zombified-piglin awareness range");

        var digger = helper.spawnWithNoFreeWill(
                InfXEntityTypes.R196_ZOMBIE.get(), new BlockPos(2, 2, 7));
        digger.setItemSlot(
                net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                InfXItems.catalog()
                        .equipment(MiteMaterial.IRON, EquipmentType.PICKAXE)
                        .holder()
                        .toStack());
        digger.setTarget(player);
        BlockPos wall = new BlockPos(4, 3, 7);
        helper.setBlock(wall, Blocks.STONE);
        for (int attempt = 0; attempt < 30 && !helper.getBlockState(wall).isAir(); attempt++) {
            MonsterTactics.tryDig(level, digger);
        }
        helper.assertTrue(helper.getBlockState(wall).isAir(), "tool-equipped blocked monster must mine through stone");
        ModCompletionGameTests.removePlayer(player);
        helper.succeed();
    }
}
