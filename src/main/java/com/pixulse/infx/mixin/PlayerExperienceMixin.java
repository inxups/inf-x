package com.pixulse.infx.mixin;

import com.pixulse.infx.progression.R196Experience;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Changes the vanilla XP step because NeoForge exposes XP changes but not this curve calculation. */
@Mixin(Player.class)
public abstract class PlayerExperienceMixin {
    @Inject(method = "getXpNeededForNextLevel", at = @At("HEAD"), cancellable = true)
    private void infx$r196ExperienceStep(CallbackInfoReturnable<Integer> callback) {
        callback.setReturnValue(R196Experience.pointsToNextLevel(((Player) (Object) this).experienceLevel));
    }
}
