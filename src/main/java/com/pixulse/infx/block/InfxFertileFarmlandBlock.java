package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * InfX fertilized farmland: a vanilla-compatible FarmlandBlock variant shown while the
 * crop-growth fertility bonus is active. All moisture, hydration and trampling behavior
 * is inherited; the fertile state ends when the crop consumes it or the block changes.
 */
public final class InfxFertileFarmlandBlock extends FarmlandBlock {
    private static final MapCodec<FarmlandBlock> CODEC = simpleCodec(InfxFertileFarmlandBlock::new);

    public InfxFertileFarmlandBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<FarmlandBlock> codec() {
        return CODEC;
    }
}
