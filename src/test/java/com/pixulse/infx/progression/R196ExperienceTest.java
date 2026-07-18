package com.pixulse.infx.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class R196ExperienceTest {
    @Test
    void curveMatchesR196MilestonesAndCapsDisplay() {
        assertEquals(20, R196Experience.cumulativeForLevel(1));
        assertEquals(203_000, R196Experience.cumulativeForLevel(200));
        assertEquals(200, R196Experience.levelForTotal(203_000));
        assertEquals(200, R196Experience.levelForTotal(1_000_000));
        assertEquals(20, R196Experience.pointsToNextLevel(0));
        assertEquals(2_010, R196Experience.pointsToNextLevel(199));
    }

    @Test
    void lowExperienceDeathsAccumulateButClampDebt() {
        assertEquals(-20, R196Experience.deathTotal(0));
        assertEquals(-40, R196Experience.deathTotal(-20));
        assertEquals(-800, R196Experience.deathTotal(-800));
        assertEquals(-1, R196Experience.levelForTotal(-20));
        assertEquals(-40, R196Experience.levelForTotal(-800));
        assertEquals(300, R196Experience.droppedOnDeath(900));
    }
}
