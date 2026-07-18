package com.pixulse.infx.crafting;

import java.util.Locale;
import java.util.Optional;

public enum BenchTier {
    HAND(0, 0.0),
    FLINT(1, 0.2),
    COPPER(4, 0.3),
    SILVER(4, 0.3),
    GOLD(4, 0.3),
    IRON(8, 0.4),
    ANCIENT_METAL(16, 0.5),
    MITHRIL(64, 0.6),
    ADAMANTIUM(256, 0.7),
    OBSIDIAN(2, 0.2);

    private final int capability;
    private final double speedBonus;

    BenchTier(int capability, double speedBonus) {
        this.capability = capability;
        this.speedBonus = speedBonus;
    }

    public double speedBonus() {
        return speedBonus;
    }

    public boolean supports(BenchTier requiredBench) {
        return capability >= requiredBench.capability;
    }

    public boolean isWorkbench() {
        return this != HAND;
    }

    public BenchTier recipeTier() {
        return switch (this) {
            case SILVER, GOLD -> COPPER;
            default -> this;
        };
    }

    public boolean isRecipeTier() {
        return recipeTier() == this;
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
