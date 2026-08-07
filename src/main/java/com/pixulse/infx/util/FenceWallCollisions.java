package com.pixulse.infx.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * InfX fences and walls collide only one block high, letting players jump them.
 * Capped shapes are cached per state; full-height shapes stay for minecarts.
 */
public final class FenceWallCollisions {
    private static final Map<BlockState, VoxelShape> ONE_BLOCK_CACHE = new ConcurrentHashMap<>();

    private FenceWallCollisions() {}

    public static VoxelShape capAtOneBlock(BlockState state, VoxelShape original) {
        if (original.max(Direction.Axis.Y) <= 1.0) {
            return original;
        }
        return ONE_BLOCK_CACHE.computeIfAbsent(state, key ->
                Shapes.join(original, Shapes.block(), BooleanOp.AND));
    }
}
