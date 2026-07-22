package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.block.R196PortalBlock;
import com.pixulse.infx.block.R196PortalBlock.PortalType;
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
    void ordinaryPortalBlocksSelectAndRouteIndependently() {
        assertEquals(
                PortalType.RETURN_SPAWN,
                UnderworldPortalEvents.portalTypeFor(Level.OVERWORLD, false, false));
        assertEquals(
                PortalType.UNDERWORLD,
                UnderworldPortalEvents.portalTypeFor(Level.OVERWORLD, true, false));
        assertEquals(
                PortalType.UNDERWORLD,
                UnderworldPortalEvents.portalTypeFor(Underworld.LEVEL, false, false));
        assertEquals(
                PortalType.NETHER,
                UnderworldPortalEvents.portalTypeFor(Underworld.LEVEL, false, true));
        assertEquals(
                PortalType.NETHER,
                UnderworldPortalEvents.portalTypeFor(Level.NETHER, false, false));

        assertEquals(
                R196PortalBlock.PortalRoute.OVERWORLD_SPAWN,
                R196PortalBlock.routeFor(PortalType.RETURN_SPAWN, Level.OVERWORLD));
        assertEquals(
                R196PortalBlock.PortalRoute.UNDERWORLD,
                R196PortalBlock.routeFor(PortalType.UNDERWORLD, Level.OVERWORLD));
        assertEquals(
                R196PortalBlock.PortalRoute.OVERWORLD,
                R196PortalBlock.routeFor(PortalType.UNDERWORLD, Underworld.LEVEL));
        assertEquals(
                R196PortalBlock.PortalRoute.NETHER,
                R196PortalBlock.routeFor(PortalType.NETHER, Underworld.LEVEL));
        assertEquals(
                R196PortalBlock.PortalRoute.UNDERWORLD,
                R196PortalBlock.routeFor(PortalType.NETHER, Level.NETHER));
        assertEquals(
                R196PortalBlock.PortalRoute.NONE,
                R196PortalBlock.routeFor(PortalType.UNDERWORLD, Level.NETHER));
        assertEquals(
                R196PortalBlock.PortalRoute.NONE,
                R196PortalBlock.routeFor(PortalType.NETHER, Level.OVERWORLD));
        assertEquals(
                R196PortalBlock.PortalRoute.NONE,
                R196PortalBlock.routeFor(PortalType.RETURN_SPAWN, Underworld.LEVEL));
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
