package com.pixulse.infx.mixin.world.entity.player;

import com.pixulse.infx.InfiniteXTestMode;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * InfX creative players reach 5 blocks for block and entity interaction. Survival reach follows
 * the interaction attributes, so the held tool's reach bonus applies on top of the 2.5 base.
 */
@Mixin(Player.class)
public abstract class PlayerReachMixin {
    @Shadow
    public abstract boolean isCreative();

    @Inject(method = "blockInteractionRange", at = @At("HEAD"), cancellable = true)
    private void blockInteractionRange(CallbackInfoReturnable<Double> callback) {
        if (InfiniteXTestMode.isEnabled() && this.isCreative()) {
            callback.setReturnValue(5.0);
            return;
        }
        callback.setReturnValue(((Player) (Object) this).getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE));
    }

    @Inject(method = "entityInteractionRange", at = @At("HEAD"), cancellable = true)
    private void entityInteractionRange(CallbackInfoReturnable<Double> callback) {
        if (InfiniteXTestMode.isEnabled() && this.isCreative()) {
            callback.setReturnValue(5.0);
            return;
        }
        callback.setReturnValue(((Player) (Object) this).getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE));
    }
}
