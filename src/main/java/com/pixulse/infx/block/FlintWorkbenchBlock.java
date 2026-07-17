package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.crafting.BenchTier;

import net.minecraft.world.level.block.state.BlockBehaviour;

public final class FlintWorkbenchBlock extends TieredWorkbenchBlock {
    public static final MapCodec<FlintWorkbenchBlock> CODEC = simpleCodec(FlintWorkbenchBlock::new);

    public FlintWorkbenchBlock(BlockBehaviour.Properties properties) {
        super(BenchTier.FLINT, "container.infx.flint_workbench", properties);
    }

    @Override
    public MapCodec<FlintWorkbenchBlock> codec() {
        return CODEC;
    }
}
