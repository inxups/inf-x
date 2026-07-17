package com.pixulse.infx.harvest;

import java.util.Optional;

public final class HarvestPolicy {
    private HarvestPolicy() {}

    public static boolean allows(
            boolean creative,
            boolean restricted,
            boolean correctForDrops,
            Optional<HarvestTier> toolTier,
            Optional<HarvestTier> requiredTier) {
        if (creative || !restricted) {
            return true;
        }
        return correctForDrops
                && toolTier.isPresent()
                && requiredTier.isPresent()
                && toolTier.orElseThrow().satisfies(requiredTier.orElseThrow());
    }
}
