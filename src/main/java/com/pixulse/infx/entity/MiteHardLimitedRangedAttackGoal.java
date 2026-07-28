package com.pixulse.infx.entity;

import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.jspecify.annotations.Nullable;

/** Modern ranged pursuit with R196's real firing cutoff instead of a stopping-distance hint. */
final class MiteHardLimitedRangedAttackGoal extends Goal {
    private final Mob mob;
    private final RangedAttackMob rangedMob;
    private final double speedModifier;
    private final int attackInterval;
    private final float attackRadius;
    private final double attackRadiusSqr;
    private @Nullable LivingEntity target;
    private int attackTime = -1;
    private int seeTime;

    MiteHardLimitedRangedAttackGoal(
            RangedAttackMob rangedMob, double speedModifier, int attackInterval, float attackRadius) {
        if (!(rangedMob instanceof Mob mob)) {
            throw new IllegalArgumentException("R196 ranged attacker must also be a Mob");
        }
        this.mob = mob;
        this.rangedMob = rangedMob;
        this.speedModifier = speedModifier;
        this.attackInterval = attackInterval;
        this.attackRadius = attackRadius;
        this.attackRadiusSqr = attackRadius * attackRadius;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity candidate = mob.getTarget();
        if (candidate == null || !candidate.isAlive()) {
            return false;
        }
        target = candidate;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() || target != null && target.isAlive() && !mob.getNavigation().isDone();
    }

    @Override
    public void stop() {
        target = null;
        seeTime = 0;
        attackTime = -1;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (target == null) {
            return;
        }
        double distanceSqr = mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean inRange = AttackRanges.withinHardRangedReach(distanceSqr, attackRadius);
        boolean visible = mob.getSensing().hasLineOfSight(target);
        seeTime = visible ? seeTime + 1 : 0;

        if (inRange && seeTime >= 5) {
            mob.getNavigation().stop();
        } else {
            mob.getNavigation().moveTo(target, speedModifier);
        }
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // RangedAttackGoal normally treats attackRadius only as a movement hint and can
        // still release from farther away. R196 may neither begin nor complete a shot there.
        if (!inRange) {
            return;
        }
        if (--attackTime == 0) {
            if (visible) {
                float power = Mth.clamp((float) Math.sqrt(distanceSqr) / attackRadius, 0.1F, 1.0F);
                rangedMob.performRangedAttack(target, power);
                attackTime = attackInterval;
            }
        } else if (attackTime < 0) {
            attackTime = attackInterval;
        }
    }
}
