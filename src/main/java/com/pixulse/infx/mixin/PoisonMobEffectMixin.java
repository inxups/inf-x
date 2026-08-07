package com.pixulse.infx.mixin;

import net.minecraft.world.effect.PoisonMobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Restores InfX's poison interval of {@code 100 >> amplifier} ticks per damage tick.
 * Vanilla 26.1.2 uses {@code 25 >> amplifier}, which deals poison damage four times as often.
 */
@Mixin(PoisonMobEffect.class)
public abstract class PoisonMobEffectMixin {
    @Inject(method = "shouldApplyEffectTickThisTick", at = @At("HEAD"), cancellable = true)
    private void infx$applyPoisonInterval(int tickCount, int amplification, CallbackInfoReturnable<Boolean> cir) {
        int interval = Math.max(1, 100 >> amplification);
        cir.setReturnValue(tickCount % interval == 0);
    }
}
