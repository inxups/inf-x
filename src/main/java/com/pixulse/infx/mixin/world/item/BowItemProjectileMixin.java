package com.pixulse.infx.mixin.world.item;

import com.pixulse.infx.item.enchantment.EnchantmentRules;
import com.pixulse.infx.item.enchantment.Enchantments;
import com.pixulse.infx.registry.InfXEnchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Applies InfX Precision, Recovery and Poisoning to projectiles fired from any bow, including
 * the vanilla bow. {@link com.pixulse.infx.item.InfxBowItem} only adds its material velocity
 * multiplier in its own override and delegates here for the enchantment effects, so the logic
 * lives in one place. The bow is read from the shooter's actively-used item, which correctly
 * reflects the off hand when an offhand bow is fired.
 */
@Mixin(BowItem.class)
abstract class BowItemProjectileMixin {
    @Redirect(
            method = "shootProjectile",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/Projectile;shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V"))
    private void infx$applyBowEnchantments(
            Projectile projectile, Entity source, float xRot, float yRot,
            float yOffset, float power, float uncertainty) {
        Level level = source.level();
        ItemStack bow = usedBow(source);
        int precision = Enchantments.level(level, bow, InfXEnchantments.PRECISION);
        int recovery = Enchantments.level(level, bow, InfXEnchantments.RECOVERY);
        int poisoning = Enchantments.level(level, bow, InfXEnchantments.POISONING);
        int experienceLevel = source instanceof Player player ? player.experienceLevel : 0;
        if (recovery > 0) {
            projectile.getPersistentData().putInt("infx_recovery_enchantment", recovery);
        }
        if (poisoning > 0) {
            projectile.getPersistentData().putInt("infx_poisoning_enchantment", poisoning);
        }
        projectile.shootFromRotation(source, xRot, yRot, yOffset, power,
                uncertainty * EnchantmentRules.precisionUncertaintyMultiplier(experienceLevel, precision));
    }

    private static ItemStack usedBow(Entity source) {
        if (source instanceof LivingEntity living) {
            ItemStack used = living.getUseItem();
            if (!used.isEmpty() && used.getItem() instanceof BowItem) {
                return used;
            }
            return living.getMainHandItem();
        }
        return ItemStack.EMPTY;
    }
}
