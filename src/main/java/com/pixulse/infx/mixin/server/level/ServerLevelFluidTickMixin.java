package com.pixulse.infx.mixin.server.level;

import com.pixulse.infx.world.FluidDecayData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Dispatches persisted INFX bucket source decay through Minecraft's scheduled fluid ticks. */
@Mixin(ServerLevel.class)
public abstract class ServerLevelFluidTickMixin {
    // Scheduled fluid ticks expose no public per-tick callback. This injection only consumes a tick
    // when FluidDecayData owns the same position and its persisted deadline has elapsed.
    @Inject(method = "tickFluid", at = @At("HEAD"), cancellable = true)
    private void infx$applyBucketSourceDecay(BlockPos pos, Fluid fluid, CallbackInfo callback) {
        ServerLevel level = (ServerLevel) (Object) this;
        if (FluidDecayData.handleScheduledTick(level, pos, fluid)) {
            callback.cancel();
        }
    }
}
