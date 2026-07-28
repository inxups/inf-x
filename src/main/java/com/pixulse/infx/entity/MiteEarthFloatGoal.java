package com.pixulse.infx.entity;

import java.util.EnumSet;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;

/** MITE earth elementals sink normally and only buoy themselves when blocked or hunting in deep lava. */
final class MiteEarthFloatGoal extends Goal {
    private final EarthElemental elemental;

    MiteEarthFloatGoal(EarthElemental elemental) {
        this.elemental = elemental;
        setFlags(EnumSet.of(Flag.JUMP));
        elemental.getNavigation().setCanFloat(true);
    }

    @Override
    public boolean canUse() {
        if (elemental.horizontalCollision) {
            return elemental.isInWater() || elemental.isInLava();
        }
        return elemental.isInLava()
                && elemental.getTarget() != null
                && elemental.getTarget().isAlive()
                && elemental.getFluidHeight(FluidTags.LAVA) > elemental.getFluidJumpThreshold();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (elemental.isEyeInFluid(FluidTags.WATER)) {
            elemental.setDeltaMovement(elemental.getDeltaMovement().with(Direction.Axis.Y, 0.1));
        } else if (elemental.getRandom().nextFloat() < 0.8F) {
            elemental.getJumpControl().jump();
        }
    }
}
