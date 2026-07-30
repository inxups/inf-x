package com.pixulse.infx.item.enchantment;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

public final class Enchantments {
    private Enchantments() {}

    public static int level(Level level, ItemStack stack, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key)
                .map(stack::getEnchantmentLevel)
                .orElse(0);
    }

    /** INFX reads every armor enchantment as the highest equipped level, never as a sum. */
    public static int maxArmorLevel(LivingEntity entity, ResourceKey<Enchantment> key) {
        int result = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            result = Math.max(result, level(entity.level(), entity.getItemBySlot(slot), key));
        }
        return result;
    }
}
