package com.pixulse.infx.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/** Carves one seed-stable cavern around every actual Underworld ancient-city start. */
final class InfXUnderworldAncientCityCave {
    private static final int NOISE_CELL_SIZE = 4;
    private static final int NOISE_GRID_WIDTH = 16 / NOISE_CELL_SIZE + 1;
    private static final int NOISE_GRID_HEIGHT =
            (Underworld.LARGE_CAVE_MAX_Y - Underworld.LARGE_CAVE_MIN_Y) / NOISE_CELL_SIZE + 1;
    private static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();

    private InfXUnderworldAncientCityCave() {}

    static boolean carveAroundAncientCities(
            WorldGenRegion region,
            long worldSeed,
            StructureManager structureManager,
            ChunkAccess targetChunk) {
        if (!(targetChunk instanceof ProtoChunk protoChunk)) {
            return false;
        }

        Structure ancientCity = region.registryAccess()
                .lookupOrThrow(Registries.STRUCTURE)
                .getOrThrow(Underworld.ANCIENT_CITY)
                .value();
        ChunkPos targetPos = targetChunk.getPos();
        CarvingMask mask = protoChunk.getOrCreateCarvingMask();
        boolean carved = false;

        for (int offsetX = -Underworld.LARGE_CAVE_STRUCTURE_SCAN_CHUNK_RANGE;
                offsetX <= Underworld.LARGE_CAVE_STRUCTURE_SCAN_CHUNK_RANGE;
                offsetX++) {
            for (int offsetZ = -Underworld.LARGE_CAVE_STRUCTURE_SCAN_CHUNK_RANGE;
                    offsetZ <= Underworld.LARGE_CAVE_STRUCTURE_SCAN_CHUNK_RANGE;
                    offsetZ++) {
                ChunkAccess sourceChunk = region.getChunk(targetPos.x() + offsetX, targetPos.z() + offsetZ);
                StructureStart start = structureManager.getStartForStructure(
                        SectionPos.bottomOf(sourceChunk), ancientCity, sourceChunk);
                if (start == null || !start.isValid()) {
                    continue;
                }
                BlockPos caveCenter = start.getPieces().getFirst().getBoundingBox().getCenter();
                carved |= carveFromStart(
                        worldSeed,
                        start.getChunkPos(),
                        caveCenter.getX(),
                        caveCenter.getZ(),
                        targetChunk,
                        mask);
            }
        }
        return carved;
    }

    static boolean carveFromStart(
            long worldSeed,
            ChunkPos ancientCityStart,
            int centerX,
            int centerZ,
            ChunkAccess targetChunk,
            CarvingMask mask) {
        ChunkPos targetPos = targetChunk.getPos();
        if (!canAffect(targetPos, centerX, centerZ)) {
            return false;
        }

        long caveSeed = caveSeed(worldSeed, ancientCityStart);
        double[][][] noiseGrid = sampleNoiseGrid(caveSeed, targetPos, centerX, centerZ);
        int minY = Math.max(targetChunk.getMinY(), Underworld.LARGE_CAVE_MIN_Y + 1);
        int maxY = Math.min(targetChunk.getMaxY(), Underworld.LARGE_CAVE_MAX_Y - 1);
        boolean carved = false;
        BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();

        for (int localX = 0; localX < 16; localX++) {
            int worldX = targetPos.getBlockX(localX);
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldZ = targetPos.getBlockZ(localZ);
                for (int worldY = minY; worldY <= maxY; worldY++) {
                    if (interpolatedNoise(noiseGrid, localX, worldY, localZ) <= 0.0) {
                        continue;
                    }
                    position.set(worldX, worldY, worldZ);
                    if (mask.get(localX, worldY, localZ)) {
                        continue;
                    }
                    BlockState state = targetChunk.getBlockState(position);
                    if (!canReplace(state, worldY)) {
                        continue;
                    }
                    mask.set(localX, worldY, localZ);
                    targetChunk.setBlockState(position, CAVE_AIR);
                    carved = true;
                }
            }
        }
        return carved;
    }

    static long caveSeed(long worldSeed, ChunkPos ancientCityStart) {
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureSeed(worldSeed, ancientCityStart.x(), ancientCityStart.z());
        return random.nextLong();
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

    static boolean canReplace(BlockState state, int y) {
        return state.is(Blocks.STONE)
                || state.is(Blocks.DEEPSLATE)
                || (y >= Underworld.LARGE_CAVE_INTERNAL_BEDROCK_MIN_Y
                        && y < Underworld.LARGE_CAVE_INTERNAL_BEDROCK_MAX_Y_EXCLUSIVE
                        && state.is(Blocks.BEDROCK));
    }
}
