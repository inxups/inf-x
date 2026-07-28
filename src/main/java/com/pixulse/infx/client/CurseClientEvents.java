package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.effect.curse.CurseManager;
import com.pixulse.infx.effect.curse.CurseStatus;
import com.pixulse.infx.effect.curse.CurseType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/** Client projection of realized curse state; pending curses are never visible here. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class CurseClientEvents {
    private static final int MAX_EDGE = 28;

    private CurseClientEvents() {}

    @SubscribeEvent
    private static void tick(ClientTickEvent.Post event) {
        var player = Minecraft.getInstance().player;
        if (player != null
                && player.isSprinting()
                && CurseManager.hasCurse(player, CurseType.CANNOT_RUN)) {
            player.setSprinting(false);
        }
    }

    @SubscribeEvent
    private static void renderVignette(RenderGuiEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !CurseManager.status(minecraft.player).active()) return;

        var graphics = event.getGuiGraphics();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int edge = Math.min(MAX_EDGE, Math.max(8, Math.min(width, height) / 10));
        graphics.fill(0, 0, width, height, 0x18000000);
        graphics.fillGradient(0, 0, width, edge, 0xB0000000, 0x00000000);
        graphics.fillGradient(0, height - edge, width, height, 0x00000000, 0xB0000000);
        graphics.fill(0, edge, edge / 2, height - edge, 0x60000000);
        graphics.fill(width - edge / 2, edge, width, height - edge, 0x60000000);
    }

    @SubscribeEvent
    private static void renderStatus(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        CurseStatus status = CurseManager.status(minecraft.player);
        if (!status.active()) return;

        Component detail = status.known() && status.type() != null
                ? status.type().title()
                : Component.translatable("curse.infx.unknown");
        event.getGuiGraphics().text(
                minecraft.font,
                Component.translatable("hud.infx.curse", detail),
                8,
                8,
                0xFFE0A8FF);
    }
}
