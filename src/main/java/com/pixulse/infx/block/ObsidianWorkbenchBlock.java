package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.recipe.BenchTier;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jspecify.annotations.NonNull;

public final class ObsidianWorkbenchBlock extends TieredWorkbenchBlock {
    public static final MapCodec<ObsidianWorkbenchBlock> CODEC = simpleCodec(ObsidianWorkbenchBlock::new);

    public ObsidianWorkbenchBlock(BlockBehaviour.Properties properties) {
        super(BenchTier.OBSIDIAN, "container.infx.obsidian_workbench", properties);
    }

    @Override
    public @NonNull MapCodec<ObsidianWorkbenchBlock> codec() {
        return CODEC;
    }
}
