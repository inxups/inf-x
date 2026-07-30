package com.pixulse.infx.screen.gui;

import com.pixulse.infx.client.CraftingProgressSmoother;
import com.pixulse.infx.screen.menu.TimedWorkbenchMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public final class TimedWorkbenchScreen extends AbstractContainerScreen<TimedWorkbenchMenu> {
    private static final Identifier BACKGROUND =
            Identifier.withDefaultNamespace("textures/gui/container/crafting_table.png");
    private static final Identifier PROGRESS =
            Identifier.withDefaultNamespace("container/furnace/burn_progress");
    // burn_progress has a one-pixel blank leading column; x=89 aligns its white
    // arrow pixels with the gray arrow in crafting_table.png at x=90.
    private static final int PROGRESS_X = 89;
    private static final int PROGRESS_Y = 35;
    private static final int PROGRESS_WIDTH = 24;
    private static final int PROGRESS_HEIGHT = 16;
    private final CraftingProgressSmoother progressSmoother = new CraftingProgressSmoother();

    public TimedWorkbenchScreen(TimedWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 29;
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                BACKGROUND,
                leftPos,
                topPos,
                0.0F,
                0.0F,
                imageWidth,
                imageHeight,
                256,
                256);
        CraftingProgressSmoother.PixelFill fill = CraftingProgressSmoother.splitPixels(
                progressSmoother.sample(menu), PROGRESS_WIDTH);
        if (fill.fullPixels() > 0) {
            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    PROGRESS,
                    PROGRESS_WIDTH,
                    PROGRESS_HEIGHT,
                    0,
                    0,
                    leftPos + PROGRESS_X,
                    topPos + PROGRESS_Y,
                    fill.fullPixels(),
                    PROGRESS_HEIGHT);
        }
        if (fill.nextPixelAlpha() > 0.0F && fill.fullPixels() < PROGRESS_WIDTH) {
            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    PROGRESS,
                    PROGRESS_WIDTH,
                    PROGRESS_HEIGHT,
                    fill.fullPixels(),
                    0,
                    leftPos + PROGRESS_X + fill.fullPixels(),
                    topPos + PROGRESS_Y,
                    1,
                    PROGRESS_HEIGHT,
                    ARGB.white(fill.nextPixelAlpha()));
        }
    }
}
