package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.crafting.BenchTier;

import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ObsidianWorkbenchBlock extends TieredWorkbenchBlock {
    public static final MapCodec<ObsidianWorkbenchBlock> CODEC = simpleCodec(ObsidianWorkbenchBlock::new);

    public ObsidianWorkbenchBlock(BlockBehaviour.Properties properties) {
        super(BenchTier.OBSIDIAN, "container.infx.obsidian_workbench", properties);
    }

    @Override
    public MapCodec<ObsidianWorkbenchBlock> codec() {
        return CODEC;
    }
}
