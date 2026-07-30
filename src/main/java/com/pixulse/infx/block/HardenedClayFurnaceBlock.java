package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.data.furnace.FurnaceHeatPolicy;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jspecify.annotations.NonNull;

public final class HardenedClayFurnaceBlock extends InfxFurnaceBlock {
    public static final MapCodec<HardenedClayFurnaceBlock> CODEC =
            simpleCodec(HardenedClayFurnaceBlock::new);

    public HardenedClayFurnaceBlock(BlockBehaviour.Properties properties) {
        super(properties, FurnaceHeatPolicy.HEAT_WOOD, true);
    }

    @Override
    public @NonNull MapCodec<HardenedClayFurnaceBlock> codec() {
        return CODEC;
    }
}
