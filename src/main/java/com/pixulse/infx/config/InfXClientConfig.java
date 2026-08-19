package com.pixulse.infx.config;

import com.iafenvoy.jupiter.config.container.FileConfigContainer;
import com.iafenvoy.jupiter.config.entry.BooleanEntry;
import com.pixulse.infx.InfiniteX;
import net.minecraft.network.chat.Component;

/** Client-only display preferences stored in {@code config/infx/infx-client.json}. */
public final class InfXClientConfig extends FileConfigContainer {
    public static final InfXClientConfig INSTANCE = new InfXClientConfig();

    public final BooleanEntry detailedFoodTooltips = BooleanEntry.builder(Component.literal("Detailed food tooltips"), true)
            .key("detailedFoodTooltips").build();
    public final BooleanEntry specialMoonRendering = BooleanEntry.builder(Component.literal("Special moon rendering"), true)
            .key("specialMoonRendering").build();
    public final BooleanEntry eliteEyeGlow = BooleanEntry.builder(Component.literal("Elite glowing eyes"), true)
            .key("eliteEyeGlow").build();

    private InfXClientConfig() {
        super(InfiniteX.id("client"), Component.literal("InfiniteX Client"), "./config/infx/infx-client.json");
    }

    @Override
    public void init() {
        createTab("client", Component.literal("Client"))
                .addEntry(detailedFoodTooltips)
                .addEntry(specialMoonRendering)
                .addEntry(eliteEyeGlow);
    }
}
