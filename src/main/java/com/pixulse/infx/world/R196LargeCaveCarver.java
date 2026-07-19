package com.pixulse.infx.world;

import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

/** A bounded 64x48x64 cellular cavern, attempted only 1/200 chunks outside radius 1000. */
public final class R196LargeCaveCarver extends WorldCarver<CarverConfiguration> {
    public static final int MINIMUM_DISTANCE = 1_000;
    public static final int CHANCE_DENOMINATOR = 200;
    public static final int HORIZONTAL_SIZE = 64;
    public static final int MIN_Y = 8;
    public static final int MAX_Y = 55;

    public R196LargeCaveCarver() {
        super(CarverConfiguration.CODEC.codec());
    }

    @Override
    public int getRange() {
        return 4;
    }

    @Override
    public boolean isStartChunk(CarverConfiguration configuration, RandomSource random) {
        return random.nextInt(CHANCE_DENOMINATOR) == 0;
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
        if (!eligibleDistance(sourceChunkPos)) return false;
        int originX = sourceChunkPos.getMinBlockX();
        int originZ = sourceChunkPos.getMinBlockZ();
        int chunkMinX = chunk.getPos().getMinBlockX();
        int chunkMinZ = chunk.getPos().getMinBlockZ();
        if (chunkMinX + 15 < originX || chunkMinX >= originX + HORIZONTAL_SIZE
                || chunkMinZ + 15 < originZ || chunkMinZ >= originZ + HORIZONTAL_SIZE) {
            return false;
        }

        boolean carved = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int localX = 0; localX < 16; localX++) {
            int x = chunkMinX + localX;
            if (x < originX || x >= originX + HORIZONTAL_SIZE) continue;
            for (int localZ = 0; localZ < 16; localZ++) {
                int z = chunkMinZ + localZ;
                if (z < originZ || z >= originZ + HORIZONTAL_SIZE) continue;
                for (int y = MIN_Y; y <= MAX_Y; y++) {
                    if (!insideCellularCave(x, y, z, originX, originZ, sourceChunkPos.pack())) continue;
                    pos.set(x, y, z);
                    if (mask.get(localX, y, localZ)
                            || !canReplaceBlock(configuration, chunk.getBlockState(pos))) continue;
                    mask.set(localX, y, localZ);
                    chunk.setBlockState(pos, CAVE_AIR);
                    carved = true;
                }
            }
        }
        return carved;
    }

    public static boolean eligibleDistance(ChunkPos source) {
        long x = source.getMiddleBlockX();
        long z = source.getMiddleBlockZ();
        return x * x + z * z >= (long) MINIMUM_DISTANCE * MINIMUM_DISTANCE;
    }

    static boolean insideCellularCave(int x, int y, int z, int originX, int originZ, long seed) {
        double dx = (x + 0.5D - (originX + 32.0D)) / 32.0D;
        double dy = (y + 0.5D - 32.0D) / 24.0D;
        double dz = (z + 0.5D - (originZ + 32.0D)) / 32.0D;
        double envelope = dx * dx + dy * dy + dz * dz;
        if (envelope >= 1.0D) return false;
        double chamberA = square(dx + 0.34D) + square(dy * 1.15D) + square(dz - 0.18D);
        double chamberB = square(dx - 0.28D) + square(dy * 1.25D + 0.12D) + square(dz + 0.25D);
        double chamberC = square(dx * 1.10D) + square(dy - 0.25D) + square(dz * 1.10D);
        double noise = hashNoise(x >> 2, y >> 2, z >> 2, seed);
        return chamberA < 0.34D || chamberB < 0.31D || chamberC < 0.25D
                || envelope < 0.82D && noise > -0.18D;
    }

    private static double square(double value) {
        return value * value;
    }

    private static double hashNoise(int x, int y, int z, long seed) {
        long value = seed ^ x * 0x632BE59BD9B4E019L ^ y * 0x9E3779B97F4A7C15L ^ z * 0x94D049BB133111EBL;
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return ((value >>> 11) * 0x1.0p-53) * 2.0D - 1.0D;
    }
}
