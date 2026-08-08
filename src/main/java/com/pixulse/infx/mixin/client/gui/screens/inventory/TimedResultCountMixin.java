package com.pixulse.infx.mixin.client.gui.screens.inventory;

import com.pixulse.infx.recipe.TimedCraftingMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Shows a logical timed-crafting output count without placing an oversized
 * stack in the result container or sending one over the network.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class TimedResultCountMixin {
    @Unique
    private @Nullable Slot infx$renderedSlot;

    @Inject(method = "renderSlotContents", at = @At("HEAD"))
    private void infx$rememberRenderedSlot(
            GuiGraphicsExtractor graphics,
            ItemStack itemStack,
            Slot slot,
            @Nullable String itemCount,
            CallbackInfo callback) {
        infx$renderedSlot = slot;
    }

    @Inject(method = "renderSlotContents", at = @At("TAIL"))
    private void infx$clearRenderedSlot(
            GuiGraphicsExtractor graphics,
            ItemStack itemStack,
            Slot slot,
            @Nullable String itemCount,
            CallbackInfo callback) {
        infx$renderedSlot = null;
    }

    @ModifyArg(
            method = "renderSlotContents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"),
            index = 4)
    private @Nullable String infx$logicalResultCount(@Nullable String itemCount) {
        Slot slot = infx$renderedSlot;
        if (slot == null || !(((MenuAccess<?>) (Object) this).getMenu() instanceof TimedCraftingMenu timedMenu)) {
            return itemCount;
        }
        if (slot.container != timedMenu.infx$resultContainer()) {
            return itemCount;
        }
        int logicalCount = timedMenu.infx$logicalResultCount();
        return logicalCount > 1 ? Integer.toString(logicalCount) : itemCount;
    }
}
