package com.pixulse.infx.entity;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.config.InfXConfig;
import com.pixulse.infx.world.SpawnerLifetime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/** Spawner-lifetime kill accounting: a player kill of a spawner-spawned mob charges its origin spawner. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class SpawnerEvents {
    private SpawnerEvents() {}

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!InfXConfig.INSTANCE.mobs.spawnerLifetime.getValue()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()
                || !(entity.level() instanceof ServerLevel level)
                || !SpawnerLifetime.isSpawnerSpawned(entity)) {
            return;
        }
        // MITE: only a player kill (recentlyHit > 0) advances the count; a lingering death must not.
        Entity killer = event.getSource().getEntity();
        LivingEntity killCredit = entity.getKillCredit();
        if (!(killer instanceof Player) && !(killCredit instanceof Player)) {
            return;
        }
        BlockPos origin = SpawnerLifetime.spawnerOrigin(entity);
        if (origin == BlockPos.ZERO) {
            return;
        }
        SpawnerLifetime.registerKill(level, origin);
    }
}
