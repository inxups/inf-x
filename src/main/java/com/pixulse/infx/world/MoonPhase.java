package com.pixulse.infx.world;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** The INFX lunar calendar shared by livestock and hostile-mob rules. */
public enum MoonPhase {
    NORMAL,
    NEW,
    FULL,
    BLOOD,
    BLUE,
    YELLOW,
    PHANTOM;

    /** MITE uses a 24,000-tick day and an eight-day base moon cycle. */
    public static final long DAY_TICKS = 24_000L;
    public static final int BASE_CYCLE_DAYS = 8;
    /** MITE daytime ends at 13:00 and begins again just after 23:00. */
    public static final long NIGHT_START_TICK = 13_000L;
    public static final long NIGHT_END_TICK_EXCLUSIVE = 23_001L;

    public static MoonPhase at(Level level) {
        return atTime(level.getOverworldClockTime());
    }

    public static MoonPhase atTime(long overworldClockTime) {
        return atDay(dayAt(overworldClockTime));
    }

    /**
     * Keeps the data-generated celestial texture in lockstep with MITE's {@code getMoonPhase}.
     * Day one starts on the waning-gibbous sprite, while day eight is a full moon.
     */
    public static net.minecraft.world.level.MoonPhase visualPhaseAtTime(long overworldClockTime) {
        int phase = (int) Math.floorMod(
                Math.floorDiv(overworldClockTime, DAY_TICKS) + 1L, BASE_CYCLE_DAYS);
        return net.minecraft.world.level.MoonPhase.values()[phase];
    }

    public static long dayAt(long overworldClockTime) {
        return Math.max(1L, Math.floorDiv(overworldClockTime, DAY_TICKS) + 1L);
    }

    public static boolean isOverworld(ResourceKey<Level> dimension) {
        return dimension.equals(Level.OVERWORLD);
    }

    public static boolean isOverworld(Level level) {
        return isOverworld(level.dimension());
    }

    public static boolean isNight(Level level) {
        return isNightTime(level.getOverworldClockTime());
    }

    public static boolean isNightTime(long overworldClockTime) {
        long time = Math.floorMod(overworldClockTime, DAY_TICKS);
        return time >= NIGHT_START_TICK && time < NIGHT_END_TICK_EXCLUSIVE;
    }

    /** True for this calendar phase anywhere in the main surface world. */
    public boolean isActiveInOverworld(Level level) {
        return isOverworld(level) && at(level) == this;
    }

    /** True only while this phase is visible during a MITE Overworld night. */
    public boolean isActiveInOverworldAtNight(Level level) {
        return isActiveInOverworld(level) && isNight(level);
    }

    public static MoonPhase atDay(long day) {
        if (day % 128L == 0L) return BLUE;
        if ((day + 8L) % 128L == 0L) return PHANTOM;
        if (day % 32L == 0L) return BLOOD;
        if ((day + 8L) % 32L == 0L) return YELLOW;
        // MITE: getMoonPhase() == (day % 8) with 0 = full moon, so day 8 is full, day 12 new.
        int vanillaPhase = (int) Math.floorMod(day, BASE_CYCLE_DAYS);
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

    public boolean allowsSleep() {
        return this != BLOOD;
    }
}
