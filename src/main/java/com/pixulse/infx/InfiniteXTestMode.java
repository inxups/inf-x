package com.pixulse.infx;

import com.pixulse.infx.config.InfXClientConfig;
import com.pixulse.infx.config.InfXConfig;

/** Accessors for the server and client development configuration switches. */
public final class InfiniteXTestMode {
    private InfiniteXTestMode() {}

    public static boolean isServerEnabled() {
        return InfXConfig.INSTANCE.development.testMode.getValue();
    }

    public static boolean isClientEnabled() {
        return InfXClientConfig.INSTANCE.development.testMode.getValue();
    }
}
