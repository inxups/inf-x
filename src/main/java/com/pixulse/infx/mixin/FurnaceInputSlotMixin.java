package com.pixulse.infx.mixin;

import com.pixulse.infx.furnace.FurnaceItemPolicy;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class FurnaceInputSlotMixin {
    @Inject(method = "mayPlace", at = @At("RETURN"), cancellable = true)
    private void infx$rejectLargeClayFurnaceInput(
            ItemStack stack, CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValue()) {
            return;
        }
        Slot slot = (Slot) (Object) this;
        if (slot.getContainerSlot() == 0
                && slot.container instanceof AbstractFurnaceBlockEntity furnace
                && !FurnaceItemPolicy.canPlaceItem(furnace.getBlockState(), stack)) {
            callback.setReturnValue(false);
        }
    }
}
