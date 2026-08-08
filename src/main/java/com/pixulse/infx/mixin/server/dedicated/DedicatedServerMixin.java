package com.pixulse.infx.mixin.server.dedicated;

import com.pixulse.infx.InfiniteXTestMode;
import com.pixulse.infx.server.ServerTestModePolicy;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Restricts dedicated-server command entry points outside test mode. */
@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin {
    private static final Component MANAGEMENT_DISABLED =
            Component.translatable("message.infx.server_management_disabled");

    @Redirect(
            method = "initServer",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/dedicated/DedicatedServerProperties;enableRcon:Z"))
    private boolean enableRconOnlyInTestMode(DedicatedServerProperties properties) {
        return properties.enableRcon && ServerTestModePolicy.allowsServerManagement(InfiniteXTestMode.isEnabled());
    }

    @Redirect(
            method = "initServer",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/dedicated/DedicatedServerProperties;managementServerEnabled:Z"))
    private boolean enableJsonRpcOnlyInTestMode(DedicatedServerProperties properties) {
        return properties.managementServerEnabled
                && ServerTestModePolicy.allowsServerManagement(InfiniteXTestMode.isEnabled());
    }

    @Inject(method = "handleConsoleInput", at = @At("HEAD"), cancellable = true)
    private void rejectConsoleCommands(String msg, CommandSourceStack source, CallbackInfo callback) {
        if (!ServerTestModePolicy.allowsConsoleCommand(InfiniteXTestMode.isEnabled(), msg)) {
            callback.cancel();
        }
    }

    @Inject(method = "runCommand", at = @At("HEAD"), cancellable = true)
    private void rejectRemoteCommands(String command, CallbackInfoReturnable<String> callback) {
        if (!ServerTestModePolicy.allowsConsoleCommand(InfiniteXTestMode.isEnabled(), command)) {
            callback.setReturnValue(MANAGEMENT_DISABLED.getString());
        }
    }
}
