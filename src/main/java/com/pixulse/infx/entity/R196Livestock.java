package com.pixulse.infx.entity;

import com.pixulse.infx.world.R196MoonPhase;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.LightLayer;
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

    private R196Livestock() {}

    public static Needs update(ServerLevel level, Animal animal) {
        long now = level.getGameTime();
        var data = animal.getPersistentData();
        if (data.getLong(LAST_WATER).orElse(0L) == 0L) data.putLong(LAST_WATER, now);
        if (data.getLong(LAST_FOOD).orElse(0L) == 0L) data.putLong(LAST_FOOD, now);

        if (touchesWater(level, animal.blockPosition())) data.putLong(LAST_WATER, now);
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

        boolean healthy = healthy(watered, fed, open, naturalLight, sheltered, safe, panicked, diseased);
        data.putBoolean(OPEN_SPACE, open);
        data.putBoolean(NATURAL_LIGHT, naturalLight);
        data.putBoolean(SHELTERED, sheltered);
        data.putBoolean(SAFE, safe);
        data.putBoolean(HEALTHY, healthy);
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

    public static boolean canBreed(ServerLevel level, Animal animal) {
        R196MoonPhase moon = R196MoonPhase.at(level);
        return isProductive(animal) && moon != R196MoonPhase.BLOOD && moon != R196MoonPhase.NEW;
    }

    public static void panic(ServerLevel level, Animal source) {
        long until = level.getGameTime() + PANIC_TICKS;
        for (Animal nearby : level.getEntitiesOfClass(
                Animal.class, source.getBoundingBox().inflate(12.0), Animal::isAlive)) {
            nearby.getPersistentData().putLong(PANIC_UNTIL, until);
            nearby.getPersistentData().putBoolean(HEALTHY, false);
        }
    }

    static void markFed(Animal animal, long now) {
        animal.getPersistentData().putLong(LAST_FOOD, now);
    }

    static void markWatered(Animal animal, long now) {
        animal.getPersistentData().putLong(LAST_WATER, now);
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

    private static boolean touchesWater(ServerLevel level, BlockPos origin) {
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, 1, 1))) {
            if (level.getFluidState(pos).is(FluidTags.WATER)) return true;
        }
        return false;
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
                food = nearestFood(level);
                if (food != null) target = food.blockPosition();
            }
            if (target == null && !needs.watered()) target = findWater(level);
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
            if (path == null || !path.canReach()) {
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
            return target != null && !animal.getNavigation().isDone() && animal.distanceToSqr(Vec3.atCenterOf(target)) > 1.5;
        }

        @Override
        public void tick() {
            if (!(animal.level() instanceof ServerLevel level)) return;
            if (food != null && food.isAlive() && animal.distanceToSqr(food) <= 2.25) {
                consumeNearbyFood(level, animal, level.getGameTime());
            }
            if (target != null
                    && animal.distanceToSqr(Vec3.atCenterOf(target)) <= 2.25
                    && touchesWater(level, animal.blockPosition())) {
                markWatered(animal, level.getGameTime());
            }
        }

        private @Nullable ItemEntity nearestFood(ServerLevel level) {
            return level.getEntitiesOfClass(
                            ItemEntity.class,
                            animal.getBoundingBox().inflate(12.0),
                            item -> item.isAlive() && animal.isFood(item.getItem()))
                    .stream()
                    .min(Comparator.comparingDouble(animal::distanceToSqr))
                    .orElse(null);
        }

        private @Nullable BlockPos findWater(ServerLevel level) {
            BlockPos origin = animal.blockPosition();
            List<BlockPos> approaches = new ArrayList<>();
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-8, -2, -8), origin.offset(8, 2, 8))) {
                if (!level.getFluidState(pos).is(FluidTags.WATER)) continue;
                for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                    for (BlockPos approach : new BlockPos[]{
                        pos.relative(direction), pos.above().relative(direction)
                    }) {
                        if (!level.getBlockState(approach).isAir()
                                || !level.getBlockState(approach.above()).isAir()
                                || !level.getBlockState(approach.below()).isFaceSturdy(
                                        level, approach.below(), net.minecraft.core.Direction.UP)) {
                            continue;
                        }
                        approaches.add(approach.immutable());
                    }
                }
            }
            path = animal.getNavigation().createPath(approaches.stream(), 1);
            return path != null && path.canReach() ? path.getTarget() : null;
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
