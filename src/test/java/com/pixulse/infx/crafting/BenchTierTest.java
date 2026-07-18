package com.pixulse.infx.crafting;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
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
        assertTrue(BenchTier.OBSIDIAN.supports(BenchTier.FLINT));
        assertFalse(BenchTier.OBSIDIAN.supports(BenchTier.COPPER));
        assertTrue(BenchTier.IRON.supports(BenchTier.OBSIDIAN));
        assertTrue(BenchTier.ADAMANTIUM.supports(BenchTier.MITHRIL));
        assertFalse(BenchTier.MITHRIL.supports(BenchTier.ADAMANTIUM));
    }

    @Test
    void copperSilverAndGoldAreEquivalentWorkbenchCapabilities() {
        assertTrue(BenchTier.COPPER.supports(BenchTier.SILVER));
        assertTrue(BenchTier.COPPER.supports(BenchTier.GOLD));
        assertTrue(BenchTier.SILVER.supports(BenchTier.COPPER));
        assertTrue(BenchTier.GOLD.supports(BenchTier.SILVER));
        assertSame(BenchTier.COPPER, BenchTier.SILVER.recipeTier());
        assertSame(BenchTier.COPPER, BenchTier.GOLD.recipeTier());
    }

    @Test
    void everySerializedNameRoundTrips() {
        for (BenchTier tier : BenchTier.values()) {
            assertSame(tier, BenchTier.fromSerializedName(tier.serializedName()).orElseThrow());
        }
    }
}
