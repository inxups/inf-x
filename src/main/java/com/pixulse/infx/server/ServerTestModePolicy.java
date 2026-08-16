package com.pixulse.infx.server;

import com.pixulse.infx.InfiniteXTestMode;
import java.util.Locale;
import java.util.Set;
import net.minecraft.server.MinecraftServer;

/** Centralizes server administration and command rules for each server mode. */
public final class ServerTestModePolicy {
    private static final Set<String> NON_TEST_CONSOLE_COMMANDS = Set.of(
            "ban",
            "ban-ip",
            "pardon",
            "pardon-ip",
            "kick",
            "whitelist",
            "stop",
            "save-off",
            "save-on",
            "help",
            "?",
            "list",
            "seed",
            "say",
            "me",
            "msg",
            "tell",
            "w",
            "scoreboard",
            "team",
            "tag",
            "bossbar",
            "recipe",
            "datapack",
            "reload",
            "schedule",
            "particle",
            "playsound",
            "title",
            "tellraw",
            "teammsg",
            "tm",
            "debug",
            "jfr",
            "perf",
            "random",
            "save-all");

    private ServerTestModePolicy() {}

    /**
     * Effective server mode for a live server: the development config switch, or any integrated
     * world whose commands are enabled. The LAN and world-creation locks only allow commands to be
     * switched on in client test mode, so such worlds keep vanilla administration — every LAN
     * player becomes an operator, and a cheated single-player owner too.
     */
    public static boolean effectiveTestMode(MinecraftServer server) {
        if (InfiniteXTestMode.isServerEnabled()) return true;
        return server != null
                && server.isSingleplayer()
                && (server.getPlayerList().isAllowCommandsForAllPlayers()
                    || server.getWorldData().isAllowCommands());
    }

    public static boolean allowsPlayerOperators(boolean testMode) {
        return testMode;
    }

    public static boolean allowsPlayerPermissions(boolean testMode) {
        return testMode;
    }

    public static boolean allowsPlayerLimitBypass(boolean testMode) {
        return testMode;
    }

    public static boolean allowsServerManagement(boolean testMode) {
        return testMode;
    }

    /**
     * Returns whether a command may be entered through the dedicated-server console.
     * Test mode keeps the complete vanilla command dispatcher; normal mode is allowlisted.
     */
    public static boolean allowsConsoleCommand(boolean testMode, String command) {
        return testMode || NON_TEST_CONSOLE_COMMANDS.contains(commandRoot(command));
    }

    /** Extracts the command root while accepting the optional console slash prefix. */
    public static String commandRoot(String command) {
        if (command == null) {
            return "";
        }

        String normalized = command.trim();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1).trim();
        }

        int end = 0;
        while (end < normalized.length() && !Character.isWhitespace(normalized.charAt(end))) {
            end++;
        }
        return normalized.substring(0, end).toLowerCase(Locale.ROOT);
    }

    public static boolean shouldSaveOpsAtDedicatedStartup(boolean testMode, boolean opsFileExists) {
        return testMode || opsFileExists;
    }

}
