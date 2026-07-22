package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.registry.ModEntityTypes;
import com.pixulse.infx.registry.ModEnchantments;
import com.pixulse.infx.registry.ModJukeboxSongs;
import com.pixulse.infx.registry.ModWorldCarvers;
import com.pixulse.infx.world.Underworld;
import com.pixulse.infx.world.R196RiverBiomes;
import com.pixulse.infx.world.R196SpawnsBiomeModifier;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.data.worldgen.placement.CavePlacements;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TimelineTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
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
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.heightproviders.BiasedToBottomHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.timeline.Timeline;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

final class ModWorldGen {
    private static final int OVERWORLD_MIN_Y = -16;
    private static final int OVERWORLD_HEIGHT = 336;
    private static final int UNDERWORLD_MIN_Y = -192;
    private static final int UNDERWORLD_HEIGHT = 512;
    private static final int UNDERWORLD_FIRST_CAVE_MIN_Y = 128;
    private static final int UNDERWORLD_FIRST_CAVE_END_Y = 216;
    private static final int UNDERWORLD_SEPARATOR_END_Y = 226;
    private static final int UNDERWORLD_ROOF_START_Y = 296;
    private static final int UNDERWORLD_TOP_Y = UNDERWORLD_MIN_Y + UNDERWORLD_HEIGHT - 1;
    private static final int UNDERWORLD_SEA_LEVEL = 140;
    private static final int MITE_R196_PROFILE_FIRST_SAMPLE = 4;
    private static final int MITE_R196_PROFILE_LAST_SAMPLE = 12;
    private static final double MITE_R196_PROFILE_FREQUENCY = Math.PI * 6.0 / 17.0;
    private static final ResourceKey<DensityFunction> MITE_R196_FIRST_CAVE =
            ResourceKey.create(Registries.DENSITY_FUNCTION, InfiniteX.id("mite_r196_first_cave"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> SILVER_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("silver_ore"));
    public static final ResourceKey<ConfiguredWorldCarver<?>> LARGE_CAVE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_CARVER, InfiniteX.id("large_cave"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> MITHRIL_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("mithril_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> R196_INFESTED_STONE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("r196_infested_stone"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> R196_INFESTED_NETHERRACK_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("r196_infested_netherrack"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> R196_FLOWERS_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("r196_flowers"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> R196_ALLIUM_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("r196_allium"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> WITHERWOOD_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("witherwood_patch"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> ADAMANTIUM_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("underworld_adamantium_ore"));
    private static final ResourceKey<PlacedFeature> SILVER_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("silver_ore"));
    private static final ResourceKey<PlacedFeature> MITHRIL_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("mithril_ore"));
    private static final ResourceKey<PlacedFeature> R196_INFESTED_STONE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("r196_infested_stone"));
    private static final ResourceKey<PlacedFeature> R196_INFESTED_NETHERRACK_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("r196_infested_netherrack"));
    private static final ResourceKey<PlacedFeature> R196_FLOWERS_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("r196_flowers"));
    private static final ResourceKey<PlacedFeature> R196_ALLIUM_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("r196_allium"));
    private static final ResourceKey<PlacedFeature> WITHERWOOD_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("witherwood_patch"));
    public static final ResourceKey<PlacedFeature> ADAMANTIUM_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_adamantium_ore"));
    private static final ResourceKey<BiomeModifier> ADD_SILVER_ORE =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_silver_ore"));
    private static final ResourceKey<BiomeModifier> ADD_MITHRIL_ORE =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_mithril_ore"));
    private static final ResourceKey<BiomeModifier> ADD_R196_INFESTED_STONE =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_r196_infested_stone"));
    private static final ResourceKey<BiomeModifier> ADD_R196_INFESTED_NETHERRACK =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_r196_infested_netherrack"));
    private static final ResourceKey<BiomeModifier> ADD_LARGE_CAVES =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_large_caves"));
    private static final ResourceKey<BiomeModifier> ADD_R196_FLOWERS =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_r196_flowers"));
    private static final ResourceKey<BiomeModifier> ADD_R196_ALLIUM =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_r196_allium"));
    private static final ResourceKey<BiomeModifier> ADD_WITHERWOOD =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_witherwood"));
    private static final ResourceKey<BiomeModifier> R196_SPAWNS =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("r196_spawns"));

    private ModWorldGen() {}

    static RegistrySetBuilder builder() {
        return new RegistrySetBuilder()
                .add(Registries.ENCHANTMENT, ModEnchantments::bootstrap)
                .add(Registries.JUKEBOX_SONG, ModJukeboxSongs::bootstrap)
                .add(Registries.STRUCTURE_SET, ModWorldGen::bootstrapStructureSets)
                .add(Registries.DENSITY_FUNCTION, ModWorldGen::bootstrapDensityFunctions)
                .add(Registries.CONFIGURED_CARVER, ModWorldGen::bootstrapConfiguredCarvers)
                .add(Registries.CONFIGURED_FEATURE, ModWorldGen::bootstrapConfiguredFeatures)
                .add(Registries.PLACED_FEATURE, ModWorldGen::bootstrapPlacedFeatures)
                .add(Registries.BIOME, ModWorldGen::bootstrapBiomes)
                .add(Registries.DIMENSION_TYPE, ModWorldGen::bootstrapDimensionTypes)
                .add(Registries.NOISE_SETTINGS, ModWorldGen::bootstrapNoiseSettings)
                .add(Registries.LEVEL_STEM, ModWorldGen::bootstrapLevelStems)
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModWorldGen::bootstrapBiomeModifiers);
    }

    private static void bootstrapStructureSets(BootstrapContext<StructureSet> context) {
        context.register(
                BuiltinStructureSets.STRONGHOLDS,
                new StructureSet(
                        context.lookup(Registries.STRUCTURE).getOrThrow(BuiltinStructures.STRONGHOLD),
                        new ConcentricRingsStructurePlacement(
                                220,
                                3,
                                128,
                                context.lookup(Registries.BIOME).getOrThrow(BiomeTags.STRONGHOLD_BIASED_TO))));
    }

    private static void bootstrapDensityFunctions(BootstrapContext<DensityFunction> context) {
        context.register(MITE_R196_FIRST_CAVE, underworldFirstCave());
    }

    private static void bootstrapConfiguredCarvers(BootstrapContext<ConfiguredWorldCarver<?>> context) {
        context.register(
                LARGE_CAVE_CONFIGURED,
                ModWorldCarvers.LARGE_CAVE.get().configured(new CarverConfiguration(
                        1.0F,
                        UniformHeight.of(VerticalAnchor.absolute(8), VerticalAnchor.absolute(55)),
                        ConstantFloat.of(1.0F),
                        VerticalAnchor.aboveBottom(8),
                        CarverDebugSettings.of(false, Blocks.OAK_BUTTON.defaultBlockState()),
                        context.lookup(Registries.BLOCK).getOrThrow(BlockTags.OVERWORLD_CARVER_REPLACEABLES))));
    }

    private static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        context.register(
                OreFeatures.ORE_GRAVEL_NETHER,
                new ConfiguredFeature<>(
                        Feature.ORE,
                        new OreConfiguration(
                                new BlockMatchTest(Blocks.NETHERRACK),
                                ModBlocks.NETHER_GRAVEL.get().defaultBlockState(),
                                33)));
        registerConfiguredOre(context, SILVER_ORE_CONFIGURED, ModBlocks.SILVER_ORE.get().defaultBlockState(), 6);
        registerConfiguredOre(context, MITHRIL_ORE_CONFIGURED, ModBlocks.MITHRIL_ORE.get().defaultBlockState(), 3);
        context.register(
                R196_INFESTED_STONE_CONFIGURED,
                new ConfiguredFeature<>(
                        Feature.ORE,
                        new OreConfiguration(
                                new BlockMatchTest(Blocks.STONE),
                                Blocks.INFESTED_STONE.defaultBlockState(),
                                3)));
        context.register(
                R196_INFESTED_NETHERRACK_CONFIGURED,
                new ConfiguredFeature<>(
                        Feature.ORE,
                        new OreConfiguration(
                                new BlockMatchTest(Blocks.NETHERRACK),
                                ModBlocks.INFESTED_NETHERRACK.get().defaultBlockState(),
                                8)));
        context.register(
                R196_FLOWERS_CONFIGURED,
                new ConfiguredFeature<>(
                        Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(new WeightedStateProvider(
                                WeightedList.<BlockState>builder()
                                        .add(ModBlocks.ROSE.get().defaultBlockState(), 2)
                                        .add(ModBlocks.ORCHID.get().defaultBlockState(), 1)
                                        .add(ModBlocks.TULIP.get().defaultBlockState(), 1)
                                        .add(ModBlocks.DAHLIA.get().defaultBlockState(), 1)
                                        .add(ModBlocks.DAISY.get().defaultBlockState(), 2)))));
        context.register(
                R196_ALLIUM_CONFIGURED,
                new ConfiguredFeature<>(
                        Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.ALLIUM.get()))));
        context.register(
                WITHERWOOD_CONFIGURED,
                new ConfiguredFeature<>(
                        Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.WITHERWOOD.get()))));
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
                R196_INFESTED_STONE_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(R196_INFESTED_STONE_CONFIGURED),
                        List.of(
                                CountPlacement.of(1),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(0), VerticalAnchor.belowTop(0)),
                                BiomeFilter.biome())));
        context.register(
                R196_INFESTED_NETHERRACK_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(R196_INFESTED_NETHERRACK_CONFIGURED),
                        List.of(
                                CountPlacement.of(UniformInt.of(2, 8)),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(10), VerticalAnchor.absolute(117)),
                                BiomeFilter.biome())));
        context.register(
                R196_FLOWERS_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(R196_FLOWERS_CONFIGURED),
                        List.of(
                                CountPlacement.of(1),
                                InSquarePlacement.spread(),
                                PlacementUtils.HEIGHTMAP,
                                BiomeFilter.biome(),
                                CountPlacement.of(32),
                                RandomOffsetPlacement.ofTriangle(7, 3),
                                BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE))));
        context.register(
                R196_ALLIUM_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(R196_ALLIUM_CONFIGURED),
                        List.of(
                                CountPlacement.of(1),
                                InSquarePlacement.spread(),
                                PlacementUtils.HEIGHTMAP,
                                BiomeFilter.biome(),
                                CountPlacement.of(8),
                                RandomOffsetPlacement.ofTriangle(7, 3),
                                BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE))));
        context.register(
                WITHERWOOD_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(WITHERWOOD_CONFIGURED),
                        List.of(
                                CountPlacement.of(16),
                                InSquarePlacement.spread(),
                                PlacementUtils.FULL_RANGE,
                                BiomeFilter.biome(),
                                CountPlacement.of(4),
                                RandomOffsetPlacement.ofTriangle(7, 3),
                                BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
                                        BlockPredicate.ONLY_IN_AIR_PREDICATE,
                                        BlockPredicate.matchesBlocks(
                                                net.minecraft.core.Direction.DOWN.getUnitVec3i(),
                                                ModBlocks.NETHER_GRAVEL.get()))))));
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
        context.register(
                R196RiverBiomes.DESERT_RIVER,
                r196River(placed, carvers, 1.4F, 0.0F, false, false));
        context.register(
                R196RiverBiomes.JUNGLE_RIVER,
                r196River(placed, carvers, 1.0F, 0.9F, true, false));
        context.register(
                R196RiverBiomes.SWAMP_RIVER,
                r196River(placed, carvers, 0.8F, 0.9F, true, true));
        MobSpawnSettings.Builder mobs = new MobSpawnSettings.Builder();
        addSpawn(mobs, EntityTypes.SPIDER, 80, 1, 2);
        addSpawn(mobs, EntityTypes.CREEPER, 100, 1, 2);
        addSpawn(mobs, EntityTypes.SLIME, 100, 1, 4);
        addSpawn(mobs, EntityTypes.ENDERMAN, 10, 1, 4);
        addSpawn(mobs, EntityTypes.CAVE_SPIDER, 40, 1, 2);
        addSpawn(mobs, EntityTypes.BAT, 100, 8, 8);
        addSpawn(mobs, EntityTypes.SQUID, 10, 4, 4);
        addUnderworldR196Spawns(mobs);

        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(placed, carvers);
        addUnderworldCarvers(generation);
        BiomeDefaultFeatures.addDefaultMonsterRoom(generation);
        addUnderworldUndergroundVariety(generation);
        addUnderworldOres(generation);
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

    private static void addUnderworldCarvers(BiomeGenerationSettings.Builder generation) {
        // Keep the Overworld cave shapes without its two lava-lake placements.
        generation.addCarver(Carvers.CAVE);
        generation.addCarver(Carvers.CAVE_EXTRA_UNDERGROUND);
        generation.addCarver(Carvers.CANYON);
    }

    private static void addUnderworldUndergroundVariety(BiomeGenerationSettings.Builder generation) {
        // This is the vanilla underground-variety list with ORE_DIRT deliberately omitted.
        for (ResourceKey<PlacedFeature> feature : List.of(
                OrePlacements.ORE_GRAVEL,
                OrePlacements.ORE_GRANITE_UPPER,
                OrePlacements.ORE_GRANITE_LOWER,
                OrePlacements.ORE_DIORITE_UPPER,
                OrePlacements.ORE_DIORITE_LOWER,
                OrePlacements.ORE_ANDESITE_UPPER,
                OrePlacements.ORE_ANDESITE_LOWER,
                OrePlacements.ORE_TUFF)) {
            generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, feature);
        }
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.GLOW_LICHEN);
    }

    private static void addUnderworldOres(BiomeGenerationSettings.Builder generation) {
        // Preserve the remaining vanilla mineral progression without either coal placement.
        for (ResourceKey<PlacedFeature> feature : List.of(
                OrePlacements.ORE_IRON_UPPER,
                OrePlacements.ORE_IRON_MIDDLE,
                OrePlacements.ORE_IRON_SMALL,
                OrePlacements.ORE_GOLD,
                OrePlacements.ORE_GOLD_LOWER,
                OrePlacements.ORE_REDSTONE,
                OrePlacements.ORE_REDSTONE_LOWER,
                OrePlacements.ORE_DIAMOND,
                OrePlacements.ORE_DIAMOND_MEDIUM,
                OrePlacements.ORE_DIAMOND_LARGE,
                OrePlacements.ORE_DIAMOND_BURIED,
                OrePlacements.ORE_LAPIS,
                OrePlacements.ORE_LAPIS_BURIED,
                OrePlacements.ORE_COPPER,
                CavePlacements.UNDERWATER_MAGMA)) {
            generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, feature);
        }
    }

    private static Biome r196River(
            HolderGetter<PlacedFeature> placed,
            HolderGetter<ConfiguredWorldCarver<?>> carvers,
            float temperature,
            float downfall,
            boolean precipitation,
            boolean swampColors) {
        Biome vanillaRiver = OverworldBiomes.river(placed, carvers, false);
        BiomeSpecialEffects.Builder effects = new BiomeSpecialEffects.Builder()
                .waterColor(vanillaRiver.getWaterColor());
        if (swampColors) {
            effects.foliageColorOverride(9_285_927)
                    .dryFoliageColorOverride(8_082_228)
                    .grassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP);
        }
        return new Biome.BiomeBuilder()
                .hasPrecipitation(precipitation)
                .temperature(temperature)
                .downfall(downfall)
                .putAttributes(vanillaRiver.getAttributes())
                .setAttribute(EnvironmentAttributes.SKY_COLOR, OverworldBiomes.calculateSkyColor(temperature))
                .specialEffects(effects.build())
                .mobSpawnSettings(vanillaRiver.getMobSettings())
                .generationSettings(vanillaRiver.getGenerationSettings())
                .build();
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
                                .set(
                                        EnvironmentAttributes.BED_RULE,
                                        new BedRule(
                                                BedRule.Rule.NEVER,
                                                BedRule.Rule.NEVER,
                                                false,
                                                Optional.of(Component.translatable("message.infx.underworld_bed_unsafe"))))
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
        HolderGetter<DensityFunction> densityFunctions = context.lookup(Registries.DENSITY_FUNCTION);
        DensityFunction firstCave = new DensityFunctions.HolderHolder(
                densityFunctions.getOrThrow(MITE_R196_FIRST_CAVE));
        context.register(
                Underworld.NOISE,
                new NoiseGeneratorSettings(
                        NoiseSettings.create(UNDERWORLD_MIN_Y, UNDERWORLD_HEIGHT, 1, 2),
                        Blocks.STONE.defaultBlockState(),
                        Blocks.WATER.defaultBlockState(),
                        underworldNoiseRouter(context.lookup(Registries.NOISE), firstCave),
                        underworldSurfaceRule(),
                        List.of(),
                        UNDERWORLD_SEA_LEVEL,
                        false,
                        false,
                        false,
                        true));
    }

    static NoiseRouter underworldNoiseRouter(HolderGetter<NormalNoise.NoiseParameters> noises) {
        return underworldNoiseRouter(noises, underworldFirstCave());
    }

    private static NoiseRouter underworldNoiseRouter(
            HolderGetter<NormalNoise.NoiseParameters> noises,
            DensityFunction firstCave) {
        DensityFunction entrances = underworldEntrances(noises);
        DensityFunction upperCave = underworldUpperCave(noises);

        // Morph the cave fields across a broad window so an opening never changes style on one Y plane.
        DensityFunction caveMorph = naturalTransition(
                noises, 204, 242, Noises.CAVE_LAYER, 0.07, 0.18);
        DensityFunction blendedCaves = DensityFunctions.lerp(caveMorph, firstCave, upperCave);

        DensityFunction entranceRise = naturalTransition(
                noises, 196, 208, Noises.SPAGHETTI_2D, 0.12, 0.24);
        DensityFunction entranceRelease = naturalTransition(
                noises, 238, 250, Noises.PILLAR, 0.10, 0.22);
        DensityFunction entranceFall = DensityFunctions.add(
                DensityFunctions.constant(1.0),
                DensityFunctions.mul(DensityFunctions.constant(-1.0), entranceRelease));
        DensityFunction entranceWindow = DensityFunctions.min(entranceRise, entranceFall);
        DensityFunction cavesWithEntrances = DensityFunctions.lerp(
                entranceWindow,
                blendedCaves,
                DensityFunctions.min(blendedCaves, entrances));

        // Two noisy signed surfaces form a variable-thickness rock band; ravines and tunnels cut through both.
        DensityFunction separatorFloor = naturalTransition(
                noises, 204, 222, Noises.PILLAR, 0.09, 0.40);
        DensityFunction separatorFloorDensity = DensityFunctions.add(
                DensityFunctions.mul(DensityFunctions.constant(2.0), separatorFloor),
                DensityFunctions.constant(-1.0));
        DensityFunction separatorCeiling = naturalTransition(
                noises, 222, 242, Noises.SPAGHETTI_2D, 0.08, 0.40);
        DensityFunction separatorCeilingDensity = DensityFunctions.add(
                DensityFunctions.constant(1.0),
                DensityFunctions.mul(DensityFunctions.constant(-2.0), separatorCeiling));
        DensityFunction separatorBand = DensityFunctions.min(
                separatorFloorDensity,
                separatorCeilingDensity);
        DensityFunction caveLayers = DensityFunctions.max(
                cavesWithEntrances,
                DensityFunctions.min(separatorBand, entrances));

        // Positive density is stone; the first cave opens gradually without carving below Y=128.
        DensityFunction solidToCaves = naturalTransition(
                noises, UNDERWORLD_FIRST_CAVE_MIN_Y - 1, 136, Noises.PILLAR, 0.09, 0.18);
        DensityFunction layeredTerrain = DensityFunctions.lerp(solidToCaves, 1.0, caveLayers);
        DensityFunction roofClosure = DensityFunctions.yClampedGradient(
                UNDERWORLD_ROOF_START_Y,
                UNDERWORLD_TOP_Y,
                -1.0,
                1.0);
        DensityFunction finalDensity = DensityFunctions.max(layeredTerrain, roofClosure).clamp(-1.0, 1.0);
        return withFinalDensity(NoiseRouterData.none(), finalDensity);
    }

    private static DensityFunction underworldFirstCave() {
        /*
         * MITE 1.6.4 R196 ChunkProviderUnderworld builds its cavern terrain from the
         * same three legacy octave stacks represented by BlendedNoise: two 16-octave
         * limit fields selected by an 8-octave main field. These five parameters are
         * the block-space form of MITE's 684.412/2053.236 and /80,/60 scales.
         */
        DensityFunction miteTerrain = BlendedNoise.createUnseeded(0.25, 0.375, 80.0, 60.0, 8.0);
        DensityFunction scaledTerrain = DensityFunctions.mul(
                DensityFunctions.constant(128.0),
                miteTerrain);
        DensityFunction verticalProfile = miteR196CavernProfile();
        DensityFunction cavernDensity = DensityFunctions.add(
                scaledTerrain,
                DensityFunctions.mul(DensityFunctions.constant(-1.0), verticalProfile));
        return DensityFunctions.interpolated(cavernDensity).clamp(-1.0, 1.0);
    }

    private static DensityFunction miteR196CavernProfile() {
        /*
         * R196's 17-point profile has cubic stone caps at both ends. The adjacent
         * InfiniteX strata already close this shorter layer, so stretch the central
         * cavern samples (4 through 12) across Y=128..216 and retain their values.
         */
        int sampleCount = MITE_R196_PROFILE_LAST_SAMPLE - MITE_R196_PROFILE_FIRST_SAMPLE;
        DensityFunction y = DensityFunctions.yClampedGradient(
                UNDERWORLD_FIRST_CAVE_MIN_Y,
                UNDERWORLD_FIRST_CAVE_END_Y,
                UNDERWORLD_FIRST_CAVE_MIN_Y,
                UNDERWORLD_FIRST_CAVE_END_Y);
        DensityFunction profile = DensityFunctions.constant(miteR196ProfileValue(MITE_R196_PROFILE_LAST_SAMPLE));
        for (int sample = MITE_R196_PROFILE_LAST_SAMPLE - 1;
                sample >= MITE_R196_PROFILE_FIRST_SAMPLE;
                sample--) {
            int segment = sample - MITE_R196_PROFILE_FIRST_SAMPLE;
            int fromY = UNDERWORLD_FIRST_CAVE_MIN_Y
                    + (UNDERWORLD_FIRST_CAVE_END_Y - UNDERWORLD_FIRST_CAVE_MIN_Y) * segment / sampleCount;
            int toY = UNDERWORLD_FIRST_CAVE_MIN_Y
                    + (UNDERWORLD_FIRST_CAVE_END_Y - UNDERWORLD_FIRST_CAVE_MIN_Y) * (segment + 1) / sampleCount;
            DensityFunction slope = DensityFunctions.yClampedGradient(
                    fromY,
                    toY,
                    miteR196ProfileValue(sample),
                    miteR196ProfileValue(sample + 1));
            profile = DensityFunctions.rangeChoice(y, fromY, toY, slope, profile);
        }
        return profile;
    }

    private static double miteR196ProfileValue(int sample) {
        return Math.cos(sample * MITE_R196_PROFILE_FREQUENCY) * 2.0;
    }

    private static DensityFunction underworldEntrances(HolderGetter<NormalNoise.NoiseParameters> noises) {
        DensityFunction ravines = airPassage(
                DensityFunctions.noise(noises.getOrThrow(Noises.CAVE_ENTRANCE), 0.45, 0.40),
                0.03);
        DensityFunction tunnelA = DensityFunctions.noise(
                noises.getOrThrow(Noises.SPAGHETTI_3D_1), 0.85, 0.45);
        DensityFunction tunnelB = DensityFunctions.noise(
                noises.getOrThrow(Noises.SPAGHETTI_3D_2), 0.85, 0.45);
        DensityFunction tunnels = DensityFunctions.add(
                DensityFunctions.max(tunnelA.abs(), tunnelB.abs()),
                DensityFunctions.constant(-0.045));
        return DensityFunctions.interpolated(DensityFunctions.min(ravines, tunnels))
                .clamp(-1.0, 1.0);
    }

    private static DensityFunction naturalTransition(
            HolderGetter<NormalNoise.NoiseParameters> noises,
            int fromY,
            int toY,
            ResourceKey<NormalNoise.NoiseParameters> roughnessNoise,
            double xzScale,
            double roughness) {
        // The triangular envelope keeps the horizontal displacement at zero outside the transition window.
        DensityFunction progress = DensityFunctions.yClampedGradient(fromY, toY, 0.0, 1.0);
        DensityFunction remaining = DensityFunctions.add(
                DensityFunctions.constant(1.0),
                DensityFunctions.mul(DensityFunctions.constant(-1.0), progress));
        DensityFunction envelope = DensityFunctions.mul(
                DensityFunctions.constant(2.0),
                DensityFunctions.min(progress, remaining));
        DensityFunction horizontalRoughness = DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(
                DensityFunctions.zero(),
                DensityFunctions.zero(),
                xzScale,
                noises.getOrThrow(roughnessNoise)));
        DensityFunction perturbation = DensityFunctions.mul(
                envelope,
                DensityFunctions.mul(DensityFunctions.constant(roughness), horizontalRoughness));
        return DensityFunctions.add(progress, perturbation).clamp(0.0, 1.0);
    }

    private static DensityFunction underworldUpperCave(HolderGetter<NormalNoise.NoiseParameters> noises) {
        DensityFunction compactChambers = DensityFunctions.add(
                DensityFunctions.add(
                        DensityFunctions.noise(noises.getOrThrow(Noises.CAVE_CHEESE), 1.35, 0.95),
                        DensityFunctions.constant(0.22)),
                DensityFunctions.mul(
                        DensityFunctions.noise(noises.getOrThrow(Noises.CAVE_LAYER), 1.8, 2.4),
                        DensityFunctions.constant(0.18)));
        DensityFunction stonePartitions = stoneBand(
                DensityFunctions.shiftedNoise2d(
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        0.8,
                        noises.getOrThrow(Noises.SPAGHETTI_2D)),
                0.055);
        DensityFunction tunnelA = DensityFunctions.noise(
                noises.getOrThrow(Noises.SPAGHETTI_3D_1), 1.4, 1.1);
        DensityFunction tunnelB = DensityFunctions.noise(
                noises.getOrThrow(Noises.SPAGHETTI_3D_2), 1.4, 1.1);
        DensityFunction tunnels = DensityFunctions.add(
                DensityFunctions.max(tunnelA.abs(), tunnelB.abs()),
                DensityFunctions.constant(-0.06));
        DensityFunction partitionedChambers = DensityFunctions.max(compactChambers, stonePartitions);
        return DensityFunctions.interpolated(DensityFunctions.min(partitionedChambers, tunnels))
                .clamp(-1.0, 1.0);
    }

    private static DensityFunction stoneBand(DensityFunction noise, double halfWidth) {
        return DensityFunctions.add(
                DensityFunctions.constant(halfWidth),
                DensityFunctions.mul(DensityFunctions.constant(-1.0), noise.abs()));
    }

    private static DensityFunction airPassage(DensityFunction noise, double halfWidth) {
        return DensityFunctions.add(noise.abs(), DensityFunctions.constant(-halfWidth));
    }

    private static SurfaceRules.RuleSource underworldSurfaceRule() {
        SurfaceRules.RuleSource deepslate = SurfaceRules.state(Blocks.DEEPSLATE.defaultBlockState());
        return SurfaceRules.sequence(
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
                        false,
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
                LevelStem.OVERWORLD,
                new LevelStem(
                        context.lookup(Registries.DIMENSION_TYPE).getOrThrow(BuiltinDimensionTypes.OVERWORLD),
                        new NoiseBasedChunkGenerator(
                                R196RiverBiomes.createSource(context.lookup(Registries.BIOME)),
                                context.lookup(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD))));
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
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);
        registerOverworldOreModifier(context, biomes, placedFeatures, ADD_SILVER_ORE, SILVER_ORE_PLACED);
        registerOverworldOreModifier(context, biomes, placedFeatures, ADD_MITHRIL_ORE, MITHRIL_ORE_PLACED);
        registerOverworldOreModifier(
                context,
                biomes,
                placedFeatures,
                ADD_R196_INFESTED_STONE,
                R196_INFESTED_STONE_PLACED);
        context.register(
                ADD_R196_INFESTED_NETHERRACK,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_NETHER),
                        HolderSet.direct(placedFeatures.getOrThrow(R196_INFESTED_NETHERRACK_PLACED)),
                        GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(
                ADD_LARGE_CAVES,
                new BiomeModifiers.AddCarversBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(carvers.getOrThrow(LARGE_CAVE_CONFIGURED))));
        context.register(
                ADD_R196_FLOWERS,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(placedFeatures.getOrThrow(R196_FLOWERS_PLACED)),
                        GenerationStep.Decoration.VEGETAL_DECORATION));
        context.register(
                ADD_R196_ALLIUM,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        HolderSet.direct(
                                biomes.getOrThrow(Biomes.SWAMP),
                                biomes.getOrThrow(Biomes.MANGROVE_SWAMP),
                                biomes.getOrThrow(R196RiverBiomes.SWAMP_RIVER)),
                        HolderSet.direct(placedFeatures.getOrThrow(R196_ALLIUM_PLACED)),
                        GenerationStep.Decoration.VEGETAL_DECORATION));
        context.register(
                ADD_WITHERWOOD,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_NETHER),
                        HolderSet.direct(placedFeatures.getOrThrow(WITHERWOOD_PLACED)),
                        GenerationStep.Decoration.VEGETAL_DECORATION));
        registerR196SpawnModifiers(context);
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

    private static void registerR196SpawnModifiers(BootstrapContext<BiomeModifier> context) {
        context.register(R196_SPAWNS, new R196SpawnsBiomeModifier());
    }

    private static void addUnderworldR196Spawns(MobSpawnSettings.Builder mobs) {
        addSpawn(mobs, ModEntityTypes.VAMPIRE_BAT.get(), 20, 8, 8);
        addSpawn(mobs, ModEntityTypes.NIGHTWING.get(), 4, 1, 4);
        addSpawn(mobs, ModEntityTypes.WIGHT.get(), 10, 1, 1);
        addSpawn(mobs, ModEntityTypes.INVISIBLE_STALKER.get(), 10, 1, 1);
        addSpawn(mobs, ModEntityTypes.DEMON_SPIDER.get(), 10, 1, 1);
        addSpawn(mobs, ModEntityTypes.HELLHOUND.get(), 10, 1, 2);
        addSpawn(mobs, ModEntityTypes.WOOD_SPIDER.get(), 20, 1, 1);
        addSpawn(mobs, ModEntityTypes.SHADOW.get(), 10, 1, 1);
        addSpawn(mobs, ModEntityTypes.EARTH_ELEMENTAL.get(), 10, 1, 1);
        addSpawn(mobs, ModEntityTypes.JELLY.get(), 30, 1, 4);
        addSpawn(mobs, ModEntityTypes.BLOB.get(), 30, 1, 4);
        addSpawn(mobs, ModEntityTypes.OOZE.get(), 20, 1, 4);
        addSpawn(mobs, ModEntityTypes.PUDDING.get(), 30, 1, 4);
        addSpawn(mobs, ModEntityTypes.PHASE_SPIDER.get(), 5, 1, 4);
        addSpawn(mobs, ModEntityTypes.LONGDEAD.get(), 40, 1, 2);
        addSpawn(mobs, ModEntityTypes.ANCIENT_BONE_LORD.get(), 5, 1, 1);
    }

    private static void addSpawn(
            MobSpawnSettings.Builder mobs,
            net.minecraft.world.entity.EntityType<?> type,
            int weight,
            int minimum,
            int maximum) {
        mobs.addSpawn(type.getCategory(), weight, new MobSpawnSettings.SpawnerData(type, minimum, maximum));
    }

}
