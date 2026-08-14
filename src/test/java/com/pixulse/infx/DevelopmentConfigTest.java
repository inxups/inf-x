package com.pixulse.infx;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.pixulse.infx.config.InfXClientConfig;
import com.pixulse.infx.config.InfXConfig;
import org.junit.jupiter.api.Test;

class DevelopmentConfigTest {
    @Test
    void developmentModesAreDisabledByDefault() {
        assertFalse(InfXConfig.INSTANCE.development.testMode.getValue());
        assertFalse(InfXClientConfig.INSTANCE.development.testMode.getValue());
    }
}
