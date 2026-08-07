package com.pixulse.infx.block;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.registry.InfXParticles;
import org.junit.jupiter.api.Test;

class InfxPortalParticleTest {
    @Test
    void portalSurfacesUseTheirParticleFamilies() {
        assertSame(
                InfXParticles.UNDERWORLD_PORTAL.get(),
                InfXBlocks.UNDERWORLD_PORTAL.get().portalParticle(InfXBlocks.UNDERWORLD_PORTAL.get().defaultBlockState()));
        assertSame(
                InfXParticles.RUNEGATE.get(),
                InfXBlocks.UNDERWORLD_PORTAL.get().portalParticle(InfXBlocks.UNDERWORLD_PORTAL.get()
                        .defaultBlockState()
                        .setValue(UnderworldPortalBlock.RUNE_GATE, true)));
        assertSame(
                InfXParticles.NETHER_PORTAL.get(),
                InfXBlocks.NETHER_PORTAL.get().portalParticle(InfXBlocks.NETHER_PORTAL.get().defaultBlockState()));
        assertSame(
                InfXParticles.RUNEGATE.get(),
                InfXBlocks.RETURN_SPAWN_PORTAL.get().portalParticle(InfXBlocks.RETURN_SPAWN_PORTAL.get()
                        .defaultBlockState()));
    }
}
