package com.pixulse.infx.mixin.world.level;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.pixulse.infx.world.SpawnGate;
import com.pixulse.infx.world.SpawnerLifetime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MITE spawner lifetime. A block spawner stops permanently once the mobs it spawned are killed
 * {@link SpawnGate#MAX_SPAWNER_KILLS} times. Each spawn stamps the origin spawner position on the
 * entity (key {@link SpawnerLifetime#SPAWNER_POS}); a {@code LivingDeathEvent} listener turns a
 * player kill of such an entity back into a spawner kill count. Exhaustion is enforced by
 * {@link #infx$stopWhenExhausted}.
 *
 * <p>The stamp runs via {@code @WrapOperation} on {@code tryAddFreshEntityWithPassengers}: the
 * spawner position is captured at {@code serverTick} entry into a thread-local, so the wrapper
 * needs no fragile local-variable capture across the method's try-with-resources block.
 */
@Mixin(BaseSpawner.class)
public abstract class BaseSpawnerMixin {
    @Unique
    private static final ThreadLocal<Long> infx$currentSpawnerPos = new ThreadLocal<>();

    /**
     * Exhausted spawners keep ticking particles but never spawn or reduce their delay — matching
     * MITE's {@code TileEntityMobSpawnerLogic.canRun()} returning false on the metadata cap.
     */
    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private void infx$stopWhenExhausted(ServerLevel level, BlockPos pos, CallbackInfo ci) {
        if (SpawnGate.spawnerExhausted(SpawnerLifetime.getKills(level, pos))) {
            ci.cancel();
        }
    }

    @Inject(method = "serverTick", at = @At("HEAD"))
    private void infx$captureSpawnerPos(ServerLevel level, BlockPos pos, CallbackInfo ci) {
        infx$currentSpawnerPos.set(pos.asLong());
    }

    @WrapOperation(
            method = "serverTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;tryAddFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean infx$stampSpawnerOrigin(ServerLevel level, Entity entity, Operation<Boolean> original) {
        Long pos = infx$currentSpawnerPos.get();
        if (pos != null) {
            entity.getPersistentData().putLong(SpawnerLifetime.SPAWNER_POS, pos);
        }
        return original.call(level, entity);
    }
}
