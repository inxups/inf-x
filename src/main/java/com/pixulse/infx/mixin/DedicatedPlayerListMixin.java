package com.pixulse.infx.mixin;

import com.pixulse.infx.InfiniteXTestMode;
import com.pixulse.infx.server.ServerTestModePolicy;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.players.NameAndId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps dedicated-server OP data inert and avoids creating a new empty ops list outside test mode. */
@Mixin(DedicatedPlayerList.class)
public abstract class DedicatedPlayerListMixin {
    @Inject(method = "canBypassPlayerLimit", at = @At("HEAD"), cancellable = true)
    private void disableOperatorLimitBypass(
            NameAndId nameAndId, CallbackInfoReturnable<Boolean> callback) {
        if (!ServerTestModePolicy.allowsPlayerLimitBypass(InfiniteXTestMode.isEnabled())) {
            callback.setReturnValue(false);
        }
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/dedicated/DedicatedPlayerList;saveOps()V"))
    private void skipInitialOpsFileOutsideTestMode(DedicatedPlayerList playerList) {
        boolean opsFileExists = playerList.getOps().getFile().exists();
        if (ServerTestModePolicy.shouldSaveOpsAtDedicatedStartup(InfiniteXTestMode.isEnabled(), opsFileExists)) {
            invokeSaveOps();
        }
    }

    @Invoker("saveOps")
    abstract void invokeSaveOps();
}
