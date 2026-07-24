package com.pixulse.infx.entity;

import com.pixulse.infx.harvest.MiteMiningRules;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;

/** Shared R196/MITE combat vulnerability helpers for special mobs. */
public final class R196MobDamageRules {
    private R196MobDamageRules() {}

    public static ItemStack resolveWeapon(DamageSource source) {
        ItemStack weapon = source.getWeaponItem();
        if (weapon != null && !weapon.isEmpty()) {
            return weapon;
        }
        if (source.getEntity() instanceof LivingEntity living) {
            return living.getMainHandItem();
        }
        return ItemStack.EMPTY;
    }

    public static boolean hasFireEnchantment(ServerLevel level, ItemStack weapon) {
        if (weapon.isEmpty()) {
            return false;
        }
        var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return weapon.getEnchantmentLevel(enchantments.getOrThrow(Enchantments.FIRE_ASPECT)) > 0
                || weapon.getEnchantmentLevel(enchantments.getOrThrow(Enchantments.FLAME)) > 0;
    }

    /**
     * MITE magma cubes / earth elementals accept tools that are effective against stone
     * (pickaxes and war hammers), not only the vanilla pickaxe item tag.
     */
    public static boolean isStoneMiningTool(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.is(ItemTags.PICKAXES)) {
            return true;
        }
        var equipment = ModItems.catalog().equipment(stack);
        if (equipment != null) {
            R196EquipmentType type = equipment.key().type();
            if (type == R196EquipmentType.PICKAXE || type == R196EquipmentType.WAR_HAMMER) {
                return true;
            }
            return MiteMiningRules.isEffective(equipment.key(), Blocks.STONE.defaultBlockState());
        }
        return stack.isCorrectToolForDrops(Blocks.STONE.defaultBlockState());
    }

    /** Magma-cube gate: water/snowball/explosion, or a stone-mining tool in the attacker's hand. */
    public static boolean magmaCubeAccepts(DamageSource source) {
        if (source.getDirectEntity() instanceof Snowball
                || source.is(DamageTypeTags.IS_DROWNING)
                || source.is(DamageTypeTags.IS_EXPLOSION)
                || source.getEntity() == null) {
            return true;
        }
        if (!(source.getEntity() instanceof Player player)) {
            return true;
        }
        return isStoneMiningTool(player.getMainHandItem());
    }

    /** Earth-elemental gate: explosions always land; players need a stone-mining tool. */
    public static boolean earthElementalAccepts(DamageSource source) {
        if (source.is(DamageTypeTags.IS_EXPLOSION) || source.getEntity() == null) {
            return true;
        }
        if (!(source.getEntity() instanceof Player player)) {
            return true;
        }
        return isStoneMiningTool(player.getMainHandItem());
    }

    /**
     * MITE blaze vulnerability: snowball/water/bypass always hurt; fire damage and fire-aspect /
     * flame weapons never do; other enchanted weapons do. Attacker ignition is ignored.
     */
    public static boolean blazeAccepts(ServerLevel level, DamageSource source) {
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
        return !weapon.isEmpty() && weapon.isEnchanted() && !hasFireEnchantment(level, weapon);
    }
}
