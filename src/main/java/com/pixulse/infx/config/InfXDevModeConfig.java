package com.pixulse.infx.config;

import com.iafenvoy.jupiter.config.container.AutoInitConfigContainer;
import com.iafenvoy.jupiter.config.entry.BooleanEntry;
import com.pixulse.infx.InfiniteX;
import net.minecraft.network.chat.Component;

/** Dedicated dev-mode switches stored in {@code config/infx/infx-devmode.json}. */
public final class InfXDevModeConfig extends AutoInitConfigContainer {
    public static final InfXDevModeConfig INSTANCE = new InfXDevModeConfig();

    public final ServerConfig server = new ServerConfig();
    public final ClientConfig client = new ClientConfig();

    private InfXDevModeConfig() {
        super(InfiniteX.id("devmode"), Component.literal("InfiniteX Dev Mode"), "./config/infx/infx-devmode.json");
    }

    private static BooleanEntry flag(String key, String name, boolean defaultValue) {
        return BooleanEntry.builder(Component.literal(name), defaultValue).key(key).build();
    }

    public static final class ServerConfig extends AutoInitConfigContainer.AutoInitConfigCategoryBase {
        public final BooleanEntry devMode = flag("devMode", "Enable server dev mode", false);

        private ServerConfig() {
            super("server", Component.literal("Server"));
        }
    }

    public static final class ClientConfig extends AutoInitConfigContainer.AutoInitConfigCategoryBase {
        public final BooleanEntry devMode = flag("devMode", "Enable client dev mode", false);

        private ClientConfig() {
            super("client", Component.literal("Client"));
        }
    }
}
