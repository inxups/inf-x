package com.pixulse.infx.mixin.world.level.block;

import com.pixulse.infx.block.InfxCropBlock;
import com.pixulse.infx.config.InfXConfig;
import com.pixulse.infx.world.BlightTracker;
import com.pixulse.infx.world.MoonPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MITE blood-moon blight for vanilla crops: on a blood-moon night a random tick has a 25%
 * chance to blight the crop (contagious, 1/64 death, growth suppressed). InfX row crops keep
 * their own blighted block-state property, so they are excluded here.
 */
@Mixin(CropBlock.class)
public abstract class CropBlockBlightMixin {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void infx$bloodMoonBlight(
            BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        BlightTracker tracker = BlightTracker.get(level);
        if (tracker.isBlighted(pos)) {
            tickBlighted(level, pos, state, random, tracker);
            ci.cancel();
            return;
        }
        if (InfXConfig.INSTANCE.world.enabled.getValue()
                && InfXConfig.INSTANCE.world.bloodMoonBlight.getValue()
                && MoonPhase.BLOOD.isActiveInOverworldAtNight(level)
                && !level.getServer().isDedicatedServer()
                && level.canSeeSky(pos)
                && random.nextFloat() < 0.25F) {
            tracker.blight(pos);
            ci.cancel();
        }
    }

    @Inject(method = "performBonemeal", at = @At("HEAD"), cancellable = true)
    private void infx$cureBlight(
            ServerLevel level, RandomSource random, BlockPos pos, BlockState state, CallbackInfo ci) {
        BlightTracker tracker = BlightTracker.get(level);
        if (tracker.isBlighted(pos)) {
            tracker.cure(pos);
            ci.cancel();
        }
    }

    private static void tickBlighted(
            ServerLevel level, BlockPos pos, BlockState state, RandomSource random, BlightTracker tracker) {
        if (random.nextInt(64) == 0) {
            Block.dropResources(state, level, pos);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            tracker.cure(pos);
            return;
        }
        if (random.nextBoolean()) {
            return;
        }
        BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
        if (target.equals(pos) || !level.isLoaded(target)) {
            return;
        }
        BlockState targetState = level.getBlockState(target);
        if (targetState.getBlock() instanceof CropBlock
                && !(targetState.getBlock() instanceof InfxCropBlock)
                && !tracker.isBlighted(target)) {
            tracker.blight(target);
        }
    }
}
