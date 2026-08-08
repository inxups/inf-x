package com.pixulse.infx.config;

import com.pixulse.infx.InfiniteX;
import net.neoforged.neoforge.common.ModConfigSpec;

/** INFX server-side configuration. */
public final class InfXConfig {
    public static final ModConfigSpec SERVER_SPEC;
    /** Whether tools can block incoming attacks (the BlocksAttacks tool component). */
    public static final ModConfigSpec.BooleanValue TOOLS_BLOCK_ATTACKS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        TOOLS_BLOCK_ATTACKS = builder.comment(
                        "Whether tools (pickaxes, axes, swords, ...) can block incoming attacks",
                        "like a shield. Disable to restore vanilla tool behavior.",
                        "Requires a restart after changing.")
                .define("toolsBlockAttacks", true);
        SERVER_SPEC = builder.build();
    }

    private InfXConfig() {}

    /** Safe read for code paths that may run before the config file is loaded. */
    public static boolean toolsBlockAttacks() {
        return !SERVER_SPEC.isLoaded() || TOOLS_BLOCK_ATTACKS.get();
    }
}
