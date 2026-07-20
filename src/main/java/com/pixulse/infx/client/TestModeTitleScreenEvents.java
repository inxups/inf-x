package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.InfiniteXTestMode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** Marks test-mode title screens and prevents unrestricted clients from entering multiplayer. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class TestModeTitleScreenEvents {
    static final String TEST_MODE_LABEL_KEY = "menu.infx.test_mode";
    static final Component TEST_MODE_LABEL = Component.translatable(TEST_MODE_LABEL_KEY)
            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
    private static final Component MULTIPLAYER_DISABLED = Component.translatable(
            "menu.infx.test_mode.multiplayer_disabled");
    private static final String MULTIPLAYER_BUTTON = "menu.multiplayer";

    private TestModeTitleScreenEvents() {}

    @SubscribeEvent
    private static void configureTitleScreen(ScreenEvent.Init.Post event) {
        if (!InfiniteXTestMode.isEnabled() || !(event.getScreen() instanceof TitleScreen screen)) return;
        disableMultiplayerButton(event.getListenersList());
        event.addListener(createTestModeLabel(screen));
    }

    private static StringWidget createTestModeLabel(TitleScreen screen) {
        var font = Minecraft.getInstance().font;
        int width = font.width(TEST_MODE_LABEL);
        return new StringWidget(
                screen.width / 2 - width / 2,
                10,
                width,
                9,
                TEST_MODE_LABEL,
                font);
    }

    static void disableMultiplayerButton(Iterable<? extends GuiEventListener> listeners) {
        for (GuiEventListener listener : listeners) {
            if (!(listener instanceof Button button)
                    || !hasTranslationKey(button.getMessage(), MULTIPLAYER_BUTTON)) {
                continue;
            }
            button.active = false;
            button.setTooltip(Tooltip.create(MULTIPLAYER_DISABLED));
        }
    }

    private static boolean hasTranslationKey(Component component, String key) {
        if (component.getContents() instanceof TranslatableContents contents) {
            if (contents.getKey().equals(key)) return true;
            for (Object argument : contents.getArgs()) {
                if (argument instanceof Component nested && hasTranslationKey(nested, key)) return true;
            }
        }
        return component.getSiblings().stream().anyMatch(sibling -> hasTranslationKey(sibling, key));
    }
}
