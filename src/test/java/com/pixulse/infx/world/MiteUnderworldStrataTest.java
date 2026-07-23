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
    void lowerStrataOnlyKeepTheMiteMantle() {
        for (int chunkX = -2; chunkX <= 2; chunkX++) {
            for (int chunkZ = -2; chunkZ <= 2; chunkZ++) {
                MiteUnderworldStrata.StrataPlan plan = MiteUnderworldStrata.plan(
                        WORLD_SEED,
                        new ChunkPos(chunkX, chunkZ));
                for (int localX = 0; localX < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localX++) {
                    for (int localZ = 0; localZ < MiteUnderworldStrata.CHUNK_SIDE_LENGTH; localZ++) {
                        int thickness = plan.boundaryThicknessAt(localX, localZ);
                        for (int relativeY = 0; relativeY < 120; relativeY++) {
                            assertEquals(
                                    relativeY < thickness,
                                    plan.hasMantleAt(localX, localZ, relativeY),
                                    "only the bottom mantle may replace lower Underworld terrain");
                        }
                        assertFalse(plan.hasMantleAt(localX, localZ, -1));
                        assertTrue(thickness >= 1 && thickness <= 3);
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
}
