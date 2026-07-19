package com.pixulse.infx.survival;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class R196SurvivalRulesTest {
    @Test
    void capsStartAtThreeIconsAndGrowEveryFiveLevels() {
        assertEquals(6.0D, R196SurvivalRules.healthCap(0));
        assertEquals(6.0D, R196SurvivalRules.foodCap(4));
        assertEquals(8.0D, R196SurvivalRules.healthCap(5));
        assertEquals(20.0D, R196SurvivalRules.foodCap(35));
        assertEquals(86.0D, R196SurvivalRules.healthCap(200));
    }

    @Test
    void energyConsumesSatiationBeforeNutrition() {
        R196SurvivalData data = new R196SurvivalData(2, 5, 100, 100, 100, 0, 0);
        R196SurvivalData first = data.consume(1.5, 1, 20);
        assertEquals(0.5D, first.satiation(), 0.0001D);
        assertEquals(5.0D, first.nutrition(), 0.0001D);
        R196SurvivalData second = first.consume(2.0, 1, 20);
        assertEquals(0.0D, second.satiation(), 0.0001D);
        assertEquals(3.5D, second.nutrition(), 0.0001D);
    }

    @Test
    void malnutritionAndInsulinThresholdsMatchR196() {
        assertTrue(new R196SurvivalData(1, 1, 0, 10, 10, 0, 0).isMalnourished());
        assertTrue(new R196SurvivalData(1, 1, 10, 0, 10, 0, 0).isMalnourished());
        assertFalse(new R196SurvivalData(1, 1, 10, 10, 0, 0, 0).isMalnourished());
        assertEquals(R196SurvivalData.InsulinResistance.MILD,
                new R196SurvivalData(1, 1, 1, 1, 1, 48_000, 0).insulinResistance());
        assertEquals(R196SurvivalData.InsulinResistance.MODERATE,
                new R196SurvivalData(1, 1, 1, 1, 1, 96_000, 0).insulinResistance());
        assertEquals(R196SurvivalData.InsulinResistance.SEVERE,
                new R196SurvivalData(1, 1, 1, 1, 1, 144_000, 0).insulinResistance());
    }

    @Test
    void wetColdSprintIsMoreExpensiveThanIdle() {
        double idle = R196SurvivalRules.metabolism(false, false, false, false, false, false, false, false);
        double exposed = R196SurvivalRules.metabolism(true, true, true, true, false, true, true, true);
        assertTrue(exposed > idle * 10.0D);
        assertEquals(4.0D / 1_250.0D, R196SurvivalRules.recoveryPerTick(true, false, 0), 1.0E-9D);
        assertEquals(0.25D / 1_250.0D, R196SurvivalRules.recoveryPerTick(false, true, 0), 1.0E-9D);
    }
}
