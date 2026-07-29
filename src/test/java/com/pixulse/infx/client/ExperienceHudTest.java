package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    @Test
    void debtHudMixinIsRegistered() throws IOException {
        var stream = ExperienceHudTest.class.getClassLoader().getResourceAsStream("infx.mixins.json");
        assertNotNull(stream);
        try (stream) {
            String mixins = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(mixins.contains("\"GuiExperienceMixin\""));
        }
    }
}
