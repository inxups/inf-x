package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.event.client.TestModeTitleScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

class TestModeTitleScreenEventsTest {
    @Test
    void titleLabelSitsBelowTheLogoAndAboveTheMenu() {
        int screenHeight = 270;
        int labelY = TestModeTitleScreenEvents.testModeLabelY(screenHeight);

        assertEquals(87, labelY);
        assertTrue(labelY > 81);
        assertTrue(labelY + 9 < screenHeight / 4 + 32);
    }

    @Test
    void titleLabelClearlyMarksTestMode() {
        TranslatableContents contents = (TranslatableContents) TestModeTitleScreenEvents.TEST_MODE_LABEL.getContents();
        assertEquals(TestModeTitleScreenEvents.TEST_MODE_LABEL_KEY, contents.getKey());
        assertTrue(TestModeTitleScreenEvents.TEST_MODE_LABEL.getStyle().isBold());
        assertEquals(
                TextColor.fromLegacyFormat(ChatFormatting.RED),
                TestModeTitleScreenEvents.TEST_MODE_LABEL.getStyle().getColor());
    }
}
