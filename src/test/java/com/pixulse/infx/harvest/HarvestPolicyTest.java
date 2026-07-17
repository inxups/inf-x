package com.pixulse.infx.harvest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class HarvestPolicyTest {
    @Test
    void unrestrictedBlocksAreLeftToVanilla() {
        assertTrue(HarvestPolicy.allows(
                false, false, false, Optional.empty(), Optional.empty()));
    }

    @Test
    void creativePlayersBypassRestrictions() {
        assertTrue(HarvestPolicy.allows(
                true, true, false, Optional.empty(), Optional.of(HarvestTier.ADAMANTIUM)));
    }

    @Test
    void restrictedBlocksRequireVanillaToolCapability() {
        assertFalse(HarvestPolicy.allows(
                false, true, false, Optional.of(HarvestTier.COPPER), Optional.of(HarvestTier.FLINT)));
    }

    @Test
    void restrictedBlocksRequireAnInfiniteXTierTag() {
        assertFalse(HarvestPolicy.allows(
                false, true, true, Optional.empty(), Optional.of(HarvestTier.FLINT)));
    }

    @Test
    void restrictedBlocksRejectLowerTiersAndAcceptEqualOrHigherTiers() {
        assertFalse(HarvestPolicy.allows(
                false, true, true, Optional.of(HarvestTier.FLINT), Optional.of(HarvestTier.COPPER)));
        assertTrue(HarvestPolicy.allows(
                false, true, true, Optional.of(HarvestTier.COPPER), Optional.of(HarvestTier.COPPER)));
        assertTrue(HarvestPolicy.allows(
                false, true, true, Optional.of(HarvestTier.IRON), Optional.of(HarvestTier.COPPER)));
    }

    @Test
    void malformedRestrictedBlocksFailClosed() {
        assertFalse(HarvestPolicy.allows(
                false, true, true, Optional.of(HarvestTier.ADAMANTIUM), Optional.empty()));
    }
}
