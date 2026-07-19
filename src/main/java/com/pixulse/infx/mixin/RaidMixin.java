package com.pixulse.infx.mixin;

import com.pixulse.infx.server.ExtremeDifficulty;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.raid.Raid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Supplies the vanilla raid switch with a safe Extreme branch. */
@Mixin(Raid.class)
public abstract class RaidMixin {
    @Inject(method = "getNumGroups", at = @At("HEAD"), cancellable = true)
    private void infx$extremeRaidGroups(Difficulty difficulty, CallbackInfoReturnable<Integer> callback) {
        if (ExtremeDifficulty.isExtreme(difficulty)) callback.setReturnValue(7);
    }
}
