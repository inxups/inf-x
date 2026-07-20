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
        return atTime(level.getOverworldClockTime());
    }

    public static R196MoonPhase atTime(long overworldClockTime) {
        return atDay(Math.max(1L, overworldClockTime / 24_000L + 1L));
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

    public int outdoorHostileSpawnDenominator() {
        return switch (this) {
            case BLUE -> 54;
            case BLOOD -> 2;
            case FULL -> 3;
            case NEW -> 6;
            default -> 4;
        };
    }

    public double fishingMultiplier() {
        return this == BLUE ? 4.0D : this == FULL ? 1.5D : this == NEW ? 0.75D : 1.0D;
    }

    public double cropMultiplier(boolean dedicatedServer) {
        return this == BLUE && !dedicatedServer ? 4.0D : 1.0D;
    }

    public boolean allowsSleep() {
        return this != BLOOD;
    }
}
