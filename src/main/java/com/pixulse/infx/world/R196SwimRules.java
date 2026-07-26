package com.pixulse.infx.world;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * MITE water physics constants and pure math.
 *
 * <p>MITE {@code World.handleMaterialAcceleration} normalises the accumulated flow of every water
 * block touching the entity and applies a constant {@code 0.014} impulse, and
 * {@code EntityLivingBase.moveEntityWithHeading} sinks swimmers at {@code 0.02}/tick instead of the
 * modern {@code 0.005}. {@code EntityLivingBase.onLivingUpdate} scales the {@code 0.04} swim-up
 * impulse down to 7/16 at the surface and inside a falling water column.
 */
public final class R196SwimRules {
    /** MITE {@code World.handleMaterialAcceleration} impulse for a normalised water flow. */
    public static final double CURRENT_SCALE = 0.014D;

    /** Squared length below which MITE treats the accumulated flow as still water. */
    public static final double CURRENT_EPSILON_SQR = 1.0E-5D;

    /**
     * MITE sinks swimmers at {@code 0.02}/tick; {@code getFluidFallingAdjustedMovement} applies
     * {@code baseGravity / 16}, so the default {@code 0.08} gravity has to be quadrupled.
     */
    public static final double WATER_GRAVITY_MULTIPLIER = 4.0D;

    /** MITE {@code EntityLivingBase.onLivingUpdate} swim-up impulse before the surface factor. */
    public static final double SWIM_UP_IMPULSE = 0.04D;

    /** MITE's 7/16 penalty for breaking the surface or climbing a falling water column. */
    public static final float SURFACE_FACTOR = 0.4375F;

    /** MITE amplifier weight for movement speed and slowness potions. */
    public static final float POTION_SPEED_STEP = 0.2F;

    /** MITE {@code EntityLivingBase.getSpeedBoostVsSlowDown} cobweb penalty. */
    public static final float COBWEB_SLOWDOWN = -0.75F;

    private R196SwimRules() {}

    /**
     * MITE normalises the summed flow instead of averaging it, so the push is exactly
     * {@link #CURRENT_SCALE} regardless of how many water blocks the entity touches.
     */
    public static Vec3 currentImpulse(Vec3 accumulated, double scale) {
        return accumulated.lengthSqr() < CURRENT_EPSILON_SQR
                ? Vec3.ZERO
                : accumulated.normalize().scale(scale);
    }

    /** Quadruples the effective gravity so {@code baseGravity / 16} becomes MITE's {@code 0.02}. */
    public static double waterGravity(double baseGravity) {
        return baseGravity * WATER_GRAVITY_MULTIPLIER;
    }

    /**
     * Vanilla skips {@code getFluidFallingAdjustedMovement} while sprinting. MITE's reduced
     * waterfall swim-up impulse still needs the normal water pull, otherwise sprinting can build
     * upward speed in a falling column.
     *
     * @param verticalMovement vertical movement after water drag
     * @param waterBaseGravity base gravity after {@link #waterGravity(double)} has been applied
     */
    public static double sprintWaterfallVerticalMovement(double verticalMovement, double waterBaseGravity) {
        return verticalMovement - waterBaseGravity / 16.0D;
    }

    /**
     * MITE {@code EntityLivingBase.onLivingUpdate} swim-up factor.
     *
     * @param feetInLiquid feet block is water or lava
     * @param fallingColumn feet and head are both inside falling water (MITE metadata 9)
     * @param drawingBow holding a drawn bow
     * @param suspendedInLiquid floating in liquid with no block within 0.2 below
     * @param horizontalCollision pressed against a wall this tick
     * @param speedModifier {@code getSpeedBoostVsSlowDown}, negative when slowed
     */
    public static float swimUpFactor(
            boolean feetInLiquid,
            boolean fallingColumn,
            boolean drawingBow,
            boolean suspendedInLiquid,
            boolean horizontalCollision,
            float speedModifier) {
        boolean penalised = !feetInLiquid || fallingColumn;
        float factor = penalised ? SURFACE_FACTOR : 1.0F;
        if (drawingBow && suspendedInLiquid) factor *= SURFACE_FACTOR;
        if (speedModifier < 0.0F && 1.0F + speedModifier < factor) factor = 1.0F + speedModifier;
        if (factor < 1.0F && horizontalCollision && !drawingBow && !penalised) {
            factor = factor * 0.7F + 0.3F;
        }
        return Mth.clamp(factor, 0.0F, 1.0F);
    }

    /** MITE applies {@code motionY += 0.04 * factor} while jump is held in liquid. */
    public static double swimUpImpulse(float factor) {
        return SWIM_UP_IMPULSE * factor;
    }

    /**
     * MITE has no counter-current sprint model; vanilla's post-1.13 sprint-swim drag reduction
     * otherwise lets a sprinting player trivially out-swim MITE's current in any direction,
     * including upstream. Blends {@code sprintSlowDown} back toward {@code normalSlowDown}
     * proportionally to how directly {@code movement} opposes {@code current}, so swimming with,
     * across, or without a current is unaffected.
     */
    public static double antiCurrentSprintDrag(
            float sprintSlowDown, float normalSlowDown, Vec3 movement, Vec3 current) {
        double opposition = opposition(movement, current);
        return Mth.lerp(opposition, sprintSlowDown, normalSlowDown);
    }

    /**
     * 0 when {@code movement} does not oppose {@code current} or either is negligible, rising to 1
     * when they point directly against each other. Compares horizontal components only, since
     * MITE's current push is horizontal.
     */
    public static double opposition(Vec3 movement, Vec3 current) {
        double mx = movement.x;
        double mz = movement.z;
        double cx = current.x;
        double cz = current.z;
        double mLenSqr = mx * mx + mz * mz;
        double cLenSqr = cx * cx + cz * cz;
        if (mLenSqr < CURRENT_EPSILON_SQR || cLenSqr < CURRENT_EPSILON_SQR) {
            return 0.0D;
        }
        double cos = (mx * cx + mz * cz) / Math.sqrt(mLenSqr * cLenSqr);
        return Mth.clamp(-cos, 0.0D, 1.0D);
    }

    /**
     * MITE {@code getSpeedBoostVsSlowDown} without paralysis resistance, which has no modern
     * counterpart.
     */
    public static float speedModifier(int slownessAmplifier, int speedAmplifier, boolean inCobweb) {
        float slow = slownessAmplifier < 0 ? 0.0F : (slownessAmplifier + 1) * -POTION_SPEED_STEP;
        float haste = speedAmplifier < 0 ? 0.0F : (speedAmplifier + 1) * POTION_SPEED_STEP;
        if (inCobweb) slow += COBWEB_SLOWDOWN;
        return slow + haste;
    }
}
