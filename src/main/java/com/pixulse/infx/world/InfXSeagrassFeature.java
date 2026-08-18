package com.pixulse.infx.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

/**
 * Places seagrass in the MITE river variants by scanning down from the water surface to the
 * riverbed, instead of trusting the {@code OCEAN_FLOOR} heightmap. Vanilla {@link
 * net.minecraft.world.level.levelgen.feature.SeagrassFeature} keys its placement Y off that
 * heightmap, which can resolve to a non-water position in the shallow/narrow river variants and
 * leave them bare; this feature lands on the surface water column directly (queried per offset,
 * like vanilla) and otherwise mirrors vanilla tall/short seagrass placement. It stops at the
 * first non-water block above the surface water so it never decorates underground cave pools.
 */
public final class InfXSeagrassFeature extends Feature<ProbabilityFeatureConfiguration> {
    private static final int HORIZONTAL_SPREAD = 8;
    private static final int MAX_SCAN_DEPTH = 16;

    public InfXSeagrassFeature(Codec<ProbabilityFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ProbabilityFeatureConfiguration> context) {
        RandomSource random = context.random();
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        ProbabilityFeatureConfiguration config = context.config();
        int x = origin.getX() + random.nextInt(HORIZONTAL_SPREAD) - random.nextInt(HORIZONTAL_SPREAD);
        int z = origin.getZ() + random.nextInt(HORIZONTAL_SPREAD) - random.nextInt(HORIZONTAL_SPREAD);
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        int floor = Math.max(level.getMinY(), surfaceY - MAX_SCAN_DEPTH);
        for (int y = surfaceY; y >= floor; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState here = level.getBlockState(pos);
            if (here.isAir()) {
                continue;
            }
            if (!here.is(Blocks.WATER)) {
                // Reached ground before any surface water — dry bank or underground; stop so we
                // never decorate cave pools that may sit below a dry column.
                return false;
            }
            BlockPos belowPos = pos.below();
            BlockState below = level.getBlockState(belowPos);
            if (!below.isFaceSturdy(level, belowPos, Direction.UP)
                    || below.is(BlockTags.CANNOT_SUPPORT_SEAGRASS)) {
                continue; // still in the water column above the bed — keep descending
            }
            boolean tall = random.nextDouble() < config.probability
                    && level.getBlockState(pos.above()).is(Blocks.WATER);
            if (tall) {
                level.setBlock(
                        pos,
                        Blocks.TALL_SEAGRASS.defaultBlockState().setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.LOWER),
                        2);
                level.setBlock(
                        pos.above(),
                        Blocks.TALL_SEAGRASS.defaultBlockState().setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER),
                        2);
            } else {
                level.setBlock(pos, Blocks.SEAGRASS.defaultBlockState(), 2);
            }
            return true;
        }
        return false;
    }
}
