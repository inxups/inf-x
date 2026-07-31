package com.pixulse.infx.mixin;

import com.pixulse.infx.recipe.TimedCraftingMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds server-synchronized timed-crafting experience costs to result tooltips. */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Shadow
    protected Slot hoveredSlot;

    @Inject(method = "getTooltipFromContainerItem", at = @At("RETURN"), cancellable = true)
    private void infx$appendCraftingExperience(
            ItemStack itemStack, CallbackInfoReturnable<List<Component>> callback) {
        if (hoveredSlot == null || !(hoveredSlot.container instanceof net.minecraft.world.inventory.ResultContainer)) {
            return;
        }
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        if (!(screen.getMenu() instanceof TimedCraftingMenu timedMenu)
                || hoveredSlot.container != timedMenu.infx$resultContainer()) {
            return;
        }
        int cost = timedMenu.infx$experienceCost();
        if (cost <= 0) {
            return;
        }
        List<Component> tooltip = new ArrayList<>(callback.getReturnValue());
        tooltip.add(Component.translatable("tooltip.infx.crafting_experience", cost)
                .withStyle(ChatFormatting.YELLOW));
        callback.setReturnValue(tooltip);
    }
}
