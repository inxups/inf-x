package com.pixulse.infx.mixin;

import com.pixulse.infx.item.enchantment.EnchantmentRules;
import com.pixulse.infx.item.enchantment.Enchantments;
import com.pixulse.infx.registry.InfXEnchantments;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** InfX applies the highest fire protection level once to every ignition request. */
@Mixin(LivingEntity.class)
abstract class LivingEntityFireProtectionMixin {
    @ModifyVariable(method = "igniteForTicks(I)V", at = @At("HEAD"), argsOnly = true)
    private int applyFireProtection(int ticks) {
        LivingEntity entity = (LivingEntity) (Object) this;
        int level = Enchantments.maxArmorLevel(entity, InfXEnchantments.VANILLA_FIRE_PROTECTION);
        return EnchantmentRules.fireProtectionTicks(ticks, level);
    }
}
