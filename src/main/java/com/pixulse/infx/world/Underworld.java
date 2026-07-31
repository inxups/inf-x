package com.pixulse.infx.world;

import com.pixulse.infx.InfiniteX;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.storage.loot.LootTable;

public final class Underworld {
    public static final int MIN_Y = -128;
    public static final int MAX_Y_EXCLUSIVE = 256;
    public static final int HEIGHT = MAX_Y_EXCLUSIVE - MIN_Y;
    public static final int DEEPSLATE_MAX_Y_EXCLUSIVE = 120;
    public static final int TERRAIN_MIN_Y = 120;
    public static final int TERRAIN_MAX_Y_EXCLUSIVE = 248;
    public static final int TERRAIN_HEIGHT = TERRAIN_MAX_Y_EXCLUSIVE - TERRAIN_MIN_Y;
    public static final int WATER_MIN_Y = 100;
    public static final int WATER_LEVEL = 144;
    public static final int LUSH_CAVES_MIN_Y = WATER_LEVEL;
    public static final int LUSH_CAVES_MAX_Y_INCLUSIVE = TERRAIN_MAX_Y_EXCLUSIVE - 1;
    public static final int LUSH_CAVES_FLOOR_SCAN_MIN_Y = LUSH_CAVES_MIN_Y + 12;
    public static final int LIQUID_SOURCE_ATTEMPTS_PER_CHUNK = 70;
    public static final int BROWN_MUSHROOM_CHANCE = 4;
    public static final int MYCELIUM_POST_CHANCE = 16;
    public static final int MYCELIUM_POST_RADIUS = 24;
    public static final int MYCELIUM_POST_SEARCH_CHUNK_RANGE = (MYCELIUM_POST_RADIUS - 1) / 16 + 1;
    public static final int LARGE_CAVE_MIN_Y = 0;
    public static final int LARGE_CAVE_MAX_Y = 80;
    public static final int LARGE_CAVE_CENTER_Y = 30;
    public static final int LARGE_CAVE_MAIN_RADIUS = 106;
    public static final int LARGE_CAVE_MAIN_VERTICAL_RADIUS = 50;
    public static final int LARGE_CAVE_OUTER_RADIUS = 124;
    public static final int LARGE_CAVE_STRUCTURE_SCAN_CHUNK_RANGE =
            (LARGE_CAVE_OUTER_RADIUS + 8) / 16;
    public static final int ANCIENT_CITY_CENTER_ANCHOR_LOCAL_Y = 24;
    public static final int ANCIENT_CITY_START_Y =
            LARGE_CAVE_MIN_Y + ANCIENT_CITY_CENTER_ANCHOR_LOCAL_Y;
    public static final int LARGE_CAVE_INTERNAL_BEDROCK_MIN_Y = -24;
    public static final int LARGE_CAVE_INTERNAL_BEDROCK_MAX_Y_EXCLUSIVE = -8;
    public static final int BOUNDARY_MAX_THICKNESS = 5;
    public static final int INTERNAL_BEDROCK_MIN_Y = MIN_Y;
    public static final int INTERNAL_BEDROCK_MAX_Y_EXCLUSIVE = DEEPSLATE_MAX_Y_EXCLUSIVE;
    public static final int BEDROCK_STRATUM_ONE_CENTER_Y = -120;
    public static final int BEDROCK_STRATUM_TWO_CENTER_Y = -72;
    public static final int BEDROCK_STRATUM_THREE_CENTER_Y = -16;
    public static final int BEDROCK_STRATUM_FOUR_CENTER_Y = 99;
    public static final int ORE_LOW_MAX_Y_INCLUSIVE = 135;
    public static final int ORE_MAX_Y_INCLUSIVE = TERRAIN_MAX_Y_EXCLUSIVE - 1;
    public static final int COPPER_ORE_LOW_COUNT = 24;
    public static final int COPPER_ORE_FULL_COUNT = 8;
    public static final int COPPER_ORE_SIZE = 6;
    public static final int SILVER_ORE_LOW_COUNT = 6;
    public static final int SILVER_ORE_FULL_COUNT = 2;
    public static final int SILVER_ORE_SIZE = 6;
    public static final int GOLD_ORE_LOW_COUNT = 12;
    public static final int GOLD_ORE_FULL_COUNT = 4;
    public static final int GOLD_ORE_SIZE = 4;
    public static final int IRON_ORE_LOW_COUNT = 36;
    public static final int IRON_ORE_FULL_COUNT = 12;
    public static final int IRON_ORE_SIZE = 6;
    public static final int MITHRIL_ORE_LOW_COUNT = 6;
    public static final int MITHRIL_ORE_FULL_COUNT = 2;
    public static final int MITHRIL_ORE_SIZE = 3;
    public static final int ADAMANTIUM_ORE_LOW_COUNT = 8;
    public static final int ADAMANTIUM_ORE_SIZE = 3;
    public static final int REDSTONE_ORE_LOW_COUNT = 6;
    public static final int REDSTONE_ORE_FULL_COUNT = 2;
    public static final int REDSTONE_ORE_SIZE = 5;
    public static final int DIAMOND_ORE_LOW_COUNT = 3;
    public static final int DIAMOND_ORE_FULL_COUNT = 1;
    public static final int DIAMOND_ORE_SIZE = 3;
    public static final int LAPIS_ORE_LOW_COUNT = 3;
    public static final int LAPIS_ORE_FULL_COUNT = 1;
    public static final int LAPIS_ORE_SIZE = 3;
    public static final int SILVERFISH_LOW_COUNT = 0;
    public static final int SILVERFISH_FULL_COUNT = 40;
    public static final int SILVERFISH_SIZE = 3;
    public static final int GRAVEL_LOW_COUNT = 0;
    public static final int GRAVEL_FULL_COUNT = 30;
    public static final int GRAVEL_SIZE = 32;
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, InfiniteX.id("underworld"));
    public static final ResourceKey<LevelStem> STEM = ResourceKey.create(Registries.LEVEL_STEM, InfiniteX.id("underworld"));
    public static final ResourceKey<DimensionType> TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, InfiniteX.id("underworld"));
    public static final ResourceKey<Biome> BIOME = ResourceKey.create(Registries.BIOME, InfiniteX.id("underworld"));
    public static final ResourceKey<Biome> LUSH_BIOME =
            ResourceKey.create(Registries.BIOME, InfiniteX.id("underworld_lush"));
    public static final ResourceKey<Biome> DEEP_DARK_BIOME =
            ResourceKey.create(Registries.BIOME, InfiniteX.id("underworld_deep_dark"));
    public static final ResourceKey<NoiseGeneratorSettings> NOISE = ResourceKey.create(Registries.NOISE_SETTINGS, InfiniteX.id("underworld"));
    public static final ResourceKey<Structure> ANCIENT_CITY =
            ResourceKey.create(Registries.STRUCTURE, InfiniteX.id("underworld_ancient_city"));
    public static final ResourceKey<StructureSet> ANCIENT_CITIES =
            ResourceKey.create(Registries.STRUCTURE_SET, InfiniteX.id("underworld_ancient_cities"));
    public static final ResourceKey<LootTable> DUNGEON_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE, InfiniteX.id("chests/underworld_dungeon"));

    private Underworld() {
    }
}
