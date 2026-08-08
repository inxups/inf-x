package com.pixulse.infx.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * InfX sweet berry bushes cannot be forced with bone meal; the modern vanilla bush allows it.
 * The INFX blueberry bush overrides the method itself, so this only covers vanilla bushes.
 */
@Mixin(SweetBerryBushBlock.class)
public abstract class SweetBerryBushBlockBonemealMixin {
    @Inject(method = "isValidBonemealTarget", at = @At("HEAD"), cancellable = true)
    private void infx$disableBonemeal(
            LevelReader level, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(false);
    }
}
