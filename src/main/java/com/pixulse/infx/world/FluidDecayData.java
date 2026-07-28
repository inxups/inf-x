package com.pixulse.infx.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.InfiniteX;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * MITE World#scheduleBlockChange for bucket pours. A poured cell is written as a source so it spreads
 * once, then degrades to flowing after the material's delay unless the player paid the source cost.
 * Persisted so a save/quit cannot strand a permanent free source.
 */
public final class FluidDecayData extends SavedData {
    private static final Codec<Map<String, Long>> DUE_TIMES = Codec.unboundedMap(Codec.STRING, Codec.LONG);
    private static final Codec<FluidDecayData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    DUE_TIMES.optionalFieldOf("water", Map.of()).forGetter(data -> data.water),
                    DUE_TIMES.optionalFieldOf("lava", Map.of()).forGetter(data -> data.lava))
            .apply(instance, FluidDecayData::new));
    public static final SavedDataType<FluidDecayData> TYPE =
            new SavedDataType<>(InfiniteX.id("r196_fluid_decay"), FluidDecayData::new, CODEC);

    private final Map<String, Long> water;
    private final Map<String, Long> lava;

    public FluidDecayData() {
        this(Map.of(), Map.of());
    }

    private FluidDecayData(Map<String, Long> water, Map<String, Long> lava) {
        this.water = new HashMap<>(water);
        this.lava = new HashMap<>(lava);
    }

    public static FluidDecayData get(ServerLevel level) {
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

    /** Handles an original scheduled fluid tick when it belongs to an unpaid R196 bucket pour. */
    public static boolean handleScheduledTick(ServerLevel level, BlockPos pos, Fluid scheduledFluid) {
        FluidDecayData data = level.getDataStorage().get(TYPE);
        if (data == null) {
            return false;
        }
        return data.handleScheduledTick(level, pos, scheduledFluid, level.getGameTime());
    }

    private boolean handleScheduledTick(ServerLevel level, BlockPos pos, Fluid scheduledFluid, long now) {
        boolean isLava = scheduledFluid.is(FluidTags.LAVA);
        if (!isLava && !scheduledFluid.is(FluidTags.WATER)) {
            return false;
        }

        Map<String, Long> pending = isLava ? lava : water;
        String encoded = key(pos);
        Long dueTick = pending.get(encoded);
        if (dueTick == null) {
            return false;
        }
        if (dueTick > now) {
            int delay = (int) Math.min(Integer.MAX_VALUE, dueTick - now);
            level.scheduleTick(pos, scheduledFluid, Math.max(1, delay));
            return false;
        }

        pending.remove(encoded);
        setDirty();
        BlockState state = level.getBlockState(pos);
        boolean matchingFluid = isLava
                ? state.getFluidState().is(FluidTags.LAVA)
                : state.getFluidState().is(FluidTags.WATER);
        // Only a pure liquid cell degrades. A waterlogged block also reports a source, and replacing
        // it would destroy a block that happened to inherit a stale scheduled entry.
        if (state.getBlock() instanceof LiquidBlock && matchingFluid && state.getFluidState().isSource()) {
            FlowingFluid fluid = isLava ? Fluids.LAVA : Fluids.WATER;
            level.setBlock(pos, fluid.getFlowing(1, false).createLegacyBlock(), 3);
            return true;
        }
        return false;
    }
}
