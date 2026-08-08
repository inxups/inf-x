package com.pixulse.infx.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * InfX leaves give projectiles (arrows, tridents, snowballs, ...) and dropped items no
 * collision shape, so they fly and fall straight through. Players, mobs, minecarts and boats
 * still collide with leaves normally.
 *
 * <p>Both arrow flight ({@code ClipContext.Block.COLLIDER}) and entity movement
 * ({@code Entity#getBlockCollisions}) funnel into the entity-aware
 * {@code BlockState#getCollisionShape(BlockGetter, BlockPos, CollisionContext)}, so this single
 * hook covers both paths. It is gated on {@link LeavesBlock} to leave every other block
 * untouched.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class LeavesBlockProjectilePassMixin {
    @Inject(
            method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD"),
            cancellable = true)
    private void infx$projectilesPassThroughLeaves(
            BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> callback) {
        BlockState state = (BlockState) (Object) this;
        if (state.getBlock() instanceof LeavesBlock
                && context instanceof EntityCollisionContext entityContext) {
            Entity entity = entityContext.getEntity();
            if (entity instanceof Projectile || entity instanceof ItemEntity) {
                callback.setReturnValue(Shapes.empty());
            }
        }
    }
}
