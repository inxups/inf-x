package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.furnace.FurnaceHeatPolicy;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class HardenedClayFurnaceBlock extends R196FurnaceBlock {
    public static final MapCodec<HardenedClayFurnaceBlock> CODEC =
            simpleCodec(HardenedClayFurnaceBlock::new);

    public HardenedClayFurnaceBlock(BlockBehaviour.Properties properties) {
        super(properties, FurnaceHeatPolicy.HEAT_WOOD, true);
    }

    @Override
    public MapCodec<HardenedClayFurnaceBlock> codec() {
        return CODEC;
    }
}
