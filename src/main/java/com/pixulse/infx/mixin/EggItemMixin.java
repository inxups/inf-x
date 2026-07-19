package com.pixulse.infx.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import com.pixulse.infx.network.R196Network;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** EggItem hard-codes throwing; R196 gives eating priority while food is needed. */
@Mixin(EggItem.class)
abstract class EggItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void infx$eatBeforeThrowing(
            Level level,
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> callback) {
        if (player.getPersistentData().getBooleanOr(R196Network.FORCE_EGG_THROW, false)
                || !player.canEat(false)) return;
        ItemStack egg = player.getItemInHand(hand);
        Consumable consumable = egg.get(DataComponents.CONSUMABLE);
        if (consumable != null) callback.setReturnValue(consumable.startConsuming(player, egg, hand));
    }
}
