package com.pixulse.infx.agriculture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.InfiniteX;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Persistent per-dimension crop disease, fertility, drought and offline bookkeeping. */
public final class R196AgricultureData extends SavedData {
    private static final Codec<Map<String, Long>> POSITION_TIMES =
            Codec.unboundedMap(Codec.STRING, Codec.LONG);
    private static final Codec<R196AgricultureData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    POSITION_TIMES.optionalFieldOf("infected", Map.of()).forGetter(data -> data.infected),
                    POSITION_TIMES.optionalFieldOf("fertile", Map.of()).forGetter(data -> data.fertile),
                    POSITION_TIMES.optionalFieldOf("dry_since", Map.of()).forGetter(data -> data.drySince),
                    POSITION_TIMES.optionalFieldOf("tracked", Map.of()).forGetter(data -> data.tracked),
                    POSITION_TIMES.optionalFieldOf("artificial_logs", Map.of()).forGetter(data -> data.artificialLogs),
                    Codec.LONG.optionalFieldOf("last_wall_clock", 0L).forGetter(data -> data.lastWallClock))
            .apply(instance, R196AgricultureData::new));
    public static final SavedDataType<R196AgricultureData> TYPE = new SavedDataType<>(
            InfiniteX.id("r196_agriculture"), R196AgricultureData::new, CODEC);

    private final Map<String, Long> infected;
    private final Map<String, Long> fertile;
    private final Map<String, Long> drySince;
    private final Map<String, Long> tracked;
    private final Map<String, Long> artificialLogs;
    private long lastWallClock;
    private transient boolean offlineCalculated;
    private transient long pendingOfflineMillis;
    private transient Set<String> pendingOfflineCrops = Set.of();
    private transient int pendingOfflineStages;

    public R196AgricultureData() {
        this(Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), System.currentTimeMillis());
    }

    private R196AgricultureData(
            Map<String, Long> infected,
            Map<String, Long> fertile,
            Map<String, Long> drySince,
            Map<String, Long> tracked,
            Map<String, Long> artificialLogs,
            long lastWallClock) {
        this.infected = new HashMap<>(infected);
        this.fertile = new HashMap<>(fertile);
        this.drySince = new HashMap<>(drySince);
        this.tracked = new HashMap<>(tracked);
        this.artificialLogs = new HashMap<>(artificialLogs);
        this.lastWallClock = lastWallClock;
    }

    public static R196AgricultureData get(ServerLevel level) {
        R196AgricultureData data = level.getDataStorage().computeIfAbsent(TYPE);
        if (!data.offlineCalculated) {
            long now = System.currentTimeMillis();
            data.pendingOfflineMillis = Math.max(0L, now - data.lastWallClock);
            data.pendingOfflineCrops = new HashSet<>(data.tracked.keySet());
            data.lastWallClock = now;
            data.offlineCalculated = true;
            data.setDirty();
        }
        return data;
    }

    public boolean isInfected(BlockPos pos) {
        return infected.containsKey(key(pos));
    }

    public long infectedSince(BlockPos pos) {
        return infected.getOrDefault(key(pos), 0L);
    }

    public void infect(BlockPos pos, long gameTime) {
        if (infected.putIfAbsent(key(pos), gameTime) == null) setDirty();
    }

    public boolean cure(BlockPos pos) {
        boolean changed = infected.remove(key(pos)) != null;
        if (changed) setDirty();
        return changed;
    }

    public boolean isFertile(BlockPos farmland) {
        return fertile.containsKey(key(farmland));
    }

    public boolean fertilize(BlockPos farmland, long gameTime) {
        boolean fresh = fertile.put(key(farmland), gameTime) == null;
        setDirty();
        return fresh;
    }

    public void track(BlockPos crop, long gameTime) {
        tracked.put(key(crop), gameTime);
        setDirty();
    }

    public void setDry(BlockPos crop, long gameTime) {
        if (drySince.putIfAbsent(key(crop), gameTime) == null) setDirty();
    }

    public void clearDry(BlockPos crop) {
        if (drySince.remove(key(crop)) != null) setDirty();
    }

    public long drySince(BlockPos crop) {
        return drySince.getOrDefault(key(crop), 0L);
    }

    public Map<String, Long> tracked() {
        return tracked;
    }

    public void remove(BlockPos crop) {
        String key = key(crop);
        if (tracked.remove(key) != null | infected.remove(key) != null | drySince.remove(key) != null) {
            setDirty();
        }
    }

    public int consumeOfflineStages(BlockPos crop, boolean singleplayer, long stageMillis, int maximumStages) {
        if (pendingOfflineMillis > 0L) {
            pendingOfflineStages = offlineStages(
                    pendingOfflineMillis, singleplayer, stageMillis, maximumStages);
            pendingOfflineMillis = 0L;
            if (!singleplayer || pendingOfflineStages == 0) {
                pendingOfflineCrops = Set.of();
            }
        }
        return pendingOfflineCrops.remove(key(crop)) ? pendingOfflineStages : 0;
    }

    static int offlineStages(long offlineMillis, boolean singleplayer, long stageMillis, int maximumStages) {
        if (!singleplayer || offlineMillis <= 0L || stageMillis <= 0L || maximumStages <= 0) return 0;
        return (int) Math.min(maximumStages, offlineMillis / stageMillis);
    }

    public void checkpointWallClock() {
        lastWallClock = System.currentTimeMillis();
        setDirty();
    }

    public void markArtificialLog(BlockPos pos, long gameTime) {
        artificialLogs.put(key(pos), gameTime);
        setDirty();
    }

    public boolean isArtificialLog(BlockPos pos) {
        return artificialLogs.containsKey(key(pos));
    }

    public void removeArtificialLog(BlockPos pos) {
        if (artificialLogs.remove(key(pos)) != null) setDirty();
    }

    static String key(BlockPos pos) {
        return Long.toString(pos.asLong());
    }
}
