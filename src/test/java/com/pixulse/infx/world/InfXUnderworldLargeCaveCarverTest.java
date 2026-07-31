package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class InfXUnderworldLargeCaveCarverTest {
    private static final long CAVE_SEED = 0x1F1A7EL;

    @Test
    void macroCentersRemainStableAcrossZeroAndNegativeCoordinates() {
        assertTrue(InfXUnderworldLargeCaveCarver.isMacroCenter(new ChunkPos(8, 8)));
        assertFalse(InfXUnderworldLargeCaveCarver.isMacroCenter(new ChunkPos(7, 8)));
        assertTrue(InfXUnderworldLargeCaveCarver.isMacroCenter(new ChunkPos(-8, -8)));
        assertFalse(InfXUnderworldLargeCaveCarver.isMacroCenter(new ChunkPos(-9, -8)));
        assertTrue(InfXUnderworldLargeCaveCarver.isMacroCenter(new ChunkPos(-24, 8)));
        assertFalse(InfXUnderworldLargeCaveCarver.isMacroCenter(new ChunkPos(-16, 8)));
    }

    @Test
    void noiseFieldUsesTheConfiguredBoundsAndKeepsOpeningsNearBothLimits() {
        assertTrue(InfXUnderworldCaveNoise.sample(CAVE_SEED, 0, Underworld.LARGE_CAVE_MIN_Y, 0) <= 0.0);
        assertTrue(InfXUnderworldCaveNoise.sample(CAVE_SEED, 0, Underworld.LARGE_CAVE_MAX_Y, 0) <= 0.0);
        assertTrue(InfXUnderworldCaveNoise.sample(CAVE_SEED, 0, Underworld.LARGE_CAVE_CENTER_Y, 0) > 0.0);
        assertTrue(InfXUnderworldCaveNoise.sample(
                        CAVE_SEED, Underworld.LARGE_CAVE_OUTER_RADIUS + 1, Underworld.LARGE_CAVE_CENTER_Y, 0)
                <= 0.0);
        assertTrue(carvedSamplesAtY(Underworld.LARGE_CAVE_MIN_Y + 4) > 0);
        assertTrue(carvedSamplesAtY(Underworld.LARGE_CAVE_MAX_Y - 4) > 0);
    }

    @Test
    void cheeseFieldIsAsymmetricAndRetainsNaturalSolidIslands() {
        int carved = 0;
        int solid = 0;
        int mirroredDifferences = 0;
        for (int x = -100; x <= 100; x += 4) {
            for (int z = -100; z <= 100; z += 4) {
                boolean current = isCarved(CAVE_SEED, x, Underworld.LARGE_CAVE_CENTER_Y, z);
                carved += current ? 1 : 0;
                solid += current ? 0 : 1;
                mirroredDifferences += current
                                != isCarved(CAVE_SEED, -x, Underworld.LARGE_CAVE_CENTER_Y, z)
                        ? 1
                        : 0;
            }
        }

        assertTrue(carved > 500, "the center slice must contain a broad cheese cavern");
        assertTrue(solid > 100, "layers and pillars must leave solid islands inside the cavern envelope");
        assertTrue(mirroredDifferences > 100, "domain warping must break the old mirrored dome shape");
    }

    @Test
    void noiseFieldIsDeterministicAndChangesWithItsCaveSeed() {
        long first = shapeHash(CAVE_SEED);
        assertEquals(first, shapeHash(CAVE_SEED));
        assertNotEquals(first, shapeHash(CAVE_SEED + 1));
    }

    @Test
    void outerRadiusLimitsWhichTargetChunksNeedNoiseSampling() {
        int centerX = new ChunkPos(8, 8).getMiddleBlockX();
        int centerZ = new ChunkPos(8, 8).getMiddleBlockZ();

        assertTrue(InfXUnderworldLargeCaveCarver.canAffect(new ChunkPos(8, 8), centerX, centerZ));
        assertTrue(InfXUnderworldLargeCaveCarver.canAffect(new ChunkPos(15, 8), centerX, centerZ));
        assertTrue(InfXUnderworldLargeCaveCarver.canAffect(new ChunkPos(16, 8), centerX, centerZ));
        assertFalse(InfXUnderworldLargeCaveCarver.canAffect(new ChunkPos(17, 8), centerX, centerZ));
    }

    @Test
    void fourBlockNoiseCellsRemainContinuousAcrossChunkBoundaries() {
        ChunkPos left = new ChunkPos(0, 0);
        ChunkPos right = new ChunkPos(1, 0);
        int centerX = new ChunkPos(8, 8).getMiddleBlockX();
        int centerZ = new ChunkPos(8, 8).getMiddleBlockZ();
        double[][][] leftGrid =
                InfXUnderworldLargeCaveCarver.sampleNoiseGrid(CAVE_SEED, left, centerX, centerZ);
        double[][][] rightGrid =
                InfXUnderworldLargeCaveCarver.sampleNoiseGrid(CAVE_SEED, right, centerX, centerZ);

        for (int gridY = 0; gridY < leftGrid[0].length; gridY++) {
            for (int gridZ = 0; gridZ < leftGrid[0][0].length; gridZ++) {
                assertEquals(leftGrid[leftGrid.length - 1][gridY][gridZ], rightGrid[0][gridY][gridZ]);
            }
        }
        assertEquals(
                rightGrid[0][12][2],
                InfXUnderworldLargeCaveCarver.interpolatedNoise(
                        rightGrid, 0, Underworld.LARGE_CAVE_MIN_Y + 12 * 4, 8));
    }

    @Test
    void onlyStoneDeepslateAndTheConfiguredInternalBedrockCanBeCarved() {
        assertTrue(InfXUnderworldLargeCaveCarver.canReplace(Blocks.STONE.defaultBlockState(), 0));
        assertTrue(InfXUnderworldLargeCaveCarver.canReplace(Blocks.DEEPSLATE.defaultBlockState(), 0));
        assertTrue(InfXUnderworldLargeCaveCarver.canReplace(Blocks.BEDROCK.defaultBlockState(), -24));
        assertFalse(InfXUnderworldLargeCaveCarver.canReplace(Blocks.BEDROCK.defaultBlockState(), -25));
        assertFalse(InfXUnderworldLargeCaveCarver.canReplace(Blocks.BEDROCK.defaultBlockState(), -8));
        assertFalse(InfXUnderworldLargeCaveCarver.canReplace(Blocks.AIR.defaultBlockState(), 0));
        assertFalse(InfXUnderworldLargeCaveCarver.canReplace(Blocks.WATER.defaultBlockState(), 0));
    }

    @Test
    void carvingTapersInsideTheDeepDarkBoundaryWithoutCrossingIt() {
        HolderOwner<Biome> owner = new HolderOwner<>() {};
        Holder<Biome> deepDark = Holder.Reference.createStandAlone(owner, Underworld.DEEP_DARK_BIOME);
        Holder<Biome> ordinary = Holder.Reference.createStandAlone(owner, Underworld.BIOME);
        Function<BlockPos, Holder<Biome>> deepDarkGetter = position -> deepDark;
        Function<BlockPos, Holder<Biome>> ordinaryGetter = position -> ordinary;
        Function<BlockPos, Holder<Biome>> edgeGetter = position -> position.getX() == 0 ? deepDark : ordinary;

        assertEquals(1.0, InfXUnderworldLargeCaveCarver.deepDarkInteriorWeight(deepDarkGetter, 0, 0));
        assertEquals(0.0, InfXUnderworldLargeCaveCarver.deepDarkInteriorWeight(ordinaryGetter, 0, 0));
        double edgeWeight = InfXUnderworldLargeCaveCarver.deepDarkInteriorWeight(edgeGetter, 0, 0);
        assertTrue(edgeWeight > 0.0 && edgeWeight < 1.0);
        assertEquals(0.0, InfXUnderworldLargeCaveCarver.deepDarkInteriorWeight(edgeGetter, 1, 0));
    }

    private static int carvedSamplesAtY(int y) {
        int carved = 0;
        for (int x = -Underworld.LARGE_CAVE_OUTER_RADIUS;
                x <= Underworld.LARGE_CAVE_OUTER_RADIUS;
                x += 4) {
            for (int z = -Underworld.LARGE_CAVE_OUTER_RADIUS;
                    z <= Underworld.LARGE_CAVE_OUTER_RADIUS;
                    z += 4) {
                carved += isCarved(CAVE_SEED, x, y, z) ? 1 : 0;
            }
        }
        return carved;
    }

    private static long shapeHash(long seed) {
        long hash = 1_125_899_906_842_597L;
        for (int x = -120; x <= 120; x += 8) {
            for (int y = Underworld.LARGE_CAVE_MIN_Y; y <= Underworld.LARGE_CAVE_MAX_Y; y += 8) {
                for (int z = -120; z <= 120; z += 8) {
                    hash = hash * 31 + (isCarved(seed, x, y, z) ? 1 : 0);
                }
            }
        }
        return hash;
    }

    private static boolean isCarved(long seed, int x, int y, int z) {
        return InfXUnderworldCaveNoise.sample(seed, x, y, z) > 0.0;
    }
}
