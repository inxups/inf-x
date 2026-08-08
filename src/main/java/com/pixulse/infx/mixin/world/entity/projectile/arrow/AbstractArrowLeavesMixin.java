package com.pixulse.infx.mixin.world.entity.projectile.arrow;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * InfX: the arrow flight clip ignores leaves ({@link
 * com.pixulse.infx.mixin.world.level.block.LeavesBlockProjectilePassMixin}), so an arrow can
 * move into a leaves block's volume. Vanilla {@code AbstractArrow#tick} then grounds any arrow
 * whose position sits inside a non-empty collision shape; this redirect makes that check treat
 * leaves as empty, letting the arrow keep flying through.
 */
@Mixin(net.minecraft.world.entity.projectile.arrow.AbstractArrow.class)
public abstract class AbstractArrowLeavesMixin {
    @Redirect(
            method = "tick",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/block/state/BlockState;getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"))
    private VoxelShape infx$noGroundingInsideLeaves(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getBlock() instanceof LeavesBlock ? Shapes.empty() : state.getCollisionShape(level, pos);
    }
}
