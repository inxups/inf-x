package com.pixulse.infx.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ServerTestModePolicyTest {
    @Test
    void onlyTestModeAllowsOperatorsAndServerManagement() {
        assertTrue(ServerTestModePolicy.allowsPlayerOperators(true));
        assertTrue(ServerTestModePolicy.allowsPlayerPermissions(true));
        assertTrue(ServerTestModePolicy.allowsPlayerLimitBypass(true));
        assertTrue(ServerTestModePolicy.allowsServerManagement(true));

        assertFalse(ServerTestModePolicy.allowsPlayerOperators(false));
        assertFalse(ServerTestModePolicy.allowsPlayerPermissions(false));
        assertFalse(ServerTestModePolicy.allowsPlayerLimitBypass(false));
        assertFalse(ServerTestModePolicy.allowsServerManagement(false));
    }

    @Test
    void normalModeAllowsOnlyTheConsoleCommandAllowlist() {
        String[] allowed = {
                "ban", "ban-ip", "pardon", "pardon-ip", "kick", "whitelist", "stop",
                "save-off", "save-on", "help", "?", "list", "seed", "say", "me",
                "msg", "tell", "w", "scoreboard", "team", "tag", "bossbar", "recipe",
                "datapack", "reload", "schedule", "particle", "playsound", "title",
                "tellraw", "teammsg", "tm", "debug", "jfr", "perf", "random", "save-all"
        };

        for (String command : allowed) {
            assertTrue(ServerTestModePolicy.allowsConsoleCommand(false, command), command);
        }

        assertTrue(ServerTestModePolicy.allowsConsoleCommand(false, "/say hello"));
        assertTrue(ServerTestModePolicy.allowsConsoleCommand(false, "  save-all flush  "));
        assertFalse(ServerTestModePolicy.allowsConsoleCommand(false, "op player"));
        assertFalse(ServerTestModePolicy.allowsConsoleCommand(false, "give player minecraft:stone"));
        assertFalse(ServerTestModePolicy.allowsConsoleCommand(false, "execute as player run say hello"));
        assertFalse(ServerTestModePolicy.allowsConsoleCommand(false, ""));
    }

    @Test
    void testModeKeepsAllConsoleCommands() {
        assertTrue(ServerTestModePolicy.allowsConsoleCommand(true, "give player minecraft:stone"));
        assertTrue(ServerTestModePolicy.allowsConsoleCommand(true, "anything"));
    }

    @Test
    void commandRootNormalizesConsoleInput() {
        assertTrue(ServerTestModePolicy.commandRoot(" /Tell player hello ").equals("tell"));
        assertTrue(ServerTestModePolicy.commandRoot(null).isEmpty());
    }

    @Test
    void nonTestDedicatedStartupOnlyPreservesAnExistingOpsFile() {
        assertTrue(ServerTestModePolicy.shouldSaveOpsAtDedicatedStartup(true, false));
        assertTrue(ServerTestModePolicy.shouldSaveOpsAtDedicatedStartup(true, true));
        assertFalse(ServerTestModePolicy.shouldSaveOpsAtDedicatedStartup(false, false));
        assertTrue(ServerTestModePolicy.shouldSaveOpsAtDedicatedStartup(false, true));
    }

}
