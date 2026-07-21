package com.pixulse.infx.harvest;

import net.minecraft.world.phys.HitResult;

/** Maps a block with no destroy progress to Minecraft's ordinary empty attack path. */
public final class MiningInputRules {
    private MiningInputRules() {}

    public static HitResult.Type attackTargetType(HitResult.Type original, boolean hasDestroyProgress) {
        return original == HitResult.Type.BLOCK && !hasDestroyProgress
                ? HitResult.Type.MISS
                : original;
    }
}
