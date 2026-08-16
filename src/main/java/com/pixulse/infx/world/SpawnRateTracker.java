package com.pixulse.infx.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.InfiniteX;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * MITE hostile-mob spawning-rate counters ({@code SpawnerAnimals.calcEffectiveHostileMobSpawningRateModifier}).
 * Each day may randomly roll a halved, doubled or disabled hostile spawn rate for a few thousand
 * ticks; a blood moon or thunderstorm forces the rate back to 1.0 so a night's offensive is never
 * throttled. Stored on the overworld like {@link BlightTracker}.
 */
public final class SpawnRateTracker extends SavedData {
    private static final Codec<SpawnRateTracker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.optionalFieldOf("decreased", 0).forGetter(tracker -> tracker.decreased),
                    Codec.INT.optionalFieldOf("increased", 0).forGetter(tracker -> tracker.increased),
                    Codec.INT.optionalFieldOf("none", 0).forGetter(tracker -> tracker.none),
                    Codec.LONG.optionalFieldOf("lastTick", 0L).forGetter(tracker -> tracker.lastTick))
            .apply(instance, SpawnRateTracker::new));
    public static final SavedDataType<SpawnRateTracker> TYPE = new SavedDataType<>(
            InfiniteX.id("infx_hostile_spawn_rate"), SpawnRateTracker::new, CODEC);

    private int decreased;
    private int increased;
    private int none;
    private long lastTick;

    public SpawnRateTracker() {}

    private SpawnRateTracker(int decreased, int increased, int none, long lastTick) {
        this.decreased = decreased;
        this.increased = increased;
        this.none = none;
        this.lastTick = lastTick;
    }

    public static SpawnRateTracker get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    /** MITE modifier assembly: ×0.5/×2/×0 counters, with a blood-moon/thunder floor of 1.0. */
    public static float modifierForCounters(int decreased, int increased, int none, boolean bloodMoonOrThunder) {
        float modifier = 1.0F;
        if (decreased > 0) {
            modifier *= 0.5F;
        }
        if (increased > 0) {
            modifier *= 2.0F;
        }
        if (none > 0) {
            modifier = 0.0F;
        }
        if (modifier < 1.0F && bloodMoonOrThunder) {
            modifier = 1.0F;
        }
        return modifier;
    }

    public float modifier(ServerLevel level) {
        if (!MoonPhase.isOverworld(level)) {
            return 1.0F;
        }
        advance(level);
        boolean floor = MoonPhase.BLOOD.isActiveInOverworld(level) || level.isThundering();
        return modifierForCounters(decreased, increased, none, floor);
    }

    /** Advances the daily counters by the elapsed ticks; the 1-in-24000 daily roll starts one. */
    private void advance(ServerLevel level) {
        long tick = level.getGameTime();
        long elapsed = Math.max(0L, Math.min(tick - lastTick, 200L));
        lastTick = tick;
        var random = level.getRandom();
        for (int i = 0; i < elapsed; i++) {
            if (decreased > 0) {
                decreased--;
            } else if (random.nextInt(24_000) == 0) {
                decreased = random.nextInt(4_000) + 1;
            }
            if (increased > 0) {
                increased--;
            } else if (random.nextInt(24_000) == 0) {
                increased = random.nextInt(2_000);
            }
            if (none > 0) {
                none--;
            } else if (random.nextInt(24_000) == 0) {
                none = random.nextInt(2_000) + random.nextInt(2_000);
            }
        }
        if (elapsed > 0) {
            setDirty();
        }
    }
}
