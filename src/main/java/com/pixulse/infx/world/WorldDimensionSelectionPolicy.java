package com.pixulse.infx.world;

import java.util.Map;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

/** Resolves the effective datapack dimensions used when a dev-mode world is baked. */
public final class WorldDimensionSelectionPolicy {
    private WorldDimensionSelectionPolicy() {}

    public static Registry<LevelStem> resolve(
            Registry<LevelStem> datapackDimensions,
            Map<ResourceKey<LevelStem>, LevelStem> selectedDimensions,
            boolean devMode) {
        if (!devMode) return datapackDimensions;

        LevelStem selectedOverworld = selectedDimensions.get(LevelStem.OVERWORLD);
        LevelStem datapackOverworld = datapackDimensions.getValue(LevelStem.OVERWORLD);
        if (selectedOverworld == null || datapackOverworld == null || selectedOverworld == datapackOverworld) {
            return datapackDimensions;
        }

        LevelStem effectiveOverworld = preserveDatapackBiomeSource(datapackOverworld, selectedOverworld);
        MappedRegistry<LevelStem> resolved =
                new MappedRegistry<>(datapackDimensions.key(), datapackDimensions.registryLifecycle());
        datapackDimensions.listElements().forEach(holder -> resolved.register(
                holder.key(),
                holder.is(LevelStem.OVERWORLD) ? effectiveOverworld : holder.value(),
                datapackDimensions.registrationInfo(holder.key()).orElse(RegistrationInfo.BUILT_IN)));
        return resolved.freeze();
    }

    private static LevelStem preserveDatapackBiomeSource(LevelStem datapackOverworld, LevelStem selectedOverworld) {
        if (!(datapackOverworld.generator() instanceof NoiseBasedChunkGenerator datapackGenerator)
                || !(selectedOverworld.generator() instanceof NoiseBasedChunkGenerator selectedGenerator)
                || !(selectedGenerator.getBiomeSource() instanceof MultiNoiseBiomeSource selectedBiomeSource)
                || !selectedBiomeSource.stable(MultiNoiseBiomeSourceParameterLists.OVERWORLD)
                || !usesVanillaOverworldNoiseSettings(selectedGenerator)) {
            return selectedOverworld;
        }

        return new LevelStem(
                selectedOverworld.type(),
                new NoiseBasedChunkGenerator(datapackGenerator.getBiomeSource(), selectedGenerator.generatorSettings()),
                selectedOverworld.seedOverride());
    }

    private static boolean usesVanillaOverworldNoiseSettings(NoiseBasedChunkGenerator generator) {
        return generator.stable(NoiseGeneratorSettings.OVERWORLD)
                || generator.stable(NoiseGeneratorSettings.LARGE_BIOMES)
                || generator.stable(NoiseGeneratorSettings.AMPLIFIED);
    }
}
