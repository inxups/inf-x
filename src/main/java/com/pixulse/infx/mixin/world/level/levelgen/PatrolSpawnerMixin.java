package com.pixulse.infx.mixin.world.level.levelgen;

import com.pixulse.infx.world.PatrolRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * InfX gates pillager patrol spawning behind the village unlock condition (day 60 plus the
 * world iron-tool milestone), with a hard 32-day floor. The vanilla five-day timeline gate is
 * superseded because the patrols otherwise appear long before any village exists.
 */
@Mixin(PatrolSpawner.class)
public abstract class PatrolSpawnerMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void infx$gatePatrolSpawning(ServerLevel level, boolean spawnEnemies, CallbackInfo callback) {
        if (!PatrolRules.maySpawn(level)) {
            callback.cancel();
        }
    }
}
