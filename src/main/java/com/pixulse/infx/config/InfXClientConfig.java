package com.pixulse.infx.config;

import com.iafenvoy.jupiter.config.container.FileConfigContainer;
import com.iafenvoy.jupiter.config.container.AutoInitConfigContainer;
import com.iafenvoy.jupiter.config.entry.BooleanEntry;
import com.pixulse.infx.InfiniteX;
import net.minecraft.network.chat.Component;

/** Client-only display preferences stored in {@code config/infx/infx-client.json}. */
public final class InfXClientConfig extends FileConfigContainer {
    public static final InfXClientConfig INSTANCE = new InfXClientConfig();

    public final DevelopmentConfig development = new DevelopmentConfig();
    public final BooleanEntry detailedFoodTooltips = BooleanEntry.builder(Component.literal("Detailed food tooltips"), true)
            .key("detailedFoodTooltips").build();
    public final BooleanEntry specialMoonRendering = BooleanEntry.builder(Component.literal("Special moon rendering"), true)
            .key("specialMoonRendering").build();

    private InfXClientConfig() {
        super(InfiniteX.id("client"), Component.literal("InfiniteX Client"), "./config/infx/infx-client.json");
    }

    @Override
    public void init() {
        createTab("development", Component.literal("Development"))
                .addEntry(development.testMode);
        createTab("client", Component.literal("Client"))
                .addEntry(detailedFoodTooltips)
                .addEntry(specialMoonRendering);
    }

    public static final class DevelopmentConfig extends AutoInitConfigContainer.AutoInitConfigCategoryBase {
        public final BooleanEntry testMode = BooleanEntry.builder(
                        Component.literal("Enable client development test mode"), false)
                .key("testMode").build();

        private DevelopmentConfig() {
            super("development", Component.literal("Development"));
        }
    }
}
