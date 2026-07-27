package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class R196BedRulesTest {
    @Test
    void adjustedClockUsesR196sSixHourOffset() {
        assertEquals(6_000L, R196BedRules.adjustedTime(0L));
        assertEquals(21_000L, R196BedRules.adjustedTime(15_000L));
        assertEquals(0L, R196BedRules.adjustedTime(18_000L));
        assertEquals(5_000L, R196BedRules.adjustedTime(23_000L));
    }

    @Test
    void fastForwardWindowEndsAtFourInTheMorning() {
        assertTrue(R196BedRules.isFastForwardWindow(15_000L));
        assertTrue(R196BedRules.isFastForwardWindow(18_000L));
        assertTrue(R196BedRules.isFastForwardWindow(21_999L));
        assertFalse(R196BedRules.isFastForwardWindow(22_000L));
        assertFalse(R196BedRules.isFastForwardWindow(23_000L));
    }

    @Test
    void sunriseDistanceWrapsAtTheR196FiveInTheMorningBoundary() {
        assertEquals(8_000, R196BedRules.ticksUntilSunrise(15_000L));
        assertEquals(5_000, R196BedRules.ticksUntilSunrise(18_000L));
        assertEquals(1_001, R196BedRules.ticksUntilSunrise(21_999L));
        assertEquals(24_000, R196BedRules.ticksUntilSunrise(23_000L));
    }
}
