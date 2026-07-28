package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.recipe.BenchTier;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jspecify.annotations.NonNull;

public final class CopperWorkbenchBlock extends TieredWorkbenchBlock {
    public static final MapCodec<CopperWorkbenchBlock> CODEC = simpleCodec(CopperWorkbenchBlock::new);

    public CopperWorkbenchBlock(BlockBehaviour.Properties properties) {
        super(BenchTier.COPPER, "container.infx.copper_workbench", properties);
    }

    @Override
    public @NonNull MapCodec<CopperWorkbenchBlock> codec() {
        return CODEC;
    }
}
