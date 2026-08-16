package com.pixulse.infx.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ServerDevModePolicyTest {
    @Test
    void onlyDevModeAllowsOperatorsAndServerManagement() {
        assertTrue(ServerDevModePolicy.allowsPlayerOperators(true));
        assertTrue(ServerDevModePolicy.allowsPlayerPermissions(true));
        assertTrue(ServerDevModePolicy.allowsPlayerLimitBypass(true));
        assertTrue(ServerDevModePolicy.allowsServerManagement(true));

        assertFalse(ServerDevModePolicy.allowsPlayerOperators(false));
        assertFalse(ServerDevModePolicy.allowsPlayerPermissions(false));
        assertFalse(ServerDevModePolicy.allowsPlayerLimitBypass(false));
        assertFalse(ServerDevModePolicy.allowsServerManagement(false));
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
            assertTrue(ServerDevModePolicy.allowsConsoleCommand(false, command), command);
        }

        assertTrue(ServerDevModePolicy.allowsConsoleCommand(false, "/say hello"));
        assertTrue(ServerDevModePolicy.allowsConsoleCommand(false, "  save-all flush  "));
        assertFalse(ServerDevModePolicy.allowsConsoleCommand(false, "op player"));
        assertFalse(ServerDevModePolicy.allowsConsoleCommand(false, "give player minecraft:stone"));
        assertFalse(ServerDevModePolicy.allowsConsoleCommand(false, "execute as player run say hello"));
        assertFalse(ServerDevModePolicy.allowsConsoleCommand(false, ""));
    }

    @Test
    void devModeKeepsAllConsoleCommands() {
        assertTrue(ServerDevModePolicy.allowsConsoleCommand(true, "give player minecraft:stone"));
        assertTrue(ServerDevModePolicy.allowsConsoleCommand(true, "anything"));
    }

    @Test
    void commandRootNormalizesConsoleInput() {
        assertTrue(ServerDevModePolicy.commandRoot(" /Tell player hello ").equals("tell"));
        assertTrue(ServerDevModePolicy.commandRoot(null).isEmpty());
    }

    @Test
    void nonTestDedicatedStartupOnlyPreservesAnExistingOpsFile() {
        assertTrue(ServerDevModePolicy.shouldSaveOpsAtDedicatedStartup(true, false));
        assertTrue(ServerDevModePolicy.shouldSaveOpsAtDedicatedStartup(true, true));
        assertFalse(ServerDevModePolicy.shouldSaveOpsAtDedicatedStartup(false, false));
        assertTrue(ServerDevModePolicy.shouldSaveOpsAtDedicatedStartup(false, true));
    }

}
