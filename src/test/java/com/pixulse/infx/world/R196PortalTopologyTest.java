package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.block.UnderworldPortalBlock;
import com.pixulse.infx.material.R196Material;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class R196PortalTopologyTest {
    @Test
    void adamantiumRuneDestinationsAreEightTimesFartherAtMinimum() {
        var mithril = UnderworldPortalBlock.runeDestinationOffset(R196Material.MITHRIL, 0x1234, 0, 0);
        var adamantium = UnderworldPortalBlock.runeDestinationOffset(R196Material.ADAMANTIUM, 0x1234, 0, 0);
        assertTrue(mithril.horizontalDistance() >= 2_500.0);
        assertTrue(mithril.horizontalDistance() <= 5_000.0);
        assertTrue(adamantium.horizontalDistance() >= 20_000.0);
        assertTrue(adamantium.horizontalDistance() <= 40_000.0);
    }

    @Test
    void allFiveOrdinaryPortalRoutesAreExplicit() {
        assertEquals(
                UnderworldPortalBlock.PortalRoute.OVERWORLD_SPAWN,
                UnderworldPortalBlock.routeFor(Level.OVERWORLD, false, false));
        assertEquals(
                UnderworldPortalBlock.PortalRoute.UNDERWORLD,
                UnderworldPortalBlock.routeFor(Level.OVERWORLD, true, false));
        assertEquals(
                UnderworldPortalBlock.PortalRoute.OVERWORLD,
                UnderworldPortalBlock.routeFor(Underworld.LEVEL, false, false));
        assertEquals(
                UnderworldPortalBlock.PortalRoute.NETHER,
                UnderworldPortalBlock.routeFor(Underworld.LEVEL, false, true));
        assertEquals(
                UnderworldPortalBlock.PortalRoute.UNDERWORLD,
                UnderworldPortalBlock.routeFor(Level.NETHER, false, false));
    }

    @Test
    void runeDestinationsAreSeedIndependentStableAndOrientationGrouped() {
        var first = UnderworldPortalBlock.runeDestinationOffset(R196Material.MITHRIL, 0xABCD, 0, 0);
        var repeated = UnderworldPortalBlock.runeDestinationOffset(R196Material.MITHRIL, 0xABCD, 0, 0);
        var opposite = UnderworldPortalBlock.runeDestinationOffset(R196Material.MITHRIL, 0xABCD, 1, 0);
        assertEquals(first, repeated);
        assertNotEquals(first, opposite);
    }

    @Test
    void oceanRetriesUseDifferentDeterministicCandidates() {
        var first = UnderworldPortalBlock.runeDestinationOffset(R196Material.ADAMANTIUM, 7, 0, 0);
        var retry = UnderworldPortalBlock.runeDestinationOffset(R196Material.ADAMANTIUM, 7, 0, 4);
        assertNotEquals(first, retry);
    }
}
