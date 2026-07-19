package com.pixulse.infx;

import org.jspecify.annotations.Nullable;

/** Startup-only escape hatch for development worlds that need unrestricted vanilla controls. */
public final class InfiniteXTestMode {
    public static final String SYSTEM_PROPERTY = "infx.testMode";
    private static final boolean ENABLED = parse(System.getProperty(SYSTEM_PROPERTY));

    private InfiniteXTestMode() {}

    public static boolean isEnabled() {
        return ENABLED;
    }

    static boolean parse(@Nullable String value) {
        return Boolean.parseBoolean(value);
    }
}
