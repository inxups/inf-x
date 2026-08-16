package com.pixulse.infx;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.pixulse.infx.config.InfXTestModeConfig;
import org.junit.jupiter.api.Test;

class DevelopmentConfigTest {
    @Test
    void developmentModesAreDisabledByDefault() {
        assertFalse(InfXTestModeConfig.INSTANCE.server.testMode.getValue());
        assertFalse(InfXTestModeConfig.INSTANCE.client.testMode.getValue());
    }
}
