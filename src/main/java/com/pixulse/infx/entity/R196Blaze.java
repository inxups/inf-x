package com.pixulse.infx.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * Blaze replacement with R196's six-point melee damage and MITE vulnerability rules.
 *
 * <p>Water/snowballs always hurt. Ordinary hits require a non-fire enchanted weapon (or
 * non-flame enchanted launcher for arrows). Fire-aspect / flame weapons are treated as fire
 * and do not land. Attacker ignition from the blaze itself must not cancel valid hits.
 */
public final class R196Blaze extends Blaze implements R196Mob {
    public R196Blaze(EntityType<? extends Blaze> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Blaze.createAttributes()
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.70)
                .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (isVulnerableTo(level, source)) {
            return super.hurtServer(level, source, damage);
        }
        return false;
    }

    /** Pure vulnerability gate used by combat and GameTests. */
    static boolean isVulnerableTo(ServerLevel level, DamageSource source) {
        if (source.getDirectEntity() instanceof Snowball
                || source.getEntity() == null
                || source.is(DamageTypeTags.BYPASSES_ARMOR)
                || source.is(DamageTypeTags.IS_DROWNING)) {
            return true;
        }
        if (source.is(DamageTypeTags.IS_FIRE)) {
            return false;
        }
        ItemStack weapon = resolveWeapon(source);
        if (weapon.isEmpty() || !weapon.isEnchanted() || hasFireEnchantment(level, weapon)) {
            return false;
        }
        return true;
    }

    private static ItemStack resolveWeapon(DamageSource source) {
        ItemStack weapon = source.getWeaponItem();
        if (weapon != null && !weapon.isEmpty()) {
            return weapon;
        }
        if (source.getEntity() instanceof LivingEntity living) {
            return living.getMainHandItem();
        }
        return ItemStack.EMPTY;
    }

    private static boolean hasFireEnchantment(ServerLevel level, ItemStack weapon) {
        var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return weapon.getEnchantmentLevel(enchantments.getOrThrow(Enchantments.FIRE_ASPECT)) > 0
                || weapon.getEnchantmentLevel(enchantments.getOrThrow(Enchantments.FLAME)) > 0;
    }
}
