package com.pixulse.infx.client;

import com.pixulse.infx.menu.TimedWorkbenchMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class TimedWorkbenchScreen extends AbstractContainerScreen<TimedWorkbenchMenu> {
    private static final Identifier BACKGROUND =
            Identifier.withDefaultNamespace("textures/gui/container/crafting_table.png");
    private static final Identifier PROGRESS =
            Identifier.withDefaultNamespace("container/furnace/burn_progress");

    public TimedWorkbenchScreen(TimedWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 29;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
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
                    leftPos + 90,
                    topPos + 35,
                    progressWidth,
                    16);
        }
    }
}
