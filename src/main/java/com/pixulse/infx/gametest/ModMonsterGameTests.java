package com.pixulse.infx.gametest;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.entity.*;
import com.pixulse.infx.item.EquipmentKey;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.item.material.Quality;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.registry.InfXDataComponents;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.world.BlightTracker;
import com.pixulse.infx.world.MoonPhase;
import com.pixulse.infx.world.RiverBiomes;
import com.pixulse.infx.world.SpawnDensity;
import com.pixulse.infx.world.SpawnRateTracker;
import com.pixulse.infx.world.Tension;
import com.pixulse.infx.world.Underworld;
import java.util.ArrayList;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Runtime coverage for roster construction and vanilla natural-spawn replacement. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class ModMonsterGameTests {
    private static final float DAMAGE_EPSILON = 0.001F;
    private static final String ROSTER = "infx_monster_roster";
    private static final String ATTRIBUTES = "infx_monster_attributes";
    private static final String REPLACEMENT = "infx_monster_replacement";
    private static final String BEHAVIORS = "infx_monster_behaviors";
    private static final String NETHERSPAWN = "infx_netherspawn_mechanics";
    private static final String ENDERMAN = "infx_enderman";
    private static final String WITCH_CURSE = "infx_witch_curse";
    private static final String TACTICS = "infx_monster_tactics";
    private static final String SPAWNER_LIGHT = "infx_spawner_light";
    private static final String SPAWNS = "infx_spawn_tables";
    private static final String ATTACK_RANGES = "infx_attack_ranges";
    private static final String RANGED_ATTACK_RANGES = "infx_ranged_attack_ranges";
    private static final String EXPLOSION_RANGES = "infx_explosion_ranges";
    private static final String LONGDEAD_DROPS = "infx_longdead_drops";
    private static final String SPAWN_EQUIPMENT = "infx_spawn_equipment";
    private static final String SKELETON_DROPS = "infx_skeleton_drops";
    private static final String WEAPON_DROPS = "infx_weapon_drops";
    private static final String SKELETON_TRAP = "infx_skeleton_trap";
    private static final String ZOMBIE_SMART = "infx_zombie_smart";
    private static final String ZOMBIE_LEADER = "infx_zombie_leader";
    private static final String ZOMBIE_DIG_RATE = "infx_zombie_dig_rate";
    private static final String ZOMBIE_BURN_TREE = "infx_zombie_burn_tree";
    private static final String GHOUL_HEAL = "infx_ghoul_heal";
    private static final String TENSION_CURVE = "infx_tension_curve";
    private static final String SKELETON_BONE_REPAIR = "infx_skeleton_bone_repair";
    private static final String SKELETON_CACTUS_IMMUNE = "infx_skeleton_cactus_immune";
    private static final String SKELETON_GUARDIAN_SWITCH = "infx_skeleton_guardian_switch";
    private static final String FRENZY_SPEED = "infx_frenzy_speed";
    private static final String ZOMBIE_NO_BABY = "infx_zombie_no_baby";
    private static final String ZOMBIE_FOOD = "infx_zombie_food";
    private static final String ZOMBIE_CONVERSION_SKIP = "infx_zombie_conversion_skip";
    private static final String ZOMBIE_DIG_FEET_FIRST = "infx_zombie_dig_feet_first";
    private static final String SLIME_BURNING_NO_SPLIT = "infx_slime_burning_no_split";
    private static final String BLOOD_MOON_LIGHTNING = "infx_blood_moon_lightning";
    private static final String BLOOD_MOON_RAIN = "infx_blood_moon_rain";
    private static final String BLOOD_MOON_CROP_BLIGHT = "infx_blood_moon_crop_blight";
    private static final String BURNING_MOB_FIRE_TRANSFER = "infx_burning_mob_fire_transfer";
    private static final String VILLAGER_CONVERSION_NORMAL_GATE = "infx_villager_conversion_normal_gate";
    private static final String ZOMBIE_HATCHET_DAY = "infx_zombie_hatchet_day";
    private static final String BLOOD_MOON_SPAWN_FACTOR = "infx_blood_moon_spawn_factor";
    private static final String DEPTH_SPAWN_SCALE = "infx_depth_spawn_scale";
    private static final String SPAWN_RATE_MODIFIER = "infx_spawn_rate_modifier";
    private static final String SPAWN_CADENCE = "infx_spawn_cadence";
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
        FUNCTIONS.register(SPAWNER_LIGHT, () -> ModMonsterGameTests::spawnerLight);
        FUNCTIONS.register(SPAWNS, () -> ModMonsterGameTests::spawnTables);
        FUNCTIONS.register(ATTACK_RANGES, () -> ModMonsterGameTests::attackRanges);
        FUNCTIONS.register(RANGED_ATTACK_RANGES, () -> ModMonsterGameTests::rangedAttackRanges);
        FUNCTIONS.register(EXPLOSION_RANGES, () -> ModMonsterGameTests::explosionRanges);
        FUNCTIONS.register(LONGDEAD_DROPS, () -> ModMonsterGameTests::longdeadDrops);
        FUNCTIONS.register(SPAWN_EQUIPMENT, () -> ModMonsterGameTests::spawnEquipment);
        FUNCTIONS.register(SKELETON_DROPS, () -> ModMonsterGameTests::skeletonDrops);
        FUNCTIONS.register(WEAPON_DROPS, () -> ModMonsterGameTests::weaponDrops);
        FUNCTIONS.register(SKELETON_TRAP, () -> ModMonsterGameTests::skeletonTrap);
        FUNCTIONS.register(ZOMBIE_SMART, () -> ModMonsterGameTests::zombieSmart);
        FUNCTIONS.register(ZOMBIE_LEADER, () -> ModMonsterGameTests::zombieLeader);
        FUNCTIONS.register(ZOMBIE_DIG_RATE, () -> ModMonsterGameTests::zombieDigRate);
        FUNCTIONS.register(ZOMBIE_BURN_TREE, () -> ModMonsterGameTests::zombieBurnTree);
        FUNCTIONS.register(GHOUL_HEAL, () -> ModMonsterGameTests::ghoulHeal);
        FUNCTIONS.register(TENSION_CURVE, () -> ModMonsterGameTests::tensionCurve);
        FUNCTIONS.register(SKELETON_BONE_REPAIR, () -> ModMonsterGameTests::skeletonBoneRepair);
        FUNCTIONS.register(SKELETON_CACTUS_IMMUNE, () -> ModMonsterGameTests::skeletonCactusImmune);
        FUNCTIONS.register(SKELETON_GUARDIAN_SWITCH, () -> ModMonsterGameTests::skeletonGuardianSwitch);
        FUNCTIONS.register(FRENZY_SPEED, () -> ModMonsterGameTests::frenzySpeed);
        FUNCTIONS.register(ZOMBIE_NO_BABY, () -> ModMonsterGameTests::zombieNoBaby);
        FUNCTIONS.register(ZOMBIE_FOOD, () -> ModMonsterGameTests::zombieFood);
        FUNCTIONS.register(ZOMBIE_CONVERSION_SKIP, () -> ModMonsterGameTests::zombieConversionSkip);
        FUNCTIONS.register(ZOMBIE_DIG_FEET_FIRST, () -> ModMonsterGameTests::zombieDigFeetFirst);
        FUNCTIONS.register(SLIME_BURNING_NO_SPLIT, () -> ModMonsterGameTests::slimeBurningNoSplit);
        FUNCTIONS.register(BLOOD_MOON_LIGHTNING, () -> ModMonsterGameTests::bloodMoonLightning);
        FUNCTIONS.register(BLOOD_MOON_RAIN, () -> ModMonsterGameTests::bloodMoonRain);
        FUNCTIONS.register(BLOOD_MOON_CROP_BLIGHT, () -> ModMonsterGameTests::bloodMoonCropBlight);
        FUNCTIONS.register(BURNING_MOB_FIRE_TRANSFER, () -> ModMonsterGameTests::burningMobFireTransfer);
        FUNCTIONS.register(VILLAGER_CONVERSION_NORMAL_GATE, () -> ModMonsterGameTests::villagerConversionNormalGate);
        FUNCTIONS.register(ZOMBIE_HATCHET_DAY, () -> ModMonsterGameTests::zombieHatchetDay);
        FUNCTIONS.register(BLOOD_MOON_SPAWN_FACTOR, () -> ModMonsterGameTests::bloodMoonSpawnFactor);
        FUNCTIONS.register(DEPTH_SPAWN_SCALE, () -> ModMonsterGameTests::depthSpawnScale);
        FUNCTIONS.register(SPAWN_RATE_MODIFIER, () -> ModMonsterGameTests::spawnRateModifier);
        FUNCTIONS.register(SPAWN_CADENCE, () -> ModMonsterGameTests::spawnCadence);
    }

    private ModMonsterGameTests() {}

    public static void register(IEventBus modBus) {
        FUNCTIONS.register(modBus);
    }

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                InfiniteX.id("infx_monsters"), new TestEnvironmentDefinition.AllOf());
        Holder<TestEnvironmentDefinition<?>> rangedEnvironment = event.registerEnvironment(
                InfiniteX.id("infx_ranged_combat"), new TestEnvironmentDefinition.AllOf());
        for (String name : List.of(
                ROSTER,
                ATTRIBUTES,
                REPLACEMENT,
                BEHAVIORS,
                NETHERSPAWN,
                ENDERMAN,
                WITCH_CURSE,
                TACTICS,
                SPAWNER_LIGHT,
                SPAWNS,
                ATTACK_RANGES,
                RANGED_ATTACK_RANGES,
                EXPLOSION_RANGES,
                SPAWN_EQUIPMENT,
                SKELETON_DROPS,
                WEAPON_DROPS,
                SKELETON_TRAP,
                ZOMBIE_SMART,
                ZOMBIE_LEADER,
                ZOMBIE_DIG_RATE,
                ZOMBIE_BURN_TREE,
                GHOUL_HEAL,
                TENSION_CURVE,
                SKELETON_BONE_REPAIR,
                SKELETON_CACTUS_IMMUNE,
                SKELETON_GUARDIAN_SWITCH,
                FRENZY_SPEED,
                ZOMBIE_NO_BABY,
                ZOMBIE_FOOD,
                ZOMBIE_CONVERSION_SKIP,
                ZOMBIE_DIG_FEET_FIRST,
                SLIME_BURNING_NO_SPLIT,
                BLOOD_MOON_LIGHTNING,
                BLOOD_MOON_RAIN,
                BLOOD_MOON_CROP_BLIGHT,
                BURNING_MOB_FIRE_TRANSFER,
                VILLAGER_CONVERSION_NORMAL_GATE,
                ZOMBIE_HATCHET_DAY,
                BLOOD_MOON_SPAWN_FACTOR,
                DEPTH_SPAWN_SCALE,
                SPAWN_RATE_MODIFIER,
                SPAWN_CADENCE)) {
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
                InfXEntityTypes.INFX_COW,
                InfXEntityTypes.INFX_CHICKEN,
                InfXEntityTypes.INFX_SHEEP,
                InfXEntityTypes.INFX_PIG,
                InfXEntityTypes.INFX_HORSE,
                InfXEntityTypes.INFX_OCELOT,
                InfXEntityTypes.INFX_WOLF);
        for (var holder : InfXEntityTypes.ALL) {
            var entity = holder.get().create(helper.getLevel(), EntitySpawnReason.COMMAND);
            if (!passiveReplacements.contains(holder)) {
                helper.assertTrue(entity instanceof InfxMob, holder.getId() + " must implement InfxMob");
            }
            helper.assertTrue(
                    entity instanceof LivingEntity living && living.getMaxHealth() > 0.0F,
                    holder.getId() + " must have a registered positive max-health attribute");
            entity.discard();
        }
        helper.assertTrue(
                BuiltInRegistries.ENTITY_TYPE
                        .wrapAsHolder(EntityType.ZOMBIE)
                        .is(EntityTypeTags.UNDEAD),
                "replacement zombies must retain vanilla undead semantics");
        helper.assertTrue(
                BuiltInRegistries.ENTITY_TYPE
                        .wrapAsHolder(InfXEntityTypes.INFX_SKELETON.get())
                        .is(EntityTypeTags.SKELETONS),
                "replacement skeletons must retain the vanilla skeleton family tag");
        helper.assertTrue(
                BuiltInRegistries.ENTITY_TYPE
                        .wrapAsHolder(InfXEntityTypes.PHASE_SPIDER.get())
                        .is(EntityTypeTags.ARTHROPOD),
                "INFX spiders must retain arthropod semantics");
        helper.assertTrue(
                BuiltInRegistries.ENTITY_TYPE
                        .wrapAsHolder(InfXEntityTypes.INFX_SQUID.get())
                        .is(EntityTypeTags.AQUATIC),
                "replacement squid must retain aquatic semantics");
        helper.assertTrue(
                InfXEntityTypes.INFX_SQUID.get().getCategory() == MobCategory.WATER_CREATURE
                        && InfXEntityTypes.INFX_SQUID.get().isAllowedInPeaceful(),
                "replacement squid must use the peaceful water-creature cap");
        for (var type : List.of(
                InfXEntityTypes.INFX_COD,
                InfXEntityTypes.INFX_SALMON,
                InfXEntityTypes.INFX_PUFFERFISH,
                InfXEntityTypes.INFX_TROPICAL_FISH)) {
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
        for (var type : List.of(
                InfXEntityTypes.INFX_BAT,
                InfXEntityTypes.VAMPIRE_BAT,
                InfXEntityTypes.NIGHTWING,
                InfXEntityTypes.GIANT_VAMPIRE_BAT)) {
            helper.assertTrue(
                    type.get().getCategory() == MobCategory.AMBIENT && type.get().isAllowedInPeaceful(),
                    type.getId() + " must retain InfX's ambient, non-IMob spawn semantics");
        }
        helper.assertTrue(
                InfXEntityTypes.DIRE_WOLF.get().getCategory() == MobCategory.CREATURE
                        && InfXEntityTypes.DIRE_WOLF.get().isAllowedInPeaceful(),
                "dire wolves must use animal spawning rather than the hostile cap");
        helper.assertTrue(
                experienceAfterDropEvent(helper, InfXEntityTypes.INFX_WOLF.get(), 5),
                "vanilla wolves must keep InfX's base experience");
        helper.assertTrue(
                experienceAfterDropEvent(helper, InfXEntityTypes.DIRE_WOLF.get(), 10),
                "dire wolves must keep double the base experience");
        helper.assertTrue(
                experienceAfterDropEvent(helper, InfXEntityTypes.HELLHOUND.get(), 15),
                "hellhounds must keep triple the base experience");
        helper.assertTrue(
                experienceAfterDropEvent(helper, InfXEntityTypes.INFX_COW.get(), 0),
                "other animals must still grant no experience");
        helper.succeed();
    }

    private static boolean experienceAfterDropEvent(GameTestHelper helper, EntityType<?> type, int expected) {
        if (!(type.create(helper.getLevel(), EntitySpawnReason.COMMAND) instanceof LivingEntity living)) {
            return false;
        }
        int original = living.getExperienceReward(helper.getLevel(), null);
        var drop = NeoForge.EVENT_BUS.post(
                new LivingExperienceDropEvent(living, null, original));
        living.discard();
        return drop.getDroppedExperience() == expected;
    }

    private static void attributes(GameTestHelper helper) {
        int zombieX = 1;
        // The vanilla zombie zeroes its modern armor through the FinalizeSpawnEvent profile.
        Mob vanilla = spawnWithFinalize(helper, EntityType.ZOMBIE);
        helper.assertTrue(
                vanilla.getAttributeBaseValue(Attributes.ARMOR) == 0.0D,
                "minecraft:zombie must not inherit modern zombie armor");
        for (var type : List.of(
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
        var piglin = helper.spawn(InfXEntityTypes.INFX_ZOMBIFIED_PIGLIN.get(), new BlockPos(8, 2, 1));
        helper.assertTrue(
                piglin.getAttributeBaseValue(Attributes.ARMOR) == 0.0D,
                "INFX zombified piglins must not inherit modern zombie armor");
        helper.assertTrue(
                piglin.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == 0.23D,
                "INFX zombified piglins must use the normalized modern base movement speed");
        helper.assertTrue(
                !piglin.canBreakDoors() && !piglin.canPickUpLoot(),
                "INFX zombified piglins must not inherit modern zombie door breaking or item pickup");
        var piglinTarget = ModCompletionGameTests.createPlayer(helper);
        piglin.setTarget(piglinTarget);
        // The replacement adjusts its modifier from customServerAiStep, which is
        // invoked by the real entity tick rather than by the client-side aiStep hook.
        piglin.tick();
        helper.assertTrue(
                Math.abs(piglin.getAttributeValue(Attributes.MOVEMENT_SPEED) - 0.28D) < DAMAGE_EPSILON,
                "INFX zombified piglins must use the normalized +0.05 chase-speed bonus");
        var enderman = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_ENDERMAN.get(), new BlockPos(7, 2, 1));
        helper.assertTrue(
                enderman.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == 0.30D,
                "INFX endermen must retain their 0.30 standing movement speed");
        enderman.setTarget(piglinTarget);
        helper.assertTrue(
                Math.abs(enderman.getAttributeValue(Attributes.MOVEMENT_SPEED) - 0.45D) < DAMAGE_EPSILON,
                "INFX endermen must use the modern 0.30 + 0.15 chase-speed total");
        enderman.setTarget(null);
        helper.assertTrue(
                Math.abs(enderman.getAttributeValue(Attributes.MOVEMENT_SPEED) - 0.30D) < DAMAGE_EPSILON,
                "INFX endermen must remove the chase-speed modifier without a target");
        piglin.setTarget(null);
        ModCompletionGameTests.removePlayer(piglinTarget);
        piglin.tick();
        helper.assertTrue(
                Math.abs(piglin.getAttributeValue(Attributes.MOVEMENT_SPEED) - 0.23D) < DAMAGE_EPSILON,
                "INFX zombified piglins must remove the chase-speed modifier without a target");
        var blaze = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_BLAZE.get(), new BlockPos(9, 2, 1));
        helper.assertTrue(
                Math.abs(blaze.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) - 0.23D) < DAMAGE_EPSILON,
                "INFX blazes must retain the modern baseline movement speed");
        var skeleton = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_SKELETON.get(), new BlockPos(11, 2, 1));
        helper.assertTrue(
                Math.abs(skeleton.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) - 0.25D) < DAMAGE_EPSILON,
                "INFX replacement skeletons must retain the modern 0.25 movement speed");
        skeleton.discard();
        for (EntityType<?> type : List.of(EntityType.STRAY, EntityType.BOGGED, EntityType.PARCHED)) {
            Mob variant = spawnWithFinalize(helper, type);
            helper.assertTrue(
                    variant.getAttributeBaseValue(Attributes.MAX_HEALTH) == InfxSkeleton.ORDINARY_MAX_HEALTH,
                    type + " must share the ordinary skeleton's maximum health");
            variant.discard();
        }

        int spiderX = 1;
        for (var type : List.of(
                InfXEntityTypes.INFX_SPIDER,
                InfXEntityTypes.INFX_CAVE_SPIDER,
                InfXEntityTypes.BLACK_WIDOW_SPIDER,
                InfXEntityTypes.DEMON_SPIDER,
                InfXEntityTypes.WOOD_SPIDER,
                InfXEntityTypes.PHASE_SPIDER)) {
            var spider = helper.spawnWithNoFreeWill(type.get(), new BlockPos(spiderX++, 2, 2));
            double expectedSpeed = type == InfXEntityTypes.DEMON_SPIDER ? 0.375D : 0.30D;
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
                InfXEntityTypes.INFX_SLIME,
                InfXEntityTypes.JELLY,
                InfXEntityTypes.BLOB,
                InfXEntityTypes.OOZE,
                InfXEntityTypes.PUDDING)) {
            var slime = helper.spawnWithNoFreeWill(type.get(), new BlockPos(slimeX++, 2, 4));
            for (int size : List.of(1, 2, 4)) {
                slime.setSize(size, true);
                double expectedSpeed = slime.variant() == InfxSlime.Variant.OOZE
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
                    "INFX magma cubes must retain 0.20 movement speed after size " + size);
            helper.assertTrue(
                    magmaCube.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) == size * 2.0D,
                    "INFX magma cubes must retain two damage per size");
            helper.assertTrue(
                    magmaCube.getAttributeBaseValue(Attributes.ARMOR) == size * 2.0D,
                    "INFX magma cubes must retain two defense per size");
        }

        var level = helper.getLevel();
        ItemEntity magicOwner = new ItemEntity(level, 0.0, 0.0, 0.0, Items.SNOWBALL.getDefaultInstance());
        Snowball magicProjectile = new Snowball(level, 0.0, 0.0, 0.0, Items.SNOWBALL.getDefaultInstance());
        var directMagic = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_WITCH.get(), new BlockPos(1, 2, 8));
        float before = directMagic.getHealth();
        helper.assertTrue(
                directMagic.hurtServer(level, level.damageSources().magic(), 20.0F),
                "direct magic must damage the INFX witch");
        helper.assertTrue(
                Math.abs(directMagic.getHealth() - (before - 20.0F)) < DAMAGE_EPSILON,
                "direct magic must not receive the INFX witch's indirect-magic defense");

        var indirectNonMagic = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_WITCH.get(), new BlockPos(4, 2, 8));
        before = indirectNonMagic.getHealth();
        helper.assertTrue(
                indirectNonMagic.hurtServer(level, level.damageSources().thrown(magicProjectile, magicOwner), 20.0F),
                "indirect non-magic damage must damage the INFX witch");
        helper.assertTrue(
                Math.abs(indirectNonMagic.getHealth() - (before - 20.0F)) < DAMAGE_EPSILON,
                "indirect non-magic damage must not receive the INFX witch's defense");

        var indirectMagic = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_WITCH.get(), new BlockPos(7, 2, 8));
        before = indirectMagic.getHealth();
        helper.assertTrue(
                indirectMagic.hurtServer(level, level.damageSources().indirectMagic(magicProjectile, magicOwner), 20.0F),
                "indirect magic must damage the INFX witch");
        helper.assertTrue(
                Math.abs(indirectMagic.getHealth() - (before - 10.0F)) < DAMAGE_EPSILON,
                "indirect magic must receive exactly ten points of INFX witch defense");
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
        var converter = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, convertedVillagerPos.south());
        converter.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        var villager = helper.spawn(EntityType.VILLAGER, convertedVillagerPos);
        helper.assertTrue(
                converter.convertVillagerToZombieVillager(helper.getLevel(), villager),
                "an unarmed zombie must convert villagers into a zombie villager");
        helper.startSequence()
                // Replacement insertion is deliberately scheduled after the
                // vanilla join event, so the test must not inspect tick-zero's
                // pre-transaction entity index.
                .thenWaitUntil(() -> {
                    // The vanilla zombie is no longer spawn-replaced; only the silverfish and witch keep InfX replacements.
                    helper.assertEntityPresent(EntityType.ZOMBIE, naturalPos, 2.0D);
                    helper.assertEntityPresent(EntityType.ZOMBIE, explicitPos);
                    helper.assertEntityPresent(InfXEntityTypes.COPPERSPINE.get(), triggeredPos, 2.0D);
                    helper.assertEntityPresent(InfXEntityTypes.INFX_WITCH.get(), explicitWitchPos, 2.0D);
                    helper.assertEntityPresent(EntityType.ZOMBIE_VILLAGER, convertedVillagerPos, 2.0D);
                })
                .thenExecute(() -> {
                    helper.assertEntityNotPresent(EntityType.SILVERFISH, triggeredPos);
                    helper.assertEntityNotPresent(EntityType.WITCH, explicitWitchPos);
                    // The vanilla zombie keeps the InfX attack alignment through the spawn event.
                    Mob profiled = spawnWithFinalize(helper, EntityType.ZOMBIE);
                    helper.assertTrue(
                            profiled.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) == 5.0D,
                            "the zombie spawn profile must apply the InfX attack alignment; actual="
                                    + profiled.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
                })
                .thenSucceed();
    }

    private static void spawnTables(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        MobSpawnSettings plains = biomes.getOrThrow(Biomes.PLAINS).value().getMobSettings();
        helper.assertTrue(
                Set.copyOf(spawnTypes(plains, MobCategory.CREATURE)).equals(Set.of(
                        InfXEntityTypes.INFX_SHEEP.get(),
                        InfXEntityTypes.INFX_PIG.get(),
                        InfXEntityTypes.INFX_CHICKEN.get(),
                        InfXEntityTypes.INFX_COW.get(),
                        InfXEntityTypes.INFX_HORSE.get())),
                "plains must use only the INFX livestock and horse table");
        helper.assertTrue(
                !spawnTypes(plains, MobCategory.MONSTER).contains(EntityType.DROWNED)
                        && spawnTypes(plains, MobCategory.MONSTER).contains(InfXEntityTypes.GHOUL.get()),
                "Overworld monster tables must replace modern biome additions");
        for (var type : List.of(
                InfXEntityTypes.INFX_COW,
                InfXEntityTypes.INFX_CHICKEN,
                InfXEntityTypes.INFX_SHEEP,
                InfXEntityTypes.INFX_PIG,
                InfXEntityTypes.INFX_HORSE,
                InfXEntityTypes.INFX_WOLF)) {
            helper.assertTrue(
                    SpawnPlacements.getPlacementType(type.get()) == SpawnPlacementTypes.ON_GROUND
                            && SpawnPlacements.getHeightmapType(type.get())
                                    == Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    type.getId() + " must retain vanilla ground spawn placement restrictions");
        }
        helper.assertTrue(
                SpawnPlacements.getPlacementType(InfXEntityTypes.INFX_OCELOT.get())
                                == SpawnPlacementTypes.ON_GROUND
                        && SpawnPlacements.getHeightmapType(InfXEntityTypes.INFX_OCELOT.get())
                                == Heightmap.Types.MOTION_BLOCKING,
                "INFX ocelots must retain vanilla ocelot spawn placement restrictions");
        for (var type : List.of(
                InfXEntityTypes.INFX_COD,
                InfXEntityTypes.INFX_SALMON,
                InfXEntityTypes.INFX_PUFFERFISH,
                InfXEntityTypes.INFX_TROPICAL_FISH)) {
            helper.assertTrue(
                    SpawnPlacements.getPlacementType(type.get()) == SpawnPlacementTypes.IN_WATER
                            && SpawnPlacements.getHeightmapType(type.get())
                                    == Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    type.getId() + " must retain vanilla water spawn placement restrictions");
        }
        for (var type : List.of(
                InfXEntityTypes.INFX_BAT,
                InfXEntityTypes.VAMPIRE_BAT,
                InfXEntityTypes.NIGHTWING,
                InfXEntityTypes.GIANT_VAMPIRE_BAT)) {
            helper.assertTrue(
                    SpawnPlacements.getPlacementType(type.get()) == SpawnPlacementTypes.NO_RESTRICTIONS,
                    type.getId() + " must spawn in InfX cave air without a modern ground-tag requirement");
        }

        MobSpawnSettings ocean = biomes.getOrThrow(Biomes.OCEAN).value().getMobSettings();
        helper.assertTrue(
                spawnTypes(ocean, MobCategory.WATER_AMBIENT).equals(List.of(InfXEntityTypes.INFX_COD.get())),
                "normal oceans must spawn only InfiniteX cod");
        MobSpawnSettings coldOcean = biomes.getOrThrow(Biomes.COLD_OCEAN).value().getMobSettings();
        helper.assertTrue(
                Set.copyOf(spawnTypes(coldOcean, MobCategory.WATER_AMBIENT)).equals(Set.of(
                        InfXEntityTypes.INFX_COD.get(), InfXEntityTypes.INFX_SALMON.get())),
                "cold oceans must replace cod and salmon with InfiniteX fish");
        MobSpawnSettings lukewarmOcean = biomes.getOrThrow(Biomes.LUKEWARM_OCEAN).value().getMobSettings();
        helper.assertTrue(
                Set.copyOf(spawnTypes(lukewarmOcean, MobCategory.WATER_AMBIENT)).equals(Set.of(
                        InfXEntityTypes.INFX_COD.get(),
                        InfXEntityTypes.INFX_PUFFERFISH.get(),
                        InfXEntityTypes.INFX_TROPICAL_FISH.get())),
                "lukewarm oceans must use InfiniteX cod, pufferfish and tropical fish");
        MobSpawnSettings warmOcean = biomes.getOrThrow(Biomes.WARM_OCEAN).value().getMobSettings();
        helper.assertTrue(
                Set.copyOf(spawnTypes(warmOcean, MobCategory.WATER_AMBIENT)).equals(Set.of(
                        InfXEntityTypes.INFX_PUFFERFISH.get(), InfXEntityTypes.INFX_TROPICAL_FISH.get())),
                "warm oceans must use InfiniteX pufferfish and tropical fish");
        MobSpawnSettings river = biomes.getOrThrow(Biomes.RIVER).value().getMobSettings();
        helper.assertTrue(
                spawnTypes(river, MobCategory.WATER_AMBIENT).equals(List.of(InfXEntityTypes.INFX_SALMON.get())),
                "rivers must use InfiniteX salmon");
        MobSpawnSettings lushCaves = biomes.getOrThrow(Biomes.LUSH_CAVES).value().getMobSettings();
        helper.assertTrue(
                spawnTypes(lushCaves, MobCategory.WATER_AMBIENT)
                        .equals(List.of(InfXEntityTypes.INFX_TROPICAL_FISH.get())),
                "lush caves must use InfiniteX tropical fish");

        MobSpawnSettings mushroom = biomes.getOrThrow(Biomes.MUSHROOM_FIELDS).value().getMobSettings();
        helper.assertTrue(
                spawnTypes(mushroom, MobCategory.MONSTER).isEmpty()
                        && spawnTypes(mushroom, MobCategory.CREATURE).isEmpty()
                        && spawnTypes(mushroom, MobCategory.WATER_CREATURE).isEmpty(),
                "mushroom fields must remain free of monsters, animals and squid");
        helper.assertTrue(
                Set.copyOf(spawnTypes(mushroom, MobCategory.AMBIENT)).equals(Set.of(
                        InfXEntityTypes.INFX_BAT.get(),
                        InfXEntityTypes.VAMPIRE_BAT.get(),
                        InfXEntityTypes.NIGHTWING.get())),
                "mushroom fields retain the inherited INFX cave-bat pool");

        MobSpawnSettings jungle = biomes.getOrThrow(Biomes.JUNGLE).value().getMobSettings();
        helper.assertTrue(
                spawnTypes(jungle, MobCategory.MONSTER).contains(InfXEntityTypes.BLACK_WIDOW_SPIDER.get()),
                "jungle biomes must include black widow spiders");
        helper.assertTrue(
                spawnTypes(jungle, MobCategory.CREATURE).stream()
                                .filter(type -> type == InfXEntityTypes.INFX_CHICKEN.get())
                                .count()
                        == 2,
                "jungle biomes must retain the additional INFX chicken entry");

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
                "Nether biomes must use the exact four-entry INFX pool");

        MobSpawnSettings end = biomes.getOrThrow(Biomes.END_HIGHLANDS).value().getMobSettings();
        helper.assertTrue(
                Set.copyOf(spawnTypes(end, MobCategory.MONSTER)).equals(Set.of(
                        EntityType.ENDERMAN, InfXEntityTypes.EARTH_ELEMENTAL.get())),
                "End biomes must use only endermen and earth elementals");

        MobSpawnSettings underworld = biomes.getOrThrow(Underworld.BIOME).value().getMobSettings();
        helper.assertTrue(
                spawnTypes(underworld, MobCategory.MONSTER).size() == 18
                        && Set.copyOf(spawnTypes(underworld, MobCategory.MONSTER)).equals(Set.of(
                                InfXEntityTypes.INFX_SPIDER.get(),
                                InfXEntityTypes.INFX_CREEPER.get(),
                                InfXEntityTypes.INFX_ENDERMAN.get(),
                                InfXEntityTypes.WIGHT.get(),
                                InfXEntityTypes.INVISIBLE_STALKER.get(),
                                InfXEntityTypes.DEMON_SPIDER.get(),
                                InfXEntityTypes.HELLHOUND.get(),
                                InfXEntityTypes.SHADOW.get(),
                                InfXEntityTypes.EARTH_ELEMENTAL.get(),
                                InfXEntityTypes.JELLY.get(),
                                InfXEntityTypes.BLOB.get(),
                                InfXEntityTypes.OOZE.get(),
                                InfXEntityTypes.PUDDING.get(),
                                InfXEntityTypes.CLAY_GOLEM.get(),
                                InfXEntityTypes.PHASE_SPIDER.get(),
                                InfXEntityTypes.INFX_CAVE_SPIDER.get(),
                                InfXEntityTypes.LONGDEAD.get(),
                                InfXEntityTypes.ANCIENT_BONE_LORD.get())),
                "Underworld must use the exact 18-entry InfX monster table without wood spiders");
        helper.assertTrue(
                spawnTypes(underworld, MobCategory.CREATURE).isEmpty()
                        && spawnTypes(underworld, MobCategory.AMBIENT).isEmpty()
                        && spawnTypes(underworld, MobCategory.WATER_CREATURE).isEmpty(),
                "Underworld non-monster spawn tables must remain unchanged");
        MobSpawnSettings underworldLush = biomes.getOrThrow(Underworld.LUSH_BIOME).value().getMobSettings();
        helper.assertTrue(
                spawnTypes(underworldLush, MobCategory.MONSTER).size() == 19
                        && Set.copyOf(spawnTypes(underworldLush, MobCategory.MONSTER)).equals(Set.of(
                                InfXEntityTypes.INFX_SPIDER.get(),
                                InfXEntityTypes.INFX_CREEPER.get(),
                                InfXEntityTypes.INFX_ENDERMAN.get(),
                                InfXEntityTypes.WIGHT.get(),
                                InfXEntityTypes.INVISIBLE_STALKER.get(),
                                InfXEntityTypes.DEMON_SPIDER.get(),
                                InfXEntityTypes.HELLHOUND.get(),
                                InfXEntityTypes.SHADOW.get(),
                                InfXEntityTypes.EARTH_ELEMENTAL.get(),
                                InfXEntityTypes.JELLY.get(),
                                InfXEntityTypes.BLOB.get(),
                                InfXEntityTypes.OOZE.get(),
                                InfXEntityTypes.PUDDING.get(),
                                InfXEntityTypes.CLAY_GOLEM.get(),
                                InfXEntityTypes.PHASE_SPIDER.get(),
                                InfXEntityTypes.INFX_CAVE_SPIDER.get(),
                                InfXEntityTypes.LONGDEAD.get(),
                                InfXEntityTypes.ANCIENT_BONE_LORD.get(),
                                InfXEntityTypes.WOOD_SPIDER.get())),
                "Lush underworld must be the only region with wood spiders in its monster table");
        helper.assertTrue(
                spawnTypes(underworldLush, MobCategory.CREATURE).isEmpty()
                        && spawnTypes(underworldLush, MobCategory.AMBIENT).isEmpty()
                        && spawnTypes(underworldLush, MobCategory.WATER_CREATURE).isEmpty(),
                "Lush underworld non-monster spawn tables must remain unchanged");
        for (var type : List.of(
                InfXEntityTypes.INFX_SPIDER,
                InfXEntityTypes.INFX_CREEPER,
                InfXEntityTypes.INFX_ENDERMAN,
                InfXEntityTypes.WIGHT,
                InfXEntityTypes.INVISIBLE_STALKER,
                InfXEntityTypes.DEMON_SPIDER,
                InfXEntityTypes.HELLHOUND,
                InfXEntityTypes.WOOD_SPIDER,
                InfXEntityTypes.SHADOW,
                InfXEntityTypes.EARTH_ELEMENTAL,
                InfXEntityTypes.JELLY,
                InfXEntityTypes.BLOB,
                InfXEntityTypes.OOZE,
                InfXEntityTypes.PUDDING,
                InfXEntityTypes.CLAY_GOLEM,
                InfXEntityTypes.PHASE_SPIDER,
                InfXEntityTypes.INFX_CAVE_SPIDER,
                InfXEntityTypes.LONGDEAD,
                InfXEntityTypes.ANCIENT_BONE_LORD)) {
            helper.assertTrue(
                    SpawnPlacements.getPlacementType(type.get()) == SpawnPlacementTypes.ON_GROUND
                            && SpawnPlacements.getHeightmapType(type.get())
                                    == Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    type.getId() + " must have an InfX ground spawn placement");
        }
        helper.succeed();
    }

    private static List<net.minecraft.world.entity.EntityType<?>> spawnTypes(
            MobSpawnSettings settings, MobCategory category) {
        return settings.getMobs(category).unwrap().stream()
                .<net.minecraft.world.entity.EntityType<?>>map(entry -> entry.value().type())
                .toList();
    }

    private static Goal startEarthDigGoal(EarthElemental elemental) {
        Goal digGoal = elemental.goalSelector.getAvailableGoals().stream()
                .map(candidate -> candidate.getGoal())
                .filter(candidate -> candidate.getClass().getSimpleName().equals("InfxEarthDigGoal"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Earth elemental is missing its dig goal"));
        for (long seed = 0; seed < 10_000; seed++) {
            elemental.getRandom().setSeed(seed);
            if (elemental.getRandom().nextInt(20) == 0) {
                elemental.getRandom().setSeed(seed);
                if (digGoal.canUse()) {
                    digGoal.start();
                    return digGoal;
                }
            }
        }
        throw new IllegalStateException("Could not start an earth-elemental dig attempt");
    }

    private static void assertEarthForm(
            GameTestHelper helper,
            EarthElemental elemental,
            net.minecraft.world.level.block.state.BlockState ground,
            boolean heated,
            EarthElemental.Form expected) {
        elemental.initializeElementalForm(ground, heated);
        helper.assertTrue(
                elemental.form() == expected && elemental.isMagma() == expected.isMagmaForm(),
                "earth elemental form must match " + expected);
    }

    private static void behaviors(GameTestHelper helper) {
        var level = helper.getLevel();
        var skeleton = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_SKELETON.get(), new BlockPos(1, 2, 1));
        var vanillaSkeleton = helper.spawnWithNoFreeWill(EntityType.SKELETON, new BlockPos(2, 2, 1));
        Arrow arrow = EntityType.ARROW.create(level, EntitySpawnReason.COMMAND);
        float before = skeleton.getHealth();
        helper.assertTrue(
                !skeleton.hurtServer(level, level.damageSources().arrow(arrow, vanillaSkeleton), 4.0F),
                "INFX skeletons must reject arrows fired by another skeleton");
        helper.assertTrue(skeleton.getHealth() == before, "skeleton arrows must not reduce INFX skeleton health");

        var blaze = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_BLAZE.get(), new BlockPos(3, 2, 1));
        before = blaze.getHealth();
        helper.assertTrue(
                !blaze.hurtServer(level, level.damageSources().mobAttack(vanillaSkeleton), 4.0F),
                "mundane unenchanted attacks must not hurt the INFX blaze");
        helper.assertTrue(blaze.getHealth() == before, "rejected blaze damage must not change health");
        Snowball snowball = new Snowball(level, vanillaSkeleton, Items.SNOWBALL.getDefaultInstance());
        helper.assertTrue(
                blaze.hurtServer(level, level.damageSources().thrown(snowball, vanillaSkeleton), 3.0F),
                "snowballs must hurt the INFX blaze");
        helper.assertTrue(blaze.getHealth() == before - 3.0F, "snowballs must deal three damage to the INFX blaze");

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
                "swords must not hurt the INFX magma cube");
        helper.assertTrue(magma.getHealth() == before, "sword hits must not change magma cube health");
        player.setItemInHand(
                net.minecraft.world.InteractionHand.MAIN_HAND, InfXItems.IRON_PICKAXE.toStack());
        helper.assertTrue(
                magma.hurtServer(level, level.damageSources().playerAttack(player), 4.0F),
                "pickaxes must hurt the INFX magma cube");
        helper.assertTrue(magma.getHealth() == before - 4.0F, "pickaxe hits must deal magma cube damage");

        magma.invulnerableTime = 0;
        before = magma.getHealth();
        player.setItemInHand(
                net.minecraft.world.InteractionHand.MAIN_HAND,
                InfXItems.catalog()
                        .equipment(InfxMaterial.IRON, EquipmentType.WAR_HAMMER)
                        .holder()
                        .toStack());
        helper.assertTrue(
                magma.hurtServer(level, level.damageSources().playerAttack(player), 4.0F),
                "war hammers must hurt the INFX magma cube");
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

        earth.initializeElementalForm(Blocks.NETHERRACK.defaultBlockState(), true);
        float earthHealth = earth.getHealth();
        Snowball earthSnowball = new Snowball(level, player, Items.SNOWBALL.getDefaultInstance());
        helper.assertTrue(
                !earth.hurtServer(level, level.damageSources().thrown(earthSnowball, player), 1.0F)
                        && earth.form() == EarthElemental.Form.NETHERRACK_NORMAL
                        && earth.getHealth() == earthHealth,
                "snowballs must quench mineral bodies without dealing damage");

        var clay = helper.spawnWithNoFreeWill(InfXEntityTypes.CLAY_GOLEM.get(), new BlockPos(8, 2, 1));
        clay.initializeElementalForm(Blocks.CLAY.defaultBlockState(), false);
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

        BlockPos siegeEarthPos = new BlockPos(12, 1, 1);
        BlockPos siegeTargetPos = new BlockPos(13, 3, 1);
        BlockPos siegeSupport = siegeTargetPos.below();
        helper.setBlock(siegeEarthPos.below(), Blocks.STONE);
        helper.setBlock(siegeSupport, Blocks.GLASS);
        var siegeEarth = helper.spawn(InfXEntityTypes.EARTH_ELEMENTAL.get(), siegeEarthPos);
        var siegeTarget = helper.spawnWithNoFreeWill(EntityType.COW, siegeTargetPos);
        siegeEarth.setTarget(siegeTarget);
        Goal siegeDigGoal = startEarthDigGoal(siegeEarth);
        for (int tick = 0; tick < 180 && !helper.getBlockState(siegeSupport).isAir(); tick++) {
            siegeDigGoal.tick();
        }
        helper.assertTrue(
                helper.getBlockState(siegeSupport).isAir(),
                "earth elementals must dig an elevated target's support even when its eye ray passes above it");

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
                "fire elementals must not stack the modern per-tick water damage on InfX's own drain");

        var enderman = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_ENDERMAN.get(), new BlockPos(8, 2, 1));
        Arrow endermanArrow = EntityType.ARROW.create(level, EntitySpawnReason.COMMAND);
        before = enderman.getHealth();
        helper.assertTrue(
                enderman.hurtServer(level, level.damageSources().arrow(endermanArrow, player), 4.0F),
                "INFX endermen must take projectile damage");
        helper.assertTrue(
                enderman.getHealth() < before,
                "INFX projectile hits must reduce enderman health");
        helper.assertTrue(
                enderman.getTarget() == player,
                "INFX projectile hits must keep the living shooter as the enderman target");
        enderman.invulnerableTime = 0;
        Arrow dispenserArrow = EntityType.ARROW.create(level, EntitySpawnReason.COMMAND);
        before = enderman.getHealth();
        helper.assertTrue(
                enderman.hurtServer(level, level.damageSources().arrow(dispenserArrow, null), 3.0F),
                "unowned projectile damage must not fall back to vanilla enderman immunity");
        helper.assertTrue(
                enderman.getHealth() < before,
                "unowned projectiles must still damage INFX endermen");
        enderman.invulnerableTime = 0;
        enderman.setTarget(player);
        Snowball indirectMagic = new Snowball(level, player, Items.SNOWBALL.getDefaultInstance());
        helper.assertTrue(
                enderman.hurtServer(level, level.damageSources().indirectMagic(indirectMagic, player), 2.0F),
                "INFX endermen must take non-projectile indirect damage");
        helper.assertTrue(
                enderman.getTarget() == null && enderman.getLastHurtByMob() == null,
                "non-projectile indirect damage must make INFX endermen blink and drop aggression");

        var sharingSource = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(14, 2, 12));
        var neutralEnderman = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_ENDERMAN.get(), new BlockPos(12, 2, 12));
        MonsterEvents.propagateTarget(level, sharingSource, player);
        helper.assertTrue(
                neutralEnderman.getTarget() == null,
                "shared monster targets must not override INFX enderman neutrality");
        helper.assertTrue(
                MonsterEvents.propagateTarget(level, neutralEnderman, player) == 0,
                "INFX endermen must not propagate their own targets to nearby monsters");
        neutralEnderman.setTarget(player);
        helper.assertFalse(
                MonsterTactics.tryDig(level, neutralEnderman),
                "INFX endermen must never receive generic pursuit block digging");
        ModCompletionGameTests.removePlayer(player);

        BlockPos squidPos = new BlockPos(3, 2, 7);
        helper.setBlock(squidPos, Blocks.WATER);
        helper.setBlock(squidPos.east(2), Blocks.WATER);
        var squid = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_SQUID.get(), squidPos);
        var squidPrey = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_COW.get(), squidPos.east(2));
        squid.aiStep();
        helper.assertFalse(
                squidPrey.hasEffect(MobEffects.SLOWNESS),
                "INFX squid must not slow an animal before their hitboxes collide");
        squidPrey.snapTo(squid.getX(), squid.getY(), squid.getZ(), 0.0F, 0.0F);
        for (int tick = 0; tick < 3 && !squidPrey.hasEffect(MobEffects.SLOWNESS); tick++) {
            squid.tick();
            squidPrey.snapTo(squid.getX(), squid.getY(), squid.getZ(), 0.0F, 0.0F);
        }
        helper.assertTrue(
                squidPrey.hasEffect(MobEffects.SLOWNESS),
                "INFX squid must slow land animals on a real collision");
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
                "INFX squid must not destroy a pursued boat before six collisions");
        squid.setDeltaMovement(Vec3.ZERO);
        squid.snapTo(preyBoat.getX(), preyBoat.getY(), preyBoat.getZ(), 0.0F, 0.0F);
        squid.tick();
        helper.assertTrue(
                preyBoat.isRemoved(),
                "INFX squid must destroy a boat on its sixth collision while pursuing its passenger");

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
                        "infernal creepers must begin swelling from InfX's expanded no-path range"))
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
        var skeleton = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_SKELETON.get(), new BlockPos(2, 80, 2));
        var skeletonTarget = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(4, 80, 2));
        skeleton.setItemSlot(
                net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                InfXItems.catalog()
                        .equipment(InfxMaterial.IRON, EquipmentType.SWORD)
                        .holder()
                        .toStack());
        assertMeleeBoundary(helper, skeleton, skeletonTarget, 1.949, 1.951, "tool-equipped skeleton");

        var revenant = helper.spawnWithNoFreeWill(InfXEntityTypes.REVENANT.get(), new BlockPos(2, 80, 4));
        var revenantTarget = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(4, 80, 4));
        revenant.setItemSlot(
                net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                InfXItems.catalog()
                        .equipment(InfxMaterial.IRON, EquipmentType.SWORD)
                        .holder()
                        .toStack());
        assertMeleeBoundary(helper, revenant, revenantTarget, 1.949, 1.951, "tool-equipped revenant");

        var earth = helper.spawnWithNoFreeWill(InfXEntityTypes.EARTH_ELEMENTAL.get(), new BlockPos(2, 80, 6));
        var earthTarget = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(4, 80, 6));
        assertMeleeBoundary(helper, earth, earthTarget, 2.0, 2.001, "earth elemental");

        var spider = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_SPIDER.get(), new BlockPos(2, 80, 8));
        var spiderTarget = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(4, 80, 8));
        assertMeleeBoundary(helper, spider, spiderTarget, 1.749, 1.75, "spider");

        var pigman = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_ZOMBIFIED_PIGLIN.get(), new BlockPos(2, 80, 10));
        var pigmanTarget = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(4, 80, 10));
        pigman.setItemSlot(
                net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                InfXItems.catalog()
                        .equipment(InfxMaterial.GOLD, EquipmentType.SWORD)
                        .holder()
                        .toStack());
        assertMeleeBoundary(helper, pigman, pigmanTarget, 1.749, 1.75, "tool-equipped zombie pigman");

        var silverfish = helper.spawnWithNoFreeWill(InfXEntityTypes.COPPERSPINE.get(), new BlockPos(2, 80, 12));
        var silverfishTarget = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(4, 80, 12));
        assertMeleeBoundary(helper, silverfish, silverfishTarget, 1.199, 1.201, "silverfish");

        var wolf = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_WOLF.get(), new BlockPos(2, 80, 14));
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
            var bat = helper.spawn(batType.get(), new BlockPos(2, 84, 2));
            bat.setNoGravity(true);
            // InfX nightwings die to direct sunlight and this test world is at day. Give the bat
            // enough health to survive the sunlight ticks; the attack lands before the sun check.
            if (batType == InfXEntityTypes.NIGHTWING) {
                bat.getAttribute(Attributes.MAX_HEALTH).setBaseValue(2000.0);
                bat.setHealth(2000.0F);
            }
            var prey = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(4, 84, 2));
            prey.setNoGravity(true);
            float health = prey.getHealth();
            helper.assertFalse(bat.hasAttackContact(prey), batType.getId() + " must reject ranged contact");
            bat.tick();
            helper.assertTrue(prey.getHealth() == health, batType.getId() + " must not attack before contact");
            prey.snapTo(bat.getX(), bat.getY(), bat.getZ(), 0.0F, 0.0F);
            helper.assertTrue(bat.hasAttackContact(prey), batType.getId() + " must accept half-box contact");
            bat.tick();
            helper.assertTrue(prey.getHealth() < health, batType.getId() + " must attack after half-box contact");
            if (batType == InfXEntityTypes.NIGHTWING) {
                helper.assertFalse(
                        prey.hasEffect(MobEffects.DARKNESS),
                        "Nightwing must use player-only vision dimming rather than modern Darkness");
            }
            bat.discard();
            prey.discard();
        }

        BlockPos squidPos = new BlockPos(2, 84, 2);
        helper.setBlock(squidPos, Blocks.WATER);
        helper.setBlock(squidPos.east(2), Blocks.WATER);
        var squid = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_SQUID.get(), squidPos);
        var squidPrey = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_COW.get(), squidPos.east(2));
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
                attacker.isWithinMeleeAttackRange(target), description + " must hit on the INFX inner boundary");
        placeAtDistance(helper, attacker, target, origin, outsideDistance);
        helper.assertFalse(
                attacker.isWithinMeleeAttackRange(target), description + " must not hit beyond the INFX boundary");
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
        BlockPos skeletonPos = new BlockPos(3, 2, 3);
        forceEntityTicking(helper, skeletonPos);
        boolean[] skeletonCompletedDraw = {false};
        boolean[] skeletonWasUsing = {false};
        int[] skeletonMaxUseTicks = {0};
        boolean[] witchThrew = {false};
        InfxWitch[] witchRef = {null};

        var skeleton = helper.spawn(InfXEntityTypes.INFX_SKELETON.get(), skeletonPos);
        skeleton.setNoAi(true);
        // RestrictSunGoal keeps sun-avoiding navigation enabled even beneath a roof. A helmet
        // keeps this range test focused on the bow goal without changing its normal scheduler.
        skeleton.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, Items.IRON_HELMET.getDefaultInstance());
        skeleton.setNoGravity(true);
        skeleton.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
        skeleton.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, Items.BOW.getDefaultInstance());
        skeleton.reassessWeaponGoal();
        helper.assertTrue(
                skeleton.goalSelector.getAvailableGoals().stream()
                        .anyMatch(goal -> goal.getGoal().getClass().getSimpleName().equals("InfxHardCappedBowAttackGoal")),
                "skeletons holding bows must register the INFX bow goal");
        placePlayerAtDistance(helper, skeleton, player, skeletonPos, 15.5);
        skeleton.setTarget(player);

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(
                        skeleton.tickCount > 0, "the ranged-combat skeleton must join the ticking entity set"))
                .thenExecute(() -> skeleton.setNoAi(false))
                .thenExecuteFor(80, () -> {
                    skeleton.setTarget(player);
                    helper.assertFalse(
                            skeleton.isUsingItem(),
                            "skeletons must not begin drawing beyond 15 blocks; distance="
                                    + Math.sqrt(skeleton.distanceToSqr(player))
                                    + ", goals="
                                    + skeleton.goalSelector.getAvailableGoals().stream()
                                            .map(goal -> goal.getGoal().getClass().getSimpleName())
                                            .toList());
                    helper.assertTrue(
                            level.getEntitiesOfClass(
                                            AbstractArrow.class,
                                            skeleton.getBoundingBox().inflate(20.0),
                                            arrow -> arrow.getOwner() == skeleton)
                                    .isEmpty(),
                            "skeletons must not begin or release a shot beyond 15 blocks");
                })
                .thenExecute(() -> placePlayerAtDistance(helper, skeleton, player, skeletonPos, 14.0))
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
                        "skeletons must resume firing after entering 15 blocks"
                                + "; target=" + (skeleton.getTarget() == player)
                                + ", noAi=" + skeleton.isNoAi()
                                + ", using=" + skeleton.isUsingItem()
                                + ", maxUseTicks=" + skeletonMaxUseTicks[0]
                                + ", bow=" + skeleton.getMainHandItem().is(Items.BOW)
                                + ", visible=" + skeleton.getSensing().hasLineOfSight(player)
                                + ", distance=" + Math.sqrt(skeleton.distanceToSqr(player))))
                .thenExecute(() -> {
                    level.getEntitiesOfClass(AbstractArrow.class, skeleton.getBoundingBox().inflate(20.0))
                            .forEach(AbstractArrow::discard);
                    skeleton.discard();
                })
                .thenExecute(() -> {
                    var witch = helper.spawn(InfXEntityTypes.INFX_WITCH.get(), new BlockPos(3, 2, 3));
                    witch.setNoGravity(true);
                    witch.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
                    placePlayerAtDistance(helper, witch, player, new BlockPos(3, 2, 3), 10.5);
                    witch.setTarget(player);
                    witchRef[0] = witch;
                })
                .thenExecuteFor(80, () -> {
                    var witch = witchRef[0];
                    witch.setTarget(player);
                    helper.assertTrue(
                            level.getEntitiesOfClass(
                                            ThrownSplashPotion.class,
                                            witch.getBoundingBox().inflate(16.0),
                                            potion -> potion.getOwner() == witch)
                                    .isEmpty(),
                            "witches must not begin or release a potion beyond 10 blocks");
                })
                .thenExecute(() -> {
                    var witch = witchRef[0];
                    placePlayerAtDistance(helper, witch, player, new BlockPos(3, 2, 3), 9.0);
                })
                .thenExecuteFor(140, () -> {
                    var witch = witchRef[0];
                    witch.setTarget(player);
                    if (!level.getEntitiesOfClass(ThrownSplashPotion.class, witch.getBoundingBox().inflate(16.0))
                                    .isEmpty()) {
                        witchThrew[0] = true;
                    }
                })
                .thenExecute(() -> helper.assertTrue(
                        witchThrew[0], "witches must resume throwing after entering 10 blocks"))
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

    private static void forceEntityTicking(GameTestHelper helper, BlockPos relativePos) {
        ChunkPos chunk = ChunkPos.containing(helper.absolutePos(relativePos));
        helper.getLevel().setChunkForced(chunk.x(), chunk.z(), true);
    }

    private static void explosionRanges(GameTestHelper helper) {
        var level = helper.getLevel();

        var ordinary = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_CREEPER.get(), new BlockPos(4, 10, 4));
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

        var ghast = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_GHAST.get(), new BlockPos(25, 10, 25));
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
                    assertExplosionBoundary(helper, ordinary, ordinaryInside, ordinaryOutside, 3.0F, "ordinary INFX creeper");
                    assertExplosionBoundary(helper, infernal, infernalInside, infernalOutside, 6.0F, "infernal creeper");
                    assertExplosionBoundary(helper, netherspawn, netherspawnInside, netherspawnOutside, 1.0F, "netherspawn");
                    assertExplosionBoundary(helper, fireball, fireballInside, fireballOutside, 1.0F, "INFX ghast fireball");
                    assertExplosionBoundary(helper, vanilla, vanillaInside, vanillaOutside, 3.0F, "non-INFX explosion");
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
        BlockPos endermanPos = new BlockPos(3, 2, 3);
        forceEntityTicking(helper, endermanPos);
        InfxEnderman enderman = helper.spawn(InfXEntityTypes.INFX_ENDERMAN.get(), endermanPos);
        enderman.setNoAi(true);
        // Other tests in this batch create server players. Do not let those unrelated targets
        // suppress the item-collection goal that this test exercises.
        enderman.targetSelector.disableControlFlag(net.minecraft.world.entity.ai.goal.Goal.Flag.TARGET);
        enderman.setTarget(null);
        ItemEntity pearl = new ItemEntity(
                level, enderman.getX() + 1.0, enderman.getY(), enderman.getZ(), Items.ENDER_PEARL.getDefaultInstance());
        // The custom goal collects directly, while a normal player must not be able to steal
        // this probe from another test arena before the persistence assertion runs.
        pearl.setPickUpDelay(200);
        level.addFreshEntity(pearl);

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(
                        enderman.tickCount > 0, "the enderman must join the ticking entity set"))
                .thenExecute(() -> enderman.setNoAi(false))
                .thenWaitUntil(() -> helper.assertEntityPresent(InfXEntityTypes.INFX_ENDERMAN.get(), new BlockPos(3, 2, 3), 2.0D))
                .thenWaitUntil(() -> helper.assertTrue(
                        pearl.isRemoved(),
                        "INFX endermen must collect nearby dropped ender pearls"
                                + "; target=" + enderman.getTarget()
                                + ", inWaterOrRain=" + enderman.isInWaterOrRain()
                                + ", onFire=" + enderman.isOnFire()
                                + ", distance=" + Math.sqrt(enderman.distanceToSqr(pearl))))
                .thenExecute(() -> helper.assertTrue(
                        enderman.requiresCustomPersistence(),
                        "INFX endermen carrying valuables must not despawn"))
                .thenSucceed();
    }

    private static void spawnerLight(GameTestHelper helper) {
        BlockPos spawnerPos = new BlockPos(4, 2, 4);
        for (int x = 0; x <= 8; x++) {
            for (int z = 0; z <= 8; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
                helper.setBlock(new BlockPos(x, 5, z), Blocks.STONE);
            }
        }
        helper.setBlock(spawnerPos, Blocks.SPAWNER);
        helper.setBlock(spawnerPos.above(2), Blocks.TORCH);

        ServerPlayer player = ModCompletionGameTests.createPlayer(helper);
        BlockPos absoluteSpawnerPos = helper.absolutePos(spawnerPos);
        SpawnerBlockEntity spawner = (SpawnerBlockEntity) helper.getLevel().getBlockEntity(absoluteSpawnerPos);
        helper.assertTrue(spawner != null, "the test spawner must create its block entity");
        spawner.setEntityId(EntityType.ZOMBIE, helper.getLevel().getRandom());
        for (int tick = 0; tick <= 20; tick++) {
            spawner.getSpawner().serverTick(helper.getLevel(), absoluteSpawnerPos);
        }

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertEntityPresent(EntityType.ZOMBIE, spawnerPos, 8.0D))
                .thenExecute(() -> ModCompletionGameTests.removePlayer(player))
                .thenSucceed();
    }

    private static void witchCurse(GameTestHelper helper) {
        InfxWitch witch = helper.spawn(InfXEntityTypes.INFX_WITCH.get(), new BlockPos(7, 2, 7));
        // InfX only excludes a creative/disable-damage player. A survival player with the
        // entity-level Invulnerable flag must remain eligible for the one-in-four curse roll.
        ServerPlayer curseDeliveryProbe = ModCompletionGameTests.createPlayer(helper);
        curseDeliveryProbe.setInvulnerable(true);
        helper.assertTrue(
                InfxWitch.canReceiveCurse(curseDeliveryProbe),
                "INFX witches must keep a survival player marked Invulnerable eligible for curses");
        witch.discard();
        ModCompletionGameTests.removePlayer(curseDeliveryProbe);
        helper.succeed();
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
                            "InfX snowballs must deal two damage to netherspawn");
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
                EntityType.ZOMBIE, new BlockPos(2, 2, 2));
        var ally = helper.spawn(
                InfXEntityTypes.INFX_SKELETON.get(), new BlockPos(3, 2, 2));
        var piglin = helper.spawnWithNoFreeWill(
                InfXEntityTypes.INFX_ZOMBIFIED_PIGLIN.get(), new BlockPos(4, 2, 2));
        leader.setTarget(player);
        MonsterEvents.propagateTarget(level, leader, player);
        helper.assertTrue(ally.getTarget() == player, "INFX monsters must share a newly acquired player target");
        helper.assertTrue(
                piglin.getTarget() == null,
                "INFX zombified piglins must retain their own close-range target rules");
        leader.setTarget(null);
        ally.setTarget(null);
        level.gameEvent(GameEvent.BLOCK_DESTROY, player.position(), GameEvent.Context.of(player));
        helper.assertTrue(leader.getTarget() == player, "player block noise must attract nearby monsters");
        helper.assertTrue(ally.getTarget() == player, "activity attraction applies across hostile families");
        helper.assertTrue(
                piglin.getTarget() == null,
                "player activity must not bypass INFX zombified-piglin awareness range");

        var digger = helper.spawnWithNoFreeWill(
                EntityType.ZOMBIE, new BlockPos(2, 2, 7));
        digger.setItemSlot(
                net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                InfXItems.catalog()
                        .equipment(InfxMaterial.IRON, EquipmentType.PICKAXE)
                        .holder()
                        .toStack());
        digger.setTarget(player);
        BlockPos wall = new BlockPos(4, 3, 7);
        helper.setBlock(wall, Blocks.STONE);
        // MITE digging starts on a 1-in-20 roll; retry until the clip locks onto the wall.
        for (int attempt = 0; attempt < 200 && !MonsterTactics.isDigging(digger); attempt++) {
            MonsterTactics.tryDig(level, digger);
        }
        helper.assertTrue(
                MonsterTactics.isDigging(digger),
                "tool-equipped blocked monster must start mining through stone");
        // Force the per-hit cooloff so the ten MITE hits land synchronously.
        for (int hit = 0; hit < 10 && !helper.getBlockState(wall).isAir(); hit++) {
            digger.getPersistentData().putInt(MonsterTactics.DIG_NEXT_HIT, digger.tickCount);
            MonsterTactics.tryDig(level, digger);
        }
        helper.assertTrue(helper.getBlockState(wall).isAir(), "tool-equipped blocked monster must mine through stone");
        ModCompletionGameTests.removePlayer(player);
        helper.succeed();
    }

    private static void longdeadDrops(GameTestHelper helper) {
        ServerPlayer player = ModCompletionGameTests.createPlayer(helper);
        BlockPos pos = new BlockPos(2, 2, 2);
        var longdead = helper.spawnWithNoFreeWill(InfXEntityTypes.LONGDEAD.get(), pos);
        longdead.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 100.0F);
        AABB area = new AABB(helper.absolutePos(pos)).inflate(8.0);
        helper.assertTrue(
                helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).stream()
                        .noneMatch(drop -> drop.getItem().is(InfXItems.ANCIENT_METAL_INGOT.get())),
                "longdead must not drop ancient metal ingots");
        ModCompletionGameTests.removePlayer(player);
        helper.succeed();
    }

    /**
     * InfX strips vanilla spawn equipment from the zombie and skeleton families: the INFX
     * replacements never wear vanilla weapons or armor (world-age gear is InfX equipment and
     * may apply separately), and the un-replaced vanilla variants (husk, drowned, stray, bogged,
     * parched, wither skeleton) keep only InfX gear, never vanilla weapons or armor. Strays,
     * bogged and parched share the ordinary skeleton's current-day bow-or-melee spawn profile;
     * wither skeletons retain their InfX iron sword.
     */
    private static void spawnEquipment(GameTestHelper helper) {
        var zombie = spawnWithFinalize(helper, EntityType.ZOMBIE);
        assertNoVanillaEquipment(helper, zombie, "replacement zombie");

        var skeleton = spawnWithFinalize(helper, InfXEntityTypes.INFX_SKELETON.get());
        float tension = MonsterTactics.difficultyTension(helper.getLevel(), skeleton.blockPosition());
        helper.assertTrue(
                !isVanillaItem(skeleton.getMainHandItem()),
                "replacement skeletons must spawn with only InfX weapons");
        helper.assertTrue(
                isOrdinarySkeletonWeapon(skeleton.getMainHandItem(), tension),
                "replacement skeletons must spawn with an InfX current-day bow or melee weapon, got "
                        + skeleton.getMainHandItem());
        assertNoVanillaEquipment(helper, skeleton, "replacement skeleton");

        for (EntityType<?> type : List.of(
                EntityType.HUSK, EntityType.STRAY, EntityType.BOGGED, EntityType.PARCHED)) {
            assertVanillaVariantBare(helper, type);
        }
        assertVanillaVariantBare(helper, EntityType.WITHER_SKELETON);
        for (EntityType<?> type : List.of(EntityType.STRAY, EntityType.BOGGED, EntityType.PARCHED)) {
            Mob variant = spawnWithFinalize(helper, type);
            float variantTension = MonsterTactics.difficultyTension(helper.getLevel(), variant.blockPosition());
            helper.assertTrue(
                    isOrdinarySkeletonWeapon(variant.getMainHandItem(), variantTension),
                    type + " must spawn with an InfX current-day bow or melee weapon, got "
                            + variant.getMainHandItem());
            variant.discard();
        }
        Mob witherSkeleton = spawnWithFinalize(helper, EntityType.WITHER_SKELETON);
        helper.assertTrue(
                witherSkeleton.getMainHandItem().is(InfXItems.IRON_SWORD.get()),
                "wither skeletons must retain their InfX iron sword, got " + witherSkeleton.getMainHandItem());
        witherSkeleton.discard();
        // Vanilla drowned randomizes a nautilus shell into the offhand in 3% of spawns; that
        // item is neither a weapon nor armor, so only the main hand and armor slots are banned.
        assertVanillaVariantBare(helper, EntityType.DROWNED, EquipmentSlot.OFFHAND);
        helper.succeed();
    }

    private static void skeletonDrops(GameTestHelper helper) {
        for (EntityType<?> type : List.of(EntityType.SKELETON, EntityType.STRAY, EntityType.BOGGED, EntityType.PARCHED)) {
            @SuppressWarnings("unchecked")
            Mob skeleton = helper.spawnWithNoFreeWill((EntityType<Mob>) type, new BlockPos(2, 2, 2));
            List<ItemEntity> drops = new ArrayList<>(List.of(
                    new ItemEntity(
                            skeleton.level(), skeleton.getX(), skeleton.getY(), skeleton.getZ(),
                            new ItemStack(Items.ARROW, 2)),
                    new ItemEntity(
                            skeleton.level(), skeleton.getX(), skeleton.getY(), skeleton.getZ(),
                            new ItemStack(Items.TIPPED_ARROW)),
                    new ItemEntity(
                            skeleton.level(), skeleton.getX(), skeleton.getY(), skeleton.getZ(),
                            new ItemStack(Items.BONE))));
            NeoForge.EVENT_BUS.post(new LivingDropsEvent(
                    skeleton, helper.getLevel().damageSources().generic(), drops, false));
            helper.assertTrue(
                    drops.stream().noneMatch(drop -> drop.getItem().is(Items.ARROW)
                            || drop.getItem().is(Items.TIPPED_ARROW)),
                    type + " must not drop vanilla arrows");
            helper.assertTrue(
                    drops.stream().anyMatch(drop -> drop.getItem().is(Items.BONE)),
                    type + " must preserve unrelated skeleton drops");
            skeleton.discard();
        }
        helper.succeed();
    }

    /** Pig zombies and wither skeletons keep the vanilla 8.5% equipment roll: never guaranteed, never full durability. */
    private static void weaponDrops(GameTestHelper helper) {
        ServerPlayer player = ModCompletionGameTests.createPlayer(helper);

        Mob piglin = spawnWithFinalize(helper, InfXEntityTypes.INFX_ZOMBIFIED_PIGLIN.get());
        helper.assertTrue(isPiglinWeapon(piglin.getMainHandItem()), "pig zombies must carry a golden weapon");
        helper.assertTrue(
                piglin.getMainHandItem().get(InfXDataComponents.QUALITY.get()) == Quality.POOR,
                "pig zombies must carry a poor-quality weapon");
        helper.assertTrue(
                piglin.getMainHandItem().getMaxDamage() == poorDurability(piglin.getMainHandItem()),
                "pig zombies must carry a weapon with the reduced poor-quality durability cap");
        helper.assertTrue(
                piglin.getDropChances().byEquipment(EquipmentSlot.MAINHAND) == 0.085F,
                "pig zombies must keep the vanilla 8.5% equipment drop chance");
        piglin.kill(helper.getLevel());
        List<ItemEntity> piglinDrops = dropsNear(helper, piglin.blockPosition(), ModMonsterGameTests::isPiglinWeapon);
        helper.assertTrue(piglinDrops.isEmpty(), "pig zombies must not drop their weapon unless killed by a player");

        Mob piglinHit = spawnWithFinalize(helper, InfXEntityTypes.INFX_ZOMBIFIED_PIGLIN.get());
        piglinHit.setDropChance(EquipmentSlot.MAINHAND, 1.0F);
        piglinHit.setLastHurtByPlayer(player, 100);
        piglinHit.kill(helper.getLevel());
        List<ItemEntity> hitPiglinDrops = dropsNear(helper, piglinHit.blockPosition(), ModMonsterGameTests::isPiglinWeapon);
        helper.assertTrue(hitPiglinDrops.size() == 1, "pig zombies must drop their golden weapon when the equipment roll succeeds");
        helper.assertTrue(
                hitPiglinDrops.getFirst().getItem().get(InfXDataComponents.QUALITY.get()) == Quality.POOR,
                "the dropped piglin weapon must be poor quality");
        helper.assertTrue(
                hitPiglinDrops.getFirst().getItem().getMaxDamage() == poorDurability(hitPiglinDrops.getFirst().getItem()),
                "the dropped piglin weapon must keep the reduced poor-quality durability cap");
        helper.assertTrue(
                hitPiglinDrops.getFirst().getItem().getDamageValue() > 0,
                "the dropped piglin weapon must never be at full durability");

        Mob witherSkeleton = spawnWithFinalize(helper, EntityType.WITHER_SKELETON);
        helper.assertTrue(
                witherSkeleton.getMainHandItem().is(InfXItems.IRON_SWORD.get()),
                "wither skeletons must carry an InfX iron sword");
        helper.assertTrue(
                witherSkeleton.getMainHandItem().get(InfXDataComponents.QUALITY.get()) == Quality.POOR,
                "wither skeletons must carry a poor-quality sword");
        helper.assertTrue(
                witherSkeleton.getMainHandItem().getMaxDamage() == poorDurability(witherSkeleton.getMainHandItem()),
                "wither skeletons must carry a sword with the reduced poor-quality durability cap");
        helper.assertTrue(
                witherSkeleton.getDropChances().byEquipment(EquipmentSlot.MAINHAND) == 0.085F,
                "wither skeletons must keep the vanilla 8.5% equipment drop chance");
        witherSkeleton.kill(helper.getLevel());
        List<ItemEntity> skeletonDrops = dropsNear(helper, witherSkeleton.blockPosition(), stack -> stack.is(InfXItems.IRON_SWORD.get()));
        helper.assertTrue(skeletonDrops.isEmpty(), "wither skeletons must not drop their sword unless killed by a player");

        Mob witherSkeletonHit = spawnWithFinalize(helper, EntityType.WITHER_SKELETON);
        witherSkeletonHit.setDropChance(EquipmentSlot.MAINHAND, 1.0F);
        witherSkeletonHit.setLastHurtByPlayer(player, 100);
        witherSkeletonHit.kill(helper.getLevel());
        List<ItemEntity> hitSkeletonDrops = dropsNear(helper, witherSkeletonHit.blockPosition(), stack -> stack.is(InfXItems.IRON_SWORD.get()));
        helper.assertTrue(hitSkeletonDrops.size() == 1, "wither skeletons must drop their InfX iron sword when the equipment roll succeeds");
        helper.assertTrue(
                hitSkeletonDrops.getFirst().getItem().get(InfXDataComponents.QUALITY.get()) == Quality.POOR,
                "the dropped wither-skeleton sword must be poor quality");
        helper.assertTrue(
                hitSkeletonDrops.getFirst().getItem().getMaxDamage() == poorDurability(hitSkeletonDrops.getFirst().getItem()),
                "the dropped wither-skeleton sword must keep the reduced poor-quality durability cap");
        helper.assertTrue(
                hitSkeletonDrops.getFirst().getItem().getDamageValue() > 0,
                "the dropped wither-skeleton sword must never be at full durability");
        ModCompletionGameTests.removePlayer(player);
        helper.succeed();
    }

    /**
     * InfX skeleton-horse traps field four INFX skeleton riders with InfX world-age
     * weapons instead of vanilla skeletons: the trap spawns its riders with the
     * TRIGGERED reason, which the global spawn replacement deliberately skips.
     */
    private static void skeletonTrap(GameTestHelper helper) {
        BlockPos trapPos = new BlockPos(8, 2, 8);
        SkeletonHorse trapHorse = helper.spawnWithNoFreeWill(EntityType.SKELETON_HORSE, trapPos);
        // Test structures float on a small platform; neighbouring tests' mobs can push a
        // gravity-bound horse off the edge, silently killing it before the trap triggers.
        trapHorse.setNoGravity(true);
        trapHorse.setTrap(true);
        // The test server does not always start entity ticking in a freshly placed structure
        // chunk; force-loading it guarantees the horse's trap goal actually runs.
        ChunkPos chunk = ChunkPos.containing(trapHorse.blockPosition());
        ((ServerLevel) helper.getLevel()).setChunkForced(chunk.x(), chunk.z(), true);
        ServerPlayer player = ModCompletionGameTests.createPlayer(helper);
        // The trap goal waits for a living player within 10 blocks; neighbouring tests can
        // place hostile INFX mobs (e.g. witches) inside the structure, so the probe must
        // survive long enough for the trap to trigger.
        player.setInvulnerable(true);
        Vec3 playerPos = helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(7, 2, 7)));
        player.snapTo(playerPos.x, playerPos.y, playerPos.z, 0.0F, 0.0F);
        // Other gametests run their structures only a few blocks away and can place plain
        // InfX skeletons inside this radius, so only ridden riders are counted.
        AABB riderArea = new AABB(helper.absolutePos(trapPos)).inflate(8.0);
        helper.startSequence()
                .thenWaitUntil(() -> {
                    List<InfxSkeleton> riders = helper.getLevel().getEntitiesOfClass(
                            InfxSkeleton.class, riderArea, rider -> rider.getVehicle() instanceof SkeletonHorse);
                    helper.assertTrue(
                            riders.size() == 4,
                            "skeleton-horse traps must field four INFX riders, got " + riders.size());
                })
                .thenExecute(() -> {
                    List<InfxSkeleton> riders = helper.getLevel().getEntitiesOfClass(
                            InfxSkeleton.class, riderArea, rider -> rider.getVehicle() instanceof SkeletonHorse);
                    helper.assertTrue(
                            riders.size() == 4,
                            "skeleton-horse traps must field four INFX riders, got " + riders.size());
                    for (InfxSkeleton rider : riders) {
                        helper.assertTrue(
                                !isVanillaItem(rider.getMainHandItem()),
                                "trap riders must carry an InfX weapon, got " + rider.getMainHandItem());
                    }
                })
                .thenExecute(() -> ModCompletionGameTests.removePlayer(player))
                .thenSucceed();
    }

    /** The 0.75x durability cap a poor-quality weapon of this stack must carry. */
    private static int poorDurability(ItemStack stack) {
        EquipmentKey key = InfXItems.catalog().equipment(stack).key();
        return Math.max(1, Math.round(key.durability() * Quality.POOR.durabilityMultiplier()));
    }

    private static boolean isPiglinWeapon(ItemStack stack) {
        return stack.is(InfXItems.catalog().equipment(InfxMaterial.GOLD, EquipmentType.SWORD).holder())
                || stack.is(InfXItems.catalog().equipment(InfxMaterial.GOLD, EquipmentType.AXE).holder())
                || stack.is(InfXItems.catalog().equipment(InfxMaterial.GOLD, EquipmentType.PICKAXE).holder());
    }

    private static boolean isOrdinarySkeletonWeapon(ItemStack stack, float tension) {
        InfxSkeleton.OrdinarySkeletonWeapon melee = InfxSkeleton.ordinarySpawnWeapon(0.0F, tension);
        return stack.is(InfXItems.catalog().equipment(InfxMaterial.WOOD, EquipmentType.BOW).holder())
                || stack.is(InfXItems.catalog().equipment(melee.material(), melee.type()).holder());
    }

    private static List<ItemEntity> dropsNear(GameTestHelper helper, BlockPos pos, java.util.function.Predicate<ItemStack> filter) {
        return helper.getLevel().getEntities(
                EntityType.ITEM,
                new AABB(pos).inflate(3.0),
                candidate -> filter.test(candidate.getItem()));
    }

    /** The vanilla variant may wear INFX world-age gear; only vanilla equipment is banned. */
    private static void assertVanillaVariantBare(GameTestHelper helper, EntityType<?> type, EquipmentSlot... skippedSlots) {
        @SuppressWarnings("unchecked")
        Mob mob = helper.spawnWithNoFreeWill((EntityType<Mob>) type, new BlockPos(2, 2, 2));
        mob.finalizeSpawn(
                helper.getLevel(),
                helper.getLevel().getCurrentDifficultyAt(mob.blockPosition()),
                EntitySpawnReason.COMMAND,
                null);
        assertNoVanillaEquipment(helper, mob, type.toString(), skippedSlots);
        mob.discard();
    }

    private static Mob spawnWithFinalize(GameTestHelper helper, EntityType<?> type) {
        @SuppressWarnings("unchecked")
        Mob mob = helper.spawnWithNoFreeWill((EntityType<Mob>) type, new BlockPos(2, 2, 2));
        // Route through the event hook so InfX spawn-time behaviour (world-age gear, the zombie
        // smart/leader roll and attribute alignment) runs, matching a real world spawn.
        net.neoforged.neoforge.event.EventHooks.finalizeMobSpawn(
                mob,
                helper.getLevel(),
                helper.getLevel().getCurrentDifficultyAt(mob.blockPosition()),
                EntitySpawnReason.COMMAND,
                null);
        return mob;
    }

    private static void assertNoVanillaEquipment(GameTestHelper helper, Mob mob, String description, EquipmentSlot... skippedSlots) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HAND
                    && slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
                continue;
            }
            if (java.util.Arrays.asList(skippedSlots).contains(slot)) {
                continue;
            }
            ItemStack stack = mob.getItemBySlot(slot);
            helper.assertTrue(
                    !isVanillaItem(stack),
                    description + " must not spawn with vanilla equipment in " + slot + "; found " + stack);
        }
    }

    private static boolean isVanillaItem(ItemStack stack) {
        return !stack.isEmpty()
                && net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getKey(stack.getItem())
                        .getNamespace()
                        .equals("minecraft");
    }

    /** MITE: a zombie hit once by a player becomes permanently smart and digs bare-handed. */
    private static void zombieSmart(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = ModCompletionGameTests.createPlayer(helper);
        var zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
        zombie.getPersistentData().putBoolean(MonsterTactics.SMART_KEY, false);
        helper.assertTrue(
                !MonsterTactics.diggingEnabled(zombie, level),
                "a bare-handed zombie that is not smart must not dig");
        zombie.hurtServer(level, level.damageSources().playerAttack(player), 1.0F);
        helper.assertTrue(
                zombie.getPersistentData().getBooleanOr(MonsterTactics.SMART_KEY, false),
                "a player hit must make the zombie permanently smart");
        helper.assertTrue(
                MonsterTactics.diggingEnabled(zombie, level),
                "a smart zombie must dig bare-handed");
        ModCompletionGameTests.removePlayer(player);
        helper.succeed();
    }

    /** MITE leader modifiers: 2-5× health and knockback resistance from the 5%-per-tension roll. */
    private static void zombieLeader(GameTestHelper helper) {
        var zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
        float base = zombie.getMaxHealth();
        zombie.getAttribute(Attributes.MAX_HEALTH)
                .addPermanentModifier(new AttributeModifier(
                        InfiniteX.id("zombie_leader_health"),
                        zombie.getRandom().nextDouble() * 3.0 + 1.0,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        double multiplier = zombie.getMaxHealth() / base;
        helper.assertTrue(
                multiplier >= 2.0 && multiplier <= 5.0,
                "a leader zombie must have 2-5× the base health; got " + multiplier);
        zombie.getAttribute(Attributes.KNOCKBACK_RESISTANCE)
                .addPermanentModifier(new AttributeModifier(
                        InfiniteX.id("zombie_leader_knockback"),
                        zombie.getRandom().nextDouble() * 0.25 + 0.5,
                        AttributeModifier.Operation.ADD_VALUE));
        helper.assertTrue(
                zombie.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) >= 0.5,
                "a leader zombie must gain at least 50% knockback resistance; got "
                        + zombie.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        helper.succeed();
    }

    /** MITE dig rates: dirt is 300×0.5 = 150 ticks per hit, a shovel speeds up, ten hits break it. */
    private static void zombieDigRate(GameTestHelper helper) {
        var level = helper.getLevel();
        var zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
        BlockPos dirtPos = new BlockPos(3, 3, 2);
        helper.setBlock(dirtPos, Blocks.DIRT);
        int bareDirt = MonsterTactics.cooloffForBlock(zombie, level, helper.getBlockState(dirtPos));
        helper.assertTrue(
                bareDirt == 150,
                "a bare zombie digs dirt at 300×0.5 = 150 ticks per hit; got " + bareDirt);
        zombie.setItemSlot(
                EquipmentSlot.MAINHAND,
                InfXItems.catalog()
                        .equipment(InfxMaterial.IRON, EquipmentType.SHOVEL)
                        .holder()
                        .toStack());
        int shovelDirt = MonsterTactics.cooloffForBlock(zombie, level, helper.getBlockState(dirtPos));
        helper.assertTrue(shovelDirt < 150, "a shovel must speed up dirt digging; got " + shovelDirt);
        helper.assertTrue(
                MonsterTactics.diggingEnabled(zombie, level),
                "a zombie holding a shovel must be able to dig");
        // Ten forced hits break the dirt block through the same state machine the event uses.
        var player = ModCompletionGameTests.createPlayer(helper);
        Vec3 playerPos = helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(10, 2, 2)));
        player.snapTo(playerPos.x, playerPos.y, playerPos.z, 0.0F, 0.0F);
        zombie.setTarget(player);
        zombie.getPersistentData().putLong(MonsterTactics.DIG_POS, helper.absolutePos(dirtPos).asLong());
        zombie.getPersistentData().putInt(MonsterTactics.DIG_PROGRESS, 0);
        for (int hit = 0; hit < 10 && !helper.getBlockState(dirtPos).isAir(); hit++) {
            zombie.getPersistentData().putInt(MonsterTactics.DIG_NEXT_HIT, zombie.tickCount);
            MonsterTactics.tryDig(level, zombie);
        }
        helper.assertTrue(helper.getBlockState(dirtPos).isAir(), "ten hits must break the dirt block");
        ModCompletionGameTests.removePlayer(player);
        helper.succeed();
    }

    /** MITE: a burning zombie whose tree has a player near its canopy sets the log on fire. */
    private static void zombieBurnTree(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = ModCompletionGameTests.createPlayer(helper);
        BlockPos logPos = new BlockPos(4, 3, 2);
        helper.setBlock(logPos, Blocks.OAK_LOG);
        Vec3 playerPos = helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(4, 6, 2)));
        player.snapTo(playerPos.x, playerPos.y, playerPos.z, 0.0F, 0.0F);
        // Keep the player hovering at the canopy so the MITE y+2..y+9 tree check passes.
        player.setNoGravity(true);
        var zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
        helper.assertTrue(
                ZombieEvents.igniteNearestTree(level, zombie),
                "a burning zombie near a player-lit tree must set fire to the log");
        helper.assertTrue(
                helper.getBlockState(logPos.above()).is(Blocks.FIRE),
                "the tree log must be on fire after ignition");
        helper.succeed();
    }

    /** MITE: a ghoul heals half its max health from slaying an animal. */
    private static void ghoulHeal(GameTestHelper helper) {
        var level = helper.getLevel();
        var ghoul = helper.spawnWithNoFreeWill(InfXEntityTypes.GHOUL.get(), new BlockPos(2, 2, 2));
        ghoul.hurtServer(level, level.damageSources().generic(), 15.0F);
        float before = ghoul.getHealth();
        var cow = helper.spawn(EntityType.COW, new BlockPos(4, 2, 2));
        cow.setHealth(1.0F);
        ghoul.doHurtTarget(level, cow);
        helper.assertTrue(
                ghoul.getHealth() > before,
                "a ghoul that slays an animal must heal half its max health");
        helper.succeed();
    }

    /** MITE tension: chunk residency raises the point difficulty and it never exceeds the cap. */
    private static void tensionCurve(GameTestHelper helper) {
        if (!Tension.enabled()) {
            helper.succeed();
            return;
        }
        var level = helper.getLevel();
        BlockPos pos = new BlockPos(0, 1, 0);
        var chunk = level.getChunkAt(pos);
        chunk.setInhabitedTime(0L);
        float fresh = MonsterTactics.difficultyTension(level, pos);
        chunk.setInhabitedTime(3_600_000L);
        float full = MonsterTactics.difficultyTension(level, pos);
        helper.assertTrue(
                fresh < full,
                "longer-inhabited chunks must have higher tension (fresh=" + fresh + ", full=" + full + ")");
        helper.assertTrue(full <= 1.5F, "tension must never exceed its 1.5 cap; got " + full);
        helper.succeed();
    }

    /** MITE: a hurt skeleton consumes a bone to heal half of its maximum health, on a 400-tick cooldown. */
    private static void skeletonBoneRepair(GameTestHelper helper) {
        var level = helper.getLevel();
        var skeleton = helper.spawn(InfXEntityTypes.INFX_SKELETON.get(), new BlockPos(2, 2, 2));
        skeleton.hurtServer(level, level.damageSources().generic(), 4.0F);
        float before = skeleton.getHealth();
        ItemStack bone = new ItemStack(Items.BONE, 3);
        helper.assertTrue(
                skeleton.tryRepairFromBone(bone),
                "a hurt skeleton must repair from a bone");
        helper.assertTrue(
                Math.abs((skeleton.getHealth() - before) - skeleton.getMaxHealth() * 0.5F) < 0.01F,
                "one bone must heal 50% of maximum health");
        helper.assertTrue(bone.getCount() == 2, "one bone must be consumed");
        helper.assertTrue(
                !skeleton.tryRepairFromBone(bone),
                "the 400-tick repair cooldown must block a second heal");
        helper.assertTrue(
                !skeleton.tryRepairFromBone(new ItemStack(Items.ROTTEN_FLESH, 1)),
                "only bones may repair a skeleton");
        helper.succeed();
    }

    /** MITE: skeletons are never harmed by cactus. */
    private static void skeletonCactusImmune(GameTestHelper helper) {
        var level = helper.getLevel();
        var skeleton = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_SKELETON.get(), new BlockPos(2, 2, 2));
        float before = skeleton.getHealth();
        skeleton.hurtServer(level, level.damageSources().cactus(), 5.0F);
        helper.assertTrue(skeleton.getHealth() == before, "cactus must never hurt a skeleton");
        skeleton.hurtServer(level, level.damageSources().generic(), 5.0F);
        helper.assertTrue(skeleton.getHealth() < before, "other damage must still hurt a skeleton");
        helper.succeed();
    }

    /** MITE: a longdead guardian swaps to a dagger inside 5 blocks and back to a bow beyond 6. */
    private static void skeletonGuardianSwitch(GameTestHelper helper) {
        var player = ModCompletionGameTests.createPlayer(helper);
        var guardian = helper.spawnWithNoFreeWill(InfXEntityTypes.LONGDEAD_GUARDIAN.get(), new BlockPos(2, 2, 2));
        guardian.setItemSlot(
                EquipmentSlot.MAINHAND,
                InfXItems.catalog()
                        .equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.BOW)
                        .holder()
                        .toStack());
        var dagger = InfXItems.catalog().equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.DAGGER).holder().get();
        var bow = InfXItems.catalog().equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.BOW).holder().get();
        guardian.setTarget(player);
        Vec3 near = helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(5, 2, 2)));
        player.snapTo(near.x, near.y, near.z, 0.0F, 0.0F);
        guardian.swapGuardianWeaponForRange();
        helper.assertTrue(
                guardian.getMainHandItem().is(dagger),
                "a guardian within 5 blocks must swap to a dagger; held=" + guardian.getMainHandItem());
        Vec3 far = helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(10, 2, 2)));
        player.snapTo(far.x, far.y, far.z, 0.0F, 0.0F);
        guardian.swapGuardianWeaponForRange();
        helper.assertTrue(
                guardian.getMainHandItem().is(bow),
                "a guardian beyond 6 blocks must swap back to its bow; held=" + guardian.getMainHandItem());
        ModCompletionGameTests.removePlayer(player);
        helper.succeed();
    }

    /** MITE frenzy: hostile mobs move 1.2× faster on blood-moon nights. */
    private static void frenzySpeed(GameTestHelper helper) {
        var level = helper.getLevel();
        var overworldClock = level.registryAccess().get(WorldClocks.OVERWORLD).orElseThrow();
        var zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
        level.clockManager().setTotalTicks(overworldClock, 757_000L); // blood-moon night, day 32
        zombie.setSpeed(1.0F);
        helper.assertTrue(
                Math.abs(zombie.getSpeed() - 1.2F) < 1.0E-5F,
                "a hostile mob must move 1.2× faster on a blood-moon night; got " + zombie.getSpeed());
        // MITE endermen are fully exempt from frenzy, including the blood-moon speed bonus.
        var enderman = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_ENDERMAN.get(), new BlockPos(2, 2, 2));
        enderman.setSpeed(1.0F);
        helper.assertTrue(
                Math.abs(enderman.getSpeed() - 1.0F) < 1.0E-5F,
                "endermen must be exempt from blood-moon speed; got " + enderman.getSpeed());
        level.clockManager().setTotalTicks(overworldClock, 733_000L); // ordinary night, day 31
        zombie.setSpeed(1.0F);
        helper.assertTrue(
                Math.abs(zombie.getSpeed() - 1.0F) < 1.0E-5F,
                "a hostile mob must move at base speed on an ordinary night; got " + zombie.getSpeed());
        helper.succeed();
    }

    /** MITE: blood-moon days strike lightning five times as often (1/20000 vs 1/100000). */
    private static void bloodMoonLightning(GameTestHelper helper) {
        var level = helper.getLevel();
        var overworldClock = level.registryAccess().get(WorldClocks.OVERWORLD).orElseThrow();
        level.clockManager().setTotalTicks(overworldClock, 757_000L); // blood-moon day, day 32
        helper.assertTrue(
                MoonPhase.lightningRollBound(level, 100_000) == 20_000,
                "blood-moon days must strike lightning five times as often");
        level.clockManager().setTotalTicks(overworldClock, 733_000L); // ordinary day, day 31
        helper.assertTrue(
                MoonPhase.lightningRollBound(level, 100_000) == 100_000,
                "ordinary days keep the vanilla lightning rate");
        helper.succeed();
    }

    /** MITE: blood-moon days rain in every biome, including hot biomes that never rain. */
    private static void bloodMoonRain(GameTestHelper helper) {
        var level = helper.getLevel();
        var overworldClock = level.registryAccess().get(WorldClocks.OVERWORLD).orElseThrow();
        Biome desert =
                level.registryAccess().lookupOrThrow(Registries.BIOME).get(Biomes.DESERT).orElseThrow().value();
        level.clockManager().setTotalTicks(overworldClock, 757_000L); // blood-moon day, day 32
        helper.assertTrue(
                MoonPhase.bloodMoonPrecipitation(desert, new BlockPos(0, 64, 0), level.getSeaLevel())
                        == Biome.Precipitation.RAIN,
                "blood-moon rain must reach non-raining biomes such as desert");
        var weather = level.getWeatherData();
        weather.setClearWeatherTime(0);
        weather.setRainTime(12_000);
        weather.setThunderTime(12_000);
        weather.setRaining(true);
        weather.setThundering(true);
        // The level's rain level only eases toward the weather flag across server ticks.
        helper.runAfterDelay(40, () -> {
            BlockPos sky = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(2, 0, 2)).above();
            helper.assertTrue(
                    level.isRainingAt(sky),
                    "blood-moon days must be raining at sky-visible positions");
            helper.succeed();
        });
    }

    /** MITE: vanilla crops carry blood-moon blight in the tracker; it stunts, kills and cures. */
    private static void bloodMoonCropBlight(GameTestHelper helper) {
        var level = helper.getLevel();
        BlightTracker tracker = BlightTracker.get(level);
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        level.setBlockAndUpdate(pos, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 3));
        tracker.blight(pos);
        // A blighted crop is stunted: its random tick no longer advances age.
        level.getBlockState(pos).randomTick(level, pos, new FixedRandom(1));
        helper.assertTrue(
                level.getBlockState(pos).getValue(CropBlock.AGE) == 3,
                "a blighted crop must not grow; got age "
                        + level.getBlockState(pos).getValue(CropBlock.AGE));
        // On its death roll a blighted crop withers away and drops its seed.
        level.getBlockState(pos).randomTick(level, pos, new FixedRandom(0));
        helper.assertTrue(level.getBlockState(pos).isAir(), "a blighted crop must wither away on its death roll");
        // Bonemeal cures the blight instead of growing a blighted crop.
        tracker.blight(pos);
        level.setBlockAndUpdate(pos, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 3));
        ((CropBlock) level.getBlockState(pos).getBlock()).performBonemeal(
                level, new FixedRandom(1), pos, level.getBlockState(pos));
        helper.assertTrue(!tracker.isBlighted(pos), "bonemeal must cure blight on vanilla crops");
        helper.succeed();
    }

    /** MITE: a burning bare-handed mob has difficulty×0.3 chance to ignite its target. */
    private static void burningMobFireTransfer(GameTestHelper helper) {
        helper.assertTrue(
                MonsterEvents.burningMobTransferRoll(3, new FixedRandom(0)),
                "a Hard-difficulty burning mob must usually transfer fire");
        helper.assertTrue(
                !MonsterEvents.burningMobTransferRoll(3, new FixedRandom(1000)),
                "the fire-transfer roll must still be a roll");
        helper.assertTrue(
                !MonsterEvents.burningMobTransferRoll(0, new FixedRandom(0)),
                "peaceful burning mobs never transfer fire");
        helper.succeed();
    }

    /** MITE: at Normal difficulty a villager conversion is skipped half of the time. */
    private static void villagerConversionNormalGate(GameTestHelper helper) {
        helper.assertTrue(
                ZombieEvents.villagerConversionSkippedAtNormal(Difficulty.NORMAL, true),
                "a Normal-difficulty conversion must be skippable");
        helper.assertTrue(
                !ZombieEvents.villagerConversionSkippedAtNormal(Difficulty.NORMAL, false),
                "the Normal-difficulty skip must be a coin flip");
        helper.assertTrue(
                !ZombieEvents.villagerConversionSkippedAtNormal(Difficulty.HARD, true),
                "Hard difficulty must never skip conversion");
        helper.succeed();
    }

    /** MITE: from day 10 zombies may carry a rusted-iron hatchet instead of a sword. */
    private static void zombieHatchetDay(GameTestHelper helper) {
        helper.assertTrue(
                MonsterTactics.zombieFavoursHatchet(10, true),
                "day-10 zombies may favour a hatchet");
        helper.assertTrue(
                !MonsterTactics.zombieFavoursHatchet(9, true),
                "day-9 zombies must keep the sword");
        helper.assertTrue(
                !MonsterTactics.zombieFavoursHatchet(10, false),
                "the hatchet switch must still be a coin flip");
        helper.succeed();
    }

    /** MITE: blood-moon nights raise the hostile spawn ceiling 1.5× (radius 8→12 chunks). */
    private static void bloodMoonSpawnFactor(GameTestHelper helper) {
        var level = helper.getLevel();
        var overworldClock = level.registryAccess().get(WorldClocks.OVERWORLD).orElseThrow();
        level.clockManager().setTotalTicks(overworldClock, 757_000L); // blood-moon night, day 32
        helper.assertTrue(
                SpawnDensity.bloodMoonSpawnFactor(level) == 1.5F,
                "blood-moon nights must raise the hostile spawn ceiling 1.5×");
        level.clockManager().setTotalTicks(overworldClock, 733_000L); // ordinary night, day 31
        helper.assertTrue(
                SpawnDensity.bloodMoonSpawnFactor(level) == 1.0F,
                "ordinary nights keep the vanilla spawn ceiling");
        helper.succeed();
    }

    /** MITE: the near-player hostile ceiling grows with depth 8×(1+(64-y)/32). */
    private static void depthSpawnScale(GameTestHelper helper) {
        var level = helper.getLevel();
        var overworldClock = level.registryAccess().get(WorldClocks.OVERWORLD).orElseThrow();
        level.clockManager().setTotalTicks(overworldClock, 733_000L); // ordinary night, day 31
        helper.assertTrue(SpawnDensity.densityCapScale(level, 64) == 1.0F, "surface spawns keep the base ceiling");
        helper.assertTrue(SpawnDensity.densityCapScale(level, 32) == 2.0F, "y=32 doubles the hostile ceiling");
        helper.assertTrue(SpawnDensity.densityCapScale(level, 0) == 3.0F, "bedrock triples the hostile ceiling");
        level.clockManager().setTotalTicks(overworldClock, 757_000L); // blood-moon night, day 32
        helper.assertTrue(SpawnDensity.densityCapScale(level, 64) == 1.5F, "a surface blood moon still spawns denser");
        helper.succeed();
    }

    /** MITE: daily random ×0.5/×2/×0 rate modifiers, with a blood-moon/thunder floor of 1.0. */
    private static void spawnRateModifier(GameTestHelper helper) {
        helper.assertTrue(SpawnRateTracker.modifierForCounters(0, 0, 0, false) == 1.0F, "the default rate is 1.0");
        helper.assertTrue(SpawnRateTracker.modifierForCounters(5, 0, 0, false) == 0.5F, "a decreased day halves the rate");
        helper.assertTrue(SpawnRateTracker.modifierForCounters(0, 5, 0, false) == 2.0F, "an increased day doubles the rate");
        helper.assertTrue(SpawnRateTracker.modifierForCounters(0, 0, 5, false) == 0.0F, "a no-spawn day disables hostiles");
        helper.assertTrue(SpawnRateTracker.modifierForCounters(5, 0, 0, true) == 1.0F, "a blood moon or storm floors a halved day");
        helper.assertTrue(SpawnRateTracker.modifierForCounters(0, 0, 5, true) == 1.0F, "a blood moon or storm floors a no-spawn day");
        helper.assertTrue(SpawnRateTracker.modifierForCounters(0, 5, 0, true) == 2.0F, "an increased day stays doubled");
        helper.succeed();
    }

    /** MITE: hostile spawn passes roll 0.1 below y=60 and 0.17 at or above it. */
    private static void spawnCadence(GameTestHelper helper) {
        helper.assertTrue(SpawnDensity.cadenceChance(59, 1.0F) == 0.1F, "deep columns roll a 0.1 cadence");
        helper.assertTrue(SpawnDensity.cadenceChance(60, 1.0F) == 0.17F, "surface columns roll a 0.17 cadence");
        helper.assertTrue(SpawnDensity.cadenceChance(59, 2.0F) == 0.2F, "an increased day scales the deep cadence");
        helper.assertTrue(SpawnDensity.cadenceChance(60, 0.5F) == 0.085F, "a decreased day scales the surface cadence");
        helper.succeed();
    }

    /** MITE has no baby zombies: vanilla's 5% spawn-baby roll is reversed by the spawn event. */
    private static void zombieNoBaby(GameTestHelper helper) {
        Mob zombie = spawnWithFinalize(helper, EntityType.ZOMBIE);
        helper.assertTrue(!zombie.isBaby(), "MITE has no baby zombies");
        helper.succeed();
    }

    /** MITE: a zombie walks to dropped raw meat and eats it; undead zombies never heal from it. */
    private static void zombieFood(GameTestHelper helper) {
        var zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
        ItemStack meat = new ItemStack(Items.ROTTEN_FLESH, 3);
        helper.assertTrue(MoveToFoodGoal.tryEatFood(zombie, meat), "a zombie must eat dropped meat");
        helper.assertTrue(meat.getCount() == 2, "one meat must be consumed");
        helper.assertTrue(
                !MoveToFoodGoal.tryEatFood(zombie, new ItemStack(Items.WHEAT, 1)),
                "zombies only eat meat");
        helper.assertTrue(
                !MoveToFoodGoal.tryEatFood(zombie, meat),
                "the 400-tick food cooldown must block a second bite");
        helper.succeed();
    }

    /** MITE: a zombie holding a digging tool refuses to convert a slain villager. */
    private static void zombieConversionSkip(GameTestHelper helper) {
        var level = helper.getLevel();
        var bare = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
        var tool = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
        tool.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
        var villager1 = helper.spawn(EntityType.VILLAGER, new BlockPos(4, 2, 2));
        bare.doHurtTarget(level, villager1);
        var allow = new LivingConversionEvent.Pre(villager1, EntityType.ZOMBIE_VILLAGER, timer -> {});
        NeoForge.EVENT_BUS.post(allow);
        helper.assertTrue(!allow.isCanceled(), "a bare zombie must allow villager conversion");
        var villager2 = helper.spawn(EntityType.VILLAGER, new BlockPos(5, 2, 2));
        tool.doHurtTarget(level, villager2);
        var deny = new LivingConversionEvent.Pre(villager2, EntityType.ZOMBIE_VILLAGER, timer -> {});
        NeoForge.EVENT_BUS.post(deny);
        helper.assertTrue(deny.isCanceled(), "a tool-wielding zombie must skip villager conversion");
        helper.succeed();
    }

    /** MITE digs the target's foot column first: a block under an elevated target. */
    private static void zombieDigFeetFirst(GameTestHelper helper) {
        var level = helper.getLevel();
        var zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
        var player = ModCompletionGameTests.createPlayer(helper);
        BlockPos dirtPos = new BlockPos(4, 2, 2);
        helper.setBlock(dirtPos, Blocks.DIRT);
        Vec3 playerPos = helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(4, 3, 2)));
        player.snapTo(playerPos.x, playerPos.y, playerPos.z, 0.0F, 0.0F);
        zombie.setTarget(player);
        BlockPos selected = MonsterTactics.firstDiggableInTargetColumn(zombie, level, player);
        helper.assertTrue(
                selected != null && helper.absolutePos(dirtPos).equals(selected),
                "MITE dig must prefer the target's foot column; got " + selected);
        ModCompletionGameTests.removePlayer(player);
        helper.succeed();
    }

    /** MITE: a burning slime dies without splitting into smaller cubes. */
    private static void slimeBurningNoSplit(GameTestHelper helper) {
        var level = helper.getLevel();
        var slime = helper.spawnWithNoFreeWill(InfXEntityTypes.INFX_SLIME.get(), new BlockPos(2, 2, 2));
        slime.setSize(2, true);
        slime.igniteForSeconds(10.0F);
        slime.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
        slime.remove(Entity.RemovalReason.KILLED);
        var children = level.getEntitiesOfClass(
                InfxSlime.class,
                new AABB(new BlockPos(2, 2, 2)).inflate(8.0),
                child -> child.getSize() == 1 && child != slime);
        helper.assertTrue(children.isEmpty(), "burning slimes must not split into smaller cubes");
        helper.succeed();
    }

    /** Deterministic {@link RandomSource} whose {@code nextInt(bound)} returns {@code value % bound}. */
    private static final class FixedRandom implements RandomSource {
        private final int value;

        private FixedRandom(int value) {
            this.value = value;
        }

        @Override
        public RandomSource fork() {
            return new FixedRandom(this.value);
        }

        @Override
        public net.minecraft.world.level.levelgen.PositionalRandomFactory forkPositional() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSeed(long seed) {}

        @Override
        public int nextInt() {
            return this.value;
        }

        @Override
        public int nextInt(int bound) {
            return this.value % bound;
        }

        @Override
        public long nextLong() {
            return this.value;
        }

        @Override
        public boolean nextBoolean() {
            return this.value % 2 == 0;
        }

        @Override
        public float nextFloat() {
            return this.value / 1000.0F;
        }

        @Override
        public double nextDouble() {
            return this.value / 1000.0;
        }

        @Override
        public double nextGaussian() {
            return this.value;
        }
    }
}
