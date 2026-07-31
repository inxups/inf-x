package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class InfXUnderworldLushRegionTest {
    @Test
    void exposesTheConfiguredHeightAndRegionShape() {
        assertEquals(144, Underworld.LUSH_CAVES_MIN_Y);
        assertEquals(247, Underworld.LUSH_CAVES_MAX_Y_INCLUSIVE);
        assertEquals(156, Underworld.LUSH_CAVES_FLOOR_SCAN_MIN_Y);
        assertEquals(4, Underworld.LUSH_REGION_CHUNK_SIZE);
    }

    @Test
    void allChunksInOneFourByFourRegionShareTheSameResult() {
        long seed = 0x1234_5678_9ABC_DEF0L;
        boolean expected = InfXUnderworldLushRegion.isLushRegion(seed, -4, 7);
        for (int chunkX = -4; chunkX < 0; chunkX++) {
            for (int chunkZ = 4; chunkZ < 8; chunkZ++) {
                assertEquals(expected, InfXUnderworldLushRegion.isLushRegion(seed, chunkX, chunkZ));
            }
        }
    }

    @Test
    void negativeChunkBoundariesUseFloorDivision() {
        long seed = 987654321L;
        assertEquals(
                InfXUnderworldLushRegion.isLushRegion(seed, -1, -1),
                InfXUnderworldLushRegion.isLushRegion(seed, -4, -4));
        assertEquals(
                InfXUnderworldLushRegion.isLushRegion(seed, -5, -5),
                InfXUnderworldLushRegion.isLushRegion(seed, -8, -8));
    }

    @Test
    void partitionChangesAcrossRegionsAndSeeds() {
        long seed = 13579L;
        boolean reference = InfXUnderworldLushRegion.isLushRegion(seed, 0, 0);
        boolean differentRegion = false;
        for (int regionX = -16; regionX <= 16 && !differentRegion; regionX++) {
            for (int regionZ = -16; regionZ <= 16; regionZ++) {
                if (regionX == 0 && regionZ == 0) {
                    continue;
                }
                if (InfXUnderworldLushRegion.isLushRegion(seed, regionX * 4, regionZ * 4) != reference) {
                    differentRegion = true;
                    break;
                }
            }
        }
        boolean differentSeed = false;
        for (long otherSeed = 1; otherSeed <= 64; otherSeed++) {
            if (InfXUnderworldLushRegion.isLushRegion(otherSeed, 0, 0) != reference) {
                differentSeed = true;
                break;
            }
        }
        assertTrue(differentRegion);
        assertTrue(differentSeed);
    }

    @Test
    void oldFungusUsesTheOppositeRegionPolicy() {
        long seed = 24680L;
        for (int chunkX = -8; chunkX <= 8; chunkX++) {
            for (int chunkZ = -8; chunkZ <= 8; chunkZ++) {
                assertEquals(
                        !InfXUnderworldLushRegion.isLushRegion(seed, chunkX, chunkZ),
                        InfXUnderworldBrownMushroomFeature.shouldGenerateInRegion(seed, chunkX, chunkZ));
            }
        }
    }

    @Test
    void myceliumPostsOnlyUseNonLushSourceRegions() {
        InfXUnderworldMyceliumFeature.MyceliumPost lushPost = null;
        InfXUnderworldMyceliumFeature.MyceliumPost fungusPost = null;
        long lushSeed = 0L;
        long fungusSeed = 0L;
        for (long seed = 0; seed < 128 && (lushPost == null || fungusPost == null); seed++) {
            for (int chunkX = -16; chunkX <= 16 && (lushPost == null || fungusPost == null); chunkX++) {
                for (int chunkZ = -16; chunkZ <= 16; chunkZ++) {
                    Optional<InfXUnderworldMyceliumFeature.MyceliumPost> post =
                            InfXUnderworldMyceliumFeature.postForChunk(seed, chunkX, chunkZ);
                    if (post.isEmpty()) {
                        continue;
                    }
                    if (InfXUnderworldLushRegion.isLushRegion(seed, chunkX, chunkZ)) {
                        lushPost = post.get();
                        lushSeed = seed;
                    } else {
                        fungusPost = post.get();
                        fungusSeed = seed;
                    }
                }
            }
        }
        assertNotNull(lushPost);
        assertNotNull(fungusPost);
        InfXUnderworldMyceliumFeature.MyceliumPost finalLushPost = lushPost;
        InfXUnderworldMyceliumFeature.MyceliumPost finalFungusPost = fungusPost;
        assertTrue(InfXUnderworldMyceliumFeature.nearbyPosts(
                lushSeed, finalLushPost.sourceChunkX(), finalLushPost.sourceChunkZ()).stream()
                .noneMatch(post -> post.sourceChunkX() == finalLushPost.sourceChunkX()
                        && post.sourceChunkZ() == finalLushPost.sourceChunkZ()));
        assertTrue(InfXUnderworldMyceliumFeature.nearbyPosts(
                fungusSeed, finalFungusPost.sourceChunkX(), finalFungusPost.sourceChunkZ()).stream()
                .anyMatch(post -> post.sourceChunkX() == finalFungusPost.sourceChunkX()
                        && post.sourceChunkZ() == finalFungusPost.sourceChunkZ()));
    }
}
