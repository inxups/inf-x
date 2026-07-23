package com.pixulse.infx.world;

import com.pixulse.infx.registry.ModBlocks;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;

/** Applies the Underworld's MITE-seeded mantle floor and bedrock ceiling. */
public final class MiteUnderworldStrata {
    static final int CHUNK_SIDE_LENGTH = 16;
    private static final int COLUMN_COUNT = CHUNK_SIDE_LENGTH * CHUNK_SIDE_LENGTH;

    private MiteUnderworldStrata() {}

    public static void apply(long worldSeed, ChunkAccess chunk) {
        StrataPlan plan = plan(worldSeed, chunk.getPos());
        int minY = chunk.getMinY();
        int height = chunk.getHeight();
        int topY = minY + height - 1;
        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        for (int localX = 0; localX < CHUNK_SIDE_LENGTH; localX++) {
            int blockX = chunkPos.getBlockX(localX);
            for (int localZ = 0; localZ < CHUNK_SIDE_LENGTH; localZ++) {
                int blockZ = chunkPos.getBlockZ(localZ);
                int thickness = Math.min(plan.boundaryThicknessAt(localX, localZ), height);

                for (int offset = 0; offset < thickness; offset++) {
                    chunk.setBlockState(
                            blockPos.set(blockX, minY + offset, blockZ),
                            ModBlocks.MANTLE.get().defaultBlockState());
                }
                for (int offset = 0; offset < thickness; offset++) {
                    chunk.setBlockState(
                            blockPos.set(blockX, topY - offset, blockZ),
                            Blocks.BEDROCK.defaultBlockState());
                }
            }
        }
    }

    static StrataPlan plan(long worldSeed, ChunkPos chunkPos) {
        long hashedWorldSeed = miteHashedWorldSeed(worldSeed);
        long columnSeed = hashedWorldSeed * (long) miteChunkHash(chunkPos.x(), chunkPos.z());
        Random columnRandom = new Random(columnSeed);
        StrataPlan plan = new StrataPlan();

        for (int localX = 0; localX < CHUNK_SIDE_LENGTH; localX++) {
            for (int localZ = 0; localZ < CHUNK_SIDE_LENGTH; localZ++) {
                plan.setBoundaryThickness(localX, localZ, columnRandom.nextInt(3) + 1);
            }
        }
        return plan;
    }

    private static long miteHashedWorldSeed(long worldSeed) {
        Random random = new Random(worldSeed);
        random.nextInt();
        return random.nextLong();
    }

    private static int miteChunkHash(int chunkX, int chunkZ) {
        int hash = 17;
        hash = hash * 31 + chunkX;
        return hash * 31 + chunkZ;
    }

    private static int columnIndex(int localX, int localZ) {
        return localZ + localX * CHUNK_SIDE_LENGTH;
    }

    static final class StrataPlan {
        private final byte[] boundaryThicknesses = new byte[COLUMN_COUNT];

        int boundaryThicknessAt(int localX, int localZ) {
            return this.boundaryThicknesses[columnIndex(localX, localZ)];
        }

        boolean hasMantleAt(int localX, int localZ, int relativeY) {
            return relativeY >= 0 && relativeY < this.boundaryThicknessAt(localX, localZ);
        }

        private void setBoundaryThickness(int localX, int localZ, int thickness) {
            this.boundaryThicknesses[columnIndex(localX, localZ)] = (byte) thickness;
        }
    }
}
