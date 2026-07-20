package com.pixulse.infx.harvest;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;

public enum HarvestTier {
    FLINT(1),
    COPPER(2),
    IRON(3),
    ANCIENT_METAL(3),
    MITHRIL(4),
    ADAMANTIUM(5);

    private final int level;

    HarvestTier(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }

    public boolean satisfies(HarvestTier requiredTier) {
        return satisfies(requiredTier.level);
    }

    public boolean satisfies(int requiredLevel) {
        return level >= requiredLevel;
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
        return tiers.stream().max(Comparator.comparingInt(HarvestTier::level).thenComparingInt(Enum::ordinal));
    }
}
