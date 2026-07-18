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
}
