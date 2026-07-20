package com.pixulse.infx.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** FoodData exposes no public hook for replacing its coupled exhaustion, healing and starvation tick. */
@Mixin(FoodData.class)
abstract class FoodDataMixin {
    @Shadow private float exhaustionLevel;

    @Shadow private int tickTimer;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void infx$useR196FoodTick(ServerPlayer player, CallbackInfo callback) {
        // R196SurvivalEvents owns both energy layers, recovery and starvation.
        this.exhaustionLevel = 0.0F;
        this.tickTimer = 0;
        callback.cancel();
    }
}
