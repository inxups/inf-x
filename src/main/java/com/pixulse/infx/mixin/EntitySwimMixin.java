package com.pixulse.infx.mixin;

import com.pixulse.infx.world.SwimPhysics;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * InfX World.handleMaterialAcceleration normalizes the summed flow of every water block touching the
 * entity, so the push is a constant 0.014/tick regardless of depth or block count. Vanilla 26.2
 * instead averages the flow for players, tapers it below 0.4 fluid height and clamps a minimum
 * magnitude. Players use the InfX form; other entities keep vanilla behaviour.
 */
@Mixin(Entity.class)
abstract class EntitySwimMixin {
    @Redirect(
            method = "updateFluidInteraction",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/EntityFluidInteraction;applyCurrentTo(Lnet/minecraft/tags/TagKey;Lnet/minecraft/world/entity/Entity;D)V"))
    private void infx$applyWaterCurrent(
            EntityFluidInteraction interaction, TagKey<Fluid> fluid, Entity entity, double scale) {
        if (entity instanceof Player && FluidTags.WATER.equals(fluid)) {
            SwimPhysics.applyNormalizedCurrent(entity, scale);
        } else {
            interaction.applyCurrentTo(fluid, entity, scale);
        }
    }
}
