package com.pixulse.infx.mixin;

import com.pixulse.infx.crafting.TimedCraftingMenu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void infx$interceptTimedResultClick(
            int slotIndex,
            int buttonNum,
            ContainerInput input,
            Player player,
            CallbackInfo callback) {
        if ((Object) this instanceof TimedCraftingMenu timedMenu
                && timedMenu.infx$hasTimedResult()
                && slotIndex == timedMenu.infx$resultSlotIndex()) {
            if (buttonNum == 0
                    && (input == ContainerInput.PICKUP || input == ContainerInput.QUICK_MOVE)) {
                timedMenu.infx$startTimedCrafting(player);
            } else if (buttonNum == 1 && input == ContainerInput.PICKUP) {
                timedMenu.infx$cycleResult(player);
            }
            callback.cancel();
        }
    }
}
