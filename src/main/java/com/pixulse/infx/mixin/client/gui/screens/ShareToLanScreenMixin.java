package com.pixulse.infx.mixin.client.gui.screens;

import com.pixulse.infx.InfiniteXDevMode;
import com.pixulse.infx.client.ClientLanPolicy;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Restricts the Share-to-LAN "Allow Commands" toggle to dev mode: outside dev mode the toggle is
 * forced off and disabled (with a tooltip), so a LAN world can never be published with cheats.
 * The game-mode selector is unaffected; only the Allow Commands button, identified by its label,
 * is modified.
 */
@Mixin(ShareToLanScreen.class)
public abstract class ShareToLanScreenMixin {
    private static final Component DEV_MODE_ONLY_TOOLTIP =
            Component.literal("INFX: allow commands requires dev mode");

    @Shadow private boolean commands;

    @Shadow private static Component ALLOW_COMMANDS_LABEL;

    @Inject(method = "init", at = @At("HEAD"))
    private void infx$forceAllowCommandsOffOutsideDevMode(CallbackInfo callbackInfo) {
        if (!ClientLanPolicy.allowsLanCommands(InfiniteXDevMode.isClientEnabled())) {
            this.commands = false;
        }
    }

    @Redirect(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/CycleButton$Builder;create(IIIILnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/CycleButton$OnValueChange;)Lnet/minecraft/client/gui/components/CycleButton;"))
    private CycleButton<Boolean> infx$disableAllowCommandsOutsideDevMode(
            CycleButton.Builder<Boolean> builder,
            int x,
            int y,
            int width,
            int height,
            Component name,
            CycleButton.OnValueChange<Boolean> onValueChange) {
        CycleButton<Boolean> button = builder.create(x, y, width, height, name, onValueChange);
        if (name == ALLOW_COMMANDS_LABEL && !ClientLanPolicy.allowsLanCommands(InfiniteXDevMode.isClientEnabled())) {
            button.active = false;
            button.setTooltip(Tooltip.create(DEV_MODE_ONLY_TOOLTIP));
        }
        return button;
    }
}
