package com.pixulse.infx.entity;

import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * MITE {@code EntityAIMoveToFoodItem}: a zombie walks to dropped raw meat and eats it. Zombies
 * are undead, so eating never heals — the meat is simply consumed on a 400-tick cooldown.
 */
public final class MoveToFoodGoal extends Goal {
    private static final double SEARCH_RADIUS = 16.0;
    private static final double PICKUP_DISTANCE_SQR = 2.5 * 2.5;
    private static final String COOLDOWN_KEY = "infx.food_cooldown";

    private final Zombie zombie;
    private ItemEntity food;

    MoveToFoodGoal(Zombie zombie) {
        this.zombie = zombie;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!zombie.isAlive()
                || zombie.tickCount < zombie.getPersistentData().getLong(COOLDOWN_KEY).orElse(0L)
                || zombie.getRandom().nextInt(40) != 0) {
            return false;
        }
        food = nearestFood();
        return food != null
                && zombie.getNavigation().createPath(food.blockPosition(), (int) SEARCH_RADIUS) != null;
    }

    @Override
    public boolean canContinueToUse() {
        return food != null
                && !food.isRemoved()
                && zombie.tickCount >= zombie.getPersistentData().getLong(COOLDOWN_KEY).orElse(0L);
    }

    @Override
    public void stop() {
        zombie.getNavigation().stop();
        food = null;
    }

    @Override
    public void tick() {
        if (food == null || food.isRemoved()) {
            return;
        }
        if (zombie.distanceToSqr(food) <= PICKUP_DISTANCE_SQR) {
            if (tryEatFood(zombie, food.getItem()) && food.getItem().isEmpty()) {
                food.discard();
            }
            return;
        }
        zombie.getNavigation().moveTo(food, 1.0);
    }

    /** MITE {@code onFoodEaten}: consumes one meat; undead zombies never heal from it. */
    public static boolean tryEatFood(Zombie zombie, ItemStack stack) {
        if (!stack.is(ItemTags.MEAT)
                || zombie.tickCount < zombie.getPersistentData().getLong(COOLDOWN_KEY).orElse(0L)) {
            return false;
        }
        stack.shrink(1);
        zombie.getPersistentData().putLong(COOLDOWN_KEY, zombie.tickCount + 400);
        zombie.playSound(
                SoundEvents.ITEM_PICKUP,
                0.2F,
                (zombie.getRandom().nextFloat() - zombie.getRandom().nextFloat()) * 0.7F + 1.0F);
        return true;
    }

    private ItemEntity nearestFood() {
        Optional<ItemEntity> nearest = zombie.level()
                .getEntitiesOfClass(
                        ItemEntity.class,
                        zombie.getBoundingBox().inflate(SEARCH_RADIUS),
                        entity -> entity.isAlive() && entity.getItem().is(ItemTags.MEAT))
                .stream()
                .min(java.util.Comparator.comparingDouble(zombie::distanceToSqr));
        return nearest.orElse(null);
    }
}
