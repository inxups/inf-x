package com.pixulse.infx.block.furnace;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.data.furnace.FurnaceHeatPolicy;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class FurnaceHeatPolicyTest {
    @Test
    void onlyTheCobblestoneFurnaceIsManagedAtHeatTwo() {
        assertEquals(2, FurnaceHeatPolicy.maximumHeat(Blocks.FURNACE.defaultBlockState()));
        assertEquals(0, FurnaceHeatPolicy.maximumHeat(Blocks.SMOKER.defaultBlockState()));
        assertEquals(0, FurnaceHeatPolicy.maximumHeat(Blocks.BLAST_FURNACE.defaultBlockState()));
    }
}
