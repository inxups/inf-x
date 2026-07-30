package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.block.InfxPortalBlock;
import com.pixulse.infx.block.InfxPortalBlock.PortalType;
import com.pixulse.infx.block.UnderworldPortalBlock;
import com.pixulse.infx.event.UnderworldPortalEvents;
import com.pixulse.infx.item.material.InfxMaterial;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class PortalTopologyTest {
    @Test
    void adamantiumRuneDestinationsAreEightTimesFartherAtMinimum() {
        var mithril = UnderworldPortalBlock.runeDestinationOffset(InfxMaterial.MITHRIL, 0x1234, 0, 0);
        var adamantium = UnderworldPortalBlock.runeDestinationOffset(InfxMaterial.ADAMANTIUM, 0x1234, 0, 0);
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
                InfxPortalBlock.PortalRoute.OVERWORLD_SPAWN,
                InfxPortalBlock.routeFor(PortalType.RETURN_SPAWN, Level.OVERWORLD));
        assertEquals(
                InfxPortalBlock.PortalRoute.UNDERWORLD,
                InfxPortalBlock.routeFor(PortalType.UNDERWORLD, Level.OVERWORLD));
        assertEquals(
                InfxPortalBlock.PortalRoute.OVERWORLD,
                InfxPortalBlock.routeFor(PortalType.UNDERWORLD, Underworld.LEVEL));
        assertEquals(
                InfxPortalBlock.PortalRoute.NETHER,
                InfxPortalBlock.routeFor(PortalType.NETHER, Underworld.LEVEL));
        assertEquals(
                InfxPortalBlock.PortalRoute.UNDERWORLD,
                InfxPortalBlock.routeFor(PortalType.NETHER, Level.NETHER));
        assertEquals(
                InfxPortalBlock.PortalRoute.NONE,
                InfxPortalBlock.routeFor(PortalType.UNDERWORLD, Level.NETHER));
        assertEquals(
                InfxPortalBlock.PortalRoute.NONE,
                InfxPortalBlock.routeFor(PortalType.NETHER, Level.OVERWORLD));
        assertEquals(
                InfxPortalBlock.PortalRoute.NONE,
                InfxPortalBlock.routeFor(PortalType.RETURN_SPAWN, Underworld.LEVEL));
    }

    @Test
    void runeDestinationsAreSeedIndependentStableAndOrientationGrouped() {
        var first = UnderworldPortalBlock.runeDestinationOffset(InfxMaterial.MITHRIL, 0xABCD, 0, 0);
        var repeated = UnderworldPortalBlock.runeDestinationOffset(InfxMaterial.MITHRIL, 0xABCD, 0, 0);
        var opposite = UnderworldPortalBlock.runeDestinationOffset(InfxMaterial.MITHRIL, 0xABCD, 1, 0);
        assertEquals(first, repeated);
        assertNotEquals(first, opposite);
    }

    @Test
    void oceanRetriesUseDifferentDeterministicCandidates() {
        var first = UnderworldPortalBlock.runeDestinationOffset(InfxMaterial.ADAMANTIUM, 7, 0, 0);
        var retry = UnderworldPortalBlock.runeDestinationOffset(InfxMaterial.ADAMANTIUM, 7, 0, 4);
        assertNotEquals(first, retry);
    }
}
