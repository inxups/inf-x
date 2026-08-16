package com.pixulse.infx;

import com.pixulse.infx.config.InfXDevModeConfig;

/** Accessors for the server and client development configuration switches. */
public final class InfiniteXDevMode {
    private InfiniteXDevMode() {}

    public static boolean isServerEnabled() {
        return InfXDevModeConfig.INSTANCE.server.devMode.getValue();
    }

    public static boolean isClientEnabled() {
        return InfXDevModeConfig.INSTANCE.client.devMode.getValue();
    }
}
