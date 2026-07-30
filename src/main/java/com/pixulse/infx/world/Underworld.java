package com.pixulse.infx.world;

import com.pixulse.infx.InfiniteX;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public final class Underworld {
    public static final int MIN_Y = 0;
    public static final int MAX_Y_EXCLUSIVE = 256;
    public static final int HEIGHT = MAX_Y_EXCLUSIVE - MIN_Y;
    public static final int TERRAIN_MIN_Y = 120;
    public static final int TERRAIN_MAX_Y_EXCLUSIVE = 248;
    public static final int TERRAIN_HEIGHT = TERRAIN_MAX_Y_EXCLUSIVE - TERRAIN_MIN_Y;
    public static final int WATER_LEVEL = 144;
    public static final int BOUNDARY_MAX_THICKNESS = 5;
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, InfiniteX.id("underworld"));
    public static final ResourceKey<LevelStem> STEM = ResourceKey.create(Registries.LEVEL_STEM, InfiniteX.id("underworld"));
    public static final ResourceKey<DimensionType> TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, InfiniteX.id("underworld"));
    public static final ResourceKey<Biome> BIOME = ResourceKey.create(Registries.BIOME, InfiniteX.id("underworld"));
    public static final ResourceKey<NoiseGeneratorSettings> NOISE = ResourceKey.create(Registries.NOISE_SETTINGS, InfiniteX.id("underworld"));

    private Underworld() {
    }
}
