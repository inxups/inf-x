package com.pixulse.infx.mixin;

import com.pixulse.infx.item.MiteBucketItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Enforces MITE's player-specific pickup grace after a held R196 bucket melts. */
@Mixin(ItemEntity.class)
public abstract class ItemEntityPickupMixin {
    // ItemEntity's public pickup delay applies to every player, so the per-player MITE rule has no
    // public extension point and must be checked at the vanilla player-touch boundary.
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void infx$blockPickupAfterBucketMelt(Player player, CallbackInfo callback) {
        if (!player.level().isClientSide() && MiteBucketItem.isMeltPickupBlocked(player)) {
            callback.cancel();
        }
    }
}
