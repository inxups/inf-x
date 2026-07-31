package com.pixulse.infx.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.InfiniteX;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.data.worldgen.BootstrapContext;

/** Selects the Underworld's ordinary, lush and deep-dark regions from one 2D noise field. */
public final class InfXUnderworldBiomeSource extends BiomeSource {
    enum Region {
        ORDINARY,
        LUSH,
        DEEP_DARK
    }

    public static final ResourceKey<NormalNoise.NoiseParameters> BIOME_NOISE =
            ResourceKey.create(Registries.NOISE, InfiniteX.id("underworld_biome"));
    public static final int BIOME_NOISE_FIRST_OCTAVE = -2;
    public static final List<Double> BIOME_NOISE_AMPLITUDES = List.of(1.0, 0.5, 0.25);
    public static final double XZ_SCALE = 1.0 / 64.0;
    public static final double Y_SCALE = 0.0;
    public static final double DEEP_DARK_THRESHOLD = -0.63;
    public static final double LUSH_THRESHOLD = 0.50;

    public static final MapCodec<InfXUnderworldBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                            Biome.CODEC.fieldOf("ordinary").forGetter(InfXUnderworldBiomeSource::ordinary),
                            Biome.CODEC.fieldOf("lush").forGetter(InfXUnderworldBiomeSource::lush),
                            Biome.CODEC.fieldOf("deep_dark").forGetter(InfXUnderworldBiomeSource::deepDark))
                    .apply(instance, instance.stable(InfXUnderworldBiomeSource::new)));

    private final Holder<Biome> ordinary;
    private final Holder<Biome> lush;
    private final Holder<Biome> deepDark;

    public InfXUnderworldBiomeSource(Holder<Biome> ordinary, Holder<Biome> lush, Holder<Biome> deepDark) {
        this.ordinary = ordinary;
        this.lush = lush;
        this.deepDark = deepDark;
    }

    public static InfXUnderworldBiomeSource create(HolderGetter<Biome> biomes) {
        return new InfXUnderworldBiomeSource(
                biomes.getOrThrow(Underworld.BIOME),
                biomes.getOrThrow(Underworld.LUSH_BIOME),
                biomes.getOrThrow(Underworld.DEEP_DARK_BIOME));
    }

    public static void bootstrapNoiseParameters(BootstrapContext<NormalNoise.NoiseParameters> context) {
        context.register(BIOME_NOISE, new NormalNoise.NoiseParameters(BIOME_NOISE_FIRST_OCTAVE, BIOME_NOISE_AMPLITUDES));
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(this.ordinary, this.lush, this.deepDark);
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        int blockX = QuartPos.toBlock(quartX);
        int blockZ = QuartPos.toBlock(quartZ);
        double value = sampler.temperature().compute(new DensityFunction.SinglePointContext(blockX, 0, blockZ));
        return switch (regionForNoise(value)) {
            case DEEP_DARK -> this.deepDark;
            case LUSH -> this.lush;
            case ORDINARY -> this.ordinary;
        };
    }

    static Region regionForNoise(double value) {
        if (value <= DEEP_DARK_THRESHOLD) {
            return Region.DEEP_DARK;
        }
        return value >= LUSH_THRESHOLD ? Region.LUSH : Region.ORDINARY;
    }

    Holder<Biome> ordinary() {
        return this.ordinary;
    }

    Holder<Biome> lush() {
        return this.lush;
    }

    Holder<Biome> deepDark() {
        return this.deepDark;
    }
}
