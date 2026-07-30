package com.pixulse.infx.mixin;

import com.pixulse.infx.recipe.TimedCraftingMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the timed hand-crafting progress to the vanilla inventory screen. */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    private static final int PROGRESS_WIDTH = 18;
    private static final int PROGRESS_HEIGHT = 16;
    private static final int PROGRESS_X = 134;
    private static final int PROGRESS_Y = 28;
    private static final int EMPTY_ARROW_SOURCE_Y = 2;
    private static final Identifier INVENTORY_BACKGROUND =
            Identifier.withDefaultNamespace("textures/gui/container/inventory.png");
    private static final Identifier PROGRESS =
            Identifier.withDefaultNamespace("container/furnace/burn_progress");

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

        int progressWidth = timedMenu.infx$scaledProgress(PROGRESS_WIDTH);
        if (progressWidth > 0) {
            int x = screen.getLeftPos() + PROGRESS_X;
            int y = screen.getTopPos() + PROGRESS_Y;
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    INVENTORY_BACKGROUND,
                    x,
                    y,
                    PROGRESS_X,
                    EMPTY_ARROW_SOURCE_Y,
                    PROGRESS_WIDTH,
                    PROGRESS_HEIGHT,
                    256,
                    256);
            graphics.enableScissor(x, y, x + progressWidth, y + PROGRESS_HEIGHT);
            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    PROGRESS,
                    x,
                    y,
                    PROGRESS_WIDTH,
                    PROGRESS_HEIGHT);
            graphics.disableScissor();
        }
    }
}
