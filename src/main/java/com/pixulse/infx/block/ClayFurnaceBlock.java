package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.data.furnace.FurnaceHeatPolicy;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jspecify.annotations.NonNull;

public final class ClayFurnaceBlock extends MiteFurnaceBlock {
    public static final MapCodec<ClayFurnaceBlock> CODEC = simpleCodec(ClayFurnaceBlock::new);

    public ClayFurnaceBlock(BlockBehaviour.Properties properties) {
        super(properties, FurnaceHeatPolicy.HEAT_WOOD, false);
    }

    @Override
    public @NonNull MapCodec<ClayFurnaceBlock> codec() {
        return CODEC;
    }
}
