package com.pixulse.infx.mixin;

import com.pixulse.infx.event.FireCookingEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies InfX's damage-based dropped-food cooking immediately before vanilla subtracts item health.
 * NeoForge's public invulnerability event exposes the damage source but not its amount, so it cannot
 * reproduce InfX's per-hit progress calculation without this narrow injection.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityFireCookingMixin {
    @Inject(
            method = "hurtServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/item/ItemEntity;markHurt()V"),
            cancellable = true)
    private void handleOpenFireCooking(
            ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> callback) {
        ItemEntity entity = (ItemEntity) (Object) this;
        if (FireCookingEvents.handleFireDamage(level, entity, source, damage)) {
            callback.setReturnValue(true);
        }
    }
}
