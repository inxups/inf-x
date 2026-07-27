package com.pixulse.infx.world;

/** Time boundaries used by MITE R196's cooperative bed fast-forward. */
public final class R196BedRules {
    public static final int DAY_LENGTH = 24_000;
    public static final int ADJUSTED_TIME_OFFSET = 6_000;
    public static final int SLEEP_START = 21_000;
    public static final int LAST_FAST_FORWARD_TICK = 4_000;
    public static final int SUNRISE = 5_000;
    public static final int DEEP_SLEEP_TICKS = 100;
    public static final int EFFECT_CLEAR_TICKS = 1_000;
    public static final int WELL_RESTED_TICKS = 6_000;

    private R196BedRules() {}

    /** Converts the vanilla 06:00-origin clock into R196's midnight-origin display clock. */
    public static long adjustedTime(long overworldClockTime) {
        return Math.floorMod(overworldClockTime + ADJUSTED_TIME_OFFSET, DAY_LENGTH);
    }

    /** R196 permits collective fast-forward from 21:00 until, but not including, 04:00. */
    public static boolean isFastForwardWindow(long overworldClockTime) {
        long adjusted = adjustedTime(overworldClockTime);
        return adjusted >= SLEEP_START || adjusted < LAST_FAST_FORWARD_TICK;
    }

    /** Returns the number of clock ticks to the next R196 05:00 sunrise. */
    public static int ticksUntilSunrise(long overworldClockTime) {
        long adjusted = adjustedTime(overworldClockTime);
        long remaining = SUNRISE - adjusted;
        return (int) (remaining <= 0L ? remaining + DAY_LENGTH : remaining);
    }
}
