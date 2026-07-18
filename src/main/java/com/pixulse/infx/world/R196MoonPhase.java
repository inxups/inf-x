package com.pixulse.infx.world;

import net.minecraft.server.level.ServerLevel;

/** The R196 lunar calendar shared by livestock and hostile-mob rules. */
public enum R196MoonPhase {
    NORMAL,
    NEW,
    FULL,
    BLOOD,
    BLUE,
    YELLOW,
    PHANTOM;

    public static R196MoonPhase at(ServerLevel level) {
        return atDay(Math.max(1L, level.getOverworldClockTime() / 24_000L + 1L));
    }

    public static R196MoonPhase atDay(long day) {
        if (day % 128L == 0L) return BLUE;
        if ((day + 8L) % 128L == 0L) return PHANTOM;
        if (day % 32L == 0L) return BLOOD;
        if ((day + 8L) % 32L == 0L) return YELLOW;
        int vanillaPhase = (int) Math.floorMod(day - 1L, 8L);
        if (vanillaPhase == 0) return FULL;
        if (vanillaPhase == 4) return NEW;
        return NORMAL;
    }
}
