package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.furnace.FurnaceHeatPolicy;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class NetherrackFurnaceBlock extends R196FurnaceBlock {
    public static final MapCodec<NetherrackFurnaceBlock> CODEC =
            simpleCodec(NetherrackFurnaceBlock::new);

    public NetherrackFurnaceBlock(BlockBehaviour.Properties properties) {
        super(properties, FurnaceHeatPolicy.HEAT_BLAZE, true);
    }

    @Override
    public MapCodec<NetherrackFurnaceBlock> codec() {
        return CODEC;
    }
}
