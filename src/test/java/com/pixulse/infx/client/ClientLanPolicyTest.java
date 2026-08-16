package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ClientLanPolicyTest {
    @Test
    void onlyDevModeAllowsLanCommands() {
        assertTrue(ClientLanPolicy.allowsLanCommands(true));
        assertFalse(ClientLanPolicy.allowsLanCommands(false));
    }
}
