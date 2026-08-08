package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ExperienceHudTest {
    @Test
    void onlyNegativeLevelsUseTheDebtHudPath() {
        assertTrue(ExperienceHud.isDebtLevel(-1));
        assertTrue(ExperienceHud.isDebtLevel(-40));
        assertFalse(ExperienceHud.isDebtLevel(0));
        assertFalse(ExperienceHud.isDebtLevel(1));
    }

    @Test
    void debtLevelTextUsesBrightRed() {
        assertEquals(0xFFFF5555, ExperienceHud.DEBT_LEVEL_COLOR);
    }
}
