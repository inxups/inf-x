package com.pixulse.infx.harvest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class HarvestRequirementsTest {
    @Test
    void explicitMiteLevelOverridesModernFallbacks() {
        assertEquals(0, HarvestRequirements.inferLevel(OptionalInt.of(0), true, true));
        assertEquals(1, HarvestRequirements.inferLevel(OptionalInt.of(1), false, true));
        assertEquals(6, HarvestRequirements.inferLevel(OptionalInt.of(6), false, true));
    }

    @Test
    void logsAndPickaxeBlocksReceiveStableFallbackLevels() {
        assertEquals(1, HarvestRequirements.inferLevel(OptionalInt.empty(), true, false));
        assertEquals(1, HarvestRequirements.inferLevel(OptionalInt.empty(), true, true));
        assertEquals(2, HarvestRequirements.inferLevel(OptionalInt.empty(), false, true));
        assertEquals(0, HarvestRequirements.inferLevel(OptionalInt.empty(), false, false));
    }
}
