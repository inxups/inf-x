package com.pixulse.infx.harvest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HarvestPolicyTest {
    @Test
    void unrestrictedBlocksAreLeftToVanilla() {
        assertTrue(HarvestPolicy.allows(false, false, false, 0, 0));
    }

    @Test
    void creativePlayersBypassRestrictions() {
        assertTrue(HarvestPolicy.allows(true, false, false, 0, 6));
    }

    @Test
    void portableBlocksBypassTheirUnderlyingMaterialLevel() {
        assertTrue(HarvestPolicy.allows(false, true, false, 0, 4));
    }

    @Test
    void positiveLevelsRequireTheEffectiveToolFamily() {
        assertFalse(HarvestPolicy.allows(false, false, false, 5, 1));
    }

    @Test
    void positiveLevelsRequireATierTag() {
        assertFalse(HarvestPolicy.allows(false, false, true, 0, 1));
    }

    @Test
    void requirementsRejectLowerLevelsAndAcceptEqualOrHigherLevels() {
        assertFalse(HarvestPolicy.allows(false, false, true, 1, 2));
        assertTrue(HarvestPolicy.allows(false, false, true, 2, 2));
        assertTrue(HarvestPolicy.allows(false, false, true, 3, 2));
        assertFalse(HarvestPolicy.allows(false, false, true, 5, 6));
    }
}
