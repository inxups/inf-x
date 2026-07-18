package com.pixulse.infx.furnace;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class FurnaceItemPolicyTest {
    @Test
    void r196LargeBlocksExcludeSmallPlantsAndFixtures() {
        assertTrue(FurnaceItemPolicy.isLargeBlock(Blocks.SAND));
        assertTrue(FurnaceItemPolicy.isLargeBlock(Blocks.OAK_LOG));
        assertFalse(FurnaceItemPolicy.isLargeBlock(Blocks.OAK_SAPLING));
        assertFalse(FurnaceItemPolicy.isLargeBlock(Blocks.TORCH));
        assertFalse(FurnaceItemPolicy.isLargeBlock(Blocks.STONE_BUTTON));
        assertFalse(FurnaceItemPolicy.isLargeBlock(Blocks.VINE));
    }
}
