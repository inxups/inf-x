package com.pixulse.infx.mixin.world.level.block;

import com.pixulse.infx.world.CactusKillTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Captures the exact cactus cell used by the otherwise positionless modern cactus damage source. */
@Mixin(CactusBlock.class)
abstract class CactusBlockKillTrackerMixin {
    @Inject(method = "entityInside", at = @At("HEAD"))
    private void infx$recordCactusContact(
            BlockState state,
            Level level,
            BlockPos pos,
            Entity entity,
            InsideBlockEffectApplier effectApplier,
            boolean isPrecise,
            CallbackInfo callback) {
        if (level instanceof ServerLevel serverLevel) {
            CactusKillTracker.recordContact(entity, pos, serverLevel.getGameTime());
        }
    }

    @Inject(method = "randomTick", at = @At("HEAD"))
    private void infx$decayTopCactusKills(
            BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo callback) {
        if (!level.getBlockState(pos.above()).is(Blocks.CACTUS) && random.nextBoolean()) {
            CactusKillTracker.get(level).decrementForCactus(level, pos);
        }
    }
}
