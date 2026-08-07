package com.pixulse.infx.event.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.data.nightwing.NightwingDimming;
import com.pixulse.infx.registry.InfXAttachments;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/** Client half of InfX's Nightwing black-screen vision dimming. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class NightwingClientEvents {
    private static final float DIMMING_DECAY_PER_TICK = 0.01F;
    private static final float MAX_OVERLAY_ALPHA = 0.9F;

    private static float visionDimming;
    private static long receivedRevision = Long.MIN_VALUE;

    private NightwingClientEvents() {}

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        NightwingDimming dimming = player.getExistingDataOrNull(InfXAttachments.NIGHTWING_DIMMING.get());
        if (dimming != null && dimming.revision() != receivedRevision) {
            visionDimming = applyIncomingDimming(visionDimming, dimming.amount());
            receivedRevision = dimming.revision();
        }
        visionDimming = decayDimming(visionDimming);
    }

    @SubscribeEvent
    public static void render(RenderGuiEvent.Pre event) {
        float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float alpha = visionDimming - partialTicks * DIMMING_DECAY_PER_TICK;
        if (alpha <= DIMMING_DECAY_PER_TICK) {
            return;
        }
        alpha = Math.min(alpha, MAX_OVERLAY_ALPHA);
        var graphics = event.getGuiGraphics();
        graphics.fill(
                0,
                0,
                graphics.guiWidth(),
                graphics.guiHeight(),
                Math.round(alpha * 255.0F) << 24);
    }

    @SubscribeEvent
    public static void clear(ClientPlayerNetworkEvent.LoggingOut event) {
        visionDimming = 0.0F;
        receivedRevision = Long.MIN_VALUE;
    }

    static float applyIncomingDimming(float current, float incoming) {
        return Math.max(Math.max(0.0F, current), Math.max(0.0F, incoming));
    }

    static float decayDimming(float current) {
        if (current < DIMMING_DECAY_PER_TICK) {
            return 0.0F;
        }
        if (current > NightwingDimming.MAX_DIMMING) {
            return NightwingDimming.MAX_DIMMING;
        }
        return current - DIMMING_DECAY_PER_TICK;
    }
}
