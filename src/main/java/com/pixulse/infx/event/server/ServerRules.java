package com.pixulse.infx.event.server;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Dedicated-server-only performance metrics logging. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class ServerRules {
    public static final int PERFORMANCE_INTERVAL = 1_000;

    private ServerRules() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (!server.isDedicatedServer()) return;
        if (server.getTickCount() % PERFORMANCE_INTERVAL == 0) {
            InfiniteX.LOGGER.info("R196 performance: {}", ServerMetrics.formatLoad(server));
        }
    }
}
