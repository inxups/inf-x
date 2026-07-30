package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.block.InfxPortalBlock;
import com.pixulse.infx.block.InfxPortalBlock.PortalType;
import com.pixulse.infx.block.UnderworldPortalBlock;
import com.pixulse.infx.event.UnderworldPortalEvents;
import com.pixulse.infx.item.material.InfxMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
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
    void runeDestinationsKeepTheSameOffsetAtDifferentEntries() {
        BlockPos firstEntry = new BlockPos(1_234, 70, -5_678);
        BlockPos secondEntry = firstEntry.offset(10_000, 0, -12_000);
        BlockPos firstDestination = UnderworldPortalBlock.runeDestinationPosition(
                firstEntry, InfxMaterial.ADAMANTIUM, 0xABCD, 0, 0);
        BlockPos secondDestination = UnderworldPortalBlock.runeDestinationPosition(
                secondEntry, InfxMaterial.ADAMANTIUM, 0xABCD, 0, 0);

        assertEquals(10_000, secondDestination.getX() - firstDestination.getX());
        assertEquals(-12_000, secondDestination.getZ() - firstDestination.getZ());
        assertEquals(firstEntry.getY(), firstDestination.getY());
        assertEquals(secondEntry.getY(), secondDestination.getY());
    }

    @Test
    void ordinaryPortalDestinationsUseTheCurrentCoordinateAndDimensionScale() {
        var registries = VanillaRegistries.createLookup();
        DimensionType overworld = registries
                .lookupOrThrow(Registries.DIMENSION_TYPE)
                .getOrThrow(BuiltinDimensionTypes.OVERWORLD)
                .value();
        DimensionType nether = registries
                .lookupOrThrow(Registries.DIMENSION_TYPE)
                .getOrThrow(BuiltinDimensionTypes.NETHER)
                .value();

        assertEquals(
                new BlockPos(100, 70, -200),
                InfxPortalBlock.scaledExitPosition(overworld, nether, new Vec3(800, 70, -1_600)));
        assertEquals(
                new BlockPos(800, 70, -1_600),
                InfxPortalBlock.scaledExitPosition(nether, overworld, new Vec3(100, 70, -200)));
    }

    @Test
    void ordinaryPortalSearchKeepsTheVanillaDimensionRadii() {
        assertEquals(16, InfxPortalBlock.portalSearchRadius(Level.NETHER));
        assertEquals(128, InfxPortalBlock.portalSearchRadius(Level.OVERWORLD));
        assertEquals(128, InfxPortalBlock.portalSearchRadius(Underworld.LEVEL));
    }

    @Test
    void oceanRetriesUseDifferentDeterministicCandidates() {
        var first = UnderworldPortalBlock.runeDestinationOffset(InfxMaterial.ADAMANTIUM, 7, 0, 0);
        var retry = UnderworldPortalBlock.runeDestinationOffset(InfxMaterial.ADAMANTIUM, 7, 0, 4);
        assertNotEquals(first, retry);
    }
}
