package com.pixulse.infx.mixin.world.entity.player;

import com.pixulse.infx.item.ItemReach;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Routes every player interaction and ordinary melee check through INFX-owned attributes.
 */
@Mixin(Player.class)
public abstract class PlayerReachMixin {
    @Inject(method = "blockInteractionRange", at = @At("HEAD"), cancellable = true)
    private void blockInteractionRange(CallbackInfoReturnable<Double> callback) {
        callback.setReturnValue(ItemReach.interactionRange((Player) (Object) this));
    }

    @Inject(method = "entityInteractionRange", at = @At("HEAD"), cancellable = true)
    private void entityInteractionRange(CallbackInfoReturnable<Double> callback) {
        callback.setReturnValue(ItemReach.interactionRange((Player) (Object) this));
    }

    @Inject(method = "isWithinAttackRange", at = @At("HEAD"), cancellable = true)
    private void isWithinAttackRange(
            ItemStack weapon, AABB bounds, double extraBuffer, CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(
                ItemReach.isWithinMeleeRange((Player) (Object) this, bounds, extraBuffer));
    }

    @Redirect(
            method = "doSweepAttack(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;FLnet/minecraft/world/phys/AABB;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;entityInteractionRange()D"))
    private double sweepAttackRange(Player player) {
        return ItemReach.meleeRange(player);
    }
}
