package com.pixulse.infx.mixin;

import com.pixulse.infx.InfiniteXTestMode;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** InfX creative players reach 5 blocks for block and entity interaction. */
@Mixin(Player.class)
public abstract class PlayerReachMixin {
    @Inject(method = "blockInteractionRange", at = @At("HEAD"), cancellable = true)
    private void blockInteractionRange(CallbackInfoReturnable<Double> callback) {
        if (!InfiniteXTestMode.isEnabled() && ((Player) (Object) this).getAbilities().instabuild) {
            callback.setReturnValue(5.0);
        }
    }

    @Inject(method = "entityInteractionRange", at = @At("HEAD"), cancellable = true)
    private void entityInteractionRange(CallbackInfoReturnable<Double> callback) {
        if (!InfiniteXTestMode.isEnabled() && ((Player) (Object) this).getAbilities().instabuild) {
            callback.setReturnValue(5.0);
        }
    }
}
