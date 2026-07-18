package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
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
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

final class ModWorldGen {
    private static final ResourceKey<ConfiguredFeature<?, ?>> SILVER_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("silver_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> MITHRIL_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("mithril_ore"));
    private static final ResourceKey<PlacedFeature> SILVER_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("silver_ore"));
    private static final ResourceKey<PlacedFeature> MITHRIL_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("mithril_ore"));
    private static final ResourceKey<BiomeModifier> ADD_SILVER_ORE =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_silver_ore"));
    private static final ResourceKey<BiomeModifier> ADD_MITHRIL_ORE =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, InfiniteX.id("add_mithril_ore"));

    private ModWorldGen() {}

    static RegistrySetBuilder builder() {
        return new RegistrySetBuilder()
                .add(Registries.CONFIGURED_FEATURE, ModWorldGen::bootstrapConfiguredFeatures)
                .add(Registries.PLACED_FEATURE, ModWorldGen::bootstrapPlacedFeatures)
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModWorldGen::bootstrapBiomeModifiers);
    }

    private static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        registerConfiguredOre(context, SILVER_ORE_CONFIGURED, ModBlocks.SILVER_ORE.get().defaultBlockState(), 6);
        registerConfiguredOre(context, MITHRIL_ORE_CONFIGURED, ModBlocks.MITHRIL_ORE.get().defaultBlockState(), 3);
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
