package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.Lifecycle;
import com.pixulse.infx.InfiniteXDevMode;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.junit.jupiter.api.Test;

class WorldDimensionSelectionPolicyTest {
    @Test
    void normalModeLeavesDatapackDimensionsUntouched() {
        HolderLookup.Provider registries = VanillaRegistries.createLookup();
        LevelStem datapackOverworld = noiseStem(
                registries,
                new FixedBiomeSource(biome(registries, Biomes.PLAINS)),
                NoiseGeneratorSettings.OVERWORLD,
                OptionalLong.empty());
        Registry<LevelStem> datapackDimensions = datapackDimensions(
                datapackOverworld, debugStem(registries, Biomes.DESERT));

        Registry<LevelStem> resolved = WorldDimensionSelectionPolicy.resolve(
                datapackDimensions,
                Map.of(LevelStem.OVERWORLD, flatStem(registries)),
                false);

        assertSame(datapackDimensions, resolved);
        assertSame(datapackOverworld, resolved.getValue(LevelStem.OVERWORLD));
    }

    @Test
    void devModeUsesFlatAndDebugGeneratorsWithoutReplacingOtherDimensions() {
        HolderLookup.Provider registries = VanillaRegistries.createLookup();
        LevelStem datapackOverworld = noiseStem(
                registries,
                new FixedBiomeSource(biome(registries, Biomes.PLAINS)),
                NoiseGeneratorSettings.OVERWORLD,
                OptionalLong.empty());
        LevelStem underworld = debugStem(registries, Biomes.DESERT);
        Registry<LevelStem> datapackDimensions = datapackDimensions(datapackOverworld, underworld);

        for (LevelStem selected : List.of(flatStem(registries), debugStem(registries, Biomes.PLAINS))) {
            Registry<LevelStem> resolved = WorldDimensionSelectionPolicy.resolve(
                    datapackDimensions, Map.of(LevelStem.OVERWORLD, selected), true);

            assertNotSame(datapackDimensions, resolved);
            assertSame(selected, resolved.getValue(LevelStem.OVERWORLD));
            assertSame(underworld, resolved.getValue(Underworld.STEM));
        }
    }

    @Test
    void vanillaNoisePresetsKeepDatapackBiomeSourceAndSelectedNoiseSettings() {
        HolderLookup.Provider registries = VanillaRegistries.createLookup();
        FixedBiomeSource datapackBiomeSource = new FixedBiomeSource(biome(registries, Biomes.PLAINS));
        LevelStem datapackOverworld = noiseStem(
                registries, datapackBiomeSource, NoiseGeneratorSettings.OVERWORLD, OptionalLong.empty());
        LevelStem underworld = debugStem(registries, Biomes.DESERT);
        Registry<LevelStem> datapackDimensions = datapackDimensions(datapackOverworld, underworld);

        for (var settingsKey : List.of(
                NoiseGeneratorSettings.OVERWORLD,
                NoiseGeneratorSettings.LARGE_BIOMES,
                NoiseGeneratorSettings.AMPLIFIED)) {
            Holder<NoiseGeneratorSettings> selectedSettings =
                    registries.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(settingsKey);
            var selectedBiomeSource = MultiNoiseBiomeSource.createFromPreset(registries
                    .lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST)
                    .getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD));
            LevelStem selected = new LevelStem(
                    overworldType(registries),
                    new NoiseBasedChunkGenerator(selectedBiomeSource, selectedSettings),
                    OptionalLong.of(42L));

            Registry<LevelStem> resolved = WorldDimensionSelectionPolicy.resolve(
                    datapackDimensions, Map.of(LevelStem.OVERWORLD, selected), true);
            LevelStem effective = resolved.getValueOrThrow(LevelStem.OVERWORLD);
            NoiseBasedChunkGenerator effectiveGenerator =
                    assertInstanceOf(NoiseBasedChunkGenerator.class, effective.generator());

            assertSame(selected.type(), effective.type());
            assertEquals(OptionalLong.of(42L), effective.seedOverride());
            assertSame(datapackBiomeSource, effectiveGenerator.getBiomeSource());
            assertSame(selectedSettings, effectiveGenerator.generatorSettings());
            assertSame(underworld, resolved.getValue(Underworld.STEM));
        }
    }

    @Test
    void customNoiseBiomeSourceRemainsSelected() {
        HolderLookup.Provider registries = VanillaRegistries.createLookup();
        LevelStem datapackOverworld = noiseStem(
                registries,
                new FixedBiomeSource(biome(registries, Biomes.PLAINS)),
                NoiseGeneratorSettings.OVERWORLD,
                OptionalLong.empty());
        Registry<LevelStem> datapackDimensions = datapackDimensions(
                datapackOverworld, debugStem(registries, Biomes.DESERT));
        LevelStem custom = noiseStem(
                registries,
                new FixedBiomeSource(biome(registries, Biomes.DESERT)),
                NoiseGeneratorSettings.LARGE_BIOMES,
                OptionalLong.of(7L));

        Registry<LevelStem> resolved = WorldDimensionSelectionPolicy.resolve(
                datapackDimensions, Map.of(LevelStem.OVERWORLD, custom), true);

        assertSame(custom, resolved.getValue(LevelStem.OVERWORLD));
    }

    @Test
    void bakeUsesSelectedOverworldOnlyWhenDevModeIsEnabled() {
        HolderLookup.Provider registries = VanillaRegistries.createLookup();
        LevelStem datapackOverworld = noiseStem(
                registries,
                new FixedBiomeSource(biome(registries, Biomes.PLAINS)),
                NoiseGeneratorSettings.OVERWORLD,
                OptionalLong.empty());
        LevelStem underworld = debugStem(registries, Biomes.DESERT);
        Registry<LevelStem> datapackDimensions = datapackDimensions(datapackOverworld, underworld);
        LevelStem selectedFlat = flatStem(registries);

        WorldDimensions.Complete baked =
                new WorldDimensions(Map.of(LevelStem.OVERWORLD, selectedFlat)).bake(datapackDimensions);

        LevelStem expectedOverworld = InfiniteXDevMode.isServerEnabled() ? selectedFlat : datapackOverworld;
        assertSame(expectedOverworld, baked.dimensions().getValue(LevelStem.OVERWORLD));
        assertSame(underworld, baked.dimensions().getValue(Underworld.STEM));
        assertEquals(
                        InfiniteXDevMode.isServerEnabled()
                        ? PrimaryLevelData.SpecialWorldProperty.FLAT
                        : PrimaryLevelData.SpecialWorldProperty.NONE,
                baked.specialWorldProperty());
    }

    @Test
    void worldDimensionsMixinIsRegistered() throws Exception {
        try (var stream = WorldDimensionSelectionPolicyTest.class
                .getClassLoader()
                .getResourceAsStream("infx.mixins.json")) {
            assertTrue(stream != null);
            String mixins = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(mixins.contains("\"world.level.levelgen.WorldDimensionsMixin\""));
        }
    }

    private static Registry<LevelStem> datapackDimensions(LevelStem overworld, LevelStem underworld) {
        MappedRegistry<LevelStem> registry = new MappedRegistry<>(Registries.LEVEL_STEM, Lifecycle.stable());
        registry.register(LevelStem.OVERWORLD, overworld, RegistrationInfo.BUILT_IN);
        registry.register(Underworld.STEM, underworld, RegistrationInfo.BUILT_IN);
        return registry.freeze();
    }

    private static LevelStem flatStem(HolderLookup.Provider registries) {
        FlatLevelGeneratorSettings settings = FlatLevelGeneratorSettings.getDefault(
                registries.lookupOrThrow(Registries.BIOME),
                registries.lookupOrThrow(Registries.STRUCTURE_SET),
                registries.lookupOrThrow(Registries.PLACED_FEATURE));
        return new LevelStem(overworldType(registries), new FlatLevelSource(settings), OptionalLong.of(11L));
    }

    private static LevelStem debugStem(HolderLookup.Provider registries, net.minecraft.resources.ResourceKey<Biome> biome) {
        return new LevelStem(overworldType(registries), new DebugLevelSource(biome(registries, biome)));
    }

    private static LevelStem noiseStem(
            HolderLookup.Provider registries,
            net.minecraft.world.level.biome.BiomeSource biomeSource,
            net.minecraft.resources.ResourceKey<NoiseGeneratorSettings> settingsKey,
            OptionalLong seedOverride) {
        Holder<NoiseGeneratorSettings> settings =
                registries.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(settingsKey);
        return new LevelStem(
                overworldType(registries), new NoiseBasedChunkGenerator(biomeSource, settings), seedOverride);
    }

    private static Holder<DimensionType> overworldType(HolderLookup.Provider registries) {
        return registries
                .lookupOrThrow(Registries.DIMENSION_TYPE)
                .getOrThrow(BuiltinDimensionTypes.OVERWORLD);
    }

    private static Holder.Reference<Biome> biome(
            HolderLookup.Provider registries, net.minecraft.resources.ResourceKey<Biome> biome) {
        return registries.lookupOrThrow(Registries.BIOME).getOrThrow(biome);
    }
}
