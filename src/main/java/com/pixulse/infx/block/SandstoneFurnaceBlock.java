package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.furnace.FurnaceHeatPolicy;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class SandstoneFurnaceBlock extends R196FurnaceBlock {
    public static final MapCodec<SandstoneFurnaceBlock> CODEC = simpleCodec(SandstoneFurnaceBlock::new);

    public SandstoneFurnaceBlock(BlockBehaviour.Properties properties) {
        super(properties, FurnaceHeatPolicy.HEAT_WOOD, true);
    }

    @Override
    public MapCodec<SandstoneFurnaceBlock> codec() {
        return CODEC;
    }
}
