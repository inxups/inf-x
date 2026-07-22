package com.pixulse.infx.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/** Blaze replacement with R196's six-point melee damage. */
public final class R196Blaze extends Blaze implements R196Mob {
    public R196Blaze(EntityType<? extends Blaze> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Blaze.createAttributes()
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.getDirectEntity() instanceof Snowball
                || source.getEntity() == null
                || source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            return super.hurtServer(level, source, damage);
        }
        ItemStack weapon = source.getWeaponItem();
        var directEntity = source.getDirectEntity();
        if (weapon == null
                || !weapon.isEnchanted()
                || source.is(DamageTypeTags.IS_FIRE)
                || (directEntity != null && directEntity.isOnFire())
                || hasFireEnchantment(level, weapon)) {
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    private static boolean hasFireEnchantment(ServerLevel level, ItemStack weapon) {
        var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return weapon.getEnchantmentLevel(enchantments.getOrThrow(Enchantments.FIRE_ASPECT)) > 0
                || weapon.getEnchantmentLevel(enchantments.getOrThrow(Enchantments.FLAME)) > 0;
    }
}
