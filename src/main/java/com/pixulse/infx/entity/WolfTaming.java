package com.pixulse.infx.entity;

import com.pixulse.infx.world.MoonPhase;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.EventHooks;

/**
 * InfX wolf taming: three-tier outcome, a 5-second cooldown after failure, and
 * hostile retaliation on the worst roll (except on blue-moon nights).
 */
public final class WolfTaming {
    public static final int COOLDOWN_TICKS = 100;

    public enum Kind {
        /** Vanilla wolf outcome table (EntityWolf.getTamingOutcome). */
        VANILLA,
        /** Dire wolf outcome table (EntityDireWolf.getTamingOutcome). */
        DIRE_WOLF
    }

    private WolfTaming() {}

    public static void attempt(Wolf wolf, Kind kind, Player player, InfxTameableWolf tameable) {
        if (tameable.tamingCooldown() > 0) {
            return;
        }
        int outcome = outcome(wolf, kind, player);
        if (outcome <= 0) {
            wolf.level().broadcastEntityEvent(wolf, (byte) 6);
            tameable.setTamingCooldown(COOLDOWN_TICKS);
            if (outcome < 0
                    && !MoonPhase.BLUE.isActiveInOverworldAtNight(wolf.level())
                    && (!(wolf instanceof net.minecraft.world.entity.Mob mob)
                            || MonsterEvents.withinFollowRange(mob, player))) {
                wolf.setTarget(player);
            }
            return;
        }
        if (EventHooks.onAnimalTame(wolf, player)) {
            wolf.level().broadcastEntityEvent(wolf, (byte) 6);
            return;
        }
        wolf.tame(player);
        wolf.getNavigation().stop();
        wolf.setTarget(null);
        wolf.setOrderedToSit(true);
        wolf.level().broadcastEntityEvent(wolf, (byte) 7);
    }

    private static int outcome(Wolf wolf, Kind kind, Player player) {
        float roll = wolf.getRandom().nextFloat();
        if (kind == Kind.VANILLA) {
            if (roll < 0.05F) {
                return -1;
            }
            if (roll < 0.1F) {
                return 0;
            }
            if (roll > 0.9F) {
                return 1;
            }
            roll += wolf.getRandom().nextFloat() * player.experienceLevel * 0.02F;
            return roll < 0.2F ? -1 : (roll < 0.8F ? 0 : 1);
        }
        if (roll < 0.2F) {
            return -1;
        }
        if (roll < 0.4F) {
            return 0;
        }
        if (roll > 0.95F) {
            return 1;
        }
        roll += wolf.getRandom().nextFloat() * player.experienceLevel * 0.02F;
        return roll < 0.5F ? -1 : (roll < 1.0F ? 0 : 1);
    }
}
