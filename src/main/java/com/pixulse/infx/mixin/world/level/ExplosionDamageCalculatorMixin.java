package com.pixulse.infx.mixin.world.level;

import com.pixulse.infx.entity.ExplosionRanges;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Uses the same INFX-only entity radius for damage falloff as the server query and knockback. */
@Mixin(ExplosionDamageCalculator.class)
public abstract class ExplosionDamageCalculatorMixin {
    @Inject(method = "getEntityDamageAmount", at = @At("HEAD"), cancellable = true)
    private void infx$r196EntityDamage(
            Explosion explosion, Entity entity, float exposure, CallbackInfoReturnable<Float> callback) {
        var radius = ExplosionRanges.entityRadius(explosion);
        if (radius.isPresent()) {
            callback.setReturnValue(ExplosionRanges.damageAmount(
                    explosion, entity, exposure, radius.getAsDouble()));
        }
    }
}
