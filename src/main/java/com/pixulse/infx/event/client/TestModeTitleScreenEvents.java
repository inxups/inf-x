package com.pixulse.infx.event.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.InfiniteXTestMode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** Marks test-mode title screens while allowing compatible clients to enter online play. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class TestModeTitleScreenEvents {
    public static final String TEST_MODE_LABEL_KEY = "menu.infx.test_mode";
    public static final Component TEST_MODE_LABEL = Component.translatable(TEST_MODE_LABEL_KEY)
            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
    private static final int TEST_MODE_LABEL_HEIGHT = 9;
    private static final int NORMAL_MENU_TOP_OFFSET = 32;
    private static final int LABEL_MENU_GAP = 3;
    private static final int LABEL_Y_BELOW_LOGO = LogoRenderer.DEFAULT_HEIGHT_OFFSET + LogoRenderer.LOGO_HEIGHT + 8;

    private TestModeTitleScreenEvents() {}

    @SubscribeEvent
    public static void configureTitleScreen(ScreenEvent.Init.Post event) {
        if (!InfiniteXTestMode.isEnabled() || !(event.getScreen() instanceof TitleScreen screen)) return;
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

    public static int testModeLabelY(int screenHeight) {
        int normalMenuTop = screenHeight / 4 + NORMAL_MENU_TOP_OFFSET;
        int yAboveMenu = normalMenuTop - TEST_MODE_LABEL_HEIGHT - LABEL_MENU_GAP;
        return Math.max(LABEL_Y_BELOW_LOGO, yAboveMenu);
    }
}
