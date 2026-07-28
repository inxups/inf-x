package com.pixulse.infx.food;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.serialization.JsonOps;
import com.pixulse.infx.data.food.FoodIngestion;
import com.pixulse.infx.data.food.FoodProfile;
import com.pixulse.infx.data.food.SurvivalData;
import com.pixulse.infx.data.food.SurvivalRules;
import org.junit.jupiter.api.Test;

class SurvivalRulesTest {
    @Test
    void attachmentDefaultStartsWithR196SurvivalState() {
        SurvivalData data = SurvivalData.initial();

        assertEquals(6.0D, data.satiation());
        assertEquals(6.0D, data.nutrition());
        assertEquals(SurvivalData.NUTRIENT_CAP, data.protein());
        assertEquals(SurvivalData.NUTRIENT_CAP, data.phytonutrients());
        assertEquals(SurvivalData.NUTRIENT_CAP, data.essentialFats());
        assertEquals(0, data.insulinResponse());
    }

    @Test
    void attachmentDataCodecPersistsAllMetabolicProgress() {
        SurvivalData expected = new SurvivalData(3.5D, 2.25D, 1, 2, 3, 4, 0.5D, 0.6D, 0.7D, 0.8D);

        var encoded = SurvivalData.CODEC.encodeStart(JsonOps.INSTANCE, expected).getOrThrow();

        assertEquals(expected, SurvivalData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
    }

    @Test
    void capsStartAtThreeIconsAndGrowEveryFiveLevels() {
        assertEquals(6.0D, SurvivalRules.healthCap(0));
        assertEquals(6.0D, SurvivalRules.foodCap(4));
        assertEquals(8.0D, SurvivalRules.healthCap(5));
        assertEquals(20.0D, SurvivalRules.foodCap(35));
        assertEquals(20.0D, SurvivalRules.healthCap(200));
        assertEquals(20.0D, SurvivalRules.foodCap(200));
    }

    @Test
    void energyConsumesSatiationBeforeNutrition() {
        SurvivalData data = new SurvivalData(2, 5, 100, 100, 100, 0, 0);
        SurvivalData first = data.consume(1.5, 1, 20);
        assertEquals(0.5D, first.satiation(), 0.0001D);
        assertEquals(5.0D, first.nutrition(), 0.0001D);
        SurvivalData second = first.consume(2.0, 1, 20);
        assertEquals(0.0D, second.satiation(), 0.0001D);
        assertEquals(3.5D, second.nutrition(), 0.0001D);
    }

    @Test
    void baselineMetabolismPeriodicallyConsumesNutrition() {
        SurvivalData data = new SurvivalData(5, 5, 100, 100, 100, 0, 0);
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
        assertTrue(new SurvivalData(1, 1, 0, 10, 10, 0, 0).isMalnourished());
        assertTrue(new SurvivalData(1, 1, 10, 0, 10, 0, 0).isMalnourished());
        assertFalse(new SurvivalData(1, 1, 10, 10, 0, 0, 0).isMalnourished());
        assertEquals(SurvivalData.InsulinResistance.MILD,
                new SurvivalData(1, 1, 1, 1, 1, 48_000, 0).insulinResistance());
        assertEquals(SurvivalData.InsulinResistance.MODERATE,
                new SurvivalData(1, 1, 1, 1, 1, 96_000, 0).insulinResistance());
        assertEquals(SurvivalData.InsulinResistance.SEVERE,
                new SurvivalData(1, 1, 1, 1, 1, 144_000, 0).insulinResistance());
        assertEquals(192_000, new SurvivalData(1, 1, 1, 1, 1, 200_000, 0).clamp(20).insulinResponse());
    }

    @Test
    void sourceFoodFlagsDeriveLongTermNutrientsAndInsulinResponse() {
        FoodProfile fish = FoodProfile.mite(3, 3, 1_000, true, true, false);
        assertEquals(24_000, fish.protein());
        assertEquals(24_000, fish.essentialFats());
        assertEquals(0, fish.phytonutrients());
        assertEquals(4_800, fish.insulinResponse());

        FoodProfile wheatSeeds = FoodProfile.mite(1, 0, 0, false, true, false, 2_000, false);
        assertEquals(2_000, wheatSeeds.essentialFats());

        SurvivalData moderate = new SurvivalData(0, 0, 1, 1, 1, 96_000, 0);
        SurvivalData afterApple = moderate.eat(FoodProfile.mite(2, 1, 1_000, false, false, true), 20);
        assertEquals(1.0D, afterApple.satiation(), 1.0E-9D);
        assertEquals(100_800, afterApple.insulinResponse());
        assertFalse(afterApple.canMetabolizeFoodSugars());

        SurvivalData capped = new SurvivalData(0, 0, 1, 1, 1, 191_999, 0)
                .eat(FoodProfile.mite(1, 0, 1_000, false, false, false), 20);
        assertEquals(192_000, capped.insulinResponse());
    }

    @Test
    void ingestionGateHonorsFoodContentAndNutrientDeficits() {
        FoodProfile protein = FoodProfile.mite(1, 1, 0, true, false, false);
        FoodProfile phytonutrients = FoodProfile.mite(1, 1, 0, false, false, true);
        FoodProfile satiationOnly = FoodProfile.mite(1, 0, 0, false, false, false);
        FoodProfile nutritionOnly = FoodProfile.mite(0, 1, 0, false, false, false);
        FoodProfile milk = FoodProfile.mite(0, 1, 0, true, false, false, 0, true);

        SurvivalData proteinDeficit = new SurvivalData(6, 6, 0, 1, 1, 0, 0);
        assertTrue(FoodIngestion.canIngest(proteinDeficit, 6, protein));
        assertFalse(FoodIngestion.canIngest(proteinDeficit, 6, phytonutrients));
        assertTrue(FoodIngestion.canIngest(proteinDeficit, 6, milk));

        assertTrue(FoodIngestion.canIngest(new SurvivalData(0, 6, 1, 1, 1, 0, 0), 6, satiationOnly));
        assertFalse(FoodIngestion.canIngest(new SurvivalData(0, 6, 1, 1, 1, 0, 0), 6, nutritionOnly));
        assertFalse(FoodIngestion.canIngest(new SurvivalData(6, 5, 1, 1, 1, 0, 0), 6, satiationOnly));
        assertTrue(FoodIngestion.canIngest(new SurvivalData(6, 5, 1, 1, 1, 0, 0), 6, nutritionOnly));
    }

    @Test
    void anyRemainingR196EnergyPermitsExhaustiveMovement() {
        assertTrue(new SurvivalData(0.1, 0, 1, 1, 1, 0, 0).hasFoodEnergy(),
                "remaining Satiation must permit sprinting");
        assertTrue(new SurvivalData(0, 0.1, 1, 1, 1, 0, 0).hasFoodEnergy(),
                "remaining Nutrition must permit sprinting");
        assertFalse(new SurvivalData(0, 0, 1, 1, 1, 0, 0).hasFoodEnergy(),
                "empty Satiation and Nutrition must stop sprinting");
        assertFalse(new SurvivalData(0.0001, 0, 1, 1, 1, 0, 0).hasFoodEnergy(),
                "sub-unit rounding noise must not re-enable sprinting");
    }

    @Test
    void emptyFoodProfileIsZeroed() {
        assertEquals(0.0D, FoodProfile.EMPTY.satiation());
        assertEquals(0.0D, FoodProfile.EMPTY.nutrition());
        assertEquals(0, FoodProfile.EMPTY.protein());
        assertEquals(0, FoodProfile.EMPTY.phytonutrients());
        assertEquals(0, FoodProfile.EMPTY.essentialFats());
        assertEquals(0, FoodProfile.EMPTY.sugarContent());
        assertFalse(FoodProfile.EMPTY.alwaysEdible());
    }

    @Test
    void baselineAndStatusCostsMatchR196FoodUnits() {
        assertEquals(0.0005D, SurvivalRules.baselineMetabolism(false, false, false), 1.0E-9D);
        assertEquals(0.001D, SurvivalRules.baselineMetabolism(true, true, true), 1.0E-9D);
        assertEquals(0.2D, SurvivalRules.enduranceModifier(4), 1.0E-9D);
        assertEquals(0.0125D, SurvivalRules.hungerEffectMetabolism(2), 1.0E-9D);
        assertEquals(1.0D / 1_250.0D, SurvivalRules.recoveryPerTick(20, false, false, 0), 1.0E-9D);
        assertEquals(4.0D * 0.00052D, SurvivalRules.recoveryPerTick(6, true, false, 0), 1.0E-9D);
        assertEquals(0.25D * 0.00052D, SurvivalRules.recoveryPerTick(6, false, true, 0), 1.0E-9D);
    }

    @Test
    void movementCostsUseR196DistanceRatesWithoutOverlap() {
        assertEquals(0.0025D, SurvivalRules.movementMetabolism(100, 0, 0, 0, 0, 0, 0), 1.0E-9D);
        assertEquals(0.0025D, SurvivalRules.movementMetabolism(0, 100, 0, 0, 0, 0, 0), 1.0E-9D);
        assertEquals(0.0125D, SurvivalRules.movementMetabolism(0, 0, 100, 0, 0, 0, 0), 1.0E-9D);
        assertEquals(0.00375D, SurvivalRules.movementMetabolism(0, 0, 0, 100, 0, 0, 0), 1.0E-9D);
        assertEquals(0.00375D, SurvivalRules.movementMetabolism(0, 0, 0, 0, 100, 0, 0), 1.0E-9D);
        assertEquals(0.00375D, SurvivalRules.movementMetabolism(0, 0, 0, 0, 0, 100, 0), 1.0E-9D);
        assertEquals(0.025D, SurvivalRules.movementMetabolism(0, 0, 0, 0, 0, 0, 100), 1.0E-9D);
        assertEquals(0.0D, SurvivalRules.movementMetabolism(-100, 0, 0, 0, 0, 0, 0), 1.0E-9D);
    }

    @Test
    void discreteBehaviorCostsMatchR196Conversion() {
        assertEquals(0.05D, SurvivalRules.jumpMetabolism(false), 1.0E-9D);
        assertEquals(0.2D, SurvivalRules.jumpMetabolism(true), 1.0E-9D);
        assertEquals(0.075D, SurvivalRules.ATTACK_METABOLISM, 1.0E-9D);
        assertEquals(0.0025D, SurvivalRules.MINING_METABOLISM_PER_TICK, 1.0E-9D);
        assertEquals(0.0025D, SurvivalRules.BOW_DRAW_METABOLISM_PER_TICK, 1.0E-9D);
        assertEquals(0.0025D, SurvivalRules.ROW_METABOLISM_PER_TICK, 1.0E-9D);
        assertEquals(0.075D, SurvivalRules.DAMAGE_METABOLISM, 1.0E-9D);
    }

    @Test
    void blockActionsUseHardnessAndSafeBounds() {
        assertEquals(0.125D, SurvivalRules.placementMetabolism(0.5D), 1.0E-9D);
        assertEquals(5.0D, SurvivalRules.placementMetabolism(50.0D), 1.0E-9D);
        assertEquals(0.0D, SurvivalRules.placementMetabolism(-1.0D), 1.0E-9D);
        assertEquals(0.0625D, SurvivalRules.tillingMetabolism(0.5D), 1.0E-9D);
        assertEquals(0.0D, SurvivalRules.tillingMetabolism(-1.0D), 1.0E-9D);
    }
}
