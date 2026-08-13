package com.pixulse.infx.mixin.world.entity.player;

import com.pixulse.infx.registry.InfXAttachments;
import com.pixulse.infx.data.food.SurvivalRules;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bridges vanilla FoodData gates to INFX's independent energy layers. The eat gate follows the
 * displayed food bar: Nutrition below the fixed cap means the bar is not full and eating is
 * permitted; Satiation stays an internal buffer that is never shown directly.
 */
@Mixin(Player.class)
abstract class PlayerFoodMixin {
    @Inject(method = "canEat", at = @At("HEAD"), cancellable = true)
    private void infx$useNutritionCap(boolean ignoreHunger, CallbackInfoReturnable<Boolean> callback) {
        Player player = (Player) (Object) this;
        var survival = player.getData(InfXAttachments.SURVIVAL);
        callback.setReturnValue(ignoreHunger || survival.nutrition() < SurvivalRules.MAX_CAP);
    }

    /** INFX requires Nutrition for sprinting; Satiation alone only powers other actions. */
    @Inject(method = "hasEnoughFoodToDoExhaustiveManoeuvres", at = @At("HEAD"), cancellable = true)
    private void infx$useR196EnergyForExhaustiveManoeuvres(CallbackInfoReturnable<Boolean> callback) {
        Player player = (Player) (Object) this;
        callback.setReturnValue(
                player.getAbilities().mayfly
                        || player.getData(InfXAttachments.SURVIVAL).hasNutritionForSprinting());
    }
}
