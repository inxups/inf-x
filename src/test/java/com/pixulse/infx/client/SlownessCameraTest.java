package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SlownessCameraTest {
    @Test
    void noSlownessLeavesTurnRateUntouched() {
        assertEquals(1.0F, SlownessCamera.turnFactor(0, 0), 0.0F);
        assertEquals(1.0F, SlownessCamera.turnFactor(0, 2), 0.0F);
    }

    @Test
    void slownessScalesTurnRatePerMiteFormula() {
        assertEquals(0.25F, SlownessCamera.turnFactor(1, 0), 0.0001F);
        assertEquals(1.0F / 7.0F, SlownessCamera.turnFactor(2, 0), 0.0001F);
        assertEquals(0.1F, SlownessCamera.turnFactor(3, 0), 0.0001F);
        assertEquals(1.0F / 13.0F, SlownessCamera.turnFactor(4, 0), 0.0001F);
        assertEquals(1.0F / 13.0F, SlownessCamera.turnFactor(5, 0), 0.0001F);
    }

    @Test
    void speedEffectCancelsSlownessLevelForLevel() {
        assertEquals(1.0F, SlownessCamera.turnFactor(1, 1), 0.0F);
        assertEquals(0.25F, SlownessCamera.turnFactor(2, 1), 0.0001F);
    }
}
