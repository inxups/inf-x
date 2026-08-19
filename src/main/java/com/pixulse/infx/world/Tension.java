package com.pixulse.infx.world;

import com.pixulse.infx.config.InfXConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;

/**
 * InfX chunk-residency difficulty, ported from MITE's {@code World.getTensionFactorForBlock}.
 *
 * <p>Tension is the MITE replacement for a global day counter: it scales a single point by how
 * long its chunk has been inhabited, the current moon phase, and the game difficulty.
 */
public final class Tension {
    /** A chunk reaches full residency after 150 in-game days (3,600,000 ticks) of being loaded. */
    private static final float FULL_RESIDENCY_TICKS = 3_600_000.0F;

    private Tension() {}

    /** Returns true when the tension ramp is enabled; falls back to the day curve when off. */
    public static boolean enabled() {
        return InfXConfig.INSTANCE.mobs.enabled.getValue()
                && InfXConfig.INSTANCE.mobs.tensionEnabled.getValue();
    }

    /** MITE formula: clamp(chunkInhabited/3,600,000, 0, 1) × (hard ? 1 : 0.75) + moonFactor × 0.25. */
    public static float forBlock(ServerLevel level, BlockPos pos) {
        boolean hard = level.getDifficulty() == Difficulty.HARD;
        int chunkX = SectionPos.blockToSectionCoord(pos.getX());
        int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
        // Non-blocking fetch: finalizeSpawn can run on the worldgen thread during chunk
        // generation/structure-mob placement (see MonsterEvents#initializeReplacement), and a
        // blocking getChunkAt there schedules a chunk load and deadlocks on CompletableFuture.join.
        // Mirrors vanilla ServerLevel#getCurrentDifficultyAt. An unavailable chunk is treated as
        // freshly generated (inhabited time 0), which is exactly what such a chunk would hold.
        ChunkAccess chunk = level.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
        long inhabitedTime = chunk != null ? chunk.getInhabitedTime() : 0L;
        float moonFactor = DimensionType.MOON_BRIGHTNESS_PER_PHASE[
                MoonPhase.visualPhaseAtTime(level.getOverworldClockTime()).index()];
        float tension = Mth.clamp(inhabitedTime / FULL_RESIDENCY_TICKS, 0.0F, 1.0F)
                * (hard ? 1.0F : 0.75F);
        tension += moonFactor * 0.25F;
        if (level.getDifficulty().getId() < 2) {
            tension *= level.getDifficulty().getId() / 2.0F;
        }
        return Mth.clamp(tension, 0.0F, hard ? 1.5F : 1.0F);
    }

    public static float forLocation(ServerLevel level, Vec3 pos) {
        return forBlock(level, BlockPos.containing(pos));
    }
}
