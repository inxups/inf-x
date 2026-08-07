package com.pixulse.infx.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** InfX flint and steel has 16 durability instead of the modern 64. */
@Mixin(IItemExtension.class)
public interface FlintAndSteelDurabilityMixin {
    @Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
    private void infx$flintAndSteelMaxDamage(ItemStack stack, CallbackInfoReturnable<Integer> callback) {
        if (stack.is(Items.FLINT_AND_STEEL)) {
            callback.setReturnValue(16);
        }
    }
}
