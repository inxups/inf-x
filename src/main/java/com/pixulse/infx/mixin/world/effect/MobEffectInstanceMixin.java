package com.pixulse.infx.mixin.world.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Prevents a newly applied poison effect from damaging on its first server tick. */
@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceMixin {
    @Unique
    private boolean infx$poisonFirstTick = true;

    @Redirect(
            method = "tickServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/effect/MobEffect;shouldApplyEffectTickThisTick(II)Z"))
    private boolean infx$skipPoisonFirstTick(MobEffect effect, int tickCount, int amplification) {
        MobEffectInstance instance = (MobEffectInstance) (Object) this;
        if (this.infx$poisonFirstTick && instance.is(MobEffects.POISON)) {
            this.infx$poisonFirstTick = false;
            return false;
        }
        return effect.shouldApplyEffectTickThisTick(tickCount, amplification);
    }
}
