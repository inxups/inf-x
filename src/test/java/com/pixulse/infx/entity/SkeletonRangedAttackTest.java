package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class SkeletonRangedAttackTest {
    private static final double EPSILON = 1.0E-6;

    @Test
    void rangedAimUsesDistanceCurveAndIgnoresVerticalMotion() {
        InfxSkeleton.InfxRangedAim aim = InfxSkeleton.calculateRangedAim(
                0.0, 0.0, 10.0, 0.0, new Vec3(0.2, 99.0, -0.1), Vec3.ZERO, 0.5F);

        assertEquals(7.585776F, aim.leadTicks(), EPSILON);
        assertEquals(11.517155, aim.x(), EPSILON);
        assertEquals(-0.758578, aim.z(), EPSILON);
        assertEquals(133.220303, aim.horizontalDistanceSqr(), 1.0E-6);
    }

    @Test
    void rangedAimFallsBackPerAxisForInvalidKnownPlayerMotion() {
        InfxSkeleton.InfxRangedAim aim = InfxSkeleton.calculateRangedAim(
                0.0,
                0.0,
                10.0,
                0.0,
                new Vec3(0.5, 0.0, 1.01),
                new Vec3(0.2, 0.0, -0.3),
                0.5F);

        assertEquals(13.792888, aim.x(), EPSILON);
        assertEquals(-2.275733, aim.z(), EPSILON);
    }

    @Test
    void verticalCorrectionKeepsRangeAndElevationRules() {
        assertEquals(-0.0475, InfxSkeleton.verticalCorrection(100.0, 5.0), EPSILON);
        assertEquals(0.10765625, InfxSkeleton.verticalCorrection(625.0, 5.0), EPSILON);
        assertEquals(0.09, InfxSkeleton.verticalCorrection(400.0, 9.0), EPSILON);
    }

    @Test
    void skeletonUncertaintyIncludesMultiplier() {
        assertEquals(21.0F, InfxSkeleton.arrowInaccuracy(0));
        assertEquals(9.0F, InfxSkeleton.arrowInaccuracy(2));
        assertEquals(3.0F, InfxSkeleton.arrowInaccuracy(3));
    }

    @Test
    void tunedSkeletonArrowUsesFasterAndFlatterBallistics() {
        assertEquals(1.8F, InfxSkeleton.skeletonArrowSpeed());
        assertEquals(0.04D, InfxSkeleton.skeletonArrowGravity(), EPSILON);
        assertEquals(0.01D, InfxSkeleton.skeletonArrowGravityCompensation(), EPSILON);
    }

    @Test
    void ballisticInterceptMatchesTheModernArrowPhysicsForAMovingTarget() {
        InfxSkeleton.BallisticAim aim = InfxSkeleton.calculateBallisticIntercept(20.0, 0.0, 0.0, 0.2, 0.0);
        assertNotNull(aim);
        double travelScale = (1.0 - Math.pow(0.99F, aim.flightTicks())) / (1.0 - 0.99F);
        double verticalDrop = InfxSkeleton.skeletonArrowGravity() / (1.0 - 0.99F) * (aim.flightTicks() - travelScale);

        assertEquals(13.571206, aim.flightTicks(), 1.0E-5);
        assertEquals(
                InfxSkeleton.skeletonArrowSpeed() * InfxSkeleton.skeletonArrowSpeed(),
                aim.x() * aim.x() + aim.y() * aim.y() + aim.z() * aim.z(),
                1.0E-5);
        assertEquals(20.0 + 0.2 * aim.flightTicks(), aim.x() * travelScale, 1.0E-5);
        assertEquals(0.0, aim.y() * travelScale - verticalDrop, 1.0E-5);
        assertEquals(0.0, aim.z() * travelScale, EPSILON);
    }

    @Test
    void ballisticInterceptFallsBackWhenNoLowArcCanReachTheTarget() {
        assertNull(InfxSkeleton.calculateBallisticIntercept(10.0, 100.0, 0.0, 0.0, 0.0));
    }

    @Test
    void ballisticInterceptLeadsLateralMotion() {
        InfxSkeleton.BallisticAim aim = InfxSkeleton.calculateBallisticIntercept(20.0, 0.0, 0.0, 0.0, 0.2);
        assertNotNull(aim);
        double travelScale = (1.0 - Math.pow(0.99F, aim.flightTicks())) / (1.0 - 0.99F);

        assertTrue(aim.z() > 0.0);
        assertEquals(0.2 * aim.flightTicks(), aim.z() * travelScale, 1.0E-5);
    }
}
