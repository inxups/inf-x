package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.event.client.DevModeTitleScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

class DevModeTitleScreenEventsTest {
    @Test
    void titleLabelSitsBelowTheLogoAndAboveTheMenu() {
        int screenHeight = 270;
        int labelY = DevModeTitleScreenEvents.devModeLabelY(screenHeight);

        assertEquals(87, labelY);
        assertTrue(labelY > 81);
        assertTrue(labelY + 9 < screenHeight / 4 + 32);
    }

    @Test
    void titleLabelClearlyMarksDevMode() {
        TranslatableContents contents = (TranslatableContents) DevModeTitleScreenEvents.DEV_MODE_LABEL.getContents();
        assertEquals(DevModeTitleScreenEvents.DEV_MODE_LABEL_KEY, contents.getKey());
        assertTrue(DevModeTitleScreenEvents.DEV_MODE_LABEL.getStyle().isBold());
        assertEquals(
                TextColor.fromLegacyFormat(ChatFormatting.RED),
                DevModeTitleScreenEvents.DEV_MODE_LABEL.getStyle().getColor());
    }
}
