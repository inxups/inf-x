package com.pixulse.infx.mixin;

import com.pixulse.infx.data.food.FoodIngestion;
import com.pixulse.infx.data.food.FoodProfiles;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Consumable#canConsume is the smallest modern hook with both the player and the item stack.
 * Player#canEat alone cannot reproduce R196's food-specific nutrient-deficit exception.
 */
@Mixin(Consumable.class)
abstract class ConsumableMixin {
    @Inject(method = "canConsume", at = @At("HEAD"), cancellable = true)
    private void infx$useR196FoodGate(
            LivingEntity entity, ItemStack stack, CallbackInfoReturnable<Boolean> callback) {
        if (!(entity instanceof Player player) || FoodProfiles.forStack(stack).isEmpty()) return;
        callback.setReturnValue(FoodIngestion.canIngest(player, stack));
    }
}
