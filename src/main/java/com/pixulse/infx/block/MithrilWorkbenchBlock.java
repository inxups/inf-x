package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.recipe.BenchTier;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jspecify.annotations.NonNull;

public final class MithrilWorkbenchBlock extends TieredWorkbenchBlock {
    public static final MapCodec<MithrilWorkbenchBlock> CODEC = simpleCodec(MithrilWorkbenchBlock::new);

    public MithrilWorkbenchBlock(BlockBehaviour.Properties properties) {
        super(BenchTier.MITHRIL, "container.infx.mithril_workbench", properties);
    }

    @Override
    public @NonNull MapCodec<MithrilWorkbenchBlock> codec() {
        return CODEC;
    }
}
