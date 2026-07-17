package com.pixulse.infx.harvest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ToolWearCalculatorTest {
    @Test
    void zeroOrNegativeHardnessDoesNotWearTools() {
        assertEquals(0, ToolWearCalculator.damageForBreaking(0.0F, 1.0F));
        assertEquals(0, ToolWearCalculator.damageForBreaking(-1.0F, 1.0F));
    }

    @Test
    void pickaxeWearScalesWithHardnessAndHasAFivePointFloor() {
        assertEquals(5, ToolWearCalculator.damageForBreaking(0.01F, 1.0F));
        assertEquals(150, ToolWearCalculator.damageForBreaking(1.5F, 1.0F));
    }

    @Test
    void flintHatchetUsesTheR196FourThirdsDecayRate() {
        assertEquals(6, ToolWearCalculator.damageForBreaking(0.01F, ToolWearCalculator.FLINT_HATCHET_DECAY));
        assertEquals(266, ToolWearCalculator.damageForBreaking(2.0F, ToolWearCalculator.FLINT_HATCHET_DECAY));
    }
}
