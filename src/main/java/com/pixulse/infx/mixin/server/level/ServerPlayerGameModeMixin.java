package com.pixulse.infx.mixin.server.level;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.pixulse.infx.data.harvest.MiningProgressAccumulator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps server mining progress additive when hunger or another speed modifier changes. */
@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
    @Unique
    private static final String DESTROY_PROGRESS =
            "Lnet/minecraft/world/level/block/state/BlockState;getDestroyProgress("
                    + "Lnet/minecraft/world/entity/player/Player;"
                    + "Lnet/minecraft/world/level/BlockGetter;"
                    + "Lnet/minecraft/core/BlockPos;)F";

    @Shadow private int destroyProgressStart;
    @Shadow private int gameTicks;

    @Unique
    private final MiningProgressAccumulator infx$miningProgress = new MiningProgressAccumulator();

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void infx$resetCompletedMiningProgress(
            BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
        infx$miningProgress.reset();
    }

    @ModifyExpressionValue(
            method = "handleBlockBreakAction",
            at = @At(value = "INVOKE", target = DESTROY_PROGRESS, ordinal = 0))
    private float infx$startMiningProgress(float initialProgress) {
        infx$miningProgress.start(initialProgress);
        return initialProgress;
    }

    @ModifyExpressionValue(
            method = "incrementDestroyProgress",
            at = @At(value = "INVOKE", target = DESTROY_PROGRESS))
    private float infx$accumulateMiningProgress(
            float currentProgress, BlockState state, BlockPos pos, int destroyStartTick) {
        if (!infx$miningProgress.isActive()) {
            return currentProgress;
        }
        int elapsedTicks = gameTicks - destroyStartTick + 1;
        return infx$miningProgress.advance(currentProgress) / elapsedTicks;
    }

    @ModifyExpressionValue(
            method = "handleBlockBreakAction",
            at = @At(value = "INVOKE", target = DESTROY_PROGRESS, ordinal = 1))
    private float infx$finishWithAccumulatedMiningProgress(float currentProgress) {
        if (!infx$miningProgress.isActive()) {
            return currentProgress;
        }
        int elapsedTicks = gameTicks - destroyProgressStart + 1;
        return infx$miningProgress.progress() / elapsedTicks;
    }
}
