package com.pixulse.infx.mixin.client;

import com.pixulse.infx.client.SlownessCamera;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * MITE-aligned: SLOWNESS scales down the mouse-look turn of the local player. The final
 * {@code player.turn} call in MouseHandler.turnPlayer is the single funnel for every look
 * branch (normal, scoping, cinematic), so scaling it matches MITE dividing its turn
 * multiplier. The bytecode owner of that call is {@link LocalPlayer} (turn is inherited
 * from {@code Entity}); no slowness passes through at factor 1.
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerCameraTurnMixin {
    @Redirect(
            method = "turnPlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void infx$slownessCameraTurn(LocalPlayer player, double xo, double yo) {
        float factor = SlownessCamera.slownessTurnFactor(player);
        player.turn(xo * factor, yo * factor);
    }
}
