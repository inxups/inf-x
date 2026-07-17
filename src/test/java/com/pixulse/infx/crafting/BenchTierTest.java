package com.pixulse.infx.crafting;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BenchTierTest {
    @Test
    void higherBenchesSupportAllLowerRecipes() {
        assertTrue(BenchTier.HAND.supports(BenchTier.HAND));
        assertFalse(BenchTier.HAND.supports(BenchTier.FLINT));
        assertTrue(BenchTier.FLINT.supports(BenchTier.HAND));
        assertFalse(BenchTier.FLINT.supports(BenchTier.COPPER));
        assertTrue(BenchTier.COPPER.supports(BenchTier.HAND));
        assertTrue(BenchTier.COPPER.supports(BenchTier.FLINT));
        assertTrue(BenchTier.COPPER.supports(BenchTier.COPPER));
    }
}
