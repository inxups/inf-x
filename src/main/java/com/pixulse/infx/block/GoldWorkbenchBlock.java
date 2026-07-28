package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.recipe.BenchTier;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jspecify.annotations.NonNull;

public final class GoldWorkbenchBlock extends TieredWorkbenchBlock {
    public static final MapCodec<GoldWorkbenchBlock> CODEC = simpleCodec(GoldWorkbenchBlock::new);

    public GoldWorkbenchBlock(BlockBehaviour.Properties properties) {
        super(BenchTier.GOLD, "container.infx.gold_workbench", properties);
    }

    @Override
    public @NonNull MapCodec<GoldWorkbenchBlock> codec() {
        return CODEC;
    }
}
