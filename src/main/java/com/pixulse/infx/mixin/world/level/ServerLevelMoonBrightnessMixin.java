package com.pixulse.infx.mixin.world.level;

import com.pixulse.infx.world.MoonPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MITE {@code getMoonBrightness}: blood moon 0.6, harvest moon 1.0, blue moon 1.1,
 * otherwise phase factor × 0.5 + 0.75, feeding the overworld's regional difficulty.
 * Non-overworld dimensions keep the vanilla table.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMoonBrightnessMixin {
    @Inject(method = "getMoonBrightness", at = @At("HEAD"), cancellable = true)
    private void infx$miteMoonBrightness(BlockPos pos, CallbackInfoReturnable<Float> cir) {
        ServerLevel self = (ServerLevel) (Object) this;
        if (MoonPhase.isOverworld(self)) {
            cir.setReturnValue(MoonPhase.miteMoonBrightness(self.getOverworldClockTime()));
        }
    }
}
