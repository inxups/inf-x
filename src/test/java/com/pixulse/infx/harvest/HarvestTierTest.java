package com.pixulse.infx.harvest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class HarvestTierTest {
    @Test
    void tiersFollowTheProgressionOrder() {
        assertEquals(
                List.of(
                        HarvestTier.FLINT,
                        HarvestTier.COPPER,
                        HarvestTier.IRON,
                        HarvestTier.ANCIENT_METAL,
                        HarvestTier.MITHRIL,
                        HarvestTier.ADAMANTIUM),
                List.of(HarvestTier.values()));
    }

    @Test
    void higherTierSatisfiesLowerRequirement() {
        assertTrue(HarvestTier.COPPER.satisfies(HarvestTier.FLINT));
        assertTrue(HarvestTier.ADAMANTIUM.satisfies(HarvestTier.MITHRIL));
        assertTrue(HarvestTier.IRON.satisfies(HarvestTier.ANCIENT_METAL));
        assertTrue(HarvestTier.ANCIENT_METAL.satisfies(HarvestTier.IRON));
        assertFalse(HarvestTier.ANCIENT_METAL.satisfies(HarvestTier.MITHRIL));
        assertEquals(3, HarvestTier.IRON.level());
        assertEquals(3, HarvestTier.ANCIENT_METAL.level());
        assertEquals(4, HarvestTier.MITHRIL.level());
        assertEquals(5, HarvestTier.ADAMANTIUM.level());
    }

    @Test
    void resolvesTheHighestTierFromSeveralTags() {
        assertEquals(
                HarvestTier.IRON,
                HarvestTier.highest(List.of(HarvestTier.FLINT, HarvestTier.IRON, HarvestTier.COPPER)).orElseThrow());
    }

    @Test
    void parsesStableLowercaseTagPaths() {
        assertEquals(HarvestTier.ANCIENT_METAL, HarvestTier.fromPath("ancient_metal").orElseThrow());
        assertTrue(HarvestTier.fromPath("diamond").isEmpty());
    }
}
