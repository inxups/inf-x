package com.pixulse.infx.client;

/** Centralizes client-side LAN sharing rules per game mode. */
public final class ClientLanPolicy {
    private ClientLanPolicy() {}

    /**
     * Returns whether the Share-to-LAN screen may offer the "Allow Commands" toggle.
     * Normal play never publishes a LAN world with cheats; only dev mode may.
     */
    public static boolean allowsLanCommands(boolean devMode) {
        return devMode;
    }
}
