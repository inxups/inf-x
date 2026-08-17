package com.pixulse.infx;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.config.InfXConfig;
import com.pixulse.infx.config.InfXDevModeConfig;
import org.junit.jupiter.api.Test;

class DevelopmentConfigTest {
    @Test
    void developmentModesAreDisabledByDefault() {
        assertFalse(InfXDevModeConfig.INSTANCE.server.devMode.getValue());
        assertFalse(InfXDevModeConfig.INSTANCE.client.devMode.getValue());
    }

    @Test
    void moonSpawnGatingDefaultsToEnabled() {
        assertTrue(InfXConfig.INSTANCE.mobs.moonSpawnGating.getValue());
    }
}
