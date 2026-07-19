package com.pixulse.infx.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** FoodData exposes no event for replacing only vanilla natural regeneration. */
@Mixin(FoodData.class)
abstract class FoodDataMixin {
    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;heal(F)V"))
    private void infx$useR196NaturalRegeneration(ServerPlayer player, float amount) {
        // R196SurvivalEvents advances the 62.5-second nutrition-driven recovery clock.
    }
}
