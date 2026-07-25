package com.pixulse.infx.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.InfiniteX;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * MITE World#scheduleBlockChange for bucket pours. A poured cell is written as a source so it spreads
 * once, then degrades to flowing after the material's delay unless the player paid the source cost.
 * Persisted so a save/quit cannot strand a permanent free source.
 */
public final class R196FluidDecayData extends SavedData {
    private static final Codec<Map<String, Long>> DUE_TIMES = Codec.unboundedMap(Codec.STRING, Codec.LONG);
    private static final Codec<R196FluidDecayData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    DUE_TIMES.optionalFieldOf("water", Map.of()).forGetter(data -> data.water),
                    DUE_TIMES.optionalFieldOf("lava", Map.of()).forGetter(data -> data.lava))
            .apply(instance, R196FluidDecayData::new));
    public static final SavedDataType<R196FluidDecayData> TYPE =
            new SavedDataType<>(InfiniteX.id("r196_fluid_decay"), R196FluidDecayData::new, CODEC);

    private final Map<String, Long> water;
    private final Map<String, Long> lava;

    public R196FluidDecayData() {
        this(Map.of(), Map.of());
    }

    private R196FluidDecayData(Map<String, Long> water, Map<String, Long> lava) {
        this.water = new HashMap<>(water);
        this.lava = new HashMap<>(lava);
    }

    public static R196FluidDecayData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    private static String key(BlockPos pos) {
        return Long.toString(pos.asLong());
    }

    public void schedule(BlockPos pos, boolean isLava, long dueTick) {
        (isLava ? lava : water).put(key(pos), dueTick);
        setDirty();
    }

    /** Cancels a pending degrade, used when something else already claimed the cell. */
    public void cancel(BlockPos pos) {
        boolean removed = water.remove(key(pos)) != null;
        removed |= lava.remove(key(pos)) != null;
        if (removed) {
            setDirty();
        }
    }

    public int pending() {
        return water.size() + lava.size();
    }

    /** Applies every degrade whose delay has elapsed and whose chunk is loaded. */
    public void tick(ServerLevel level) {
        if (water.isEmpty() && lava.isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        boolean changed = degrade(level, water, Fluids.WATER, now);
        changed |= degrade(level, lava, Fluids.LAVA, now);
        if (changed) {
            setDirty();
        }
    }

    private static boolean degrade(ServerLevel level, Map<String, Long> pending, FlowingFluid fluid, long now) {
        if (pending.isEmpty()) {
            return false;
        }
        List<String> due = new ArrayList<>();
        for (Map.Entry<String, Long> entry : pending.entrySet()) {
            if (entry.getValue() <= now) {
                due.add(entry.getKey());
            }
        }
        boolean changed = false;
        for (String encoded : due) {
            BlockPos pos = BlockPos.of(Long.parseLong(encoded));
            // Leave the entry queued while the chunk sleeps so the degrade is not silently lost.
            if (!level.isLoaded(pos)) {
                continue;
            }
            pending.remove(encoded);
            changed = true;
            BlockState state = level.getBlockState(pos);
            // Only a pure liquid cell degrades. A waterlogged block also reports a water source, and
            // replacing it would destroy the block a stale entry happens to land on.
            if (state.getBlock() instanceof LiquidBlock
                    && state.getFluidState().is(fluid)
                    && state.getFluidState().isSource()) {
                level.setBlock(pos, fluid.getFlowing(1, false).createLegacyBlock(), 3);
            }
        }
        return changed;
    }
}
