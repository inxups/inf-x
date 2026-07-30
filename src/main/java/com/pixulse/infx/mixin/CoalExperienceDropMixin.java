package com.pixulse.infx.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** MITE coal ore yields no experience when mined. */
@Mixin(DropExperienceBlock.class)
public abstract class CoalExperienceDropMixin {
    @Inject(method = "getExpDrop", at = @At("HEAD"), cancellable = true)
    private void infx$removeCoalExperience(
            BlockState state,
            LevelAccessor level,
            BlockPos pos,
            @Nullable BlockEntity blockEntity,
            @Nullable Entity breaker,
            ItemStack tool,
            CallbackInfoReturnable<Integer> callback) {
        if (state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE)) {
            callback.setReturnValue(0);
        }
    }
}
