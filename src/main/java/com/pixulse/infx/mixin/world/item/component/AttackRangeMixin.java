package com.pixulse.infx.mixin.world.item.component;

import com.pixulse.infx.item.ItemReach;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The vanilla AttackRange value is only an API adapter for players; INFX owns the actual rule.
 */
@Mixin(AttackRange.class)
public abstract class AttackRangeMixin {
    @Inject(method = "isInRange(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/Vec3;)Z", at = @At("HEAD"), cancellable = true)
    private void isInRange(
            LivingEntity attacker, Vec3 location, CallbackInfoReturnable<Boolean> callback) {
        if (attacker instanceof Player player) {
            callback.setReturnValue(ItemReach.isWithinMeleeRange(player, location));
        }
    }

    @Inject(method = "isInRange(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/AABB;D)Z", at = @At("HEAD"), cancellable = true)
    private void isInRange(
            LivingEntity attacker, AABB boundingBox, double extraBuffer, CallbackInfoReturnable<Boolean> callback) {
        if (attacker instanceof Player player) {
            callback.setReturnValue(ItemReach.isWithinMeleeRange(player, boundingBox, extraBuffer));
        }
    }
}
