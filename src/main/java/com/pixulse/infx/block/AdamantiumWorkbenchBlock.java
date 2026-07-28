package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.recipe.BenchTier;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jspecify.annotations.NonNull;

public final class AdamantiumWorkbenchBlock extends TieredWorkbenchBlock {
    public static final MapCodec<AdamantiumWorkbenchBlock> CODEC = simpleCodec(AdamantiumWorkbenchBlock::new);

    public AdamantiumWorkbenchBlock(BlockBehaviour.Properties properties) {
        super(BenchTier.ADAMANTIUM, "container.infx.adamantium_workbench", properties);
    }

    @Override
    public @NonNull MapCodec<AdamantiumWorkbenchBlock> codec() {
        return CODEC;
    }
}
