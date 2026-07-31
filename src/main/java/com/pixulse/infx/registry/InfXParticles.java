package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Portal particle types with client factories that preserve vanilla portal motion. */
public final class InfXParticles {
    private static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, InfiniteX.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> UNDERWORLD_PORTAL =
            PARTICLES.register("underworld_portal", InfxSimpleParticleType::new);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> NETHER_PORTAL =
            PARTICLES.register("nether_portal", InfxSimpleParticleType::new);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RUNEGATE =
            PARTICLES.register("runegate", InfxSimpleParticleType::new);

    private InfXParticles() {}

    public static void register(IEventBus modBus) {
        PARTICLES.register(modBus);
    }

    private static final class InfxSimpleParticleType extends SimpleParticleType {
        private InfxSimpleParticleType() {
            super(false);
        }
    }
}
