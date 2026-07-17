package com.pixulse.infx.harvest;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

public enum HarvestTier {
    FLINT,
    COPPER,
    IRON,
    ANCIENT_METAL,
    MITHRIL,
    ADAMANTIUM;

    public boolean satisfies(HarvestTier requiredTier) {
        return ordinal() >= requiredTier.ordinal();
    }

    public String path() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Optional<HarvestTier> fromPath(String path) {
        for (HarvestTier tier : values()) {
            if (tier.path().equals(path)) {
                return Optional.of(tier);
            }
        }
        return Optional.empty();
    }

    public static Optional<HarvestTier> highest(Collection<HarvestTier> tiers) {
        return tiers.stream().max(Enum::compareTo);
    }
}
