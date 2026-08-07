package com.pixulse.infx.mixin;

import com.pixulse.infx.item.InfxCarrotOnAStickItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla 26.1 pigs only accept the exact {@code minecraft:carrot_on_a_stick} as their control
 * item. A rider holding an InfX carrot on a stick is therefore not the controlling passenger:
 * the pig ignores steering input and {@code InfxCarrotOnAStickItem#use} never reaches its boost.
 * Widen the check to every InfX stick so riding works like with the vanilla item.
 */
@Mixin(Pig.class)
public abstract class PigCarrotControlMixin {
    @Inject(method = "getControllingPassenger", at = @At("HEAD"), cancellable = true)
    private void infx$acceptInfxCarrotOnAStick(CallbackInfoReturnable<LivingEntity> cir) {
        Pig pig = (Pig) (Object) this;
        if (pig.isSaddled()
                && pig.getFirstPassenger() instanceof Player player
                && player.isHolding(stack -> stack.getItem() instanceof InfxCarrotOnAStickItem)) {
            cir.setReturnValue(player);
        }
    }
}
