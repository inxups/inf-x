package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.crafting.BenchTier;

import net.minecraft.world.level.block.state.BlockBehaviour;

public final class SilverWorkbenchBlock extends TieredWorkbenchBlock {
    public static final MapCodec<SilverWorkbenchBlock> CODEC = simpleCodec(SilverWorkbenchBlock::new);

    public SilverWorkbenchBlock(BlockBehaviour.Properties properties) {
        super(BenchTier.SILVER, "container.infx.silver_workbench", properties);
    }

    @Override
    public MapCodec<SilverWorkbenchBlock> codec() {
        return CODEC;
    }
}
