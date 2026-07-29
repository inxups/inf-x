package com.pixulse.infx.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** Client presentation helpers for InfiniteX's negative experience debt levels. */
public final class ExperienceHud {
    public static final int DEBT_LEVEL_COLOR = 0xFFFF5555;
    private static final int OUTLINE_COLOR = 0xFF000000;

    private ExperienceHud() {}

    public static boolean isDebtLevel(int level) {
        return level < 0;
    }

    /** Mirrors vanilla's experience-level placement and outline with a red debt foreground. */
    public static void extractDebtLevel(GuiGraphicsExtractor graphics, Font font, int level) {
        Component text = Component.translatable("gui.experience.level", level);
        int x = (graphics.guiWidth() - font.width(text)) / 2;
        int y = graphics.guiHeight() - 24 - 9 - 2;
        graphics.text(font, text, x + 1, y, OUTLINE_COLOR, false);
        graphics.text(font, text, x - 1, y, OUTLINE_COLOR, false);
        graphics.text(font, text, x, y + 1, OUTLINE_COLOR, false);
        graphics.text(font, text, x, y - 1, OUTLINE_COLOR, false);
        graphics.text(font, text, x, y, DEBT_LEVEL_COLOR, false);
    }
}
