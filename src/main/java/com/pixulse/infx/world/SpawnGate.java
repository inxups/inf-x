package com.pixulse.infx.world;

import com.pixulse.infx.config.InfXConfig;
import com.pixulse.infx.entity.MonsterTactics;
import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.registry.tag.InfXEntityTypeTags;
import java.time.LocalDate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.util.RandomSource;

/**
 * Single authoritative entry point for every InfX "should this mob spawn, as what, how many"
 * decision. Events and the spawn-touching mixins delegate here; {@link SpawnDensity},
 * {@link SpawnRateTracker}, {@link Tension}, {@link MoonPhase} and {@link MonsterTactics} keep
 * their pure math and are orchestrated from this class. Combat behavior (frenzy damage, fire
 * transfer, targeting, projectiles) is deliberately not part of the gate.
 *
 * <p>The moon-brightness → regional-difficulty → gear chain
 * ({@code ServerLevelMoonBrightnessMixin} → {@code DifficultyInstanceMixin} → {@link Tension} →
 * {@code MonsterTactics.equipForWorldAge}) feeds spawn <em>quality</em>, not spawn
 * <em>decisions</em>, and stays out of this class.
 */
public final class SpawnGate {
    private SpawnGate() {}

    /** Master switch for every InfX mob rule. */
    public static boolean enabled() {
        return InfXConfig.INSTANCE.mobs.enabled.getValue();
    }

    // ------------------------------------------------------------------ natural spawn gating

    /**
     * InfX lunar spawn gating for a NATURAL spawn attempt. Returns {@code true} when the spawn is
     * allowed to proceed; the caller cancels the entity/spawn when {@code false}.
     *
     * <p>MITE moon cadence (was {@code MoonEvents.limitHostileSpawn}): creatures only on blue-moon
     * nights (1-in-400 tick gate); ambient/water 1-in-400; non-overworld enemies 1-in-4;
     * overworld sky-exposed hostile night spawns roll against the phase denominator
     * ({@code MoonPhase.outdoorHostileSpawnDenominator()}).
     */
    public static boolean shouldSpawnNatural(
            ServerLevel level, EntityType<?> type, MobCategory category, boolean enemy, boolean skyExposed) {
        if (category == MobCategory.CREATURE) {
            return MoonPhase.BLUE.isActiveInOverworldAtNight(level) && level.getGameTime() % 400L == 0L;
        }
        if (category == MobCategory.AMBIENT
                || category == MobCategory.WATER_CREATURE
                || category == MobCategory.WATER_AMBIENT
                || category == MobCategory.UNDERGROUND_WATER_CREATURE) {
            return level.getGameTime() % 400L == 0L;
        }
        if (!enemy) {
            return true;
        }
        if (!MoonPhase.isOverworld(level)) {
            return level.getRandom().nextInt(4) == 0;
        }
        MoonPhase phase = MoonPhase.at(level);
        if (MoonPhase.isNight(level) && skyExposed) {
            return !MoonPhase.BLUE.isActiveInOverworldAtNight(level)
                    && level.getRandom().nextInt(phase.outdoorHostileSpawnDenominator()) == 0;
        }
        return true;
    }

    /** Witch natural/structure spawns are replaced by the InfX witch; preserve the vanilla allow set. */
    public static boolean shouldCancelVanillaWitch(EntitySpawnReason reason) {
        return reason != EntitySpawnReason.STRUCTURE
                && reason != EntitySpawnReason.COMMAND
                && reason != EntitySpawnReason.SPAWN_ITEM_USE
                && reason != EntitySpawnReason.DISPENSER
                && reason != EntitySpawnReason.LOAD;
    }

    // ------------------------------------------------------------------ spawn density

    /** MITE per-night hostile spawn cadence: roll {@code (y<60 ? 0.1 : 0.17) ×} the daily rate. */
    public static boolean cadenceChancePasses(ServerLevel level, int y) {
        if (!enabled() || !InfXConfig.INSTANCE.mobs.spawnCadence.getValue()) {
            return true;
        }
        if (!MoonPhase.isOverworld(level)) {
            return true;
        }
        float modifier = SpawnRateTracker.get(level).modifier(level);
        return level.getRandom().nextFloat() < SpawnDensity.cadenceChance(y, modifier);
    }

    /** MITE caps hostile mobs at 50 per player instead of vanilla's 70. */
    public static int hostileCapCeiling(MobCategory category, int vanillaCap) {
        if (enabled() && category == MobCategory.MONSTER) {
            return 50;
        }
        return vanillaCap;
    }

    /**
     * MITE blood-moon ×1.5 + depth radius: whether the depth/stronghold scaling is active at all for
     * this category. When {@code false} the caller falls back to the vanilla per-player cap.
     */
    public static boolean depthCapActive(MobCategory category) {
        boolean depth = InfXConfig.INSTANCE.mobs.depthSpawn.getValue();
        boolean stronghold = InfXConfig.INSTANCE.mobs.strongholdProximity.getValue();
        return category == MobCategory.MONSTER && enabled() && (depth || stronghold);
    }

    /**
     * MITE per-player hostile ceiling scale for a depth-cap decision:
     * {@code depthFactor × (1 + strongholdProximity)}, clamped up to the blood-moon factor.
     */
    public static float depthCapScale(ServerLevel level, ServerPlayer player) {
        boolean depth = InfXConfig.INSTANCE.mobs.depthSpawn.getValue();
        boolean stronghold = InfXConfig.INSTANCE.mobs.strongholdProximity.getValue();
        float scale = depth ? SpawnDensity.densityCapScale(level, player.getBlockY()) : 1.0F;
        if (stronghold) {
            scale *= 1.0F + SpawnDensity.strongholdProximity(player);
        }
        return scale;
    }

    /** MITE blood-moon spawn radius ×1.5 (8→12 chunks): scale the global-cap chunk count. */
    public static int scaleSpawnChunkCount(ServerLevel level, int chunkCount) {
        if (enabled() && InfXConfig.INSTANCE.mobs.depthSpawn.getValue()) {
            return (int) (chunkCount * SpawnDensity.bloodMoonSpawnFactor(level));
        }
        return chunkCount;
    }

    // ------------------------------------------------------------------ spawners

    /** InfX block spawners ignore torch light but never spawn something sunlight would burn. */
    public static boolean allowSpawnerLight(
            EntitySpawnReason reason,
            boolean vanillaPlacementAllowed,
            boolean placementAllowedIgnoringLight,
            boolean burnsInDirectSunlight) {
        return MonsterTactics.allowsSpawnerLightBypass(
                reason, vanillaPlacementAllowed, placementAllowedIgnoringLight, burnsInDirectSunlight);
    }

    /** A spawner stops once its 16-block radius holds its family cap. */
    public static boolean limitSpawnerPopulation(int nearbyMatchingMobs) {
        return MonsterTactics.spawnerAtCap(nearbyMatchingMobs);
    }

    /** MITE spawner lifetime cap: 15 kills. */
    public static final int MAX_SPAWNER_KILLS = 15;

    /** MITE spawner lifetime: a block spawner stops permanently once its spawned mobs are killed 15 times. */
    public static boolean spawnerExhausted(int spawnsKilled) {
        return enabled()
                && InfXConfig.INSTANCE.mobs.spawnerLifetime.getValue()
                && spawnsKilled >= MAX_SPAWNER_KILLS;
    }

    /**
     * MITE dungeon depth layering ({@code WorldGenDungeons.pickMobSpawner}). Half the rooms roll a
     * flat {@code rand(4)}; the other half roll a depth term {@code (int)(max(1-y/64,0)×4)} plus a
     * jitter, with a negative result falling back to the flat roll. The cast binds the whole product
     * — {@code (int)(max*4)} — so danger 4 (Wight) needs y≤32, danger 5 (Demon Spider) y≤16 and
     * danger 6+ (Hellhound) y≤0. Only {@code spawnerDepthLayering} and the mob master switch gate it.
     */
    public static EntityType<?> spawnerDepthType(RandomSource random, int y) {
        int danger;
        if (random.nextInt(2) == 0) {
            danger = random.nextInt(4);
        } else {
            danger = (int) (Math.max(1.0F - (float) y / 64.0F, 0.0F) * 4) + random.nextInt(3) - random.nextInt(3);
            if (danger < 0) {
                danger = random.nextInt(4);
            }
        }
        return switch (danger) {
            case 0 -> EntityType.ZOMBIE;
            case 1 -> InfXEntityTypes.GHOUL.get();
            case 2 -> InfXEntityTypes.INFX_SKELETON.get();
            case 3 -> InfXEntityTypes.INFX_SPIDER.get();
            case 4 -> InfXEntityTypes.WIGHT.get();
            case 5 -> InfXEntityTypes.DEMON_SPIDER.get();
            default -> InfXEntityTypes.HELLHOUND.get();
        };
    }

    // ------------------------------------------------------------------ placement predicates

    /** InfX placement predicate for InfX monster types. */
    public static boolean checkMonsterSpawnRules(
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
        return true;
    }

    /** InfX bats spawn in empty cave air, not on the {@code BATS_SPAWNABLE_ON} ground tag. */
    public static boolean checkBatSpawnRules(
            EntityType<? extends Mob> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random) {
        ServerLevel serverLevel = level.getLevel();
        if (!serverLevel.isEmptyBlock(pos)
                || serverLevel.dimension() == Level.OVERWORLD && pos.getY() >= 63
                || !checkBatDepth(type, level, pos)) {
            return false;
        }
        boolean halloween = isBatHalloweenWindow(LocalDate.now());
        if (!halloween && random.nextBoolean()) {
            return false;
        }
        int lightBound = halloween ? 7 : 4;
        return maximumBatBlockLight(serverLevel, pos) <= random.nextInt(lightBound);
    }

    /** MITE ghast spacing (SpawnerAnimals.java:307): a ghast never spawns within 48 blocks of a player. */
    public static boolean checkGhastSpacing(
            EntityType<? extends Mob> type,
            net.minecraft.world.level.LevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random) {
        return !InfXConfig.INSTANCE.mobs.ghastSpacing.getValue()
                || level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 48.0, null) == null;
    }

    /** Vanilla creepers stay out of sky-lit Overworld nights unless 1-in-4. */
    public static boolean checkCreeperNightSky(
            EntityType<?> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random) {
        ServerLevel serverLevel = level.getLevel();
        return serverLevel.dimension() != Level.OVERWORLD
                || !MoonPhase.isNight(serverLevel)
                || random.nextInt(4) == 0
                || !serverLevel.canSeeSky(pos);
    }

    /** Vanilla spiders only slip through sky-lit Overworld nights 1-in-4. */
    public static boolean checkSpiderNightSky(
            EntityType<?> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random) {
        return level.getLevel().dimension() != Level.OVERWORLD
                || random.nextInt(4) != 0
                || !level.getLevel().canSeeSky(pos);
    }

    /** Vanilla slime natural spawns require a stone ceiling above. */
    public static boolean checkSlimeStoneAbove(
            EntityType<?> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random) {
        return !hasStoneAbove(level.getLevel(), pos);
    }

    /** Slime natural spawns require a stone ceiling above ({@code MobSpawnRules.hasStoneAbove}). */
    public static boolean hasStoneAbove(ServerLevel level, BlockPos pos) {
        int maximumY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
        for (int y = pos.getY() + 1; y <= maximumY; y++) {
            BlockState state = level.getBlockState(new BlockPos(pos.getX(), y, pos.getZ()));
            if (!state.isAir()) return state.is(Blocks.STONE);
        }
        return false;
    }

    // ------------------------------------------------------------------ replacement

    /** Canonical InfX type for a vanilla spawn, or {@code null} when the vanilla mob is kept. */
    public static EntityType<? extends Mob> replacementFor(EntityType<?> original) {
        if (isKeptVanilla(original)) return null;
        if (original == EntityType.BAT) return InfXEntityTypes.INFX_BAT.get();
        if (original == EntityType.SKELETON) return InfXEntityTypes.INFX_SKELETON.get();
        if (original == EntityType.SPIDER) return InfXEntityTypes.INFX_SPIDER.get();
        if (original == EntityType.CAVE_SPIDER) return InfXEntityTypes.INFX_CAVE_SPIDER.get();
        if (original == EntityType.CREEPER) return InfXEntityTypes.INFX_CREEPER.get();
        if (original == EntityType.SLIME) return InfXEntityTypes.INFX_SLIME.get();
        if (original == EntityType.ENDERMAN) return InfXEntityTypes.INFX_ENDERMAN.get();
        if (original == EntityType.SQUID) return InfXEntityTypes.INFX_SQUID.get();
        if (original == EntityType.COD) return InfXEntityTypes.INFX_COD.get();
        if (original == EntityType.SALMON) return InfXEntityTypes.INFX_SALMON.get();
        if (original == EntityType.PUFFERFISH) return InfXEntityTypes.INFX_PUFFERFISH.get();
        if (original == EntityType.TROPICAL_FISH) return InfXEntityTypes.INFX_TROPICAL_FISH.get();
        if (original == EntityType.WITCH) return InfXEntityTypes.INFX_WITCH.get();
        if (original == EntityType.ZOMBIFIED_PIGLIN) return InfXEntityTypes.INFX_ZOMBIFIED_PIGLIN.get();
        if (original == EntityType.BLAZE) return InfXEntityTypes.INFX_BLAZE.get();
        if (original == EntityType.GHAST) return InfXEntityTypes.INFX_GHAST.get();
        if (original == EntityType.MAGMA_CUBE) return InfXEntityTypes.MAGMA_CUBE.get();
        if (original == EntityType.COW) return InfXEntityTypes.INFX_COW.get();
        if (original == EntityType.CHICKEN) return InfXEntityTypes.INFX_CHICKEN.get();
        if (original == EntityType.SHEEP) return InfXEntityTypes.INFX_SHEEP.get();
        if (original == EntityType.PIG) return InfXEntityTypes.INFX_PIG.get();
        if (original == EntityType.HORSE) return InfXEntityTypes.INFX_HORSE.get();
        if (original == EntityType.OCELOT) return InfXEntityTypes.INFX_OCELOT.get();
        if (original == EntityType.WOLF) return InfXEntityTypes.INFX_WOLF.get();
        return null;
    }

    /** Two types are the same spawn family when they share a canonical InfX replacement. */
    public static boolean sameSpawnFamily(EntityType<?> first, EntityType<?> second) {
        EntityType<? extends Mob> firstReplacement = replacementFor(first);
        EntityType<? extends Mob> secondReplacement = replacementFor(second);
        EntityType<?> firstCanonical = firstReplacement == null ? first : firstReplacement;
        EntityType<?> secondCanonical = secondReplacement == null ? second : secondReplacement;
        return firstCanonical == secondCanonical;
    }

    /**
     * InfX spawn-type replacement decision for an entity joining the world, or {@code null} to keep
     * the vanilla entity. Special cases (witch, nether wither skeleton, longdead guardian, infernal
     * creeper, vampire bat, silverfish) plus the generic {@link #replacementFor} table.
     */
    public static EntityType<? extends Mob> replacementForSpawn(ServerLevel level, Mob original) {
        if (isKeptVanilla(original.getType())) {
            return null;
        }
        // InfX has a single witch implementation. Unlike the other replacements, a manually
        // summoned or spawn-egg witch must not retain the modern vanilla class, because that
        // class has no INFX curse lifecycle. Leave loaded entities alone to avoid silently
        // replacing persisted vanilla-witch state in existing worlds.
        if (original.getType() == EntityType.WITCH && original.getSpawnType() != EntitySpawnReason.LOAD) {
            return InfXEntityTypes.INFX_WITCH.get();
        }
        // Natural Nether wither-skeleton spawns intentionally retain the vanilla entity and its
        // loot mixin. Only structures and explicit egg/dispenser creation receive the MITE type.
        if (original.getType() == EntityType.WITHER_SKELETON
                && shouldReplaceWitherSkeleton(original.getSpawnType())) {
            return InfXEntityTypes.INFX_WITHER_SKELETON.get();
        }
        if (original.getType() == InfXEntityTypes.LONGDEAD.get()
                && shouldReplaceLongdeadWithGuardian(
                        original.getType(), level.dimension(), original.getSpawnType(), original.getRandom().nextInt(6))) {
            return InfXEntityTypes.LONGDEAD_GUARDIAN.get();
        }
        if (isWorldSpawn(original.getSpawnType())) {
            if (original.getType() == EntityType.CREEPER) {
                int y = original.blockPosition().getY();
                // InfX caps the infernal replacement odds at 50% even far below y=0.
                if (y < 40 && original.getRandom().nextFloat() < Math.min(0.5F, Math.max(0, 40 - y) / 80.0F)) {
                    return InfXEntityTypes.INFERNAL_CREEPER.get();
                }
            }
            EntityType<? extends Mob> vanillaReplacement = replacementFor(original.getType());
            if (vanillaReplacement != null) return vanillaReplacement;
            if (original.getType() == InfXEntityTypes.VAMPIRE_BAT.get()
                    && level.dimension() == Underworld.LEVEL
                    && original.getRandom().nextInt(6) == 0) {
                return InfXEntityTypes.GIANT_VAMPIRE_BAT.get();
            }
        }
        if (original.getType() == EntityType.SILVERFISH
                && original.getSpawnType() == EntitySpawnReason.TRIGGERED) {
            if (level.dimension() == Level.NETHER) return InfXEntityTypes.NETHERSPAWN.get();
            if (level.dimension() == Underworld.LEVEL) return InfXEntityTypes.HOARY_SILVERFISH.get();
            if (level.dimension() == Level.OVERWORLD && copperNear(level, original.blockPosition())) {
                return InfXEntityTypes.COPPERSPINE.get();
            }
        }
        return null;
    }

    private static boolean copperNear(ServerLevel level, BlockPos origin) {
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-3, -3, -3), origin.offset(3, 3, 3))) {
            if (level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.COPPER_ORE)
                    || level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.DEEPSLATE_COPPER_ORE)) {
                return true;
            }
        }
        return false;
    }

    /** World/spawner-driven spawn reasons that InfX replaces. */
    public static boolean isWorldSpawn(EntitySpawnReason reason) {        return reason == EntitySpawnReason.NATURAL
                || reason == EntitySpawnReason.CHUNK_GENERATION
                || reason == EntitySpawnReason.SPAWNER
                || reason == EntitySpawnReason.STRUCTURE
                || reason == EntitySpawnReason.REINFORCEMENT
                || reason == EntitySpawnReason.PATROL
                || reason == EntitySpawnReason.TRIAL_SPAWNER;
    }

    /** Natural nether wither skeletons keep the vanilla entity; only structures/eggs/dispensers swap. */
    public static boolean shouldReplaceWitherSkeleton(EntitySpawnReason reason) {
        return reason == EntitySpawnReason.STRUCTURE
                || reason == EntitySpawnReason.SPAWN_ITEM_USE
                || reason == EntitySpawnReason.DISPENSER;
    }

    public static boolean shouldReplaceLongdeadWithGuardian(
            EntityType<?> originalType,
            ResourceKey<Level> dimension,
            EntitySpawnReason reason,
            int roll) {
        if (roll < 0 || roll >= 6) {
            throw new IllegalArgumentException("Longdead Guardian roll must be in [0, 6): " + roll);
        }
        return originalType == InfXEntityTypes.LONGDEAD.get()
                && dimension == Underworld.LEVEL
                && reason == EntitySpawnReason.NATURAL
                && roll == 0;
    }

    // ------------------------------------------------------------------ phantoms

    /** -1 = deny the wave, 0 = keep vanilla behavior, n = allow n phantoms per player wave. */
    public static int phantomWaveCount(Level level) {
        if (!enabled() || !InfXConfig.INSTANCE.mobs.phantomMoonSpawns.getValue()) {
            return 0;
        }
        if (!MoonPhase.isOverworld(level)) return -1;
        MoonPhase phase = MoonPhase.at(level);
        if (!MoonPhase.isNight(level)) return -1;
        if (phase == MoonPhase.BLOOD || phase == MoonPhase.PHANTOM) {
            return 1 + level.getRandom().nextInt(2);
        }
        return -1;
    }

    // ------------------------------------------------------------------ patrols

    /** InfX gates pillager patrols behind the village-generation unlock, not vanilla's five days. */
    public static boolean allowPatrolSpawning(ServerLevel level) {
        return StructureGenerationGates.isUnlocked(StructureGenerationGates.VILLAGE_RULE, level);
    }

    // ------------------------------------------------------------------ despawn

    /** MITE: bone-lord troops, observed/targeted mobs and InfX-gear holders never despawn. */
    public static boolean preventDespawn(Mob mob, ServerLevel level) {
        if (!(mob instanceof Enemy)) return false;
        if (BoneLordSummonRegistry.get(level).isTracked(mob.getUUID())) {
            return true;
        }
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
        return hasTarget || observed || specialEquipment;
    }

    // ------------------------------------------------------------------ shared predicates

    /** MITE frenzy predicate: a blood-moon night in the overworld with frenzy enabled. */
    public static boolean isBloodMoonFrenzied(Level level) {
        return enabled()
                && InfXConfig.INSTANCE.mobs.bloodMoonFrenzy.getValue()
                && MoonPhase.BLOOD.isActiveInOverworldAtNight(level);
    }

    /** InfX hostile-piglin predicate: single config gate for target AI, barter blocks and kill drops. */
    public static boolean isPiglinHostilityEnabled() {
        return enabled() && InfXConfig.INSTANCE.mobs.piglinHostility.getValue();
    }

    /** MITE 14h/10h day window: overworld nights never sun-burn hostile mobs. */
    public static boolean miteDayNightPreventsBurn(Level level) {
        return enabled()
                && InfXConfig.INSTANCE.mobs.miteDayNight.getValue()
                && MoonPhase.isOverworld(level)
                && MoonPhase.isNight(level);
    }

    /** Sun-exposed spawner position check: bright outside, dry and sky-visible head block. */
    public static boolean isExposedToSunlight(ServerLevel level, BlockPos pos) {
        BlockPos head = pos.above();
        return level.isBrightOutside() && !level.isRainingAt(head) && level.canSeeSky(head);
    }

    // ------------------------------------------------------------------ private helpers

    /**
     * Third-party escape hatch for entity replacement: {@code mobs.replaceVanillaMobs} off, or the
     * type listed in the {@code infx:keep_vanilla_entity} tag, keeps the vanilla entity. The tag
     * query must tolerate unbound holders (unit tests never load datapack tags) and treat that as
     * "not listed".
     */
    private static boolean isKeptVanilla(EntityType<?> type) {
        if (!InfXConfig.INSTANCE.mobs.replaceVanillaMobs.getValue()) {
            return true;
        }
        Holder<EntityType<?>> holder = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type);
        if (!(holder instanceof Holder.Reference<?> reference)) {
            return false;
        }
        try {
            return reference.tags().anyMatch(InfXEntityTypeTags.KEEP_VANILLA_ENTITY::equals);
        } catch (IllegalStateException tagsNotBound) {
            return false;
        }
    }

    private static int maximumBatBlockLight(ServerLevel level, BlockPos pos) {
        int maximum = level.getBrightness(LightLayer.BLOCK, pos);
        for (BlockPos sample = pos.below(); sample.getY() >= level.getMinY(); sample = sample.below()) {
            if (level.getBlockState(sample).isSolidRender()) {
                break;
            }
            maximum = Math.max(maximum, level.getBrightness(LightLayer.BLOCK, sample));
        }
        return maximum;
    }

    private static boolean checkBatDepth(EntityType<? extends Mob> type, ServerLevelAccessor level, BlockPos pos) {
        ServerLevel serverLevel = level.getLevel();
        if (type == InfXEntityTypes.INFX_BAT.get()) {
            return true;
        }
        int maximumY = type == InfXEntityTypes.NIGHTWING.get() ? 32 : 48;
        return serverLevel.dimension() != Level.OVERWORLD
                || pos.getY() <= maximumY
                || MoonPhase.BLOOD.isActiveInOverworldAtNight(serverLevel);
    }

    private static boolean earthElementalGround(ServerLevel level, BlockPos pos) {
        return com.pixulse.infx.entity.EarthElemental.isValidGround(level.getBlockState(pos.below()));
    }

    private static boolean clayGolemGround(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(Blocks.CLAY);
    }

    public static boolean isBatHalloweenWindow(LocalDate date) {
        return (date.getMonthValue() == 10 && date.getDayOfMonth() >= 20)
                || (date.getMonthValue() == 11 && date.getDayOfMonth() <= 3);
    }
}
