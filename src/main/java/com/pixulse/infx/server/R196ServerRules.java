package com.pixulse.infx.server;

import com.pixulse.infx.InfiniteX;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Dedicated-server-only chat throttling, metrics and reconnect restrictions. */
public final class R196ServerRules {
    public static final int CHAT_INCREMENT = 20;
    public static final int CHAT_THRESHOLD = 200;
    public static final int PERFORMANCE_INTERVAL = 1_000;
    private static final Map<UUID, Integer> CHAT_SCORES = new HashMap<>();
    private static final Set<UUID> DENIED_LOGINS = new HashSet<>();

    private R196ServerRules() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(R196ServerRules::onChat);
        gameBus.addListener(R196ServerRules::onServerTick);
        gameBus.addListener(R196ServerRules::onLogin);
        gameBus.addListener(R196ServerRules::onLogout);
    }

    private static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        MinecraftServer server = player.level().getServer();
        if (!server.isDedicatedServer() || server.getPlayerList().isOp(player.nameAndId())) return;
        int score = chatScoreAfterMessage(CHAT_SCORES.getOrDefault(player.getUUID(), 0));
        CHAT_SCORES.put(player.getUUID(), score);
        if (score > CHAT_THRESHOLD) {
            event.setCanceled(true);
            player.connection.disconnect(Component.translatable("disconnect.infx.chat_spam"));
        }
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (!server.isDedicatedServer()) return;
        Iterator<Map.Entry<UUID, Integer>> scores = CHAT_SCORES.entrySet().iterator();
        while (scores.hasNext()) {
            Map.Entry<UUID, Integer> score = scores.next();
            int decayed = decayChatScore(score.getValue());
            if (decayed == 0) scores.remove();
            else score.setValue(decayed);
        }
        if (server.getTickCount() % PERFORMANCE_INTERVAL == 0) {
            InfiniteX.LOGGER.info("R196 performance: {}", R196ServerMetrics.formatLoad(server));
        }
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MinecraftServer server = player.level().getServer();
        if (!server.isDedicatedServer()) return;
        R196ReconnectData data = R196ReconnectData.get(server);
        data.restriction(player.getUUID()).ifPresent(restriction -> {
            long gameTime = server.overworld().getGameTime();
            long dayTime = server.overworld().getOverworldClockTime();
            if (restriction.mayReconnect(gameTime, dayTime)) {
                data.clear(player.getUUID());
                return;
            }
            DENIED_LOGINS.add(player.getUUID());
            player.connection.disconnect(Component.translatable(
                    "disconnect.infx.reconnect_limited",
                    restriction.adjustedLogoutHour(),
                    restriction.minimumRemainingTicks(gameTime) / 20L));
        });
    }

    private static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MinecraftServer server = player.level().getServer();
        CHAT_SCORES.remove(player.getUUID());
        if (!server.isDedicatedServer()) return;
        R196ReconnectData data = R196ReconnectData.get(server);
        if (DENIED_LOGINS.remove(player.getUUID())) return;
        if (player.isDeadOrDying() || player.isSleeping()) {
            data.clear(player.getUUID());
            return;
        }
        data.restrict(
                player.getUUID(),
                server.overworld().getGameTime(),
                server.overworld().getOverworldClockTime());
    }

    public static int chatScoreAfterMessage(int current) {
        return Math.max(0, current) + CHAT_INCREMENT;
    }

    public static int decayChatScore(int current) {
        return Math.max(0, current - 1);
    }
}
