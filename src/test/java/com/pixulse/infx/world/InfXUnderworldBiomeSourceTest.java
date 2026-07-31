package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.junit.jupiter.api.Test;

class InfXUnderworldBiomeSourceTest {
    @Test
    void thresholdsAreStrictAndRegionsAreMutuallyExclusive() {
        assertEquals(InfXUnderworldBiomeSource.Region.DEEP_DARK,
                InfXUnderworldBiomeSource.regionForNoise(-0.63));
        assertEquals(InfXUnderworldBiomeSource.Region.ORDINARY,
                InfXUnderworldBiomeSource.regionForNoise(-0.629999));
        assertEquals(InfXUnderworldBiomeSource.Region.ORDINARY,
                InfXUnderworldBiomeSource.regionForNoise(0.499999));
        assertEquals(InfXUnderworldBiomeSource.Region.LUSH,
                InfXUnderworldBiomeSource.regionForNoise(0.50));
    }

    @Test
    void biomeSelectionIgnoresHeightAndUsesTheTemperatureNoiseValue() {
        Holder<Biome> ordinary = biome();
        Holder<Biome> lush = biome();
        Holder<Biome> deepDark = biome();
        InfXUnderworldBiomeSource source = new InfXUnderworldBiomeSource(ordinary, lush, deepDark);

        assertSame(lush, source.getNoiseBiome(0, -32, 0, sampler(0.75)));
        assertSame(lush, source.getNoiseBiome(0, 64, 0, sampler(0.75)));
        assertSame(deepDark, source.getNoiseBiome(0, 0, 0, sampler(-0.75)));
        assertSame(ordinary, source.getNoiseBiome(0, 0, 0, sampler(0.0)));
    }

    @Test
    void constantsDescribeTheLowFrequencyTwoDimensionalNoise() {
        assertEquals(1.0 / 64.0, InfXUnderworldBiomeSource.XZ_SCALE);
        assertEquals(0.0, InfXUnderworldBiomeSource.Y_SCALE);
        assertEquals(-2, InfXUnderworldBiomeSource.BIOME_NOISE_FIRST_OCTAVE);
        assertEquals(List.of(1.0, 0.5, 0.25), InfXUnderworldBiomeSource.BIOME_NOISE_AMPLITUDES);
    }

    private static Climate.Sampler sampler(double value) {
        DensityFunction temperature = DensityFunctions.constant(value);
        DensityFunction zero = DensityFunctions.zero();
        return new Climate.Sampler(temperature, zero, zero, zero, zero, zero, List.of());
    }

    private static Holder<Biome> biome() {
        return Holder.direct(new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.5F)
                .downfall(0.0F)
                .specialEffects(new BiomeSpecialEffects.Builder().waterColor(4_159_204).build())
                .mobSpawnSettings(MobSpawnSettings.EMPTY)
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .build());
    }
}
