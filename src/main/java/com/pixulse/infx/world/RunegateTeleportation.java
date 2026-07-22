package com.pixulse.infx.world;

import com.pixulse.infx.network.R196Network;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Coordinates MITE's client-led rune-gate fade before committing the teleport. */
public final class RunegateTeleportation {
    public static final int LOADING_TICKS = 20;

    private static final Map<UUID, PendingTeleport> PENDING = new HashMap<>();

    private RunegateTeleportation() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(RunegateTeleportation::onLogout);
        gameBus.addListener(RunegateTeleportation::onClone);
        gameBus.addListener(RunegateTeleportation::onDimensionChanged);
        gameBus.addListener(RunegateTeleportation::onServerStopping);
    }

    /** Starts one client animation for a player unless that player is riding or carrying another entity. */
    public static boolean start(ServerPlayer player, TeleportTransition transition) {
        if (player.isPassenger() || player.isVehicle()) {
            return false;
        }

        UUID playerId = player.getUUID();
        if (PENDING.containsKey(playerId)) {
            return true;
        }

        PENDING.put(
                playerId,
                new PendingTeleport(player.level(), transition));
        PacketDistributor.sendToPlayer(player, R196Network.RunegateStartPayload.INSTANCE);
        return true;
    }

    /** Executes only the transition that was previously started for this exact player. */
    public static void execute(ServerPlayer player) {
        PendingTeleport pending = PENDING.get(player.getUUID());
        if (pending == null) {
            return;
        }

        PENDING.remove(player.getUUID());
        if (player.level() != pending.sourceLevel()) {
            finish(player);
            return;
        }

        if (player.teleport(pending.transition()) != null) {
            finish(player);
        }
    }

    private static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PENDING.remove(player.getUUID());
        }
    }

    private static void onClone(PlayerEvent.Clone event) {
        if (!(event.getOriginal() instanceof ServerPlayer original)
                || PENDING.remove(original.getUUID()) == null
                || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        finish(player);
    }

    private static void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && PENDING.remove(player.getUUID()) != null) {
            finish(player);
        }
    }

    private static void onServerStopping(ServerStoppingEvent event) {
        PENDING.clear();
    }

    private static void finish(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, R196Network.RunegateFinishedPayload.INSTANCE);
    }

    private record PendingTeleport(ServerLevel sourceLevel, TeleportTransition transition) {}
}
