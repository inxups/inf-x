package com.pixulse.infx.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ServerTestModePolicyTest {
    @Test
    void onlyTestModeAllowsOperatorsAndServerManagement() {
        assertTrue(ServerTestModePolicy.allowsPlayerOperators(true));
        assertTrue(ServerTestModePolicy.allowsPlayerLimitBypass(true));
        assertTrue(ServerTestModePolicy.allowsServerManagement(true));

        assertFalse(ServerTestModePolicy.allowsPlayerOperators(false));
        assertFalse(ServerTestModePolicy.allowsPlayerLimitBypass(false));
        assertFalse(ServerTestModePolicy.allowsServerManagement(false));
    }

    @Test
    void nonTestDedicatedStartupOnlyPreservesAnExistingOpsFile() {
        assertTrue(ServerTestModePolicy.shouldSaveOpsAtDedicatedStartup(true, false));
        assertTrue(ServerTestModePolicy.shouldSaveOpsAtDedicatedStartup(true, true));
        assertFalse(ServerTestModePolicy.shouldSaveOpsAtDedicatedStartup(false, false));
        assertTrue(ServerTestModePolicy.shouldSaveOpsAtDedicatedStartup(false, true));
    }

    @Test
    void clientAndServerModesMustMatchExactly() {
        assertTrue(ServerTestModePolicy.modesMatch(false, false));
        assertTrue(ServerTestModePolicy.modesMatch(true, true));
        assertFalse(ServerTestModePolicy.modesMatch(false, true));
        assertFalse(ServerTestModePolicy.modesMatch(true, false));
    }
}
