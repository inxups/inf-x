package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.crafting.BenchTier;

import net.minecraft.world.level.block.state.BlockBehaviour;

public final class IronWorkbenchBlock extends TieredWorkbenchBlock {
    public static final MapCodec<IronWorkbenchBlock> CODEC = simpleCodec(IronWorkbenchBlock::new);

    public IronWorkbenchBlock(BlockBehaviour.Properties properties) {
        super(BenchTier.IRON, "container.infx.iron_workbench", properties);
    }

    @Override
    public MapCodec<IronWorkbenchBlock> codec() {
        return CODEC;
    }
}
