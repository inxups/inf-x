package com.pixulse.infx.mixin.server.level;

import com.pixulse.infx.world.MoonPhase;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * MITE: blood-moon days strike lightning five times as often (1/20000 vs 1/100000 per tick).
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelLightningRateMixin {
    @ModifyArg(
            method = "tickThunder",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"),
            index = 0)
    private int infx$bloodMoonLightningRate(int bound) {
        return MoonPhase.lightningRollBound((ServerLevel) (Object) this, bound);
    }
}
