package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.junit.jupiter.api.Test;

class InfXUnderworldMyceliumFeatureTest {
    private static final BlockPos ORIGIN = new BlockPos(0, Underworld.MIN_Y, 0);
    private static final InfXUnderworldMyceliumFeature FEATURE =
            new InfXUnderworldMyceliumFeature(NoneFeatureConfiguration.CODEC);

    @Test
    void usesAOneInSixteenPostRollAndMitePairHash() {
        assertAll(
                () -> assertTrue(InfXUnderworldMyceliumFeature.isPostRoll(0.0F)),
                () -> assertTrue(InfXUnderworldMyceliumFeature.isPostRoll(0.0624F)),
                () -> assertFalse(InfXUnderworldMyceliumFeature.isPostRoll(0.0625F)),
                () -> assertEquals(16_434, InfXUnderworldMyceliumFeature.intPairHash(3, 4)));
    }

    @Test
    void scansTheFiveByFivePostFieldWithStableSeededProfiles() {
        long seed = seedWithAffectedColumn();
        List<InfXUnderworldMyceliumFeature.MyceliumPost> posts =
                InfXUnderworldMyceliumFeature.nearbyPosts(seed, 0, 0);

        assertAll(
                () -> assertEquals(posts, InfXUnderworldMyceliumFeature.nearbyPosts(seed, 0, 0)),
                () -> assertNotEquals(
                        InfXUnderworldMyceliumFeature.postSeed(seed, 0, 0),
                        InfXUnderworldMyceliumFeature.postSeed(seed + 1, 0, 0)),
                () -> assertTrue(posts.stream().allMatch(post ->
                        Math.abs(post.sourceChunkX()) <= Underworld.MYCELIUM_POST_SEARCH_CHUNK_RANGE
                                && Math.abs(post.sourceChunkZ()) <= Underworld.MYCELIUM_POST_SEARCH_CHUNK_RANGE)),
                () -> assertTrue(posts.stream().allMatch(post ->
                        post.y() >= 144 && post.y() <= 215 && post.height() >= 1 && post.height() <= 5)));
    }

    @Test
    void includesMitesRadiusToleranceAtThePostBoundary() {
        InfXUnderworldMyceliumFeature.MyceliumPost post =
                new InfXUnderworldMyceliumFeature.MyceliumPost(0L, 0, 0, 0, 0, 144, 1);

        assertAll(
                () -> assertTrue(post.affects(24, 2)),
                () -> assertFalse(post.affects(24, 3)),
                () -> assertFalse(post.affects(25, 0)));
    }

    @Test
    void replacesOnlyTerrainStoneBelowAnAirOpening() {
        Candidate candidate = firstAffectedColumn();
        UnderworldFeatureTestLevel level = new UnderworldFeatureTestLevel(candidate.seed());
        BlockPos ground = new BlockPos(candidate.x(), candidate.post().y(), candidate.z());
        level.setRaw(ground.above(), Blocks.CAVE_AIR.defaultBlockState());

        assertTrue(place(level));
        assertAll(
                () -> assertEquals(Blocks.MYCELIUM, level.blockAt(ground).getBlock()),
                () -> assertEquals(Blocks.STONE, level.blockAt(ground.below()).getBlock()));
    }

    @Test
    void doesNotReplaceDeepSlateAtAnOtherwiseValidPostColumn() {
        Candidate candidate = firstAffectedColumn();
        UnderworldFeatureTestLevel level = new UnderworldFeatureTestLevel(candidate.seed());
        BlockPos ground = new BlockPos(candidate.x(), candidate.post().y(), candidate.z());
        level.setRaw(ground, Blocks.DEEPSLATE.defaultBlockState());
        level.setRaw(ground.above(), Blocks.CAVE_AIR.defaultBlockState());

        assertFalse(place(level));
        assertEquals(Blocks.DEEPSLATE, level.blockAt(ground).getBlock());
    }

    private static boolean place(UnderworldFeatureTestLevel level) {
        return FEATURE.place(new FeaturePlaceContext<>(
                Optional.empty(),
                level.world(),
                null,
                RandomSource.create(0L),
                ORIGIN,
                NoneFeatureConfiguration.INSTANCE));
    }

    private static long seedWithAffectedColumn() {
        return firstAffectedColumn().seed();
    }

    private static Candidate firstAffectedColumn() {
        for (long seed = 0L; seed < 4_096L; seed++) {
            if (InfXUnderworldLushRegion.isLushRegion(seed, 0, 0)) {
                continue;
            }
            for (InfXUnderworldMyceliumFeature.MyceliumPost post :
                    InfXUnderworldMyceliumFeature.nearbyPosts(seed, 0, 0)) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        if (post.affects(x, z)) {
                            return new Candidate(seed, post, x, z);
                        }
                    }
                }
            }
        }
        throw new AssertionError("Expected a deterministic mycelium post near the origin");
    }

    private record Candidate(long seed, InfXUnderworldMyceliumFeature.MyceliumPost post, int x, int z) {}
}
