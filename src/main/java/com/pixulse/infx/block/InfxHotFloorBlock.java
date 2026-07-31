package com.pixulse.infx.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Solid hot ground that ignites entities standing on it. */
public final class InfxHotFloorBlock extends Block {
    private static final float FIRE_SECONDS = 8.0F;

    public InfxHotFloorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState onState, Entity entity) {
        if (!entity.fireImmune()) {
            entity.igniteForSeconds(FIRE_SECONDS);
        }
        super.stepOn(level, pos, onState, entity);
    }
}
