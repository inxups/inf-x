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
    private static final int UNDERWORLD_MIN_Y = Underworld.MIN_Y;

    @Test
    void fixedFiveBlockBoundaryUsesTheConfiguredUnderworldRange() {
        int topY = Underworld.MAX_Y_EXCLUSIVE - 1;
        assertAll(
                () -> assertEquals(320, Underworld.HEIGHT),
                () -> assertEquals(192, Underworld.MAX_Y_EXCLUSIVE),
                () -> assertEquals(5, MiteUnderworldStrata.BOUNDARY_BEDROCK_THICKNESS),
                () -> assertTrue(MiteUnderworldStrata.isBoundaryBedrock(UNDERWORLD_MIN_Y, Underworld.HEIGHT, -128)),
                () -> assertTrue(MiteUnderworldStrata.isBoundaryBedrock(UNDERWORLD_MIN_Y, Underworld.HEIGHT, -124)),
                () -> assertFalse(MiteUnderworldStrata.isBoundaryBedrock(UNDERWORLD_MIN_Y, Underworld.HEIGHT, -123)),
                () -> assertFalse(MiteUnderworldStrata.isBoundaryBedrock(UNDERWORLD_MIN_Y, Underworld.HEIGHT, 186)),
                () -> assertTrue(MiteUnderworldStrata.isBoundaryBedrock(UNDERWORLD_MIN_Y, Underworld.HEIGHT, 187)),
                () -> assertTrue(MiteUnderworldStrata.isBoundaryBedrock(UNDERWORLD_MIN_Y, Underworld.HEIGHT, topY)),
                () -> assertFalse(MiteUnderworldStrata.isBoundaryBedrock(UNDERWORLD_MIN_Y, Underworld.HEIGHT, 192)));
    }

    @Test
    void strataAnchorsMoveWithTheNewUnderworldFloor() {
        assertAll(
                () -> assertEquals(0, MiteUnderworldStrata.FOUNDATION_WORLD_Y),
                () -> assertEquals(-96, UNDERWORLD_MIN_Y + MiteUnderworldStrata.FIRST_SHEET_ANCHOR_Y),
                () -> assertEquals(-56, UNDERWORLD_MIN_Y + MiteUnderworldStrata.SECOND_SHEET_ANCHOR_Y),
                () -> assertEquals(-32, UNDERWORLD_MIN_Y + MiteUnderworldStrata.THIRD_SHEET_ANCHOR_Y));
    }

    @Test
    void foundationStartsAtAbsoluteZeroAndNeverFillsTheOldFloor() {
        MiteUnderworldStrata.StrataPlan plan = MiteUnderworldStrata.plan(WORLD_SEED, CHUNK_POS);
        int foundationColumns = 0;

        for (int localX = 0; localX < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localX++) {
            for (int localZ = 0; localZ < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localZ++) {
                int foundationHeight = plan.foundationHeightAt(localX, localZ);
                assertFalse(plan.hasFoundationAt(localX, localZ, -1));
                assertFalse(plan.hasFoundationAt(localX, localZ, UNDERWORLD_MIN_Y));
                assertEquals(foundationHeight > 0, plan.hasFoundationAt(localX, localZ, 0));
                assertFalse(plan.hasFoundationAt(localX, localZ, foundationHeight));
                if (foundationHeight > 0) {
                    foundationColumns++;
                    assertTrue(plan.hasFoundationAt(localX, localZ, foundationHeight - 1));
                }
            }
        }

        assertTrue(foundationColumns > 0, "the sampled chunk must exercise the moved foundation");
    }

    @Test
    void lowerStrataKeepAllThreeIrregularMiteBedrockBands() {
        MiteUnderworldStrata.StrataPlan plan = MiteUnderworldStrata.plan(WORLD_SEED, CHUNK_POS);
        int[] sheetCounts = new int[3];
        for (int chunkX = -2; chunkX <= 2; chunkX++) {
            for (int chunkZ = -2; chunkZ <= 2; chunkZ++) {
                MiteUnderworldStrata.StrataPlan sampledPlan = MiteUnderworldStrata.plan(
                        WORLD_SEED,
                        new ChunkPos(chunkX, chunkZ));
                sheetCounts[0] += countBedrock(sampledPlan, 24, 48);
                sheetCounts[1] += countBedrock(sampledPlan, 64, 88);
                sheetCounts[2] += countBedrock(sampledPlan, 88, 112);
            }
        }

        int cellsPerBand = 25 * 24 * 16 * 16;
        assertAll(
                () -> assertTrue(sheetCounts[0] > 0),
                () -> assertTrue(sheetCounts[1] > 0),
                () -> assertTrue(sheetCounts[2] > 0),
                () -> assertTrue(sheetCounts[0] < cellsPerBand),
                () -> assertTrue(sheetCounts[1] < cellsPerBand),
                () -> assertTrue(sheetCounts[2] < cellsPerBand));
    }

    @Test
    void internalSheetsKeepAContinuousSeededCenterCore() {
        int[] centers = {
            MiteUnderworldStrata.FIRST_SHEET_ANCHOR_Y,
            MiteUnderworldStrata.SECOND_SHEET_ANCHOR_Y,
            MiteUnderworldStrata.THIRD_SHEET_ANCHOR_Y
        };

        for (int chunkX = -2; chunkX <= 2; chunkX++) {
            for (int chunkZ = -2; chunkZ <= 2; chunkZ++) {
                MiteUnderworldStrata.StrataPlan plan = MiteUnderworldStrata.plan(
                        WORLD_SEED,
                        new ChunkPos(chunkX, chunkZ));
                for (int localX = 0; localX < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localX++) {
                    for (int localZ = 0; localZ < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localZ++) {
                        for (int center : centers) {
                            assertTrue(
                                    plan.hasBedrockAt(localX, localZ, center),
                                    "missing internal sheet center at chunk " + chunkX + "," + chunkZ
                                            + " column " + localX + "," + localZ + " relative Y=" + center);
                        }
                    }
                }
            }
        }
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
    void movedFoundationAndInternalSheetsMatchReferenceCoverageAcrossSeedsAndChunks() {
        List<StrataProfile> profiles = List.of(
                sampleGrid(WORLD_SEED),
                sampleGrid(0L),
                sampleGrid(-1L),
                sampleGrid(0x1234_5678_9ABCL));

        assertEquals(
                List.of(
                        new StrataProfile(761, -1603477306, 31289, 40026, 35126, -1424677138),
                        new StrataProfile(80015, -1817601626, 9528, 83438, 6983, 1645480110),
                        new StrataProfile(136900, 1214704297, 22048, 34293, 8388, -1366305652),
                        new StrataProfile(16964, 1470626957, 22626, 13841, 73162, 1998106787)),
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
                signature = 31 * signature + plan.foundationHeightAt(localX, localZ);
                for (int relativeY = 0; relativeY < MiteUnderworldStrata.LEGACY_TERRAIN_START_Y; relativeY++) {
                    int replacement = plan.hasBedrockAt(localX, localZ, relativeY) ? 1 : 0;
                    signature = 31 * signature + replacement;
                }
            }
        }
        return signature;
    }

    private static StrataProfile sampleGrid(long worldSeed) {
        int foundationBlockCount = 0;
        int foundationHeightSignature = 1;
        int[] sheetCounts = new int[3];
        int signature = 1;

        for (int chunkX = -2; chunkX <= 2; chunkX++) {
            for (int chunkZ = -2; chunkZ <= 2; chunkZ++) {
                MiteUnderworldStrata.StrataPlan plan = MiteUnderworldStrata.plan(
                        worldSeed,
                        new ChunkPos(chunkX, chunkZ));
                for (int localX = 0; localX < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localX++) {
                    for (int localZ = 0; localZ < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localZ++) {
                        int foundationHeight = plan.foundationHeightAt(localX, localZ);
                        foundationBlockCount += foundationHeight;
                        foundationHeightSignature = 31 * foundationHeightSignature + foundationHeight;
                        signature = 31 * signature + foundationHeight;
                        for (int relativeY = 0;
                                relativeY < MiteUnderworldStrata.LEGACY_TERRAIN_START_Y;
                                relativeY++) {
                            int replacement = plan.hasBedrockAt(localX, localZ, relativeY) ? 1 : 0;
                            signature = 31 * signature + replacement;
                            if (replacement != 1) continue;
                            if (relativeY >= 24 && relativeY < 48) sheetCounts[0]++;
                            else if (relativeY >= 64 && relativeY < 88) sheetCounts[1]++;
                            else if (relativeY >= 88 && relativeY < 112) sheetCounts[2]++;
                        }
                    }
                }
            }
        }

        return new StrataProfile(
                foundationBlockCount,
                foundationHeightSignature,
                sheetCounts[0],
                sheetCounts[1],
                sheetCounts[2],
                signature);
    }

    private record StrataProfile(
            int foundationBlockCount,
            int foundationHeightSignature,
            int firstSheetCoverage,
            int secondSheetCoverage,
            int thirdSheetCoverage,
            int signature) {}
}
