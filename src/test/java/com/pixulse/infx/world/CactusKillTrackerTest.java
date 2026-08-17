package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class CactusKillTrackerTest {
    @Test
    void countsClampAndTopDecayRemovesTheSandBackedEntry() {
        CactusKillTracker tracker = new CactusKillTracker();
        BlockPos sand = new BlockPos(12, 48, -7);
        for (int index = 0; index < CactusKillTracker.MAX_KILLS + 4; index++) {
            tracker.increment(sand);
        }
        assertEquals(CactusKillTracker.MAX_KILLS, tracker.count(sand));

        for (int index = 0; index < CactusKillTracker.MAX_KILLS; index++) {
            tracker.decrement(sand);
        }
        assertEquals(0, tracker.count(sand));

        tracker.increment(sand);
        tracker.clear(sand);
        assertEquals(0, tracker.count(sand));
    }
}
