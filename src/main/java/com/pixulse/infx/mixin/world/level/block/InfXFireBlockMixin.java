package com.pixulse.infx.mixin.world.level.block;

import com.pixulse.infx.world.InfXFireSpreadRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies InfX's fire rates and update algorithm to the vanilla FireBlock implementation. */
@Mixin(FireBlock.class)
public abstract class InfXFireBlockMixin {
    @Inject(method = "getIgniteOdds(Lnet/minecraft/world/level/block/state/BlockState;)I", at = @At("HEAD"), cancellable = true)
    private void infx$useFireSpreadSpeed(BlockState state, CallbackInfoReturnable<Integer> callback) {
        if (InfXFireSpreadRules.rate(state.getBlock()) != null) {
            callback.setReturnValue(InfXFireSpreadRules.fireSpreadSpeed(state));
        }
    }

    @Inject(method = "getBurnOdds(Lnet/minecraft/world/level/block/state/BlockState;)I", at = @At("HEAD"), cancellable = true)
    private void infx$useFlammability(BlockState state, CallbackInfoReturnable<Integer> callback) {
        if (InfXFireSpreadRules.rate(state.getBlock()) != null) {
            callback.setReturnValue(InfXFireSpreadRules.flammability(state));
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void infx$runFireUpdate(
            BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo callback) {
        InfXFireSpreadRules.tick((FireBlock) (Object) this, state, level, pos, random);
        callback.cancel();
    }
}
