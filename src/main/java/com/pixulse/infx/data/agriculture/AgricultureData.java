package com.pixulse.infx.data.agriculture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.InfiniteX;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Persistent MITE farmland fertility and artificial-log bookkeeping. */
public final class AgricultureData extends SavedData {
    private static final Codec<Map<String, Long>> POSITION_TIMES =
            Codec.unboundedMap(Codec.STRING, Codec.LONG);
    private static final Codec<AgricultureData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    POSITION_TIMES.optionalFieldOf("fertile", Map.of()).forGetter(data -> data.fertile),
                    POSITION_TIMES.optionalFieldOf("artificial_logs", Map.of()).forGetter(data -> data.artificialLogs))
            .apply(instance, AgricultureData::new));
    public static final SavedDataType<AgricultureData> TYPE = new SavedDataType<>(
            InfiniteX.id("r196_agriculture"), AgricultureData::new, CODEC);

    private final Map<String, Long> fertile;
    private final Map<String, Long> artificialLogs;

    public AgricultureData() {
        this(Map.of(), Map.of());
    }

    private AgricultureData(Map<String, Long> fertile, Map<String, Long> artificialLogs) {
        this.fertile = new HashMap<>(fertile);
        this.artificialLogs = new HashMap<>(artificialLogs);
    }

    public static AgricultureData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isFertile(BlockPos farmland) {
        return fertile.containsKey(key(farmland));
    }

    public boolean fertilize(BlockPos farmland, long gameTime) {
        boolean fresh = fertile.put(key(farmland), gameTime) == null;
        setDirty();
        return fresh;
    }

    /** MITE consumes the fertilized bit only after a successful crop growth roll. */
    public boolean consumeFertility(BlockPos farmland) {
        boolean consumed = fertile.remove(key(farmland)) != null;
        if (consumed) setDirty();
        return consumed;
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
