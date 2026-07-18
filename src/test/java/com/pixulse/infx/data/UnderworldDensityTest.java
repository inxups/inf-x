package com.pixulse.infx.data;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
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
    private static final int PASSAGE_MIN = -32;
    private static final int PASSAGE_MAX = 32;
    private static final int PASSAGE_STEP = 1;
    private static final int PASSAGE_BOTTOM_Y = 210;
    private static final int PASSAGE_TOP_Y = 234;

    @Test
    void layeredCavesKeepMiteOpennessNaturalTransitionsAndConnections() {
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
        boolean connectedPassage = hasSeparatorPassage(generator, randomState);

        assertAll(
                () -> assertTrue(stats.firstLayerOpen() > 0.45,
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
                () -> assertTrue(connectedPassage,
                        "a three-dimensional air path must connect the first and upper caves"),
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
        int straightEntranceColumns = 0;
        int partialEntranceColumns = 0;
        int columns = 0;
        boolean lowerStratumSolid = true;
        boolean topSolid = true;
        int[] stoneByY = new int[41];
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
                int separatorOpenBlocks = 0;
                for (int y = 216; y < 226; y++) {
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
                for (int y = 236; y < 296; y++) {
                    if (column.getBlock(y).isAir()) {
                        upperLayerOpen++;
                    }
                    upperLayerTotal++;
                }
                for (int y = 200; y <= 240; y++) {
                    if (column.getBlock(y).is(Blocks.STONE)) {
                        stoneByY[y - 200]++;
                    }
                }
                boolean connected = true;
                for (int y = 215; y <= 226; y++) {
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

    private static boolean hasSeparatorPassage(
            NoiseBasedChunkGenerator generator,
            RandomState randomState) {
        LevelHeightAccessor height = LevelHeightAccessor.create(-192, 512);
        int side = (PASSAGE_MAX - PASSAGE_MIN) / PASSAGE_STEP;
        int layers = PASSAGE_TOP_Y - PASSAGE_BOTTOM_Y + 1;
        int layerSize = side * side;
        boolean[] open = new boolean[layerSize * layers];
        for (int xIndex = 0; xIndex < side; xIndex++) {
            int x = PASSAGE_MIN + xIndex * PASSAGE_STEP;
            for (int zIndex = 0; zIndex < side; zIndex++) {
                int z = PASSAGE_MIN + zIndex * PASSAGE_STEP;
                NoiseColumn column = generator.getBaseColumn(x, z, height, randomState);
                for (int layer = 0; layer < layers; layer++) {
                    var block = column.getBlock(PASSAGE_BOTTOM_Y + layer);
                    open[index(xIndex, zIndex, layer, side)] = block.isAir() || block.is(Blocks.WATER);
                }
            }
        }

        boolean[] visited = new boolean[open.length];
        ArrayDeque<Integer> pending = new ArrayDeque<>();
        for (int xIndex = 0; xIndex < side; xIndex++) {
            for (int zIndex = 0; zIndex < side; zIndex++) {
                int start = index(xIndex, zIndex, 0, side);
                if (open[start]) {
                    visited[start] = true;
                    pending.add(start);
                }
            }
        }

        int[] xOffsets = {-1, 1, 0, 0, 0, 0};
        int[] zOffsets = {0, 0, -1, 1, 0, 0};
        int[] yOffsets = {0, 0, 0, 0, -1, 1};
        while (!pending.isEmpty()) {
            int current = pending.removeFirst();
            int layer = current / layerSize;
            if (layer == layers - 1) {
                return true;
            }
            int withinLayer = current % layerSize;
            int zIndex = withinLayer / side;
            int xIndex = withinLayer % side;
            for (int direction = 0; direction < xOffsets.length; direction++) {
                int nextX = xIndex + xOffsets[direction];
                int nextZ = zIndex + zOffsets[direction];
                int nextLayer = layer + yOffsets[direction];
                if (nextX < 0 || nextX >= side || nextZ < 0 || nextZ >= side
                        || nextLayer < 0 || nextLayer >= layers) {
                    continue;
                }
                int next = index(nextX, nextZ, nextLayer, side);
                if (open[next] && !visited[next]) {
                    visited[next] = true;
                    pending.add(next);
                }
            }
        }
        return false;
    }

    private static int index(int xIndex, int zIndex, int layer, int side) {
        return (layer * side + zIndex) * side + xIndex;
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
