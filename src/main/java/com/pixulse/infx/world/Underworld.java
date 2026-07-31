package com.pixulse.infx.world;

import com.pixulse.infx.InfiniteX;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.storage.loot.LootTable;

public final class Underworld {
    public static final int MIN_Y = -128;
    public static final int MAX_Y_EXCLUSIVE = 256;
    public static final int HEIGHT = MAX_Y_EXCLUSIVE - MIN_Y;
    public static final int DEEPSLATE_MAX_Y_EXCLUSIVE = 120;
    public static final int TERRAIN_MIN_Y = 120;
    public static final int TERRAIN_MAX_Y_EXCLUSIVE = 248;
    public static final int TERRAIN_HEIGHT = TERRAIN_MAX_Y_EXCLUSIVE - TERRAIN_MIN_Y;
    public static final int WATER_LEVEL = 144;
    public static final int LIQUID_SOURCE_ATTEMPTS_PER_CHUNK = 70;
    public static final int BROWN_MUSHROOM_CHANCE = 4;
    public static final int MYCELIUM_POST_CHANCE = 16;
    public static final int MYCELIUM_POST_RADIUS = 24;
    public static final int MYCELIUM_POST_SEARCH_CHUNK_RANGE = (MYCELIUM_POST_RADIUS - 1) / 16 + 1;
    public static final int BOUNDARY_MAX_THICKNESS = 5;
    public static final int INTERNAL_BEDROCK_MIN_Y = MIN_Y;
    public static final int INTERNAL_BEDROCK_MAX_Y_EXCLUSIVE = DEEPSLATE_MAX_Y_EXCLUSIVE;
    public static final int BEDROCK_STRATUM_ONE_CENTER_Y = -120;
    public static final int BEDROCK_STRATUM_TWO_CENTER_Y = -72;
    public static final int BEDROCK_STRATUM_THREE_CENTER_Y = -16;
    public static final int BEDROCK_STRATUM_FOUR_CENTER_Y = 99;
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, InfiniteX.id("underworld"));
    public static final ResourceKey<LevelStem> STEM = ResourceKey.create(Registries.LEVEL_STEM, InfiniteX.id("underworld"));
    public static final ResourceKey<DimensionType> TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, InfiniteX.id("underworld"));
    public static final ResourceKey<Biome> BIOME = ResourceKey.create(Registries.BIOME, InfiniteX.id("underworld"));
    public static final ResourceKey<NoiseGeneratorSettings> NOISE = ResourceKey.create(Registries.NOISE_SETTINGS, InfiniteX.id("underworld"));
    public static final ResourceKey<LootTable> DUNGEON_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE, InfiniteX.id("chests/underworld_dungeon"));

    private Underworld() {
    }
}
