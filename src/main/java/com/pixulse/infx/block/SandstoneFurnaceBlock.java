package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.data.furnace.FurnaceHeatPolicy;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jspecify.annotations.NonNull;

public final class SandstoneFurnaceBlock extends MiteFurnaceBlock {
    public static final MapCodec<SandstoneFurnaceBlock> CODEC = simpleCodec(SandstoneFurnaceBlock::new);

    public SandstoneFurnaceBlock(BlockBehaviour.Properties properties) {
        super(properties, FurnaceHeatPolicy.HEAT_WOOD, true);
    }

    @Override
    public @NonNull MapCodec<SandstoneFurnaceBlock> codec() {
        return CODEC;
    }
}
