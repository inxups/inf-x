package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.furnace.FurnaceHeatPolicy;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ClayFurnaceBlock extends R196FurnaceBlock {
    public static final MapCodec<ClayFurnaceBlock> CODEC = simpleCodec(ClayFurnaceBlock::new);

    public ClayFurnaceBlock(BlockBehaviour.Properties properties) {
        super(properties, FurnaceHeatPolicy.HEAT_WOOD, false);
    }

    @Override
    public MapCodec<ClayFurnaceBlock> codec() {
        return CODEC;
    }
}
