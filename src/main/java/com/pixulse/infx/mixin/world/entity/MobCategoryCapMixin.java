package com.pixulse.infx.mixin.world.entity;

import com.pixulse.infx.world.SpawnGate;
import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** MITE caps hostile mobs at 50 per player instead of vanilla's 70. See {@link SpawnGate#hostileCapCeiling}. */
@Mixin(MobCategory.class)
public abstract class MobCategoryCapMixin {
    @Inject(method = "getMaxInstancesPerChunk", at = @At("HEAD"), cancellable = true)
    private void infx$miteMonsterCap(CallbackInfoReturnable<Integer> cir) {
        int capped = SpawnGate.hostileCapCeiling((MobCategory) (Object) this, 70);
        if (capped != 70) {
            cir.setReturnValue(capped);
        }
    }
}
