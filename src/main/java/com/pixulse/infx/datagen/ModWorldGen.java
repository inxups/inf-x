package com.pixulse.infx.datagen;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.registry.InfXCarvers;
import com.pixulse.infx.registry.InfXEnchantments;
import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.registry.InfXFeatures;
import com.pixulse.infx.registry.InfXJukeboxSongs;
import com.pixulse.infx.world.InfXUnderworldBedrockStrata;
import com.pixulse.infx.world.InfXUnderworldBiomeSource;
import com.pixulse.infx.world.InfXUnderworldChunkGenerator;
import com.pixulse.infx.world.InfXShiftedYDensityFunction;
import com.pixulse.infx.world.Underworld;
import com.pixulse.infx.world.RiverBiomes;
import com.pixulse.infx.world.SpawnsBiomeModifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.data.worldgen.features.CaveFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.CavePlacements;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TimelineTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
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
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.heightproviders.BiasedToBottomHeight;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.timeline.Timeline;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModWorldGen {
    private static final int OVERWORLD_MIN_Y = -16;
    private static final int OVERWORLD_HEIGHT = 336;
    private static final int UNDERWORLD_MIN_Y = Underworld.MIN_Y;
    private static final int UNDERWORLD_HEIGHT = Underworld.HEIGHT;
    private static final int MITE_TERRAIN_SAMPLE_COUNT = 17;
    private static final int MITE_TERRAIN_CELL_HEIGHT = 8;
    private static final int MITE_TOP_SLIDE_START_SAMPLE = 13;
    private static final double MITE_PROFILE_FREQUENCY = Math.PI * 6.0 / MITE_TERRAIN_SAMPLE_COUNT;
    private static final ResourceKey<DensityFunction> UNDERWORLD_TERRAIN =
            ResourceKey.create(Registries.DENSITY_FUNCTION, InfiniteX.id("underworld_terrain"));
    private static final ResourceKey<ConfiguredWorldCarver<?>> UNDERWORLD_LARGE_CAVE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_CARVER, InfiniteX.id("underworld_large_cave"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> UNDERWORLD_DUNGEON_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("underworld_dungeon"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> UNDERWORLD_MYCELIUM_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("underworld_mycelium"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> UNDERWORLD_BROWN_MUSHROOM_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("underworld_brown_mushroom"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> UNDERWORLD_LIQUID_SOURCE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("underworld_liquid_source"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> UNDERWORLD_COPPER_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("underworld_copper_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> UNDERWORLD_SILVER_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("underworld_silver_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> UNDERWORLD_GOLD_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("underworld_gold_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> UNDERWORLD_IRON_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("underworld_iron_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> UNDERWORLD_MITHRIL_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("underworld_mithril_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> UNDERWORLD_ADAMANTIUM_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("underworld_adamantium_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> UNDERWORLD_REDSTONE_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("underworld_redstone_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> UNDERWORLD_DIAMOND_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("underworld_diamond_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> UNDERWORLD_LAPIS_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("underworld_lapis_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> UNDERWORLD_SILVERFISH_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("underworld_silverfish"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> UNDERWORLD_GRAVEL_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("underworld_gravel"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_COAL_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("overworld_coal_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_COPPER_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("overworld_copper_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_IRON_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("overworld_iron_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_SILVER_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("overworld_silver_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_GOLD_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("overworld_gold_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_MITHRIL_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("overworld_mithril_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_REDSTONE_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("overworld_redstone_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_DIAMOND_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("overworld_diamond_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_LAPIS_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("overworld_lapis_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_EMERALD_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("overworld_emerald_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> INFX_INFESTED_STONE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("infx_infested_stone"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> INFX_INFESTED_NETHERRACK_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("infx_infested_netherrack"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> WITHERWOOD_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("witherwood_patch"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> BLUEBERRY_BUSH_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("blueberry_bush_patch"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> SGRAVEL_DISK_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("sgravel_disk"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> SGRAVEL_GRAVEL_DISK_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("sgravel_gravel_disk"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> MOUNTAIN_SGRAVEL_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("mountain_sgravel_ore"));
    // Keep these distinct from mountain disks to avoid cross-biome feature-order cycles.
    private static final ResourceKey<ConfiguredFeature<?, ?>> SHORE_RIVER_SGRAVEL_DISK_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("shore_river_sgravel_disk"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> SHORE_RIVER_SGRAVEL_GRAVEL_DISK_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("shore_river_sgravel_gravel_disk"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> SHORE_RIVER_SGRAVEL_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("shore_river_sgravel_ore"));
    private static final ResourceKey<PlacedFeature> OVERWORLD_COAL_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("overworld_coal_ore"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_DUNGEON_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_dungeon"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_MYCELIUM_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_mycelium"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_BROWN_MUSHROOM_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_brown_mushroom"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_LIQUID_SOURCE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_liquid_source"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_LUSH_CAVES_CEILING_VEGETATION_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_lush_caves_ceiling_vegetation"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_CAVE_VINES_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_cave_vines"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_LUSH_CAVES_CLAY_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_lush_caves_clay"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_LUSH_CAVES_VEGETATION_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_lush_caves_vegetation"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_ROOTED_AZALEA_TREE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_rooted_azalea_tree"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_SPORE_BLOSSOM_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_spore_blossom"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_CLASSIC_VINES_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_classic_vines"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_COPPER_ORE_LOW_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_copper_ore_low"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_COPPER_ORE_FULL_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_copper_ore_full"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_SILVER_ORE_LOW_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_silver_ore_low"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_SILVER_ORE_FULL_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_silver_ore_full"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_GOLD_ORE_LOW_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_gold_ore_low"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_GOLD_ORE_FULL_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_gold_ore_full"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_IRON_ORE_LOW_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_iron_ore_low"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_IRON_ORE_FULL_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_iron_ore_full"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_MITHRIL_ORE_LOW_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_mithril_ore_low"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_MITHRIL_ORE_FULL_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_mithril_ore_full"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_ADAMANTIUM_ORE_LOW_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_adamantium_ore_low"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_ADAMANTIUM_ORE_FULL_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_adamantium_ore_full"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_REDSTONE_ORE_LOW_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_redstone_ore_low"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_REDSTONE_ORE_FULL_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_redstone_ore_full"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_DIAMOND_ORE_LOW_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_diamond_ore_low"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_DIAMOND_ORE_FULL_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_diamond_ore_full"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_LAPIS_ORE_LOW_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_lapis_ore_low"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_LAPIS_ORE_FULL_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_lapis_ore_full"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_SILVERFISH_LOW_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_silverfish_low"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_SILVERFISH_FULL_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_silverfish_full"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_GRAVEL_LOW_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_gravel_low"));
    private static final ResourceKey<PlacedFeature> UNDERWORLD_GRAVEL_FULL_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("underworld_gravel_full"));
    private static final ResourceKey<PlacedFeature> OVERWORLD_COPPER_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("overworld_copper_ore"));
    private static final ResourceKey<PlacedFeature> OVERWORLD_IRON_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("overworld_iron_ore"));
    private static final ResourceKey<PlacedFeature> OVERWORLD_SILVER_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("overworld_silver_ore"));
    private static final ResourceKey<PlacedFeature> OVERWORLD_GOLD_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("overworld_gold_ore"));
    private static final ResourceKey<PlacedFeature> OVERWORLD_MITHRIL_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("overworld_mithril_ore"));
    private static final ResourceKey<PlacedFeature> OVERWORLD_REDSTONE_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("overworld_redstone_ore"));
    private static final ResourceKey<PlacedFeature> OVERWORLD_DIAMOND_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("overworld_diamond_ore"));
    private static final ResourceKey<PlacedFeature> OVERWORLD_LAPIS_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("overworld_lapis_ore"));
    private static final ResourceKey<PlacedFeature> OVERWORLD_EMERALD_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("overworld_emerald_ore"));
    private static final ResourceKey<PlacedFeature> INFX_INFESTED_STONE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("infx_infested_stone"));
    private static final ResourceKey<PlacedFeature> INFX_INFESTED_NETHERRACK_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("infx_infested_netherrack"));
    private static final ResourceKey<PlacedFeature> WITHERWOOD_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("witherwood_patch"));
    private static final ResourceKey<PlacedFeature> BLUEBERRY_BUSH_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("blueberry_bush_patch"));
    private static final ResourceKey<PlacedFeature> SGRAVEL_DISK_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("sgravel_disk"));
    private static final ResourceKey<PlacedFeature> SGRAVEL_GRAVEL_DISK_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("sgravel_gravel_disk"));
    private static final ResourceKey<PlacedFeature> MOUNTAIN_SGRAVEL_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("mountain_sgravel_ore"));
    private static final ResourceKey<PlacedFeature> SHORE_RIVER_SGRAVEL_DISK_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("shore_river_sgravel_disk"));
    private static final ResourceKey<PlacedFeature> SHORE_RIVER_SGRAVEL_GRAVEL_DISK_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("shore_river_sgravel_gravel_disk"));
    private static final ResourceKey<PlacedFeature> SHORE_RIVER_SGRAVEL_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("shore_river_sgravel_ore"));
    private static final ResourceKey<BiomeModifier> ADD_SILVER_ORE =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_silver_ore"));
    private static final ResourceKey<BiomeModifier> ADD_MITHRIL_ORE =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_mithril_ore"));
    private static final ResourceKey<BiomeModifier> REMOVE_OVERWORLD_RESOURCE_ORES =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("remove_overworld_resource_ores"));
    private static final ResourceKey<BiomeModifier> ADD_OVERWORLD_RESOURCE_ORES =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_overworld_resource_ores"));
    private static final ResourceKey<BiomeModifier> ADD_OVERWORLD_EMERALD_ORE =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_overworld_emerald_ore"));
    private static final ResourceKey<BiomeModifier> ADD_R196_INFESTED_STONE =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_infx_infested_stone"));
    private static final ResourceKey<BiomeModifier> ADD_R196_INFESTED_NETHERRACK =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_infx_infested_netherrack"));
    private static final ResourceKey<BiomeModifier> ADD_WITHERWOOD =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_witherwood"));
    private static final ResourceKey<BiomeModifier> ADD_BLUEBERRY_BUSH =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_blueberry_bush"));
    private static final ResourceKey<BiomeModifier> ADD_MOUNTAIN_SGRAVEL_DISKS =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_mountain_sgravel_disks"));
    private static final ResourceKey<BiomeModifier> REPLACE_MOUNTAIN_SOFT_DISKS =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("replace_mountain_soft_disks"));
    private static final ResourceKey<BiomeModifier> ADD_SHORE_RIVER_SGRAVEL_DISKS =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_shore_river_sgravel_disks"));
    private static final ResourceKey<BiomeModifier> REPLACE_SHORE_RIVER_SOFT_DISKS =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("replace_shore_river_soft_disks"));
    private static final ResourceKey<BiomeModifier> REMOVE_JUNGLE_MELONS =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("remove_jungle_melons"));
    private static final ResourceKey<BiomeModifier> INFX_SPAWNS =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("infx_spawns"));

    private ModWorldGen() {}

    static RegistrySetBuilder builder() {
        return new RegistrySetBuilder()
                .add(Registries.ENCHANTMENT, InfXEnchantments::bootstrap)
                .add(Registries.JUKEBOX_SONG, InfXJukeboxSongs::bootstrap)
                .add(Registries.STRUCTURE_SET, ModWorldGen::bootstrapStructureSets)
                .add(Registries.NOISE, InfXUnderworldBedrockStrata::bootstrapNoiseParameters)
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
        context.register(UNDERWORLD_TERRAIN, underworldTerrainDensity());
    }

    private static void bootstrapConfiguredCarvers(BootstrapContext<ConfiguredWorldCarver<?>> context) {
        context.register(
                UNDERWORLD_LARGE_CAVE_CONFIGURED,
                new ConfiguredWorldCarver<>(
                        InfXCarvers.UNDERWORLD_LARGE_CAVE.get(),
                        new CarverConfiguration(
                                1.0F,
                                UniformHeight.of(
                                        VerticalAnchor.absolute(Underworld.LARGE_CAVE_MIN_Y),
                                        VerticalAnchor.absolute(Underworld.LARGE_CAVE_MAX_Y)),
                                ConstantFloat.of(1.0F),
                                VerticalAnchor.absolute(Underworld.LARGE_CAVE_MIN_Y),
                                CarverDebugSettings.DEFAULT,
                                HolderSet.direct(
                                        Blocks.STONE.builtInRegistryHolder(),
                                        Blocks.DEEPSLATE.builtInRegistryHolder(),
                                        Blocks.BEDROCK.builtInRegistryHolder()))));
    }

    private static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        context.register(
                UNDERWORLD_DUNGEON_CONFIGURED,
                new ConfiguredFeature<>(InfXFeatures.UNDERWORLD_DUNGEON.get(), NoneFeatureConfiguration.INSTANCE));
        context.register(
                UNDERWORLD_MYCELIUM_CONFIGURED,
                new ConfiguredFeature<>(InfXFeatures.UNDERWORLD_MYCELIUM.get(), NoneFeatureConfiguration.INSTANCE));
        context.register(
                UNDERWORLD_BROWN_MUSHROOM_CONFIGURED,
                new ConfiguredFeature<>(InfXFeatures.UNDERWORLD_BROWN_MUSHROOM.get(), NoneFeatureConfiguration.INSTANCE));
        context.register(
                UNDERWORLD_LIQUID_SOURCE_CONFIGURED,
                new ConfiguredFeature<>(InfXFeatures.UNDERWORLD_LIQUID_SOURCE.get(), NoneFeatureConfiguration.INSTANCE));
        registerUnderworldOre(
                context,
                UNDERWORLD_COPPER_ORE_CONFIGURED,
                Feature.ORE,
                Blocks.COPPER_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState(),
                Underworld.COPPER_ORE_SIZE);
        registerUnderworldOre(
                context,
                UNDERWORLD_SILVER_ORE_CONFIGURED,
                Feature.ORE,
                InfXBlocks.SILVER_ORE.get().defaultBlockState(),
                InfXBlocks.DEEPSLATE_SILVER_ORE.get().defaultBlockState(),
                Underworld.SILVER_ORE_SIZE);
        registerUnderworldOre(
                context,
                UNDERWORLD_GOLD_ORE_CONFIGURED,
                Feature.ORE,
                Blocks.GOLD_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState(),
                Underworld.GOLD_ORE_SIZE);
        registerUnderworldOre(
                context,
                UNDERWORLD_IRON_ORE_CONFIGURED,
                Feature.ORE,
                Blocks.IRON_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(),
                Underworld.IRON_ORE_SIZE);
        registerUnderworldOre(
                context,
                UNDERWORLD_MITHRIL_ORE_CONFIGURED,
                Feature.ORE,
                InfXBlocks.MITHRIL_ORE.get().defaultBlockState(),
                InfXBlocks.DEEPSLATE_MITHRIL_ORE.get().defaultBlockState(),
                Underworld.MITHRIL_ORE_SIZE);
        registerUnderworldOre(
                context,
                UNDERWORLD_ADAMANTIUM_ORE_CONFIGURED,
                Feature.ORE,
                InfXBlocks.ADAMANTIUM_ORE.get().defaultBlockState(),
                InfXBlocks.DEEPSLATE_ADAMANTIUM_ORE.get().defaultBlockState(),
                Underworld.ADAMANTIUM_ORE_SIZE);
        registerUnderworldOre(
                context,
                UNDERWORLD_REDSTONE_ORE_CONFIGURED,
                Feature.ORE,
                Blocks.REDSTONE_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_REDSTONE_ORE.defaultBlockState(),
                Underworld.REDSTONE_ORE_SIZE);
        registerUnderworldOre(
                context,
                UNDERWORLD_DIAMOND_ORE_CONFIGURED,
                Feature.ORE,
                Blocks.DIAMOND_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState(),
                Underworld.DIAMOND_ORE_SIZE);
        registerUnderworldOre(
                context,
                UNDERWORLD_LAPIS_ORE_CONFIGURED,
                Feature.ORE,
                Blocks.LAPIS_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_LAPIS_ORE.defaultBlockState(),
                Underworld.LAPIS_ORE_SIZE);
        registerUnderworldOre(
                context,
                UNDERWORLD_SILVERFISH_CONFIGURED,
                Feature.ORE,
                Blocks.INFESTED_STONE.defaultBlockState(),
                Blocks.INFESTED_DEEPSLATE.defaultBlockState(),
                Underworld.SILVERFISH_SIZE);
        registerUnderworldOre(
                context,
                UNDERWORLD_GRAVEL_CONFIGURED,
                InfXFeatures.UNDERWORLD_SUPPORTED_GRAVEL.get(),
                Blocks.GRAVEL.defaultBlockState(),
                Blocks.GRAVEL.defaultBlockState(),
                Underworld.GRAVEL_SIZE);
        context.register(
                OreFeatures.ORE_GRAVEL_NETHER,
                new ConfiguredFeature<>(
                        Feature.ORE,
                        new OreConfiguration(
                                new BlockMatchTest(Blocks.NETHERRACK),
                                InfXBlocks.NETHER_GRAVEL.get().defaultBlockState(),
                                33)));
        registerOverworldConfiguredOre(
                context,
                OVERWORLD_COAL_ORE_CONFIGURED,
                Blocks.COAL_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_COAL_ORE.defaultBlockState(),
                12);
        registerOverworldConfiguredOre(
                context,
                OVERWORLD_COPPER_ORE_CONFIGURED,
                Blocks.COPPER_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState(),
                6);
        registerOverworldConfiguredOre(
                context,
                OVERWORLD_IRON_ORE_CONFIGURED,
                Blocks.IRON_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(),
                6);
        registerOverworldConfiguredOre(
                context,
                OVERWORLD_SILVER_ORE_CONFIGURED,
                InfXBlocks.SILVER_ORE.get().defaultBlockState(),
                InfXBlocks.DEEPSLATE_SILVER_ORE.get().defaultBlockState(),
                6);
        registerOverworldConfiguredOre(
                context,
                OVERWORLD_GOLD_ORE_CONFIGURED,
                Blocks.GOLD_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState(),
                4);
        registerOverworldConfiguredOre(
                context,
                OVERWORLD_MITHRIL_ORE_CONFIGURED,
                InfXBlocks.MITHRIL_ORE.get().defaultBlockState(),
                InfXBlocks.DEEPSLATE_MITHRIL_ORE.get().defaultBlockState(),
                3);
        registerOverworldConfiguredOre(
                context,
                OVERWORLD_REDSTONE_ORE_CONFIGURED,
                Blocks.REDSTONE_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_REDSTONE_ORE.defaultBlockState(),
                5);
        registerOverworldConfiguredOre(
                context,
                OVERWORLD_DIAMOND_ORE_CONFIGURED,
                Blocks.DIAMOND_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState(),
                3);
        registerOverworldConfiguredOre(
                context,
                OVERWORLD_LAPIS_ORE_CONFIGURED,
                Blocks.LAPIS_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_LAPIS_ORE.defaultBlockState(),
                3);
        registerOverworldConfiguredOre(
                context,
                OVERWORLD_EMERALD_ORE_CONFIGURED,
                Blocks.EMERALD_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_EMERALD_ORE.defaultBlockState(),
                1);
        context.register(
                INFX_INFESTED_STONE_CONFIGURED,
                new ConfiguredFeature<>(
                        Feature.ORE,
                        new OreConfiguration(
                                new BlockMatchTest(Blocks.STONE),
                                Blocks.INFESTED_STONE.defaultBlockState(),
                                3)));
        context.register(
                INFX_INFESTED_NETHERRACK_CONFIGURED,
                new ConfiguredFeature<>(
                        Feature.ORE,
                        new OreConfiguration(
                                new BlockMatchTest(Blocks.NETHERRACK),
                                InfXBlocks.INFESTED_NETHERRACK.get().defaultBlockState(),
                                8)));
        context.register(
                WITHERWOOD_CONFIGURED,
                new ConfiguredFeature<>(
                        Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(InfXBlocks.WITHERWOOD.get()))));
        context.register(
                BLUEBERRY_BUSH_CONFIGURED,
                new ConfiguredFeature<>(
                        Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(InfXBlocks.BLUEBERRY_BUSH.get()
                                .defaultBlockState()
                                .setValue(SweetBerryBushBlock.AGE, SweetBerryBushBlock.MAX_AGE)))));
        context.register(
                SGRAVEL_DISK_CONFIGURED,
                new ConfiguredFeature<>(
                        Feature.DISK,
                        new DiskConfiguration(
                                BlockStateProvider.simple(InfXBlocks.GRAVEL.get()),
                                BlockPredicate.matchesBlocks(List.of(Blocks.DIRT, Blocks.GRASS_BLOCK)),
                                UniformInt.of(2, 6),
                                2)));
        context.register(
                SGRAVEL_GRAVEL_DISK_CONFIGURED,
                new ConfiguredFeature<>(
                        Feature.DISK,
                        new DiskConfiguration(
                                BlockStateProvider.simple(InfXBlocks.GRAVEL.get()),
                                BlockPredicate.matchesBlocks(List.of(Blocks.DIRT, Blocks.GRASS_BLOCK)),
                                UniformInt.of(2, 5),
                                2)));
        context.register(
                SHORE_RIVER_SGRAVEL_DISK_CONFIGURED,
                new ConfiguredFeature<>(
                        Feature.DISK,
                        new DiskConfiguration(
                                BlockStateProvider.simple(InfXBlocks.GRAVEL.get()),
                                BlockPredicate.matchesBlocks(List.of(Blocks.DIRT, Blocks.GRASS_BLOCK)),
                                UniformInt.of(2, 6),
                                2)));
        context.register(
                SHORE_RIVER_SGRAVEL_GRAVEL_DISK_CONFIGURED,
                new ConfiguredFeature<>(
                        Feature.DISK,
                        new DiskConfiguration(
                                BlockStateProvider.simple(InfXBlocks.GRAVEL.get()),
                                BlockPredicate.matchesBlocks(List.of(Blocks.DIRT, Blocks.GRASS_BLOCK)),
                                UniformInt.of(2, 5),
                                2)));
        registerSgravelOre(context, MOUNTAIN_SGRAVEL_ORE_CONFIGURED);
        registerSgravelOre(context, SHORE_RIVER_SGRAVEL_ORE_CONFIGURED);
    }

    private static void registerSgravelOre(
            BootstrapContext<ConfiguredFeature<?, ?>> context,
            ResourceKey<ConfiguredFeature<?, ?>> key) {
        context.register(
                key,
                new ConfiguredFeature<>(
                        Feature.ORE,
                        new OreConfiguration(
                                new TagMatchTest(BlockTags.BASE_STONE_OVERWORLD),
                                InfXBlocks.GRAVEL.get().defaultBlockState(),
                                33)));
    }

    private static void registerUnderworldOre(
            BootstrapContext<ConfiguredFeature<?, ?>> context,
            ResourceKey<ConfiguredFeature<?, ?>> key,
            Feature<OreConfiguration> feature,
            BlockState stoneState,
            BlockState deepslateState,
            int size) {
        context.register(
                key,
                new ConfiguredFeature<>(
                        feature,
                        new OreConfiguration(
                                List.of(
                                        OreConfiguration.target(new BlockMatchTest(Blocks.STONE), stoneState),
                                        OreConfiguration.target(new BlockMatchTest(Blocks.DEEPSLATE), deepslateState)),
                                size)));
    }

    private static void registerOverworldConfiguredOre(
            BootstrapContext<ConfiguredFeature<?, ?>> context,
            ResourceKey<ConfiguredFeature<?, ?>> key,
            BlockState stoneState,
            BlockState deepslateState,
            int size) {
        context.register(
                key,
                new ConfiguredFeature<>(
                        Feature.ORE,
                        new OreConfiguration(
                                List.of(
                                        OreConfiguration.target(
                                                new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), stoneState),
                                        OreConfiguration.target(
                                                new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), deepslateState)),
                                size)));
    }

    private static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures =
                context.lookup(Registries.CONFIGURED_FEATURE);
        context.register(
                UNDERWORLD_DUNGEON_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(UNDERWORLD_DUNGEON_CONFIGURED),
                        List.of(
                                CountPlacement.of(16),
                                InSquarePlacement.spread(),
                                RandomOffsetPlacement.horizontal(ConstantInt.of(8)),
                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(140), VerticalAnchor.absolute(171)),
                                BiomeFilter.biome())));
        context.register(
                UNDERWORLD_MYCELIUM_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(UNDERWORLD_MYCELIUM_CONFIGURED),
                        List.of(CountPlacement.of(1), BiomeFilter.biome())));
        context.register(
                UNDERWORLD_BROWN_MUSHROOM_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(UNDERWORLD_BROWN_MUSHROOM_CONFIGURED),
                        List.of(CountPlacement.of(1), BiomeFilter.biome())));
        context.register(
                UNDERWORLD_LIQUID_SOURCE_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(UNDERWORLD_LIQUID_SOURCE_CONFIGURED),
                        List.of(CountPlacement.of(1), BiomeFilter.biome())));
        context.register(
                UNDERWORLD_LUSH_CAVES_CEILING_VEGETATION_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(CaveFeatures.MOSS_PATCH_CEILING),
                        underworldLushCavePlacement(
                                CountPlacement.of(125),
                                Underworld.LUSH_CAVES_MIN_Y,
                                EnvironmentScanPlacement.scanningFor(
                                        Direction.UP,
                                        BlockPredicate.solid(),
                                        BlockPredicate.ONLY_IN_AIR_PREDICATE,
                                        12),
                                RandomOffsetPlacement.vertical(ConstantInt.of(-1)))));
        context.register(
                UNDERWORLD_CAVE_VINES_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(CaveFeatures.CAVE_VINE),
                        underworldLushCavePlacement(
                                CountPlacement.of(188),
                                Underworld.LUSH_CAVES_MIN_Y,
                                EnvironmentScanPlacement.scanningFor(
                                        Direction.UP,
                                        BlockPredicate.hasSturdyFace(Direction.DOWN),
                                        BlockPredicate.ONLY_IN_AIR_PREDICATE,
                                        12),
                                RandomOffsetPlacement.vertical(ConstantInt.of(-1)))));
        context.register(
                UNDERWORLD_LUSH_CAVES_CLAY_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(CaveFeatures.LUSH_CAVES_CLAY),
                        underworldLushCavePlacement(
                                CountPlacement.of(62),
                                Underworld.LUSH_CAVES_FLOOR_SCAN_MIN_Y,
                                EnvironmentScanPlacement.scanningFor(
                                        Direction.DOWN,
                                        BlockPredicate.solid(),
                                        BlockPredicate.ONLY_IN_AIR_PREDICATE,
                                        12),
                                RandomOffsetPlacement.vertical(ConstantInt.of(1)))));
        context.register(
                UNDERWORLD_LUSH_CAVES_VEGETATION_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(CaveFeatures.MOSS_PATCH),
                        underworldLushCavePlacement(
                                CountPlacement.of(125),
                                Underworld.LUSH_CAVES_FLOOR_SCAN_MIN_Y,
                                EnvironmentScanPlacement.scanningFor(
                                        Direction.DOWN,
                                        BlockPredicate.solid(),
                                        BlockPredicate.ONLY_IN_AIR_PREDICATE,
                                        12),
                                RandomOffsetPlacement.vertical(ConstantInt.of(1)))));
        context.register(
                UNDERWORLD_ROOTED_AZALEA_TREE_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(CaveFeatures.ROOTED_AZALEA_TREE),
                        underworldLushCavePlacement(
                                CountPlacement.of(UniformInt.of(1, 2)),
                                Underworld.LUSH_CAVES_MIN_Y,
                                EnvironmentScanPlacement.scanningFor(
                                        Direction.UP,
                                        BlockPredicate.solid(),
                                        BlockPredicate.ONLY_IN_AIR_PREDICATE,
                                        12),
                                RandomOffsetPlacement.vertical(ConstantInt.of(-1)))));
        context.register(
                UNDERWORLD_SPORE_BLOSSOM_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(CaveFeatures.SPORE_BLOSSOM),
                        underworldLushCavePlacement(
                                CountPlacement.of(25),
                                Underworld.LUSH_CAVES_MIN_Y,
                                EnvironmentScanPlacement.scanningFor(
                                        Direction.UP,
                                        BlockPredicate.solid(),
                                        BlockPredicate.ONLY_IN_AIR_PREDICATE,
                                        12),
                                RandomOffsetPlacement.vertical(ConstantInt.of(-1)))));
        context.register(
                UNDERWORLD_CLASSIC_VINES_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(VegetationFeatures.VINES),
                        underworldLushCavePlacement(CountPlacement.of(256), Underworld.LUSH_CAVES_MIN_Y)));
        registerUnderworldOrePlacements(
                context,
                configuredFeatures,
                UNDERWORLD_COPPER_ORE_CONFIGURED,
                UNDERWORLD_COPPER_ORE_LOW_PLACED,
                UNDERWORLD_COPPER_ORE_FULL_PLACED,
                Underworld.COPPER_ORE_LOW_COUNT,
                Underworld.COPPER_ORE_FULL_COUNT);
        registerUnderworldOrePlacements(
                context,
                configuredFeatures,
                UNDERWORLD_SILVER_ORE_CONFIGURED,
                UNDERWORLD_SILVER_ORE_LOW_PLACED,
                UNDERWORLD_SILVER_ORE_FULL_PLACED,
                Underworld.SILVER_ORE_LOW_COUNT,
                Underworld.SILVER_ORE_FULL_COUNT);
        registerUnderworldOrePlacements(
                context,
                configuredFeatures,
                UNDERWORLD_GOLD_ORE_CONFIGURED,
                UNDERWORLD_GOLD_ORE_LOW_PLACED,
                UNDERWORLD_GOLD_ORE_FULL_PLACED,
                Underworld.GOLD_ORE_LOW_COUNT,
                Underworld.GOLD_ORE_FULL_COUNT);
        registerUnderworldOrePlacements(
                context,
                configuredFeatures,
                UNDERWORLD_IRON_ORE_CONFIGURED,
                UNDERWORLD_IRON_ORE_LOW_PLACED,
                UNDERWORLD_IRON_ORE_FULL_PLACED,
                Underworld.IRON_ORE_LOW_COUNT,
                Underworld.IRON_ORE_FULL_COUNT);
        registerUnderworldOrePlacements(
                context,
                configuredFeatures,
                UNDERWORLD_MITHRIL_ORE_CONFIGURED,
                UNDERWORLD_MITHRIL_ORE_LOW_PLACED,
                UNDERWORLD_MITHRIL_ORE_FULL_PLACED,
                Underworld.MITHRIL_ORE_LOW_COUNT,
                Underworld.MITHRIL_ORE_FULL_COUNT);
        registerUnderworldOrePlacements(
                context,
                configuredFeatures,
                UNDERWORLD_ADAMANTIUM_ORE_CONFIGURED,
                UNDERWORLD_ADAMANTIUM_ORE_LOW_PLACED,
                UNDERWORLD_ADAMANTIUM_ORE_FULL_PLACED,
                Underworld.ADAMANTIUM_ORE_LOW_COUNT,
                0);
        registerUnderworldOrePlacements(
                context,
                configuredFeatures,
                UNDERWORLD_REDSTONE_ORE_CONFIGURED,
                UNDERWORLD_REDSTONE_ORE_LOW_PLACED,
                UNDERWORLD_REDSTONE_ORE_FULL_PLACED,
                Underworld.REDSTONE_ORE_LOW_COUNT,
                Underworld.REDSTONE_ORE_FULL_COUNT);
        registerUnderworldOrePlacements(
                context,
                configuredFeatures,
                UNDERWORLD_DIAMOND_ORE_CONFIGURED,
                UNDERWORLD_DIAMOND_ORE_LOW_PLACED,
                UNDERWORLD_DIAMOND_ORE_FULL_PLACED,
                Underworld.DIAMOND_ORE_LOW_COUNT,
                Underworld.DIAMOND_ORE_FULL_COUNT);
        registerUnderworldOrePlacements(
                context,
                configuredFeatures,
                UNDERWORLD_LAPIS_ORE_CONFIGURED,
                UNDERWORLD_LAPIS_ORE_LOW_PLACED,
                UNDERWORLD_LAPIS_ORE_FULL_PLACED,
                Underworld.LAPIS_ORE_LOW_COUNT,
                Underworld.LAPIS_ORE_FULL_COUNT);
        registerUnderworldOrePlacements(
                context,
                configuredFeatures,
                UNDERWORLD_SILVERFISH_CONFIGURED,
                UNDERWORLD_SILVERFISH_LOW_PLACED,
                UNDERWORLD_SILVERFISH_FULL_PLACED,
                Underworld.SILVERFISH_LOW_COUNT,
                Underworld.SILVERFISH_FULL_COUNT);
        registerUnderworldOrePlacements(
                context,
                configuredFeatures,
                UNDERWORLD_GRAVEL_CONFIGURED,
                UNDERWORLD_GRAVEL_LOW_PLACED,
                UNDERWORLD_GRAVEL_FULL_PLACED,
                Underworld.GRAVEL_LOW_COUNT,
                Underworld.GRAVEL_FULL_COUNT);
        registerPlacedOverworldOre(
                context,
                configuredFeatures,
                OVERWORLD_COAL_ORE_CONFIGURED,
                OVERWORLD_COAL_ORE_PLACED,
                CountPlacement.of(4),
                UniformHeight.of(VerticalAnchor.absolute(32), VerticalAnchor.absolute(160)));
        registerPlacedOverworldOre(
                context,
                configuredFeatures,
                OVERWORLD_COPPER_ORE_CONFIGURED,
                OVERWORLD_COPPER_ORE_PLACED,
                CountPlacement.of(3),
                UniformHeight.of(VerticalAnchor.absolute(-8), VerticalAnchor.absolute(96)));
        registerPlacedOverworldOre(
                context,
                configuredFeatures,
                OVERWORLD_IRON_ORE_CONFIGURED,
                OVERWORLD_IRON_ORE_PLACED,
                CountPlacement.of(4),
                BiasedToBottomHeight.of(VerticalAnchor.absolute(-8), VerticalAnchor.absolute(80), 1));
        registerPlacedOverworldOre(
                context,
                configuredFeatures,
                OVERWORLD_SILVER_ORE_CONFIGURED,
                OVERWORLD_SILVER_ORE_PLACED,
                RarityFilter.onAverageOnceEvery(2),
                BiasedToBottomHeight.of(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(64), 1));
        registerPlacedOverworldOre(
                context,
                configuredFeatures,
                OVERWORLD_GOLD_ORE_CONFIGURED,
                OVERWORLD_GOLD_ORE_PLACED,
                CountPlacement.of(1),
                BiasedToBottomHeight.of(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(40), 1));
        registerPlacedOverworldOre(
                context,
                configuredFeatures,
                OVERWORLD_MITHRIL_ORE_CONFIGURED,
                OVERWORLD_MITHRIL_ORE_PLACED,
                RarityFilter.onAverageOnceEvery(4),
                BiasedToBottomHeight.of(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(8), 1));
        registerPlacedOverworldOre(
                context,
                configuredFeatures,
                OVERWORLD_REDSTONE_ORE_CONFIGURED,
                OVERWORLD_REDSTONE_ORE_PLACED,
                RarityFilter.onAverageOnceEvery(2),
                BiasedToBottomHeight.of(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(16), 1));
        registerPlacedOverworldOre(
                context,
                configuredFeatures,
                OVERWORLD_DIAMOND_ORE_CONFIGURED,
                OVERWORLD_DIAMOND_ORE_PLACED,
                RarityFilter.onAverageOnceEvery(4),
                BiasedToBottomHeight.of(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(4), 1));
        registerPlacedOverworldOre(
                context,
                configuredFeatures,
                OVERWORLD_LAPIS_ORE_CONFIGURED,
                OVERWORLD_LAPIS_ORE_PLACED,
                RarityFilter.onAverageOnceEvery(4),
                UniformHeight.of(VerticalAnchor.absolute(8), VerticalAnchor.absolute(32)));
        registerPlacedOverworldOre(
                context,
                configuredFeatures,
                OVERWORLD_EMERALD_ORE_CONFIGURED,
                OVERWORLD_EMERALD_ORE_PLACED,
                RarityFilter.onAverageOnceEvery(2),
                UniformHeight.of(VerticalAnchor.absolute(48), VerticalAnchor.absolute(128)));
        context.register(
                INFX_INFESTED_STONE_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(INFX_INFESTED_STONE_CONFIGURED),
                        List.of(
                                CountPlacement.of(1),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(0), VerticalAnchor.belowTop(0)),
                                BiomeFilter.biome())));
        context.register(
                INFX_INFESTED_NETHERRACK_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(INFX_INFESTED_NETHERRACK_CONFIGURED),
                        List.of(
                                CountPlacement.of(UniformInt.of(2, 8)),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(10), VerticalAnchor.absolute(117)),
                                BiomeFilter.biome())));
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
                                                InfXBlocks.NETHER_GRAVEL.get()))))));
        context.register(
                BLUEBERRY_BUSH_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(BLUEBERRY_BUSH_CONFIGURED),
                        List.of(
                                RarityFilter.onAverageOnceEvery(5),
                                CountPlacement.of(4),
                                InSquarePlacement.spread(),
                                HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES),
                                BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
                                        BlockPredicate.ONLY_IN_AIR_PREDICATE,
                                        BlockPredicate.wouldSurvive(
                                                InfXBlocks.BLUEBERRY_BUSH.get().defaultBlockState(), Vec3i.ZERO))),
                                BiomeFilter.biome())));
        context.register(
                SGRAVEL_DISK_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(SGRAVEL_DISK_CONFIGURED),
                        List.of(
                                CountPlacement.of(3),
                                InSquarePlacement.spread(),
                                PlacementUtils.HEIGHTMAP_TOP_SOLID,
                                BlockPredicateFilter.forPredicate(BlockPredicate.matchesFluids(Fluids.WATER)),
                                BiomeFilter.biome())));
        context.register(
                SGRAVEL_GRAVEL_DISK_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(SGRAVEL_GRAVEL_DISK_CONFIGURED),
                        List.of(
                                InSquarePlacement.spread(),
                                PlacementUtils.HEIGHTMAP_TOP_SOLID,
                                BlockPredicateFilter.forPredicate(BlockPredicate.matchesFluids(Fluids.WATER)),
                                BiomeFilter.biome())));
        context.register(
                SHORE_RIVER_SGRAVEL_DISK_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(SHORE_RIVER_SGRAVEL_DISK_CONFIGURED),
                        List.of(
                                CountPlacement.of(3),
                                InSquarePlacement.spread(),
                                PlacementUtils.HEIGHTMAP_TOP_SOLID,
                                BlockPredicateFilter.forPredicate(BlockPredicate.matchesFluids(Fluids.WATER)),
                                BiomeFilter.biome())));
        context.register(
                SHORE_RIVER_SGRAVEL_GRAVEL_DISK_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(SHORE_RIVER_SGRAVEL_GRAVEL_DISK_CONFIGURED),
                        List.of(
                                InSquarePlacement.spread(),
                                PlacementUtils.HEIGHTMAP_TOP_SOLID,
                                BlockPredicateFilter.forPredicate(BlockPredicate.matchesFluids(Fluids.WATER)),
                                BiomeFilter.biome())));
        registerSgravelOrePlacement(
                context,
                configuredFeatures,
                MOUNTAIN_SGRAVEL_ORE_CONFIGURED,
                MOUNTAIN_SGRAVEL_ORE_PLACED);
        registerSgravelOrePlacement(
                context,
                configuredFeatures,
                SHORE_RIVER_SGRAVEL_ORE_CONFIGURED,
                SHORE_RIVER_SGRAVEL_ORE_PLACED);
    }

    private static List<PlacementModifier> underworldLushCavePlacement(
            PlacementModifier count, int minimumY, PlacementModifier... additional) {
        List<PlacementModifier> placement = new ArrayList<>();
        placement.add(count);
        placement.add(InSquarePlacement.spread());
        placement.add(HeightRangePlacement.uniform(
                VerticalAnchor.absolute(minimumY),
                VerticalAnchor.absolute(Underworld.LUSH_CAVES_MAX_Y_INCLUSIVE)));
        placement.addAll(List.of(additional));
        placement.add(BiomeFilter.biome());
        return List.copyOf(placement);
    }

    private static void registerSgravelOrePlacement(
            BootstrapContext<PlacedFeature> context,
            HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures,
            ResourceKey<ConfiguredFeature<?, ?>> configuredKey,
            ResourceKey<PlacedFeature> placedKey) {
        context.register(
                placedKey,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(configuredKey),
                        List.of(
                                CountPlacement.of(14),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.top()),
                                BiomeFilter.biome())));
    }

    private static void bootstrapBiomes(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        var carvers = context.lookup(Registries.CONFIGURED_CARVER);
        context.register(
                RiverBiomes.DESERT_RIVER,
                r196River(placed, carvers, 1.4F, 0.0F, false, false));
        context.register(
                RiverBiomes.JUNGLE_RIVER,
                r196River(placed, carvers, 1.0F, 0.9F, true, false));
        context.register(
                RiverBiomes.SWAMP_RIVER,
                r196River(placed, carvers, 0.8F, 0.9F, true, true));
        BiomeGenerationSettings.Builder ordinaryGeneration = underworldCommonGeneration(placed, carvers);
        addUnderworldFungusFeatures(ordinaryGeneration);
        addUnderworldLiquidFeature(ordinaryGeneration);
        BiomeGenerationSettings.Builder lushGeneration = underworldCommonGeneration(placed, carvers);
        addUnderworldLiquidFeature(lushGeneration);
        addUnderworldLushFeatures(lushGeneration);
        BiomeGenerationSettings.Builder deepDarkGeneration = underworldCommonGeneration(placed, carvers);
        addUnderworldFungusFeatures(deepDarkGeneration);
        addUnderworldLiquidFeature(deepDarkGeneration);
        deepDarkGeneration.addCarver(UNDERWORLD_LARGE_CAVE_CONFIGURED);
        deepDarkGeneration.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, CavePlacements.SCULK_VEIN);
        deepDarkGeneration.addFeature(
                GenerationStep.Decoration.UNDERGROUND_DECORATION, CavePlacements.SCULK_PATCH_DEEP_DARK);
        deepDarkGeneration.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.GLOW_LICHEN);

        context.register(
                Underworld.BIOME,
                new Biome.BiomeBuilder()
                        .hasPrecipitation(false)
                        .temperature(0.5F)
                        .downfall(0.0F)
                        .specialEffects(new BiomeSpecialEffects.Builder().waterColor(4_159_204).build())
                        .setAttribute(EnvironmentAttributes.FOG_COLOR, 1_710_619)
                        .setAttribute(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                        .mobSpawnSettings(underworldMobSpawns())
                        .generationSettings(ordinaryGeneration.build())
                        .build());
        context.register(
                Underworld.LUSH_BIOME,
                new Biome.BiomeBuilder()
                        .hasPrecipitation(false)
                        .temperature(0.5F)
                        .downfall(0.0F)
                        .specialEffects(new BiomeSpecialEffects.Builder().waterColor(4_159_204).build())
                        .setAttribute(
                                EnvironmentAttributes.BACKGROUND_MUSIC,
                                new BackgroundMusic(SoundEvents.MUSIC_BIOME_LUSH_CAVES))
                        .setAttribute(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                        .mobSpawnSettings(underworldMobSpawns())
                        .generationSettings(lushGeneration.build())
                        .build());
        context.register(
                Underworld.DEEP_DARK_BIOME,
                new Biome.BiomeBuilder()
                        .hasPrecipitation(false)
                        .temperature(0.8F)
                        .downfall(0.0F)
                        .specialEffects(new BiomeSpecialEffects.Builder().waterColor(4_159_204).build())
                        .setAttribute(
                                EnvironmentAttributes.BACKGROUND_MUSIC,
                                new BackgroundMusic(SoundEvents.MUSIC_BIOME_DEEP_DARK))
                        .setAttribute(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                        .mobSpawnSettings(underworldMobSpawns())
                        .generationSettings(deepDarkGeneration.build())
                        .build());
    }

    private static BiomeGenerationSettings.Builder underworldCommonGeneration(
            HolderGetter<PlacedFeature> placed, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(placed, carvers);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, UNDERWORLD_DUNGEON_PLACED);
        addUnderworldOreFeatures(generation);
        return generation;
    }

    private static void addUnderworldFungusFeatures(BiomeGenerationSettings.Builder generation) {
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, UNDERWORLD_MYCELIUM_PLACED);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, UNDERWORLD_BROWN_MUSHROOM_PLACED);
    }

    private static void addUnderworldLiquidFeature(BiomeGenerationSettings.Builder generation) {
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, UNDERWORLD_LIQUID_SOURCE_PLACED);
    }

    private static void addUnderworldLushFeatures(BiomeGenerationSettings.Builder generation) {
        generation.addFeature(
                GenerationStep.Decoration.VEGETAL_DECORATION,
                UNDERWORLD_LUSH_CAVES_CEILING_VEGETATION_PLACED);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, UNDERWORLD_CAVE_VINES_PLACED);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, UNDERWORLD_LUSH_CAVES_CLAY_PLACED);
        generation.addFeature(
                GenerationStep.Decoration.VEGETAL_DECORATION,
                UNDERWORLD_LUSH_CAVES_VEGETATION_PLACED);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, UNDERWORLD_ROOTED_AZALEA_TREE_PLACED);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, UNDERWORLD_SPORE_BLOSSOM_PLACED);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, UNDERWORLD_CLASSIC_VINES_PLACED);
    }

    private static MobSpawnSettings underworldMobSpawns() {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        addUnderworldMonster(spawns, InfXEntityTypes.INFX_SPIDER.get(), 80, 1, 2);
        addUnderworldMonster(spawns, InfXEntityTypes.INFX_CREEPER.get(), 100, 1, 2);
        addUnderworldMonster(spawns, InfXEntityTypes.INFX_ENDERMAN.get(), 10, 1, 1);
        addUnderworldMonster(spawns, InfXEntityTypes.WIGHT.get(), 10, 1, 1);
        addUnderworldMonster(spawns, InfXEntityTypes.INVISIBLE_STALKER.get(), 10, 1, 1);
        addUnderworldMonster(spawns, InfXEntityTypes.DEMON_SPIDER.get(), 10, 1, 1);
        addUnderworldMonster(spawns, InfXEntityTypes.HELLHOUND.get(), 10, 1, 2);
        addUnderworldMonster(spawns, InfXEntityTypes.WOOD_SPIDER.get(), 20, 1, 1);
        addUnderworldMonster(spawns, InfXEntityTypes.SHADOW.get(), 10, 1, 1);
        addUnderworldMonster(spawns, InfXEntityTypes.EARTH_ELEMENTAL.get(), 10, 1, 1);
        addUnderworldMonster(spawns, InfXEntityTypes.JELLY.get(), 30, 1, 4);
        addUnderworldMonster(spawns, InfXEntityTypes.BLOB.get(), 30, 1, 4);
        addUnderworldMonster(spawns, InfXEntityTypes.OOZE.get(), 20, 1, 4);
        addUnderworldMonster(spawns, InfXEntityTypes.PUDDING.get(), 30, 1, 4);
        addUnderworldMonster(spawns, InfXEntityTypes.CLAY_GOLEM.get(), 50, 1, 1);
        addUnderworldMonster(spawns, InfXEntityTypes.PHASE_SPIDER.get(), 5, 1, 1);
        addUnderworldMonster(spawns, InfXEntityTypes.INFX_CAVE_SPIDER.get(), 40, 1, 2);
        addUnderworldMonster(spawns, InfXEntityTypes.LONGDEAD.get(), 40, 1, 2);
        addUnderworldMonster(spawns, InfXEntityTypes.ANCIENT_BONE_LORD.get(), 5, 1, 1);
        return spawns.build();
    }

    private static void addUnderworldMonster(
            MobSpawnSettings.Builder spawns, EntityType<?> type, int weight, int minimum, int maximum) {
        spawns.addSpawn(MobCategory.MONSTER, weight, new MobSpawnSettings.SpawnerData(type, minimum, maximum));
    }

    private static void addUnderworldOreFeatures(BiomeGenerationSettings.Builder generation) {
        addUnderworldOreFeature(generation, UNDERWORLD_COPPER_ORE_LOW_PLACED);
        addUnderworldOreFeature(generation, UNDERWORLD_COPPER_ORE_FULL_PLACED);
        addUnderworldOreFeature(generation, UNDERWORLD_SILVER_ORE_LOW_PLACED);
        addUnderworldOreFeature(generation, UNDERWORLD_SILVER_ORE_FULL_PLACED);
        addUnderworldOreFeature(generation, UNDERWORLD_GOLD_ORE_LOW_PLACED);
        addUnderworldOreFeature(generation, UNDERWORLD_GOLD_ORE_FULL_PLACED);
        addUnderworldOreFeature(generation, UNDERWORLD_IRON_ORE_LOW_PLACED);
        addUnderworldOreFeature(generation, UNDERWORLD_IRON_ORE_FULL_PLACED);
        addUnderworldOreFeature(generation, UNDERWORLD_MITHRIL_ORE_LOW_PLACED);
        addUnderworldOreFeature(generation, UNDERWORLD_MITHRIL_ORE_FULL_PLACED);
        addUnderworldOreFeature(generation, UNDERWORLD_ADAMANTIUM_ORE_LOW_PLACED);
        addUnderworldOreFeature(generation, UNDERWORLD_ADAMANTIUM_ORE_FULL_PLACED);
        addUnderworldOreFeature(generation, UNDERWORLD_REDSTONE_ORE_LOW_PLACED);
        addUnderworldOreFeature(generation, UNDERWORLD_REDSTONE_ORE_FULL_PLACED);
        addUnderworldOreFeature(generation, UNDERWORLD_DIAMOND_ORE_LOW_PLACED);
        addUnderworldOreFeature(generation, UNDERWORLD_DIAMOND_ORE_FULL_PLACED);
        addUnderworldOreFeature(generation, UNDERWORLD_LAPIS_ORE_LOW_PLACED);
        addUnderworldOreFeature(generation, UNDERWORLD_LAPIS_ORE_FULL_PLACED);
        addUnderworldOreFeature(generation, UNDERWORLD_SILVERFISH_LOW_PLACED);
        addUnderworldOreFeature(generation, UNDERWORLD_SILVERFISH_FULL_PLACED);
        addUnderworldOreFeature(generation, UNDERWORLD_GRAVEL_LOW_PLACED);
        addUnderworldOreFeature(generation, UNDERWORLD_GRAVEL_FULL_PLACED);
    }

    private static void addUnderworldOreFeature(
            BiomeGenerationSettings.Builder generation, ResourceKey<PlacedFeature> placedFeature) {
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, placedFeature);
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
                        BlockTags.INFINIBURN_OVERWORLD,
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
                        BlockTags.INFINIBURN_OVERWORLD,
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
        registerNetherNoiseSettings(context);
        DensityFunction underworldTerrain = new DensityFunctions.HolderHolder(
                context.lookup(Registries.DENSITY_FUNCTION).getOrThrow(UNDERWORLD_TERRAIN));
        DensityFunction underworldBiomeTemperature = DensityFunctions.noise(
                context.lookup(Registries.NOISE).getOrThrow(InfXUnderworldBiomeSource.BIOME_NOISE),
                InfXUnderworldBiomeSource.XZ_SCALE,
                InfXUnderworldBiomeSource.Y_SCALE);
        context.register(
                Underworld.NOISE,
                new NoiseGeneratorSettings(
                        NoiseSettings.create(UNDERWORLD_MIN_Y, UNDERWORLD_HEIGHT, 1, 2),
                        Blocks.STONE.defaultBlockState(),
                        Blocks.WATER.defaultBlockState(),
                        underworldNoiseRouter(underworldTerrain, underworldBiomeTemperature),
                        underworldSurfaceRule(),
                        List.of(),
                        Underworld.WATER_LEVEL,
                        false,
                        false,
                        false,
                        true));
    }

    private static void registerNetherNoiseSettings(BootstrapContext<NoiseGeneratorSettings> context) {
        NoiseGeneratorSettings vanilla = NoiseGeneratorSettings.nether(context);
        context.register(
                NoiseGeneratorSettings.NETHER,
                new NoiseGeneratorSettings(
                        vanilla.noiseSettings(),
                        vanilla.defaultBlock(),
                        vanilla.defaultFluid(),
                        vanilla.noiseRouter(),
                        withNetherBoundaryLayers(vanilla.surfaceRule()),
                        vanilla.spawnTarget(),
                        vanilla.seaLevel(),
                        vanilla.disableMobGeneration(),
                        vanilla.aquifersEnabled(),
                        vanilla.oreVeinsEnabled(),
                        vanilla.useLegacyRandomSource()));
    }

    public static NoiseRouter underworldNoiseRouter() {
        return underworldNoiseRouter(underworldTerrainDensity());
    }

    private static NoiseRouter underworldNoiseRouter(DensityFunction finalDensity) {
        return underworldNoiseRouter(finalDensity, DensityFunctions.zero());
    }

    private static NoiseRouter underworldNoiseRouter(
            DensityFunction finalDensity, DensityFunction temperature) {
        return withTemperature(withFinalDensity(NoiseRouterData.none(), finalDensity), temperature);
    }

    private static NoiseRouter withTemperature(NoiseRouter original, DensityFunction temperature) {
        return new NoiseRouter(
                original.barrierNoise(),
                original.fluidLevelFloodednessNoise(),
                original.fluidLevelSpreadNoise(),
                original.lavaNoise(),
                temperature,
                original.vegetation(),
                original.continents(),
                original.erosion(),
                original.depth(),
                original.ridges(),
                original.preliminarySurfaceLevel(),
                original.finalDensity(),
                original.veinToggle(),
                original.veinRidged(),
                original.veinGap());
    }

    private static DensityFunction underworldTerrainDensity() {
        DensityFunction legacyNetherNoise = new InfXShiftedYDensityFunction(
                BlendedNoise.createUnseeded(0.25, 0.375, 80.0, 60.0, 8.0),
                Underworld.TERRAIN_MIN_Y);
        DensityFunction scaledNoise = DensityFunctions.mul(
                DensityFunctions.constant(128.0), legacyNetherNoise);
        DensityFunction rawDensity = DensityFunctions.add(
                scaledNoise,
                DensityFunctions.mul(
                        DensityFunctions.constant(-1.0), miteTerrainVerticalProfile()));

        int topSlideStartY = Underworld.TERRAIN_MIN_Y
                + MITE_TOP_SLIDE_START_SAMPLE * MITE_TERRAIN_CELL_HEIGHT;
        DensityFunction topSlide = DensityFunctions.yClampedGradient(
                topSlideStartY,
                Underworld.TERRAIN_MAX_Y_EXCLUSIVE,
                0.0,
                1.0);
        DensityFunction shiftedTerrain = DensityFunctions.lerp(
                topSlide, rawDensity, DensityFunctions.constant(-10.0));
        DensityFunction interpolatedTerrain =
                DensityFunctions.interpolated(shiftedTerrain).clamp(-1.0, 1.0);

        DensityFunction y = absoluteUnderworldY();
        return DensityFunctions.rangeChoice(
                y,
                Underworld.TERRAIN_MIN_Y,
                Underworld.TERRAIN_MAX_Y_EXCLUSIVE,
                interpolatedTerrain,
                DensityFunctions.constant(1.0));
    }

    private static DensityFunction miteTerrainVerticalProfile() {
        DensityFunction y = absoluteUnderworldY();
        DensityFunction profile = DensityFunctions.constant(
                miteTerrainProfileValue(MITE_TERRAIN_SAMPLE_COUNT - 1));
        for (int sample = MITE_TERRAIN_SAMPLE_COUNT - 2; sample >= 0; sample--) {
            int fromY = Underworld.TERRAIN_MIN_Y + sample * MITE_TERRAIN_CELL_HEIGHT;
            int toY = fromY + MITE_TERRAIN_CELL_HEIGHT;
            DensityFunction segment = DensityFunctions.yClampedGradient(
                    fromY,
                    toY,
                    miteTerrainProfileValue(sample),
                    miteTerrainProfileValue(sample + 1));
            profile = DensityFunctions.rangeChoice(y, fromY, toY, segment, profile);
        }
        return profile;
    }

    private static double miteTerrainProfileValue(int sample) {
        double value = Math.cos(sample * MITE_PROFILE_FREQUENCY) * 2.0;
        int edgeDistance = Math.min(sample, MITE_TERRAIN_SAMPLE_COUNT - 1 - sample);
        if (edgeDistance < 4) {
            int capDepth = 4 - edgeDistance;
            value -= capDepth * capDepth * capDepth * 10.0;
        }
        return value;
    }

    private static DensityFunction absoluteUnderworldY() {
        return DensityFunctions.yClampedGradient(
                Underworld.MIN_Y,
                Underworld.MAX_Y_EXCLUSIVE,
                Underworld.MIN_Y,
                Underworld.MAX_Y_EXCLUSIVE);
    }

    private static SurfaceRules.RuleSource underworldSurfaceRule() {
        SurfaceRules.RuleSource mantle = SurfaceRules.state(InfXBlocks.MANTLE.get().defaultBlockState());
        SurfaceRules.RuleSource bedrock = SurfaceRules.state(Blocks.BEDROCK.defaultBlockState());
        SurfaceRules.RuleSource deepslate = SurfaceRules.state(Blocks.DEEPSLATE.defaultBlockState());
        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.verticalGradient(
                                "infx:underworld_mantle",
                                VerticalAnchor.bottom(),
                                VerticalAnchor.aboveBottom(Underworld.BOUNDARY_MAX_THICKNESS)),
                        mantle),
                SurfaceRules.ifTrue(
                        SurfaceRules.not(SurfaceRules.verticalGradient(
                                "infx:underworld_bedrock_roof",
                                VerticalAnchor.belowTop(Underworld.BOUNDARY_MAX_THICKNESS),
                                VerticalAnchor.top())),
                        bedrock),
                SurfaceRules.ifTrue(
                        SurfaceRules.not(SurfaceRules.yBlockCheck(
                                VerticalAnchor.absolute(Underworld.DEEPSLATE_MAX_Y_EXCLUSIVE), 0)),
                        deepslate),
                SurfaceRules.state(Blocks.STONE.defaultBlockState()));
    }

    private static SurfaceRules.RuleSource withNetherBoundaryLayers(SurfaceRules.RuleSource vanilla) {
        SurfaceRules.RuleSource core = SurfaceRules.state(InfXBlocks.CORE.get().defaultBlockState());
        SurfaceRules.RuleSource mantle = SurfaceRules.state(InfXBlocks.MANTLE.get().defaultBlockState());
        // yBlockCheck is inclusive upward, so its inverse selects only the lowest build layer.
        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.aboveBottom(1), 0)), core),
                SurfaceRules.ifTrue(SurfaceRules.yBlockCheck(VerticalAnchor.belowTop(0), 0), mantle),
                vanilla);
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
                        withSgravelSurface(vanilla.surfaceRule()),
                        vanilla.spawnTarget(),
                        vanilla.seaLevel(),
                        false,
                        vanilla.aquifersEnabled(),
                        false,
                        vanilla.useLegacyRandomSource()));
    }

    /**
     * Replaces the targeted vanilla gravel surface-rule branches. Biome modifiers
     * separately handle their placed soft disks; those modifiers cannot change
     * terrain produced by {@link SurfaceRules}.
     */
    private static SurfaceRules.RuleSource withSgravelSurface(SurfaceRules.RuleSource vanilla) {
        SurfaceRules.RuleSource sgravel = SurfaceRules.state(InfXBlocks.GRAVEL.get().defaultBlockState());
        SurfaceRules.RuleSource sgravelWhenNotOnCeiling = SurfaceRules.ifTrue(
                SurfaceRules.not(SurfaceRules.ON_CEILING), sgravel);
        SurfaceRules.ConditionSource abovePreliminarySurface = SurfaceRules.abovePreliminarySurface();
        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        abovePreliminarySurface,
                        windsweptGravellyHillsSgravelRule(sgravelWhenNotOnCeiling)),
                SurfaceRules.ifTrue(abovePreliminarySurface, stonyShoreSgravelRule(sgravelWhenNotOnCeiling)),
                SurfaceRules.ifTrue(abovePreliminarySurface, waterfrontGravelFallbackRule(sgravelWhenNotOnCeiling)),
                vanilla);
    }

    private static SurfaceRules.RuleSource windsweptGravellyHillsSgravelRule(
            SurfaceRules.RuleSource sgravelWhenNotOnCeiling) {
        SurfaceRules.RuleSource gravelBranches = SurfaceRules.sequence(
                SurfaceRules.ifTrue(surfaceNoiseAbove(2.0), sgravelWhenNotOnCeiling),
                SurfaceRules.ifTrue(SurfaceRules.not(surfaceNoiseAbove(-1.0)), sgravelWhenNotOnCeiling));
        return SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.WINDSWEPT_GRAVELLY_HILLS),
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(
                                SurfaceRules.ON_FLOOR,
                                SurfaceRules.ifTrue(SurfaceRules.waterBlockCheck(-1, 0), gravelBranches)),
                        SurfaceRules.ifTrue(
                                SurfaceRules.waterStartCheck(-6, -1),
                                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, gravelBranches))));
    }

    private static SurfaceRules.RuleSource stonyShoreSgravelRule(SurfaceRules.RuleSource sgravelWhenNotOnCeiling) {
        SurfaceRules.RuleSource gravelPatch = SurfaceRules.ifTrue(
                SurfaceRules.noiseCondition(Noises.GRAVEL, -0.05, 0.05), sgravelWhenNotOnCeiling);
        return SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.STONY_SHORE),
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(
                                SurfaceRules.ON_FLOOR,
                                SurfaceRules.ifTrue(SurfaceRules.waterBlockCheck(-1, 0), gravelPatch)),
                        SurfaceRules.ifTrue(
                                SurfaceRules.waterStartCheck(-6, -1),
                                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, gravelPatch))));
    }

    /** Matches the vanilla final gravel fallback only when no biome under-surface rule applies. */
    private static SurfaceRules.RuleSource waterfrontGravelFallbackRule(
            SurfaceRules.RuleSource sgravelWhenNotOnCeiling) {
        SurfaceRules.ConditionSource notUnderwater = SurfaceRules.waterBlockCheck(-1, 0);
        SurfaceRules.ConditionSource notUnderDeepWater = SurfaceRules.waterStartCheck(-6, -1);
        return SurfaceRules.ifTrue(
                SurfaceRules.isBiome(
                        Biomes.STONY_SHORE,
                        Biomes.RIVER,
                        Biomes.FROZEN_RIVER,
                        RiverBiomes.DESERT_RIVER,
                        RiverBiomes.JUNGLE_RIVER,
                        RiverBiomes.SWAMP_RIVER),
                SurfaceRules.ifTrue(
                        SurfaceRules.ON_FLOOR,
                        SurfaceRules.ifTrue(
                                SurfaceRules.not(notUnderwater),
                                SurfaceRules.sequence(
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.not(notUnderDeepWater), sgravelWhenNotOnCeiling),
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.not(SurfaceRules.UNDER_FLOOR), sgravelWhenNotOnCeiling)))));
    }

    private static SurfaceRules.ConditionSource surfaceNoiseAbove(double threshold) {
        return SurfaceRules.noiseCondition(Noises.SURFACE, threshold / 8.25, Double.MAX_VALUE);
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
                                RiverBiomes.createSource(context.lookup(Registries.BIOME)),
                                context.lookup(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD))));
        context.register(
                Underworld.STEM,
                new LevelStem(
                        context.lookup(Registries.DIMENSION_TYPE).getOrThrow(Underworld.TYPE),
                        new InfXUnderworldChunkGenerator(
                                InfXUnderworldBiomeSource.create(context.lookup(Registries.BIOME)),
                                context.lookup(Registries.NOISE_SETTINGS).getOrThrow(Underworld.NOISE))));
    }

    private static void registerPlacedOverworldOre(
            BootstrapContext<PlacedFeature> context,
            HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures,
            ResourceKey<ConfiguredFeature<?, ?>> configuredKey,
            ResourceKey<PlacedFeature> placedKey,
            PlacementModifier frequency,
            HeightProvider height) {
        context.register(
                placedKey,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(configuredKey),
                        List.of(
                                frequency,
                                InSquarePlacement.spread(),
                                HeightRangePlacement.of(height),
                                BiomeFilter.biome())));
    }

    private static void registerUnderworldOrePlacements(
            BootstrapContext<PlacedFeature> context,
            HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures,
            ResourceKey<ConfiguredFeature<?, ?>> configuredKey,
            ResourceKey<PlacedFeature> lowPlacedKey,
            ResourceKey<PlacedFeature> fullPlacedKey,
            int lowCount,
            int fullCount) {
        context.register(
                lowPlacedKey,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(configuredKey),
                        List.of(
                                CountPlacement.of(lowCount),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(Underworld.MIN_Y),
                                        VerticalAnchor.absolute(Underworld.ORE_LOW_MAX_Y_INCLUSIVE)),
                                BiomeFilter.biome())));
        context.register(
                fullPlacedKey,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(configuredKey),
                        List.of(
                                CountPlacement.of(fullCount),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(Underworld.MIN_Y),
                                        VerticalAnchor.absolute(Underworld.ORE_MAX_Y_INCLUSIVE)),
                                BiomeFilter.biome())));
    }

    private static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);
        registerOverworldResourceOreRemoval(context, biomes, placedFeatures);
        context.register(
                ADD_OVERWORLD_RESOURCE_ORES,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(
                                placedFeatures.getOrThrow(OVERWORLD_COAL_ORE_PLACED),
                                placedFeatures.getOrThrow(OVERWORLD_COPPER_ORE_PLACED),
                                placedFeatures.getOrThrow(OVERWORLD_IRON_ORE_PLACED),
                                placedFeatures.getOrThrow(OVERWORLD_GOLD_ORE_PLACED),
                                placedFeatures.getOrThrow(OVERWORLD_REDSTONE_ORE_PLACED),
                                placedFeatures.getOrThrow(OVERWORLD_DIAMOND_ORE_PLACED),
                                placedFeatures.getOrThrow(OVERWORLD_LAPIS_ORE_PLACED)),
                        GenerationStep.Decoration.UNDERGROUND_ORES));
        registerOverworldOreModifier(
                context, biomes, placedFeatures, ADD_SILVER_ORE, OVERWORLD_SILVER_ORE_PLACED);
        registerOverworldOreModifier(
                context, biomes, placedFeatures, ADD_MITHRIL_ORE, OVERWORLD_MITHRIL_ORE_PLACED);
        context.register(
                ADD_OVERWORLD_EMERALD_ORE,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_MOUNTAIN),
                        HolderSet.direct(placedFeatures.getOrThrow(OVERWORLD_EMERALD_ORE_PLACED)),
                        GenerationStep.Decoration.UNDERGROUND_ORES));
        registerOverworldOreModifier(
                context,
                biomes,
                placedFeatures,
                ADD_R196_INFESTED_STONE,
                INFX_INFESTED_STONE_PLACED);
        context.register(
                ADD_R196_INFESTED_NETHERRACK,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_NETHER),
                        HolderSet.direct(placedFeatures.getOrThrow(INFX_INFESTED_NETHERRACK_PLACED)),
                        GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(
                ADD_WITHERWOOD,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_NETHER),
                        HolderSet.direct(placedFeatures.getOrThrow(WITHERWOOD_PLACED)),
                        GenerationStep.Decoration.VEGETAL_DECORATION));
        context.register(
                ADD_BLUEBERRY_BUSH,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        HolderSet.direct(
                                biomes.getOrThrow(Biomes.FOREST),
                                biomes.getOrThrow(Biomes.FLOWER_FOREST),
                                biomes.getOrThrow(Biomes.OLD_GROWTH_BIRCH_FOREST)),
                        HolderSet.direct(placedFeatures.getOrThrow(BLUEBERRY_BUSH_PLACED)),
                        GenerationStep.Decoration.VEGETAL_DECORATION));
        HolderSet<Biome> mountainSgravelBiomes = HolderSet.direct(
                biomes.getOrThrow(Biomes.STONY_PEAKS),
                biomes.getOrThrow(Biomes.WINDSWEPT_GRAVELLY_HILLS));
        context.register(
                REPLACE_MOUNTAIN_SOFT_DISKS,
                new BiomeModifiers.RemoveFeaturesBiomeModifier(
                        mountainSgravelBiomes,
                        HolderSet.direct(
                                placedFeatures.getOrThrow(MiscOverworldPlacements.DISK_SAND),
                                placedFeatures.getOrThrow(MiscOverworldPlacements.DISK_GRAVEL),
                                placedFeatures.getOrThrow(OrePlacements.ORE_GRAVEL)),
                        Set.of(GenerationStep.Decoration.UNDERGROUND_ORES)));
        context.register(
                ADD_MOUNTAIN_SGRAVEL_DISKS,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        mountainSgravelBiomes,
                        HolderSet.direct(
                                placedFeatures.getOrThrow(SGRAVEL_DISK_PLACED),
                                placedFeatures.getOrThrow(SGRAVEL_GRAVEL_DISK_PLACED),
                                placedFeatures.getOrThrow(MOUNTAIN_SGRAVEL_ORE_PLACED)),
                        GenerationStep.Decoration.UNDERGROUND_ORES));
        HolderSet<Biome> shoreRiverSgravelBiomes = HolderSet.direct(
                biomes.getOrThrow(Biomes.STONY_SHORE),
                biomes.getOrThrow(Biomes.RIVER),
                biomes.getOrThrow(Biomes.FROZEN_RIVER),
                biomes.getOrThrow(RiverBiomes.DESERT_RIVER),
                biomes.getOrThrow(RiverBiomes.JUNGLE_RIVER),
                biomes.getOrThrow(RiverBiomes.SWAMP_RIVER));
        context.register(
                REPLACE_SHORE_RIVER_SOFT_DISKS,
                new BiomeModifiers.RemoveFeaturesBiomeModifier(
                        shoreRiverSgravelBiomes,
                        HolderSet.direct(
                                placedFeatures.getOrThrow(MiscOverworldPlacements.DISK_SAND),
                                placedFeatures.getOrThrow(MiscOverworldPlacements.DISK_GRAVEL),
                                placedFeatures.getOrThrow(OrePlacements.ORE_GRAVEL)),
                        Set.of(GenerationStep.Decoration.UNDERGROUND_ORES)));
        context.register(
                ADD_SHORE_RIVER_SGRAVEL_DISKS,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        shoreRiverSgravelBiomes,
                        HolderSet.direct(
                                placedFeatures.getOrThrow(SHORE_RIVER_SGRAVEL_DISK_PLACED),
                                placedFeatures.getOrThrow(SHORE_RIVER_SGRAVEL_GRAVEL_DISK_PLACED),
                                placedFeatures.getOrThrow(SHORE_RIVER_SGRAVEL_ORE_PLACED)),
                        GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(
                REMOVE_JUNGLE_MELONS,
                new BiomeModifiers.RemoveFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_JUNGLE),
                        HolderSet.direct(
                                placedFeatures.getOrThrow(VegetationPlacements.PATCH_MELON),
                                placedFeatures.getOrThrow(VegetationPlacements.PATCH_MELON_SPARSE)),
                        Set.of(GenerationStep.Decoration.VEGETAL_DECORATION)));
        registerInfxSpawnModifiers(context);
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

    private static void registerOverworldResourceOreRemoval(
            BootstrapContext<BiomeModifier> context,
            HolderGetter<Biome> biomes,
            HolderGetter<PlacedFeature> placedFeatures) {
        context.register(
                REMOVE_OVERWORLD_RESOURCE_ORES,
                new BiomeModifiers.RemoveFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(
                                placedFeatures.getOrThrow(OrePlacements.ORE_COAL_UPPER),
                                placedFeatures.getOrThrow(OrePlacements.ORE_COAL_LOWER),
                                placedFeatures.getOrThrow(OrePlacements.ORE_IRON_UPPER),
                                placedFeatures.getOrThrow(OrePlacements.ORE_IRON_MIDDLE),
                                placedFeatures.getOrThrow(OrePlacements.ORE_IRON_SMALL),
                                placedFeatures.getOrThrow(OrePlacements.ORE_GOLD_EXTRA),
                                placedFeatures.getOrThrow(OrePlacements.ORE_GOLD),
                                placedFeatures.getOrThrow(OrePlacements.ORE_GOLD_LOWER),
                                placedFeatures.getOrThrow(OrePlacements.ORE_REDSTONE),
                                placedFeatures.getOrThrow(OrePlacements.ORE_REDSTONE_LOWER),
                                placedFeatures.getOrThrow(OrePlacements.ORE_DIAMOND),
                                placedFeatures.getOrThrow(OrePlacements.ORE_DIAMOND_MEDIUM),
                                placedFeatures.getOrThrow(OrePlacements.ORE_DIAMOND_LARGE),
                                placedFeatures.getOrThrow(OrePlacements.ORE_DIAMOND_BURIED),
                                placedFeatures.getOrThrow(OrePlacements.ORE_LAPIS),
                                placedFeatures.getOrThrow(OrePlacements.ORE_LAPIS_BURIED),
                                placedFeatures.getOrThrow(OrePlacements.ORE_COPPER),
                                placedFeatures.getOrThrow(OrePlacements.ORE_COPPER_LARGE),
                                placedFeatures.getOrThrow(OrePlacements.ORE_EMERALD)),
                        Set.of(GenerationStep.Decoration.UNDERGROUND_ORES)));
    }

    private static void registerInfxSpawnModifiers(BootstrapContext<BiomeModifier> context) {
        context.register(INFX_SPAWNS, new SpawnsBiomeModifier());
    }

}
