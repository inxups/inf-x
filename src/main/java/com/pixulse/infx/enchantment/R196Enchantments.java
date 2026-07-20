package com.pixulse.infx.enchantment;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

public final class R196Enchantments {
    private R196Enchantments() {}

    public static int level(Level level, ItemStack stack, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key)
                .map(stack::getEnchantmentLevel)
                .orElse(0);
    }

    public static int armorLevel(LivingEntity entity, ResourceKey<Enchantment> key) {
        int result = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            result += level(entity.level(), entity.getItemBySlot(slot), key);
        }
        return result;
    }

    public static int maxArmorLevel(LivingEntity entity, ResourceKey<Enchantment> key) {
        int result = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            result = Math.max(result, level(entity.level(), entity.getItemBySlot(slot), key));
        }
        return result;
    }
}
