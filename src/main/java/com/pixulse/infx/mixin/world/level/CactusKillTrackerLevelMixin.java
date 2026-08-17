package com.pixulse.infx.mixin.world.level;

import com.pixulse.infx.world.CactusKillTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Clears the sand-backed count when the bottom cactus in a column is removed. */
@Mixin(Level.class)
abstract class CactusKillTrackerLevelMixin {
    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD"))
    private void infx$clearRemovedBottomCactus(
            BlockPos pos,
            BlockState replacement,
            int updateFlags,
            int updateLimit,
            CallbackInfoReturnable<Boolean> callback) {
        Level self = (Level) (Object) this;
        if (!(self instanceof ServerLevel level)) {
            return;
        }
        BlockState oldState = level.getBlockState(pos);
        if (oldState.is(Blocks.CACTUS)
                && !replacement.is(Blocks.CACTUS)
                && level.getBlockState(pos.below()).is(Blocks.SAND)) {
            CactusKillTracker.get(level).clear(pos.below());
        }
    }
}
