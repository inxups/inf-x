package com.pixulse.infx.crafting;

import java.util.Locale;
import java.util.Optional;

public enum BenchTier {
    HAND(0.0),
    FLINT(0.2),
    COPPER(0.3);

    private final double speedBonus;

    BenchTier(double speedBonus) {
        this.speedBonus = speedBonus;
    }

    public double speedBonus() {
        return speedBonus;
    }

    public boolean supports(BenchTier requiredBench) {
        return ordinal() >= requiredBench.ordinal();
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Optional<BenchTier> fromSerializedName(String name) {
        for (BenchTier tier : values()) {
            if (tier.serializedName().equals(name)) {
                return Optional.of(tier);
            }
        }
        return Optional.empty();
    }
}
