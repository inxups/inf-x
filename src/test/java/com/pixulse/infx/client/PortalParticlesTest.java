package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.block.InfxPortalBlock.PortalType;
import com.pixulse.infx.world.Underworld;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class PortalParticlesTest {
    @Test
    void colorsAndBrightnessMatchPortalFamilies() {
        assertEquals(0x4401B4, PortalParticles.UNDERWORLD_PORTAL_RGB);
        assertEquals(0xBE250B, PortalParticles.NETHER_PORTAL_RGB);
        assertEquals(0x359FFF, PortalParticles.RUNEGATE_RGB);
        assertEquals(0x4401B4, PortalParticles.RUNEGATE_UNDERWORLD_RGB);
        assertEquals(0xBE250B, PortalParticles.RUNEGATE_NETHER_RGB);
        assertEquals(0.4F, PortalParticles.colorScale(false, 0.0F), 0.0001F);
        assertEquals(1.0F, PortalParticles.colorScale(false, 1.0F), 0.0001F);
        assertEquals(0.7F, PortalParticles.colorScale(true, 0.0F), 0.0001F);
        assertEquals(1.0F, PortalParticles.colorScale(true, 1.0F), 0.0001F);
    }

    @Test
    void particleColorsFollowPortalDestinations() {
        assertEquals(
                PortalDestinationColors.UNDERWORLD_RGB,
                PortalParticles.rgbFor(PortalType.UNDERWORLD, false, Level.OVERWORLD, 0));
        assertEquals(
                PortalDestinationColors.OVERWORLD_RGB,
                PortalParticles.rgbFor(PortalType.UNDERWORLD, false, Underworld.LEVEL, 0));
        assertEquals(
                PortalDestinationColors.NETHER_RGB,
                PortalParticles.rgbFor(PortalType.NETHER, false, Underworld.LEVEL, 0));
        assertEquals(
                PortalDestinationColors.UNDERWORLD_RGB,
                PortalParticles.rgbFor(PortalType.NETHER, false, Level.NETHER, 0));
        assertEquals(
                PortalDestinationColors.UNDERWORLD_RGB,
                PortalParticles.rgbFor(PortalType.UNDERWORLD, true, Underworld.LEVEL, 0));
    }
}
