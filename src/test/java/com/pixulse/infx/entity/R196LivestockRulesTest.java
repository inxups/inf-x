package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.world.R196MoonPhase;
import org.junit.jupiter.api.Test;

class R196LivestockRulesTest {
    @Test
    void everyNeedIsRequiredForHealthyProduction() {
        assertTrue(R196Livestock.healthy(true, true, true, true, true, true, false, false));
        assertFalse(R196Livestock.healthy(false, true, true, true, true, true, false, false));
        assertFalse(R196Livestock.healthy(true, false, true, true, true, true, false, false));
        assertFalse(R196Livestock.healthy(true, true, false, true, true, true, false, false));
        assertFalse(R196Livestock.healthy(true, true, true, true, true, true, true, false));
        assertFalse(R196Livestock.healthy(true, true, true, true, true, true, false, true));
    }

    @Test
    void lunarCalendarKeepsBloodAndBlueMoonReplacementRules() {
        assertEquals(R196MoonPhase.FULL, R196MoonPhase.atDay(1));
        assertEquals(R196MoonPhase.NEW, R196MoonPhase.atDay(5));
        assertEquals(R196MoonPhase.BLOOD, R196MoonPhase.atDay(32));
        assertEquals(R196MoonPhase.BLUE, R196MoonPhase.atDay(128));
        assertEquals(R196MoonPhase.YELLOW, R196MoonPhase.atDay(24));
        assertEquals(R196MoonPhase.PHANTOM, R196MoonPhase.atDay(120));
    }

    @Test
    void horseFailureCooldownIsExactlyTwoHundredSeconds() {
        assertEquals(4_000L, R196AnimalEvents.horseRetryTicks());
    }

    @Test
    void unhealthyAdultChickenCannotLayAnEggOnTheNextTick() {
        assertTrue(R196AnimalEvents.shouldDelayEgg(true, false, false, 1));
        assertFalse(R196AnimalEvents.shouldDelayEgg(true, false, true, 1));
        assertFalse(R196AnimalEvents.shouldDelayEgg(false, false, false, 1));
        assertFalse(R196AnimalEvents.shouldDelayEgg(true, true, false, 1));
        assertFalse(R196AnimalEvents.shouldDelayEgg(true, false, false, 2));
    }
}
