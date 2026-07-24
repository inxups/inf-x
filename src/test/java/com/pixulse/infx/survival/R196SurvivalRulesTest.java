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
        assertEquals(20.0D, R196SurvivalRules.healthCap(200));
        assertEquals(20.0D, R196SurvivalRules.foodCap(200));
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
    void baselineMetabolismPeriodicallyConsumesNutrition() {
        R196SurvivalData data = new R196SurvivalData(5, 5, 100, 100, 100, 0, 0);
        for (int unit = 0; unit < 3; unit++) {
            data = data.metabolize(1.0D, 0.25D, 0, 20);
        }
        assertEquals(2.0D, data.satiation(), 0.0001D);
        assertEquals(5.0D, data.nutrition(), 0.0001D);

        data = data.metabolize(1.0D, 0.25D, 0, 20);
        assertEquals(2.0D, data.satiation(), 0.0001D);
        assertEquals(4.0D, data.nutrition(), 0.0001D);
        assertEquals(0.0D, data.nutritionHungerProgress(), 0.0001D);
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
    void anyRemainingR196EnergyPermitsExhaustiveMovement() {
        assertTrue(new R196SurvivalData(0.1, 0, 1, 1, 1, 0, 0).hasFoodEnergy(),
                "remaining Satiation must permit sprinting");
        assertTrue(new R196SurvivalData(0, 0.1, 1, 1, 1, 0, 0).hasFoodEnergy(),
                "remaining Nutrition must permit sprinting");
        assertFalse(new R196SurvivalData(0, 0, 1, 1, 1, 0, 0).hasFoodEnergy(),
                "empty Satiation and Nutrition must stop sprinting");
        assertFalse(new R196SurvivalData(0.0001, 0, 1, 1, 1, 0, 0).hasFoodEnergy(),
                "sub-unit rounding noise must not re-enable sprinting");
    }

    @Test
    void emptyFoodProfileIsZeroed() {
        assertEquals(0.0D, R196FoodProfile.EMPTY.satiation());
        assertEquals(0.0D, R196FoodProfile.EMPTY.nutrition());
        assertEquals(0, R196FoodProfile.EMPTY.protein());
        assertEquals(0, R196FoodProfile.EMPTY.phytonutrients());
        assertEquals(0, R196FoodProfile.EMPTY.essentialFats());
        assertEquals(0, R196FoodProfile.EMPTY.sugar());
    }

    @Test
    void baselineAndStatusCostsMatchR196FoodUnits() {
        assertEquals(0.0005D, R196SurvivalRules.baselineMetabolism(false, false, false), 1.0E-9D);
        assertEquals(0.001D, R196SurvivalRules.baselineMetabolism(true, true, true), 1.0E-9D);
        assertEquals(0.2D, R196SurvivalRules.enduranceModifier(4), 1.0E-9D);
        assertEquals(0.0125D, R196SurvivalRules.hungerEffectMetabolism(2), 1.0E-9D);
        assertEquals(1.0D / 1_250.0D, R196SurvivalRules.recoveryPerTick(20, false, false, 0), 1.0E-9D);
        assertEquals(4.0D * 0.00052D, R196SurvivalRules.recoveryPerTick(6, true, false, 0), 1.0E-9D);
        assertEquals(0.25D * 0.00052D, R196SurvivalRules.recoveryPerTick(6, false, true, 0), 1.0E-9D);
    }

    @Test
    void movementCostsUseR196DistanceRatesWithoutOverlap() {
        assertEquals(0.0025D, R196SurvivalRules.movementMetabolism(100, 0, 0, 0, 0, 0, 0), 1.0E-9D);
        assertEquals(0.0025D, R196SurvivalRules.movementMetabolism(0, 100, 0, 0, 0, 0, 0), 1.0E-9D);
        assertEquals(0.0125D, R196SurvivalRules.movementMetabolism(0, 0, 100, 0, 0, 0, 0), 1.0E-9D);
        assertEquals(0.00375D, R196SurvivalRules.movementMetabolism(0, 0, 0, 100, 0, 0, 0), 1.0E-9D);
        assertEquals(0.00375D, R196SurvivalRules.movementMetabolism(0, 0, 0, 0, 100, 0, 0), 1.0E-9D);
        assertEquals(0.00375D, R196SurvivalRules.movementMetabolism(0, 0, 0, 0, 0, 100, 0), 1.0E-9D);
        assertEquals(0.025D, R196SurvivalRules.movementMetabolism(0, 0, 0, 0, 0, 0, 100), 1.0E-9D);
        assertEquals(0.0D, R196SurvivalRules.movementMetabolism(-100, 0, 0, 0, 0, 0, 0), 1.0E-9D);
    }

    @Test
    void discreteBehaviorCostsMatchR196Conversion() {
        assertEquals(0.05D, R196SurvivalRules.jumpMetabolism(false), 1.0E-9D);
        assertEquals(0.2D, R196SurvivalRules.jumpMetabolism(true), 1.0E-9D);
        assertEquals(0.075D, R196SurvivalRules.ATTACK_METABOLISM, 1.0E-9D);
        assertEquals(0.0025D, R196SurvivalRules.MINING_METABOLISM_PER_TICK, 1.0E-9D);
        assertEquals(0.0025D, R196SurvivalRules.BOW_DRAW_METABOLISM_PER_TICK, 1.0E-9D);
        assertEquals(0.0025D, R196SurvivalRules.ROW_METABOLISM_PER_TICK, 1.0E-9D);
        assertEquals(0.075D, R196SurvivalRules.DAMAGE_METABOLISM, 1.0E-9D);
    }

    @Test
    void blockActionsUseHardnessAndSafeBounds() {
        assertEquals(0.125D, R196SurvivalRules.placementMetabolism(0.5D), 1.0E-9D);
        assertEquals(5.0D, R196SurvivalRules.placementMetabolism(50.0D), 1.0E-9D);
        assertEquals(0.0D, R196SurvivalRules.placementMetabolism(-1.0D), 1.0E-9D);
        assertEquals(0.0625D, R196SurvivalRules.tillingMetabolism(0.5D), 1.0E-9D);
        assertEquals(0.0D, R196SurvivalRules.tillingMetabolism(-1.0D), 1.0E-9D);
    }
}
