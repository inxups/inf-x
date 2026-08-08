package com.pixulse.infx.recipe;

/**
 * The server-side crafting facts that are needed to apply the INFX
 * crafting rules to one recipe match.
 *
 * <p>{@code difficulty} is the unmodified recipe difficulty (the sum of the
 * component difficulties or an explicit recipe-rule value).  {@code materialGated}
 * distinguishes recipes for which InfX checks the hardness of the workbench
 * from ordinary recipes that merely need a 3x3 grid.  The distinction matters
 * because an ordinary recipe receives the fixed 20% workbench bonus even when
 * it is opened on an adamantium bench.</p>
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
}
