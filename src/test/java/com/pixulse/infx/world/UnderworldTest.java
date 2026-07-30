package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.InfiniteX;
import org.junit.jupiter.api.Test;

class UnderworldTest {
    @Test
    void verticalRangeEndsAtTheConfiguredExclusiveUpperBoundary() {
        assertEquals(-128, Underworld.MIN_Y);
        assertEquals(192, Underworld.MAX_Y_EXCLUSIVE);
        assertEquals(320, Underworld.HEIGHT);
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
