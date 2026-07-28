package com.pixulse.infx.mixin;

import com.pixulse.infx.data.food.FoodIngestion;
import com.pixulse.infx.data.food.FoodProfiles;
import com.pixulse.infx.event.SurvivalEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * CakeBlock hard-codes slice count and feeds FoodData directly, with no item-use event for R196
 * nutrition. Adjust servings and bridge successful bites into the dual energy layers.
 */
@Mixin(CakeBlock.class)
abstract class CakeBlockMixin {
    /** CakeBlock otherwise calls only Player#canEat and loses R196's nutrient-deficit exception. */
    @Redirect(
            method = "eat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;canEat(Z)Z"))
    private static boolean infx$useR196CakeGate(Player player, boolean ignoredVanillaHunger) {
        return FoodIngestion.canIngest(player, FoodProfiles.cakeSlice());
    }

    @ModifyConstant(method = "eat", constant = @Constant(intValue = 6))
    private static int infx$sixServings(int vanillaLastBite) {
        return 5;
    }

    @Inject(method = "eat", at = @At("RETURN"))
    private static void infx$applyR196CakeNutrition(
            LevelAccessor level,
            BlockPos pos,
            BlockState state,
            Player player,
            CallbackInfoReturnable<InteractionResult> callback) {
        if (!callback.getReturnValue().consumesAction()
                || !(player instanceof ServerPlayer serverPlayer)
                || serverPlayer.isSpectator()) {
            return;
        }
        SurvivalEvents.applyFood(serverPlayer, Items.CAKE.getDefaultInstance());
    }
}
