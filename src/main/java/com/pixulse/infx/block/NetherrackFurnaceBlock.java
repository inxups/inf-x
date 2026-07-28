package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.data.furnace.FurnaceHeatPolicy;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jspecify.annotations.NonNull;

public final class NetherrackFurnaceBlock extends MiteFurnaceBlock {
    public static final MapCodec<NetherrackFurnaceBlock> CODEC =
            simpleCodec(NetherrackFurnaceBlock::new);

    public NetherrackFurnaceBlock(BlockBehaviour.Properties properties) {
        super(properties, FurnaceHeatPolicy.HEAT_BLAZE, true);
    }

    @Override
    public @NonNull MapCodec<NetherrackFurnaceBlock> codec() {
        return CODEC;
    }
}
