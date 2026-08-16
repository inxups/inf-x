package com.pixulse.infx.mixin.server.level;

import com.pixulse.infx.config.InfXConfig;
import com.pixulse.infx.world.SpawnDensity;
import net.minecraft.server.level.ServerChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * MITE blood-moon spawn radius ×1.5 (8→12 chunks): the global hostile cap derives from the spawn-area
 * chunk count ({@code SpawnState.canSpawnForCategoryGlobal}), so scaling it on a blood-moon night lets
 * more mobs fill the area. Applied only to the {@code createState} count, never the actual chunk list.
 */
@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheSpawnCapMixin {
    @ModifyArg(
            method = "tickChunks(Lnet/minecraft/util/profiling/ProfilerFiller;J)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/NaturalSpawner;createState(ILjava/lang/Iterable;Lnet/minecraft/world/level/NaturalSpawner$ChunkGetter;Lnet/minecraft/world/level/LocalMobCapCalculator;)Lnet/minecraft/world/level/NaturalSpawner$SpawnState;"),
            index = 0)
    private int infx$bloodMoonSpawnCap(int chunkCount) {
        if (!InfXConfig.INSTANCE.mobs.enabled.getValue() || !InfXConfig.INSTANCE.mobs.depthSpawn.getValue()) {
            return chunkCount;
        }
        ServerChunkCache cache = (ServerChunkCache) (Object) this;
        return (int) (chunkCount * SpawnDensity.bloodMoonSpawnFactor(cache.level));
    }
}
