package com.pixulse.infx.mixin.world.level.block.state;

import com.pixulse.infx.registry.tag.InfXBlockTags;
import com.pixulse.infx.world.SoilCollapse;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseLandslideMixin {
    @Unique
    private BlockBehaviour.BlockStateBase self() {
        return (BlockBehaviour.BlockStateBase) (Object) this;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void runScheduledSoilCollapse(
            ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo callbackInfo) {
        if (self().is(InfXBlockTags.GRAVITY_SOILS)) SoilCollapse.onScheduledTick(level, pos);
    }

    @Inject(method = "handleNeighborChanged", at = @At("TAIL"))
    private void scheduleSoilAfterSupportChanged(
            Level level,
            BlockPos pos,
            Block neighborBlock,
            Orientation orientation,
            boolean movedByPiston,
            CallbackInfo callbackInfo) {
        if (level instanceof ServerLevel serverLevel && self().is(InfXBlockTags.GRAVITY_SOILS)) {
            SoilCollapse.schedule(serverLevel, pos);
        }
    }
}
