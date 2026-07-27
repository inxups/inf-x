package com.pixulse.infx.entity;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/** MITE's expanded infernal-creeper swell windows, adapted to the modern goal API. */
final class InfernalCreeperSwellGoal extends Goal {
    private final R196Creeper creeper;
    private @Nullable LivingEntity target;

    InfernalCreeperSwellGoal(R196Creeper creeper) {
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
                && !candidate.isDeadOrDying()
                && creeper.distanceToSqr(candidate)
                        < R196Creeper.infernalSwellStartDistanceSqr(
                                creeper.getNavigation().isDone(), creeper.healthFraction());
    }

    @Override
    public void start() {
        creeper.getNavigation().stop();
        target = creeper.getTarget();
    }

    @Override
    public void stop() {
        target = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (creeper.hasCactusFuseTrigger()) {
            creeper.setSwellDir(1);
        } else if (target == null || target.isDeadOrDying()) {
            creeper.setSwellDir(-1);
        } else if (hasVisiblePlayerInRange()) {
            creeper.setSwellDir(1);
        } else {
            creeper.setSwellDir(-1);
        }
    }

    private boolean hasVisiblePlayerInRange() {
        double distanceSqr = R196Creeper.infernalSwellContinueDistanceSqr(creeper.healthFraction());
        for (Player player : creeper.level().players()) {
            if (!player.isAlive()
                    || player.isSpectator()
                    || creeper.distanceToSqr(player) > distanceSqr
                    || !creeper.getSensing().hasLineOfSight(player)) {
                continue;
            }
            return true;
        }
        return false;
    }
}
