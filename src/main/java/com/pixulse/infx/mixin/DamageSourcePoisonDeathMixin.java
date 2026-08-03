package com.pixulse.infx.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Uses MITE's dedicated poison death message when a poisoned victim dies to magic damage. */
@Mixin(DamageSource.class)
public abstract class DamageSourcePoisonDeathMixin {
    @Inject(method = "getLocalizedDeathMessage", at = @At("HEAD"), cancellable = true)
    private void infx$poisonDeathMessage(LivingEntity victim, CallbackInfoReturnable<Component> cir) {
        DamageSource source = (DamageSource) (Object) this;
        if (source.is(DamageTypes.MAGIC) && victim.isDeadOrDying() && victim.hasEffect(MobEffects.POISON)) {
            cir.setReturnValue(Component.translatable("death.infx.poison", victim.getDisplayName()));
        }
    }
}
