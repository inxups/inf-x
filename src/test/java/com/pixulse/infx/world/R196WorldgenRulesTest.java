package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

class R196WorldgenRulesTest {
    @Test
    void largeCavesKeepDistanceProbabilityAndEnvelopeConstants() {
        assertEquals(200, R196LargeCaveCarver.CHANCE_DENOMINATOR);
        assertEquals(64, R196LargeCaveCarver.HORIZONTAL_SIZE);
        assertEquals(8, R196LargeCaveCarver.MIN_Y);
        assertEquals(55, R196LargeCaveCarver.MAX_Y);
        assertFalse(R196LargeCaveCarver.eligibleDistance(new ChunkPos(0, 0)));
        assertTrue(R196LargeCaveCarver.eligibleDistance(new ChunkPos(63, 0)));
        assertTrue(R196LargeCaveCarver.insideCellularCave(32, 32, 32, 0, 0, 1234L));
        assertFalse(R196LargeCaveCarver.insideCellularCave(64, 32, 32, 0, 0, 1234L));
    }

    @Test
    void riverClimateRoutingProducesAllThreeR196Variants() {
        assertEquals(R196RiverBiomes.DESERT_RIVER, R196RiverBiomes.select(.8F, .1F));
        assertEquals(R196RiverBiomes.JUNGLE_RIVER, R196RiverBiomes.select(.3F, .6F));
        assertEquals(R196RiverBiomes.SWAMP_RIVER, R196RiverBiomes.select(-.2F, .6F));
        assertNull(R196RiverBiomes.select(0.0F, 0.0F));
    }

    @Test
    void pyramidWallTorchesFaceFromEachChestTowardTheCenter() {
        BlockPos center = new BlockPos(10, 20, 10);
        assertEquals(Direction.EAST, R196StructureSafetyEvents.towardCenter(new BlockPos(8, 19, 10), center));
        assertEquals(Direction.WEST, R196StructureSafetyEvents.towardCenter(new BlockPos(12, 19, 10), center));
        assertEquals(Direction.SOUTH, R196StructureSafetyEvents.towardCenter(new BlockPos(10, 19, 8), center));
        assertEquals(Direction.NORTH, R196StructureSafetyEvents.towardCenter(new BlockPos(10, 19, 12), center));
    }
}
