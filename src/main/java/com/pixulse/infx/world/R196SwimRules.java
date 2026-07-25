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
