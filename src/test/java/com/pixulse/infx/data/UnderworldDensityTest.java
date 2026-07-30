package com.pixulse.infx.data;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.pixulse.infx.datagen.ModWorldGen;
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
    void layeredCavesKeepMiteOpennessNaturalTransitionsAndConnections() {
        HolderLookup.Provider registries = VanillaRegistries.createLookup();
        var noises = registries.lookupOrThrow(Registries.NOISE);
        var router = ModWorldGen.underworldNoiseRouter(noises);
        var settings = new NoiseGeneratorSettings(
                NoiseSettings.create(-128, 320, 1, 2),
                Blocks.STONE.defaultBlockState(),
                Blocks.WATER.defaultBlockState(),
                router,
                SurfaceRules.state(Blocks.STONE.defaultBlockState()),
                List.of(),
                12,
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
                () -> assertTrue(stats.firstLayerOpen() > 0.44,
                        () -> "first cave should be spacious, open fraction=" + stats.firstLayerOpen()),
                () -> assertTrue(stats.firstLayerOpen() < 0.85,
                        () -> "first cave still needs stone separators, open fraction=" + stats.firstLayerOpen()),
                () -> assertTrue(stats.separatorStone() > 0.70,
                        () -> "separator should be mostly stone, stone fraction=" + stats.separatorStone()),
                () -> assertTrue(stats.separatorStone() < 0.95,
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
                () -> assertTrue(stats.straightEntranceColumns() > 0.02,
                        () -> "separator needs direct entrances, column fraction="
                                + stats.straightEntranceColumns()),
                () -> assertTrue(stats.straightEntranceColumns() < 0.25,
                        () -> "direct entrances must not erase the separator, column fraction="
                                + stats.straightEntranceColumns()),
                () -> assertTrue(stats.partialEntranceColumns() > 0.05,
                        () -> "sloped cave mouths should change laterally, partial column fraction="
                                + stats.partialEntranceColumns()),
                () -> assertTrue(stats.partialEntranceColumns() < 0.35,
                        () -> "partial openings must leave a stone-dominant separator, fraction="
                                + stats.partialEntranceColumns()),
                () -> assertTrue(stats.maxTransitionStoneStep() < 0.30,
                        () -> "layer transitions must not change on a single flat Y plane, maximum step="
                                + stats.maxTransitionStoneStep()),
                () -> assertTrue(stats.lowerStratumSolid(), "Y=-1 must remain solid"),
                () -> assertTrue(stats.topSolid(), "top density must remain solid"));
    }

    private static CaveStats sampleBaseTerrain(
            NoiseBasedChunkGenerator generator,
            RandomState randomState) {
        LevelHeightAccessor height = LevelHeightAccessor.create(-128, 320);
        int firstLayerOpen = 0;
        int firstLayerTotal = 0;
        int separatorStone = 0;
        int separatorTotal = 0;
        int upperLayerOpen = 0;
        int upperLayerTotal = 0;
        int straightEntranceColumns = 0;
        int partialEntranceColumns = 0;
        int columns = 0;
        boolean lowerStratumSolid = true;
        boolean topSolid = true;
        int[] stoneByY = new int[41];
        for (int x = SAMPLE_MIN; x < SAMPLE_MAX; x += SAMPLE_STEP) {
            for (int z = SAMPLE_MIN; z < SAMPLE_MAX; z += SAMPLE_STEP) {
                NoiseColumn column = generator.getBaseColumn(x, z, height, randomState);
                lowerStratumSolid &= column.getBlock(-1).is(Blocks.STONE);
                topSolid &= column.getBlock(191).is(Blocks.STONE);

                for (int y = 0; y < 88; y++) {
                    var block = column.getBlock(y);
                    if (block.isAir() || block.is(Blocks.WATER)) {
                        firstLayerOpen++;
                    }
                    firstLayerTotal++;
                }
                int separatorOpenBlocks = 0;
                for (int y = 88; y < 98; y++) {
                    if (column.getBlock(y).is(Blocks.STONE)) {
                        separatorStone++;
                    } else {
                        separatorOpenBlocks++;
                    }
                    separatorTotal++;
                }
                if (separatorOpenBlocks > 0 && separatorOpenBlocks < 10) {
                    partialEntranceColumns++;
                }
                for (int y = 108; y < 168; y++) {
                    if (column.getBlock(y).isAir()) {
                        upperLayerOpen++;
                    }
                    upperLayerTotal++;
                }
                for (int y = 72; y <= 112; y++) {
                    if (column.getBlock(y).is(Blocks.STONE)) {
                        stoneByY[y - 72]++;
                    }
                }
                boolean connected = true;
                for (int y = 87; y <= 98; y++) {
                    if (!column.getBlock(y).isAir()) {
                        connected = false;
                        break;
                    }
                }
                if (connected) {
                    straightEntranceColumns++;
                }
                columns++;
            }
        }
        int maxTransitionStoneStep = 0;
        for (int index = 1; index < stoneByY.length; index++) {
            maxTransitionStoneStep = Math.max(
                    maxTransitionStoneStep,
                    Math.abs(stoneByY[index] - stoneByY[index - 1]));
        }
        return new CaveStats(
                (double) firstLayerOpen / firstLayerTotal,
                (double) separatorStone / separatorTotal,
                (double) upperLayerOpen / upperLayerTotal,
                (double) straightEntranceColumns / columns,
                (double) partialEntranceColumns / columns,
                (double) maxTransitionStoneStep / columns,
                lowerStratumSolid,
                topSolid);
    }

    private record CaveStats(
            double firstLayerOpen,
            double separatorStone,
            double upperLayerOpen,
            double straightEntranceColumns,
            double partialEntranceColumns,
            double maxTransitionStoneStep,
            boolean lowerStratumSolid,
            boolean topSolid) {}
}
