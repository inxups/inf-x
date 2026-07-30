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
    @Test
    void underworldNoiseRouterFillsTheConfiguredHeightWithStone() {
        HolderLookup.Provider registries = VanillaRegistries.createLookup();
        var noises = registries.lookupOrThrow(Registries.NOISE);
        var settings = new NoiseGeneratorSettings(
                NoiseSettings.create(Underworld.MIN_Y, Underworld.HEIGHT, 1, 2),
                Blocks.STONE.defaultBlockState(),
                Blocks.WATER.defaultBlockState(),
                ModWorldGen.underworldNoiseRouter(),
                SurfaceRules.state(Blocks.STONE.defaultBlockState()),
                List.of(),
                0,
                false,
                false,
                false,
                true);
        RandomState randomState = RandomState.create(settings, noises, 0x1F1A7EL);
        Holder<net.minecraft.world.level.biome.Biome> biome =
                registries.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
        var generator = new NoiseBasedChunkGenerator(new FixedBiomeSource(biome), Holder.direct(settings));
        LevelHeightAccessor height = LevelHeightAccessor.create(Underworld.MIN_Y, Underworld.HEIGHT);

        for (int x : List.of(-256, -1, 0, 127, 256)) {
            for (int z : List.of(-256, -1, 0, 127, 256)) {
                var column = generator.getBaseColumn(x, z, height, randomState);
                for (int y = Underworld.MIN_Y; y < Underworld.MAX_Y_EXCLUSIVE; y++) {
                    assertTrue(
                            column.getBlock(y).is(Blocks.STONE),
                            "Expected stone at x=" + x + ", y=" + y + ", z=" + z);
                }
            }
        }
    }
}
