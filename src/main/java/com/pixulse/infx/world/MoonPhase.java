package com.pixulse.infx.world;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;

/** The INFX lunar calendar shared by livestock and hostile-mob rules. */
public enum MoonPhase {
    NORMAL,
    NEW,
    FULL,
    BLOOD,
    BLUE,
    YELLOW,
    PHANTOM;

    /** InfX uses a 24,000-tick day and an eight-day base moon cycle. */
    public static final long DAY_TICKS = 24_000L;
    public static final int BASE_CYCLE_DAYS = 8;
    /** InfX daytime ends at 13:00 and begins again just after 23:00. */
    public static final long NIGHT_START_TICK = 13_000L;
    public static final long NIGHT_END_TICK_EXCLUSIVE = 23_000L;

    public static MoonPhase at(Level level) {
        return atTime(level.getOverworldClockTime());
    }

    public static MoonPhase atTime(long overworldClockTime) {
        return atDay(dayAt(overworldClockTime));
    }

    /**
     * Keeps the data-generated celestial texture in lockstep with InfX's {@code getMoonPhase}.
     * Day one starts on the waning-gibbous sprite, while day eight is a full moon.
     */
    public static net.minecraft.world.level.MoonPhase visualPhaseAtTime(long overworldClockTime) {
        int phase = (int) Math.floorMod(
                Math.floorDiv(overworldClockTime, DAY_TICKS) + 1L, BASE_CYCLE_DAYS);
        return net.minecraft.world.level.MoonPhase.values()[phase];
    }

    /**
     * MITE {@code getMoonBrightness}: blood moon 0.6, harvest moon 1.0, blue moon 1.1,
     * otherwise phase factor × 0.5 + 0.75. Feeds the regional difficulty.
     */
    public static float miteMoonBrightness(long overworldClockTime) {
        return switch (atTime(overworldClockTime)) {
            case BLOOD -> 0.6F;
            case YELLOW -> 1.0F;
            case BLUE -> 1.1F;
            default -> DimensionType.MOON_BRIGHTNESS_PER_PHASE[
                            visualPhaseAtTime(overworldClockTime).index()]
                    * 0.5F + 0.75F;
        };
    }

    /**
     * MITE blood-moon storm: 6:00 (dawn) to 19:00 (sunset), raw time-of-day [0, 13_000).
     * {@code World.java:8675-8680} generates {@code WeatherEvent(first_tick_of_day + 6_000, 13_000)};
     * {@code first_tick_of_day + 6_000} lands on the unadjusted day start = raw 0 = dawn (not noon).
     */
    public static boolean isBloodMoonThunderWindow(long overworldClockTime) {
        return atTime(overworldClockTime) == BLOOD
                && Math.floorMod(overworldClockTime, DAY_TICKS) < 13_000L;
    }

    /**
     * MITE blood-moon storm countdown: the storm lasts 13,000 ticks from dawn, ending at
     * 19:00 (raw 13_000 = sunset), instead of re-arming a fresh 13,000 ticks on every pass.
     */
    public static long bloodMoonStormRemainingTicks(long overworldClockTime) {
        if (!isBloodMoonThunderWindow(overworldClockTime)) {
            return 0L;
        }
        long time = Math.floorMod(overworldClockTime, DAY_TICKS);
        return 13_000L - time;
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

    /** True only while this phase is visible during a InfX Overworld night. */
    public boolean isActiveInOverworldAtNight(Level level) {
        return isActiveInOverworld(level) && isNight(level);
    }

    public static MoonPhase atDay(long day) {
        if (day % 128L == 0L) return BLUE;
        if ((day + 8L) % 128L == 0L) return PHANTOM;
        if (day % 32L == 0L) return BLOOD;
        if ((day + 8L) % 32L == 0L) return YELLOW;
        // InfX: getMoonPhase() == (day % 8) with 0 = full moon, so day 8 is full, day 12 new.
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

    /** MITE blood-moon lightning: strikes roll 1/20000 per tick instead of 1/100000. */
    public static int lightningRollBound(ServerLevel level, int bound) {
        return BLOOD.isActiveInOverworld(level) ? 20_000 : bound;
    }

    /** MITE blood-moon all-biome rain: bypasses the hot-biome no-precipitation gate. */
    public static Biome.Precipitation bloodMoonPrecipitation(Biome biome, BlockPos pos, int seaLevel) {
        return biome.coldEnoughToSnow(pos, seaLevel)
                ? Biome.Precipitation.SNOW
                : Biome.Precipitation.RAIN;
    }
}
