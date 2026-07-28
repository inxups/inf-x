package com.pixulse.infx.entity;

import com.pixulse.infx.item.ToolItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

/** Exact R196 melee and contact predicates, independent from modern hitbox-expanded reach. */
final class AttackRanges {
    static final double NEW_AI_REACH = 1.5;
    static final double OLD_AI_REACH = 1.75;
    static final double EARTH_ELEMENTAL_REACH = 2.0;
    static final double SILVERFISH_REACH = 1.2;
    static final double WITCH_RANGED_REACH = 20.0;
    static final double SKELETON_RANGED_REACH = 30.0;

    private AttackRanges() {}

    static boolean withinNewAiReach(Mob attacker, LivingEntity target) {
        return withinNewAiReach(attacker, target, NEW_AI_REACH);
    }

    static boolean withinNewAiReach(Mob attacker, LivingEntity target, double baseReach) {
        return withinNewAiReachDistanceSqr(
                attacker.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ()),
                baseReach,
                heldToolReachBonus(attacker));
    }

    static double newAiReach(double baseReach, float toolReachBonus) {
        return baseReach + toolReachBonus * 0.6;
    }

    static boolean withinNewAiReachDistanceSqr(double distanceSqr, double baseReach, float toolReachBonus) {
        double reach = newAiReach(baseReach, toolReachBonus);
        return distanceSqr <= reach * reach;
    }

    static boolean withinOldAiReach(Mob attacker, LivingEntity target, double reach) {
        return withinOldAiReachDistanceSqr(
                attacker.distanceToSqr(target.getX(), target.getY(), target.getZ()),
                verticallyOverlaps(attacker, target),
                reach);
    }

    static boolean withinOldAiReachDistanceSqr(double distanceSqr, boolean verticallyOverlaps, double reach) {
        return verticallyOverlaps && distanceSqr < reach * reach;
    }

    static boolean withinWolfReach(Mob attacker, LivingEntity target) {
        double reachSqr = wolfReachSqr(attacker.getBbWidth(), target.getBbWidth(), heldToolReachBonus(attacker));
        return attacker.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ()) <= reachSqr;
    }

    static double wolfReachSqr(double attackerWidth, double targetWidth, float toolReachBonus) {
        double scaledWidth = attackerWidth * 1.75;
        return scaledWidth * scaledWidth + targetWidth + toolReachBonus;
    }

    static float heldToolReachBonus(Mob attacker) {
        return attacker.getMainHandItem().getItem() instanceof ToolItem tool
                ? tool.key().type().reachBonus()
                : 0.0F;
    }

    static boolean verticallyOverlaps(Entity attacker, Entity target) {
        return target.getBoundingBox().maxY > attacker.getBoundingBox().minY
                && target.getBoundingBox().minY < attacker.getBoundingBox().maxY;
    }

    static boolean scaledHorizontalContact(Entity attacker, Entity target, double horizontalScale) {
        return scaledHorizontalContact(attacker.getBoundingBox(), target.getBoundingBox(), horizontalScale);
    }

    static boolean scaledHorizontalContact(AABB box, AABB targetBox, double horizontalScale) {
        double centerX = (box.minX + box.maxX) * 0.5;
        double centerZ = (box.minZ + box.maxZ) * 0.5;
        double halfWidth = (box.maxX - box.minX) * horizontalScale * 0.5;
        double halfDepth = (box.maxZ - box.minZ) * horizontalScale * 0.5;
        AABB scaled = new AABB(
                centerX - halfWidth,
                box.minY,
                centerZ - halfDepth,
                centerX + halfWidth,
                box.maxY,
                centerZ + halfDepth);
        return scaled.intersects(targetBox);
    }

    static boolean isArachnidLeapDistance(double distanceSqr) {
        return distanceSqr > 4.0 && distanceSqr < 36.0;
    }

    static boolean withinHardRangedReach(double distanceSqr, double reach) {
        return distanceSqr <= reach * reach;
    }
}
