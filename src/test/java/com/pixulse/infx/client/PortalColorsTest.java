package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.world.Underworld;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.junit.jupiter.api.Test;

class PortalColorsTest {
    @Test
    void netherPortalUsesItsDedicatedRedTintSource() {
        BlockColors colors = new BlockColors();
        PortalColors.registerBlockTintSources(new RegisterColorHandlersEvent.BlockTintSources(colors));

        var source = colors.getTintSource(InfXBlocks.NETHER_PORTAL.get().defaultBlockState(), 0);
        assertNotNull(source);
        assertEquals(PortalColors.NETHER_PORTAL_TINT, source.color(InfXBlocks.NETHER_PORTAL.get().defaultBlockState()));
    }

    @Test
    void runegateUsesTheRequestedDimensionPalette() {
        assertEquals(0x359FFF, RunegateColors.rgbFor(Level.OVERWORLD));
        assertEquals(0x4401B4, RunegateColors.rgbFor(Underworld.LEVEL));
        assertEquals(0x000000, RunegateColors.rgbFor(Level.NETHER));
        assertEquals(0xFFFFFF, RunegateColors.rgbFor(Level.END));
    }

    @Test
    void runegateTintSourceIsRegisteredForBothRuneGateSurfaces() {
        BlockColors colors = new BlockColors();
        PortalColors.registerBlockTintSources(new RegisterColorHandlersEvent.BlockTintSources(colors));

        var underworldSource = colors.getTintSource(InfXBlocks.UNDERWORLD_PORTAL.get().defaultBlockState(), 0);
        var returnSpawnSource = colors.getTintSource(InfXBlocks.RETURN_SPAWN_PORTAL.get().defaultBlockState(), 0);
        assertNotNull(underworldSource);
        assertNotNull(returnSpawnSource);
        assertEquals(PortalColors.RUNEGATE_OVERWORLD_TINT,
                underworldSource.color(InfXBlocks.UNDERWORLD_PORTAL.get().defaultBlockState()));
        assertEquals(PortalColors.RUNEGATE_OVERWORLD_TINT,
                returnSpawnSource.color(InfXBlocks.RETURN_SPAWN_PORTAL.get().defaultBlockState()));
    }
}
