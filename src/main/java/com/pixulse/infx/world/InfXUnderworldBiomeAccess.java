package com.pixulse.infx.world;

import net.minecraft.core.QuartPos;
import net.minecraft.world.level.WorldGenLevel;

/** Reads Underworld biomes without asking a WorldGenRegion for another chunk. */
final class InfXUnderworldBiomeAccess {
    private InfXUnderworldBiomeAccess() {}

    static boolean isLushBiome(WorldGenLevel level, int x, int y, int z) {
        return level.getUncachedNoiseBiome(
                        QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z))
                .is(Underworld.LUSH_BIOME);
    }
}
