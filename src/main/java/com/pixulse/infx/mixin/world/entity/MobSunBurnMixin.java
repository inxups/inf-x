package com.pixulse.infx.mixin.world.entity;

import com.pixulse.infx.world.SpawnGate;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MITE 14h/10h day window (adjusted sunrise 5000 / sunset 19000): hostile mobs may only burn in
 * sunlight during the MITE daytime (overworld ticks [23000, 13000)). Vanilla burns by environment
 * light alone; this restores the MITE hard day/night gate on top.
 * The decision lives in {@link SpawnGate#miteDayNightPreventsBurn}.
 */
@Mixin(Mob.class)
public abstract class MobSunBurnMixin {
    @Inject(method = "isSunBurnTick", at = @At("HEAD"), cancellable = true)
    private void infx$miteDayNightBurn(CallbackInfoReturnable<Boolean> cir) {
        if (SpawnGate.miteDayNightPreventsBurn(((Mob) (Object) this).level())) {
            cir.setReturnValue(false);
        }
    }
}
