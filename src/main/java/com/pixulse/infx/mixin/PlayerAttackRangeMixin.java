package com.pixulse.infx.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.AttackRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Separates R196's 1.5-block empty-hand attack reach from its 2.5-block entity interaction reach. */
@Mixin(LivingEntity.class)
public abstract class PlayerAttackRangeMixin {
    @Inject(method = "getAttackRangeWith", at = @At("HEAD"), cancellable = true)
    private void infx$emptyHandAttackRange(ItemStack stack, CallbackInfoReturnable<AttackRange> callback) {
        if ((Object) this instanceof Player && !stack.has(DataComponents.ATTACK_RANGE)) {
            callback.setReturnValue(new AttackRange(0.0F, 1.5F, 0.0F, 5.0F, 0.0F, 1.0F));
        }
    }
}
