package com.pixulse.infx.mixin;

import com.pixulse.infx.entity.SpawnerBurnout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The regular block-entity wrapper is the narrow hook with both the block and its spawner tick. */
@Mixin(SpawnerBlockEntity.class)
abstract class SpawnerBlockEntityMixin {
    @Inject(method = "clientTick", at = @At("HEAD"), cancellable = true)
    private static void infx$stopExhaustedSpawnerAnimation(
            Level level, BlockPos pos, BlockState state, SpawnerBlockEntity entity, CallbackInfo callback) {
        if (SpawnerBurnout.isExhausted(entity)) {
            callback.cancel();
        }
    }

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private static void infx$startSpawnerTick(
            Level level, BlockPos pos, BlockState state, SpawnerBlockEntity entity, CallbackInfo callback) {
        if (SpawnerBurnout.isExhausted(entity)) {
            callback.cancel();
            return;
        }
        SpawnerBurnout.registerSpawner(entity);
    }
}
