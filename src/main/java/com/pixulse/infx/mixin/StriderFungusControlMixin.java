package com.pixulse.infx.mixin;

import com.pixulse.infx.item.InfxWarpedFungusOnAStickItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla 26.1 striders only accept the exact {@code minecraft:warped_fungus_on_a_stick} as
 * their control item, so riders holding an InfX warped-fungus stick cannot steer or boost.
 * Widen the check to every InfX stick, mirroring {@link PigCarrotControlMixin}.
 */
@Mixin(Strider.class)
public abstract class StriderFungusControlMixin {
    @Inject(method = "getControllingPassenger", at = @At("HEAD"), cancellable = true)
    private void infx$acceptInfxFungusOnAStick(CallbackInfoReturnable<LivingEntity> cir) {
        Strider strider = (Strider) (Object) this;
        if (strider.isSaddled()
                && strider.getFirstPassenger() instanceof Player player
                && player.isHolding(stack -> stack.getItem() instanceof InfxWarpedFungusOnAStickItem)) {
            cir.setReturnValue(player);
        }
    }
}
