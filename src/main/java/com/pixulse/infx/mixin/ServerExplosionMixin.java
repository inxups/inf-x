package com.pixulse.infx.mixin;

import com.pixulse.infx.entity.R196ExplosionRanges;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Keeps R196 entity blast reach independent from block power. The public explosion API exposes
 * one radius for both, so the entity query/distance local is the smallest viable injection point.
 */
@Mixin(ServerExplosion.class)
public abstract class ServerExplosionMixin {
    @ModifyVariable(
            method = "hurtEntities(Ljava/util/List;)V",
            at = @At(value = "STORE"),
            ordinal = 0)
    private float infx$r196EntityRadius(float vanillaDoubleRadius) {
        return (float) R196ExplosionRanges.entityRadius((Explosion) (Object) this)
                .orElse(vanillaDoubleRadius);
    }
}
