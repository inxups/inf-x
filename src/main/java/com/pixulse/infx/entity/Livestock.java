package com.pixulse.infx.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** MITE INFX livestock wellness and the goals that keep food, water, and freedom healthy. */
public final class Livestock {
    static final String FOOD = "infx_livestock_food";
    static final String WATER = "infx_livestock_water";
    static final String FREEDOM = "infx_livestock_freedom";
    static final String WELLNESS_INITIALIZED = "infx_livestock_wellness_initialized";
    static final String PANIC_UNTIL = "infx_livestock_panic_until";
    static final String PANIC_ORIGIN = "infx_livestock_panic_origin";
    static final String GOALS_ADDED = "infx_livestock_goals_added";

    static final float INITIAL_WELLNESS_MIN = 0.8F;
    static final float WELLNESS_BENEFIT = 0.1F;
    static final float WELLNESS_PENALTY = -0.005F;
    static final float RAIN_WATER_BENEFIT = WELLNESS_BENEFIT / 10.0F;
    static final float WELL_THRESHOLD = 0.25F;
    static final float NEEDY_THRESHOLD = 0.5F;
    static final float DESPERATE_THRESHOLD = 0.05F;
    static final int NEEDS_TICK_INTERVAL = 100;
    static final int BASE_SEARCH_RANGE = 16;
    static final int VERY_NEEDY_SEARCH_RANGE = 32;
    static final int DESPERATE_SEARCH_RANGE = 48;
    static final int PANIC_ESCAPE_HORIZONTAL_RANGE = 4;
    static final int PANIC_ESCAPE_VERTICAL_RANGE = 0;
    static final int PANIC_ESCAPE_ATTEMPTS = 8;
    static final double PANIC_ESCAPE_SPEED = 1.0;
    static final double PANIC_MOVEMENT_SPEED_MULTIPLIER = 1.5;
    private static final Identifier PANIC_MOVEMENT_SPEED_ID =
            Identifier.fromNamespaceAndPath("infx", "livestock_panic_speed");
    private static final AttributeModifier PANIC_MOVEMENT_SPEED =
            new AttributeModifier(
                    PANIC_MOVEMENT_SPEED_ID,
                    PANIC_MOVEMENT_SPEED_MULTIPLIER - 1.0,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    static final String HORSE_RETRY = "infx_horse_tame_retry";
    static final long HORSE_RETRY_TICKS = 4_000L;
    static final String HORSE_FEED_RETRY = "infx_horse_feed_retry";
    static final long HORSE_FEED_RETRY_TICKS = 4_000L;

    private Livestock() {}

    /**
     * Define a per-class isWell flag. Its accessor is registered while the concrete INFX entity
     * class initializes, before the entity's {@link SynchedEntityData.Builder} sizes its data array.
     * It must not use {@code Animal.class}, which collides with vanilla Cow/Pig/etc. variant data.
     */
    public static void defineWellData(
            SynchedEntityData.Builder entityData, EntityDataAccessor<Boolean> well) {
        entityData.define(well, true);
    }

    /**
     * MITE isWell: the minimum of food, water, and freedom must remain at least 0.25.
     * The server owns the float values; the synced flag is used by clients for the unwell skin.
     */
    public static boolean isWell(Animal animal) {
        EntityDataAccessor<Boolean> well = wellData(animal);
        if (well == null) return true;
        if (animal.level() instanceof ServerLevel) {
            return isWell(food(animal), water(animal), freedom(animal));
        }
        return animal.getEntityData().get(well);
    }

    static boolean isWell(float food, float water, float freedom) {
        return Math.min(freedom, Math.min(food, water)) >= WELL_THRESHOLD;
    }

    public static void setWell(Animal animal, boolean value) {
        EntityDataAccessor<Boolean> well = wellData(animal);
        if (well != null) {
            animal.getEntityData().set(well, value);
        }
    }

    private static @Nullable EntityDataAccessor<Boolean> wellData(Animal animal) {
        if (animal instanceof InfxCow) return InfxCow.dataWell();
        if (animal instanceof InfxChicken) return InfxChicken.dataWell();
        if (animal instanceof InfxPig) return InfxPig.dataWell();
        if (animal instanceof InfxSheep) return InfxSheep.dataWell();
        return null;
    }

    /** True for cow/pig/sheep/chicken classes (including vanilla). */
    public static boolean isLivestock(Entity entity) {
        return entity instanceof AbstractCow
                || entity instanceof Pig
                || entity instanceof Sheep
                || entity instanceof Chicken;
    }

    /** INFX livestock that carry the client-synced isWell flag and unwell skin. */
    public static boolean hasSickSkin(Entity entity) {
        return entity instanceof InfxCow
                || entity instanceof InfxChicken
                || entity instanceof InfxPig
                || entity instanceof InfxSheep;
    }

    /**
     * Install MITE-style seek and flee goals once on INFX livestock (cow/chicken/sheep/pig).
     * Horses are not livestock and must not call this.
     */
    public static void ensureGoals(Animal animal) {
        if (animal instanceof AbstractHorse) {
            return;
        }
        if (!hasLivestockPanicGoal(animal)) {
            animal.goalSelector.addGoal(1, new LivestockPanicGoal(animal));
        }
        if (!animal.getPersistentData().getBooleanOr(GOALS_ADDED, false)) {
            animal.goalSelector.addGoal(2, new NeedsGoal(animal));
            animal.goalSelector.addGoal(1, new AvoidEntityGoal<>(
                    animal,
                    Mob.class,
                    mob -> mob instanceof Enemy,
                    10.0F,
                    1.15,
                    1.4,
                    LivingEntity::isAlive));
            animal.getPersistentData().putBoolean(GOALS_ADDED, true);
        }
    }

    private static boolean hasLivestockPanicGoal(Animal animal) {
        return animal.goalSelector.getAvailableGoals().stream()
                .anyMatch(goal -> goal.getGoal() instanceof LivestockPanicGoal);
    }

    /** MITE advances the three wellness values at most once every 100 ticks. */
    public static void serverTick(Animal animal) {
        if (!(animal.level() instanceof ServerLevel level)) return;
        ensureWellness(animal);
        syncPanicMovementSpeed(animal, level.getGameTime());
        if (animal.tickCount % NEEDS_TICK_INTERVAL == 0 && animal.getRandom().nextInt(10) > 0) {
            update(level, animal);
        }
    }

    /**
     * Start the MITE panic response only after a livestock hit has completed. The victim flees
     * the attacker while nearby livestock flee the wounded herd member.
     */
    public static void onHurt(Animal animal, DamageSource source, float inflictedDamage) {
        if (inflictedDamage <= 0.0F
                || !hasSickSkin(animal)
                || !(animal.level() instanceof ServerLevel level)) {
            return;
        }
        Entity attacker = source.getEntity();
        if (attacker == null) {
            attacker = source.getDirectEntity();
        }
        panic(level, animal, attacker == null ? animal.blockPosition() : attacker.blockPosition());
    }

    /** MITE food interactions add 0.5 food only after vanilla accepts the item. */
    public static void markFedAfterInteraction(
            Animal animal, boolean offeredFood, InteractionResult result) {
        if (!(animal.level() instanceof ServerLevel) || !foodInteractionSucceeded(offeredFood, result)) {
            return;
        }
        markFed(animal, 0L);
    }

    static boolean foodInteractionSucceeded(boolean offeredFood, InteractionResult result) {
        return offeredFood
                && result instanceof InteractionResult.Success success
                && success.wasItemInteraction()
                && success.swingSource() != InteractionResult.SwingSource.NONE;
    }

    public static boolean canMateWith(ServerLevel level, Animal self, Animal partner) {
        return canBreed(self) && canBreed(partner);
    }

    public static long horseRetryTicks() {
        return HORSE_RETRY_TICKS;
    }

    public static boolean isHorseMountBlocked(AbstractHorse horse, long now) {
        return !horse.isTamed() && horse.getPersistentData().getLong(HORSE_RETRY).orElse(0L) > now;
    }

    public static void markHorseDismount(AbstractHorse horse, long now) {
        if (!horse.isTamed()) {
            horse.getPersistentData().putLong(HORSE_RETRY, now + HORSE_RETRY_TICKS);
        }
    }

    /** MITE wild horses refuse every further feed for 4000 ticks after accepting one. */
    public static boolean isHorseFeedBlocked(AbstractHorse horse, long now) {
        return !horse.isTamed() && horse.getPersistentData().getLong(HORSE_FEED_RETRY).orElse(0L) > now;
    }

    public static void markHorseFed(AbstractHorse horse, long now) {
        if (!horse.isTamed()) {
            horse.getPersistentData().putLong(HORSE_FEED_RETRY, now + HORSE_FEED_RETRY_TICKS);
        }
    }

    /** Advance one MITE wellness cycle. This is intentionally not called by navigation goals. */
    public static Wellness update(ServerLevel level, Animal animal) {
        ensureWellness(animal);
        consumeNearbyFood(level, animal);

        float food = adjustNeed(food(animal), isNearFoodSource(level, animal));
        boolean nearWater = isNearWaterSource(level, animal);
        float water = nearWater
                ? Mth.clamp(water(animal) + WELLNESS_BENEFIT, 0.0F, 1.0F)
                : level.isRainingAt(animal.blockPosition().above())
                        ? Mth.clamp(water(animal) + RAIN_WATER_BENEFIT, 0.0F, 1.0F)
                        : adjustNeed(water(animal), false);
        boolean crowded = isCrowded(level, animal);
        float freedom = adjustNeed(freedom(animal), !crowded);

        setWellness(animal, food, water, freedom);
        return new Wellness(food, water, freedom, crowded, isWell(food, water, freedom));
    }

    /** Returns the current MITE wellness values without advancing the 100-tick cycle. */
    private static Wellness snapshot(ServerLevel level, Animal animal) {
        ensureWellness(animal);
        float food = food(animal);
        float water = water(animal);
        float freedom = freedom(animal);
        return new Wellness(food, water, freedom, isCrowded(level, animal), isWell(food, water, freedom));
    }

    static float adjustNeed(float value, boolean satisfied) {
        return Mth.clamp(value + (satisfied ? WELLNESS_BENEFIT : WELLNESS_PENALTY), 0.0F, 1.0F);
    }

    public static boolean isProductive(Animal animal) {
        return hasSickSkin(animal) && isWell(animal);
    }

    public static boolean canBreed(Animal animal) {
        return isProductive(animal);
    }

    /** Newborn INFX livestock inherit the lowest wellness value from each parent. */
    public static void adoptWellnessFromParents(Animal child, Animal firstParent, Animal secondParent) {
        if (!hasSickSkin(child)) return;
        setWellness(
                child,
                Math.min(food(firstParent), food(secondParent)),
                Math.min(water(firstParent), water(secondParent)),
                Math.min(freedom(firstParent), freedom(secondParent)));
    }

    /** Panic starts on the affected INFX livestock and spreads without altering wellness values. */
    public static void panic(ServerLevel level, Animal source) {
        panic(level, source, source.blockPosition());
    }

    private static void panic(ServerLevel level, Animal source, BlockPos threatOrigin) {
        long now = level.getGameTime();
        long until = level.getGameTime() + 400L + source.getRandom().nextInt(400);
        markPanicked(source, until, threatOrigin, now);
        for (Animal nearby : level.getEntitiesOfClass(
                Animal.class,
                source.getBoundingBox().inflate(8.0, 4.0, 8.0),
                other -> other != source && other.isAlive() && hasSickSkin(other))) {
            markPanicked(nearby, until, source.blockPosition(), now);
        }
    }

    /** Returns whether this INFX livestock animal must keep fleeing at the supplied game time. */
    public static boolean isPanicked(Animal animal, long now) {
        return isPanicActive(animal.getPersistentData().getLong(PANIC_UNTIL).orElse(0L), now);
    }

    static boolean isPanicActive(long until, long now) {
        return until > now;
    }

    private static void markPanicked(Animal animal, long until, BlockPos origin, long now) {
        var data = animal.getPersistentData();
        data.putLong(PANIC_UNTIL, extendPanicUntil(data.getLong(PANIC_UNTIL).orElse(0L), until));
        // The newest scare gives the herd an escape direction even when an older panic lasts longer.
        data.putLong(PANIC_ORIGIN, origin.asLong());
        syncPanicMovementSpeed(animal, now);
    }

    static long extendPanicUntil(long currentUntil, long proposedUntil) {
        return Math.max(currentUntil, proposedUntil);
    }

    static double panicMovementSpeed(double normalMovementSpeed) {
        return normalMovementSpeed * PANIC_MOVEMENT_SPEED_MULTIPLIER;
    }

    /** Whether the temporary panic movement-speed modifier is currently active. */
    public static boolean hasPanicMovementSpeedBoost(Animal animal) {
        AttributeInstance movementSpeed = animal.getAttribute(Attributes.MOVEMENT_SPEED);
        return movementSpeed != null && movementSpeed.hasModifier(PANIC_MOVEMENT_SPEED_ID);
    }

    private static void syncPanicMovementSpeed(Animal animal, long now) {
        AttributeInstance movementSpeed = animal.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) {
            return;
        }
        if (isPanicked(animal, now)) {
            if (!hasPanicMovementSpeedBoost(animal)) {
                movementSpeed.addTransientModifier(PANIC_MOVEMENT_SPEED);
            }
        } else {
            movementSpeed.removeModifier(PANIC_MOVEMENT_SPEED_ID);
        }
    }

    private static @Nullable Vec3 panicOrigin(Animal animal) {
        long packedOrigin = animal.getPersistentData().getLong(PANIC_ORIGIN).orElse(Long.MIN_VALUE);
        return packedOrigin == Long.MIN_VALUE ? null : Vec3.atCenterOf(BlockPos.of(packedOrigin));
    }

    /** MITE feeding restores half of the food meter. */
    public static void markFed(Animal animal, long ignoredNow) {
        ensureWellness(animal);
        setWellness(animal, food(animal) + 0.5F, water(animal), freedom(animal));
    }

    /** MITE buckets have standard volume four and therefore refill the whole water meter. */
    public static void markWatered(Animal animal, long ignoredNow) {
        ensureWellness(animal);
        setWellness(animal, food(animal), water(animal) + 1.0F, freedom(animal));
    }

    /** Read-only thirst check for water-vessel interaction. */
    public static boolean isThirsty(Animal animal, long ignoredNow) {
        return water(animal) < NEEDY_THRESHOLD;
    }

    /** MITE suppresses manure only when the food meter is below 0.05. */
    public static boolean isDesperateForFood(Animal animal) {
        return isDesperateForFood(food(animal));
    }

    static boolean isDesperateForFood(float food) {
        return food < DESPERATE_THRESHOLD;
    }

    static int searchRange(float wellness) {
        if (wellness < DESPERATE_THRESHOLD) return DESPERATE_SEARCH_RANGE;
        if (wellness < WELL_THRESHOLD) return VERY_NEEDY_SEARCH_RANGE;
        return BASE_SEARCH_RANGE;
    }

    private static void ensureWellness(Animal animal) {
        var data = animal.getPersistentData();
        if (data.getBooleanOr(WELLNESS_INITIALIZED, false)) return;
        data.putFloat(FOOD, initialWellness(animal));
        data.putFloat(WATER, initialWellness(animal));
        data.putFloat(FREEDOM, initialWellness(animal));
        data.putBoolean(WELLNESS_INITIALIZED, true);
        // Old saves can retain these unused extension tags; they must no longer influence livestock.
        data.remove("infx_livestock_diseased");
        data.remove("infx_livestock_healthy");
        data.remove("infx_livestock_last_food");
        data.remove("infx_livestock_last_water");
        data.remove("infx_livestock_open_space");
        data.remove("infx_livestock_natural_light");
        data.remove("infx_livestock_sheltered");
        data.remove("infx_livestock_safe");
        syncWellFlag(animal);
    }

    private static float initialWellness(Animal animal) {
        return INITIAL_WELLNESS_MIN + animal.getRandom().nextFloat() * (1.0F - INITIAL_WELLNESS_MIN);
    }

    private static float food(Animal animal) {
        return wellnessValue(animal, FOOD);
    }

    private static float water(Animal animal) {
        return wellnessValue(animal, WATER);
    }

    private static float freedom(Animal animal) {
        return wellnessValue(animal, FREEDOM);
    }

    private static float wellnessValue(Animal animal, String key) {
        return Mth.clamp(animal.getPersistentData().getFloatOr(key, 1.0F), 0.0F, 1.0F);
    }

    private static void setWellness(Animal animal, float food, float water, float freedom) {
        var data = animal.getPersistentData();
        data.putFloat(FOOD, Mth.clamp(food, 0.0F, 1.0F));
        data.putFloat(WATER, Mth.clamp(water, 0.0F, 1.0F));
        data.putFloat(FREEDOM, Mth.clamp(freedom, 0.0F, 1.0F));
        data.putBoolean(WELLNESS_INITIALIZED, true);
        syncWellFlag(animal);
    }

    private static void syncWellFlag(Animal animal) {
        setWell(animal, isWell(food(animal), water(animal), freedom(animal)));
    }

    private static boolean isCrowded(ServerLevel level, Animal animal) {
        return isCrowded(level, animal.blockPosition(), animal.getBoundingBox());
    }

    private static boolean isCrowded(ServerLevel level, BlockPos pos, AABB bounds) {
        return !isOutdoors(level, pos)
                || level.getEntitiesOfClass(
                                        LivingEntity.class,
                                        bounds.inflate(2.0, 0.5, 2.0),
                        LivingEntity::isAlive)
                                .size()
                        > 2;
    }

    private static boolean isOutdoors(ServerLevel level, BlockPos pos) {
        return level.canSeeSky(pos.above());
    }

    private static boolean isNearWaterSource(ServerLevel level, Animal animal) {
        BlockPos origin = animal.blockPosition();
        int height = Math.max(0, Mth.floor(animal.getBbHeight()));
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, height, 1))) {
            if (isWaterSource(level, pos)) return true;
        }
        return false;
    }

    private static boolean isWaterSource(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return level.getFluidState(pos).is(FluidTags.WATER)
                || state.is(Blocks.SNOW)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.WATER_CAULDRON);
    }

    private static boolean isNearFoodSource(ServerLevel level, Animal animal) {
        BlockPos origin = animal.blockPosition();
        int height = Math.max(0, Mth.floor(animal.getBbHeight()));
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, height, 1))) {
            if (isFoodSource(animal, level.getBlockState(pos))) return true;
        }
        return false;
    }

    private static boolean isFoodSource(Animal animal, BlockState state) {
        if (animal instanceof AbstractCow) {
            return state.is(Blocks.SHORT_GRASS)
                    || state.is(Blocks.TALL_GRASS)
                    || state.is(Blocks.DANDELION);
        }
        if (animal instanceof Pig) {
            return state.is(Blocks.BROWN_MUSHROOM)
                    || state.is(Blocks.GRASS_BLOCK)
                    || state.is(Blocks.SHORT_GRASS)
                    || state.is(Blocks.TALL_GRASS);
        }
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.TALL_GRASS);
    }

    /**
     * Consumes part of a daily milk quota, keyed by overworld day. Callers decide whether the
     * animal must be productive; vanilla goats have no INFX wellness skin and are always milkable.
     */
    public static boolean takeMilk(
            Animal animal, ServerLevel level, int units, String dayKey, String unitsKey, int maxUnitsPerDay) {
        long day = level.getOverworldClockTime() / 24_000L;
        var data = animal.getPersistentData();
        if (data.getLong(dayKey).orElse(Long.MIN_VALUE) != day) {
            data.putLong(dayKey, day);
            data.putInt(unitsKey, 0);
        }
        int used = data.getInt(unitsKey).orElse(0);
        if (used + units > maxUnitsPerDay) return false;
        data.putInt(unitsKey, used + units);
        return true;
    }

    private static void consumeNearbyFood(ServerLevel level, Animal animal) {
        ItemEntity food = level.getEntitiesOfClass(
                        ItemEntity.class,
                        animal.getBoundingBox().inflate(1.4),
                        item -> item.isAlive() && animal.isFood(item.getItem()))
                .stream()
                .min(Comparator.comparingDouble(animal::distanceToSqr))
                .orElse(null);
        if (food == null) return;
        food.getItem().shrink(1);
        if (food.getItem().isEmpty()) food.discard();
        markFed(animal, 0L);
    }

    public record Wellness(float food, float water, float freedom, boolean crowded, boolean well) {}

    /** Flee while the livestock panic marker is active, away from the animal that raised it. */
    public static final class LivestockPanicGoal extends Goal {
        private final Animal animal;
        private @Nullable Path path;

        public LivestockPanicGoal(Animal animal) {
            this.animal = animal;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!(animal.level() instanceof ServerLevel level)
                    || !isPanicked(animal, level.getGameTime())) {
                return false;
            }
            path = findEscapePath();
            return NeedsGoal.isUsefulPath(path, animal.blockPosition());
        }

        @Override
        public void start() {
            if (path != null) {
                animal.getNavigation().moveTo(path, PANIC_ESCAPE_SPEED);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return animal.level() instanceof ServerLevel level
                    && isPanicked(animal, level.getGameTime())
                    && !animal.getNavigation().isDone();
        }

        @Override
        public void stop() {
            animal.getNavigation().stop();
            path = null;
        }

        private @Nullable Path findEscapePath() {
            Vec3 origin = panicOrigin(animal);
            if (origin != null) {
                Path directed = findDirectedEscapePath(origin);
                if (directed != null) {
                    return directed;
                }
            }
            for (int attempt = 0; attempt < PANIC_ESCAPE_ATTEMPTS; attempt++) {
                Vec3 destination = origin == null
                        ? DefaultRandomPos.getPos(
                                animal, PANIC_ESCAPE_HORIZONTAL_RANGE, PANIC_ESCAPE_VERTICAL_RANGE)
                        : DefaultRandomPos.getPosAway(
                                animal,
                                PANIC_ESCAPE_HORIZONTAL_RANGE,
                                PANIC_ESCAPE_VERTICAL_RANGE,
                                origin);
                if (destination == null) continue;
                Path candidate = animal.getNavigation().createPath(BlockPos.containing(destination), 0);
                if (isUsefulEscapePath(candidate, origin)) {
                    return candidate;
                }
            }
            return null;
        }

        private @Nullable Path findDirectedEscapePath(Vec3 origin) {
            double awayX = animal.getX() - origin.x;
            double awayZ = animal.getZ() - origin.z;
            if (awayX * awayX + awayZ * awayZ < 1.0E-4) {
                return null;
            }
            int forwardX = (int) Math.signum(awayX);
            int forwardZ = (int) Math.signum(awayZ);
            BlockPos start = animal.blockPosition();
            for (int distance = PANIC_ESCAPE_HORIZONTAL_RANGE; distance >= 1; distance--) {
                BlockPos destination = start.offset(forwardX * distance, 0, forwardZ * distance);
                Path candidate = animal.getNavigation().createPath(destination, 0);
                if (isUsefulEscapePath(candidate, origin)) {
                    return candidate;
                }
            }
            return null;
        }

        private boolean isUsefulEscapePath(@Nullable Path candidate, @Nullable Vec3 origin) {
            if (!NeedsGoal.isUsefulPath(candidate, animal.blockPosition())) {
                return false;
            }
            if (origin == null) {
                return true;
            }
            BlockPos endpoint = candidate.getEndNode().asBlockPos();
            double currentDistance = horizontalDistanceToSqr(animal.position(), origin);
            return horizontalDistanceToSqr(Vec3.atCenterOf(endpoint), origin) > currentDistance;
        }

        private static double horizontalDistanceToSqr(Vec3 first, Vec3 second) {
            double x = first.x - second.x;
            double z = first.z - second.z;
            return x * x + z * z;
        }
    }

    public static final class NeedsGoal extends Goal {
        private final Animal animal;
        private @Nullable BlockPos target;
        private @Nullable ItemEntity food;
        private @Nullable Path path;
        private int nextSearch;

        public NeedsGoal(Animal animal) {
            this.animal = animal;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        public @Nullable BlockPos selectedTarget() {
            return target;
        }

        @Override
        public boolean canUse() {
            if (!(animal.level() instanceof ServerLevel level) || animal.tickCount < nextSearch) return false;
            nextSearch = animal.tickCount + 40;
            Wellness wellness = snapshot(level, animal);
            food = null;
            target = null;
            path = null;
            if (wellness.food() < NEEDY_THRESHOLD) {
                target = findFood(level, searchRange(wellness.food()));
            }
            if (target == null && wellness.water() < NEEDY_THRESHOLD) {
                target = findWater(level, searchRange(wellness.water()));
            }
            if (target == null && wellness.freedom() <= NEEDY_THRESHOLD && wellness.crowded()) {
                target = findOpenPosition(level);
            }
            if (target != null && path == null) {
                path = animal.getNavigation().createPath(target, 1);
            }
            if (!isUsefulPath(path, animal.blockPosition())) {
                target = null;
                food = null;
                return false;
            }
            return true;
        }

        @Override
        public void start() {
            animal.getNavigation().moveTo(path, 1.15);
        }

        @Override
        public boolean canContinueToUse() {
            return target != null && !animal.getNavigation().isDone();
        }

        @Override
        public void tick() {
            if (!(animal.level() instanceof ServerLevel level)) return;
            if (food != null && food.isAlive() && animal.distanceToSqr(food) <= 2.25) {
                consumeNearbyFood(level, animal);
            }
        }

        @Override
        public void stop() {
            target = null;
            food = null;
            path = null;
        }

        private @Nullable BlockPos findFood(ServerLevel level, int range) {
            BlockPos origin = animal.blockPosition();
            Map<BlockPos, ItemEntity> itemTargets = new LinkedHashMap<>();
            level.getEntitiesOfClass(
                            ItemEntity.class,
                            animal.getBoundingBox().inflate(range),
                            item -> item.isAlive() && animal.isFood(item.getItem()))
                    .stream()
                    .sorted(Comparator.comparingDouble(animal::distanceToSqr))
                    .forEach(item -> itemTargets.putIfAbsent(item.blockPosition(), item));

            Set<BlockPos> targets = new LinkedHashSet<>(itemTargets.keySet());
            int sources = 0;
            int verticalRange = Math.max(2, range / 8);
            int sourceLimit =
                    range == DESPERATE_SEARCH_RANGE ? 24 : range == VERY_NEEDY_SEARCH_RANGE ? 16 : 8;
            for (BlockPos pos : BlockPos.withinManhattan(origin, range, verticalRange, range)) {
                if (!level.hasChunkAt(pos) || !isFoodSource(animal, level.getBlockState(pos))) continue;
                addStandableApproaches(level, pos, targets);
                if (++sources >= sourceLimit) break;
            }

            path = preferredPath(createPaths(targets), origin);
            if (path == null) return null;
            target = path.getTarget();
            food = itemTargets.get(target);
            return target;
        }

        private @Nullable BlockPos findWater(ServerLevel level, int range) {
            BlockPos origin = animal.blockPosition();
            Set<BlockPos> approaches = new LinkedHashSet<>();
            int sources = 0;
            int verticalRange = Math.max(2, range / 8);
            int sourceLimit =
                    range == DESPERATE_SEARCH_RANGE ? 24 : range == VERY_NEEDY_SEARCH_RANGE ? 16 : 8;
            for (BlockPos pos : BlockPos.withinManhattan(origin, range, verticalRange, range)) {
                if (!level.hasChunkAt(pos) || !isWaterSource(level, pos)) continue;
                addStandableApproaches(level, pos, approaches);
                if (++sources >= sourceLimit) break;
            }
            path = preferredPath(createPaths(approaches), origin);
            return path == null ? null : path.getTarget();
        }

        private List<Path> createPaths(Set<BlockPos> targets) {
            return targets.stream()
                    .sorted(Comparator.comparingDouble(
                            target -> distanceToSqr(animal.blockPosition(), target)))
                    .map(target -> animal.getNavigation().createPath(target, 0))
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }

        private static void addStandableApproaches(
                ServerLevel level, BlockPos source, Set<BlockPos> approaches) {
            List<BlockPos> candidates = new ArrayList<>();
            candidates.add(source);
            candidates.add(source.above());
            for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                candidates.add(source.relative(direction));
                candidates.add(source.above().relative(direction));
            }
            for (BlockPos approach : candidates) {
                if (canStandAt(level, approach)) {
                    approaches.add(approach.immutable());
                }
            }
        }

        private static boolean canStandAt(ServerLevel level, BlockPos pos) {
            return level.getFluidState(pos).isEmpty()
                    && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                    && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
                    && level.getBlockState(pos.below())
                            .isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP);
        }

        static @Nullable Path preferredPath(List<Path> candidates, BlockPos origin) {
            Path bestPartial = null;
            double bestRemainingFraction = Double.POSITIVE_INFINITY;
            for (Path candidate : candidates) {
                if (!hasNavigableNodes(candidate)) continue;
                if (candidate.canReach()) return candidate;
                if (!isUsefulPath(candidate, origin)) continue;
                double startDistance = distanceToSqr(origin, candidate.getTarget());
                double endDistance = distanceToSqr(candidate.getEndNode().asBlockPos(), candidate.getTarget());
                double remainingFraction = endDistance / Math.max(1.0, startDistance);
                if (remainingFraction < bestRemainingFraction) {
                    bestPartial = candidate;
                    bestRemainingFraction = remainingFraction;
                }
            }
            return bestPartial;
        }

        static boolean hasNavigableNodes(@Nullable Path path) {
            return path != null && path.getNodeCount() > 0 && path.getEndNode() != null;
        }

        static boolean isUsefulPath(@Nullable Path path, BlockPos origin) {
            if (!hasNavigableNodes(path)) return false;
            if (path.canReach()) return true;
            return distanceToSqr(path.getEndNode().asBlockPos(), path.getTarget())
                    < distanceToSqr(origin, path.getTarget());
        }

        private static double distanceToSqr(BlockPos first, BlockPos second) {
            double dx = first.getX() - second.getX();
            double dy = first.getY() - second.getY();
            double dz = first.getZ() - second.getZ();
            return dx * dx + dy * dy + dz * dz;
        }

        private @Nullable BlockPos findOpenPosition(ServerLevel level) {
            BlockPos origin = animal.blockPosition();
            for (int radius = 2; radius <= 16; radius += 2) {
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos pos = origin.offset(x, 0, z);
                        if (!canStandAt(level, pos)
                                || isCrowded(level, pos, new AABB(pos))) {
                            continue;
                        }
                        return pos;
                    }
                }
            }
            return null;
        }
    }
}
