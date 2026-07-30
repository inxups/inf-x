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

        int progressWidth = timedMenu.infx$scaledProgress(16);
        if (progressWidth > 0) {
            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    PROGRESS,
                    24,
                    16,
                    0,
                    0,
                    screen.getLeftPos() + 134,
                    screen.getTopPos() + 28,
                    progressWidth,
                    16);
        }
    }
}
