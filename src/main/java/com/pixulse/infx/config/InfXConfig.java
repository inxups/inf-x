package com.pixulse.infx.config;

import com.pixulse.infx.InfiniteX;
import net.neoforged.neoforge.common.ModConfigSpec;

/** INFX server-side configuration. */
public final class InfXConfig {
    public static final ModConfigSpec SERVER_SPEC;


    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        SERVER_SPEC = builder.build();
    }
}
