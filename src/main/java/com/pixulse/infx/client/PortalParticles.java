package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.InfxPortalBlock;
import com.pixulse.infx.block.InfxPortalBlock.PortalType;
import com.pixulse.infx.registry.InfXParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/** Destination-colored variants of the vanilla portal particle. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class PortalParticles {
    static final int UNDERWORLD_PORTAL_RGB = PortalDestinationColors.UNDERWORLD_RGB;
    static final int NETHER_PORTAL_RGB = PortalDestinationColors.NETHER_RGB;
    static final int RUNEGATE_RGB = PortalDestinationColors.OVERWORLD_RGB;
    static final int RUNEGATE_UNDERWORLD_RGB = PortalDestinationColors.UNDERWORLD_RGB;
    static final int RUNEGATE_NETHER_RGB = PortalDestinationColors.NETHER_RGB;

    private PortalParticles() {}

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(
                InfXParticles.UNDERWORLD_PORTAL.get(),
                sprites -> new TintedPortalParticleProvider(
                        sprites, PortalType.UNDERWORLD, false, UNDERWORLD_PORTAL_RGB));
        event.registerSpriteSet(
                InfXParticles.NETHER_PORTAL.get(),
                sprites -> new TintedPortalParticleProvider(
                        sprites, PortalType.NETHER, false, NETHER_PORTAL_RGB));
        event.registerSpriteSet(
                InfXParticles.RUNEGATE.get(),
                sprites -> new TintedPortalParticleProvider(
                        sprites, PortalType.UNDERWORLD, true, RUNEGATE_RGB));
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
        private final PortalType portalType;
        private final boolean runegate;
        private final int fallbackRgb;

        private TintedPortalParticleProvider(
                SpriteSet sprites, PortalType portalType, boolean runegate, int fallbackRgb) {
            this.sprites = sprites;
            this.portalType = portalType;
            this.runegate = runegate;
            this.fallbackRgb = fallbackRgb;
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
            int particleRgb = rgbFor(portalType, runegate, level.dimension(), fallbackRgb);
            return new TintedPortalParticle(
                    level, x, y, z, xd, yd, zd, sprites.get(random), particleRgb, runegate);
        }
    }

    static int rgbFor(
            PortalType portalType, boolean runegate, ResourceKey<Level> currentDimension, int fallbackRgb) {
        if (runegate) {
            return PortalDestinationColors.rgbFor(currentDimension);
        }
        ResourceKey<Level> destination = InfxPortalBlock.destinationDimension(portalType, currentDimension);
        return destination == null ? fallbackRgb : PortalDestinationColors.rgbFor(destination);
    }
}
