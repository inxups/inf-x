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
    private static final ResourceKey<ConfiguredFeature<?, ?>> MITHRIL_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, InfiniteX.id("mithril_ore"));
    private static final ResourceKey<PlacedFeature> MITHRIL_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, InfiniteX.id("mithril_ore"));
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
        context.register(
                MITHRIL_ORE_CONFIGURED,
                new ConfiguredFeature<>(
                        Feature.ORE,
                        new OreConfiguration(
                                new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
                                ModBlocks.MITHRIL_ORE.get().defaultBlockState(),
                                3)));
    }

    private static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures =
                context.lookup(Registries.CONFIGURED_FEATURE);
        context.register(
                MITHRIL_ORE_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(MITHRIL_ORE_CONFIGURED),
                        List.of(
                                CountPlacement.of(1),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.of(BiasedToBottomHeight.of(
                                        VerticalAnchor.absolute(0), VerticalAnchor.absolute(32), 1)),
                                BiomeFilter.biome())));
    }

    private static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        context.register(
                ADD_MITHRIL_ORE,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(placedFeatures.getOrThrow(MITHRIL_ORE_PLACED)),
                        GenerationStep.Decoration.UNDERGROUND_ORES));
    }
}
