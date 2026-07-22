package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.network.R196Network;
import com.pixulse.infx.world.RunegateTeleportation;
import com.pixulse.infx.world.Underworld;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

/** Recreates MITE's rune-gate full-screen fade and client-timed execution signal. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class RunegateAnimation {
    private static final int FINISHED_COUNTER = 30;
    private static final int OVERWORLD_RGB = 0x359FFF;
    private static final int UNDERWORLD_RGB = 0x4401B4;
    private static final int NETHER_RGB = 0xE47B4E;
    private static final int FALLBACK_RGB = 0xFFFFFF;

    private static boolean teleporting;
    private static int counter;

    private RunegateAnimation() {}

    @SubscribeEvent
    private static void registerPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(R196Network.RunegateStartPayload.TYPE, (payload, context) -> start());
        event.register(R196Network.RunegateFinishedPayload.TYPE, (payload, context) -> finish());
    }

    @SubscribeEvent
    private static void tick(ClientTickEvent.Post event) {
        if (teleporting) {
            counter = nextCounter(true, counter);
            if (counter == RunegateTeleportation.LOADING_TICKS) {
                ClientPacketDistributor.sendToServer(R196Network.RunegateExecutePayload.INSTANCE);
            }
        } else if (counter > 0) {
            counter = nextCounter(false, counter);
        }
    }

    @SubscribeEvent
    private static void render(RenderGuiEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (counter <= 0 || minecraft.level == null) {
            return;
        }

        var graphics = event.getGuiGraphics();
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), overlayColor(minecraft.level.dimension(), counter));
    }

    @SubscribeEvent
    private static void clear(ClientPlayerNetworkEvent.LoggingOut event) {
        teleporting = false;
        counter = 0;
    }

    static int nextCounter(boolean isTeleporting, int currentCounter) {
        if (!isTeleporting) {
            return Math.max(0, currentCounter - 1);
        }
        int next = currentCounter + 1;
        return next > RunegateTeleportation.LOADING_TICKS ? FINISHED_COUNTER : next;
    }

    static int overlayColor(ResourceKey<Level> dimension, int currentCounter) {
        int clamped = Math.clamp(currentCounter, 0, RunegateTeleportation.LOADING_TICKS);
        int alpha = Math.round(255.0F * clamped / RunegateTeleportation.LOADING_TICKS);
        return alpha << 24 | colorFor(dimension);
    }

    private static int colorFor(ResourceKey<Level> dimension) {
        if (dimension.equals(Level.OVERWORLD)) {
            return OVERWORLD_RGB;
        }
        if (dimension.equals(Underworld.LEVEL)) {
            return UNDERWORLD_RGB;
        }
        if (dimension.equals(Level.NETHER)) {
            return NETHER_RGB;
        }
        return FALLBACK_RGB;
    }

    private static void start() {
        teleporting = true;
        counter = 0;
    }

    private static void finish() {
        teleporting = false;
        counter = FINISHED_COUNTER;
    }
}
