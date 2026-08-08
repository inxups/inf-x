package com.pixulse.infx.mixin.world.level.levelgen;

import com.pixulse.infx.world.StructureGenerationGates;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * InfX gates pillager patrol spawning behind the same condition that unlocks village
 * generation (day 60 plus the world iron-tool milestone), superseding the vanilla five-day
 * timeline gate so patrols never appear before any village exists.
 */
@Mixin(PatrolSpawner.class)
public abstract class PatrolSpawnerMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void infx$gatePatrolSpawning(ServerLevel level, boolean spawnEnemies, CallbackInfo callback) {
        if (!StructureGenerationGates.isUnlocked(StructureGenerationGates.VILLAGE_RULE, level)) {
            callback.cancel();
        }
    }
}
