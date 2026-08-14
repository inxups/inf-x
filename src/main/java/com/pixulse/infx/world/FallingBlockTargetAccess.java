package com.pixulse.infx.world;

import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;

/** Stores a temporary horizontal landing target for a sliding falling block. */
public interface FallingBlockTargetAccess {
    @Nullable
    BlockPos getAllocatedTarget();

    void setAllocatedTarget(@Nullable BlockPos target);
}
