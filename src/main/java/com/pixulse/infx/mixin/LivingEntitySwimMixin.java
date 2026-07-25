package com.pixulse.infx.mixin;

import com.pixulse.infx.world.R196SwimPhysics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
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
    /** MITE applies a constant 0.02 downward pull in water; vanilla derives it from baseGravity/16. */
    @ModifyVariable(method = "travelInWater", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double infx$useMiteWaterGravity(double baseGravity) {
        return (Object) this instanceof Player ? R196SwimPhysics.waterGravity(baseGravity) : baseGravity;
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
            R196SwimPhysics.swimUp(player);
        } else {
            entity.jumpInFluid(type);
        }
    }
}
