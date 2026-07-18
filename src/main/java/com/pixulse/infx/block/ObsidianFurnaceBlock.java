package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.furnace.FurnaceHeatPolicy;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ObsidianFurnaceBlock extends R196FurnaceBlock {
    public static final MapCodec<ObsidianFurnaceBlock> CODEC = simpleCodec(ObsidianFurnaceBlock::new);

    public ObsidianFurnaceBlock(BlockBehaviour.Properties properties) {
        super(properties, FurnaceHeatPolicy.HEAT_LAVA, true);
    }

    @Override
    public MapCodec<ObsidianFurnaceBlock> codec() {
        return CODEC;
    }
}
