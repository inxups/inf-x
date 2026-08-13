package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.level.dimension.DimensionType;
import org.junit.jupiter.api.Test;

class LavaLakeRulesTest {
    @Test
    void flushSurfacePoolIsOpenToTheSky() {
        assertTrue(LavaLakeRules.isSurfaceLake(-10, -10));
        assertTrue(LavaLakeRules.isSurfaceLake(-30, -30));
    }

    @Test
    void terrainBelowTheLakeSurfaceIsExposed() {
        assertTrue(LavaLakeRules.isSurfaceLake(-15, -10));
        assertTrue(LavaLakeRules.isSurfaceLake(-54, -40));
    }

    @Test
    void buriedLakesAreLeftAsLava() {
        assertFalse(LavaLakeRules.isSurfaceLake(80, -20));
        assertFalse(LavaLakeRules.isSurfaceLake(63, -10));
    }

    @Test
    void noLakeSentinelIsNeverExposed() {
        assertFalse(LavaLakeRules.isSurfaceLake(60, DimensionType.WAY_BELOW_MIN_Y));
    }

    @Test
    void waterLakesAboveTheLavaEligibleBandStayUntouched() {
        assertFalse(LavaLakeRules.isSurfaceLake(20, 40));
        assertFalse(LavaLakeRules.isSurfaceLake(60, 63));
    }
}
