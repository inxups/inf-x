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

/** Carves one large, deterministic cavern from the center of deep-dark 16x16 chunk macro-regions. */
public final class InfXUnderworldLargeCaveCarver extends WorldCarver<CarverConfiguration> {
    private static final int SIDE_OFFSET = 80;
    private static final int SIDE_HORIZONTAL_RADIUS = 48;
    private static final int SIDE_DEPTH_RADIUS = 32;
    private static final int SIDE_VERTICAL_RADIUS = 44;
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
        int minY = Math.max(context.getMinGenY(), Underworld.LARGE_CAVE_MIN_Y);
        int maxY = Math.min(context.getMinGenY() + context.getGenDepth() - 1, Underworld.LARGE_CAVE_MAX_Y);
        ChunkPos targetChunk = chunk.getPos();
        boolean carved = false;
        BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();

        for (int localX = 0; localX < 16; localX++) {
            int worldX = targetChunk.getBlockX(localX);
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldZ = targetChunk.getBlockZ(localZ);
                if (!isDeepDark(biomeGetter, worldX, worldZ)) {
                    continue;
                }
                for (int worldY = minY; worldY <= maxY; worldY++) {
                    if (!isInsideCave(worldX - centerX, worldY - Underworld.LARGE_CAVE_CENTER_Y, worldZ - centerZ)) {
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

    static boolean isMacroCenter(ChunkPos chunkPos) {
        return chunkPos.x() == macroCenterChunk(chunkPos.x())
                && chunkPos.z() == macroCenterChunk(chunkPos.z());
    }

    static int macroCenterChunk(int chunkCoordinate) {
        return Math.floorDiv(chunkCoordinate, Underworld.LARGE_CAVE_MACRO_CHUNK_SIZE)
                * Underworld.LARGE_CAVE_MACRO_CHUNK_SIZE
                + Underworld.LARGE_CAVE_MACRO_CENTER_OFFSET;
    }

    static boolean isInsideCave(int relativeX, int relativeY, int relativeZ) {
        if (relativeY < Underworld.LARGE_CAVE_MIN_Y - Underworld.LARGE_CAVE_CENTER_Y
                || relativeY > Underworld.LARGE_CAVE_MAX_Y - Underworld.LARGE_CAVE_CENTER_Y) {
            return false;
        }
        if (insideEllipsoid(relativeX, relativeY, relativeZ,
                Underworld.LARGE_CAVE_MAIN_RADIUS,
                Underworld.LARGE_CAVE_MAIN_VERTICAL_RADIUS,
                Underworld.LARGE_CAVE_MAIN_RADIUS)) {
            return true;
        }
        return insideEllipsoid(relativeX - SIDE_OFFSET, relativeY, relativeZ,
                        SIDE_HORIZONTAL_RADIUS, SIDE_VERTICAL_RADIUS, SIDE_DEPTH_RADIUS)
                || insideEllipsoid(relativeX + SIDE_OFFSET, relativeY, relativeZ,
                        SIDE_HORIZONTAL_RADIUS, SIDE_VERTICAL_RADIUS, SIDE_DEPTH_RADIUS)
                || insideEllipsoid(relativeZ - SIDE_OFFSET, relativeY, relativeX,
                        SIDE_HORIZONTAL_RADIUS, SIDE_VERTICAL_RADIUS, SIDE_DEPTH_RADIUS)
                || insideEllipsoid(relativeZ + SIDE_OFFSET, relativeY, relativeX,
                        SIDE_HORIZONTAL_RADIUS, SIDE_VERTICAL_RADIUS, SIDE_DEPTH_RADIUS);
    }

    static boolean canReplace(BlockState state, int y) {
        return state.is(Blocks.STONE)
                || state.is(Blocks.DEEPSLATE)
                || (y >= Underworld.LARGE_CAVE_INTERNAL_BEDROCK_MIN_Y
                        && y < Underworld.LARGE_CAVE_INTERNAL_BEDROCK_MAX_Y_EXCLUSIVE
                        && state.is(Blocks.BEDROCK));
    }

    private static boolean insideEllipsoid(
            int relativeX, int relativeY, int relativeZ, double horizontalRadius, double verticalRadius, double depthRadius) {
        double x = relativeX / horizontalRadius;
        double y = relativeY / verticalRadius;
        double z = relativeZ / depthRadius;
        return x * x + y * y + z * z < 1.0;
    }

    static boolean isDeepDark(Function<BlockPos, Holder<Biome>> biomeGetter, int x, int z) {
        return biomeGetter.apply(new BlockPos(x, 0, z)).is(Underworld.DEEP_DARK_BIOME);
    }
}
