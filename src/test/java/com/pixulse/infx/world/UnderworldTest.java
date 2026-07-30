package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.InfiniteX;
import org.junit.jupiter.api.Test;

class UnderworldTest {
    @Test
    void verticalRangeSpansTheDeepslateBaseAndShiftedMiteTerrain() {
        assertEquals(-128, Underworld.MIN_Y);
        assertEquals(256, Underworld.MAX_Y_EXCLUSIVE);
        assertEquals(384, Underworld.HEIGHT);
        assertEquals(120, Underworld.DEEPSLATE_MAX_Y_EXCLUSIVE);
        assertEquals(120, Underworld.TERRAIN_MIN_Y);
        assertEquals(248, Underworld.TERRAIN_MAX_Y_EXCLUSIVE);
        assertEquals(128, Underworld.TERRAIN_HEIGHT);
        assertEquals(144, Underworld.WATER_LEVEL);
        assertEquals(5, Underworld.BOUNDARY_MAX_THICKNESS);
    }

    @Test
    void allUnderworldRegistryKeysUseTheStableId() {
        assertEquals(InfiniteX.id("underworld"), Underworld.LEVEL.identifier());
        assertEquals(InfiniteX.id("underworld"), Underworld.STEM.identifier());
        assertEquals(InfiniteX.id("underworld"), Underworld.TYPE.identifier());
        assertEquals(InfiniteX.id("underworld"), Underworld.BIOME.identifier());
        assertEquals(InfiniteX.id("underworld"), Underworld.NOISE.identifier());
    }
}
