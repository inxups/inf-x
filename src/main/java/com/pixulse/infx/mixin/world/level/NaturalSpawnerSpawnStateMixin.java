package com.pixulse.infx.mixin.world.level;

import com.pixulse.infx.world.SpawnGate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
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
 * The decision lives in {@link SpawnGate#cadenceChancePasses}.
 */
@Mixin(NaturalSpawner.SpawnState.class)
public abstract class NaturalSpawnerSpawnStateMixin {
    @Inject(method = "canSpawn", at = @At("HEAD"), cancellable = true)
    private void infx$spawnCadence(EntityType<?> type, BlockPos testPos, ChunkAccess chunk, CallbackInfoReturnable<Boolean> cir) {
        if (type.getCategory() != MobCategory.MONSTER
                || !(chunk instanceof LevelChunk levelChunk)) {
            return;
        }
        if (levelChunk.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel
                && !SpawnGate.cadenceChancePasses(serverLevel, testPos.getY())) {
            cir.setReturnValue(false);
        }
    }
}
