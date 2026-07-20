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
        return requiredTicks(difficulty, experienceLevel, benchTier, benchTier.materialGatedTier(), false);
    }

    /**
     * Applies the exact R196 order: quality-adjusted base period, optional
     * Clumsiness doubling, then level/workbench division, then the 25-tick
     * floor.  In particular, Clumsiness does not incorrectly raise the final
     * floor to 50 ticks.
     */
    public static int requiredTicks(
            double difficulty,
            int experienceLevel,
            BenchTier benchTier,
            boolean materialGated,
            boolean clumsy) {
        if (benchTier == null) {
            throw new NullPointerException("benchTier");
        }
        double divisor = 1.0D
                + MiteCraftingRules.levelModifier(experienceLevel)
                + MiteCraftingRules.benchModifier(benchTier, materialGated);
        if (!Double.isFinite(divisor) || divisor <= 0.0D) {
            throw new IllegalArgumentException("crafting speed divisor must be positive");
        }
        double unmodified = baseTicks(difficulty);
        if (clumsy) {
            unmodified *= 2.0D;
        }
        int adjusted = (int) (unmodified / divisor);
        return Math.max(MINIMUM_TICKS, adjusted);
    }

    private static void validateDifficulty(double difficulty) {
        if (!Double.isFinite(difficulty) || difficulty <= 0.0) {
            throw new IllegalArgumentException("difficulty must be a positive finite number");
        }
    }
}
