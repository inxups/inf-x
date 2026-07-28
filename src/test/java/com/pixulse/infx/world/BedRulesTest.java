package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BedRulesTest {
    @Test
    void adjustedClockUsesR196sSixHourOffset() {
        assertEquals(6_000L, BedRules.adjustedTime(0L));
        assertEquals(21_000L, BedRules.adjustedTime(15_000L));
        assertEquals(0L, BedRules.adjustedTime(18_000L));
        assertEquals(5_000L, BedRules.adjustedTime(23_000L));
    }

    @Test
    void fastForwardWindowEndsAtFourInTheMorning() {
        assertTrue(BedRules.isFastForwardWindow(15_000L));
        assertTrue(BedRules.isFastForwardWindow(18_000L));
        assertTrue(BedRules.isFastForwardWindow(21_999L));
        assertFalse(BedRules.isFastForwardWindow(22_000L));
        assertFalse(BedRules.isFastForwardWindow(23_000L));
    }

    @Test
    void sunriseDistanceWrapsAtTheR196FiveInTheMorningBoundary() {
        assertEquals(8_000, BedRules.ticksUntilSunrise(15_000L));
        assertEquals(5_000, BedRules.ticksUntilSunrise(18_000L));
        assertEquals(1_001, BedRules.ticksUntilSunrise(21_999L));
        assertEquals(24_000, BedRules.ticksUntilSunrise(23_000L));
    }
}
