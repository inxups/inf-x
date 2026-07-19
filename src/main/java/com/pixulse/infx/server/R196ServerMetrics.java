package com.pixulse.infx.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;

/** Live server counters used by R196 performance logging and diagnostic commands. */
public final class R196ServerMetrics {
    private R196ServerMetrics() {}

    public static Snapshot snapshot(MinecraftServer server) {
        long entities = 0L;
        long mobs = 0L;
        long itemEntities = 0L;
        int activeChunks = 0;
        int loadedChunks = 0;
        for (ServerLevel level : server.getAllLevels()) {
            loadedChunks += level.getChunkSource().getLoadedChunksCount();
            var spawnState = level.getChunkSource().getLastSpawnState();
            if (spawnState != null) activeChunks += spawnState.getSpawnableChunkCount();
            for (var entity : level.getAllEntities()) {
                entities++;
                if (entity instanceof Mob) mobs++;
                if (entity instanceof ItemEntity) itemEntities++;
            }
        }
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1_048_576L;
        long maximumMemory = runtime.maxMemory() / 1_048_576L;
        double averageTickMillis = server.getAverageTickTimeNanos() / 1_000_000.0D;
        double smoothedTickMillis = server.getCurrentSmoothedTickTime();
        double loadPercent = Math.max(averageTickMillis, smoothedTickMillis) / 50.0D * 100.0D;
        return new Snapshot(
                averageTickMillis,
                smoothedTickMillis,
                loadPercent,
                server.getPlayerCount(),
                activeChunks,
                loadedChunks,
                entities,
                mobs,
                itemEntities,
                usedMemory,
                maximumMemory);
    }

    public static String formatLoad(MinecraftServer server) {
        Snapshot value = snapshot(server);
        return String.format(
                Locale.ROOT,
                "Tick avg %.2f ms (smoothed %.2f ms); load %.1f%%; players %d; chunks active/loaded %d/%d; entities %d; mobs %d; drops %d; memory %d/%d MiB",
                value.averageTickMillis(),
                value.smoothedTickMillis(),
                value.loadPercent(),
                value.players(),
                value.activeChunks(),
                value.loadedChunks(),
                value.entities(),
                value.mobs(),
                value.itemEntities(),
                value.usedMemoryMiB(),
                value.maximumMemoryMiB());
    }

    public static List<String> formatChunks(ServerPlayer player) {
        List<String> lines = new ArrayList<>();
        lines.add("Chunk " + player.chunkPosition() + ": "
                + player.level().getChunkSource().getChunkDebugData(player.chunkPosition()));
        for (ServerLevel level : player.level().getServer().getAllLevels()) {
            lines.add(level.dimension().identifier() + ": loaded "
                    + level.getChunkSource().getLoadedChunksCount());
        }
        return List.copyOf(lines);
    }

    public record Snapshot(
            double averageTickMillis,
            double smoothedTickMillis,
            double loadPercent,
            int players,
            int activeChunks,
            int loadedChunks,
            long entities,
            long mobs,
            long itemEntities,
            long usedMemoryMiB,
            long maximumMemoryMiB) {}
}
