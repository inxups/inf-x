package com.pixulse.infx.mixin.server.level;

import com.pixulse.infx.InfiniteXTestMode;
import com.pixulse.infx.server.ServerTestModePolicy;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Outside test mode every player permission lookup resolves to NO_PERMISSIONS, so commands, game
 * rules, JEI cheat mode and other permission-gated features always see a player without any
 * rights. GameTest servers keep vanilla behavior so automated tests are unaffected.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerPermissionsMixin {
    @Inject(method = "permissions", at = @At("HEAD"), cancellable = true)
    private void restrictPlayerPermissionsOutsideTestMode(
            CallbackInfoReturnable<PermissionSet> callback) {
        if (!ServerTestModePolicy.allowsPlayerPermissions(InfiniteXTestMode.isEnabled())
                && !(((ServerPlayer) (Object) this).level().getServer() instanceof GameTestServer)) {
            callback.setReturnValue(PermissionSet.NO_PERMISSIONS);
        }
    }
}
