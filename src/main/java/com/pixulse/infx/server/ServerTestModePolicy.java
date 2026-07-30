package com.pixulse.infx.server;

/** Centralizes server administration rules that are available only in test mode. */
public final class ServerTestModePolicy {
    private ServerTestModePolicy() {}

    public static boolean allowsPlayerOperators(boolean testMode) {
        return testMode;
    }

    public static boolean allowsPlayerLimitBypass(boolean testMode) {
        return testMode;
    }

    public static boolean allowsServerManagement(boolean testMode) {
        return testMode;
    }

    public static boolean shouldSaveOpsAtDedicatedStartup(boolean testMode, boolean opsFileExists) {
        return testMode || opsFileExists;
    }

    public static boolean modesMatch(boolean serverTestMode, boolean clientTestMode) {
        return serverTestMode == clientTestMode;
    }
}
