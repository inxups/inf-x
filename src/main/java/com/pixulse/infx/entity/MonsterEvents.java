package com.pixulse.infx.entity;

import com.pixulse.infx.registry.ModEntityTypes;
import com.pixulse.infx.world.MoonPhase;
import com.pixulse.infx.world.RiverBiomes;
import com.pixulse.infx.world.Underworld;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Registration, spawn replacement and cross-family AI hooks for R196 mobs. */
public final class MonsterEvents {
    private static final int LIGHT_SEARCH_INTERVAL = 80;
    private static boolean sharingTarget;

    private MonsterEvents() {}

    public static void register(IEventBus modBus, IEventBus gameBus) {
        modBus.addListener(MonsterEvents::createAttributes);
        modBus.addListener(MonsterEvents::registerSpawnPlacements);
        gameBus.addListener(MonsterEvents::finalizeSpawn);
        gameBus.addListener(MonsterEvents::leadRangedProjectile);
        gameBus.addListener(MonsterEvents::replaceVanillaSpawn);
        gameBus.addListener(MonsterEvents::shareTarget);
        gameBus.addListener(MonsterEvents::amplifyInfernalCreeperExplosion);
        gameBus.addListener(MonsterEvents::limitCreeperTerrainDamage);
        gameBus.addListener(MonsterEvents::protectNetherspawnTerrain);
        gameBus.addListener(MonsterEvents::attractToPlayerActivity);
        gameBus.addListener(MonsterEvents::coordinateAndSeekLight);
        gameBus.addListener(MonsterEvents::reduceSkeletonArrowGravity);
        gameBus.addListener(MonsterEvents::applyWitchMagicDefense);
        gameBus.addListener(MonsterEvents::limitSpawnerPopulation);
        gameBus.addListener(MonsterEvents::preventObservedDespawn);
        gameBus.addListener(MonsterEvents::applyFrenzyDamage);
        gameBus.addListener(MonsterEvents::applyMiteProjectileDamage);
        gameBus.addListener(MonsterEvents::retaliateAgainstBareHands);
        gameBus.addListener(MonsterEvents::armCreeperFromCactus);
    }

    /**
     * MITE frenzy: during blood-moon nights (and under bone-lord inspiration) monster melee
     * gains half its base attack again. Endermen are explicitly exempt in MITE.
     */
    private static void applyFrenzyDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Mob attacker)
                || !(attacker instanceof Enemy)
                || attacker instanceof MiteEnderman
                || !event.getSource().isDirect()
                || !(attacker.level() instanceof ServerLevel level)) {
            return;
        }
        boolean frenzied = level.dimension() == Level.OVERWORLD
                && MoonPhase.at(level) == MoonPhase.BLOOD
                && !isDay(level);
        if (!frenzied && attacker instanceof MiteSkeleton skeleton && skeleton.isInspired()) {
            frenzied = true;
        }
        if (!frenzied) {
            return;
        }
        var attack = attacker.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            event.setAmount(event.getAmount() + 0.5F * (float) attack.getBaseValue());
        }
    }

    /**
     * MITE projectile numbers: blaze small fireballs hit for a flat 2; skeleton arrows carry a
     * per-variant floor (rusted 5, longdead's ancient 9) and stay at the floor unless the bow
     * is enchanted.
     */
    private static void applyMiteProjectileDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getDirectEntity()
                        instanceof net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball fireball
                && fireball.getOwner() instanceof MiteBlaze) {
            event.setAmount(2.0F);
            return;
        }
        if (event.getSource().getDirectEntity()
                        instanceof net.minecraft.world.entity.projectile.arrow.AbstractArrow arrow
                && arrow.getOwner() instanceof MiteSkeleton skeleton) {
            float floor = skeleton.variant() == MiteSkeleton.Variant.LONGDEAD ? 9.0F : 5.0F;
            var bow = skeleton.getMainHandItem();
            boolean enchantedBow = !bow.isEmpty() && bow.isEnchanted();
            event.setAmount(enchantedBow ? Math.max(event.getAmount(), floor) : floor);
        }
    }

    /**
     * MITE melee retaliation: punching a blaze or fire elemental without a tool always burns the
     * hand for one point; any other monster currently fighting back has a 1-in-8 chance.
     */
    private static void retaliateAgainstBareHands(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof Mob victim)
                || !(victim instanceof Enemy)
                || !(victim.level() instanceof ServerLevel level)) {
            return;
        }
        var source = event.getSource();
        if (!source.isDirect() || !(source.getEntity() instanceof Player attacker)) {
            return;
        }
        var weapon = attacker.getMainHandItem();
        boolean toolLike = weapon.has(net.minecraft.core.component.DataComponents.TOOL)
                || weapon.is(net.minecraft.world.item.Items.STICK)
                || weapon.is(net.minecraft.world.item.Items.BONE);
        if (toolLike) {
            return;
        }
        boolean alwaysRetaliates = victim instanceof MiteBlaze || victim instanceof FireElemental;
        if (alwaysRetaliates || (victim.getTarget() == attacker && victim.getRandom().nextFloat() < 0.125F)) {
            attacker.hurtServer(level, level.damageSources().mobAttack(victim), 1.0F);
        }
    }

    /** MITE's conspicuous-cactus trigger, mapped to a real cactus hit in the modern damage pipeline. */
    private static void armCreeperFromCactus(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof MiteCreeper creeper)
                || creeper.level().isClientSide()
                || event.getHealthDamage() <= 0.0F
                || !event.getSource().is(DamageTypes.CACTUS)
                || !creeper.getRandom().nextBoolean()) {
            return;
        }
        creeper.armCactusFuse();
    }

    private static void createAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.R196_ZOMBIE.get(), MiteZombie.attributes(MiteZombie.Variant.ZOMBIE).build());
        event.put(ModEntityTypes.INVISIBLE_STALKER.get(), MiteZombie.attributes(MiteZombie.Variant.INVISIBLE_STALKER).build());
        event.put(ModEntityTypes.GHOUL.get(), MiteZombie.attributes(MiteZombie.Variant.GHOUL).build());
        event.put(ModEntityTypes.SHADOW.get(), MiteZombie.attributes(MiteZombie.Variant.SHADOW).build());
        event.put(ModEntityTypes.WIGHT.get(), MiteZombie.attributes(MiteZombie.Variant.WIGHT).build());
        event.put(ModEntityTypes.REVENANT.get(), MiteZombie.attributes(MiteZombie.Variant.REVENANT).build());

        event.put(ModEntityTypes.R196_SKELETON.get(), MiteSkeleton.attributes(MiteSkeleton.Variant.SKELETON).build());
        event.put(ModEntityTypes.LONGDEAD.get(), MiteSkeleton.attributes(MiteSkeleton.Variant.LONGDEAD).build());
        event.put(ModEntityTypes.BONE_LORD.get(), MiteSkeleton.attributes(MiteSkeleton.Variant.BONE_LORD).build());
        event.put(ModEntityTypes.ANCIENT_BONE_LORD.get(), MiteSkeleton.attributes(MiteSkeleton.Variant.ANCIENT_BONE_LORD).build());

        event.put(ModEntityTypes.R196_SPIDER.get(), MiteSpider.attributes(MiteSpider.Variant.SPIDER).build());
        event.put(ModEntityTypes.R196_CAVE_SPIDER.get(), MiteSpider.attributes(MiteSpider.Variant.CAVE_SPIDER).build());
        event.put(ModEntityTypes.BLACK_WIDOW_SPIDER.get(), MiteSpider.attributes(MiteSpider.Variant.BLACK_WIDOW).build());
        event.put(ModEntityTypes.DEMON_SPIDER.get(), MiteSpider.attributes(MiteSpider.Variant.DEMON).build());
        event.put(ModEntityTypes.WOOD_SPIDER.get(), MiteSpider.attributes(MiteSpider.Variant.WOOD).build());
        event.put(ModEntityTypes.PHASE_SPIDER.get(), MiteSpider.attributes(MiteSpider.Variant.PHASE).build());

        event.put(ModEntityTypes.R196_CREEPER.get(), MiteCreeper.attributes(MiteCreeper.Variant.CREEPER).build());
        event.put(ModEntityTypes.INFERNAL_CREEPER.get(), MiteCreeper.attributes(MiteCreeper.Variant.INFERNAL).build());

        event.put(ModEntityTypes.R196_SLIME.get(), MiteSlime.attributes(MiteSlime.Variant.SLIME).build());
        event.put(ModEntityTypes.JELLY.get(), MiteSlime.attributes(MiteSlime.Variant.JELLY).build());
        event.put(ModEntityTypes.BLOB.get(), MiteSlime.attributes(MiteSlime.Variant.BLOB).build());
        event.put(ModEntityTypes.OOZE.get(), MiteSlime.attributes(MiteSlime.Variant.OOZE).build());
        event.put(ModEntityTypes.PUDDING.get(), MiteSlime.attributes(MiteSlime.Variant.PUDDING).build());
        event.put(ModEntityTypes.MAGMA_CUBE.get(), MiteMagmaCube.attributes().build());

        for (var type : List.of(ModEntityTypes.NETHERSPAWN, ModEntityTypes.COPPERSPINE, ModEntityTypes.HOARY_SILVERFISH)) {
            event.put(type.get(), MiteSilverfish.attributes().build());
        }

        event.put(ModEntityTypes.VAMPIRE_BAT.get(), MiteBat.attributes(MiteBat.Variant.VAMPIRE).build());
        event.put(ModEntityTypes.NIGHTWING.get(), MiteBat.attributes(MiteBat.Variant.NIGHTWING).build());
        event.put(ModEntityTypes.GIANT_VAMPIRE_BAT.get(), MiteBat.attributes(MiteBat.Variant.GIANT_VAMPIRE).build());
        event.put(ModEntityTypes.HELLHOUND.get(), MiteWolf.attributes(MiteWolf.Variant.HELLHOUND).build());
        event.put(ModEntityTypes.DIRE_WOLF.get(), MiteWolf.attributes(MiteWolf.Variant.DIRE_WOLF).build());
        event.put(ModEntityTypes.FIRE_ELEMENTAL.get(), FireElemental.attributes().build());
        event.put(ModEntityTypes.EARTH_ELEMENTAL.get(), EarthElemental.attributes().build());
        event.put(ModEntityTypes.CLAY_GOLEM.get(), ClayGolem.attributes().build());

        event.put(ModEntityTypes.R196_ENDERMAN.get(), MiteEnderman.attributes().build());
        event.put(ModEntityTypes.R196_SQUID.get(), MiteSquid.attributes().build());
        event.put(ModEntityTypes.R196_COD.get(), MiteCod.attributes().build());
        event.put(ModEntityTypes.R196_SALMON.get(), MiteSalmon.attributes().build());
        event.put(ModEntityTypes.R196_PUFFERFISH.get(), MitePufferfish.attributes().build());
        event.put(ModEntityTypes.R196_TROPICAL_FISH.get(), MiteTropicalFish.attributes().build());
        event.put(ModEntityTypes.R196_WITCH.get(), MiteWitch.attributes().build());
        event.put(ModEntityTypes.R196_ZOMBIFIED_PIGLIN.get(), MiteZombifiedPiglin.attributes().build());
        event.put(ModEntityTypes.R196_BLAZE.get(), MiteBlaze.attributes().build());
        event.put(ModEntityTypes.R196_GHAST.get(), MiteGhast.attributes().build());

        event.put(ModEntityTypes.R196_COW.get(), MiteCow.attributes().build());
        event.put(ModEntityTypes.R196_CHICKEN.get(), MiteChicken.attributes().build());
        event.put(ModEntityTypes.R196_SHEEP.get(), MiteSheep.attributes().build());
        event.put(ModEntityTypes.R196_PIG.get(), MitePig.attributes().build());
        event.put(ModEntityTypes.R196_HORSE.get(), MiteHorse.attributes().build());
        event.put(ModEntityTypes.R196_OCELOT.get(), MiteOcelot.attributes().build());
        event.put(ModEntityTypes.R196_WOLF.get(), VanillaWolf.attributes().build());
    }

    private static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
                EntityType.CREEPER,
                null,
                null,
                (type, level, reason, pos, random) -> {
                    ServerLevel serverLevel = level.getLevel();
                    return serverLevel.dimension() != Level.OVERWORLD
                            || isDay(serverLevel)
                            || random.nextInt(4) == 0
                            || !serverLevel.canSeeSky(pos);
                },
                RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(
                EntityType.SPIDER,
                null,
                null,
                (type, level, reason, pos, random) -> level.getLevel().dimension() != Level.OVERWORLD
                        || random.nextInt(4) != 0
                        || !level.getLevel().canSeeSky(pos),
                RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(
                EntityType.SLIME,
                null,
                null,
                (type, level, reason, pos, random) -> !stoneAbove(level.getLevel(), pos),
                RegisterSpawnPlacementsEvent.Operation.AND);
        for (var type : List.of(
                ModEntityTypes.INVISIBLE_STALKER,
                ModEntityTypes.GHOUL,
                ModEntityTypes.SHADOW,
                ModEntityTypes.WIGHT,
                ModEntityTypes.REVENANT)) {
            event.register(type.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    MonsterEvents::checkR196MonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
        for (var type : List.of(ModEntityTypes.LONGDEAD, ModEntityTypes.BONE_LORD, ModEntityTypes.ANCIENT_BONE_LORD)) {
            event.register(type.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    MonsterEvents::checkR196MonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
        for (var type : List.of(
                ModEntityTypes.BLACK_WIDOW_SPIDER,
                ModEntityTypes.DEMON_SPIDER,
                ModEntityTypes.WOOD_SPIDER,
                ModEntityTypes.PHASE_SPIDER)) {
            event.register(type.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    MonsterEvents::checkR196MonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
        event.register(ModEntityTypes.INFERNAL_CREEPER.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> pos.getY() < 40
                        && Monster.checkMonsterSpawnRules(type, level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        for (var type : List.of(ModEntityTypes.JELLY, ModEntityTypes.BLOB, ModEntityTypes.OOZE, ModEntityTypes.PUDDING)) {
            event.register(type.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    MonsterEvents::checkR196MonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
        event.register(ModEntityTypes.MAGMA_CUBE.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> MagmaCube.checkMagmaCubeSpawnRules(
                        asEntityType(type), level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        for (var type : List.of(ModEntityTypes.NETHERSPAWN, ModEntityTypes.COPPERSPINE, ModEntityTypes.HOARY_SILVERFISH)) {
            event.register(type.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
        for (var type : List.of(ModEntityTypes.VAMPIRE_BAT, ModEntityTypes.NIGHTWING, ModEntityTypes.GIANT_VAMPIRE_BAT)) {
            event.register(type.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (entityType, level, reason, pos, random) ->
                            net.minecraft.world.entity.ambient.Bat.checkBatSpawnRules(
                                    asEntityType(entityType), level, reason, pos, random)
                                    && checkR196BatDepth(entityType, level, pos),
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
        event.register(ModEntityTypes.HELLHOUND.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MonsterEvents::checkR196MonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntityTypes.DIRE_WOLF.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> Wolf.checkWolfSpawnRules(
                        asEntityType(type), level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntityTypes.FIRE_ELEMENTAL.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> Mob.checkMobSpawnRules(type, level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntityTypes.EARTH_ELEMENTAL.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MonsterEvents::checkR196MonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntityTypes.CLAY_GOLEM.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MonsterEvents::checkR196MonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);

        registerAnimalSpawnPlacement(event, ModEntityTypes.R196_COW.get());
        registerAnimalSpawnPlacement(event, ModEntityTypes.R196_CHICKEN.get());
        registerAnimalSpawnPlacement(event, ModEntityTypes.R196_SHEEP.get());
        registerAnimalSpawnPlacement(event, ModEntityTypes.R196_PIG.get());
        registerAnimalSpawnPlacement(event, ModEntityTypes.R196_HORSE.get());
        event.register(ModEntityTypes.R196_OCELOT.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING,
                (type, level, reason, pos, random) -> Ocelot.checkOcelotSpawnRules(
                        asEntityType(type), level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntityTypes.R196_WOLF.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> Wolf.checkWolfSpawnRules(
                        asEntityType(type), level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        registerWaterAnimalSpawnPlacement(event, ModEntityTypes.R196_COD.get());
        registerWaterAnimalSpawnPlacement(event, ModEntityTypes.R196_SALMON.get());
        registerWaterAnimalSpawnPlacement(event, ModEntityTypes.R196_PUFFERFISH.get());
        event.register(
                ModEntityTypes.R196_TROPICAL_FISH.get(),
                SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> TropicalFish.checkTropicalFishSpawnRules(
                        asEntityType(type), level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    /** Match vanilla passive-animal ground and light spawn restrictions. */
    private static void registerAnimalSpawnPlacement(
            RegisterSpawnPlacementsEvent event, EntityType<? extends Animal> type) {
        event.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private static void registerWaterAnimalSpawnPlacement(
            RegisterSpawnPlacementsEvent event, EntityType<? extends WaterAnimal> type) {
        event.register(type, SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WaterAnimal::checkSurfaceWaterAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    static boolean checkR196MonsterSpawnRules(
            EntityType<? extends Mob> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random) {
        ServerLevel serverLevel = level.getLevel();
        String path = BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath();
        boolean bypassNetherLight = path.equals("earth_elemental") && serverLevel.dimension() == Level.NETHER;
        if (!(bypassNetherLight
                ? Mob.checkMobSpawnRules(type, level, reason, pos, random)
                : Monster.checkMonsterSpawnRules(type, level, reason, pos, random))) {
            return false;
        }
        if (path.equals("earth_elemental") && !earthElementalGround(serverLevel, pos)) return false;
        if (path.equals("clay_golem") && !clayGolemGround(serverLevel, pos)) return false;
        if (serverLevel.dimension() != Level.OVERWORLD) return true;

        int y = pos.getY();
        boolean bloodMoonUp = MoonPhase.at(serverLevel) == MoonPhase.BLOOD
                && !isDay(serverLevel);
        boolean freezing = serverLevel.getBiome(pos).value().getBaseTemperature() <= 0.15F;
        boolean desert = serverLevel.getBiome(pos).is(net.minecraft.world.level.biome.Biomes.DESERT)
                || serverLevel.getBiome(pos).is(RiverBiomes.DESERT_RIVER);
        return switch (path) {
            case "ghoul" -> y <= 56 || bloodMoonUp;
            case "wight" -> y <= 48 || bloodMoonUp && freezing;
            case "revenant" -> y <= 44 || bloodMoonUp;
            case "invisible_stalker", "earth_elemental" -> y <= 40;
            case "blob" -> y <= 40 && stoneAbove(serverLevel, pos);
            case "ooze" -> y <= 32 && serverLevel.getBlockState(pos.below()).is(Blocks.STONE)
                    && stoneAbove(serverLevel, pos);
            case "nightwing", "bone_lord" -> y <= 32 || bloodMoonUp;
            case "pudding" -> y <= 24 && serverLevel.getBlockState(pos.below()).is(Blocks.STONE)
                    && stoneAbove(serverLevel, pos);
            case "hellhound" -> y <= 32;
            case "shadow" -> y <= 32 || bloodMoonUp && desert;
            case "wood_spider" -> woodSpiderHabitat(serverLevel, pos);
            case "demon_spider", "phase_spider" -> y <= 32;
            case "black_widow_spider" -> random.nextBoolean();
            case "jelly" -> stoneAbove(serverLevel, pos);
            default -> true;
        };
    }

    private static boolean checkR196BatDepth(
            EntityType<? extends Mob> type, ServerLevelAccessor level, BlockPos pos) {
        ServerLevel serverLevel = level.getLevel();
        int maximumY = type == ModEntityTypes.NIGHTWING.get() ? 32 : 48;
        return serverLevel.dimension() != Level.OVERWORLD
                || pos.getY() <= maximumY
                || MoonPhase.at(serverLevel) == MoonPhase.BLOOD && !isDay(serverLevel);
    }

    private static boolean earthElementalGround(ServerLevel level, BlockPos pos) {
        return EarthElemental.isValidGround(level.getBlockState(pos.below()));
    }

    private static boolean clayGolemGround(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(Blocks.CLAY);
    }

    private static boolean stoneAbove(ServerLevel level, BlockPos pos) {
        int maximumY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
        for (int y = pos.getY() + 1; y <= maximumY; y++) {
            var state = level.getBlockState(new BlockPos(pos.getX(), y, pos.getZ()));
            if (!state.isAir()) return state.is(Blocks.STONE);
        }
        return false;
    }

    private static boolean woodSpiderHabitat(ServerLevel level, BlockPos pos) {
        if (!level.canSeeSky(pos)
                && !firstBlockAboveIs(level, pos, BlockTags.LOGS)
                && !firstBlockAboveIs(level, pos, BlockTags.LEAVES)) {
            return false;
        }
        return blockTagNear(level, pos, BlockTags.LOGS, 5, 2)
                && blockTagNear(level, pos.above(5), BlockTags.LEAVES, 5, 5);
    }

    private static boolean firstBlockAboveIs(ServerLevel level, BlockPos pos, TagKey<Block> tag) {
        int maximumY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
        for (int y = pos.getY() + 1; y <= maximumY; y++) {
            var state = level.getBlockState(new BlockPos(pos.getX(), y, pos.getZ()));
            if (!state.isAir()) return state.is(tag);
        }
        return false;
    }

    private static boolean blockTagNear(
            ServerLevel level,
            BlockPos origin,
            TagKey<Block> tag,
            int horizontalRadius,
            int verticalRadius) {
        for (BlockPos nearby : BlockPos.betweenClosed(
                origin.offset(-horizontalRadius, -verticalRadius, -horizontalRadius),
                origin.offset(horizontalRadius, verticalRadius, horizontalRadius))) {
            if (level.getBlockState(nearby).is(tag)) return true;
        }
        return false;
    }

    private static boolean isDay(ServerLevel level) {
        return Math.floorMod(level.getOverworldClockTime(), 24_000L) < 12_000L;
    }

    /**
     * The vanilla spawn predicates are declared against the vanilla entity type, while this mod
     * registers subclasses with their own entity types. Keep passing the actual mod type at
     * runtime, but adapt the generic signature for NeoForge versions whose vanilla declarations
     * still use an exact parent type.
     */
    @SuppressWarnings("unchecked")
    private static <T extends Entity> EntityType<T> asEntityType(EntityType<?> type) {
        return (EntityType<T>) type;
    }

    private static void finalizeSpawn(FinalizeSpawnEvent event) {
        if (event.getEntity() instanceof EarthElemental elemental
                && event.getSpawnType() != EntitySpawnReason.LOAD) {
            elemental.initializeMiteForm();
        }
        if (event.getEntity() instanceof Monster monster
                && monster.level() instanceof ServerLevel level
                && isWorldSpawn(event.getSpawnType())) {
            MonsterTactics.equipForWorldAge(level, monster);
        }
        if (event.getEntity().getType() == EntityType.WITCH
                && event.getSpawnType() != EntitySpawnReason.STRUCTURE
                && event.getSpawnType() != EntitySpawnReason.COMMAND
                && event.getSpawnType() != EntitySpawnReason.SPAWN_ITEM_USE
                && event.getSpawnType() != EntitySpawnReason.DISPENSER
                && event.getSpawnType() != EntitySpawnReason.LOAD) {
            event.setSpawnCancelled(true);
        }
    }

    private static void attractToPlayerActivity(VanillaGameEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getCause() instanceof Player player)) return;
        double radius = MoonPhase.at(level) == MoonPhase.BLOOD ? 96.0 : 48.0;
        for (Mob mob : level.getEntitiesOfClass(
                Mob.class,
                new AABB(event.getEventPosition(), event.getEventPosition()).inflate(radius),
                candidate -> participatesInGenericTargeting(candidate)
                        && candidate.isAlive()
                        // MITE base spiders are peaceful in daylight; noise must not override that.
                        && !(candidate instanceof MiteSpider spider
                                && spider.variant() == MiteSpider.Variant.SPIDER
                                && spider.getLightLevelDependentMagicValue() >= 0.5F))) {
            mob.setTarget(player);
            if (!mob.hasLineOfSight(player)) {
                mob.getNavigation().moveTo(event.getEventPosition().x, event.getEventPosition().y, event.getEventPosition().z, 1.05);
            }
        }
    }

    private static void coordinateAndSeekLight(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob)
                || !(mob instanceof Enemy)
                || !(mob.level() instanceof ServerLevel level)) {
            return;
        }
        // MITE idle regeneration: non-undead monsters recover 10% of max health every 1000
        // ticks; fire elementals are the explicit exception.
        if (mob.tickCount % 1000 == 999
                && !(mob instanceof FireElemental)
                && !BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(mob.getType()).is(net.minecraft.tags.EntityTypeTags.UNDEAD)
                && mob.getHealth() < mob.getMaxHealth()) {
            mob.heal(mob.getMaxHealth() * 0.1F);
        }
        // Endermen remain neutral until their own stare, pearl, or damage rules choose a target.
        // They also must not inherit the generic flanking and block-digging behavior below.
        if (!participatesInGenericTargeting(mob)) {
            return;
        }
        if (mob.getTarget() != null) {
            if (mob.tickCount % 20 == 0) MonsterTactics.cooperate(level, mob);
            return;
        }
        if (mob.tickCount % 20 == 0) {
            double range = MoonPhase.at(level) == MoonPhase.BLOOD ? 96.0 : 48.0;
            Player illuminated = level.getNearestPlayer(
                    mob.getX(), mob.getY(), mob.getZ(), range,
                    player -> !player.isSpectator()
                            && level.getBrightness(LightLayer.BLOCK, player.blockPosition()) >= 7);
            if (illuminated != null) {
                mob.setTarget(illuminated);
                return;
            }
        }
        if (shouldSearchForLight(mob.tickCount, mob.getId())) {
            BlockPos origin = mob.blockPosition();
            BlockPos brightest = null;
            int brightness = 6;
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-10, -4, -10), origin.offset(10, 4, 10))) {
                int candidate = level.getBrightness(LightLayer.BLOCK, pos);
                if (candidate > brightness) {
                    brightness = candidate;
                    brightest = pos.immutable();
                }
            }
            if (brightest != null) mob.getNavigation().moveTo(brightest.getX() + .5, brightest.getY(), brightest.getZ() + .5, 1.0);
        }
    }

    /**
     * Vanilla applies arrow gravity after movement. Restoring this amount after the tick gives
     * R196 skeleton arrows their tuned effective air gravity without altering any other arrow.
     */
    private static void reduceSkeletonArrowGravity(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof AbstractArrow arrow)
                || !(arrow.getOwner() instanceof MiteSkeleton)
                || !(arrow.level() instanceof ServerLevel)
                || arrow.isNoPhysics()
                || arrow.isInWater()) {
            return;
        }
        Vec3 velocity = arrow.getDeltaMovement();
        if (velocity.lengthSqr() > 0.0D) {
            arrow.setDeltaMovement(velocity.add(0.0D, MiteSkeleton.skeletonArrowGravityCompensation(), 0.0D));
        }
    }

    /**
     * Spreads the full light search across the interval while keeping each mob's cadence intact.
     *
     * <p>Mobs loaded around a respawn point often begin ticking together. Their runtime IDs give
     * the expensive 21-by-9-by-21 search a stable phase without retaining per-mob state.
     */
    static boolean shouldSearchForLight(int tickCount, int entityId) {
        return Math.floorMod(tickCount + entityId, LIGHT_SEARCH_INTERVAL) == 0;
    }

    private static void limitSpawnerPopulation(MobSpawnEvent.PositionCheck event) {
        if (event.getSpawnType() != EntitySpawnReason.SPAWNER
                || !(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        Mob spawning = event.getEntity();
        int nearby = level.getEntitiesOfClass(
                        Mob.class,
                        spawning.getBoundingBox().inflate(16.0),
                        mob -> mob.isAlive() && sameSpawnFamily(mob.getType(), spawning.getType()))
                .size();
        if (MonsterTactics.spawnerAtCap(nearby)) event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
    }

    static boolean sameSpawnFamily(EntityType<?> first, EntityType<?> second) {
        EntityType<? extends Mob> firstReplacement = replacementFor(first);
        EntityType<? extends Mob> secondReplacement = replacementFor(second);
        EntityType<?> firstCanonical = firstReplacement == null ? first : firstReplacement;
        EntityType<?> secondCanonical = secondReplacement == null ? second : secondReplacement;
        return firstCanonical == secondCanonical;
    }

    private static void preventObservedDespawn(MobDespawnEvent event) {
        Mob mob = event.getEntity();
        if (!(mob instanceof Enemy) || !(mob.level() instanceof ServerLevel level)) return;
        boolean hasTarget = mob.getTarget() instanceof Player;
        boolean observed = level.getEntitiesOfClass(
                        Player.class,
                        mob.getBoundingBox().inflate(48.0),
                        player -> !player.isSpectator() && (mob.hasLineOfSight(player) || player.hasLineOfSight(mob)))
                .stream()
                .findAny()
                .isPresent();
        boolean specialEquipment = false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            var stack = mob.getItemBySlot(slot);
            if (!stack.isEmpty()
                    && (BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("infx")
                            || EnchantmentHelper.hasAnyEnchantments(stack))) {
                specialEquipment = true;
                break;
            }
        }
        if (hasTarget || observed || specialEquipment) event.setResult(MobDespawnEvent.Result.DENY);
    }

    private static void replaceVanillaSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()
                || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof Mob original)) {
            return;
        }
        EntityType<? extends Mob> replacementType = replacementForSpawn(level, original);
        if (replacementType == null) {
            return;
        }

        Mob replacement = replacementType.create(level, original.getSpawnType());
        if (replacement == null) {
            return;
        }
        initializeReplacement(level, original, replacement);
        event.setCanceled(true);
        level.getServer().execute(() -> {
            if (!replacement.isRemoved()) {
                level.addFreshEntityWithPassengers(replacement);
            }
        });
    }

    private static EntityType<? extends Mob> replacementForSpawn(ServerLevel level, Mob original) {
        // MITE has a single witch implementation. Unlike the other replacements, a manually
        // summoned or spawn-egg witch must not retain the modern vanilla class, because that
        // class has no R196 curse lifecycle. Leave loaded entities alone to avoid silently
        // replacing persisted vanilla-witch state in existing worlds.
        if (original.getType() == EntityType.WITCH && original.getSpawnType() != EntitySpawnReason.LOAD) {
            return ModEntityTypes.R196_WITCH.get();
        }
        if (isWorldSpawn(original.getSpawnType())) {
            if (original.getType() == EntityType.CREEPER) {
                int y = original.blockPosition().getY();
                // MITE caps the infernal replacement odds at 50% even far below y=0.
                if (y < 40 && original.getRandom().nextFloat() < Math.min(0.5F, Math.max(0, 40 - y) / 80.0F)) {
                    return ModEntityTypes.INFERNAL_CREEPER.get();
                }
            }
            EntityType<? extends Mob> vanillaReplacement = replacementFor(original.getType());
            if (vanillaReplacement != null) return vanillaReplacement;
            if (original.getType() == ModEntityTypes.VAMPIRE_BAT.get()
                    && level.dimension() == Underworld.LEVEL
                    && original.getRandom().nextInt(6) == 0) {
                return ModEntityTypes.GIANT_VAMPIRE_BAT.get();
            }
        }
        if (original.getType() == EntityType.SILVERFISH
                && original.getSpawnType() == EntitySpawnReason.TRIGGERED) {
            if (level.dimension() == Level.NETHER) return ModEntityTypes.NETHERSPAWN.get();
            if (level.dimension() == Underworld.LEVEL) return ModEntityTypes.HOARY_SILVERFISH.get();
            if (level.dimension() == Level.OVERWORLD && copperNear(level, original.blockPosition())) {
                return ModEntityTypes.COPPERSPINE.get();
            }
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    private static void initializeReplacement(ServerLevel level, Mob original, Mob replacement) {
        replacement.copyPosition(original);
        replacement.setDeltaMovement(original.getDeltaMovement());
        replacement.setCustomName(original.getCustomName());
        replacement.setCustomNameVisible(original.isCustomNameVisible());
        replacement.setSilent(original.isSilent());
        replacement.setInvulnerable(original.isInvulnerable());
        if (original.isPersistenceRequired()) replacement.setPersistenceRequired();
        replacement.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(replacement.blockPosition()),
                original.getSpawnType(),
                null);
        if (replacement instanceof Monster monster && isWorldSpawn(original.getSpawnType())) {
            MonsterTactics.equipForWorldAge(level, monster);
        }
        replacement.setHealth(replacement.getMaxHealth());
    }

    private static boolean copperNear(ServerLevel level, BlockPos origin) {
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-3, -3, -3), origin.offset(3, 3, 3))) {
            if (level.getBlockState(pos).is(Blocks.COPPER_ORE)
                    || level.getBlockState(pos).is(Blocks.DEEPSLATE_COPPER_ORE)) {
                return true;
            }
        }
        return false;
    }

    public static boolean trySpawnFireElemental(
            ServerLevel level, BlockPos lavaPos, FluidState fluidState, RandomSource random) {
        int rarity = level.dimension() == Level.OVERWORLD ? 16 + Math.max(0, lavaPos.getY()) : 16;
        if (random.nextInt(rarity) != 0
                || level.getEntitiesOfClass(
                                FireElemental.class,
                                new AABB(lavaPos).inflate(16.0D),
                                Entity::isAlive)
                        .size()
                        >= 2
                || !fluidState.is(FluidTags.LAVA)) {
            return false;
        }

        boolean canSpawn = false;
        if (level.dimension() == Level.NETHER
                && !level.getFluidState(lavaPos.below()).is(FluidTags.LAVA)
                && level.getBlockState(lavaPos.above()).isAir()
                && random.nextInt(4) == 0) {
            if (!fluidState.isSource()) {
                canSpawn = playerNear(level, lavaPos, 64.0D);
            } else if (random.nextInt(4) == 0) {
                canSpawn = playerNear(level, lavaPos, 16.0D);
            }
        }
        if (!canSpawn
                && random.nextInt(16) == 0
                && level.getFluidState(lavaPos.above()).isSource()
                && level.getFluidState(lavaPos.above()).is(FluidTags.LAVA)
                && level.getBlockState(lavaPos.above(2)).isAir()
                && level.getBlockState(lavaPos.above(3)).isAir()) {
            canSpawn = playerNear(level, lavaPos, 16.0D);
        }
        if (!canSpawn) return false;

        FireElemental elemental = ModEntityTypes.FIRE_ELEMENTAL.get().create(level, EntitySpawnReason.EVENT);
        if (elemental == null) return false;
        elemental.snapTo(lavaPos.getX() + 0.5D, lavaPos.getY() + 0.1D, lavaPos.getZ() + 0.5D, 0.0F, 0.0F);
        elemental.finalizeSpawn(
                level, level.getCurrentDifficultyAt(lavaPos), EntitySpawnReason.EVENT, null);
        return level.noCollision(elemental) && level.addFreshEntity(elemental);
    }

    private static boolean playerNear(ServerLevel level, BlockPos pos, double radius) {
        return level.getNearestPlayer(
                        pos.getX() + 0.5D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D,
                        radius,
                        false)
                != null;
    }

    private static void leadRangedProjectile(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel)
                || !(event.getEntity() instanceof Projectile projectile)
                || !(projectile.getOwner() instanceof Mob shooter)
                || !(shooter instanceof MiteWitch || shooter instanceof MiteBlaze || shooter instanceof MiteGhast)) {
            return;
        }
        var target = shooter.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        Vec3 velocity = projectile.getDeltaMovement();
        double speed = velocity.length();
        if (speed < 0.01) {
            return;
        }
        double flightTicks = Math.min(40.0, projectile.distanceTo(target) / speed);
        Vec3 targetVelocity = target.getDeltaMovement();
        Vec3 currentAim = target.getEyePosition().subtract(projectile.position());
        if (currentAim.lengthSqr() < 0.01 || targetVelocity.horizontalDistanceSqr() < 0.0001) {
            return;
        }
        Vec3 predictedAim = currentAim.add(
                targetVelocity.x * flightTicks,
                targetVelocity.y * Math.min(10.0, flightTicks),
                targetVelocity.z * flightTicks);
        double ballisticLift = velocity.y - currentAim.normalize().y * speed;
        Vec3 predictedVelocity = predictedAim.normalize().scale(speed);
        projectile.setDeltaMovement(
                predictedVelocity.x, predictedVelocity.y + ballisticLift, predictedVelocity.z);
    }

    public static EntityType<? extends Mob> replacementFor(EntityType<?> original) {
        if (original == EntityType.ZOMBIE) return ModEntityTypes.R196_ZOMBIE.get();
        if (original == EntityType.SKELETON) return ModEntityTypes.R196_SKELETON.get();
        if (original == EntityType.SPIDER) return ModEntityTypes.R196_SPIDER.get();
        if (original == EntityType.CAVE_SPIDER) return ModEntityTypes.R196_CAVE_SPIDER.get();
        if (original == EntityType.CREEPER) return ModEntityTypes.R196_CREEPER.get();
        if (original == EntityType.SLIME) return ModEntityTypes.R196_SLIME.get();
        if (original == EntityType.ENDERMAN) return ModEntityTypes.R196_ENDERMAN.get();
        if (original == EntityType.SQUID) return ModEntityTypes.R196_SQUID.get();
        if (original == EntityType.COD) return ModEntityTypes.R196_COD.get();
        if (original == EntityType.SALMON) return ModEntityTypes.R196_SALMON.get();
        if (original == EntityType.PUFFERFISH) return ModEntityTypes.R196_PUFFERFISH.get();
        if (original == EntityType.TROPICAL_FISH) return ModEntityTypes.R196_TROPICAL_FISH.get();
        if (original == EntityType.WITCH) return ModEntityTypes.R196_WITCH.get();
        if (original == EntityType.ZOMBIFIED_PIGLIN) return ModEntityTypes.R196_ZOMBIFIED_PIGLIN.get();
        if (original == EntityType.BLAZE) return ModEntityTypes.R196_BLAZE.get();
        if (original == EntityType.GHAST) return ModEntityTypes.R196_GHAST.get();
        if (original == EntityType.MAGMA_CUBE) return ModEntityTypes.MAGMA_CUBE.get();
        if (original == EntityType.COW) return ModEntityTypes.R196_COW.get();
        if (original == EntityType.CHICKEN) return ModEntityTypes.R196_CHICKEN.get();
        if (original == EntityType.SHEEP) return ModEntityTypes.R196_SHEEP.get();
        if (original == EntityType.PIG) return ModEntityTypes.R196_PIG.get();
        if (original == EntityType.HORSE) return ModEntityTypes.R196_HORSE.get();
        if (original == EntityType.OCELOT) return ModEntityTypes.R196_OCELOT.get();
        if (original == EntityType.WOLF) return ModEntityTypes.R196_WOLF.get();
        return null;
    }

    static boolean isWorldSpawn(EntitySpawnReason reason) {
        return reason == EntitySpawnReason.NATURAL
                || reason == EntitySpawnReason.CHUNK_GENERATION
                || reason == EntitySpawnReason.SPAWNER
                || reason == EntitySpawnReason.STRUCTURE
                || reason == EntitySpawnReason.REINFORCEMENT
                || reason == EntitySpawnReason.PATROL
                || reason == EntitySpawnReason.TRIAL_SPAWNER;
    }

    private static void applyWitchMagicDefense(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof MiteWitch witch)
                || event.getSource().getEntity() == witch
                || !event.getSource().is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            return;
        }
        event.addReductionModifier(
                net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.INNATE_RESISTANCE,
                (container, vanillaReduction) -> MiteWitch.magicDefenseReduction(
                        event.getSource(), container.getNewDamage()));
    }

    private static void shareTarget(LivingChangeTargetEvent event) {
        if (sharingTarget || !(event.getEntity() instanceof MiteMob)
                || !(event.getNewAboutToBeSetTarget() instanceof Player player)
                || !(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        sharingTarget = true;
        try {
            propagateTarget(level, event.getEntity(), player);
        } finally {
            sharingTarget = false;
        }
    }

    public static int propagateTarget(ServerLevel level, LivingEntity source, Player player) {
        if (!(source instanceof Mob sourceMob) || !participatesInGenericTargeting(sourceMob)) {
            return 0;
        }
        int shared = 0;
        for (Mob nearby : level.getEntitiesOfClass(
                Mob.class,
                source.getBoundingBox().inflate(16.0),
                mob -> mob != source && participatesInGenericTargeting(mob) && mob.getTarget() == null)) {
            nearby.setTarget(player);
            if (nearby.getTarget() == player) shared++;
        }
        return shared;
    }

    static boolean participatesInGenericTargeting(Mob mob) {
        // Pig zombies own their MITE 6/24-block player awareness and must not receive the
        // broad player-noise, illuminated-player, or cross-family target propagation rules.
        return mob instanceof Enemy && !(mob instanceof MiteEnderman || mob instanceof MiteZombifiedPiglin);
    }

    private static void amplifyInfernalCreeperExplosion(ExplosionEvent.Start event) {
        if (!(event.getExplosion().getDirectSourceEntity() instanceof MiteCreeper creeper)
                || creeper.variant() != MiteCreeper.Variant.INFERNAL
                || creeper.isAmplifyingExplosion()) {
            return;
        }
        event.setCanceled(true);
        creeper.setAmplifyingExplosion(true);
        try {
            float radius = creeper.isPowered() ? 12.0F : 6.0F;
            // MITE infernal creeper explosions are always flaming.
            event.getLevel().explode(
                    creeper,
                    creeper.getX(),
                    creeper.getY(),
                    creeper.getZ(),
                    radius,
                    true,
                    net.minecraft.world.level.Level.ExplosionInteraction.MOB);
        } finally {
            creeper.setAmplifyingExplosion(false);
        }
    }

    /** Ordinary R196 creepers cannot crack stone; infernal creepers retain normal blast terrain damage. */
    private static void limitCreeperTerrainDamage(ExplosionEvent.Detonate event) {
        if (!(event.getExplosion().getDirectSourceEntity() instanceof MiteCreeper creeper)) {
            return;
        }
        event.getAffectedBlocks().removeIf(pos -> {
            float hardness = event.getLevel().getBlockState(pos).getDestroySpeed(event.getLevel(), pos);
            return isCreeperTerrainProtected(creeper.variant(), hardness);
        });
    }

    static boolean isCreeperTerrainProtected(MiteCreeper.Variant variant, float hardness) {
        return hardness < 0.0F || (variant != MiteCreeper.Variant.INFERNAL && hardness >= 1.5F);
    }

    /** MITE netherspawn blasts leave their native netherrack and gold/quartz ore veins intact. */
    private static void protectNetherspawnTerrain(ExplosionEvent.Detonate event) {
        if (!(event.getExplosion().getDirectSourceEntity() instanceof MiteSilverfish silverfish)
                || silverfish.variant() != MiteSilverfish.Variant.NETHERSPAWN) {
            return;
        }
        event.getAffectedBlocks().removeIf(
                pos -> MiteSilverfish.isNetherspawnExplosionProtected(event.getLevel().getBlockState(pos)));
    }
}
