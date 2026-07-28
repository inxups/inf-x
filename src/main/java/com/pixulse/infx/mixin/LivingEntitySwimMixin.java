package com.pixulse.infx.mixin;

import com.pixulse.infx.world.SwimPhysics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * MITE EntityLivingBase water movement: sinking uses a flat 0.02/tick instead of vanilla's
 * gravity/16, and holding jump underwater adds 0.04 scaled by MITE's surface / waterfall / slowdown
 * factor. Only players are converted; mobs keep vanilla water handling.
 */
@Mixin(LivingEntity.class)
abstract class LivingEntitySwimMixin {
    @Shadow
    protected abstract float getWaterSlowDown();

    /** MITE applies a constant 0.02 downward pull in water; vanilla derives it from baseGravity/16. */
    @ModifyVariable(method = "travelInWater", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double infx$useMiteWaterGravity(double baseGravity) {
        return (Object) this instanceof Player ? SwimPhysics.waterGravity(baseGravity) : baseGravity;
    }

    /**
     * MITE has no counter-current sprint model; vanilla's sprint-swim drag reduction otherwise lets a
     * sprinting player trivially out-swim MITE's current in any direction, including upstream.
     * {@code movement} (the multiply receiver) is this tick's already-computed thrust, so it doubles
     * as the direction proxy for how directly the player is swimming against the current.
     */
    @Redirect(
            method = "travelInWater",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 infx$antiCurrentSprintDrag(Vec3 movement, double x, double y, double z) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Player) || !self.isSprinting()) {
            return movement.multiply(x, y, z);
        }
        double blended = SwimPhysics.antiCurrentSprintDrag(self, (float) x, this.getWaterSlowDown(), movement);
        return movement.multiply(blended, y, blended);
    }

    /**
     * Vanilla intentionally omits {@code baseGravity / 16} while sprinting. That makes MITE's
     * 7/16 waterfall jump impulse positive after drag, so reapply the same MITE water pull only
     * when both the player's feet and head are in the falling column.
     */
    @Redirect(
            method = "travelInWater",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/LivingEntity;getFluidFallingAdjustedMovement(DZLnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 infx$restoreWaterfallGravityForSprinting(
            LivingEntity entity, double baseGravity, boolean isFalling, Vec3 movement) {
        Vec3 adjusted = entity.getFluidFallingAdjustedMovement(baseGravity, isFalling, movement);
        if (entity instanceof Player player && player.isSprinting()) {
            return SwimPhysics.applyFallingWaterSprintGravity(player, baseGravity, adjusted);
        }
        return adjusted;
    }

    @Redirect(
            method = "aiStep",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/LivingEntity;jumpInFluid(Lnet/neoforged/neoforge/fluids/FluidType;)V"))
    private void infx$useMiteSwimUp(LivingEntity entity, FluidType type) {
        if (entity instanceof Player player && type == NeoForgeMod.WATER_TYPE.value()) {
            SwimPhysics.swimUp(player);
        } else {
            entity.jumpInFluid(type);
        }
    }
}
