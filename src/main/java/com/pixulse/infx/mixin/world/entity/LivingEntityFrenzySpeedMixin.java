package com.pixulse.infx.mixin.world.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.pixulse.infx.entity.InfxEnderman;
import com.pixulse.infx.world.SpawnGate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** MITE frenzy: hostile mobs move 1.2× faster on blood-moon nights; endermen are exempt. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityFrenzySpeedMixin {
    @ModifyReturnValue(method = "getSpeed", at = @At("RETURN"))
    private float infx$frenzySpeed(float speed) {
        LivingEntity self = (LivingEntity) (Object) this;
        return self instanceof Enemy && !(self instanceof InfxEnderman)
                        && SpawnGate.isBloodMoonFrenzied(self.level())
                ? speed * 1.2F
                : speed;
    }
}
