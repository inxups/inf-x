package com.pixulse.infx.harvest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HarvestSpeedRulesTest {
    @Test
    void levelsAccelerateWhileR196ConditionsStackMultiplicatively() {
        assertEquals(1.0F, HarvestSpeedRules.multiplier(0, false, false, false, false, false));
        assertEquals(5.0F, HarvestSpeedRules.multiplier(200, false, false, false, false, false));
        float impaired = HarvestSpeedRules.multiplier(0, true, true, true, true, true);
        assertEquals(0.00008F, impaired, 0.000001F);
    }

    @Test
    void convertsModernCorrectToolSpeedToMiteProgressUnits() {
        float strength = 7.0F;
        float hardness = 1.5F;
        float adjustedSpeed = HarvestSpeedRules.toModernBreakSpeed(strength);

        assertEquals(strength * 30.0F / 512.0F, adjustedSpeed);
        assertEquals(strength / hardness / 512.0F, adjustedSpeed / hardness / 30.0F);
    }

    @Test
    void portableBlocksHaveAOneHundredTwentyEightTickBaseline() {
        float hardness = 3.5F;
        float strength = HarvestSpeedRules.portableStrength(hardness, 0.0001F);
        float adjustedSpeed = HarvestSpeedRules.toModernBreakSpeed(strength);

        assertEquals(hardness * 4.0F, strength);
        assertEquals(1.0F / 128.0F, adjustedSpeed / hardness / 30.0F);
        assertEquals(hardness * 8.0F, HarvestSpeedRules.portableStrength(hardness, 2.0F));
    }

    @Test
    void miningFatigueUsesR196LinearStrengthInsteadOfModernExponentialStrength() {
        assertEquals(0.8F, HarvestSpeedRules.miteMiningFatigueMultiplier(0), 1.0E-6F);
        assertEquals(0.6F, HarvestSpeedRules.miteMiningFatigueMultiplier(1), 1.0E-6F);
        assertEquals(0.4F, HarvestSpeedRules.miteMiningFatigueMultiplier(2), 1.0E-6F);
        assertEquals(0.2F, HarvestSpeedRules.miteMiningFatigueMultiplier(3), 1.0E-6F);
        assertEquals(0.0F, HarvestSpeedRules.miteMiningFatigueMultiplier(4), 1.0E-6F);
        assertEquals(0.0027F, HarvestSpeedRules.modernMiningFatigueMultiplier(2));
    }
}
