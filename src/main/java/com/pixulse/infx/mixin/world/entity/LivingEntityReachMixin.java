package com.pixulse.infx.mixin.world.entity;

import com.pixulse.infx.item.ItemReach;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.AttackRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adapts vanilla's fixed AttackRange API to the independent INFX player melee attribute. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityReachMixin {
    @Inject(method = "getAttackRangeWith", at = @At("HEAD"), cancellable = true)
    private void getAttackRangeWith(ItemStack weapon, CallbackInfoReturnable<AttackRange> callback) {
        if ((Object) this instanceof Player player) {
            callback.setReturnValue(ItemReach.vanillaAdapter(player));
        }
    }
}
