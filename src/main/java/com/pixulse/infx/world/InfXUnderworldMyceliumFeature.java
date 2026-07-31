package com.pixulse.infx.world;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Generates MITE-style mycelium post fields without shared mutable world-generation state. */
public final class InfXUnderworldMyceliumFeature extends Feature<NoneFeatureConfiguration> {
    private static final int CHUNK_SIZE = 16;
    private static final int POST_MIN_LOCAL_Y = 24;
    private static final int POST_SHORT_Y_RANGE = 16;
    private static final int POST_TALL_Y_RANGE = 72;
    private static final int POST_MAX_HEIGHT = 5;
    private static final int POST_RADIUS_SQUARED_WITH_TOLERANCE =
            Underworld.MYCELIUM_POST_RADIUS * Underworld.MYCELIUM_POST_RADIUS + 4;
    private static final long POST_SEED_SALT = 0x5EED_4D59_CE11_0001L;
    private static final long POST_CHUNK_X_SALT = 341_873_128_712L;
    private static final long POST_CHUNK_Z_SALT = 132_897_987_541L;
    private static final BlockState MYCELIUM = Blocks.MYCELIUM.defaultBlockState();
    private static final BlockState BROWN_MUSHROOM = Blocks.BROWN_MUSHROOM.defaultBlockState();

    public InfXUnderworldMyceliumFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos origin = context.origin();
        int chunkX = SectionPos.blockToSectionCoord(origin.getX());
        int chunkZ = SectionPos.blockToSectionCoord(origin.getZ());
        int minX = SectionPos.sectionToBlockCoord(chunkX);
        int minZ = SectionPos.sectionToBlockCoord(chunkZ);
        List<MyceliumPost> posts = nearbyPosts(context.level().getSeed(), chunkX, chunkZ);
        boolean placedAny = false;

        for (int x = minX; x < minX + CHUNK_SIZE; x++) {
            for (int z = minZ; z < minZ + CHUNK_SIZE; z++) {
                for (MyceliumPost post : posts) {
                    if (post.affects(x, z) && placeColumn(context.level(), x, z, post)) {
                        placedAny = true;
                        break;
                    }
                }
            }
        }

        return placedAny;
    }

    static List<MyceliumPost> nearbyPosts(long worldSeed, int chunkX, int chunkZ) {
        List<MyceliumPost> posts = new ArrayList<>();
        for (int offsetX = -Underworld.MYCELIUM_POST_SEARCH_CHUNK_RANGE;
                offsetX <= Underworld.MYCELIUM_POST_SEARCH_CHUNK_RANGE;
                offsetX++) {
            for (int offsetZ = -Underworld.MYCELIUM_POST_SEARCH_CHUNK_RANGE;
                    offsetZ <= Underworld.MYCELIUM_POST_SEARCH_CHUNK_RANGE;
                    offsetZ++) {
                postForChunk(worldSeed, chunkX + offsetX, chunkZ + offsetZ).ifPresent(posts::add);
            }
        }
        return posts;
    }

    static Optional<MyceliumPost> postForChunk(long worldSeed, int chunkX, int chunkZ) {
        long seed = postSeed(worldSeed, chunkX, chunkZ);
        RandomSource placementRandom = new LegacyRandomSource(seed);
        placementRandom.nextInt();
        if (!isPostRoll(placementRandom.nextFloat())) {
            return Optional.empty();
        }

        int minX = SectionPos.sectionToBlockCoord(chunkX);
        int minZ = SectionPos.sectionToBlockCoord(chunkZ);
        int x = minX + placementRandom.nextInt(CHUNK_SIZE);
        int z = minZ + placementRandom.nextInt(CHUNK_SIZE);

        RandomSource profileRandom = new LegacyRandomSource(seed);
        profileRandom.nextInt();
        int localY = profileRandom.nextInt(profileRandom.nextBoolean() ? POST_SHORT_Y_RANGE : POST_TALL_Y_RANGE)
                + POST_MIN_LOCAL_Y;
        int height = profileRandom.nextInt(POST_MAX_HEIGHT) + 1;
        return Optional.of(new MyceliumPost(
                seed,
                chunkX,
                chunkZ,
                x,
                z,
                Underworld.TERRAIN_MIN_Y + localY,
                height));
    }

    static long postSeed(long worldSeed, int chunkX, int chunkZ) {
        long value = worldSeed ^ POST_SEED_SALT;
        value ^= (long) chunkX * POST_CHUNK_X_SALT;
        value ^= (long) chunkZ * POST_CHUNK_Z_SALT;
        return mix64(value);
    }

    static boolean isPostRoll(float roll) {
        return roll < 1.0F / Underworld.MYCELIUM_POST_CHANCE;
    }

    static boolean shouldPlacePostMushroom(long postSeed, int x, int z) {
        RandomSource random = new LegacyRandomSource(postSeed + intPairHash(x, z));
        random.nextInt();
        return random.nextInt(Underworld.MYCELIUM_POST_CHANCE) == 0;
    }

    static int intPairHash(int first, int second) {
        int hash = 17;
        hash = hash * 31 + first;
        return hash * 31 + second;
    }

    private static boolean placeColumn(WorldGenLevel level, int x, int z, MyceliumPost post) {
        int y = post.y();
        for (int rise = 0; rise < post.height(); rise++, y++) {
            BlockPos groundPos = new BlockPos(x, y, z);
            if (!level.isEmptyBlock(groundPos.above())) {
                continue;
            }
            if (!level.getBlockState(groundPos).is(Blocks.STONE)
                    || !level.getBlockState(groundPos.below()).is(Blocks.STONE)) {
                return false;
            }
            if (!level.setBlock(groundPos, MYCELIUM, 2)) {
                return false;
            }

            BlockPos mushroomPos = groundPos.above();
            if (shouldPlacePostMushroom(post.seed(), x, z)
                    && level.isEmptyBlock(mushroomPos)
                    && BROWN_MUSHROOM.canSurvive(level, mushroomPos)) {
                level.setBlock(mushroomPos, BROWN_MUSHROOM, 2);
            }
            return true;
        }
        return false;
    }

    private static long mix64(long value) {
        value = (value ^ value >>> 30) * 0xBF58_476D_1CE4_E5B9L;
        value = (value ^ value >>> 27) * 0x94D0_49BB_1331_11EBL;
        return value ^ value >>> 31;
    }

    record MyceliumPost(long seed, int sourceChunkX, int sourceChunkZ, int x, int z, int y, int height) {
        boolean affects(int blockX, int blockZ) {
            long deltaX = (long) this.x - blockX;
            long deltaZ = (long) this.z - blockZ;
            return deltaX * deltaX + deltaZ * deltaZ <= POST_RADIUS_SQUARED_WITH_TOLERANCE;
        }
    }
}
