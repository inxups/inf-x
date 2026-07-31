package com.pixulse.infx.world;

import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

/** Carves a warped, layered cheese cavern from rare deep-dark macro-region centers. */
public final class InfXUnderworldLargeCaveCarver extends WorldCarver<CarverConfiguration> {
    private static final int NOISE_CELL_SIZE = 4;
    private static final int NOISE_GRID_WIDTH = 16 / NOISE_CELL_SIZE + 1;
    private static final int NOISE_GRID_HEIGHT =
            (Underworld.LARGE_CAVE_MAX_Y - Underworld.LARGE_CAVE_MIN_Y) / NOISE_CELL_SIZE + 1;
    private static final int BIOME_FADE_RADIUS = 16;
    private static final int[][] BIOME_FADE_OFFSETS = {
        {0, 0},
        {BIOME_FADE_RADIUS, 0},
        {-BIOME_FADE_RADIUS, 0},
        {0, BIOME_FADE_RADIUS},
        {0, -BIOME_FADE_RADIUS},
        {12, 12},
        {12, -12},
        {-12, 12},
        {-12, -12}
    };
    private static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();

    public InfXUnderworldLargeCaveCarver() {
        super(CarverConfiguration.CODEC.codec());
    }

    @Override
    public int getRange() {
        return 8;
    }

    @Override
    public boolean isStartChunk(CarverConfiguration configuration, RandomSource random) {
        return true;
    }

    @Override
    public boolean carve(
            CarvingContext context,
            CarverConfiguration configuration,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            RandomSource random,
            Aquifer aquifer,
            ChunkPos sourceChunkPos,
            CarvingMask mask) {
        if (!isMacroCenter(sourceChunkPos)
                || !isDeepDark(biomeGetter, sourceChunkPos.getMiddleBlockX(), sourceChunkPos.getMiddleBlockZ())) {
            return false;
        }

        int centerX = sourceChunkPos.getMiddleBlockX();
        int centerZ = sourceChunkPos.getMiddleBlockZ();
        ChunkPos targetChunk = chunk.getPos();
        if (!canAffect(targetChunk, centerX, centerZ)) {
            return false;
        }

        int minY = Math.max(context.getMinGenY(), Underworld.LARGE_CAVE_MIN_Y + 1);
        int maxY = Math.min(context.getMinGenY() + context.getGenDepth() - 1, Underworld.LARGE_CAVE_MAX_Y - 1);
        long caveSeed = random.nextLong();
        double[][][] noiseGrid = sampleNoiseGrid(caveSeed, targetChunk, centerX, centerZ);
        boolean carved = false;
        BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();

        for (int localX = 0; localX < 16; localX++) {
            int worldX = targetChunk.getBlockX(localX);
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldZ = targetChunk.getBlockZ(localZ);
                double biomeWeight = deepDarkInteriorWeight(biomeGetter, worldX, worldZ);
                if (biomeWeight == 0.0) {
                    continue;
                }
                double edgeThreshold = (1.0 - biomeWeight) * 0.40;
                for (int worldY = minY; worldY <= maxY; worldY++) {
                    if (interpolatedNoise(noiseGrid, localX, worldY, localZ) <= edgeThreshold) {
                        continue;
                    }
                    position.set(worldX, worldY, worldZ);
                    if (mask.get(localX, worldY, localZ)) {
                        continue;
                    }
                    BlockState state = chunk.getBlockState(position);
                    if (!canReplace(state, worldY)) {
                        continue;
                    }
                    mask.set(localX, worldY, localZ);
                    chunk.setBlockState(position, CAVE_AIR);
                    carved = true;
                }
            }
        }
        return carved;
    }

    static double[][][] sampleNoiseGrid(long seed, ChunkPos targetChunk, int centerX, int centerZ) {
        double[][][] samples = new double[NOISE_GRID_WIDTH][NOISE_GRID_HEIGHT][NOISE_GRID_WIDTH];
        for (int gridX = 0; gridX < NOISE_GRID_WIDTH; gridX++) {
            double relativeX = targetChunk.getMinBlockX() + gridX * NOISE_CELL_SIZE - centerX;
            for (int gridZ = 0; gridZ < NOISE_GRID_WIDTH; gridZ++) {
                double relativeZ = targetChunk.getMinBlockZ() + gridZ * NOISE_CELL_SIZE - centerZ;
                for (int gridY = 0; gridY < NOISE_GRID_HEIGHT; gridY++) {
                    double worldY = Underworld.LARGE_CAVE_MIN_Y + gridY * NOISE_CELL_SIZE;
                    samples[gridX][gridY][gridZ] =
                            InfXUnderworldCaveNoise.sample(seed, relativeX, worldY, relativeZ);
                }
            }
        }
        return samples;
    }

    static double interpolatedNoise(double[][][] samples, int localX, int worldY, int localZ) {
        int cellX = localX / NOISE_CELL_SIZE;
        int cellY = (worldY - Underworld.LARGE_CAVE_MIN_Y) / NOISE_CELL_SIZE;
        int cellZ = localZ / NOISE_CELL_SIZE;
        double factorX = (localX % NOISE_CELL_SIZE) / (double) NOISE_CELL_SIZE;
        double factorY = Math.floorMod(worldY - Underworld.LARGE_CAVE_MIN_Y, NOISE_CELL_SIZE)
                / (double) NOISE_CELL_SIZE;
        double factorZ = (localZ % NOISE_CELL_SIZE) / (double) NOISE_CELL_SIZE;
        return net.minecraft.util.Mth.lerp3(
                factorX,
                factorY,
                factorZ,
                samples[cellX][cellY][cellZ],
                samples[cellX + 1][cellY][cellZ],
                samples[cellX][cellY + 1][cellZ],
                samples[cellX + 1][cellY + 1][cellZ],
                samples[cellX][cellY][cellZ + 1],
                samples[cellX + 1][cellY][cellZ + 1],
                samples[cellX][cellY + 1][cellZ + 1],
                samples[cellX + 1][cellY + 1][cellZ + 1]);
    }

    static boolean isMacroCenter(ChunkPos chunkPos) {
        return chunkPos.x() == macroCenterChunk(chunkPos.x())
                && chunkPos.z() == macroCenterChunk(chunkPos.z());
    }

    static int macroCenterChunk(int chunkCoordinate) {
        return Math.floorDiv(chunkCoordinate, Underworld.LARGE_CAVE_MACRO_CHUNK_SIZE)
                * Underworld.LARGE_CAVE_MACRO_CHUNK_SIZE
                + Underworld.LARGE_CAVE_MACRO_CENTER_OFFSET;
    }

    static boolean canAffect(ChunkPos chunkPos, int centerX, int centerZ) {
        int deltaX = distanceToRange(centerX, chunkPos.getMinBlockX(), chunkPos.getMaxBlockX());
        int deltaZ = distanceToRange(centerZ, chunkPos.getMinBlockZ(), chunkPos.getMaxBlockZ());
        return deltaX * deltaX + deltaZ * deltaZ
                <= Underworld.LARGE_CAVE_OUTER_RADIUS * Underworld.LARGE_CAVE_OUTER_RADIUS;
    }

    private static int distanceToRange(int value, int minimum, int maximum) {
        if (value < minimum) {
            return minimum - value;
        }
        return value > maximum ? value - maximum : 0;
    }

    static double deepDarkInteriorWeight(Function<BlockPos, Holder<Biome>> biomeGetter, int x, int z) {
        if (!isDeepDark(biomeGetter, x, z)) {
            return 0.0;
        }
        int deepDarkSamples = 0;
        for (int[] offset : BIOME_FADE_OFFSETS) {
            deepDarkSamples += isDeepDark(biomeGetter, x + offset[0], z + offset[1]) ? 1 : 0;
        }
        return deepDarkSamples / (double) BIOME_FADE_OFFSETS.length;
    }

    static boolean canReplace(BlockState state, int y) {
        return state.is(Blocks.STONE)
                || state.is(Blocks.DEEPSLATE)
                || (y >= Underworld.LARGE_CAVE_INTERNAL_BEDROCK_MIN_Y
                        && y < Underworld.LARGE_CAVE_INTERNAL_BEDROCK_MAX_Y_EXCLUSIVE
                        && state.is(Blocks.BEDROCK));
    }

    static boolean isDeepDark(Function<BlockPos, Holder<Biome>> biomeGetter, int x, int z) {
        return biomeGetter.apply(new BlockPos(x, 0, z)).is(Underworld.DEEP_DARK_BIOME);
    }
}
