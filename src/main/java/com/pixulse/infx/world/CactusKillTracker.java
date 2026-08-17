package com.pixulse.infx.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.InfiniteX;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Per-dimension MITE cactus kill counts, keyed by the supporting sand block. */
public final class CactusKillTracker extends SavedData {
    public static final String CONTACT_POS_KEY = "infx.cactus_contact_pos";
    public static final String CONTACT_TICK_KEY = "infx.cactus_contact_tick";
    public static final int MAX_KILLS = 127;

    private static final Codec<Map<String, Integer>> COUNTS = Codec.unboundedMap(Codec.STRING, Codec.INT);
    private static final Codec<CactusKillTracker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    COUNTS.optionalFieldOf("counts", Map.of()).forGetter(data -> data.counts))
            .apply(instance, CactusKillTracker::new));
    public static final SavedDataType<CactusKillTracker> TYPE =
            new SavedDataType<>(InfiniteX.id("infx_cactus_kills"), CactusKillTracker::new, CODEC);

    private final Map<String, Integer> counts;

    public CactusKillTracker() {
        this(Map.of());
    }

    private CactusKillTracker(Map<String, Integer> counts) {
        this.counts = new HashMap<>(counts);
    }

    public static CactusKillTracker get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public static void recordContact(Entity entity, BlockPos cactusPos, long gameTime) {
        entity.getPersistentData().putLong(CONTACT_POS_KEY, cactusPos.asLong());
        entity.getPersistentData().putLong(CONTACT_TICK_KEY, gameTime);
    }

    /** Only accepts the cactus interaction that caused damage during this exact server tick. */
    public static Optional<BlockPos> contactFor(Entity entity, long gameTime) {
        if (entity.getPersistentData().getLongOr(CONTACT_TICK_KEY, Long.MIN_VALUE) != gameTime) {
            return Optional.empty();
        }
        return entity.getPersistentData().getLong(CONTACT_POS_KEY).map(BlockPos::of);
    }

    public static Optional<BlockPos> baseSand(Level level, BlockPos cactusPos) {
        BlockPos.MutableBlockPos cursor = cactusPos.mutable().move(0, -1, 0);
        while (level.getBlockState(cursor).is(Blocks.CACTUS)) {
            cursor.move(0, -1, 0);
        }
        return level.getBlockState(cursor).is(Blocks.SAND) ? Optional.of(cursor.immutable()) : Optional.empty();
    }

    public int count(BlockPos sandPos) {
        return counts.getOrDefault(key(sandPos), 0);
    }

    public int countForCactus(ServerLevel level, BlockPos cactusPos) {
        return baseSand(level, cactusPos).map(this::count).orElse(0);
    }

    public void incrementForCactus(ServerLevel level, BlockPos cactusPos) {
        baseSand(level, cactusPos).ifPresent(this::increment);
    }

    public void decrementForCactus(ServerLevel level, BlockPos cactusPos) {
        baseSand(level, cactusPos).ifPresent(this::decrement);
    }

    public void increment(BlockPos sandPos) {
        String key = key(sandPos);
        int updated = Math.min(MAX_KILLS, count(sandPos) + 1);
        counts.put(key, updated);
        setDirty();
    }

    public void decrement(BlockPos sandPos) {
        String key = key(sandPos);
        int current = count(sandPos);
        if (current <= 1) {
            if (counts.remove(key) != null) {
                setDirty();
            }
            return;
        }
        counts.put(key, current - 1);
        setDirty();
    }

    public void clear(BlockPos sandPos) {
        if (counts.remove(key(sandPos)) != null) {
            setDirty();
        }
    }

    private static String key(BlockPos pos) {
        return Long.toString(pos.asLong());
    }
}
