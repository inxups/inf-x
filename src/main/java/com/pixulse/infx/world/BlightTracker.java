package com.pixulse.infx.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.InfiniteX;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Persists MITE blood-moon blight on vanilla crops. InfX row crops keep their own blighted
 * block-state property ({@code InfxCropBlock}); vanilla {@code CropBlock}s cannot carry one,
 * so their blight lives in this overworld-wide tracker.
 */
public final class BlightTracker extends SavedData {
    private static final Codec<Set<BlockPos>> BLIGHTED =
            Codec.list(BlockPos.CODEC).xmap(HashSet::new, List::copyOf);
    private static final Codec<BlightTracker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    BLIGHTED.optionalFieldOf("blighted", Set.of()).forGetter(tracker -> tracker.blighted))
            .apply(instance, BlightTracker::new));
    public static final SavedDataType<BlightTracker> TYPE = new SavedDataType<>(
            InfiniteX.id("infx_vanilla_crop_blight"), BlightTracker::new, CODEC);

    private final Set<BlockPos> blighted = new HashSet<>();

    public BlightTracker() {
        this(Set.of());
    }

    private BlightTracker(Set<BlockPos> blighted) {
        this.blighted.addAll(blighted);
    }

    public static BlightTracker get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isBlighted(BlockPos pos) {
        return blighted.contains(pos);
    }

    public void blight(BlockPos pos) {
        if (blighted.add(pos.immutable())) {
            setDirty();
        }
    }

    public void cure(BlockPos pos) {
        if (blighted.remove(pos)) {
            setDirty();
        }
    }
}
