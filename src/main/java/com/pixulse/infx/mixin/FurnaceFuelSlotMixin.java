package com.pixulse.infx.mixin;

import com.pixulse.infx.furnace.FurnaceHeatPolicy;
import com.pixulse.infx.furnace.FurnaceItemPolicy;
import net.minecraft.world.inventory.FurnaceFuelSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FurnaceFuelSlot.class)
public abstract class FurnaceFuelSlotMixin {
    @Inject(method = "mayPlace", at = @At("RETURN"), cancellable = true)
    private void infx$enforceFurnaceFuelPolicy(
            ItemStack stack, CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValue()) {
            return;
        }
        Slot slot = (Slot) (Object) this;
        if (!(slot.container instanceof AbstractFurnaceBlockEntity furnace)) {
            return;
        }
        if (!FurnaceItemPolicy.canPlaceItem(furnace.getBlockState(), stack)) {
            callback.setReturnValue(false);
            return;
        }
        int maximumHeat = FurnaceHeatPolicy.maximumHeat(furnace.getBlockState());
        if (maximumHeat == 0 || stack.is(Items.BUCKET) || furnace.getLevel() == null) {
            return;
        }
        int burnTime = stack.getBurnTime(RecipeType.SMELTING, furnace.getLevel().fuelValues());
        if (FurnaceHeatPolicy.fuelHeat(stack, burnTime) > maximumHeat) {
            callback.setReturnValue(false);
        }
    }
}
