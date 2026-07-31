package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.InfXParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/** MITE-colored variants of the vanilla portal particle. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class PortalParticles {
    static final int NETHER_PORTAL_RGB = 0xBE250B;
    static final int RUNEGATE_RGB = RunegateColors.OVERWORLD_RGB;
    static final int RUNEGATE_UNDERWORLD_RGB = RunegateColors.UNDERWORLD_RGB;
    static final int RUNEGATE_NETHER_RGB = RunegateColors.NETHER_RGB;

    private PortalParticles() {}

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(
                InfXParticles.NETHER_PORTAL.get(),
                sprites -> new TintedPortalParticleProvider(sprites, NETHER_PORTAL_RGB, false));
        event.registerSpriteSet(
                InfXParticles.RUNEGATE.get(),
                sprites -> new TintedPortalParticleProvider(sprites, RUNEGATE_RGB, true));
    }

    static float colorScale(boolean runegate, float random) {
        float brightness = random * 0.6F + 0.4F;
        return runegate ? (brightness + 1.0F) * 0.5F : brightness;
    }

    private static final class TintedPortalParticle extends PortalParticle {
        private TintedPortalParticle(
                ClientLevel level,
                double x,
                double y,
                double z,
                double xd,
                double yd,
                double zd,
                TextureAtlasSprite sprite,
                int rgb,
                boolean runegate) {
            super(level, x, y, z, xd, yd, zd, sprite);
            float scale = colorScale(runegate, random.nextFloat());
            setColor(
                    ((rgb >>> 16) & 0xFF) / 255.0F * scale,
                    ((rgb >>> 8) & 0xFF) / 255.0F * scale,
                    (rgb & 0xFF) / 255.0F * scale);
        }
    }

    private static final class TintedPortalParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final int rgb;
        private final boolean runegate;

        private TintedPortalParticleProvider(SpriteSet sprites, int rgb, boolean runegate) {
            this.sprites = sprites;
            this.rgb = rgb;
            this.runegate = runegate;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xd,
                double yd,
                double zd,
                RandomSource random) {
            int particleRgb = runegate ? RunegateColors.rgbFor(level.dimension()) : rgb;
            return new TintedPortalParticle(
                    level, x, y, z, xd, yd, zd, sprites.get(random), particleRgb, runegate);
        }
    }
}
