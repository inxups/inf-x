package com.pixulse.infx.entity;

import com.pixulse.infx.registry.ModEntityTypes;
import com.pixulse.infx.world.R196MoonPhase;
import com.pixulse.infx.world.R196RiverBiomes;
import com.pixulse.infx.world.Underworld;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.cubemob.MagmaCube;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.EnderManAngerEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Registration, spawn replacement and cross-family AI hooks for R196 mobs. */
public final class R196MonsterEvents {
    private static boolean sharingTarget;

    private R196MonsterEvents() {}

    public static void register(IEventBus modBus, IEventBus gameBus) {
        modBus.addListener(R196MonsterEvents::createAttributes);
        modBus.addListener(R196MonsterEvents::registerSpawnPlacements);
        gameBus.addListener(R196MonsterEvents::finalizeSpawn);
        gameBus.addListener(R196MonsterEvents::leadRangedProjectile);
        gameBus.addListener(R196MonsterEvents::replaceVanillaSpawn);
        gameBus.addListener(R196MonsterEvents::suppressSneakingEndermanAnger);
        gameBus.addListener(R196MonsterEvents::shareTarget);
        gameBus.addListener(R196MonsterEvents::amplifyInfernalCreeperExplosion);
        gameBus.addListener(R196MonsterEvents::limitCreeperTerrainDamage);
        gameBus.addListener(R196MonsterEvents::attractToPlayerActivity);
        gameBus.addListener(R196MonsterEvents::coordinateAndSeekLight);
        gameBus.addListener(R196MonsterEvents::limitSpawnerPopulation);
        gameBus.addListener(R196MonsterEvents::preventObservedDespawn);
    }

    private static void createAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.R196_ZOMBIE.get(), R196Zombie.attributes(R196Zombie.Variant.ZOMBIE).build());
        event.put(ModEntityTypes.INVISIBLE_STALKER.get(), R196Zombie.attributes(R196Zombie.Variant.INVISIBLE_STALKER).build());
        event.put(ModEntityTypes.GHOUL.get(), R196Zombie.attributes(R196Zombie.Variant.GHOUL).build());
        event.put(ModEntityTypes.SHADOW.get(), R196Zombie.attributes(R196Zombie.Variant.SHADOW).build());
        event.put(ModEntityTypes.WIGHT.get(), R196Zombie.attributes(R196Zombie.Variant.WIGHT).build());
        event.put(ModEntityTypes.REVENANT.get(), R196Zombie.attributes(R196Zombie.Variant.REVENANT).build());

        event.put(ModEntityTypes.R196_SKELETON.get(), R196Skeleton.attributes(R196Skeleton.Variant.SKELETON).build());
        event.put(ModEntityTypes.LONGDEAD.get(), R196Skeleton.attributes(R196Skeleton.Variant.LONGDEAD).build());
        event.put(ModEntityTypes.BONE_LORD.get(), R196Skeleton.attributes(R196Skeleton.Variant.BONE_LORD).build());
        event.put(ModEntityTypes.ANCIENT_BONE_LORD.get(), R196Skeleton.attributes(R196Skeleton.Variant.ANCIENT_BONE_LORD).build());

        event.put(ModEntityTypes.R196_SPIDER.get(), R196Spider.attributes(R196Spider.Variant.SPIDER).build());
        event.put(ModEntityTypes.R196_CAVE_SPIDER.get(), R196Spider.attributes(R196Spider.Variant.CAVE_SPIDER).build());
        event.put(ModEntityTypes.BLACK_WIDOW_SPIDER.get(), R196Spider.attributes(R196Spider.Variant.BLACK_WIDOW).build());
        event.put(ModEntityTypes.DEMON_SPIDER.get(), R196Spider.attributes(R196Spider.Variant.DEMON).build());
        event.put(ModEntityTypes.WOOD_SPIDER.get(), R196Spider.attributes(R196Spider.Variant.WOOD).build());
        event.put(ModEntityTypes.PHASE_SPIDER.get(), R196Spider.attributes(R196Spider.Variant.PHASE).build());

        event.put(ModEntityTypes.R196_CREEPER.get(), R196Creeper.attributes(R196Creeper.Variant.CREEPER).build());
        event.put(ModEntityTypes.INFERNAL_CREEPER.get(), R196Creeper.attributes(R196Creeper.Variant.INFERNAL).build());

        for (var type : List.of(
                ModEntityTypes.R196_SLIME,
                ModEntityTypes.JELLY,
                ModEntityTypes.BLOB,
                ModEntityTypes.OOZE,
                ModEntityTypes.PUDDING)) {
            event.put(type.get(), R196Slime.attributes().build());
        }
        event.put(ModEntityTypes.MAGMA_CUBE.get(), R196MagmaCube.attributes().build());

        for (var type : List.of(ModEntityTypes.NETHERSPAWN, ModEntityTypes.COPPERSPINE, ModEntityTypes.HOARY_SILVERFISH)) {
            event.put(type.get(), R196Silverfish.attributes().build());
        }

        event.put(ModEntityTypes.VAMPIRE_BAT.get(), R196Bat.attributes(R196Bat.Variant.VAMPIRE).build());
        event.put(ModEntityTypes.NIGHTWING.get(), R196Bat.attributes(R196Bat.Variant.NIGHTWING).build());
        event.put(ModEntityTypes.GIANT_VAMPIRE_BAT.get(), R196Bat.attributes(R196Bat.Variant.GIANT_VAMPIRE).build());
        event.put(ModEntityTypes.HELLHOUND.get(), R196Wolf.attributes(R196Wolf.Variant.HELLHOUND).build());
        event.put(ModEntityTypes.DIRE_WOLF.get(), R196Wolf.attributes(R196Wolf.Variant.DIRE_WOLF).build());
        event.put(ModEntityTypes.FIRE_ELEMENTAL.get(), R196FireElemental.attributes().build());
        event.put(ModEntityTypes.EARTH_ELEMENTAL.get(), R196EarthElemental.attributes().build());

        event.put(ModEntityTypes.R196_ENDERMAN.get(), R196Enderman.attributes().build());
        event.put(ModEntityTypes.R196_SQUID.get(), R196Squid.attributes().build());
        event.put(ModEntityTypes.R196_WITCH.get(), R196Witch.attributes().build());
        event.put(ModEntityTypes.R196_ZOMBIFIED_PIGLIN.get(), R196ZombifiedPiglin.attributes().build());
        event.put(ModEntityTypes.R196_BLAZE.get(), R196Blaze.attributes().build());
        event.put(ModEntityTypes.R196_GHAST.get(), R196Ghast.attributes().build());
    }

    private static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
                EntityTypes.CREEPER,
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
                EntityTypes.SPIDER,
                null,
                null,
                (type, level, reason, pos, random) -> level.getLevel().dimension() != Level.OVERWORLD
                        || random.nextInt(4) != 0
                        || !level.getLevel().canSeeSky(pos),
                RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(
                EntityTypes.SLIME,
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
                    R196MonsterEvents::checkR196MonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
        for (var type : List.of(ModEntityTypes.LONGDEAD, ModEntityTypes.BONE_LORD, ModEntityTypes.ANCIENT_BONE_LORD)) {
            event.register(type.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    R196MonsterEvents::checkR196MonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
        for (var type : List.of(
                ModEntityTypes.BLACK_WIDOW_SPIDER,
                ModEntityTypes.DEMON_SPIDER,
                ModEntityTypes.WOOD_SPIDER,
                ModEntityTypes.PHASE_SPIDER)) {
            event.register(type.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    R196MonsterEvents::checkR196MonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
        event.register(ModEntityTypes.INFERNAL_CREEPER.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> pos.getY() < 40
                        && Monster.checkMonsterSpawnRules(type, level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        for (var type : List.of(ModEntityTypes.JELLY, ModEntityTypes.BLOB, ModEntityTypes.OOZE, ModEntityTypes.PUDDING)) {
            event.register(type.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    R196MonsterEvents::checkR196MonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
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
                R196MonsterEvents::checkR196MonsterSpawnRules,
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
                R196MonsterEvents::checkR196MonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    static boolean checkR196MonsterSpawnRules(
            EntityType<? extends Mob> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random) {
        if (!Monster.checkMonsterSpawnRules(type, level, reason, pos, random)) return false;
        ServerLevel serverLevel = level.getLevel();
        String path = BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath();
        if (path.equals("earth_elemental") && !earthElementalGround(serverLevel, pos)) return false;
        if (serverLevel.dimension() != Level.OVERWORLD) return true;

        int y = pos.getY();
        boolean bloodMoonUp = R196MoonPhase.at(serverLevel) == R196MoonPhase.BLOOD
                && !isDay(serverLevel);
        boolean freezing = serverLevel.getBiome(pos).value().getBaseTemperature() <= 0.15F;
        boolean desert = serverLevel.getBiome(pos).is(net.minecraft.world.level.biome.Biomes.DESERT)
                || serverLevel.getBiome(pos).is(R196RiverBiomes.DESERT_RIVER);
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
                || R196MoonPhase.at(serverLevel) == R196MoonPhase.BLOOD && !isDay(serverLevel);
    }

    private static boolean earthElementalGround(ServerLevel level, BlockPos pos) {
        var ground = level.getBlockState(pos.below());
        return ground.is(Blocks.STONE)
                || ground.is(Blocks.OBSIDIAN)
                || ground.is(Blocks.NETHERRACK)
                || ground.is(Blocks.END_STONE);
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
        if (event.getEntity() instanceof Monster monster
                && monster.level() instanceof ServerLevel level
                && isWorldSpawn(event.getSpawnType())) {
            R196MonsterTactics.equipForWorldAge(level, monster);
        }
        if (event.getEntity().getType() == EntityTypes.WITCH
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
        double radius = R196MoonPhase.at(level) == R196MoonPhase.BLOOD ? 96.0 : 48.0;
        for (Mob mob : level.getEntitiesOfClass(
                Mob.class,
                new AABB(event.getEventPosition(), event.getEventPosition()).inflate(radius),
                candidate -> candidate instanceof Enemy && candidate.isAlive())) {
            mob.setTarget(player);
            if (!mob.hasLineOfSight(player)) {
                mob.getNavigation().moveTo(event.getEventPosition().x, event.getEventPosition().y, event.getEventPosition().z, 1.05);
            }
        }
    }

    private static void coordinateAndSeekLight(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob)
                || !(mob instanceof Enemy)
                || !(mob.level() instanceof ServerLevel level)
                || mob.tickCount % 20 != 0) {
            return;
        }
        if (mob.getTarget() != null) {
            R196MonsterTactics.cooperate(level, mob);
            return;
        }
        double range = R196MoonPhase.at(level) == R196MoonPhase.BLOOD ? 96.0 : 48.0;
        Player illuminated = level.getNearestPlayer(
                mob.getX(), mob.getY(), mob.getZ(), range,
                player -> !player.isSpectator()
                        && level.getBrightness(LightLayer.BLOCK, player.blockPosition()) >= 7);
        if (illuminated != null) {
            mob.setTarget(illuminated);
            return;
        }
        if (mob.tickCount % 80 == 0) {
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
        if (R196MonsterTactics.spawnerAtCap(nearby)) event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
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
        if (isWorldSpawn(original.getSpawnType())) {
            if (original.getType() == EntityTypes.CREEPER) {
                int y = original.blockPosition().getY();
                if (y < 40 && original.getRandom().nextFloat() < Math.max(0, 40 - y) / 80.0F) {
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
        if (original.getType() == EntityTypes.SILVERFISH
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
            R196MonsterTactics.equipForWorldAge(level, monster);
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
                                R196FireElemental.class,
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

        R196FireElemental elemental = ModEntityTypes.FIRE_ELEMENTAL.get().create(level, EntitySpawnReason.EVENT);
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
                || !(shooter instanceof R196Witch || shooter instanceof R196Blaze || shooter instanceof R196Ghast)) {
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
        if (original == EntityTypes.ZOMBIE) return ModEntityTypes.R196_ZOMBIE.get();
        if (original == EntityTypes.SKELETON) return ModEntityTypes.R196_SKELETON.get();
        if (original == EntityTypes.SPIDER) return ModEntityTypes.R196_SPIDER.get();
        if (original == EntityTypes.CAVE_SPIDER) return ModEntityTypes.R196_CAVE_SPIDER.get();
        if (original == EntityTypes.CREEPER) return ModEntityTypes.R196_CREEPER.get();
        if (original == EntityTypes.SLIME) return ModEntityTypes.R196_SLIME.get();
        if (original == EntityTypes.ENDERMAN) return ModEntityTypes.R196_ENDERMAN.get();
        if (original == EntityTypes.SQUID) return ModEntityTypes.R196_SQUID.get();
        if (original == EntityTypes.WITCH) return ModEntityTypes.R196_WITCH.get();
        if (original == EntityTypes.ZOMBIFIED_PIGLIN) return ModEntityTypes.R196_ZOMBIFIED_PIGLIN.get();
        if (original == EntityTypes.BLAZE) return ModEntityTypes.R196_BLAZE.get();
        if (original == EntityTypes.GHAST) return ModEntityTypes.R196_GHAST.get();
        if (original == EntityTypes.MAGMA_CUBE) return ModEntityTypes.MAGMA_CUBE.get();
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

    private static void suppressSneakingEndermanAnger(EnderManAngerEvent event) {
        if (event.getEntity() instanceof R196Enderman && event.getPlayer().isShiftKeyDown()) {
            event.setCanceled(true);
        }
    }

    private static void shareTarget(LivingChangeTargetEvent event) {
        if (sharingTarget || !(event.getEntity() instanceof R196Mob)
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
        int shared = 0;
        for (Mob nearby : level.getEntitiesOfClass(
                Mob.class,
                source.getBoundingBox().inflate(16.0),
                mob -> mob != source && mob instanceof Enemy && mob.getTarget() == null)) {
            nearby.setTarget(player);
            if (nearby.getTarget() == player) shared++;
        }
        return shared;
    }

    private static void amplifyInfernalCreeperExplosion(ExplosionEvent.Start event) {
        if (!(event.getExplosion().getDirectSourceEntity() instanceof R196Creeper creeper)
                || creeper.variant() != R196Creeper.Variant.INFERNAL
                || creeper.isAmplifyingExplosion()) {
            return;
        }
        event.setCanceled(true);
        creeper.setAmplifyingExplosion(true);
        try {
            float radius = creeper.isPowered() ? 12.0F : 6.0F;
            event.getLevel().explode(
                    creeper, creeper.getX(), creeper.getY(), creeper.getZ(), radius, net.minecraft.world.level.Level.ExplosionInteraction.MOB);
        } finally {
            creeper.setAmplifyingExplosion(false);
        }
    }

    private static void limitCreeperTerrainDamage(ExplosionEvent.Detonate event) {
        if (!(event.getExplosion().getDirectSourceEntity() instanceof R196Creeper creeper)
                || creeper.variant() != R196Creeper.Variant.CREEPER) {
            return;
        }
        event.getAffectedBlocks().removeIf(pos -> {
            float hardness = event.getLevel().getBlockState(pos).getDestroySpeed(event.getLevel(), pos);
            return hardness < 0.0F || hardness > 1.5F;
        });
    }
}
