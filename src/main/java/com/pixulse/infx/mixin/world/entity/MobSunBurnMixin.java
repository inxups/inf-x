package com.pixulse.infx.mixin.world.entity;

import com.pixulse.infx.config.InfXConfig;
import com.pixulse.infx.world.MoonPhase;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MITE 14h/10h day window (adjusted sunrise 5000 / sunset 19000): hostile mobs may only burn in
 * sunlight during the MITE daytime (overworld ticks [23000, 13000)). Vanilla burns by environment
 * light alone; this restores the MITE hard day/night gate on top.
 */
@Mixin(Mob.class)
public abstract class MobSunBurnMixin {
    @Inject(method = "isSunBurnTick", at = @At("HEAD"), cancellable = true)
    private void infx$miteDayNightBurn(CallbackInfoReturnable<Boolean> cir) {
        if (!InfXConfig.INSTANCE.mobs.enabled.getValue()
                || !InfXConfig.INSTANCE.mobs.miteDayNight.getValue()) {
            return;
        }
        Level level = ((Mob) (Object) this).level();
        if (MoonPhase.isOverworld(level) && MoonPhase.isNight(level)) {
            cir.setReturnValue(false);
        }
    }
}
