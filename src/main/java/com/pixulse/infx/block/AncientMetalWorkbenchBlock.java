package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.recipe.BenchTier;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jspecify.annotations.NonNull;

public final class AncientMetalWorkbenchBlock extends TieredWorkbenchBlock {
    public static final MapCodec<AncientMetalWorkbenchBlock> CODEC = simpleCodec(AncientMetalWorkbenchBlock::new);

    public AncientMetalWorkbenchBlock(BlockBehaviour.Properties properties) {
        super(BenchTier.ANCIENT_METAL, "container.infx.ancient_metal_workbench", properties);
    }

    @Override
    public @NonNull MapCodec<AncientMetalWorkbenchBlock> codec() {
        return CODEC;
    }
}
