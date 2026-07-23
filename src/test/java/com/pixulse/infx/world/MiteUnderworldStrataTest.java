package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
    void lowerStrataKeepMiteMantleAndAllFourIrregularBedrockBands() {
        MiteUnderworldStrata.StrataPlan plan = MiteUnderworldStrata.plan(WORLD_SEED, CHUNK_POS);
        int[] bandCounts = new int[4];
        for (int chunkX = -2; chunkX <= 2; chunkX++) {
            for (int chunkZ = -2; chunkZ <= 2; chunkZ++) {
                MiteUnderworldStrata.StrataPlan sampledPlan = MiteUnderworldStrata.plan(
                        WORLD_SEED,
                        new ChunkPos(chunkX, chunkZ));
                bandCounts[0] += countBedrock(sampledPlan, 0, 24);
                bandCounts[1] += countBedrock(sampledPlan, 24, 48);
                bandCounts[2] += countBedrock(sampledPlan, 64, 88);
                bandCounts[3] += countBedrock(sampledPlan, 88, 112);
            }
        }

        for (int localX = 0; localX < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localX++) {
            for (int localZ = 0; localZ < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localZ++) {
                int thickness = plan.boundaryThicknessAt(localX, localZ);
                for (int relativeY = 0; relativeY < thickness; relativeY++) {
                    assertTrue(plan.hasMantleAt(localX, localZ, relativeY));
                }
                assertFalse(plan.hasMantleAt(localX, localZ, thickness));
                assertFalse(plan.hasMantleAt(localX, localZ, -1));
                assertTrue(thickness >= 1 && thickness <= 3);
            }
        }

        int cellsPerBand = 25 * 24 * 16 * 16;
        assertAll(
                () -> assertTrue(bandCounts[0] > 0),
                () -> assertTrue(bandCounts[1] > 0),
                () -> assertTrue(bandCounts[2] > 0),
                () -> assertTrue(bandCounts[3] > 0),
                () -> assertTrue(bandCounts[0] < cellsPerBand),
                () -> assertTrue(bandCounts[1] < cellsPerBand),
                () -> assertTrue(bandCounts[2] < cellsPerBand),
                () -> assertTrue(bandCounts[3] < cellsPerBand));
    }

    @Test
    void samplingIsStableAcrossRepeatedAndParallelCalls() {
        List<ChunkPos> chunks = List.of(
                new ChunkPos(-19, -7),
                new ChunkPos(-2, 3),
                new ChunkPos(0, 0),
                new ChunkPos(7, -11),
                new ChunkPos(23, 17));
        List<Integer> expected = chunks.stream()
                .map(chunk -> signature(MiteUnderworldStrata.plan(WORLD_SEED, chunk)))
                .toList();
        List<Integer> parallel = chunks.parallelStream()
                .map(chunk -> signature(MiteUnderworldStrata.plan(WORLD_SEED, chunk)))
                .toList();

        assertEquals(expected, parallel);
        assertTrue(expected.stream().distinct().count() > 1, "different chunks must not reuse one strata field");
    }

    @Test
    void legacyNoiseMatchesReferenceCoverageAcrossSeedsAndChunks() {
        List<StrataProfile> profiles = List.of(
                sampleGrid(WORLD_SEED),
                sampleGrid(0L),
                sampleGrid(-1L),
                sampleGrid(0x1234_5678_9ABCL));

        assertEquals(
                List.of(
                        new StrataProfile(2173, 2089, 2138, 1046, 29983, 38006, 32754, 1651998336),
                        new StrataProfile(2139, 2188, 2073, 86299, 4483, 83113, 978, 626797791),
                        new StrataProfile(2156, 2151, 2093, 104832, 56503, 32662, 3219, -1026987722),
                        new StrataProfile(2100, 2135, 2165, 19268, 18636, 9290, 72875, -330995834)),
                profiles);
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

    private static int signature(MiteUnderworldStrata.StrataPlan plan) {
        int signature = 1;
        for (int localX = 0; localX < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localX++) {
            for (int localZ = 0; localZ < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localZ++) {
                for (int relativeY = 0; relativeY < MiteUnderworldStrata.LEGACY_TERRAIN_START_Y; relativeY++) {
                    int replacement = plan.hasMantleAt(localX, localZ, relativeY)
                            ? 1
                            : plan.hasBedrockAt(localX, localZ, relativeY) ? 2 : 0;
                    signature = 31 * signature + replacement;
                }
            }
        }
        return signature;
    }

    private static StrataProfile sampleGrid(long worldSeed) {
        int oneBlockBoundaries = 0;
        int twoBlockBoundaries = 0;
        int threeBlockBoundaries = 0;
        int[] bandCounts = new int[4];
        int signature = 1;

        for (int chunkX = -2; chunkX <= 2; chunkX++) {
            for (int chunkZ = -2; chunkZ <= 2; chunkZ++) {
                MiteUnderworldStrata.StrataPlan plan = MiteUnderworldStrata.plan(
                        worldSeed,
                        new ChunkPos(chunkX, chunkZ));
                for (int localX = 0; localX < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localX++) {
                    for (int localZ = 0; localZ < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localZ++) {
                        switch (plan.boundaryThicknessAt(localX, localZ)) {
                            case 1 -> oneBlockBoundaries++;
                            case 2 -> twoBlockBoundaries++;
                            case 3 -> threeBlockBoundaries++;
                            default -> throw new AssertionError("Unexpected boundary thickness");
                        }
                        for (int relativeY = 0;
                                relativeY < MiteUnderworldStrata.LEGACY_TERRAIN_START_Y;
                                relativeY++) {
                            int replacement = plan.hasMantleAt(localX, localZ, relativeY)
                                    ? 1
                                    : plan.hasBedrockAt(localX, localZ, relativeY) ? 2 : 0;
                            signature = 31 * signature + replacement;
                            if (replacement != 2) continue;
                            if (relativeY < 24) bandCounts[0]++;
                            else if (relativeY < 48) bandCounts[1]++;
                            else if (relativeY >= 64 && relativeY < 88) bandCounts[2]++;
                            else if (relativeY >= 88 && relativeY < 112) bandCounts[3]++;
                        }
                    }
                }
            }
        }

        return new StrataProfile(
                oneBlockBoundaries,
                twoBlockBoundaries,
                threeBlockBoundaries,
                bandCounts[0],
                bandCounts[1],
                bandCounts[2],
                bandCounts[3],
                signature);
    }

    private record StrataProfile(
            int oneBlockBoundaries,
            int twoBlockBoundaries,
            int threeBlockBoundaries,
            int foundationCoverage,
            int firstSheetCoverage,
            int secondSheetCoverage,
            int thirdSheetCoverage,
            int signature) {}
}
