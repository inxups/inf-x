package com.pixulse.infx.data;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
    void underworldTerrainKeepsStoneOutsideTheShiftedBand() {
        for (long seed : List.of(TEST_SEED, TEST_SEED + 1, -TEST_SEED)) {
            assertTerrainBand(seed);
        }
    }

    private static void assertTerrainBand(long seed) {
        GeneratedTerrain terrain = generatedTerrain(seed);
        LevelHeightAccessor height = LevelHeightAccessor.create(Underworld.MIN_Y, Underworld.HEIGHT);
        int waterBlocks = 0;
        int airBlocks = 0;
        int bandStoneBlocks = 0;

        for (int x = -256; x <= 256; x += 32) {
            for (int z = -256; z <= 256; z += 32) {
                var column = terrain.generator().getBaseColumn(x, z, height, terrain.randomState());
                for (int y = Underworld.MIN_Y; y < Underworld.TERRAIN_MIN_Y; y++) {
                    var block = column.getBlock(y);
                    assertTrue(
                            block.is(Blocks.STONE),
                            coordinateMessage("stone", seed, x, y, z));
                    if (y < Underworld.WATER_MIN_Y) {
                        assertFalse(
                                block.is(Blocks.WATER),
                                coordinateMessage("not water", seed, x, y, z));
                    }
                }
                for (int y = Underworld.TERRAIN_MIN_Y; y < Underworld.WATER_LEVEL; y++) {
                    var block = column.getBlock(y);
                    assertTrue(
                            block.is(Blocks.STONE) || block.is(Blocks.WATER),
                            coordinateMessage("stone or water", seed, x, y, z));
                    if (block.is(Blocks.WATER)) {
                        waterBlocks++;
                    } else {
                        bandStoneBlocks++;
                    }
                }
                for (int y = Underworld.WATER_LEVEL; y < Underworld.TERRAIN_MAX_Y_EXCLUSIVE; y++) {
                    var block = column.getBlock(y);
                    assertTrue(
                            block.is(Blocks.STONE) || block.isAir(),
                            coordinateMessage("stone or air", seed, x, y, z));
                    if (block.isAir()) {
                        airBlocks++;
                    } else {
                        bandStoneBlocks++;
                    }
                }
                for (int y = Underworld.TERRAIN_MAX_Y_EXCLUSIVE;
                        y < Underworld.MAX_Y_EXCLUSIVE;
                        y++) {
                    assertTrue(
                            column.getBlock(y).is(Blocks.STONE),
                            coordinateMessage("stone", seed, x, y, z));
                }
            }
        }

        int sampledWater = waterBlocks;
        int sampledAir = airBlocks;
        int sampledStone = bandStoneBlocks;
        assertAll(
                "seed=" + seed,
                () -> assertTrue(sampledWater > 0, "the shifted low terrain must contain water"),
                () -> assertTrue(sampledAir > 0, "the shifted terrain must contain air caves"),
                () -> assertTrue(sampledStone > 0, "the shifted terrain must retain stone"));
    }

    @Test
    void legacyTerrainIsDeterministicAndChangesWithTheWorldSeed() {
        long first = terrainHash(generatedTerrain(TEST_SEED));
        long repeated = terrainHash(generatedTerrain(TEST_SEED));
        long different = terrainHash(generatedTerrain(TEST_SEED + 1));

        assertEquals(first, repeated);
        assertNotEquals(first, different, "the wrapped BlendedNoise must be reseeded by RandomState");
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
                Underworld.WATER_LEVEL,
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

    private static long terrainHash(GeneratedTerrain terrain) {
        LevelHeightAccessor height = LevelHeightAccessor.create(Underworld.MIN_Y, Underworld.HEIGHT);
        long hash = 1_125_899_906_842_597L;
        for (int x = -128; x <= 128; x += 16) {
            for (int z = -128; z <= 128; z += 16) {
                var column = terrain.generator().getBaseColumn(x, z, height, terrain.randomState());
                for (int y = Underworld.TERRAIN_MIN_Y;
                        y < Underworld.TERRAIN_MAX_Y_EXCLUSIVE;
                        y += 2) {
                    var block = column.getBlock(y);
                    int value = block.is(Blocks.STONE) ? 1 : block.is(Blocks.WATER) ? 2 : 3;
                    hash = hash * 31 + value;
                }
            }
        }
        return hash;
    }

    private static String coordinateMessage(String expected, long seed, int x, int y, int z) {
        return "Expected " + expected + " at seed=" + seed + ", x=" + x + ", y=" + y + ", z="
                + z;
    }

    private record GeneratedTerrain(NoiseBasedChunkGenerator generator, RandomState randomState) {}
}
