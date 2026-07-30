package com.pixulse.infx.mixin;

import com.pixulse.infx.client.CraftingProgressSmoother;
import com.pixulse.infx.recipe.TimedCraftingMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the timed hand-crafting progress to the vanilla inventory screen. */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    private static final int PROGRESS_WIDTH = 16;
    private static final int PROGRESS_HEIGHT = 13;
    private static final int PROGRESS_X = 135;
    private static final int PROGRESS_Y = 29;
    private static final int ARROW_HEAD_X = 9;
    private static final int ARROW_COLOR = 0xFFFFFFFF;
    private static final int[] ARROW_ROW_WIDTHS = {1, 2, 3, 4, 5, 15, 16, 15, 5, 4, 3, 2, 1};
    @Unique
    private CraftingProgressSmoother infx$progressSmoother;

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void infx$renderCraftingProgress(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo callback) {
        InventoryScreen screen = (InventoryScreen) (Object) this;
        if (!(screen.getMenu() instanceof TimedCraftingMenu timedMenu)) {
            return;
        }

        if (infx$progressSmoother == null) {
            infx$progressSmoother = new CraftingProgressSmoother();
        }
        CraftingProgressSmoother.PixelFill fill = CraftingProgressSmoother.splitPixels(
                infx$progressSmoother.sample(timedMenu), PROGRESS_WIDTH);
        int x = screen.getLeftPos() + PROGRESS_X;
        int y = screen.getTopPos() + PROGRESS_Y;
        if (fill.fullPixels() > 0) {
            graphics.enableScissor(x, y, x + fill.fullPixels(), y + PROGRESS_HEIGHT);
            for (int row = 0; row < ARROW_ROW_WIDTHS.length; row++) {
                int rowX = row >= 5 && row <= 7 ? x : x + ARROW_HEAD_X;
                graphics.fill(rowX, y + row, rowX + ARROW_ROW_WIDTHS[row], y + row + 1, ARROW_COLOR);
            }
            graphics.disableScissor();
        }
        if (fill.nextPixelAlpha() > 0.0F && fill.fullPixels() < PROGRESS_WIDTH) {
            int column = fill.fullPixels();
            int color = ARGB.white(fill.nextPixelAlpha());
            for (int row = 0; row < ARROW_ROW_WIDTHS.length; row++) {
                int rowStart = row >= 5 && row <= 7 ? 0 : ARROW_HEAD_X;
                if (column >= rowStart && column < rowStart + ARROW_ROW_WIDTHS[row]) {
                    graphics.fill(x + column, y + row, x + column + 1, y + row + 1, color);
                }
            }
        }
    }
}
