package com.pixulse.infx.entity;

import com.pixulse.infx.harvest.MiteMiningRules;
import com.pixulse.infx.item.R196ArrowItem;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

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
        return isEffectiveMiningTool(stack, Blocks.STONE.defaultBlockState());
    }

    /** Returns whether a held tool is effective against a specific MITE earth-elemental body. */
    public static boolean isEffectiveMiningTool(ItemStack stack, BlockState state) {
        if (stack.isEmpty()) {
            return false;
        }
        var equipment = ModItems.catalog().equipment(stack);
        if (equipment != null) {
            return MiteMiningRules.isEffective(equipment.key(), state);
        }
        return stack.is(ItemTags.PICKAXES)
                && stack.getDestroySpeed(state) > 1.0F
                || stack.getDestroySpeed(state) > 1.0F
                || stack.isCorrectToolForDrops(state);
    }

    /**
     * Magma-cube gate: water/snowball/explosion, or a stone-mining tool in the attacker's hand.
     * MITE blocks every other source, including melee from mobs without an effective tool.
     */
    public static boolean magmaCubeAccepts(DamageSource source) {
        if (source.getDirectEntity() instanceof Snowball
                || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || source.is(DamageTypeTags.IS_DROWNING)
                || source.is(DamageTypeTags.IS_EXPLOSION)) {
            return true;
        }
        if (source.getEntity() instanceof LivingEntity attacker) {
            return isStoneMiningTool(attacker.getMainHandItem());
        }
        return false;
    }

    /**
     * Earth-elemental gate: explosions and falls always land; iron-golem melee is exempt; every
     * other attacker needs a stone-mining tool. MITE grants no free pass to mob melee or hazards.
     */
    public static boolean earthElementalAccepts(R196EarthElemental elemental, DamageSource source) {
        if (elemental.isNormalClay()) {
            return true;
        }
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || source.is(DamageTypeTags.IS_EXPLOSION)
                || source.is(DamageTypeTags.IS_FALL)) {
            return true;
        }
        if (source.getEntity() instanceof net.minecraft.world.entity.animal.golem.IronGolem && source.isDirect()) {
            return true;
        }
        if (source.getEntity() instanceof LivingEntity attacker) {
            ItemStack weapon = resolveWeapon(source);
            return isEffectiveMiningTool(weapon.isEmpty() ? attacker.getMainHandItem() : weapon,
                    elemental.materialBlock().defaultBlockState());
        }
        return false;
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

    /**
     * MITE fire elemental uses the same immunity shape as blaze: water/snowball always hurt;
     * only non-fire enchanted weapons land otherwise.
     */
    public static boolean fireElementalAccepts(ServerLevel level, DamageSource source) {
        return blazeAccepts(level, source);
    }

    /** MITE silver aspect: silver-headed arrows in flight or a silver weapon in the attacker's hand. */
    public static boolean hasSilverAspect(DamageSource source) {
        if (source.getDirectEntity() instanceof AbstractArrow arrow
                && arrow.getPickupItemStackOrigin().getItem() instanceof R196ArrowItem arrowItem) {
            return arrowItem.key().material() == R196Material.SILVER;
        }
        if (source.getEntity() instanceof LivingEntity attacker) {
            var entry = ModItems.catalog().equipment(attacker.getMainHandItem());
            return entry != null
                    && entry.key().material() == R196Material.SILVER
                    && entry.key().type() != R196EquipmentType.ARROW;
        }
        return false;
    }

    /** MITE magic aspect: magic-typed damage, or any enchanted weapon (melee hand or firing bow). */
    public static boolean hasMagicAspect(DamageSource source) {
        if (source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            return true;
        }
        ItemStack weapon = resolveWeapon(source);
        return !weapon.isEmpty() && weapon.isEnchanted();
    }

    /**
     * MITE shadow/nightwing gate: immune to everything except silver, magic and sunlight; the
     * sunlight instant-kill arrives as generic-kill damage, covered by BYPASSES_INVULNERABILITY.
     */
    public static boolean silverMagicGateAccepts(DamageSource source) {
        return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || hasSilverAspect(source)
                || hasMagicAspect(source);
    }

    /** MITE wight gate: immune to everything except fire, lava, silver and magic. */
    public static boolean wightAccepts(DamageSource source) {
        return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || source.is(DamageTypeTags.IS_FIRE)
                || hasSilverAspect(source)
                || hasMagicAspect(source);
    }
}
