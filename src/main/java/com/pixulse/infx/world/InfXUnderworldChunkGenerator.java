package com.pixulse.infx.world;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.blending.Blender;

/** Applies the Underworld's dry lower fluid rule, ancient-city caves and InfX-style internal bedrock. */
public final class InfXUnderworldChunkGenerator extends NoiseBasedChunkGenerator {
    public static final MapCodec<InfXUnderworldChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource),
                            NoiseGeneratorSettings.CODEC
                                    .fieldOf("settings")
                                    .forGetter(InfXUnderworldChunkGenerator::generatorSettings))
                    .apply(instance, instance.stable(InfXUnderworldChunkGenerator::new)));
    private final Supplier<Aquifer.FluidPicker> fluidPicker;

    public InfXUnderworldChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
        this.fluidPicker = Suppliers.memoize(() -> InfXUnderworldFluidPicker.create(settings.value()));
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    protected NoiseChunk createNoiseChunk(
            ChunkAccess chunk, StructureManager structureManager, Blender blender, RandomState randomState) {
        return NoiseChunk.forChunk(
                chunk,
                randomState,
                Beardifier.forStructuresInChunk(structureManager, chunk.getPos()),
                generatorSettings().value(),
                this.fluidPicker.get(),
                blender);
    }

    @Override
    public int getBaseHeight(
            int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
        if (type == Heightmap.Types.WORLD_SURFACE_WG && isDeepDarkColumn(x, z, randomState)) {
            return Underworld.LARGE_CAVE_MIN_Y;
        }
        return super.getBaseHeight(x, z, type, heightAccessor, randomState);
    }

    private boolean isDeepDarkColumn(int x, int z, RandomState randomState) {
        return this.biomeSource.getNoiseBiome(
                        QuartPos.fromBlock(x), 0, QuartPos.fromBlock(z), randomState.sampler())
                .is(Underworld.DEEP_DARK_BIOME);
    }

    @Override
    public void applyCarvers(
            WorldGenRegion region,
            long seed,
            RandomState randomState,
            BiomeManager biomeManager,
            StructureManager structureManager,
            ChunkAccess chunk) {
        // Deliberately skip NoiseBasedChunkGenerator's biome carvers: lower caves must have a real city start.
        if (!SharedConstants.DEBUG_DISABLE_CARVERS) {
            InfXUnderworldAncientCityCave.carveAroundAncientCities(region, seed, structureManager, chunk);
        }
    }

    @Override
    public void buildSurface(
            ChunkAccess protoChunk,
            WorldGenerationContext context,
            RandomState randomState,
            StructureManager structureManager,
            BiomeManager biomeManager,
            Registry<Biome> biomeRegistry,
            Blender blender) {
        super.buildSurface(protoChunk, context, randomState, structureManager, biomeManager, biomeRegistry, blender);
        InfXUnderworldBedrockStrata.apply(protoChunk, randomState);
    }
}
