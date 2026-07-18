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
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, InfiniteX.id("underworld"));
    public static final ResourceKey<LevelStem> STEM = ResourceKey.create(Registries.LEVEL_STEM, InfiniteX.id("underworld"));
    public static final ResourceKey<DimensionType> TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, InfiniteX.id("underworld"));
    public static final ResourceKey<Biome> BIOME = ResourceKey.create(Registries.BIOME, InfiniteX.id("underworld"));
    public static final ResourceKey<NoiseGeneratorSettings> NOISE =
            ResourceKey.create(Registries.NOISE_SETTINGS, InfiniteX.id("underworld"));

    private Underworld() {}
}
