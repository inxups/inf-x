package com.pixulse.infx.crafting;

/**
 * The server-side crafting facts that are needed to apply the MITE R196
 * crafting rules to one recipe match.
 *
 * <p>{@code difficulty} is the unmodified recipe difficulty (the sum of the
 * component difficulties).  {@code materialGated} distinguishes recipes for
 * which MITE checks the hardness of the workbench from ordinary recipes that
 * merely need a 3x3 grid.  The distinction matters because an ordinary recipe
 * receives the fixed 20% workbench bonus even when it is opened on an
 * adamantium bench.</p>
 */
public record CraftingProfile(BenchTier requiredBench, float difficulty, boolean materialGated) {
    public CraftingProfile {
        if (requiredBench == null) {
            throw new NullPointerException("requiredBench");
        }
        if (!Float.isFinite(difficulty) || difficulty <= 0.0F) {
            throw new IllegalArgumentException("difficulty must be a positive finite number");
        }
    }

    public static CraftingProfile explicit(BenchTier requiredBench, float difficulty) {
        return new CraftingProfile(requiredBench, difficulty, requiredBench.materialGatedTier());
    }
}
