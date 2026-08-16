package com.pixulse.infx.entity;

import com.pixulse.infx.InfiniteX;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * MITE zombie behaviour applied directly to the vanilla {@code Zombie} through NeoForge events,
 * replacing the removed {@code infx_zombie} spawn-replacement entity: smart zombies, leaders,
 * the MITE block-digging switch, attribute alignment, and the burning-zombie tree-ignition AI.
 */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class ZombieEvents {
    private static final double LEADER_HEALTH_MULTIPLIER_MIN = 1.0;
    private static final double LEADER_HEALTH_MULTIPLIER_RANGE = 3.0;

    private ZombieEvents() {}

    @SubscribeEvent
    public static void finalizeSpawn(FinalizeSpawnEvent event) {
        if (event.getSpawnType() == EntitySpawnReason.LOAD
                || !(event.getEntity() instanceof Zombie zombie)
                || zombie.getType() != EntityType.ZOMBIE
                || !(zombie.level() instanceof ServerLevel level)) {
            return;
        }
        // Attribute alignment with the removed infx_zombie replacement: 5 melee, no modern armor.
        var attack = zombie.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            attack.setBaseValue(5.0);
        }
        var armor = zombie.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.setBaseValue(0.0);
        }
        // MITE smart zombie: 1 in 8 spawns smart, unlocking bare-handed digging.
        if (zombie.getRandom().nextInt(8) == 0) {
            zombie.getPersistentData().putBoolean(MonsterTactics.SMART_KEY, true);
        }
        // MITE leader zombie: 5% × tension, 2-5× health and knockback resistance.
        float tension = MonsterTactics.difficultyTension(level, zombie.blockPosition());
        if (zombie.getRandom().nextFloat() < tension * 0.05F) {
            var maxHealth = zombie.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.addPermanentModifier(new AttributeModifier(
                        InfiniteX.id("zombie_leader_health"),
                        LEADER_HEALTH_MULTIPLIER_MIN + zombie.getRandom().nextDouble() * LEADER_HEALTH_MULTIPLIER_RANGE,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
            var knockback = zombie.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (knockback != null) {
                knockback.addPermanentModifier(new AttributeModifier(
                        InfiniteX.id("zombie_leader_knockback"),
                        0.5 + zombie.getRandom().nextDouble() * 0.25,
                        AttributeModifier.Operation.ADD_VALUE));
            }
            zombie.setHealth(zombie.getMaxHealth());
        }
    }

    /** MITE: a zombie that is hit once by a player becomes permanently smart. */
    @SubscribeEvent
    public static void smartenOnPlayerHit(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Zombie zombie
                && zombie.getType() == EntityType.ZOMBIE
                && event.getSource().isDirect()
                && event.getSource().getEntity() instanceof Player) {
            zombie.getPersistentData().putBoolean(MonsterTactics.SMART_KEY, true);
        }
    }

    /**
     * MITE burning-zombie tree ignition: every 40 ticks a burning zombie seeks the nearest log
     * with a player near its canopy, then sets fire to it on arrival. Modern burning spread is
     * not the old chance-based model, so the target log is ignited directly.
     */
    @SubscribeEvent
    public static void burnTree(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Zombie zombie)
                || !(zombie.level() instanceof ServerLevel level)
                || !zombie.isOnFire()
                || zombie.tickCount % 40 != 0
                || !level.getGameRules().get(GameRules.MOB_GRIEFING)) {
            return;
        }
        igniteNearestTree(level, zombie);
    }

    /** Ignites the nearest player-lit log once the zombie is within reach; returns whether fire was set. */
    public static boolean igniteNearestTree(ServerLevel level, Zombie zombie) {
        BlockPos log = nearestLog(level, zombie);
        if (log == null || !playerNearTree(level, log)) {
            return false;
        }
        if (zombie.distanceToSqr(Vec3.atCenterOf(log)) >= 9.0) {
            zombie.getNavigation().moveTo(log.getX() + 0.5, log.getY(), log.getZ() + 0.5, 1.0);
            return false;
        }
        BlockPos firePos = log.above();
        if (!level.isEmptyBlock(firePos) || !BaseFireBlock.canBePlacedAt(level, firePos, Direction.UP)) {
            return false;
        }
        level.setBlockAndUpdate(firePos, BaseFireBlock.getState(level, firePos));
        return true;
    }

    private static BlockPos nearestLog(ServerLevel level, Zombie zombie) {
        BlockPos origin = zombie.blockPosition();
        BlockPos nearest = null;
        double nearestDistance = 16.0 * 16.0;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-16, -16, -16), origin.offset(16, 16, 16))) {
            BlockState state = level.getBlockState(pos);
            if (!state.is(BlockTags.LOGS)) {
                continue;
            }
            double distance = pos.distToCenterSqr(origin.getX(), origin.getY(), origin.getZ());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = pos.immutable();
            }
        }
        return nearest;
    }

    /** MITE requires a player standing at the tree's y+2..y+9 within 5.6 blocks horizontally. */
    private static boolean playerNearTree(ServerLevel level, BlockPos log) {
        AABB box = new AABB(log).inflate(5.6);
        return level.getEntitiesOfClass(
                        Player.class,
                        box,
                        player -> {
                            int py = player.getBlockY();
                            return py >= log.getY() + 2 && py <= log.getY() + 9;
                        })
                .stream()
                .findAny()
                .isPresent();
    }
}
