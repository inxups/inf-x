package com.pixulse.infx.event.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class BlockDropEventsTest {
    @Test
    void seedGrassCoversAllSeedDroppingPlants() {
        assertTrue(BlockDropEvents.isSeedGrass(Blocks.SHORT_GRASS.defaultBlockState()));
        assertTrue(BlockDropEvents.isSeedGrass(Blocks.TALL_GRASS.defaultBlockState()));
        assertTrue(BlockDropEvents.isSeedGrass(Blocks.FERN.defaultBlockState()));
        assertTrue(BlockDropEvents.isSeedGrass(Blocks.LARGE_FERN.defaultBlockState()));
    }

    @Test
    void otherBlocksAreNotSeedGrass() {
        assertFalse(BlockDropEvents.isSeedGrass(Blocks.STONE.defaultBlockState()));
        assertFalse(BlockDropEvents.isSeedGrass(Blocks.GRASS_BLOCK.defaultBlockState()));
        assertFalse(BlockDropEvents.isSeedGrass(Blocks.AIR.defaultBlockState()));
        assertFalse(BlockDropEvents.isSeedGrass(Blocks.POPPY.defaultBlockState()));
    }
}
