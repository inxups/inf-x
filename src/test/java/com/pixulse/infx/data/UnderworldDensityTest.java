package com.pixulse.infx.data;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.datagen.ModWorldGen;
import com.pixulse.infx.world.Underworld;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.junit.jupiter.api.Test;

class UnderworldDensityTest {
    private static final long TEST_SEED = 0x1F1A7EL;

    @Test
    void underworldDensityFillsTheExpandedRangeWithStone() {
        for (long seed : List.of(TEST_SEED, TEST_SEED + 1, -TEST_SEED)) {
            assertSolidDensity(seed);
        }
    }

    private static void assertSolidDensity(long seed) {
        GeneratedTerrain terrain = generatedTerrain(seed);
        LevelHeightAccessor height = LevelHeightAccessor.create(Underworld.MIN_Y, Underworld.HEIGHT);

        for (int x = -128; x <= 128; x += 32) {
            for (int z = -128; z <= 128; z += 32) {
                var column = terrain.generator().getBaseColumn(x, z, height, terrain.randomState());
                for (int y = Underworld.MIN_Y; y < Underworld.MAX_Y_EXCLUSIVE; y++) {
                    assertTrue(
                            column.getBlock(y).is(Blocks.STONE),
                            coordinateMessage("stone", seed, x, y, z));
                }
            }
        }
    }

    private static GeneratedTerrain generatedTerrain(long seed) {
        HolderLookup.Provider registries = VanillaRegistries.createLookup();
        var noises = registries.lookupOrThrow(Registries.NOISE);
        var settings = new NoiseGeneratorSettings(
                NoiseSettings.create(Underworld.MIN_Y, Underworld.HEIGHT, 1, 2),
                Blocks.STONE.defaultBlockState(),
                Blocks.WATER.defaultBlockState(),
                ModWorldGen.underworldNoiseRouter(),
                SurfaceRules.state(Blocks.STONE.defaultBlockState()),
                List.of(),
                Underworld.SEA_LEVEL,
                false,
                false,
                false,
                true);
        RandomState randomState = RandomState.create(settings, noises, seed);
        Holder<net.minecraft.world.level.biome.Biome> biome =
                registries.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
        var generator = new NoiseBasedChunkGenerator(new FixedBiomeSource(biome), Holder.direct(settings));
        return new GeneratedTerrain(generator, randomState);
    }

    private static String coordinateMessage(String expected, long seed, int x, int y, int z) {
        return "Expected " + expected + " at seed=" + seed + ", x=" + x + ", y=" + y + ", z="
                + z;
    }

    private record GeneratedTerrain(NoiseBasedChunkGenerator generator, RandomState randomState) {}
}
