package com.pixulse.infx.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Restores InfX's tightly enclosed Underworld water and lava source attempts. */
public final class InfXUnderworldLiquidSourceFeature extends Feature<NoneFeatureConfiguration> {
    private static final int SOURCE_COLUMN_SIZE = 16;
    private static final int SOURCE_COLUMN_OFFSET = 8;
    private static final int LOCAL_Y_RANDOM_BOUND = Underworld.TERRAIN_HEIGHT - SOURCE_COLUMN_OFFSET;
    private static final int HIGH_WATER_ROLL_BOUND = 32;
    private static final int HIGH_WATER_ROLL_OFFSET = 16;
    private static final float LAVA_CHANCE = 0.95F;
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState LAVA = Blocks.LAVA.defaultBlockState();

    public InfXUnderworldLiquidSourceFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos origin = context.origin();
        int chunkX = SectionPos.blockToSectionCoord(origin.getX());
        int chunkZ = SectionPos.blockToSectionCoord(origin.getZ());
        int minX = SectionPos.sectionToBlockCoord(chunkX);
        int minZ = SectionPos.sectionToBlockCoord(chunkZ);
        RandomSource random = context.random();
        WorldGenLevel level = context.level();
        boolean placedAny = false;

        for (int attempt = 0; attempt < Underworld.LIQUID_SOURCE_ATTEMPTS_PER_CHUNK; attempt++) {
            int x = minX + random.nextInt(SOURCE_COLUMN_SIZE) + SOURCE_COLUMN_OFFSET;
            int localY = sampleLocalY(random);
            int z = minZ + random.nextInt(SOURCE_COLUMN_SIZE) + SOURCE_COLUMN_OFFSET;
            BlockPos sourcePos = new BlockPos(x, Underworld.TERRAIN_MIN_Y + localY, z);
            BlockState fluid = chooseFluid(random, localY);
            if (fluid.is(Blocks.WATER) && !isWaterAllowedAtY(sourcePos.getY())) {
                continue;
            }
            if (!hasValidSourceGeometry(level, sourcePos)) {
                continue;
            }

            if (level.setBlock(sourcePos, fluid, 2)) {
                level.scheduleTick(sourcePos, fluid.getFluidState().getType(), 0);
                placedAny = true;
            }
        }

        return placedAny;
    }

    static int sampleLocalY(RandomSource random) {
        return random.nextInt(random.nextInt(LOCAL_Y_RANDOM_BOUND) + SOURCE_COLUMN_OFFSET);
    }

    static boolean isUpperWaterAttempt(int localY, int waterRoll) {
        return waterRoll + HIGH_WATER_ROLL_OFFSET < localY;
    }

    static boolean isWaterAllowedAtY(int y) {
        return y >= Underworld.WATER_MIN_Y;
    }

    static boolean hasValidSourceGeometry(WorldGenLevel level, BlockPos sourcePos) {
        if (!level.getBlockState(sourcePos.above()).is(Blocks.STONE)
                || !level.getBlockState(sourcePos.below()).is(Blocks.STONE)) {
            return false;
        }

        BlockState state = level.getBlockState(sourcePos);
        if (!state.isAir() && !state.is(Blocks.STONE)) {
            return false;
        }

        int stoneNeighbors = 0;
        int airNeighbors = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = sourcePos.relative(direction);
            if (level.getBlockState(neighbor).is(Blocks.STONE)) {
                stoneNeighbors++;
            }
            if (level.isEmptyBlock(neighbor)) {
                airNeighbors++;
            }
        }
        return stoneNeighbors == 3 && airNeighbors == 1;
    }

    private static BlockState chooseFluid(RandomSource random, int localY) {
        if (isUpperWaterAttempt(localY, random.nextInt(HIGH_WATER_ROLL_BOUND))) {
            return WATER;
        }
        return random.nextFloat() < LAVA_CHANCE ? LAVA : WATER;
    }
}
