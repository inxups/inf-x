package com.pixulse.infx.event.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.client.ExperienceHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Adds the debt level that vanilla's HUD intentionally suppresses at zero and below.
 *
 * <p>NeoForge 26.1 renders the HUD through {@code GuiLayerManager} layers; listening to the
 * EXPERIENCE_LEVEL layer's pre-phase reproduces the old {@code extractExperienceLevel} HEAD
 * injection, including the F1 hidden-HUD gating that the vanilla layer applies internally.
 */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class HudLayerEvents {
    private HudLayerEvents() {}

    @SubscribeEvent
    public static void extractDebtExperienceLevel(RenderGuiLayerEvent.Pre event) {
        if (!event.getName().equals(VanillaGuiLayers.EXPERIENCE_LEVEL)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null
                || minecraft.gameMode == null
                || !minecraft.gameMode.hasExperience()
                || !ExperienceHud.isDebtLevel(player.experienceLevel)
                || minecraft.options.hideGui) {
            return;
        }
        ExperienceHud.extractDebtLevel(event.getGuiGraphics(), minecraft.font, player.experienceLevel);
    }
}
