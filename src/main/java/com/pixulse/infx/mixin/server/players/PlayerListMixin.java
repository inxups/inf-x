package com.pixulse.infx.mixin.server.players;

import com.pixulse.infx.server.ServerDevModePolicy;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Disables every vanilla player operator source outside dev mode. */
@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(method = "isOp", at = @At("HEAD"), cancellable = true)
    private void disableOperatorsOutsideDevMode(
            NameAndId nameAndId, CallbackInfoReturnable<Boolean> callback) {
        if (!ServerDevModePolicy.allowsPlayerOperators(
                ServerDevModePolicy.effectiveDevMode(((PlayerList) (Object) this).getServer()))) {
            callback.setReturnValue(false);
        }
    }
}
