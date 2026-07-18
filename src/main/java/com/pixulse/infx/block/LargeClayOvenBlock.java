package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.furnace.FurnaceHeatPolicy;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** The distinct heat-one oven body that accepts full blocks and other large inputs. */
public final class LargeClayOvenBlock extends R196FurnaceBlock {
    public static final MapCodec<LargeClayOvenBlock> CODEC = simpleCodec(LargeClayOvenBlock::new);

    public LargeClayOvenBlock(BlockBehaviour.Properties properties) {
        super(properties, FurnaceHeatPolicy.HEAT_WOOD, true);
    }

    @Override
    public MapCodec<LargeClayOvenBlock> codec() {
        return CODEC;
    }
}
