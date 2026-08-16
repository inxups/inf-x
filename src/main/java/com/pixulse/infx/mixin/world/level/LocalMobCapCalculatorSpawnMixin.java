package com.pixulse.infx.mixin.world.level;

import com.pixulse.infx.config.InfXConfig;
import com.pixulse.infx.world.SpawnDensity;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LocalMobCapCalculator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MITE blood-moon ×1.5 and depth radius: the near-player hostile ceiling ({@code canSpawn}) scales
 * per player with {@code max(1+(64-y)/32, blood-moon factor)} instead of a flat chunk cap, so deeper
 * or blood-moon-night players tolerate more hostile mobs around them. Stronghold proximity
 * ({@code WorldServer.getStrongholdProximity}) raises the ceiling further near far-out strongholds.
 */
@Mixin(LocalMobCapCalculator.class)
public abstract class LocalMobCapCalculatorSpawnMixin {
    @Shadow
    @Final
    private Map<ServerPlayer, LocalMobCapCalculator.MobCounts> playerMobCounts;

    @Shadow
    protected abstract List<ServerPlayer> getPlayersNear(ChunkPos pos);

    @Inject(method = "canSpawn", at = @At("HEAD"), cancellable = true)
    private void infx$depthSpawnCap(MobCategory category, ChunkPos pos, CallbackInfoReturnable<Boolean> cir) {
        boolean depth = InfXConfig.INSTANCE.mobs.depthSpawn.getValue();
        boolean stronghold = InfXConfig.INSTANCE.mobs.strongholdProximity.getValue();
        if (category != MobCategory.MONSTER
                || !InfXConfig.INSTANCE.mobs.enabled.getValue()
                || !depth && !stronghold) {
            return;
        }
        for (ServerPlayer player : this.getPlayersNear(pos)) {
            LocalMobCapCalculator.MobCounts counts = this.playerMobCounts.get(player);
            float scale = depth ? SpawnDensity.densityCapScale(player.level(), player.getBlockY()) : 1.0F;
            if (stronghold) {
                scale *= 1.0F + SpawnDensity.strongholdProximity(player);
            }
            int cap = (int) Math.ceil(category.getMaxInstancesPerChunk() * scale);
            if (counts == null
                    || ((LocalMobCapCalculatorMobCountsAccessor) (Object) counts).counts().getInt(category) < cap) {
                cir.setReturnValue(true);
                return;
            }
        }
        cir.setReturnValue(false);
    }
}
