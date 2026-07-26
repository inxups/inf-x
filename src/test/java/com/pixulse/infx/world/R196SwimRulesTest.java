package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class R196SwimRulesTest {
    private static final double WATER_DRAG = 0.8D;
    private static final double SWIM_ACCELERATION = 0.02D;

    @Test
    void currentImpulseIsConstantRegardlessOfTouchedBlockCount() {
        Vec3 single = R196SwimRules.currentImpulse(new Vec3(1.0D, 0.0D, 0.0D), R196SwimRules.CURRENT_SCALE);
        Vec3 many = R196SwimRules.currentImpulse(new Vec3(6.0D, 0.0D, 0.0D), R196SwimRules.CURRENT_SCALE);
        assertEquals(R196SwimRules.CURRENT_SCALE, single.length(), 1.0E-9D);
        assertEquals(R196SwimRules.CURRENT_SCALE, many.length(), 1.0E-9D);
        assertEquals(single, many);
    }

    @Test
    void opposingFlowsCancelAndStillWaterPushesNothing() {
        assertEquals(Vec3.ZERO, R196SwimRules.currentImpulse(Vec3.ZERO, R196SwimRules.CURRENT_SCALE));
        assertEquals(
                Vec3.ZERO,
                R196SwimRules.currentImpulse(new Vec3(1.0D, 0.0D, 0.0D).add(-1.0D, 0.0D, 0.0D), R196SwimRules.CURRENT_SCALE));
    }

    @Test
    void waterGravityRestoresMiteSinkRate() {
        double vanillaGravity = 0.08D;
        assertEquals(0.005D, vanillaGravity / 16.0D, 1.0E-9D);
        assertEquals(0.02D, R196SwimRules.waterGravity(vanillaGravity) / 16.0D, 1.0E-9D);
    }

    @Test
    void submergedSwimmerGetsFullSwimUpImpulse() {
        float factor = R196SwimRules.swimUpFactor(true, false, false, true, false, 0.0F);
        assertEquals(1.0F, factor);
        assertEquals(0.04D, R196SwimRules.swimUpImpulse(factor), 1.0E-9D);
    }

    @Test
    void surfaceAndFallingColumnBothCutSwimUpToSevenSixteenths() {
        assertEquals(
                R196SwimRules.SURFACE_FACTOR,
                R196SwimRules.swimUpFactor(false, false, false, true, false, 0.0F));
        assertEquals(
                R196SwimRules.SURFACE_FACTOR,
                R196SwimRules.swimUpFactor(true, true, false, true, false, 0.0F));
    }

    @Test
    void drawnBowWhileSuspendedAppliesTheSurfacePenaltyOnTop() {
        assertEquals(
                R196SwimRules.SURFACE_FACTOR,
                R196SwimRules.swimUpFactor(true, false, true, true, false, 0.0F),
                1.0E-6F);
        assertEquals(
                R196SwimRules.SURFACE_FACTOR * R196SwimRules.SURFACE_FACTOR,
                R196SwimRules.swimUpFactor(false, false, true, true, false, 0.0F),
                1.0E-6F);
        assertEquals(1.0F, R196SwimRules.swimUpFactor(true, false, true, false, false, 0.0F));
    }

    @Test
    void slowdownCapsSwimUpAndWallContactPartlyRestoresIt() {
        assertEquals(0.6F, R196SwimRules.swimUpFactor(true, false, false, true, false, -0.4F), 1.0E-6F);
        assertEquals(
                0.6F * 0.7F + 0.3F,
                R196SwimRules.swimUpFactor(true, false, false, true, true, -0.4F),
                1.0E-6F);
        assertEquals(0.0F, R196SwimRules.swimUpFactor(true, false, false, true, false, -1.5F));
    }

    @Test
    void wallContactDoesNotRestoreSwimUpAtTheSurface() {
        assertEquals(
                R196SwimRules.SURFACE_FACTOR,
                R196SwimRules.swimUpFactor(false, false, false, true, true, 0.0F));
    }

    @Test
    void speedModifierFollowsMiteAmplifierWeights() {
        assertEquals(0.0F, R196SwimRules.speedModifier(-1, -1, false));
        assertEquals(-0.2F, R196SwimRules.speedModifier(0, -1, false), 1.0E-6F);
        assertEquals(-0.4F, R196SwimRules.speedModifier(1, -1, false), 1.0E-6F);
        assertEquals(0.2F, R196SwimRules.speedModifier(-1, 0, false), 1.0E-6F);
        assertEquals(0.0F, R196SwimRules.speedModifier(0, 0, false), 1.0E-6F);
        assertEquals(-0.75F, R196SwimRules.speedModifier(-1, -1, true), 1.0E-6F);
    }

    @Test
    void upstreamSwimmingStillMakesHeadway() {
        double still = terminalSpeed(SWIM_ACCELERATION);
        double upstream = terminalSpeed(SWIM_ACCELERATION - R196SwimRules.CURRENT_SCALE);
        double downstream = terminalSpeed(SWIM_ACCELERATION + R196SwimRules.CURRENT_SCALE);
        assertEquals(0.08D, still, 1.0E-6D);
        assertEquals(0.024D, upstream, 1.0E-6D);
        assertEquals(0.136D, downstream, 1.0E-6D);
        assertTrue(upstream > 0.0D, "MITE's 0.02 swim acceleration beats the 0.014 current");
        assertTrue(upstream < still && still < downstream);
    }

    @Test
    void sinkRateConvergesOnMiteTerminalVelocity() {
        double velocity = 0.0D;
        for (int tick = 0; tick < 500; tick++) {
            velocity = velocity * WATER_DRAG - R196SwimRules.waterGravity(0.08D) / 16.0D;
        }
        assertEquals(-0.1D, velocity, 1.0E-6D);
    }

    /** MITE applies acceleration first, then the 0.8 drag, so the fixed point is 4x the acceleration. */
    private static double terminalSpeed(double acceleration) {
        double velocity = 0.0D;
        for (int tick = 0; tick < 500; tick++) {
            velocity = (velocity + acceleration) * WATER_DRAG;
        }
        return velocity;
    }

    @Test
    void oppositionIsZeroWhenSwimmingWithOrAcrossTheCurrent() {
        Vec3 current = new Vec3(1.0D, 0.0D, 0.0D);
        assertEquals(0.0D, R196SwimRules.opposition(new Vec3(1.0D, 0.0D, 0.0D), current), 1.0E-9D);
        assertEquals(0.0D, R196SwimRules.opposition(new Vec3(0.0D, 0.0D, 1.0D), current), 1.0E-9D);
    }

    @Test
    void oppositionIsOneWhenSwimmingDirectlyUpstream() {
        Vec3 current = new Vec3(1.0D, 0.0D, 0.0D);
        assertEquals(1.0D, R196SwimRules.opposition(new Vec3(-1.0D, 0.0D, 0.0D), current), 1.0E-9D);
    }

    @Test
    void oppositionIsPartialAtAnAngleAndIgnoresVerticalMotion() {
        Vec3 current = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 backAndUp = new Vec3(-1.0D, 5.0D, 0.0D);
        assertEquals(1.0D, R196SwimRules.opposition(backAndUp, current), 1.0E-9D);
        Vec3 diagonal = new Vec3(-1.0D, 0.0D, 1.0D);
        assertEquals(Math.sqrt(2.0D) / 2.0D, R196SwimRules.opposition(diagonal, current), 1.0E-9D);
    }

    @Test
    void oppositionIsZeroWhenMovementOrCurrentIsNegligible() {
        Vec3 current = new Vec3(1.0D, 0.0D, 0.0D);
        assertEquals(0.0D, R196SwimRules.opposition(Vec3.ZERO, current), 1.0E-9D);
        assertEquals(0.0D, R196SwimRules.opposition(new Vec3(-1.0D, 0.0D, 0.0D), Vec3.ZERO), 1.0E-9D);
    }

    @Test
    void antiCurrentSprintDragOnlyKicksInWhileSwimmingUpstream() {
        float sprintSlowDown = 0.9F;
        float normalSlowDown = 0.8F;
        Vec3 current = new Vec3(1.0D, 0.0D, 0.0D);

        assertEquals(
                sprintSlowDown,
                R196SwimRules.antiCurrentSprintDrag(
                        sprintSlowDown, normalSlowDown, new Vec3(1.0D, 0.0D, 0.0D), current),
                1.0E-6D,
                "swimming with the current keeps the sprint slowdown unchanged");
        assertEquals(
                normalSlowDown,
                R196SwimRules.antiCurrentSprintDrag(
                        sprintSlowDown, normalSlowDown, new Vec3(-1.0D, 0.0D, 0.0D), current),
                1.0E-6D,
                "swimming directly upstream falls all the way back to the non-sprint slowdown");

        double partial = R196SwimRules.antiCurrentSprintDrag(
                sprintSlowDown, normalSlowDown, new Vec3(-1.0D, 0.0D, 1.0D), current);
        assertTrue(
                partial > normalSlowDown && partial < sprintSlowDown,
                "swimming diagonally against the current only partly restores drag");
    }
}
