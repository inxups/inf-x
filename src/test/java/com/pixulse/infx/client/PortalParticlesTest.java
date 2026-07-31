package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PortalParticlesTest {
    @Test
    void colorsAndBrightnessMatchMitePortalFamilies() {
        assertEquals(0xBE250B, PortalParticles.NETHER_PORTAL_RGB);
        assertEquals(0x359FFF, PortalParticles.RUNEGATE_RGB);
        assertEquals(0x4401B4, PortalParticles.RUNEGATE_UNDERWORLD_RGB);
        assertEquals(0xBE250B, PortalParticles.RUNEGATE_NETHER_RGB);
        assertEquals(0.4F, PortalParticles.colorScale(false, 0.0F), 0.0001F);
        assertEquals(1.0F, PortalParticles.colorScale(false, 1.0F), 0.0001F);
        assertEquals(0.7F, PortalParticles.colorScale(true, 0.0F), 0.0001F);
        assertEquals(1.0F, PortalParticles.colorScale(true, 1.0F), 0.0001F);
    }
}
