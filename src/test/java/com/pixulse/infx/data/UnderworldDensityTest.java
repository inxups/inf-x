package com.pixulse.infx.data;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
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
    private static final int SAMPLE_MIN = -256;
    private static final int SAMPLE_MAX = 256;
    private static final int SAMPLE_STEP = 32;

    @Test
    void miteFirstCaveKeepsTheRequestedOpennessAndConnections() {
        HolderLookup.Provider registries = VanillaRegistries.createLookup();
        var noises = registries.lookupOrThrow(Registries.NOISE);
        var router = ModWorldGen.underworldNoiseRouter(noises);
        var settings = new NoiseGeneratorSettings(
                NoiseSettings.create(-192, 512, 1, 2),
                Blocks.STONE.defaultBlockState(),
                Blocks.WATER.defaultBlockState(),
                router,
                SurfaceRules.state(Blocks.STONE.defaultBlockState()),
                List.of(),
                140,
                false,
                false,
                false,
                true);
        RandomState randomState = RandomState.create(settings, noises, 0x1F1A7EL);
        var biome = registries.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
        var generator = new NoiseBasedChunkGenerator(
                new FixedBiomeSource(biome),
                Holder.direct(settings));
        CaveStats stats = sampleBaseTerrain(generator, randomState);

        assertAll(
                () -> assertTrue(stats.firstLayerOpen() > 0.45,
                        () -> "first cave should be spacious, open fraction=" + stats.firstLayerOpen()),
                () -> assertTrue(stats.firstLayerOpen() < 0.85,
                        () -> "first cave still needs stone separators, open fraction=" + stats.firstLayerOpen()),
                () -> assertTrue(stats.separatorStone() > 0.65,
                        () -> "separator should be mostly stone, stone fraction=" + stats.separatorStone()),
                () -> assertTrue(stats.separatorStone() < 0.98,
                        () -> "separator needs entrances, stone fraction=" + stats.separatorStone()),
                () -> assertTrue(stats.upperLayerOpen() > 0.05,
                        () -> "upper cave still needs open space, open fraction=" + stats.upperLayerOpen()),
                () -> assertTrue(stats.upperLayerOpen() < 0.45,
                        () -> "upper cave should remain compact, open fraction=" + stats.upperLayerOpen()),
                () -> assertTrue(stats.upperLayerOpen() + 0.10 < stats.firstLayerOpen(),
                        () -> "upper cave must be less spacious: first="
                                + stats.firstLayerOpen()
                                + ", upper="
                                + stats.upperLayerOpen()),
                () -> assertTrue(stats.entranceColumns() > 0.01,
                        () -> "separator needs connected entrances, column fraction=" + stats.entranceColumns()),
                () -> assertTrue(stats.entranceColumns() < 0.35,
                        () -> "entrances must not erase the separator, column fraction=" + stats.entranceColumns()),
                () -> assertTrue(stats.lowerStratumSolid(), "Y=127 must remain solid"),
                () -> assertTrue(stats.topSolid(), "top density must remain solid"));
    }

    private static CaveStats sampleBaseTerrain(
            NoiseBasedChunkGenerator generator,
            RandomState randomState) {
        LevelHeightAccessor height = LevelHeightAccessor.create(-192, 512);
        int firstLayerOpen = 0;
        int firstLayerTotal = 0;
        int separatorStone = 0;
        int separatorTotal = 0;
        int upperLayerOpen = 0;
        int upperLayerTotal = 0;
        int entranceColumns = 0;
        int columns = 0;
        boolean lowerStratumSolid = true;
        boolean topSolid = true;
        for (int x = SAMPLE_MIN; x < SAMPLE_MAX; x += SAMPLE_STEP) {
            for (int z = SAMPLE_MIN; z < SAMPLE_MAX; z += SAMPLE_STEP) {
                NoiseColumn column = generator.getBaseColumn(x, z, height, randomState);
                lowerStratumSolid &= column.getBlock(127).is(Blocks.STONE);
                topSolid &= column.getBlock(319).is(Blocks.STONE);

                for (int y = 128; y < 216; y++) {
                    var block = column.getBlock(y);
                    if (block.isAir() || block.is(Blocks.WATER)) {
                        firstLayerOpen++;
                    }
                    firstLayerTotal++;
                }
                for (int y = 216; y < 226; y++) {
                    if (column.getBlock(y).is(Blocks.STONE)) {
                        separatorStone++;
                    }
                    separatorTotal++;
                }
                for (int y = 236; y < 296; y++) {
                    if (column.getBlock(y).isAir()) {
                        upperLayerOpen++;
                    }
                    upperLayerTotal++;
                }
                boolean connected = true;
                for (int y = 215; y <= 226; y++) {
                    if (!column.getBlock(y).isAir()) {
                        connected = false;
                        break;
                    }
                }
                if (connected) {
                    entranceColumns++;
                }
                columns++;
            }
        }
        return new CaveStats(
                (double) firstLayerOpen / firstLayerTotal,
                (double) separatorStone / separatorTotal,
                (double) upperLayerOpen / upperLayerTotal,
                (double) entranceColumns / columns,
                lowerStratumSolid,
                topSolid);
    }

    private record CaveStats(
            double firstLayerOpen,
            double separatorStone,
            double upperLayerOpen,
            double entranceColumns,
            boolean lowerStratumSolid,
            boolean topSolid) {}
}
