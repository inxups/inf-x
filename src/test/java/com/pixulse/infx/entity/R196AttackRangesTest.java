package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.item.R196EquipmentType;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

class R196AttackRangesTest {
    private static final double EPSILON = 1.0E-6;

    @Test
    void newAiToolReachUsesTheR196MobFactorAndInclusiveBoundary() {
        assertEquals(1.95, R196AttackRanges.newAiReach(1.5, R196EquipmentType.SWORD.reachBonus()), EPSILON);
        assertEquals(1.65, R196AttackRanges.newAiReach(1.5, R196EquipmentType.KNIFE.reachBonus()), EPSILON);
        assertEquals(2.1, R196AttackRanges.newAiReach(1.5, R196EquipmentType.SCYTHE.reachBonus()), EPSILON);

        assertTrue(R196AttackRanges.withinNewAiReachDistanceSqr(1.5 * 1.5, 1.5, 0.0F));
        assertFalse(R196AttackRanges.withinNewAiReachDistanceSqr(1.5001 * 1.5001, 1.5, 0.0F));
        assertTrue(R196AttackRanges.withinNewAiReachDistanceSqr(2.1 * 2.1, 1.5, 1.0F));
        assertFalse(R196AttackRanges.withinNewAiReachDistanceSqr(2.1001 * 2.1001, 1.5, 1.0F));
        assertTrue(R196AttackRanges.withinNewAiReachDistanceSqr(4.0, 2.0, 0.0F));
    }

    @Test
    void oldAiFamiliesUseStrictDistanceAndVerticalCollision() {
        assertTrue(R196AttackRanges.withinOldAiReachDistanceSqr(1.7499 * 1.7499, true, 1.75));
        assertFalse(R196AttackRanges.withinOldAiReachDistanceSqr(1.75 * 1.75, true, 1.75));
        assertFalse(R196AttackRanges.withinOldAiReachDistanceSqr(1.0, false, 1.75));
        assertTrue(R196AttackRanges.withinOldAiReachDistanceSqr(1.1999 * 1.1999, true, 1.2));
        assertFalse(R196AttackRanges.withinOldAiReachDistanceSqr(1.2 * 1.2, true, 1.2));
    }

    @Test
    void wolfWidthFormulaAndBatHalfBoxMatchR196() {
        assertEquals(2.100625, R196AttackRanges.wolfReachSqr(0.7, 0.6, 0.0F), EPSILON);
        assertEquals(1.7025, R196AttackRanges.wolfReachSqr(0.6, 0.6, 0.0F), EPSILON);
        assertEquals(2.350625, R196AttackRanges.wolfReachSqr(0.7, 0.6, 0.25F), EPSILON);

        AABB bat = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
        assertTrue(R196AttackRanges.scaledHorizontalContact(
                bat, new AABB(0.74, 0.0, 0.4, 1.0, 1.0, 0.6), 0.5));
        assertFalse(R196AttackRanges.scaledHorizontalContact(
                bat, new AABB(0.76, 0.0, 0.4, 1.0, 1.0, 0.6), 0.5));
    }

    @Test
    void rangedAndArachnidBoundariesAreHardLimits() {
        assertTrue(R196AttackRanges.withinHardRangedReach(400.0, R196AttackRanges.WITCH_RANGED_REACH));
        assertFalse(R196AttackRanges.withinHardRangedReach(400.01, R196AttackRanges.WITCH_RANGED_REACH));
        assertTrue(R196AttackRanges.withinHardRangedReach(900.0, R196AttackRanges.SKELETON_RANGED_REACH));
        assertFalse(R196AttackRanges.withinHardRangedReach(900.01, R196AttackRanges.SKELETON_RANGED_REACH));
        assertFalse(R196AttackRanges.isArachnidLeapDistance(4.0));
        assertTrue(R196AttackRanges.isArachnidLeapDistance(4.01));
        assertTrue(R196AttackRanges.isArachnidLeapDistance(35.99));
        assertFalse(R196AttackRanges.isArachnidLeapDistance(36.0));
    }

    @Test
    void explosionEntityRadiiKeepR196SourceMappingAndChargeMultiplier() {
        assertEquals(4.4, R196ExplosionRanges.creeperEntityRadius(R196Creeper.Variant.CREEPER, false), EPSILON);
        assertEquals(8.8, R196ExplosionRanges.creeperEntityRadius(R196Creeper.Variant.CREEPER, true), EPSILON);
        assertEquals(8.8, R196ExplosionRanges.creeperEntityRadius(R196Creeper.Variant.INFERNAL, false), EPSILON);
        assertEquals(17.6, R196ExplosionRanges.creeperEntityRadius(R196Creeper.Variant.INFERNAL, true), EPSILON);
        assertEquals(4.0, R196ExplosionRanges.NETHERSPAWN_ENTITY_RADIUS, EPSILON);
        assertEquals(4.0, R196ExplosionRanges.GHAST_FIREBALL_ENTITY_RADIUS, EPSILON);
        assertTrue(R196ExplosionRanges.entityRadius((net.minecraft.world.entity.Entity) null).isEmpty());
    }
}
