package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals(100, Underworld.WATER_MIN_Y);
        assertTrue(Underworld.WATER_MIN_Y < Underworld.TERRAIN_MIN_Y);
        assertEquals(144, Underworld.WATER_LEVEL);
        assertEquals(70, Underworld.LIQUID_SOURCE_ATTEMPTS_PER_CHUNK);
        assertEquals(4, Underworld.BROWN_MUSHROOM_CHANCE);
        assertEquals(16, Underworld.MYCELIUM_POST_CHANCE);
        assertEquals(24, Underworld.MYCELIUM_POST_RADIUS);
        assertEquals(2, Underworld.MYCELIUM_POST_SEARCH_CHUNK_RANGE);
        assertEquals(-20, Underworld.LARGE_CAVE_MIN_Y);
        assertEquals(80, Underworld.LARGE_CAVE_MAX_Y);
        assertEquals(30, Underworld.LARGE_CAVE_CENTER_Y);
        assertEquals(106, Underworld.LARGE_CAVE_MAIN_RADIUS);
        assertEquals(50, Underworld.LARGE_CAVE_MAIN_VERTICAL_RADIUS);
        assertEquals(124, Underworld.LARGE_CAVE_OUTER_RADIUS);
        assertEquals(8, Underworld.LARGE_CAVE_STRUCTURE_SCAN_CHUNK_RANGE);
        assertEquals(24, Underworld.ANCIENT_CITY_CENTER_ANCHOR_LOCAL_Y);
        assertEquals(4, Underworld.ANCIENT_CITY_START_Y);
        assertEquals(
                Underworld.LARGE_CAVE_MIN_Y,
                Underworld.ANCIENT_CITY_START_Y - Underworld.ANCIENT_CITY_CENTER_ANCHOR_LOCAL_Y);
        assertEquals(-24, Underworld.LARGE_CAVE_INTERNAL_BEDROCK_MIN_Y);
        assertEquals(-8, Underworld.LARGE_CAVE_INTERNAL_BEDROCK_MAX_Y_EXCLUSIVE);
        assertEquals(5, Underworld.BOUNDARY_MAX_THICKNESS);
        assertEquals(-128, Underworld.INTERNAL_BEDROCK_MIN_Y);
        assertEquals(120, Underworld.INTERNAL_BEDROCK_MAX_Y_EXCLUSIVE);
        assertEquals(-120, Underworld.BEDROCK_STRATUM_ONE_CENTER_Y);
        assertEquals(-72, Underworld.BEDROCK_STRATUM_TWO_CENTER_Y);
        assertEquals(-16, Underworld.BEDROCK_STRATUM_THREE_CENTER_Y);
        assertEquals(99, Underworld.BEDROCK_STRATUM_FOUR_CENTER_Y);
        assertEquals(135, Underworld.ORE_LOW_MAX_Y_INCLUSIVE);
        assertEquals(247, Underworld.ORE_MAX_Y_INCLUSIVE);
        assertEquals(24, Underworld.COPPER_ORE_LOW_COUNT);
        assertEquals(8, Underworld.COPPER_ORE_FULL_COUNT);
        assertEquals(6, Underworld.COPPER_ORE_SIZE);
        assertEquals(6, Underworld.SILVER_ORE_LOW_COUNT);
        assertEquals(2, Underworld.SILVER_ORE_FULL_COUNT);
        assertEquals(6, Underworld.SILVER_ORE_SIZE);
        assertEquals(12, Underworld.GOLD_ORE_LOW_COUNT);
        assertEquals(4, Underworld.GOLD_ORE_FULL_COUNT);
        assertEquals(4, Underworld.GOLD_ORE_SIZE);
        assertEquals(36, Underworld.IRON_ORE_LOW_COUNT);
        assertEquals(12, Underworld.IRON_ORE_FULL_COUNT);
        assertEquals(6, Underworld.IRON_ORE_SIZE);
        assertEquals(6, Underworld.MITHRIL_ORE_LOW_COUNT);
        assertEquals(2, Underworld.MITHRIL_ORE_FULL_COUNT);
        assertEquals(3, Underworld.MITHRIL_ORE_SIZE);
        assertEquals(8, Underworld.ADAMANTIUM_ORE_LOW_COUNT);
        assertEquals(3, Underworld.ADAMANTIUM_ORE_SIZE);
        assertEquals(6, Underworld.REDSTONE_ORE_LOW_COUNT);
        assertEquals(2, Underworld.REDSTONE_ORE_FULL_COUNT);
        assertEquals(5, Underworld.REDSTONE_ORE_SIZE);
        assertEquals(3, Underworld.DIAMOND_ORE_LOW_COUNT);
        assertEquals(1, Underworld.DIAMOND_ORE_FULL_COUNT);
        assertEquals(3, Underworld.DIAMOND_ORE_SIZE);
        assertEquals(3, Underworld.LAPIS_ORE_LOW_COUNT);
        assertEquals(1, Underworld.LAPIS_ORE_FULL_COUNT);
        assertEquals(3, Underworld.LAPIS_ORE_SIZE);
        assertEquals(0, Underworld.SILVERFISH_LOW_COUNT);
        assertEquals(40, Underworld.SILVERFISH_FULL_COUNT);
        assertEquals(3, Underworld.SILVERFISH_SIZE);
        assertEquals(0, Underworld.GRAVEL_LOW_COUNT);
        assertEquals(30, Underworld.GRAVEL_FULL_COUNT);
        assertEquals(32, Underworld.GRAVEL_SIZE);
    }

    @Test
    void allUnderworldRegistryKeysUseTheStableId() {
        assertEquals(InfiniteX.id("underworld"), Underworld.LEVEL.identifier());
        assertEquals(InfiniteX.id("underworld"), Underworld.STEM.identifier());
        assertEquals(InfiniteX.id("underworld"), Underworld.TYPE.identifier());
        assertEquals(InfiniteX.id("underworld"), Underworld.BIOME.identifier());
        assertEquals(InfiniteX.id("underworld_lush"), Underworld.LUSH_BIOME.identifier());
        assertEquals(InfiniteX.id("underworld_deep_dark"), Underworld.DEEP_DARK_BIOME.identifier());
        assertEquals(InfiniteX.id("underworld"), Underworld.NOISE.identifier());
        assertEquals(InfiniteX.id("underworld_ancient_city"), Underworld.ANCIENT_CITY.identifier());
        assertEquals(InfiniteX.id("underworld_ancient_cities"), Underworld.ANCIENT_CITIES.identifier());
        assertEquals(InfiniteX.id("chests/underworld_dungeon"), Underworld.DUNGEON_LOOT.identifier());
    }
}
