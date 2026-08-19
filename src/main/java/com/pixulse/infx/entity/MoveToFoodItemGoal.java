package com.pixulse.infx.entity;

import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * MITE {@code EntityAIMoveToFoodItem}: a breedable {@link Animal} walks to a dropped
 * breeding-food item and eats it. The {@link Livestock#canEat(Animal)} gate keeps animals from
 * eating while in love or in breeding cooldown, and {@link Livestock#eatDroppedFood} makes a well
 * animal fall in love. Registered for every animal except horses (which keep their
 * interaction-gated feeding), so rabbits, turtles and other vanilla breedables eat ground food
 * the same way InfX livestock do.
 */
public final class MoveToFoodItemGoal extends Goal {
    private static final double SEARCH_RADIUS = 16.0;
    private static final double PICKUP_DISTANCE_SQR = 2.5 * 2.5;

    private final Animal animal;
    private ItemEntity food;

    public MoveToFoodItemGoal(Animal animal) {
        this.animal = animal;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!Livestock.canEat(animal) || animal.getRandom().nextInt(40) != 0) {
            return false;
        }
        food = nearestFood();
        return food != null
                && animal.getNavigation().createPath(food.blockPosition(), (int) SEARCH_RADIUS) != null;
    }

    @Override
    public boolean canContinueToUse() {
        return food != null && food.isAlive() && Livestock.canEat(animal);
    }

    @Override
    public void stop() {
        animal.getNavigation().stop();
        food = null;
    }

    @Override
    public void tick() {
        if (food == null || food.isRemoved()) {
            return;
        }
        if (animal.distanceToSqr(food) <= PICKUP_DISTANCE_SQR) {
            if (animal.level() instanceof ServerLevel serverLevel) {
                Livestock.eatDroppedFood(serverLevel, animal);
            }
            if (food.isRemoved() || food.getItem().isEmpty()) {
                food = null;
            }
            return;
        }
        animal.getNavigation().moveTo(food, 1.0);
    }

    private ItemEntity nearestFood() {
        Optional<ItemEntity> nearest = animal.level()
                .getEntitiesOfClass(
                        ItemEntity.class,
                        animal.getBoundingBox().inflate(SEARCH_RADIUS),
                        entity -> entity.isAlive() && animal.isFood(entity.getItem()))
                .stream()
                .min(java.util.Comparator.comparingDouble(animal::distanceToSqr));
        return nearest.orElse(null);
    }
}
