package com.pixulse.infx.mixin;

import com.pixulse.infx.client.ExperienceHud;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the debt level that vanilla's HUD intentionally suppresses at zero and below. */
@Mixin(Gui.class)
public abstract class GuiExperienceMixin {
    @Shadow @Final private Minecraft minecraft;

    /**
     * Vanilla renders the number only when it is positive. There is no public HUD hook for that
     * conditional, so inject alongside it and leave the positive-level path untouched.
     */
    @Inject(method = "extractExperienceLevel", at = @At("HEAD"))
    private void infx$extractDebtExperienceLevel(
            GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo callback) {
        LocalPlayer player = minecraft.player;
        if (player == null
                || minecraft.gameMode == null
                || !minecraft.gameMode.hasExperience()
                || !ExperienceHud.isDebtLevel(player.experienceLevel)) {
            return;
        }
        ExperienceHud.extractDebtLevel(graphics, minecraft.font, player.experienceLevel);
    }
}
