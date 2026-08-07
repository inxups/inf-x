package com.pixulse.infx.entity;

import com.pixulse.infx.data.harvest.InfxMiningRules;
import com.pixulse.infx.item.InfxArrowItem;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Shared INFX combat vulnerability helpers for special mobs. */
public final class MobDamageRules {
    private MobDamageRules() {}

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
     * InfX magma cubes / earth elementals accept tools that are effective against stone
     * (pickaxes and war hammers), not only the vanilla pickaxe item tag.
     */
    public static boolean isStoneMiningTool(ItemStack stack) {
        return isEffectiveMiningTool(stack, Blocks.STONE.defaultBlockState());
    }

    /** Returns whether a held tool is effective against a specific InfX earth-elemental body. */
    public static boolean isEffectiveMiningTool(ItemStack stack, BlockState state) {
        if (stack.isEmpty()) {
            return false;
        }
        var equipment = InfXItems.catalog().equipment(stack);
        if (equipment != null) {
            return InfxMiningRules.isEffective(equipment.key(), state);
        }
        return stack.is(ItemTags.PICKAXES)
                && stack.getDestroySpeed(state) > 1.0F
                || stack.getDestroySpeed(state) > 1.0F
                || stack.isCorrectToolForDrops(state);
    }

    /**
     * Magma-cube gate: water/snowball/explosion, or a stone-mining tool in the attacker's hand.
     * InfX blocks every other source, including melee from mobs without an effective tool.
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
     * other attacker needs a stone-mining tool. InfX grants no free pass to mob melee or hazards.
     */
    public static boolean earthElementalAccepts(EarthElemental elemental, DamageSource source) {
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
     * InfX blaze vulnerability: only snowball and water damage always hurt; fire damage and
     * fire-aspect / flame weapons never do; other enchanted weapons do. Attacker ignition is ignored.
     */
    public static boolean blazeAccepts(ServerLevel level, DamageSource source) {
        if (source.getDirectEntity() instanceof Snowball || source.is(DamageTypeTags.IS_DROWNING)) {
            return true;
        }
        if (source.is(DamageTypeTags.IS_FIRE)) {
            return false;
        }
        ItemStack weapon = resolveWeapon(source);
        return !weapon.isEmpty() && weapon.isEnchanted() && !hasFireEnchantment(level, weapon);
    }

    /**
     * InfX fire elemental uses the same immunity shape as blaze: water/snowball always hurt;
     * only non-fire enchanted weapons land otherwise.
     */
    public static boolean fireElementalAccepts(ServerLevel level, DamageSource source) {
        return blazeAccepts(level, source);
    }

    /** InfX silver aspect: silver-headed arrows in flight or a silver weapon in the attacker's hand. */
    public static boolean hasSilverAspect(DamageSource source) {
        if (source.getDirectEntity() instanceof AbstractArrow arrow
                && arrow.getPickupItemStackOrigin().getItem() instanceof InfxArrowItem arrowItem) {
            return arrowItem.key().material() == InfxMaterial.SILVER;
        }
        if (source.getEntity() instanceof LivingEntity attacker) {
            var entry = InfXItems.catalog().equipment(attacker.getMainHandItem());
            return entry != null
                    && entry.key().material() == InfxMaterial.SILVER
                    && entry.key().type() != EquipmentType.ARROW;
        }
        return false;
    }

    /** InfX magic aspect: magic-typed damage, or any enchanted weapon (melee hand or firing bow). */
    public static boolean hasMagicAspect(DamageSource source) {
        if (source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            return true;
        }
        ItemStack weapon = resolveWeapon(source);
        return !weapon.isEmpty() && weapon.isEnchanted();
    }

    /**
     * InfX shadow/nightwing gate: immune to everything except silver, magic and sunlight; the
     * sunlight instant-kill arrives as generic-kill damage, covered by BYPASSES_INVULNERABILITY.
     */
    public static boolean silverMagicGateAccepts(DamageSource source) {
        return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || hasSilverAspect(source)
                || hasMagicAspect(source);
    }

    /** InfX wight gate: immune to everything except fire, lava, silver and magic. */
    public static boolean wightAccepts(DamageSource source) {
        return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || source.is(DamageTypeTags.IS_FIRE)
                || hasSilverAspect(source)
                || hasMagicAspect(source);
    }
}
