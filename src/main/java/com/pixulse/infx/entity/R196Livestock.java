package com.pixulse.infx.entity;

import com.pixulse.infx.world.R196MoonPhase;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Persistent R196 livestock needs and the goal used to satisfy them. */
public final class R196Livestock {
    static final String LAST_WATER = "infx_livestock_last_water";
    static final String LAST_FOOD = "infx_livestock_last_food";
    static final String PANIC_UNTIL = "infx_livestock_panic_until";
    static final String DISEASED = "infx_livestock_diseased";
    static final String HEALTHY = "infx_livestock_healthy";
    static final String OPEN_SPACE = "infx_livestock_open_space";
    static final String NATURAL_LIGHT = "infx_livestock_natural_light";
    static final String SHELTERED = "infx_livestock_sheltered";
    static final String SAFE = "infx_livestock_safe";
    static final String GOALS_ADDED = "infx_livestock_goals_added";

    static final long WATER_GRACE = 24_000L;
    static final long FOOD_GRACE = 48_000L;
    static final long PANIC_TICKS = 600L;
    static final int NEEDS_TICK_INTERVAL = 100;
    static final int BASE_SEARCH_RANGE = 16;
    static final int VERY_NEEDY_SEARCH_RANGE = 32;
    static final int DESPERATE_SEARCH_RANGE = 48;
    static final String HORSE_RETRY = "infx_horse_tame_retry";
    static final long HORSE_RETRY_TICKS = 4_000L;

    private R196Livestock() {}

    /**
     * Define a per-class isWell flag. Its accessor is registered while the concrete R196 entity
     * class initializes, before the entity's {@link SynchedEntityData.Builder} sizes its data array.
     * It must not use {@code Animal.class}, which collides with vanilla Cow/Pig/etc. variant data.
     */
    public static void defineWellData(
            SynchedEntityData.Builder entityData, EntityDataAccessor<Boolean> well) {
        entityData.define(well, true);
    }

    /** MITE isWell: healthy and not diseased (client-synced). Vanilla livestock default well. */
    public static boolean isWell(Animal animal) {
        EntityDataAccessor<Boolean> well = wellData(animal);
        return well == null || animal.getEntityData().get(well);
    }

    public static void setWell(Animal animal, boolean value) {
        EntityDataAccessor<Boolean> well = wellData(animal);
        if (well == null) return;
        // Force dirty so clients always resync when disease is toggled by command.
        animal.getEntityData().set(well, value, true);
    }

    private static @Nullable EntityDataAccessor<Boolean> wellData(Animal animal) {
        if (animal instanceof R196Cow) return R196Cow.dataWell();
        if (animal instanceof R196Chicken) return R196Chicken.dataWell();
        if (animal instanceof R196Pig) return R196Pig.dataWell();
        if (animal instanceof R196Sheep) return R196Sheep.dataWell();
        return null;
    }

    /** True for cow/pig/sheep/chicken classes (including vanilla). */
    public static boolean isLivestock(Entity entity) {
        return entity instanceof AbstractCow
                || entity instanceof Pig
                || entity instanceof Sheep
                || entity instanceof Chicken;
    }

    /** R196 livestock that carry client-synced isWell and sick skins. */
    public static boolean hasSickSkin(Entity entity) {
        return entity instanceof R196Cow
                || entity instanceof R196Chicken
                || entity instanceof R196Pig
                || entity instanceof R196Sheep;
    }

    /**
     * Install needs/seek and flee goals once on R196 livestock (cow/chicken/sheep/pig).
     * Horses are not livestock and must not call this.
     */
    public static void ensureGoals(Animal animal) {
        if (animal instanceof AbstractHorse) {
            return;
        }
        if (animal.getPersistentData().getBooleanOr(GOALS_ADDED, false)) return;
        animal.goalSelector.addGoal(2, new NeedsGoal(animal));
        animal.goalSelector.addGoal(1, new AvoidEntityGoal<>(
                animal,
                Mob.class,
                mob -> mob instanceof Enemy,
                10.0F,
                1.15,
                1.4,
                entity -> entity.isAlive()));
        animal.getPersistentData().putBoolean(GOALS_ADDED, true);
    }

    /** Server tick hook: refresh needs and advance disease at one fixed cadence. */
    public static void serverTick(Animal animal) {
        if (!(animal.level() instanceof ServerLevel level)) return;
        if (animal.tickCount % NEEDS_TICK_INTERVAL == 0) update(level, animal, true);
    }

    /** Panic nearby animals when this one is hurt. */
    public static void onHurt(Animal animal, float inflictedDamage) {
        if (inflictedDamage <= 0.0F || !(animal.level() instanceof ServerLevel level)) return;
        panic(level, animal);
    }

    /** Mark fed only after vanilla confirms that a food interaction actually succeeded. */
    public static void markFedAfterInteraction(
            Animal animal, boolean offeredFood, InteractionResult result) {
        if (!(animal.level() instanceof ServerLevel level)
                || !foodInteractionSucceeded(offeredFood, result)) {
            return;
        }
        markFed(animal, level.getGameTime());
    }

    static boolean foodInteractionSucceeded(boolean offeredFood, InteractionResult result) {
        return offeredFood
                && result instanceof InteractionResult.Success success
                && success.wasItemInteraction()
                && success.swingSource() != InteractionResult.SwingSource.NONE;
    }

    public static boolean canMateWith(ServerLevel level, Animal self, Animal partner) {
        return canBreed(level, self) && canBreed(level, partner);
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

    public static Needs update(ServerLevel level, Animal animal) {
        return update(level, animal, false);
    }

    private static Needs update(ServerLevel level, Animal animal, boolean advanceDisease) {
        long now = level.getGameTime();
        var data = animal.getPersistentData();
        if (data.getLong(LAST_WATER).orElse(0L) == 0L) data.putLong(LAST_WATER, now);
        if (data.getLong(LAST_FOOD).orElse(0L) == 0L) data.putLong(LAST_FOOD, now);

        if (isNearWaterSource(level, animal)) data.putLong(LAST_WATER, now);
        if (isNearFoodSource(level, animal)) markFed(animal, now);
        consumeNearbyFood(level, animal, now);

        boolean watered = now - data.getLong(LAST_WATER).orElse(now) <= WATER_GRACE;
        boolean fed = now - data.getLong(LAST_FOOD).orElse(now) <= FOOD_GRACE;
        boolean open = hasOpenSpace(level, animal);
        boolean naturalLight = level.getBrightness(LightLayer.SKY, animal.blockPosition().above()) >= 8;
        boolean sheltered = !level.isRainingAt(animal.blockPosition().above())
                || !level.canSeeSky(animal.blockPosition().above());
        boolean safe = !animal.isOnFire()
                && !animal.isInWater()
                && level.getEntitiesOfClass(
                                Entity.class,
                                animal.getBoundingBox().inflate(10.0),
                                entity -> entity instanceof Enemy && entity.isAlive())
                        .isEmpty();
        boolean panicked = data.getLong(PANIC_UNTIL).orElse(0L) > now;
        boolean diseased = data.getBooleanOr(DISEASED, false);

        if (advanceDisease) {
            if (!diseased && isExposedToDisease(level, animal)) {
                diseased = true;
                data.putBoolean(DISEASED, true);
            } else if (!diseased && (!watered || !fed || !open || !safe)) {
                int chance = R196MoonPhase.at(level) == R196MoonPhase.BLOOD ? 2_000 : 8_000;
                if (animal.getRandom().nextInt(chance) == 0) {
                    diseased = true;
                    data.putBoolean(DISEASED, true);
                }
            } else if (diseased && watered && fed && open && naturalLight && safe && sheltered) {
                int chance = R196MoonPhase.at(level) == R196MoonPhase.BLUE ? 4 : 1_200;
                if (animal.getRandom().nextInt(chance) == 0) {
                    diseased = false;
                    data.putBoolean(DISEASED, false);
                }
            }
        }

        boolean healthy = healthy(watered, fed, open, naturalLight, sheltered, safe, panicked, diseased);
        data.putBoolean(OPEN_SPACE, open);
        data.putBoolean(NATURAL_LIGHT, naturalLight);
        data.putBoolean(SHELTERED, sheltered);
        data.putBoolean(SAFE, safe);
        data.putBoolean(HEALTHY, healthy);
        // MITE skins use !isWell(); keep client in sync with productive health.
        setWell(animal, healthy && !diseased);
        return new Needs(watered, fed, open, naturalLight, sheltered, safe, panicked, diseased, healthy);
    }

    public static boolean healthy(
            boolean watered,
            boolean fed,
            boolean open,
            boolean naturalLight,
            boolean sheltered,
            boolean safe,
            boolean panicked,
            boolean diseased) {
        return watered && fed && open && naturalLight && sheltered && safe && !panicked && !diseased;
    }

    public static boolean isProductive(Animal animal) {
        return animal.getPersistentData().getBooleanOr(HEALTHY, false)
                && !animal.getPersistentData().getBooleanOr(DISEASED, false);
    }

    public static boolean isDiseased(Animal animal) {
        return animal.getPersistentData().getBooleanOr(DISEASED, false);
    }

    /**
     * Force or clear disease on R196 livestock (cow/chicken/sheep/pig).
     * No-op for vanilla livestock (no isWell / sick renderer) and non-livestock.
     * Sets well/healthy flags immediately so production and sick skins update without waiting for tick.
     */
    public static boolean setDiseased(Animal animal, boolean diseased) {
        if (!hasSickSkin(animal)) {
            return false;
        }
        animal.getPersistentData().putBoolean(DISEASED, diseased);
        if (diseased) {
            animal.getPersistentData().putBoolean(HEALTHY, false);
            setWell(animal, false);
        } else if (animal.level() instanceof ServerLevel level) {
            update(level, animal);
        }
        return true;
    }

    public static boolean canBreed(ServerLevel level, Animal animal) {
        R196MoonPhase moon = R196MoonPhase.at(level);
        return isProductive(animal) && moon != R196MoonPhase.BLOOD && moon != R196MoonPhase.NEW;
    }

    public static void panic(ServerLevel level, Animal source) {
        long until = level.getGameTime() + PANIC_TICKS;
        for (Animal nearby : level.getEntitiesOfClass(
                Animal.class,
                source.getBoundingBox().inflate(12.0),
                other -> other.isAlive() && !(other instanceof AbstractHorse))) {
            nearby.getPersistentData().putLong(PANIC_UNTIL, until);
            nearby.getPersistentData().putBoolean(HEALTHY, false);
            if (isLivestock(nearby)) {
                setWell(nearby, false);
            }
        }
    }

    public static void markFed(Animal animal, long now) {
        animal.getPersistentData().putLong(LAST_FOOD, now);
    }

    public static void markWatered(Animal animal, long now) {
        animal.getPersistentData().putLong(LAST_WATER, now);
    }

    /**
     * Read-only thirst check for water-bucket interaction. Unlike {@link #update} this advances no
     * state, so it is safe to call before deciding whether an offered vessel should be spent.
     */
    public static boolean isThirsty(Animal animal, long now) {
        long lastWater = animal.getPersistentData().getLong(LAST_WATER).orElse(0L);
        return lastWater != 0L && now - lastWater > WATER_GRACE;
    }

    private static boolean hasOpenSpace(ServerLevel level, Animal animal) {
        int sameKind = level.getEntitiesOfClass(
                        Animal.class,
                        animal.getBoundingBox().inflate(6.0, 3.0, 6.0),
                        other -> other.isAlive() && other.getType() == animal.getType())
                .size();
        if (sameKind > 6) return false;
        BlockPos origin = animal.blockPosition();
        int open = 0;
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (level.getBlockState(origin.offset(x, 0, z)).isAir()
                        && level.getBlockState(origin.offset(x, 1, z)).isAir()) {
                    open++;
                }
            }
        }
        return open >= 12;
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
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.TALL_GRASS);
    }

    static int searchRange(long elapsed, long grace) {
        long overdue = Math.max(0L, elapsed - grace);
        if (overdue >= grace * 2L) return DESPERATE_SEARCH_RANGE;
        if (overdue >= grace) return VERY_NEEDY_SEARCH_RANGE;
        return BASE_SEARCH_RANGE;
    }

    private static void consumeNearbyFood(ServerLevel level, Animal animal, long now) {
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
        markFed(animal, now);
    }

    private static boolean isExposedToDisease(ServerLevel level, Animal animal) {
        return level.getEntitiesOfClass(
                        Animal.class,
                        animal.getBoundingBox().inflate(4.0),
                        other -> other != animal
                                && other.isAlive()
                                && other.getPersistentData().getBooleanOr(DISEASED, false))
                .stream()
                .anyMatch(other -> animal.getRandom().nextInt(16) == 0);
    }

    public record Needs(
            boolean watered,
            boolean fed,
            boolean open,
            boolean naturalLight,
            boolean sheltered,
            boolean safe,
            boolean panicked,
            boolean diseased,
            boolean healthy) {}

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
            Needs needs = update(level, animal);
            food = null;
            target = null;
            path = null;
            if (!needs.fed()) {
                target = findFood(level, searchRange(
                        level.getGameTime()
                                - animal.getPersistentData().getLong(LAST_FOOD).orElse(level.getGameTime()),
                        FOOD_GRACE));
            }
            if (target == null && !needs.watered()) {
                target = findWater(level, searchRange(
                        level.getGameTime()
                                - animal.getPersistentData().getLong(LAST_WATER).orElse(level.getGameTime()),
                        WATER_GRACE));
            }
            if (target == null
                    && (!needs.sheltered()
                            || !needs.open()
                            || !needs.naturalLight()
                            || !needs.safe()
                            || animal.isInWater()
                            || animal.isOnFire())) {
                target = findSafeOpenPosition(level, needs);
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
                consumeNearbyFood(level, animal, level.getGameTime());
            }
            if (target != null && animal.distanceToSqr(Vec3.atCenterOf(target)) <= 2.25) {
                update(level, animal);
            }
        }

        @Override
        public void stop() {
            if (animal.level() instanceof ServerLevel level) {
                update(level, animal);
            }
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

        private @Nullable BlockPos findSafeOpenPosition(ServerLevel level, Needs needs) {
            BlockPos origin = animal.blockPosition();
            boolean shelterWanted = !needs.sheltered() && level.isRainingAt(origin.above());
            boolean lightWanted = !needs.naturalLight() && !shelterWanted;
            for (boolean strict : new boolean[]{true, false}) {
                for (int radius = 2; radius <= 10; radius += 2) {
                    for (int x = -radius; x <= radius; x++) {
                        for (int z = -radius; z <= radius; z++) {
                            BlockPos pos = origin.offset(x, 0, z);
                            if (!level.getBlockState(pos).isAir()
                                    || !level.getBlockState(pos.above()).isAir()
                                    || !level.getFluidState(pos).isEmpty()
                                    || !level.getBlockState(pos.below()).isFaceSturdy(
                                            level, pos.below(), net.minecraft.core.Direction.UP)) {
                                continue;
                            }
                            if (strict && shelterWanted && level.canSeeSky(pos.above())) continue;
                            if (strict && lightWanted && level.getBrightness(LightLayer.SKY, pos.above()) < 8) continue;
                            if (strict && !level.getEntitiesOfClass(
                                            Entity.class,
                                            new AABB(pos).inflate(6.0),
                                            entity -> entity instanceof Enemy && entity.isAlive())
                                    .isEmpty()) {
                                continue;
                            }
                            return pos;
                        }
                    }
                }
            }
            return null;
        }
    }
}
