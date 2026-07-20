package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

class TestModeTitleScreenEventsTest {
    @Test
    void disablesOnlyTheMultiplayerButton() {
        Button singleplayer = button("menu.singleplayer");
        Button multiplayer = button("menu.multiplayer");
        Button online = button("menu.online");

        TestModeTitleScreenEvents.disableMultiplayerButton(List.of(singleplayer, multiplayer, online));

        assertTrue(singleplayer.active);
        assertFalse(multiplayer.active);
        assertTrue(online.active);
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

    private static Button button(String translationKey) {
        return Button.builder(Component.translatable(translationKey), ignored -> {}).build();
    }
}
