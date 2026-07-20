package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.InfiniteXTestMode;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.LogoRenderer;
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

/** Marks test-mode title screens and prevents unrestricted clients from entering online play. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class TestModeTitleScreenEvents {
    static final String TEST_MODE_LABEL_KEY = "menu.infx.test_mode";
    static final Component TEST_MODE_LABEL = Component.translatable(TEST_MODE_LABEL_KEY)
            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
    private static final Component ONLINE_PLAY_DISABLED = Component.translatable(
            "menu.infx.test_mode.online_disabled");
    private static final Set<String> ONLINE_BUTTONS = Set.of("menu.multiplayer", "menu.online");
    private static final int TEST_MODE_LABEL_HEIGHT = 9;
    private static final int NORMAL_MENU_TOP_OFFSET = 32;
    private static final int LABEL_MENU_GAP = 3;
    private static final int LABEL_Y_BELOW_LOGO = LogoRenderer.DEFAULT_HEIGHT_OFFSET + LogoRenderer.LOGO_HEIGHT + 8;

    private TestModeTitleScreenEvents() {}

    @SubscribeEvent
    private static void configureTitleScreen(ScreenEvent.Init.Post event) {
        if (!InfiniteXTestMode.isEnabled() || !(event.getScreen() instanceof TitleScreen screen)) return;
        disableOnlineButtons(event.getListenersList());
        event.addListener(createTestModeLabel(screen));
    }

    private static StringWidget createTestModeLabel(TitleScreen screen) {
        var font = Minecraft.getInstance().font;
        int width = font.width(TEST_MODE_LABEL);
        return new StringWidget(
                screen.width / 2 - width / 2,
                testModeLabelY(screen.height),
                width,
                TEST_MODE_LABEL_HEIGHT,
                TEST_MODE_LABEL,
                font);
    }

    static int testModeLabelY(int screenHeight) {
        int normalMenuTop = screenHeight / 4 + NORMAL_MENU_TOP_OFFSET;
        int yAboveMenu = normalMenuTop - TEST_MODE_LABEL_HEIGHT - LABEL_MENU_GAP;
        return Math.max(LABEL_Y_BELOW_LOGO, yAboveMenu);
    }

    static void disableOnlineButtons(Iterable<? extends GuiEventListener> listeners) {
        for (GuiEventListener listener : listeners) {
            if (!(listener instanceof Button button)
                    || ONLINE_BUTTONS.stream().noneMatch(key -> hasTranslationKey(button.getMessage(), key))) {
                continue;
            }
            button.active = false;
            button.setTooltip(Tooltip.create(ONLINE_PLAY_DISABLED));
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
