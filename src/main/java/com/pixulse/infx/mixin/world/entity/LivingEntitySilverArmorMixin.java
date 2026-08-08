package com.pixulse.infx.mixin.world.entity;

import com.pixulse.infx.item.Catalog;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** InfX silver armor shortens harmful effect durations by 15% per worn piece. */
@Mixin(LivingEntity.class)
public abstract class LivingEntitySilverArmorMixin {
    @ModifyVariable(
            method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"),
            argsOnly = true)
    private MobEffectInstance infx$shortenHarmfulEffectDuration(MobEffectInstance effect) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof Player player)
                || effect == null
                || effect.isInfiniteDuration()
                || effect.getEffect().value().isBeneficial()) {
            return effect;
        }
        int silverPieces = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            Catalog.EquipmentEntry entry = InfXItems.catalog().equipment(player.getItemBySlot(slot));
            if (entry != null && entry.key().material() == InfxMaterial.SILVER) {
                silverPieces++;
            }
        }
        if (silverPieces == 0) {
            return effect;
        }
        float scale = Math.max(0.4F, 1.0F - silverPieces * 0.15F);
        return effect.withScaledDuration(scale);
    }
}
