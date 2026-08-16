package com.pixulse.infx.mixin.world.entity.ai.goal;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.DoorInteractGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Bridges DoorInteractGoal's protected mob owner for the frenzy door-break mixin. */
@Mixin(DoorInteractGoal.class)
public interface DoorInteractGoalAccessor {
    @Accessor("mob")
    Mob infx$getMob();
}
