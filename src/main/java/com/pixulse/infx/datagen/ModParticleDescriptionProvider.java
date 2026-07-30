package com.pixulse.infx.datagen;

import com.pixulse.infx.registry.InfXParticles;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.data.ParticleDescriptionProvider;

/** Reuses the vanilla portal sprite sequence for INFX's colored portal particles. */
final class ModParticleDescriptionProvider extends ParticleDescriptionProvider {
    ModParticleDescriptionProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void addDescriptions() {
        spriteSet(InfXParticles.NETHER_PORTAL.get(), Identifier.withDefaultNamespace("generic"), 8, false);
        spriteSet(InfXParticles.RUNEGATE.get(), Identifier.withDefaultNamespace("generic"), 8, false);
    }
}
