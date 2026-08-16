package com.pixulse.infx.config;

import com.iafenvoy.jupiter.config.container.AutoInitConfigContainer;
import com.iafenvoy.jupiter.config.entry.BooleanEntry;
import com.pixulse.infx.InfiniteX;
import net.minecraft.network.chat.Component;

/** Dedicated test-mode switches stored in {@code config/infx/infx-testmode.json}. */
public final class InfXTestModeConfig extends AutoInitConfigContainer {
    public static final InfXTestModeConfig INSTANCE = new InfXTestModeConfig();

    public final ServerConfig server = new ServerConfig();
    public final ClientConfig client = new ClientConfig();

    private InfXTestModeConfig() {
        super(InfiniteX.id("testmode"), Component.literal("InfiniteX Test Mode"), "./config/infx/infx-testmode.json");
    }

    private static BooleanEntry flag(String key, String name, boolean defaultValue) {
        return BooleanEntry.builder(Component.literal(name), defaultValue).key(key).build();
    }

    public static final class ServerConfig extends AutoInitConfigContainer.AutoInitConfigCategoryBase {
        public final BooleanEntry testMode = flag("testMode", "Enable server development test mode", false);

        private ServerConfig() {
            super("server", Component.literal("Server"));
        }
    }

    public static final class ClientConfig extends AutoInitConfigContainer.AutoInitConfigCategoryBase {
        public final BooleanEntry testMode = flag("testMode", "Enable client development test mode", false);

        private ClientConfig() {
            super("client", Component.literal("Client"));
        }
    }
}
