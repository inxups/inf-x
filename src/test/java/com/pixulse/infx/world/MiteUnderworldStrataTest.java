package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

class MiteUnderworldStrataTest {
    private static final long WORLD_SEED = 0x4D4954455F313936L;
    private static final ChunkPos CHUNK_POS = new ChunkPos(7, -11);

    @Test
    void boundaryThicknessUsesTheMiteHashedSeedAndChunkHash() {
        MiteUnderworldStrata.StrataPlan plan = MiteUnderworldStrata.plan(WORLD_SEED, CHUNK_POS);

        assertAll(
                () -> assertEquals(2, plan.boundaryThicknessAt(0, 0)),
                () -> assertEquals(1, plan.boundaryThicknessAt(0, 15)),
                () -> assertEquals(1, plan.boundaryThicknessAt(3, 4)),
                () -> assertEquals(3, plan.boundaryThicknessAt(7, 4)),
                () -> assertEquals(3, plan.boundaryThicknessAt(15, 0)),
                () -> assertEquals(3, plan.boundaryThicknessAt(15, 15)));
    }

    @Test
    void lowerStrataKeepMantleAndThreeInternalMiteBedrockBands() {
        MiteUnderworldStrata.StrataPlan plan = MiteUnderworldStrata.plan(WORLD_SEED, CHUNK_POS);
        int[] bandCounts = new int[3];
        int bottomSlabCount = 0;
        for (int chunkX = -2; chunkX <= 2; chunkX++) {
            for (int chunkZ = -2; chunkZ <= 2; chunkZ++) {
                MiteUnderworldStrata.StrataPlan sampledPlan = MiteUnderworldStrata.plan(
                        WORLD_SEED,
                        new ChunkPos(chunkX, chunkZ));
                bottomSlabCount += countBedrock(sampledPlan, 0, 16);
                bandCounts[0] += countBedrock(sampledPlan, 24, 48);
                bandCounts[1] += countBedrock(sampledPlan, 64, 88);
                bandCounts[2] += countBedrock(sampledPlan, 88, 112);
            }
        }

        for (int localX = 0; localX < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localX++) {
            for (int localZ = 0; localZ < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localZ++) {
                int thickness = plan.boundaryThicknessAt(localX, localZ);
                for (int relativeY = 0; relativeY < thickness; relativeY++) {
                    assertTrue(plan.hasMantleAt(localX, localZ, relativeY));
                }
                assertFalse(plan.hasMantleAt(localX, localZ, thickness));
                assertFalse(plan.hasBedrockAt(localX, localZ, thickness));
            }
        }

        int cellsPerBand = 25 * 24 * 16 * 16;
        int observedBottomSlabCount = bottomSlabCount;
        assertAll(
                () -> assertEquals(0, observedBottomSlabCount),
                () -> assertTrue(bandCounts[0] > 0),
                () -> assertTrue(bandCounts[1] > 0),
                () -> assertTrue(bandCounts[2] > 0),
                () -> assertTrue(bandCounts[0] < cellsPerBand),
                () -> assertTrue(bandCounts[1] < cellsPerBand),
                () -> assertTrue(bandCounts[2] < cellsPerBand));
    }

    @Test
    void internalBedrockCentersUseBroadSeededPassagesWithoutLosingMostOfEachLayer() {
        int[] centers = {32, 72, 96};
        int[][] ranges = {{3, 52}, {52, 84}, {84, 120}};
        int macroRegionChunkSide = 2;
        int macroRegionColumnCount = macroRegionChunkSide
                * macroRegionChunkSide
                * MiteUnderworldStrata.CHUNK_SIDE_LENGTH
                * MiteUnderworldStrata.CHUNK_SIDE_LENGTH;
        int oldRectangularPassageColumnCount = macroRegionChunkSide * macroRegionChunkSide * 4;

        for (int regionX = -1; regionX <= 1; regionX++) {
            for (int regionZ = -1; regionZ <= 1; regionZ++) {
                MiteUnderworldStrata.StrataPlan[][] plans =
                        new MiteUnderworldStrata.StrataPlan[macroRegionChunkSide][macroRegionChunkSide];
                for (int chunkOffsetX = 0; chunkOffsetX < macroRegionChunkSide; chunkOffsetX++) {
                    for (int chunkOffsetZ = 0; chunkOffsetZ < macroRegionChunkSide; chunkOffsetZ++) {
                        plans[chunkOffsetX][chunkOffsetZ] = MiteUnderworldStrata.plan(
                                WORLD_SEED,
                                new ChunkPos(
                                        regionX * macroRegionChunkSide + chunkOffsetX,
                                        regionZ * macroRegionChunkSide + chunkOffsetZ));
                    }
                }

                for (int stratum = 0; stratum < centers.length; stratum++) {
                    int gapCount = 0;
                    int coreColumnCount = 0;
                    int transitionColumnCount = 0;
                    for (int chunkOffsetX = 0; chunkOffsetX < macroRegionChunkSide; chunkOffsetX++) {
                        for (int chunkOffsetZ = 0; chunkOffsetZ < macroRegionChunkSide; chunkOffsetZ++) {
                            ChunkPos chunkPos = new ChunkPos(
                                    regionX * macroRegionChunkSide + chunkOffsetX,
                                    regionZ * macroRegionChunkSide + chunkOffsetZ);
                            MiteUnderworldStrata.StrataPlan plan = plans[chunkOffsetX][chunkOffsetZ];
                            PassageStrengthCounts strengthCounts = countPassageStrengths(plan, stratum);
                            coreColumnCount += strengthCounts.coreColumns();
                            transitionColumnCount += strengthCounts.transitionColumns();
                            gapCount += assertPassageColumnsTraverseBand(
                                    plan,
                                    chunkPos,
                                    centers[stratum],
                                    ranges[stratum][0],
                                    ranges[stratum][1]);
                        }
                    }

                    String location = "macro region " + regionX + "," + regionZ + " stratum " + stratum;
                    int observedGapCount = gapCount;
                    int observedCoreColumnCount = coreColumnCount;
                    int observedTransitionColumnCount = transitionColumnCount;
                    assertAll(
                            () -> assertTrue(
                                    observedGapCount > oldRectangularPassageColumnCount,
                                    location + " must be broader than four independent 2-by-2 cuts"),
                            () -> assertTrue(
                                    observedGapCount < macroRegionColumnCount / 2,
                                    location + " must retain most of the center bedrock surface"),
                            () -> assertTrue(
                                    observedCoreColumnCount > 0,
                                    location + " must contain a fully eroded passage core"),
                            () -> assertTrue(
                                    observedTransitionColumnCount > observedCoreColumnCount,
                                    location + " must contain a broad fractional falloff"));
                }
            }
        }
    }

    @Test
    void smoothPassageFieldIsDeterministicAndWorldSeeded() {
        MiteUnderworldStrata.StrataPlan first = MiteUnderworldStrata.plan(WORLD_SEED, CHUNK_POS);
        MiteUnderworldStrata.StrataPlan repeated = MiteUnderworldStrata.plan(WORLD_SEED, CHUNK_POS);
        MiteUnderworldStrata.StrataPlan otherSeed = MiteUnderworldStrata.plan(WORLD_SEED + 1, CHUNK_POS);
        boolean differsFromOtherSeed = false;

        for (int stratum = 0; stratum < 3; stratum++) {
            for (int localX = 0; localX < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localX++) {
                for (int localZ = 0; localZ < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localZ++) {
                    double firstStrength = first.passageStrengthAt(stratum, localX, localZ);
                    assertEquals(firstStrength, repeated.passageStrengthAt(stratum, localX, localZ));
                    if (firstStrength != otherSeed.passageStrengthAt(stratum, localX, localZ)) {
                        differsFromOtherSeed = true;
                    }
                }
            }
        }

        assertTrue(differsFromOtherSeed);
    }

    @Test
    void generationMixinIsRegistered() throws IOException {
        var stream = MiteUnderworldStrataTest.class.getClassLoader().getResourceAsStream("infx.mixins.json");
        assertNotNull(stream);
        try (stream) {
            String mixins = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(mixins.contains("\"NoiseBasedChunkGeneratorMixin\""));
        }
    }

    private static int countBedrock(MiteUnderworldStrata.StrataPlan plan, int minimumY, int maximumY) {
        int count = 0;
        for (int localX = 0; localX < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localX++) {
            for (int localZ = 0; localZ < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localZ++) {
                for (int relativeY = minimumY; relativeY < maximumY; relativeY++) {
                    if (plan.hasBedrockAt(localX, localZ, relativeY)) count++;
                }
            }
        }
        return count;
    }

    private static PassageStrengthCounts countPassageStrengths(
            MiteUnderworldStrata.StrataPlan plan, int stratum) {
        int coreColumns = 0;
        int transitionColumns = 0;
        for (int localX = 0; localX < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localX++) {
            for (int localZ = 0; localZ < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localZ++) {
                double strength = plan.passageStrengthAt(stratum, localX, localZ);
                if (strength == 1.0) {
                    coreColumns++;
                } else if (strength > 0.0) {
                    transitionColumns++;
                }
            }
        }
        return new PassageStrengthCounts(coreColumns, transitionColumns);
    }

    private static int assertPassageColumnsTraverseBand(
            MiteUnderworldStrata.StrataPlan plan,
            ChunkPos chunkPos,
            int center,
            int minimumY,
            int maximumY) {
        int gapCount = 0;
        for (int localX = 0; localX < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localX++) {
            for (int localZ = 0; localZ < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localZ++) {
                if (plan.hasBedrockAt(localX, localZ, center)) continue;
                gapCount++;
                for (int relativeY = minimumY; relativeY < maximumY; relativeY++) {
                    assertFalse(
                            plan.hasBedrockAt(localX, localZ, relativeY),
                            "passage blocked at chunk " + chunkPos.x() + "," + chunkPos.z() + " column "
                                    + localX + "," + localZ + " relative Y=" + relativeY);
                }
            }
        }
        return gapCount;
    }

    private record PassageStrengthCounts(int coreColumns, int transitionColumns) {}
}
