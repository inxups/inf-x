package com.pixulse.infx.client;

import com.pixulse.infx.block.InfxPortalBlock;
import com.pixulse.infx.block.InfxPortalBlock.PortalType;
import com.pixulse.infx.block.UnderworldPortalBlock;
import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.world.Underworld;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** Shared destination palette for portal surfaces, particles, and rune-gate animation. */
final class PortalDestinationColors {
    static final int OVERWORLD_RGB = 0x359FFF;
    static final int UNDERWORLD_RGB = 0x4401B4;
    static final int NETHER_RGB = 0xBE250B;
    static final int FALLBACK_RGB = 0xFFFFFF;

    private PortalDestinationColors() {}

    static int rgbFor(@Nullable ResourceKey<Level> dimension) {
        if (Level.OVERWORLD.equals(dimension)) {
            return OVERWORLD_RGB;
        }
        if (Underworld.LEVEL.equals(dimension)) {
            return UNDERWORLD_RGB;
        }
        if (Level.NETHER.equals(dimension)) {
            return NETHER_RGB;
        }
        return FALLBACK_RGB;
    }

    static int tintFor(@Nullable ResourceKey<Level> dimension) {
        return 0xFF000000 | rgbFor(dimension);
    }

    static int tintFor(BlockState state) {
        return tintFor(destinationDimension(state));
    }

    static int tintFor(BlockState state, ResourceKey<Level> currentDimension) {
        return tintFor(destinationDimension(state, currentDimension));
    }

    static @Nullable ResourceKey<Level> destinationDimension(BlockState state) {
        return destinationDimension(state, null);
    }

    static @Nullable ResourceKey<Level> destinationDimension(
            BlockState state, @Nullable ResourceKey<Level> currentDimension) {
        if (state.is(InfXBlocks.RETURN_SPAWN_PORTAL.get())) {
            return Level.OVERWORLD;
        }
        if (state.is(InfXBlocks.UNDERWORLD_PORTAL.get())
                && state.getValue(UnderworldPortalBlock.RUNE_GATE)) {
            return currentDimension == null ? Level.OVERWORLD : currentDimension;
        }
        if (!(state.getBlock() instanceof InfxPortalBlock portal)) {
            return null;
        }
        if (currentDimension != null) {
            ResourceKey<Level> destination =
                    InfxPortalBlock.destinationDimension(portal.portalType(), currentDimension);
            if (destination != null) {
                return destination;
            }
        }
        return defaultDimension(portal.portalType());
    }

    private static ResourceKey<Level> defaultDimension(PortalType portalType) {
        return switch (portalType) {
            case UNDERWORLD -> Underworld.LEVEL;
            case NETHER -> Level.NETHER;
            case RETURN_SPAWN -> Level.OVERWORLD;
        };
    }
}
