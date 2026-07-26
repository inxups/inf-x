package com.pixulse.infx.world;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Bridges {@link R196SwimRules} to the live world: reads the blocks MITE's
 * {@code World.handleMaterialAcceleration} and {@code EntityLivingBase.onLivingUpdate} inspected and
 * feeds the results back as motion changes.
 */
public final class R196SwimPhysics {
    private R196SwimPhysics() {}

    /**
     * MITE {@code World.handleMaterialAcceleration}: sum every touched fluid block's unit flow, then
     * normalise once and scale. Unlike vanilla there is no shallow-depth taper and no minimum
     * magnitude, so a one-deep stream pushes exactly as hard as a full block.
     */
    public static void applyNormalizedCurrent(Entity entity, double scale) {
        Vec3 impulse = R196SwimRules.currentImpulse(accumulatedCurrentFlow(entity), scale);
        if (impulse.lengthSqr() > 0.0D) entity.addDeltaMovement(impulse);
    }

    /**
     * Raw, un-normalised sum of every touched water block's unit flow, as MITE's
     * {@code World.handleMaterialAcceleration} computes it before normalising. Exposed separately so
     * callers that only need the current's direction (not its impulse) don't repeat the scan.
     */
    public static Vec3 accumulatedCurrentFlow(Entity entity) {
        AABB box = entity.getFluidInteractionBox();
        if (box == null) return Vec3.ZERO;
        BlockGetter level = entity.level();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        Vec3 accumulated = Vec3.ZERO;
        int minX = Mth.floor(box.minX);
        int minY = Mth.floor(box.minY);
        int minZ = Mth.floor(box.minZ);
        int maxX = Mth.ceil(box.maxX) - 1;
        int maxY = Mth.ceil(box.maxY) - 1;
        int maxZ = Mth.ceil(box.maxZ) - 1;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    cursor.set(x, y, z);
                    FluidState fluid = level.getFluidState(cursor);
                    if (!fluid.is(FluidTags.WATER)) continue;
                    if (y + fluid.getHeight(level, cursor) < box.minY) continue;
                    accumulated = accumulated.add(fluid.getFlow(level, cursor));
                }
            }
        }
        return accumulated;
    }

    /**
     * MITE has no counter-current sprint model; scales {@code sprintSlowDown} back toward
     * {@code normalSlowDown} proportionally to how directly the entity's own movement opposes the
     * surrounding current, so a sprinting player can no longer trivially out-swim it upstream.
     */
    public static double antiCurrentSprintDrag(
            Entity entity, float sprintSlowDown, float normalSlowDown, Vec3 movement) {
        Vec3 current = accumulatedCurrentFlow(entity);
        return R196SwimRules.antiCurrentSprintDrag(sprintSlowDown, normalSlowDown, movement, current);
    }

    /** Quadruples the effective gravity so vanilla's {@code baseGravity / 16} becomes MITE's 0.02. */
    public static double waterGravity(double baseGravity) {
        return R196SwimRules.waterGravity(baseGravity);
    }

    /**
     * Restores MITE's water pull for a sprinting player in a falling water column. Vanilla omits
     * this pull while sprinting, which would otherwise let the reduced waterfall swim-up impulse
     * accumulate into upward movement.
     */
    public static Vec3 applyFallingWaterSprintGravity(
            LivingEntity entity, double waterBaseGravity, Vec3 movement) {
        if (!isFallingWaterColumn(entity)) return movement;
        return new Vec3(
                movement.x,
                R196SwimRules.sprintWaterfallVerticalMovement(movement.y, waterBaseGravity),
                movement.z);
    }

    /** Adds MITE's factored swim-up impulse in place of vanilla's flat 0.04. */
    public static void swimUp(LivingEntity entity) {
        entity.addDeltaMovement(new Vec3(0.0D, swimUpImpulse(entity), 0.0D));
    }

    /**
     * MITE {@code EntityLivingBase.onLivingUpdate}: the upward push while holding jump underwater,
     * cut to 7/16 at the surface and inside a falling column so waterfalls cannot be climbed.
     */
    public static double swimUpImpulse(LivingEntity entity) {
        BlockGetter level = entity.level();
        BlockPos feet = entity.blockPosition();
        BlockPos head = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
        FluidState feetFluid = level.getFluidState(feet);
        boolean feetInLiquid = feetFluid.is(FluidTags.WATER) || feetFluid.is(FluidTags.LAVA);
        boolean fallingColumn = isFallingWaterColumn(entity);
        boolean suspended = !entity.isPassenger()
                && !entity.onGround()
                && (entity.isInWater() || entity.isInLava());
        boolean drawingBow = entity.isUsingItem() && entity.getUseItem().getItem() instanceof BowItem;
        float factor = R196SwimRules.swimUpFactor(
                feetInLiquid,
                fallingColumn,
                drawingBow,
                suspended,
                entity.horizontalCollision,
                speedModifier(entity));
        return R196SwimRules.swimUpImpulse(factor);
    }

    /** MITE {@code getSpeedBoostVsSlowDown} without the paralysis-resistance term, which has no port. */
    public static float speedModifier(LivingEntity entity) {
        MobEffectInstance slowness = entity.getEffect(MobEffects.SLOWNESS);
        MobEffectInstance speed = entity.getEffect(MobEffects.SPEED);
        return R196SwimRules.speedModifier(
                slowness == null ? -1 : slowness.getAmplifier(),
                speed == null ? -1 : speed.getAmplifier(),
                entity.level().getBlockState(entity.blockPosition()).is(Blocks.COBWEB));
    }

    private static boolean isFalling(FluidState fluid) {
        return fluid.is(FluidTags.WATER)
                && fluid.hasProperty(FlowingFluid.FALLING)
                && fluid.getValue(FlowingFluid.FALLING);
    }

    /**
     * Returns whether both the entity's feet and head are inside falling water. This mirrors the
     * MITE metadata-9 check used for waterfall swim penalties.
     */
    public static boolean isFallingWaterColumn(LivingEntity entity) {
        BlockGetter level = entity.level();
        BlockPos feet = entity.blockPosition();
        BlockPos head = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
        return isFalling(level.getFluidState(feet)) && isFalling(level.getFluidState(head));
    }
}
