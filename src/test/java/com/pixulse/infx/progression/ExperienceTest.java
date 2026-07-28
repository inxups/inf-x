package com.pixulse.infx.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ExperienceTest {
    @Test
    void curveMatchesR196MilestonesAndCapsDisplay() {
        assertEquals(20, Experience.cumulativeForLevel(1));
        assertEquals(203_000, Experience.cumulativeForLevel(200));
        assertEquals(200, Experience.levelForTotal(203_000));
        assertEquals(200, Experience.levelForTotal(1_000_000));
        assertEquals(20, Experience.pointsToNextLevel(0));
        assertEquals(2_010, Experience.pointsToNextLevel(199));
    }

    @Test
    void lowExperienceDeathsAccumulateButClampDebt() {
        assertEquals(-20, Experience.deathTotal(0));
        assertEquals(-40, Experience.deathTotal(-20));
        assertEquals(-800, Experience.deathTotal(-800));
        assertEquals(0, Experience.deathTotal(20));
        assertEquals(0, Experience.deathTotal(900));
        assertEquals(-1, Experience.levelForTotal(-1));
        assertEquals(-1, Experience.levelForTotal(-20));
        assertEquals(-2, Experience.levelForTotal(-21));
        assertEquals(-40, Experience.levelForTotal(-800));
        assertEquals(-40, Experience.levelForTotal(-1_000));
        assertEquals(300, Experience.droppedOnDeath(900));
        assertEquals(0, Experience.droppedOnDeath(-40));
        assertEquals(0.95F, Experience.progressForTotal(-1, -1), 0.0001F);
        assertEquals(0.0F, Experience.progressForTotal(-20, -1), 0.0001F);
        assertEquals(0.5F, Experience.progressForTotal(-10, -1), 0.0001F);
        assertEquals(20, Experience.pointsToNextLevel(-5));
    }

    @Test
    void negativeLevelsApplyR196DebtPenalties() {
        assertEquals(0.0F, Experience.harvestOrCraftLevelBonus(0), 0.0001F);
        assertEquals(0.5F, Experience.harvestOrCraftLevelBonus(25), 0.0001F);
        assertEquals(-0.8F, Experience.harvestOrCraftLevelBonus(-40), 0.0001F);
        assertEquals(1.0F, Experience.harvestOrCraftMultiplier(0), 0.0001F);
        assertEquals(0.2F, Experience.harvestOrCraftMultiplier(-40), 0.0001F);

        assertEquals(0.0F, Experience.meleeLevelBonus(0), 0.0001F);
        assertEquals(0.05F, Experience.meleeLevelBonus(10), 0.0001F);
        assertEquals(1.0F, Experience.meleeMultiplier(0), 0.0001F);
        assertEquals(1.05F, Experience.meleeMultiplier(10), 0.0001F);
        assertEquals(0.2F, Experience.meleeMultiplier(-40), 0.0001F);
        assertEquals(0.98F, Experience.meleeMultiplier(-1), 0.0001F);
    }
}
