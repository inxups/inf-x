package com.pixulse.infx.item;

import com.mojang.datafixers.util.Either;
import com.pixulse.infx.registry.InfXAttributes;
import java.util.Collection;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** Canonical player interaction and melee reach rules for every item stack. */
public final class ItemReach {
    public static final double BASE_RANGE = 2.5;
    public static final double CREATIVE_RANGE = 5.0;
    public static final double MAX_RANGE = 64.0;
    public static final double MAX_HEIGHT_ADJUSTMENT = 1.0;

    private ItemReach() {}

    public static double interactionRange(Player player) {
        return player.isCreative()
                ? CREATIVE_RANGE
                : mainHandRange(player, InfXAttributes.ITEM_INTERACTION_RANGE);
    }

    public static double meleeRange(Player player) {
        return player.isCreative()
                ? CREATIVE_RANGE
                : mainHandRange(player, InfXAttributes.ITEM_MELEE_RANGE);
    }

    /** Maximum entity scan distance; candidate-specific validation happens after the raycast. */
    public static double targetingRange(Player player) {
        double melee = meleeRange(player);
        if (!player.isCreative() && hasPositiveMeleeBonus(player.getMainHandItem())) {
            melee += MAX_HEIGHT_ADJUSTMENT;
        }
        return Math.max(interactionRange(player), melee);
    }

    public static boolean isWithinInteractionRange(Player player, Vec3 location) {
        return location.closerThan(player.getEyePosition(), interactionRange(player));
    }

    public static boolean isWithinTargetingRange(Player player, Vec3 location) {
        return isWithinInteractionRange(player, location) || isWithinMeleeRange(player, location);
    }

    public static boolean isWithinMeleeRange(Player player, Vec3 location) {
        double adjustment = pointHeightAdjustment(player, location);
        double distance = player.getEyePosition().distanceTo(location);
        return distance <= meleeRange(player) + adjustment;
    }

    public static boolean isWithinMeleeRange(Player player, AABB bounds, double extraBuffer) {
        double adjustment = boxHeightAdjustment(player, bounds);
        double distance = Math.sqrt(bounds.distanceToSqr(player.getEyePosition()));
        return distance <= meleeRange(player) + adjustment + extraBuffer;
    }

    /** Adapter for vanilla APIs whose fixed return type is AttackRange; no item ATTACK_RANGE is read. */
    public static AttackRange vanillaAdapter(Player player) {
        float survivalRange = (float) mainHandRange(player, InfXAttributes.ITEM_MELEE_RANGE);
        return new AttackRange(0.0F, survivalRange, 0.0F, (float) CREATIVE_RANGE, 0.0F, 1.0F);
    }

    /** Scan adapter for piercing attacks; every returned candidate must still pass the melee predicate. */
    public static AttackRange vanillaScanAdapter(Player player) {
        float survivalRange = (float) mainHandRange(player, InfXAttributes.ITEM_MELEE_RANGE);
        if (hasPositiveMeleeBonus(player.getMainHandItem())) {
            survivalRange += (float) MAX_HEIGHT_ADJUSTMENT;
        }
        return new AttackRange(0.0F, survivalRange, 0.0F, (float) CREATIVE_RANGE, 0.0F, 1.0F);
    }

    /** Routes vanilla fixed-range piercing scans through the INFX candidate and range rules. */
    public static Either<BlockHitResult, Collection<EntityHitResult>> getMeleeHitEntitiesAlong(
            Entity attacker,
            AttackRange attackRange,
            Predicate<Entity> matching,
            ClipContext.Block blockClipType) {
        if (!(attacker instanceof Player player)) {
            return ProjectileUtil.getHitEntitiesAlong(attacker, attackRange, matching, blockClipType);
        }

        Either<BlockHitResult, Collection<EntityHitResult>> result = ProjectileUtil.getHitEntitiesAlong(
                attacker, vanillaScanAdapter(player), matching, blockClipType);
        if (result.right().isEmpty()) {
            return result;
        }
        return Either.right(result.right().orElseThrow().stream()
                .filter(hit -> isWithinMeleeRange(player, hit.getLocation()))
                .toList());
    }

    public static boolean hasPositiveMeleeBonus(ItemStack stack) {
        return hasPositiveMeleeBonus(stack.getAttributeModifiers());
    }

    static boolean hasPositiveMeleeBonus(ItemAttributeModifiers attributes) {
        double withItem = applyMainHandModifiers(
                attributes, InfXAttributes.ITEM_MELEE_RANGE, BASE_RANGE);
        return withItem > BASE_RANGE;
    }

    static double applyMainHandModifiers(
            ItemAttributeModifiers attributes, Holder<Attribute> attribute, double baseValue) {
        double value = attributes.compute(attribute, baseValue, EquipmentSlot.MAINHAND);
        return Double.isNaN(value) ? 0.0 : Mth.clamp(value, 0.0, MAX_RANGE);
    }

    static double heightAdjustment(double elevationDifference) {
        if (elevationDifference > 0.5) {
            return Math.min(MAX_HEIGHT_ADJUSTMENT, (elevationDifference - 0.5) * 0.5);
        }
        if (elevationDifference < -0.5) {
            return Math.max(-MAX_HEIGHT_ADJUSTMENT, (elevationDifference + 0.5) * 0.5);
        }
        return 0.0;
    }

    private static double pointHeightAdjustment(Player player, Vec3 location) {
        if (player.isCreative() || !hasPositiveMeleeBonus(player.getMainHandItem())) {
            return 0.0;
        }
        return heightAdjustment(player.getEyeY() - location.y);
    }

    private static double boxHeightAdjustment(Player player, AABB bounds) {
        if (player.isCreative() || !hasPositiveMeleeBonus(player.getMainHandItem())) {
            return 0.0;
        }
        return heightAdjustment(player.getY() - bounds.minY);
    }

    private static double mainHandRange(Player player, Holder<Attribute> attribute) {
        return applyMainHandModifiers(
                player.getMainHandItem().getAttributeModifiers(),
                attribute,
                player.getAttributeBaseValue(attribute));
    }
}
