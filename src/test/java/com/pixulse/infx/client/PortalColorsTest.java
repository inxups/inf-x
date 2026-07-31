package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.block.UnderworldPortalBlock;
import com.pixulse.infx.world.Underworld;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.junit.jupiter.api.Test;

class PortalColorsTest {
    @Test
    void portalTintSourceUsesTheDefaultDestinationPalette() {
        BlockColors colors = new BlockColors();
        PortalColors.registerBlockTintSources(new RegisterColorHandlersEvent.BlockTintSources(colors));

        var netherSource = colors.getTintSource(InfXBlocks.NETHER_PORTAL.get().defaultBlockState(), 0);
        var underworldSource = colors.getTintSource(InfXBlocks.UNDERWORLD_PORTAL.get().defaultBlockState(), 0);
        var returnSpawnSource = colors.getTintSource(InfXBlocks.RETURN_SPAWN_PORTAL.get().defaultBlockState(), 0);
        assertNotNull(netherSource);
        assertNotNull(underworldSource);
        assertNotNull(returnSpawnSource);
        assertEquals(
                PortalDestinationColors.tintFor(Level.NETHER),
                netherSource.color(InfXBlocks.NETHER_PORTAL.get().defaultBlockState()));
        assertEquals(
                PortalDestinationColors.tintFor(Underworld.LEVEL),
                underworldSource.color(InfXBlocks.UNDERWORLD_PORTAL.get().defaultBlockState()));
        assertEquals(
                PortalDestinationColors.tintFor(Level.OVERWORLD),
                returnSpawnSource.color(InfXBlocks.RETURN_SPAWN_PORTAL.get().defaultBlockState()));
    }

    @Test
    void destinationPaletteUsesTheRequestedDimension() {
        assertEquals(0x359FFF, PortalDestinationColors.rgbFor(Level.OVERWORLD));
        assertEquals(0x4401B4, PortalDestinationColors.rgbFor(Underworld.LEVEL));
        assertEquals(0xBE250B, PortalDestinationColors.rgbFor(Level.NETHER));
        assertEquals(0xFFFFFF, PortalDestinationColors.rgbFor(Level.END));
    }

    @Test
    void portalSurfacesResolveColorsFromTheirDestination() {
        var underworld = InfXBlocks.UNDERWORLD_PORTAL.get().defaultBlockState();
        var runeGate = underworld.setValue(UnderworldPortalBlock.RUNE_GATE, true);
        var nether = InfXBlocks.NETHER_PORTAL.get().defaultBlockState();

        assertEquals(Underworld.LEVEL, PortalDestinationColors.destinationDimension(underworld, Level.OVERWORLD));
        assertEquals(Level.OVERWORLD, PortalDestinationColors.destinationDimension(underworld, Underworld.LEVEL));
        assertEquals(Level.NETHER, PortalDestinationColors.destinationDimension(nether, Underworld.LEVEL));
        assertEquals(Underworld.LEVEL, PortalDestinationColors.destinationDimension(nether, Level.NETHER));
        assertEquals(Level.OVERWORLD, PortalDestinationColors.destinationDimension(runeGate, Level.OVERWORLD));
        assertEquals(Underworld.LEVEL, PortalDestinationColors.destinationDimension(runeGate, Underworld.LEVEL));
    }
}
