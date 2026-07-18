package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.world.Underworld;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TimelineTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.BiasedToBottomHeight;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.timeline.Timeline;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

final class ModWorldGen {
    private static final int OVERWORLD_MIN_Y = -16;
    private static final int OVERWORLD_HEIGHT = 336;
    private static final int UNDERWORLD_MIN_Y = -192;
    private static final int UNDERWORLD_HEIGHT = 512;
    private static final int UNDERWORLD_STONE_CEILING = 128;
    private static final int UNDERWORLD_SEA_LEVEL = 140;
    private static final ResourceKey<ConfiguredFeature<?, ?>> SILVER_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("silver_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> MITHRIL_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("mithril_ore"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> ADAMANTIUM_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("underworld_adamantium_ore"));
    private static final ResourceKey<PlacedFeature> SILVER_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("silver_ore"));
    private static final ResourceKey<PlacedFeature> MITHRIL_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("mithril_ore"));
    public static final ResourceKey<PlacedFeature> ADAMANTIUM_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_adamantium_ore"));
    private static final ResourceKey<BiomeModifier> ADD_SILVER_ORE =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_silver_ore"));
    private static final ResourceKey<BiomeModifier> ADD_MITHRIL_ORE =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_mithril_ore"));

    private ModWorldGen() {}

    static RegistrySetBuilder builder() {
        return new RegistrySetBuilder()
                .add(Registries.CONFIGURED_FEATURE, ModWorldGen::bootstrapConfiguredFeatures)
                .add(Registries.PLACED_FEATURE, ModWorldGen::bootstrapPlacedFeatures)
                .add(Registries.BIOME, ModWorldGen::bootstrapBiomes)
                .add(Registries.DIMENSION_TYPE, ModWorldGen::bootstrapDimensionTypes)
                .add(Registries.NOISE_SETTINGS, ModWorldGen::bootstrapNoiseSettings)
                .add(Registries.LEVEL_STEM, ModWorldGen::bootstrapLevelStems)
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModWorldGen::bootstrapBiomeModifiers);
    }

    private static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        registerConfiguredOre(context, SILVER_ORE_CONFIGURED, ModBlocks.SILVER_ORE.get().defaultBlockState(), 6);
        registerConfiguredOre(context, MITHRIL_ORE_CONFIGURED, ModBlocks.MITHRIL_ORE.get().defaultBlockState(), 3);
        registerConfiguredOre(context, ADAMANTIUM_ORE_CONFIGURED, ModBlocks.ADAMANTIUM_ORE.get().defaultBlockState(), 3);
    }

    private static void registerConfiguredOre(
            BootstrapContext<ConfiguredFeature<?, ?>> context,
            ResourceKey<ConfiguredFeature<?, ?>> key,
            BlockState state,
            int size) {
        context.register(
                key,
                new ConfiguredFeature<>(
                        Feature.ORE,
                        new OreConfiguration(
                                new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
                                state,
                                size)));
    }

    private static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures =
                context.lookup(Registries.CONFIGURED_FEATURE);
        registerPlacedOre(context, configuredFeatures, SILVER_ORE_CONFIGURED, SILVER_ORE_PLACED, 96);
        registerPlacedOre(context, configuredFeatures, MITHRIL_ORE_CONFIGURED, MITHRIL_ORE_PLACED, 32);
        context.register(
                ADAMANTIUM_ORE_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(ADAMANTIUM_ORE_CONFIGURED),
                        List.of(
                                CountPlacement.of(8),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.of(BiasedToBottomHeight.of(
                                        VerticalAnchor.absolute(0), VerticalAnchor.absolute(136), 1)),
                                BiomeFilter.biome())));
    }

    private static void bootstrapBiomes(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        var carvers = context.lookup(Registries.CONFIGURED_CARVER);
        MobSpawnSettings.Builder mobs = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.commonSpawns(mobs, 120);
        mobs.addSpawn(MobCategory.MONSTER, 20, new MobSpawnSettings.SpawnerData(EntityTypes.CAVE_SPIDER, 1, 2));

        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(placed, carvers);
        BiomeDefaultFeatures.addDefaultCarversAndLakes(generation);
        BiomeDefaultFeatures.addDefaultMonsterRoom(generation);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(generation);
        BiomeDefaultFeatures.addDefaultOres(generation);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, placed.getOrThrow(SILVER_ORE_PLACED));
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, placed.getOrThrow(MITHRIL_ORE_PLACED));
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, placed.getOrThrow(ADAMANTIUM_ORE_PLACED));

        context.register(
                Underworld.BIOME,
                new Biome.BiomeBuilder()
                        .hasPrecipitation(false)
                        .temperature(0.5F)
                        .downfall(0.0F)
                        .specialEffects(new BiomeSpecialEffects.Builder()
                                .waterColor(4_159_204)
                                .build())
                        .setAttribute(EnvironmentAttributes.FOG_COLOR, 1_710_619)
                        .setAttribute(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                        .mobSpawnSettings(mobs.build())
                        .generationSettings(generation.build())
                        .build());
    }

    private static void bootstrapDimensionTypes(BootstrapContext<DimensionType> context) {
        HolderGetter<Block> blocks = context.lookup(Registries.BLOCK);
        HolderGetter<Timeline> timelines = context.lookup(Registries.TIMELINE);
        HolderGetter<WorldClock> clocks = context.lookup(Registries.WORLD_CLOCK);
        EnvironmentAttributeMap overworldAttributes = EnvironmentAttributeMap.builder()
                .set(EnvironmentAttributes.FOG_COLOR, -4_138_753)
                .set(EnvironmentAttributes.SKY_COLOR, OverworldBiomes.calculateSkyColor(0.8F))
                .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, -16_119_286)
                .set(EnvironmentAttributes.CLOUD_COLOR, ARGB.white(0.8F))
                .set(EnvironmentAttributes.CLOUD_HEIGHT, 192.33F)
                .set(EnvironmentAttributes.BACKGROUND_MUSIC, BackgroundMusic.OVERWORLD)
                .set(EnvironmentAttributes.BED_RULE, BedRule.CAN_SLEEP_WHEN_DARK)
                .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
                .set(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLINS, true)
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                .build();
        context.register(
                BuiltinDimensionTypes.OVERWORLD,
                new DimensionType(
                        false,
                        true,
                        false,
                        false,
                        1.0,
                        OVERWORLD_MIN_Y,
                        OVERWORLD_HEIGHT,
                        OVERWORLD_HEIGHT,
                        blocks.getOrThrow(BlockTags.INFINIBURN_OVERWORLD),
                        0.0F,
                        new DimensionType.MonsterSettings(UniformInt.of(0, 7), 0),
                        DimensionType.Skybox.OVERWORLD,
                        CardinalLighting.Type.DEFAULT,
                        overworldAttributes,
                        timelines.getOrThrow(TimelineTags.IN_OVERWORLD),
                        Optional.of(clocks.getOrThrow(WorldClocks.OVERWORLD))));
        context.register(
                Underworld.TYPE,
                new DimensionType(
                        true,
                        false,
                        true,
                        false,
                        1.0,
                        UNDERWORLD_MIN_Y,
                        UNDERWORLD_HEIGHT,
                        UNDERWORLD_HEIGHT,
                        blocks.getOrThrow(BlockTags.INFINIBURN_OVERWORLD),
                        0.05F,
                        new DimensionType.MonsterSettings(ConstantInt.of(7), 15),
                        DimensionType.Skybox.NONE,
                        CardinalLighting.Type.NETHER,
                        EnvironmentAttributeMap.builder()
                                .set(EnvironmentAttributes.FOG_START_DISTANCE, 8.0F)
                                .set(EnvironmentAttributes.FOG_END_DISTANCE, 96.0F)
                                .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0.0F)
                                .set(EnvironmentAttributes.BED_RULE, BedRule.EXPLODES)
                                .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
                                .set(EnvironmentAttributes.CAN_START_RAID, false)
                                .set(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                                .build(),
                        timelines.getOrThrow(TimelineTags.IN_NETHER),
                        Optional.empty()));
    }

    private static void bootstrapNoiseSettings(BootstrapContext<NoiseGeneratorSettings> context) {
        registerOverworldNoiseSettings(context, NoiseGeneratorSettings.OVERWORLD, false, false);
        registerOverworldNoiseSettings(context, NoiseGeneratorSettings.LARGE_BIOMES, false, true);
        registerOverworldNoiseSettings(context, NoiseGeneratorSettings.AMPLIFIED, true, false);
        context.register(
                Underworld.NOISE,
                new NoiseGeneratorSettings(
                        NoiseSettings.create(UNDERWORLD_MIN_Y, UNDERWORLD_HEIGHT, 1, 2),
                        Blocks.STONE.defaultBlockState(),
                        Blocks.WATER.defaultBlockState(),
                        underworldNoiseRouter(),
                        underworldSurfaceRule(),
                        List.of(),
                        UNDERWORLD_SEA_LEVEL,
                        false,
                        false,
                        false,
                        true));
    }

    private static NoiseRouter underworldNoiseRouter() {
        DensityFunction finalDensity = DensityFunctions.yClampedGradient(
                UNDERWORLD_STONE_CEILING - 1,
                UNDERWORLD_STONE_CEILING,
                1.0,
                -1.0);
        return withFinalDensity(NoiseRouterData.none(), finalDensity);
    }

    private static SurfaceRules.RuleSource underworldSurfaceRule() {
        SurfaceRules.RuleSource bedrock = SurfaceRules.state(Blocks.BEDROCK.defaultBlockState());
        SurfaceRules.RuleSource deepslate = SurfaceRules.state(Blocks.DEEPSLATE.defaultBlockState());
        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.verticalGradient(
                                "bedrock_floor",
                                VerticalAnchor.bottom(),
                                VerticalAnchor.aboveBottom(5)),
                        bedrock),
                SurfaceRules.ifTrue(
                        SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(0), 0)),
                        deepslate));
    }

    private static void registerOverworldNoiseSettings(
            BootstrapContext<NoiseGeneratorSettings> context,
            ResourceKey<NoiseGeneratorSettings> key,
            boolean amplified,
            boolean largeBiomes) {
        NoiseGeneratorSettings vanilla = NoiseGeneratorSettings.overworld(context, amplified, largeBiomes);
        context.register(
                key,
                new NoiseGeneratorSettings(
                        NoiseSettings.create(OVERWORLD_MIN_Y, OVERWORLD_HEIGHT, 1, 2),
                        vanilla.defaultBlock(),
                        vanilla.defaultFluid(),
                        withRaisedOverworldFloor(vanilla.noiseRouter()),
                        vanilla.surfaceRule(),
                        vanilla.spawnTarget(),
                        vanilla.seaLevel(),
                        vanilla.disableMobGeneration(),
                        vanilla.aquifersEnabled(),
                        vanilla.oreVeinsEnabled(),
                        vanilla.useLegacyRandomSource()));
    }

    private static NoiseRouter withRaisedOverworldFloor(NoiseRouter vanilla) {
        DensityFunction bottomTransition = DensityFunctions.yClampedGradient(
                OVERWORLD_MIN_Y, OVERWORLD_MIN_Y + 24, 0.0, 1.0);
        DensityFunction finalDensity = DensityFunctions.lerp(
                bottomTransition, 0.1171875, vanilla.finalDensity());
        return withFinalDensity(vanilla, finalDensity);
    }

    private static NoiseRouter withFinalDensity(NoiseRouter original, DensityFunction finalDensity) {
        return new NoiseRouter(
                original.barrierNoise(),
                original.fluidLevelFloodednessNoise(),
                original.fluidLevelSpreadNoise(),
                original.lavaNoise(),
                original.temperature(),
                original.vegetation(),
                original.continents(),
                original.erosion(),
                original.depth(),
                original.ridges(),
                original.preliminarySurfaceLevel(),
                finalDensity,
                original.veinToggle(),
                original.veinRidged(),
                original.veinGap());
    }

    private static void bootstrapLevelStems(BootstrapContext<LevelStem> context) {
        context.register(
                Underworld.STEM,
                new LevelStem(
                        context.lookup(Registries.DIMENSION_TYPE).getOrThrow(Underworld.TYPE),
                        new NoiseBasedChunkGenerator(
                                new FixedBiomeSource(context.lookup(Registries.BIOME).getOrThrow(Underworld.BIOME)),
                                context.lookup(Registries.NOISE_SETTINGS).getOrThrow(Underworld.NOISE))));
    }

    private static void registerPlacedOre(
            BootstrapContext<PlacedFeature> context,
            HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures,
            ResourceKey<ConfiguredFeature<?, ?>> configuredKey,
            ResourceKey<PlacedFeature> placedKey,
            int maximumY) {
        context.register(
                placedKey,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(configuredKey),
                        List.of(
                                CountPlacement.of(1),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.of(BiasedToBottomHeight.of(
                                        VerticalAnchor.absolute(0), VerticalAnchor.absolute(maximumY), 1)),
                                BiomeFilter.biome())));
    }

    private static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        registerOverworldOreModifier(context, biomes, placedFeatures, ADD_SILVER_ORE, SILVER_ORE_PLACED);
        registerOverworldOreModifier(context, biomes, placedFeatures, ADD_MITHRIL_ORE, MITHRIL_ORE_PLACED);
    }

    private static void registerOverworldOreModifier(
            BootstrapContext<BiomeModifier> context,
            HolderGetter<Biome> biomes,
            HolderGetter<PlacedFeature> placedFeatures,
            ResourceKey<BiomeModifier> modifierKey,
            ResourceKey<PlacedFeature> placedKey) {
        context.register(
                modifierKey,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(placedFeatures.getOrThrow(placedKey)),
                        GenerationStep.Decoration.UNDERGROUND_ORES));
    }
}
