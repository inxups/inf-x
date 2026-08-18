package com.pixulse.infx.world;

import com.pixulse.infx.registry.InfXAttachments;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

/**
 * MITE spawner lifetime bookkeeping. A block spawner counts how many of its spawned mobs were
 * killed by players; at {@link SpawnGate#MAX_SPAWNER_KILLS} the spawner stops permanently. The
 * count lives on the {@link SpawnerBlockEntity} via a NeoForge attachment so it persists with the
 * chunk, matching MITE's weaponised block metadata (which has no modern analogue).
 */
public final class SpawnerLifetime {
    private SpawnerLifetime() {}

    /** Persistent-data key holding the block spawner that spawned this entity. */
    public static final String SPAWNER_POS = "infx.spawner_pos";

    /** Current lifetime kill count for the block spawner at {@code pos}, or 0 when absent. */
    public static int getKills(ServerLevel level, BlockPos pos) {
        return getSpawner(level, pos).map(spawner -> spawner.getData(InfXAttachments.SPAWNER_KILLS)).orElse(0);
    }

    /** Record one player kill of a spawner-spawned mob against its origin block spawner. */
    public static void registerKill(ServerLevel level, BlockPos pos) {
        getSpawner(level, pos).ifPresent(spawner -> {
            int kills = spawner.getData(InfXAttachments.SPAWNER_KILLS) + 1;
            spawner.setData(InfXAttachments.SPAWNER_KILLS, Math.min(kills, SpawnGate.MAX_SPAWNER_KILLS));
            spawner.setChanged();
        });
    }

    private static Optional<SpawnerBlockEntity> getSpawner(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof SpawnerBlockEntity spawner
                ? Optional.of(spawner)
                : Optional.empty();
    }

    /** Whether the entity's persistent data marks it as spawned by a block spawner. */
    public static boolean isSpawnerSpawned(Entity entity) {
        return entity.getPersistentData().contains(SPAWNER_POS);
    }

    /** Origin spawner position stamped by {@code BaseSpawnerMixin}, or {@link BlockPos#ZERO} when absent. */
    public static BlockPos spawnerOrigin(Entity entity) {
        var data = entity.getPersistentData();
        return data.contains(SPAWNER_POS)
                ? BlockPos.of(data.getLong(SPAWNER_POS).orElse(0L))
                : BlockPos.ZERO;
    }
}