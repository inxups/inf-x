package com.pixulse.infx.mixin.world.entity.monster;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.pixulse.infx.entity.MonsterEvents;
import com.pixulse.infx.mixin.world.entity.ai.goal.DoorInteractGoalAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.monster.Enemy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** MITE frenzy: hostile mobs break doors twice as fast on blood-moon nights. */
@Mixin(BreakDoorGoal.class)
public abstract class BreakDoorGoalFrenzyMixin {
    @ModifyReturnValue(method = "getDoorBreakTime", at = @At("RETURN"))
    private int infx$frenzyDoorBreak(int ticks) {
        Mob mob = ((DoorInteractGoalAccessor) (Object) this).infx$getMob();
        return mob instanceof Enemy && MonsterEvents.isBloodMoonFrenzied(mob.level())
                ? ticks / 2
                : ticks;
    }
}
