package com.pixulse.infx.mixin;

import com.pixulse.infx.util.FenceWallCollisions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MITE fences and bars collide only one block high so players can jump them;
 * minecarts keep full height.
 */
@Mixin(CrossCollisionBlock.class)
public abstract class CrossCollisionBlockCollisionMixin {
    @Inject(method = "getCollisionShape", at = @At("RETURN"), cancellable = true)
    private void infx$oneBlockFenceCollision(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context,
            CallbackInfoReturnable<VoxelShape> callback) {
        if (context instanceof EntityCollisionContext entityContext
                && entityContext.getEntity() instanceof AbstractMinecart) {
            return;
        }
        callback.setReturnValue(FenceWallCollisions.capAtOneBlock(state, callback.getReturnValue()));
    }
}
