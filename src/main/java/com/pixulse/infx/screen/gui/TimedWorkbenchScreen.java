package com.pixulse.infx.screen.gui;

import com.pixulse.infx.screen.menu.TimedWorkbenchMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
        int progressWidth = menu.infx$scaledProgress(24);
        if (progressWidth > 0) {
            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    PROGRESS,
                    24,
                    16,
                    0,
                    0,
                    leftPos + PROGRESS_X,
                    topPos + PROGRESS_Y,
                    progressWidth,
                    16);
        }
    }
}
