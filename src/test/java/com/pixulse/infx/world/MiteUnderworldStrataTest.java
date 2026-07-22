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
}
