package com.pixulse.infx.world;

import net.minecraft.world.level.dimension.DimensionType;

/**
 * Surface lava lake suppression rules.
 *
 * <p>In 26.1.2 an aquifer lake becomes lava only when its fluid surface is at or below
 * y = -10 (the overworld noise router's {@code lava} density function is sampled with a
 * {@code |value| > 0.3} threshold in {@code Aquifer$NoiseBasedAquifer.computeFluidType}).
 * The fluid surface is always derived from the lowest preliminary terrain surface around
 * the aquifer cell, so a lake is either buried below the terrain (underground cave lake)
 * or flush with it (an open surface pool). Only the latter is open to the sky and gets
 * suppressed here.
 */
public final class LavaLakeRules {
    private LavaLakeRules() {
    }

    /**
     * Returns whether a lava-eligible aquifer lake would be open to the sky at the sampled
     * column instead of buried below the terrain surface.
     *
     * @param preliminarySurfaceLevel terrain surface at the sampled column
     * @param fluidSurfaceLevel       computed fluid surface of the lake
     */
    public static boolean isSurfaceLake(int preliminarySurfaceLevel, int fluidSurfaceLevel) {
        if (fluidSurfaceLevel <= -10 && fluidSurfaceLevel != DimensionType.WAY_BELOW_MIN_Y) {
            return preliminarySurfaceLevel <= fluidSurfaceLevel;
        }
        return false;
    }
}
