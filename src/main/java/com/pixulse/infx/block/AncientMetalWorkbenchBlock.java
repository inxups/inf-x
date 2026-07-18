package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.crafting.BenchTier;

import net.minecraft.world.level.block.state.BlockBehaviour;

public final class AncientMetalWorkbenchBlock extends TieredWorkbenchBlock {
    public static final MapCodec<AncientMetalWorkbenchBlock> CODEC = simpleCodec(AncientMetalWorkbenchBlock::new);

    public AncientMetalWorkbenchBlock(BlockBehaviour.Properties properties) {
        super(BenchTier.ANCIENT_METAL, "container.infx.ancient_metal_workbench", properties);
    }

    @Override
    public MapCodec<AncientMetalWorkbenchBlock> codec() {
        return CODEC;
    }
}
