package com.pixulse.infx.mixin.world.item;

import com.pixulse.infx.item.enchantment.EnchantmentRules;
import com.pixulse.infx.item.enchantment.Enchantments;
import com.pixulse.infx.registry.InfXEnchantments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * InfX precision and recovery enchantments also affect crossbows. The INFX bows apply the
 * same two effects in their own shoot override; this mirrors them for the vanilla crossbow,
 * which has no public extension point.
 */
@Mixin(CrossbowItem.class)
public abstract class CrossbowItemProjectileMixin {
    @Redirect(
            method = "shootProjectile",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;shoot(DDDFF)V"))
    private void infx$applyPrecisionAndRecovery(
            Projectile projectile, double x, double y, double z, float power, float uncertainty) {
        int precision = 0;
        int recovery = 0;
        int experienceLevel = 0;
        if (projectile.getOwner() instanceof LivingEntity shooter) {
            ItemStack crossbow = shooter.getMainHandItem();
            Level level = shooter.level();
            precision = Enchantments.level(level, crossbow, InfXEnchantments.PRECISION);
            recovery = Enchantments.level(level, crossbow, InfXEnchantments.RECOVERY);
            experienceLevel = shooter instanceof Player player ? player.experienceLevel : 0;
        }
        if (recovery > 0) {
            projectile.getPersistentData().putInt("infx_recovery_enchantment", recovery);
        }
        projectile.shoot(
                x, y, z, power, uncertainty * EnchantmentRules.precisionUncertaintyMultiplier(experienceLevel, precision));
    }
}
