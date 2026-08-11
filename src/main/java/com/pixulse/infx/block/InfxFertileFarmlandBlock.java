package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.TriState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

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

    /**
     * Brown mushrooms may only be planted on moist fertilized farmland, which immediately
     * converts to mycelium; vanilla farmland can never carry a mushroom.
     */
    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction facing, BlockState plant) {
        return plant.getBlock() instanceof MushroomBlock
                ? TriState.TRUE
                : super.canSustainPlant(state, level, pos, facing, plant);
    }
}
