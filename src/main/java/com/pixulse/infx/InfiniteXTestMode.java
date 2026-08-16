package com.pixulse.infx;

import com.pixulse.infx.config.InfXTestModeConfig;

/** Accessors for the server and client development configuration switches. */
public final class InfiniteXTestMode {
    private InfiniteXTestMode() {}

    public static boolean isServerEnabled() {
        return InfXTestModeConfig.INSTANCE.server.testMode.getValue();
    }

    public static boolean isClientEnabled() {
        return InfXTestModeConfig.INSTANCE.client.testMode.getValue();
    }
}
