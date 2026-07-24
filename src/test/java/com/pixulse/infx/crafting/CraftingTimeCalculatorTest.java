package com.pixulse.infx.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CraftingTimeCalculatorTest {
    @Test
    void rejectsNonPositiveDifficulty() {
        assertThrows(IllegalArgumentException.class, () -> CraftingTimeCalculator.requiredTicks(0.0, 0, BenchTier.HAND));
        assertThrows(IllegalArgumentException.class, () -> CraftingTimeCalculator.requiredTicks(-1.0, 0, BenchTier.HAND));
    }

    @Test
    void appliesAllThreeDifficultyBranchesAtTheirBoundaries() {
        assertEquals(25, CraftingTimeCalculator.baseTicks(0.1));
        assertEquals(25, CraftingTimeCalculator.baseTicks(24.999));
        assertEquals(25, CraftingTimeCalculator.baseTicks(25.0));
        assertEquals(51, CraftingTimeCalculator.baseTicks(50.5));
        assertEquals(100, CraftingTimeCalculator.baseTicks(100.0));
        assertEquals(101, CraftingTimeCalculator.baseTicks(101.0));
        assertEquals(196, CraftingTimeCalculator.baseTicks(400.0));
    }

    @Test
    void levelAndBenchBonusesDivideThenTruncateThePeriod() {
        assertEquals(196, CraftingTimeCalculator.requiredTicks(400.0, 0, BenchTier.HAND));
        assertEquals(130, CraftingTimeCalculator.requiredTicks(400.0, 25, BenchTier.HAND));
        assertEquals(115, CraftingTimeCalculator.requiredTicks(400.0, 25, BenchTier.FLINT));
        assertEquals(115, CraftingTimeCalculator.requiredTicks(400.0, 25, BenchTier.OBSIDIAN));
        assertEquals(108, CraftingTimeCalculator.requiredTicks(400.0, 25, BenchTier.COPPER));
        assertEquals(108, CraftingTimeCalculator.requiredTicks(400.0, 25, BenchTier.SILVER));
        assertEquals(108, CraftingTimeCalculator.requiredTicks(400.0, 25, BenchTier.GOLD));
        assertEquals(103, CraftingTimeCalculator.requiredTicks(400.0, 25, BenchTier.IRON));
        assertEquals(98, CraftingTimeCalculator.requiredTicks(400.0, 25, BenchTier.ANCIENT_METAL));
        assertEquals(93, CraftingTimeCalculator.requiredTicks(400.0, 25, BenchTier.MITHRIL));
        assertEquals(89, CraftingTimeCalculator.requiredTicks(400.0, 25, BenchTier.ADAMANTIUM));
    }

    @Test
    void finalPeriodNeverDropsBelowTwentyFiveTicks() {
        assertEquals(25, CraftingTimeCalculator.requiredTicks(50.0, 200, BenchTier.COPPER));
    }

    @Test
    void ordinaryRecipesAlwaysUseTheFixedWorkbenchBonus() {
        assertEquals(115, CraftingTimeCalculator.requiredTicks(
                400.0, 25, BenchTier.ADAMANTIUM, false, false));
        assertEquals(89, CraftingTimeCalculator.requiredTicks(
                400.0, 25, BenchTier.ADAMANTIUM, true, false));
    }

    @Test
    void clumsinessDoublesBeforeDivisionAndDoesNotDoubleTheFloor() {
        assertEquals(392, CraftingTimeCalculator.requiredTicks(
                400.0, 0, BenchTier.HAND, false, true));
        assertEquals(25, CraftingTimeCalculator.requiredTicks(
                25.0, 200, BenchTier.ADAMANTIUM, true, true));
    }

    @Test
    void negativeDebtLevelsSlowCraftingWithoutCrossingZeroDivisor() {
        assertEquals(980, CraftingTimeCalculator.requiredTicks(400.0, -40, BenchTier.HAND));
        assertEquals(490, CraftingTimeCalculator.requiredTicks(400.0, -40, BenchTier.FLINT));
    }
}
