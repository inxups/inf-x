package com.pixulse.infx.harvest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.data.harvest.MiningProgressAccumulator;
import org.junit.jupiter.api.Test;

class MiningProgressAccumulatorTest {
    @Test
    void preservesProgressWhenHungerSlowsMiningMidSession() {
        MiningProgressAccumulator progress = new MiningProgressAccumulator();
        progress.start(0.1F);

        for (int tick = 0; tick < 7; tick++) {
            progress.advance(0.1F);
        }
        for (int tick = 0; tick < 11; tick++) {
            progress.advance(0.02F);
        }

        assertEquals(1.02F, progress.progress(), 1.0E-6F);
        assertTrue(progress.progress() >= 1.0F);
        float oldRecalculatedProgress = 0.02F * 19;
        assertEquals(0.38F, oldRecalculatedProgress, 1.0E-6F);
        assertTrue(
                oldRecalculatedProgress < 0.7F && oldRecalculatedProgress < progress.progress(),
                "the vanilla current-speed-times-elapsed formula reprices earlier progress");
    }

    @Test
    void resetStartsAnIndependentMiningSession() {
        MiningProgressAccumulator progress = new MiningProgressAccumulator();
        progress.start(0.25F);
        progress.advance(0.25F);

        progress.reset();

        assertFalse(progress.isActive());
        assertEquals(0.0F, progress.progress());
        progress.start(0.05F);
        assertTrue(progress.isActive());
        assertEquals(0.05F, progress.progress());
    }
}
