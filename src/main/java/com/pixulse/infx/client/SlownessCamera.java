package com.pixulse.infx.client;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * MITE-aligned camera slow: the SLOWNESS effect reduces the mouse-look turn rate.
 *
 * MITE divides the per-frame turn multiplier by {@code 1 - slow * 15} (EntityRenderer
 * updateCameraAndRender) where {@code slow} is the combined speed-boost-vs-slowdown
 * {@code slowLevel * -0.2 + speedLevel * 0.2}, clamped to -0.8. That quarters the turn
 * rate at Slowness I, divides by 7 at II, by 10 at III and by 13 at IV+; the SPEED
 * effect cancels it level for level. InfX mirrors the exact division factor.
 */
public final class SlownessCamera {
    private static final float MAX_SLOW = -0.8F;

    private SlownessCamera() {}

    /** Levels are {@code amplifier + 1}; 0 means the effect is absent. */
    public static float turnFactor(int slownessLevel, int speedLevel) {
        float slow = slownessLevel * -0.2F + speedLevel * 0.2F;
        if (slow >= 0.0F) {
            return 1.0F;
        }
        if (slow < MAX_SLOW) {
            slow = MAX_SLOW;
        }
        return 1.0F / (1.0F - slow * 15.0F);
    }

    public static float slownessTurnFactor(LivingEntity entity) {
        return turnFactor(effectLevel(entity, MobEffects.SLOWNESS), effectLevel(entity, MobEffects.SPEED));
    }

    private static int effectLevel(LivingEntity entity, Holder<MobEffect> effect) {
        MobEffectInstance instance = entity.getEffect(effect);
        return instance == null ? 0 : instance.getAmplifier() + 1;
    }
}
