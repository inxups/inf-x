package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.crafting.BenchTier;

import net.minecraft.world.level.block.state.BlockBehaviour;

public final class CopperWorkbenchBlock extends TieredWorkbenchBlock {
    public static final MapCodec<CopperWorkbenchBlock> CODEC = simpleCodec(CopperWorkbenchBlock::new);

    public CopperWorkbenchBlock(BlockBehaviour.Properties properties) {
        super(BenchTier.COPPER, "container.infx.copper_workbench", properties);
    }

    @Override
    public MapCodec<CopperWorkbenchBlock> codec() {
        return CODEC;
    }
}
