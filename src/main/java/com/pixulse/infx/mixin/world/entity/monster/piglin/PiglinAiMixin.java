package com.pixulse.infx.mixin.world.entity.monster.piglin;

import com.pixulse.infx.world.SpawnGate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * InfX hostile piglins: the Nether is not a trading post. Piglins are brain-driven with no
 * public extension point for their static AI helpers, so three minimal injections make every
 * player a valid unprovoked target (gold armor no longer pacifies them) and close both barter
 * paths (right-click handover and ground pickup). The kept gold ingot still drops on death via
 * vanilla's guaranteed offhand drop. Piglin-versus-piglin rules stay vanilla, and disabling
 * the single {@code mobs.piglinHostility} config gate restores full vanilla behavior.
 */
@Mixin(PiglinAi.class)
abstract class PiglinAiMixin {
    @Inject(method = "isWearingSafeArmor", at = @At("HEAD"), cancellable = true)
    private static void infx$goldNeverPacifiesPlayers(
            LivingEntity entity, CallbackInfoReturnable<Boolean> callback) {
        if (SpawnGate.isPiglinHostilityEnabled() && entity instanceof Player) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "canAdmire", at = @At("HEAD"), cancellable = true)
    private static void infx$noHandedOverBarter(
            Piglin piglin, ItemStack offered, CallbackInfoReturnable<Boolean> callback) {
        if (SpawnGate.isPiglinHostilityEnabled()) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "admireGoldItem", at = @At("HEAD"), cancellable = true)
    private static void infx$noPickedUpBarter(LivingEntity piglin, CallbackInfo callback) {
        if (SpawnGate.isPiglinHostilityEnabled()) {
            callback.cancel();
        }
    }
}
