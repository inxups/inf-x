package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class R196SkeletonRangedAttackTest {
    private static final double EPSILON = 1.0E-6;

    @Test
    void rangedAimUsesMiteDistanceCurveAndIgnoresVerticalMotion() {
        R196Skeleton.MiteRangedAim aim = R196Skeleton.calculateMiteRangedAim(
                0.0, 0.0, 10.0, 0.0, new Vec3(0.2, 99.0, -0.1), Vec3.ZERO, 0.5F);

        assertEquals(7.585776F, aim.leadTicks(), EPSILON);
        assertEquals(11.517155, aim.x(), EPSILON);
        assertEquals(-0.758578, aim.z(), EPSILON);
        assertEquals(133.220303, aim.horizontalDistanceSqr(), 1.0E-6);
    }

    @Test
    void rangedAimFallsBackPerAxisForInvalidKnownPlayerMotion() {
        R196Skeleton.MiteRangedAim aim = R196Skeleton.calculateMiteRangedAim(
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
    void verticalCorrectionKeepsMiteRangeAndElevationRules() {
        assertEquals(-0.0475, R196Skeleton.miteVerticalCorrection(100.0, 5.0), EPSILON);
        assertEquals(0.10765625, R196Skeleton.miteVerticalCorrection(625.0, 5.0), EPSILON);
        assertEquals(0.09, R196Skeleton.miteVerticalCorrection(400.0, 9.0), EPSILON);
    }

    @Test
    void skeletonUncertaintyIncludesMiteMultiplier() {
        assertEquals(21.0F, R196Skeleton.miteArrowInaccuracy(0));
        assertEquals(9.0F, R196Skeleton.miteArrowInaccuracy(2));
        assertEquals(3.0F, R196Skeleton.miteArrowInaccuracy(3));
    }
}
