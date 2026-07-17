package com.pixulse.infx.harvest;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
