package com.pixulse.infx.world;

import com.pixulse.infx.registry.InfXItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * InfX fishing bite timing: the base lure wait scales with the time of day (fastest at dawn and
 * dusk), halves while it rains and when the angler carries a worm as bait.
 */
public final class FishingRules {
    private static final long BEST_BITE_TICKS = 5_500L;
    private static final long SECOND_BEST_BITE_TICKS = 17_500L;

    private FishingRules() {}

    public static int lureDelay(ServerLevel level, Player player, int rolledDelay) {
        long timeOfDay = Math.floorMod(level.getOverworldClockTime(), 24_000L);
        float timeFactor =
                Math.min(Math.abs(timeOfDay - BEST_BITE_TICKS), Math.abs(timeOfDay - SECOND_BEST_BITE_TICKS))
                        / 600.0F;
        int chance = Math.clamp((int) (600.0F * timeFactor), 600, 2400);
        if (MoonPhase.BLUE.isActiveInOverworld(level)) {
            chance = 600;
        }
        if (level.isRaining()) {
            chance /= 2;
        }
        if (hasWormBait(player)) {
            chance /= 2;
        }
        return Math.max(1, rolledDelay * chance / 600);
    }

    private static boolean hasWormBait(Player player) {
        // InfX consumes the worm from the hotbar, so only hotbar slots count as bait.
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(InfXItems.WORM.get())) {
                return true;
            }
        }
        return false;
    }
}
