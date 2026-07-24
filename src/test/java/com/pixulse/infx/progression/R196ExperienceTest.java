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
        assertEquals(0, R196Experience.deathTotal(20));
        assertEquals(0, R196Experience.deathTotal(900));
        assertEquals(-1, R196Experience.levelForTotal(-1));
        assertEquals(-1, R196Experience.levelForTotal(-20));
        assertEquals(-2, R196Experience.levelForTotal(-21));
        assertEquals(-40, R196Experience.levelForTotal(-800));
        assertEquals(-40, R196Experience.levelForTotal(-1_000));
        assertEquals(300, R196Experience.droppedOnDeath(900));
        assertEquals(0, R196Experience.droppedOnDeath(-40));
        assertEquals(0.95F, R196Experience.progressForTotal(-1, -1), 0.0001F);
        assertEquals(0.0F, R196Experience.progressForTotal(-20, -1), 0.0001F);
        assertEquals(0.5F, R196Experience.progressForTotal(-10, -1), 0.0001F);
        assertEquals(20, R196Experience.pointsToNextLevel(-5));
    }

    @Test
    void negativeLevelsApplyR196DebtPenalties() {
        assertEquals(0.0F, R196Experience.harvestOrCraftLevelBonus(0), 0.0001F);
        assertEquals(0.5F, R196Experience.harvestOrCraftLevelBonus(25), 0.0001F);
        assertEquals(-0.8F, R196Experience.harvestOrCraftLevelBonus(-40), 0.0001F);
        assertEquals(1.0F, R196Experience.harvestOrCraftMultiplier(0), 0.0001F);
        assertEquals(0.2F, R196Experience.harvestOrCraftMultiplier(-40), 0.0001F);

        assertEquals(0.0F, R196Experience.meleeLevelBonus(0), 0.0001F);
        assertEquals(0.05F, R196Experience.meleeLevelBonus(10), 0.0001F);
        assertEquals(1.0F, R196Experience.meleeMultiplier(0), 0.0001F);
        assertEquals(1.05F, R196Experience.meleeMultiplier(10), 0.0001F);
        assertEquals(0.2F, R196Experience.meleeMultiplier(-40), 0.0001F);
        assertEquals(0.98F, R196Experience.meleeMultiplier(-1), 0.0001F);
    }
}
