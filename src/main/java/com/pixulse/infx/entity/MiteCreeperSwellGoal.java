package com.pixulse.infx.entity;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

/** R196 ordinary and infernal creeper ignition/maintenance windows. */
final class MiteCreeperSwellGoal extends Goal {
    private final R196Creeper creeper;

    MiteCreeperSwellGoal(R196Creeper creeper) {
        this.creeper = creeper;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (creeper.getSwellDir() > 0 || creeper.hasCactusFuseTrigger()) {
            return true;
        }
        LivingEntity candidate = creeper.getTarget();
        return candidate != null
                && candidate.isAlive()
                && creeper.distanceToSqr(candidate) < R196Creeper.swellStartDistanceSqr(
                        creeper.variant(), creeper.getNavigation().isDone(), creeper.healthFraction());
    }

    @Override
    public void start() {
        creeper.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        creeper.setSwellDir(creeper.hasCactusFuseTrigger() || hasVisiblePlayerInRange() ? 1 : -1);
    }

    private boolean hasVisiblePlayerInRange() {
        double distanceSqr = R196Creeper.swellContinueDistanceSqr(creeper.variant(), creeper.healthFraction());
        for (Player player : creeper.level().players()) {
            if (player.isAlive()
                    && !player.isSpectator()
                    && creeper.distanceToSqr(player) <= distanceSqr
                    && creeper.getSensing().hasLineOfSight(player)) {
                return true;
            }
        }
        return false;
    }
}
