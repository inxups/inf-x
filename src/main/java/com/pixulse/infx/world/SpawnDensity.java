package com.pixulse.infx.world;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.Level;

/**
 * MITE spawn-density helpers backing the depth-generation cluster. MITE's "spawn radius" is really
 * a near-player mob-density threshold ({@code SpawnerAnimals.setEligibleChunksForSpawning}): the
 * eligible search area is always a fixed 17×17 chunk square, but a player's personal hostile
 * ceiling grows with the blood moon (×1.5) and with depth ({@code 8×(1+(64-y)/32)}). These helpers
 * scale the modern per-player/global mob caps and the per-night spawn cadence to match.
 */
public final class SpawnDensity {
    private static final long PROXIMITY_TTL_TICKS = 200L;
    private static final int STRONGHOLD_SEARCH_RADIUS = 30_000;
    private static final Map<UUID, ProximityCache> STRONGHOLD_CACHE = new ConcurrentHashMap<>();

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

    /**
     * MITE stronghold proximity ({@code SpawnerAnimals} × {@code WorldServer.getStrongholdProximity}):
     * a player near a far-out stronghold tolerates a higher hostile ceiling. The nearest-stronghold
     * lookup is expensive, so the factor is cached per player for 200 ticks.
     */
    public static float strongholdProximity(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return 0.0F;
        }
        long tick = level.getGameTime();
        BlockPos pos = player.blockPosition().immutable();
        ProximityCache cached = STRONGHOLD_CACHE.get(player.getUUID());
        if (cached != null && tick - cached.queriedAtTick() < PROXIMITY_TTL_TICKS && cached.playerPos().equals(pos)) {
            return cached.factor();
        }
        float factor = computeStrongholdProximity(level, pos);
        STRONGHOLD_CACHE.put(player.getUUID(), new ProximityCache(pos, tick, factor));
        return factor;
    }

    /** MITE proximity formula: 0 outside a 2000-block ring, else {@code 1 - playerToStronghold/spawnToStronghold}. */
    public static float strongholdProximityFromDistances(double spawnToStronghold, double playerToStronghold) {
        if (spawnToStronghold < 2_000.0) {
            return 0.0F;
        }
        return (float) Math.max(0.0, 1.0 - playerToStronghold / spawnToStronghold);
    }

    private static float computeStrongholdProximity(ServerLevel level, BlockPos playerPos) {
        BlockPos stronghold = level.findNearestMapStructure(
                StructureTags.EYE_OF_ENDER_LOCATED, playerPos, STRONGHOLD_SEARCH_RADIUS, false);
        if (stronghold == null) {
            return 0.0F;
        }
        BlockPos spawn = level.getRespawnData().globalPos().pos();
        double spawnToStronghold = horizontalDistance(spawn, stronghold);
        double playerToStronghold = horizontalDistance(playerPos, stronghold);
        return strongholdProximityFromDistances(spawnToStronghold, playerToStronghold);
    }

    private static double horizontalDistance(BlockPos first, BlockPos second) {
        double dx = first.getX() - second.getX();
        double dz = first.getZ() - second.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    private record ProximityCache(BlockPos playerPos, long queriedAtTick, float factor) {}
}
