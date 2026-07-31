package com.pixulse.infx.world;

/** Provides a deterministic, seed-based partition for Underworld decorations. */
public final class InfXUnderworldLushRegion {
    private static final long REGION_SEED_SALT = 0x4C55_5348_5EED_0001L;
    private static final long REGION_X_SALT = 341_873_128_712L;
    private static final long REGION_Z_SALT = 132_897_987_541L;

    private InfXUnderworldLushRegion() {}

    public static boolean isLushRegion(long worldSeed, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, Underworld.LUSH_REGION_CHUNK_SIZE);
        int regionZ = Math.floorDiv(chunkZ, Underworld.LUSH_REGION_CHUNK_SIZE);
        long value = worldSeed ^ REGION_SEED_SALT;
        value ^= (long) regionX * REGION_X_SALT;
        value ^= (long) regionZ * REGION_Z_SALT;
        return (mix64(value) & 1L) == 0L;
    }

    static long mix64(long value) {
        value = (value ^ value >>> 30) * 0xBF58_476D_1CE4_E5B9L;
        value = (value ^ value >>> 27) * 0x94D0_49BB_1331_11EBL;
        return value ^ value >>> 31;
    }
}
