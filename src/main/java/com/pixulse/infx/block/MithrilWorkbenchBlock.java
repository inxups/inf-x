package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.crafting.BenchTier;

import net.minecraft.world.level.block.state.BlockBehaviour;

public final class MithrilWorkbenchBlock extends TieredWorkbenchBlock {
    public static final MapCodec<MithrilWorkbenchBlock> CODEC = simpleCodec(MithrilWorkbenchBlock::new);

    public MithrilWorkbenchBlock(BlockBehaviour.Properties properties) {
        super(BenchTier.MITHRIL, "container.infx.mithril_workbench", properties);
    }

    @Override
    public MapCodec<MithrilWorkbenchBlock> codec() {
        return CODEC;
    }
}
