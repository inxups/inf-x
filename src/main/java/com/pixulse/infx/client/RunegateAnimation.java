package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.network.Network;
import com.pixulse.infx.world.RunegateTeleportation;
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

    private static boolean teleporting;
    private static int counter;

    private RunegateAnimation() {}

    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(Network.RunegateStartPayload.TYPE, (payload, context) -> start());
        event.register(Network.RunegateFinishedPayload.TYPE, (payload, context) -> finish());
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (teleporting) {
            counter = nextCounter(true, counter);
            if (counter == RunegateTeleportation.LOADING_TICKS) {
                ClientPacketDistributor.sendToServer(Network.RunegateExecutePayload.INSTANCE);
            }
        } else if (counter > 0) {
            counter = nextCounter(false, counter);
        }
    }

    @SubscribeEvent
    public static void render(RenderGuiEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (counter <= 0 || minecraft.level == null) {
            return;
        }

        var graphics = event.getGuiGraphics();
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), overlayColor(minecraft.level.dimension(), counter));
    }

    @SubscribeEvent
    public static void clear(ClientPlayerNetworkEvent.LoggingOut event) {
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
        return PortalDestinationColors.rgbFor(dimension);
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
