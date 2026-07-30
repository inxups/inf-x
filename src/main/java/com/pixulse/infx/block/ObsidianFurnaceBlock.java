package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.data.furnace.FurnaceHeatPolicy;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jspecify.annotations.NonNull;

public final class ObsidianFurnaceBlock extends InfxFurnaceBlock {
    public static final MapCodec<ObsidianFurnaceBlock> CODEC = simpleCodec(ObsidianFurnaceBlock::new);

    public ObsidianFurnaceBlock(BlockBehaviour.Properties properties) {
        super(properties, FurnaceHeatPolicy.HEAT_LAVA, true);
    }

    @Override
    public @NonNull MapCodec<ObsidianFurnaceBlock> codec() {
        return CODEC;
    }
}
