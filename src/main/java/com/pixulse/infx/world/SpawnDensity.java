package com.pixulse.infx.world;

import net.minecraft.world.level.Level;

/**
 * MITE spawn-density helpers backing the depth-generation cluster. MITE's "spawn radius" is really
 * a near-player mob-density threshold ({@code SpawnerAnimals.setEligibleChunksForSpawning}): the
 * eligible search area is always a fixed 17×17 chunk square, but a player's personal hostile
 * ceiling grows with the blood moon (×1.5) and with depth ({@code 8×(1+(64-y)/32)}). These helpers
 * scale the modern per-player/global mob caps and the per-night spawn cadence to match.
 */
public final class SpawnDensity {
    private SpawnDensity() {}

    /** MITE blood-moon radius ×1.5 (8→12 chunks): factor 1.5 on a blood-moon night, else 1.0. */
    public static float bloodMoonSpawnFactor(Level level) {
        return MoonPhase.BLOOD.isActiveInOverworldAtNight(level) ? 1.5F : 1.0F;
    }

    /**
     * MITE depth radius {@code 8×(1+(64-y)/32)}: the near-player hostile ceiling grows from 1.0 at
     * y=64 to 3.0 at y=0 (bedrock), never below the blood-moon factor.
     */
    public static float densityCapScale(Level level, double playerY) {
        if (!MoonPhase.isOverworld(level)) {
            return 1.0F;
        }
        float depthFactor = (float) Math.clamp(1.0 + (64.0 - playerY) / 32.0, 1.0, 3.0);
        return Math.max(depthFactor, bloodMoonSpawnFactor(level));
    }

    /** MITE per-night hostile cadence: y&lt;60 passes roll 0.1, y≥60 passes roll 0.17, scaled by the rate modifier. */
    public static float cadenceChance(int y, float modifier) {
        return (float) Math.clamp((y < 60 ? 0.1F : 0.17F) * modifier, 0.0, 1.0);
    }
}
