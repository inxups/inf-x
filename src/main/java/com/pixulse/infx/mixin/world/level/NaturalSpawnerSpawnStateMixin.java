package com.pixulse.infx.mixin.world.level;

import com.pixulse.infx.config.InfXConfig;
import com.pixulse.infx.world.MoonPhase;
import com.pixulse.infx.world.SpawnDensity;
import com.pixulse.infx.world.SpawnRateTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MITE per-night hostile spawn cadence ({@code SpawnerAnimals.performRandomLivingEntitySpawning}):
 * each overworld natural spawn attempt rolls {@code (y<60 ? 0.1 : 0.17) ×} the daily rate modifier,
 * reproducing MITE's 10%/17% per-tick cadence instead of modern vanilla's every-tick attempts.
 */
@Mixin(NaturalSpawner.SpawnState.class)
public abstract class NaturalSpawnerSpawnStateMixin {
    @Inject(method = "canSpawn", at = @At("HEAD"), cancellable = true)
    private void infx$spawnCadence(EntityType<?> type, BlockPos testPos, ChunkAccess chunk, CallbackInfoReturnable<Boolean> cir) {
        if (type.getCategory() != MobCategory.MONSTER
                || !(chunk instanceof LevelChunk levelChunk)
                || !InfXConfig.INSTANCE.mobs.enabled.getValue()
                || !InfXConfig.INSTANCE.mobs.spawnCadence.getValue()) {
            return;
        }
        Level level = levelChunk.getLevel();
        if (!(level instanceof ServerLevel serverLevel) || !MoonPhase.isOverworld(serverLevel)) {
            return;
        }
        float modifier = SpawnRateTracker.get(serverLevel).modifier(serverLevel);
        if (serverLevel.getRandom().nextFloat() >= SpawnDensity.cadenceChance(testPos.getY(), modifier)) {
            cir.setReturnValue(false);
        }
    }
}
