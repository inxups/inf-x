package com.pixulse.infx.entity;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;

/** Skeleton bow goal whose INFX radius is a hard draw-and-release boundary. */
final class InfxHardCappedBowAttackGoal<T extends Mob & RangedAttackMob> extends Goal {
    private final T mob;
    private final double speedModifier;
    private final double attackRadiusSqr;
    private int attackInterval;
    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;
    private boolean targetWasOutOfRange;

    InfxHardCappedBowAttackGoal(T mob, double speedModifier, int attackInterval, float attackRadius) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.attackInterval = attackInterval;
        this.attackRadiusSqr = attackRadius * attackRadius;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    void setAttackInterval(int ticks) {
        attackInterval = ticks;
    }

    @Override
    public boolean canUse() {
        return mob.getTarget() != null && isHoldingBow();
    }

    @Override
    public boolean canContinueToUse() {
        return (canUse() || !mob.getNavigation().isDone()) && isHoldingBow();
    }

    private boolean isHoldingBow() {
        return mob.isHolding(stack -> stack.getItem() instanceof BowItem);
    }

    @Override
    public void start() {
        mob.setAggressive(true);
    }

    @Override
    public void stop() {
        mob.setAggressive(false);
        seeTime = 0;
        attackTime = -1;
        targetWasOutOfRange = false;
        mob.stopUsingItem();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) {
            return;
        }
        double distanceSqr = mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean inRange = distanceSqr <= attackRadiusSqr;
        boolean visible = mob.getSensing().hasLineOfSight(target);
        boolean wasVisible = seeTime > 0;
        if (visible != wasVisible) {
            seeTime = 0;
        }
        seeTime += visible ? 1 : -1;

        if (inRange && seeTime >= 20) {
            mob.getNavigation().stop();
            strafingTime++;
        } else {
            mob.getNavigation().moveTo(target, speedModifier);
            strafingTime = -1;
        }

        if (!inRange) {
            // Cancel a draw that crossed the INFX cutoff and never begin a new one outside it.
            targetWasOutOfRange = true;
            mob.stopUsingItem();
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            return;
        }
        if (targetWasOutOfRange) {
            // A hard cutoff cancels the prior firing cycle. Do not carry a stale bow cooldown
            // across the boundary once the target is again a valid INFX ranged target.
            attackTime = -1;
            targetWasOutOfRange = false;
        }

        if (strafingTime >= 20) {
            if (mob.getRandom().nextFloat() < 0.3F) {
                strafingClockwise = !strafingClockwise;
            }
            if (mob.getRandom().nextFloat() < 0.3F) {
                strafingBackwards = !strafingBackwards;
            }
            strafingTime = 0;
        }
        if (strafingTime > -1) {
            if (distanceSqr > attackRadiusSqr * 0.75F) {
                strafingBackwards = false;
            } else if (distanceSqr < attackRadiusSqr * 0.25F) {
                strafingBackwards = true;
            }
            mob.getMoveControl().strafe(strafingBackwards ? -0.5F : 0.5F, strafingClockwise ? 0.5F : -0.5F);
            if (mob.getControlledVehicle() instanceof Mob vehicle) {
                vehicle.lookAt(target, 30.0F, 30.0F);
            }
            mob.lookAt(target, 30.0F, 30.0F);
        } else {
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (mob.isUsingItem()) {
            if (!visible && seeTime < -60) {
                mob.stopUsingItem();
            } else if (visible && mob.getTicksUsingItem() >= 20) {
                int pullTime = mob.getTicksUsingItem();
                mob.stopUsingItem();
                mob.performRangedAttack(target, BowItem.getPowerForTime(pullTime));
                attackTime = attackInterval;
            }
        } else if (--attackTime <= 0 && seeTime >= -60) {
            mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(mob, item -> item instanceof BowItem));
        }
    }
}
