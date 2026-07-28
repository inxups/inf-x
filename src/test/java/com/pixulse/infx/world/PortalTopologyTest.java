package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.block.MitePortalBlock;
import com.pixulse.infx.block.MitePortalBlock.PortalType;
import com.pixulse.infx.block.UnderworldPortalBlock;
import com.pixulse.infx.item.material.MiteMaterial;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class PortalTopologyTest {
    @Test
    void adamantiumRuneDestinationsAreEightTimesFartherAtMinimum() {
        var mithril = UnderworldPortalBlock.runeDestinationOffset(MiteMaterial.MITHRIL, 0x1234, 0, 0);
        var adamantium = UnderworldPortalBlock.runeDestinationOffset(MiteMaterial.ADAMANTIUM, 0x1234, 0, 0);
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
                MitePortalBlock.PortalRoute.OVERWORLD_SPAWN,
                MitePortalBlock.routeFor(PortalType.RETURN_SPAWN, Level.OVERWORLD));
        assertEquals(
                MitePortalBlock.PortalRoute.UNDERWORLD,
                MitePortalBlock.routeFor(PortalType.UNDERWORLD, Level.OVERWORLD));
        assertEquals(
                MitePortalBlock.PortalRoute.OVERWORLD,
                MitePortalBlock.routeFor(PortalType.UNDERWORLD, Underworld.LEVEL));
        assertEquals(
                MitePortalBlock.PortalRoute.NETHER,
                MitePortalBlock.routeFor(PortalType.NETHER, Underworld.LEVEL));
        assertEquals(
                MitePortalBlock.PortalRoute.UNDERWORLD,
                MitePortalBlock.routeFor(PortalType.NETHER, Level.NETHER));
        assertEquals(
                MitePortalBlock.PortalRoute.NONE,
                MitePortalBlock.routeFor(PortalType.UNDERWORLD, Level.NETHER));
        assertEquals(
                MitePortalBlock.PortalRoute.NONE,
                MitePortalBlock.routeFor(PortalType.NETHER, Level.OVERWORLD));
        assertEquals(
                MitePortalBlock.PortalRoute.NONE,
                MitePortalBlock.routeFor(PortalType.RETURN_SPAWN, Underworld.LEVEL));
    }

    @Test
    void runeDestinationsAreSeedIndependentStableAndOrientationGrouped() {
        var first = UnderworldPortalBlock.runeDestinationOffset(MiteMaterial.MITHRIL, 0xABCD, 0, 0);
        var repeated = UnderworldPortalBlock.runeDestinationOffset(MiteMaterial.MITHRIL, 0xABCD, 0, 0);
        var opposite = UnderworldPortalBlock.runeDestinationOffset(MiteMaterial.MITHRIL, 0xABCD, 1, 0);
        assertEquals(first, repeated);
        assertNotEquals(first, opposite);
    }

    @Test
    void oceanRetriesUseDifferentDeterministicCandidates() {
        var first = UnderworldPortalBlock.runeDestinationOffset(MiteMaterial.ADAMANTIUM, 7, 0, 0);
        var retry = UnderworldPortalBlock.runeDestinationOffset(MiteMaterial.ADAMANTIUM, 7, 0, 4);
        assertNotEquals(first, retry);
    }
}
