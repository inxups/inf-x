package com.pixulse.infx.entity;

import com.pixulse.infx.registry.InfXAttachments;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

/** MITE's finite ordinary-spawner lifecycle: fifteen player-attributed deaths exhaust a block. */
public final class SpawnerBurnout {
    public static final int KILL_LIMIT = 15;
    public static final int PLAYER_DAMAGE_TICKS = 100;
    private static final Map<BaseSpawner, WeakReference<SpawnerBlockEntity>> BLOCK_SPAWNERS = new WeakHashMap<>();

    private SpawnerBurnout() {}

    /** Records the ordinary block spawner that created a mob, including its dimension. */
    public static void recordOrigin(Mob mob, SpawnerBlockEntity spawner) {
        if (!(mob.level() instanceof ServerLevel level)) return;
        mob.setData(
                InfXAttachments.SPAWNER_ORIGIN,
                Optional.of(GlobalPos.of(level.dimension(), spawner.getBlockPos())));
    }

    /** Registers the exact ordinary block owner before its BaseSpawner executes spawn events. */
    public static void registerSpawner(SpawnerBlockEntity spawner) {
        BaseSpawner baseSpawner = spawner.getSpawner();
        WeakReference<SpawnerBlockEntity> existing = BLOCK_SPAWNERS.get(baseSpawner);
        if (existing == null || existing.get() != spawner) {
            BLOCK_SPAWNERS.put(baseSpawner, new WeakReference<>(spawner));
        }
    }

    /** Resolves an ordinary block spawner from NeoForge's PositionCheck BaseSpawner reference. */
    public static void recordOrigin(Mob mob, BaseSpawner spawner) {
        WeakReference<SpawnerBlockEntity> reference = BLOCK_SPAWNERS.get(spawner);
        SpawnerBlockEntity blockSpawner = reference == null ? null : reference.get();
        if (blockSpawner != null) recordOrigin(mob, blockSpawner);
    }

    /** Vanilla-to-R196 replacement happens after spawn finalization, so carry the origin forward. */
    public static void copyOrigin(Mob original, Mob replacement) {
        Optional<GlobalPos> origin = original.getExistingDataOrNull(InfXAttachments.SPAWNER_ORIGIN);
        if (origin != null && origin.isPresent()) {
            replacement.setData(InfXAttachments.SPAWNER_ORIGIN, origin);
        }
    }

    /** Starts MITE's 100-tick player/tamed-wolf kill-credit window for a spawner mob. */
    public static void recordPlayerDamage(LivingEntity mob, long gameTime) {
        if (originOf(mob) == null) return;
        mob.setData(InfXAttachments.SPAWNER_PLAYER_DAMAGE_UNTIL, playerDamageExpiresAt(gameTime));
    }

    public static long playerDamageExpiresAt(long gameTime) {
        return gameTime + PLAYER_DAMAGE_TICKS;
    }

    public static boolean hasActivePlayerDamageCredit(long gameTime, long expiresAt) {
        return gameTime <= expiresAt;
    }

    /** Applies one kill to the originating ordinary block spawner when MITE credit remains active. */
    public static boolean countAttributedDeath(LivingEntity mob) {
        if (!(mob.level() instanceof ServerLevel currentLevel)) return false;
        Long expiresAt = mob.getExistingDataOrNull(InfXAttachments.SPAWNER_PLAYER_DAMAGE_UNTIL);
        if (expiresAt == null || !hasActivePlayerDamageCredit(currentLevel.getGameTime(), expiresAt)) {
            return false;
        }

        GlobalPos origin = originOf(mob);
        if (origin == null) return false;
        ServerLevel spawnerLevel = currentLevel.getServer().getLevel(origin.dimension());
        if (spawnerLevel == null || !spawnerLevel.hasChunkAt(origin.pos())) return false;
        if (!(spawnerLevel.getBlockEntity(origin.pos()) instanceof SpawnerBlockEntity spawner)) return false;

        int kills = killCount(spawner);
        if (isExhausted(kills)) return false;

        spawner.setData(InfXAttachments.SPAWNER_KILLS, kills + 1);
        spawner.setChanged();
        var state = spawner.getBlockState();
        spawnerLevel.sendBlockUpdated(origin.pos(), state, state, Block.UPDATE_CLIENTS);
        return true;
    }

    public static int killCount(SpawnerBlockEntity spawner) {
        Integer kills = spawner.getExistingDataOrNull(InfXAttachments.SPAWNER_KILLS);
        return kills == null ? 0 : Math.clamp(kills, 0, KILL_LIMIT);
    }

    public static boolean isExhausted(int kills) {
        return kills >= KILL_LIMIT;
    }

    public static boolean isExhausted(SpawnerBlockEntity spawner) {
        return isExhausted(killCount(spawner));
    }

    private static GlobalPos originOf(LivingEntity mob) {
        Optional<GlobalPos> origin = mob.getExistingDataOrNull(InfXAttachments.SPAWNER_ORIGIN);
        return origin == null ? null : origin.orElse(null);
    }
}
