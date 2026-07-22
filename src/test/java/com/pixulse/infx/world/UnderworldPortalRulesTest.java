package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class UnderworldPortalRulesTest {
    @Test
    void firePortalIgnitionIncludesUnderworldWithoutNarrowingExistingDimensions() {
        assertTrue(UnderworldPortalRules.allowsFirePortalIgnition(false, Underworld.LEVEL));
        assertFalse(UnderworldPortalRules.allowsFirePortalIgnition(false, Level.END));
        assertTrue(UnderworldPortalRules.allowsFirePortalIgnition(true, Level.END));
    }
}
