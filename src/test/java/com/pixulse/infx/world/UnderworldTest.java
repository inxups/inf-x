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
        assertEquals(70, Underworld.LIQUID_SOURCE_ATTEMPTS_PER_CHUNK);
        assertEquals(4, Underworld.BROWN_MUSHROOM_CHANCE);
        assertEquals(16, Underworld.MYCELIUM_POST_CHANCE);
        assertEquals(24, Underworld.MYCELIUM_POST_RADIUS);
        assertEquals(2, Underworld.MYCELIUM_POST_SEARCH_CHUNK_RANGE);
        assertEquals(5, Underworld.BOUNDARY_MAX_THICKNESS);
        assertEquals(-128, Underworld.INTERNAL_BEDROCK_MIN_Y);
        assertEquals(120, Underworld.INTERNAL_BEDROCK_MAX_Y_EXCLUSIVE);
        assertEquals(-120, Underworld.BEDROCK_STRATUM_ONE_CENTER_Y);
        assertEquals(-72, Underworld.BEDROCK_STRATUM_TWO_CENTER_Y);
        assertEquals(-16, Underworld.BEDROCK_STRATUM_THREE_CENTER_Y);
        assertEquals(99, Underworld.BEDROCK_STRATUM_FOUR_CENTER_Y);
    }

    @Test
    void allUnderworldRegistryKeysUseTheStableId() {
        assertEquals(InfiniteX.id("underworld"), Underworld.LEVEL.identifier());
        assertEquals(InfiniteX.id("underworld"), Underworld.STEM.identifier());
        assertEquals(InfiniteX.id("underworld"), Underworld.TYPE.identifier());
        assertEquals(InfiniteX.id("underworld"), Underworld.BIOME.identifier());
        assertEquals(InfiniteX.id("underworld"), Underworld.NOISE.identifier());
        assertEquals(InfiniteX.id("chests/underworld_dungeon"), Underworld.DUNGEON_LOOT.identifier());
    }
}
