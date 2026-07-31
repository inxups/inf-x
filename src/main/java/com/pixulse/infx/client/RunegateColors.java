package com.pixulse.infx.client;

import com.pixulse.infx.world.Underworld;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** Dimension palette shared by rune-gate surfaces, particles, and the loading fade. */
final class RunegateColors {
    static final int OVERWORLD_RGB = 0x359FFF;
    static final int UNDERWORLD_RGB = 0x4401B4;
    static final int NETHER_RGB = 0x000000;
    static final int FALLBACK_RGB = 0xFFFFFF;

    private RunegateColors() {}

    static int rgbFor(ResourceKey<Level> dimension) {
        if (dimension.equals(Level.OVERWORLD)) {
            return OVERWORLD_RGB;
        }
        if (dimension.equals(Underworld.LEVEL)) {
            return UNDERWORLD_RGB;
        }
        if (dimension.equals(Level.NETHER)) {
            return NETHER_RGB;
        }
        return FALLBACK_RGB;
    }

    static int tintFor(ResourceKey<Level> dimension) {
        return 0xFF000000 | rgbFor(dimension);
    }
}
