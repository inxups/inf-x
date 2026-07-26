package com.pixulse.infx.mixin;

import com.pixulse.infx.world.R196SwimPhysics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * MITE has no modern camera-directed sprint-swim lift. Leaving it active in a falling column lets
 * a player looking upward overcome the reduced MITE jump impulse and water pull.
 */
@Mixin(Player.class)
abstract class PlayerWaterfallSwimMixin {
    @Redirect(
            method = "travel",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/player/Player;isSwimming()Z"))
    private boolean infx$disableSprintSwimLiftInWaterfalls(Player player) {
        return player.isSwimming()
                && !(player.isSprinting() && R196SwimPhysics.isFallingWaterColumn(player));
    }
}
