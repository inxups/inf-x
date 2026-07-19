package com.pixulse.infx.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.InfiniteX;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** World-persistent dedicated-server reconnect windows from MITE R196. */
public final class R196ReconnectData extends SavedData {
    public static final int GRACE_TICKS = 600;
    public static final int SUNRISE_HOUR = 5;
    public static final int LATEST_RECONNECT_HOUR = 20;
    private static final Codec<Map<String, Restriction>> RESTRICTIONS =
            Codec.unboundedMap(Codec.STRING, Restriction.CODEC);
    private static final Codec<R196ReconnectData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    RESTRICTIONS.optionalFieldOf("restrictions", Map.of()).forGetter(data -> data.restrictions))
            .apply(instance, R196ReconnectData::new));
    public static final SavedDataType<R196ReconnectData> TYPE = new SavedDataType<>(
            InfiniteX.id("r196_reconnect_limits"), R196ReconnectData::new, CODEC);

    private final Map<String, Restriction> restrictions;

    public R196ReconnectData() {
        this(Map.of());
    }

    private R196ReconnectData(Map<String, Restriction> restrictions) {
        this.restrictions = new HashMap<>(restrictions);
    }

    public static R196ReconnectData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public Optional<Restriction> restriction(UUID player) {
        return Optional.ofNullable(restrictions.get(player.toString()));
    }

    public void restrict(UUID player, long gameTime, long dayTime) {
        int adjusted = adjustedTime(dayTime);
        int hour = restrictedLogoutHour(adjusted / 1_000);
        long soonest = gameTime + (24_000L - adjusted) + hour * 1_000L;
        restrictions.put(player.toString(), new Restriction(gameTime, hour, soonest));
        setDirty();
    }

    public void clear(UUID player) {
        if (restrictions.remove(player.toString()) != null) setDirty();
    }

    public static int adjustedTime(long dayTime) {
        return (int) Math.floorMod(dayTime + 6_000L, 24_000L);
    }

    public static int adjustedHour(long dayTime) {
        return adjustedTime(dayTime) / 1_000;
    }

    static int restrictedLogoutHour(int adjustedHour) {
        return adjustedHour < SUNRISE_HOUR || adjustedHour > LATEST_RECONNECT_HOUR
                ? LATEST_RECONNECT_HOUR
                : adjustedHour;
    }

    public record Restriction(long logoutTick, int adjustedLogoutHour, long soonestReconnectTick) {
        static final Codec<Restriction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Codec.LONG.fieldOf("logout_tick").forGetter(Restriction::logoutTick),
                        Codec.INT.fieldOf("adjusted_logout_hour").forGetter(Restriction::adjustedLogoutHour),
                        Codec.LONG.fieldOf("soonest_reconnect_tick").forGetter(Restriction::soonestReconnectTick))
                .apply(instance, Restriction::new));

        public boolean mayReconnect(long gameTime, long dayTime) {
            long disconnectedTicks = Math.max(0L, gameTime - logoutTick);
            if (disconnectedTicks <= GRACE_TICKS) return true;
            int hour = adjustedHour(dayTime);
            if (hour == LATEST_RECONNECT_HOUR) return true;
            return gameTime >= soonestReconnectTick
                    && hour >= adjustedLogoutHour
                    && hour <= LATEST_RECONNECT_HOUR;
        }

        public long minimumRemainingTicks(long gameTime) {
            return Math.max(0L, soonestReconnectTick - gameTime);
        }
    }
}
