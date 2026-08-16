package com.pixulse.infx.entity;

import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

/**
 * MITE {@code EntityAIMoveToRepairItem}: a hurt skeleton walks to the nearest dropped bone
 * and consumes it to heal half of its maximum health. Gated by the skeleton's repair
 * cooldown, a 1-in-40 per-tick roll and the default pickup range.
 */
final class MoveToBoneRepairGoal extends Goal {
    private static final double SEARCH_RADIUS = 16.0;
    private static final double PICKUP_DISTANCE_SQR = 2.5 * 2.5;

    private final InfxSkeleton skeleton;
    private ItemEntity bone;

    MoveToBoneRepairGoal(InfxSkeleton skeleton) {
        this.skeleton = skeleton;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!skeleton.isAlive()
                || !skeleton.canPickUpLoot()
                || !skeleton.canRepairFromBone()
                || skeleton.getRandom().nextInt(40) != 0) {
            return false;
        }
        bone = nearestBone();
        return bone != null
                && skeleton.getNavigation().createPath(bone.blockPosition(), (int) SEARCH_RADIUS) != null;
    }

    @Override
    public boolean canContinueToUse() {
        return bone != null && !bone.isRemoved() && skeleton.canRepairFromBone();
    }

    @Override
    public void stop() {
        skeleton.getNavigation().stop();
        bone = null;
    }

    @Override
    public void tick() {
        if (bone == null || bone.isRemoved()) {
            return;
        }
        if (skeleton.distanceToSqr(bone) <= PICKUP_DISTANCE_SQR) {
            if (skeleton.tryRepairFromBone(bone.getItem()) && bone.getItem().isEmpty()) {
                bone.discard();
            }
            return;
        }
        skeleton.getNavigation().moveTo(bone, 1.0);
    }

    private ItemEntity nearestBone() {
        Optional<ItemEntity> nearest = skeleton.level()
                .getEntitiesOfClass(
                        ItemEntity.class,
                        skeleton.getBoundingBox().inflate(SEARCH_RADIUS),
                        entity -> entity.isAlive() && entity.getItem().is(Items.BONE))
                .stream()
                .min(java.util.Comparator.comparingDouble(skeleton::distanceToSqr));
        return nearest.orElse(null);
    }
}
