package com.pixulse.infx.crafting;

public final class CraftingTimeCalculator {
    private static final int MINIMUM_TICKS = 25;

    private CraftingTimeCalculator() {}

    public static int baseTicks(double difficulty) {
        validateDifficulty(difficulty);
        if (difficulty < 25.0) {
            return MINIMUM_TICKS;
        }
        if (difficulty <= 100.0) {
            return Math.toIntExact(Math.round(difficulty));
        }
        return Math.toIntExact(Math.round(Math.pow(difficulty - 100.0, 0.8))) + 100;
    }

    public static int requiredTicks(double difficulty, int experienceLevel, BenchTier benchTier) {
        double divisor = 1.0 + experienceLevel * 0.02 + benchTier.speedBonus();
        int adjusted = (int) (baseTicks(difficulty) / divisor);
        return Math.max(MINIMUM_TICKS, adjusted);
    }

    private static void validateDifficulty(double difficulty) {
        if (!Double.isFinite(difficulty) || difficulty <= 0.0) {
            throw new IllegalArgumentException("difficulty must be a positive finite number");
        }
    }
}
