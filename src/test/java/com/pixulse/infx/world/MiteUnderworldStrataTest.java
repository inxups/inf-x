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
    void internalBedrockCentersKeepOneSeededTwoByTwoPassagePerChunk() {
        int[] centers = {32, 72, 96};
        int[][] ranges = {{3, 52}, {52, 84}, {84, 120}};

        for (int chunkX = -2; chunkX <= 2; chunkX++) {
            for (int chunkZ = -2; chunkZ <= 2; chunkZ++) {
                MiteUnderworldStrata.StrataPlan plan = MiteUnderworldStrata.plan(
                        WORLD_SEED,
                        new ChunkPos(chunkX, chunkZ));
                Passage[] passages = new Passage[centers.length];
                for (int stratum = 0; stratum < centers.length; stratum++) {
                    passages[stratum] = assertTwoByTwoPassage(
                            plan,
                            new ChunkPos(chunkX, chunkZ),
                            centers[stratum],
                            ranges[stratum][0],
                            ranges[stratum][1]);
                }
                for (int first = 0; first < passages.length; first++) {
                    for (int second = first + 1; second < passages.length; second++) {
                        assertFalse(
                                passages[first].overlaps(passages[second]),
                                "strata passages must not overlap in chunk " + chunkX + "," + chunkZ);
                    }
                }
            }
        }
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

    private static Passage assertTwoByTwoPassage(
            MiteUnderworldStrata.StrataPlan plan,
            ChunkPos chunkPos,
            int center,
            int minimumY,
            int maximumY) {
        int gapCount = 0;
        int minimumX = MiteUnderworldStrata.CHUNK_SIDE_LENGTH;
        int minimumZ = MiteUnderworldStrata.CHUNK_SIDE_LENGTH;
        int maximumX = -1;
        int maximumZ = -1;
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
                minimumX = Math.min(minimumX, localX);
                minimumZ = Math.min(minimumZ, localZ);
                maximumX = Math.max(maximumX, localX);
                maximumZ = Math.max(maximumZ, localZ);
            }
        }

        String location = "chunk " + chunkPos.x() + "," + chunkPos.z() + " relative Y=" + center;
        int observedGapCount = gapCount;
        int passageWidth = maximumX - minimumX;
        int passageDepth = maximumZ - minimumZ;
        assertAll(
                () -> assertEquals(4, observedGapCount, location + " must contain four passage columns"),
                () -> assertEquals(1, passageWidth, location + " passage must be two blocks wide"),
                () -> assertEquals(1, passageDepth, location + " passage must be two blocks deep"));
        return new Passage(minimumX, minimumZ);
    }

    private record Passage(int minimumX, int minimumZ) {
        private boolean overlaps(Passage other) {
            return this.minimumX < other.minimumX + 2
                    && this.minimumX + 2 > other.minimumX
                    && this.minimumZ < other.minimumZ + 2
                    && this.minimumZ + 2 > other.minimumZ;
        }
    }
}
