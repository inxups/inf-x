package com.pixulse.infx.mixin;

import com.pixulse.infx.registry.InfXAttachments;
import com.pixulse.infx.food.SurvivalRules;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Bridges vanilla FoodData gates to R196's independent energy layers and level-scaled cap. */
@Mixin(Player.class)
abstract class PlayerFoodMixin {
    @Inject(method = "canEat", at = @At("HEAD"), cancellable = true)
    private void infx$useNutritionCap(boolean ignoreHunger, CallbackInfoReturnable<Boolean> callback) {
        Player player = (Player) (Object) this;
        var survival = player.getData(InfXAttachments.SURVIVAL);
        double cap = SurvivalRules.foodCap(player.experienceLevel);
        callback.setReturnValue(ignoreHunger || survival.satiation() < cap || survival.nutrition() < cap);
    }

    /** MITE allows sprinting while either Satiation or Nutrition remains above zero. */
    @Inject(method = "hasEnoughFoodToDoExhaustiveManoeuvres", at = @At("HEAD"), cancellable = true)
    private void infx$useR196EnergyForExhaustiveManoeuvres(CallbackInfoReturnable<Boolean> callback) {
        Player player = (Player) (Object) this;
        callback.setReturnValue(
                player.getAbilities().mayfly || player.getData(InfXAttachments.SURVIVAL).hasFoodEnergy());
    }
}
