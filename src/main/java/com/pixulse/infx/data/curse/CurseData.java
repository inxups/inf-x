package com.pixulse.infx.data.curse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.InfiniteX;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** World-persistent pending and realized witch curses, shared by every dimension. */
public final class CurseData extends SavedData {
    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);
    private static final Codec<Map<String, Entry>> ENTRIES = Codec.unboundedMap(Codec.STRING, Entry.CODEC);
    static final Codec<CurseData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ENTRIES.optionalFieldOf("entries", Map.of()).forGetter(data -> data.entries))
            .apply(instance, CurseData::new));

    public static final SavedDataType<CurseData> TYPE = new SavedDataType<>(
            InfiniteX.id("r196_witch_curses"), CurseData::new, CODEC);

    private final Map<String, Entry> entries;

    public CurseData() {
        this(Map.of());
    }

    private CurseData(Map<String, Entry> entries) {
        this.entries = new HashMap<>(entries);
    }

    public static CurseData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public Optional<Entry> entry(UUID player) {
        return Optional.ofNullable(entries.get(player.toString()));
    }

    public boolean add(UUID player, UUID witch, CurseType type, long realizationTick) {
        if (entries.containsKey(player.toString())) return false;
        entries.put(player.toString(), new Entry(witch, type, realizationTick, false, false));
        setDirty();
        return true;
    }

    public Optional<Entry> realizeIfDue(UUID player, long gameTime) {
        String key = player.toString();
        Entry current = entries.get(key);
        if (current == null || current.realized() || current.realizationTick() > gameTime) {
            return Optional.ofNullable(current);
        }
        Entry realized = current.realize();
        entries.put(key, realized);
        setDirty();
        return Optional.of(realized);
    }

    public Optional<Entry> learn(UUID player) {
        String key = player.toString();
        Entry current = entries.get(key);
        if (current == null || !current.realized() || current.known()) {
            return Optional.ofNullable(current);
        }
        Entry known = current.learn();
        entries.put(key, known);
        setDirty();
        return Optional.of(known);
    }

    public Optional<Entry> remove(UUID player) {
        Entry removed = entries.remove(player.toString());
        if (removed != null) setDirty();
        return Optional.ofNullable(removed);
    }

    public Map<UUID, Entry> removeForWitch(UUID witch) {
        Map<UUID, Entry> removed = new LinkedHashMap<>();
        entries.entrySet().removeIf(entry -> {
            if (!entry.getValue().witch().equals(witch)) return false;
            removed.put(UUID.fromString(entry.getKey()), entry.getValue());
            return true;
        });
        if (!removed.isEmpty()) setDirty();
        return Map.copyOf(removed);
    }

    public Map<UUID, Entry> entries() {
        Map<UUID, Entry> copy = new LinkedHashMap<>();
        entries.forEach((player, entry) -> copy.put(UUID.fromString(player), entry));
        return Map.copyOf(copy);
    }

    public record Entry(
            UUID witch,
            CurseType type,
            long realizationTick,
            boolean realized,
            boolean known) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        UUID_CODEC.fieldOf("witch").forGetter(Entry::witch),
                        CurseType.CODEC.fieldOf("type").forGetter(Entry::type),
                        Codec.LONG.fieldOf("realization_tick").forGetter(Entry::realizationTick),
                        Codec.BOOL.optionalFieldOf("realized", false).forGetter(Entry::realized),
                        Codec.BOOL.optionalFieldOf("known", false).forGetter(Entry::known))
                .apply(instance, Entry::new));

        Entry realize() {
            return new Entry(witch, type, realizationTick, true, known);
        }

        Entry learn() {
            return new Entry(witch, type, realizationTick, realized, true);
        }
    }
}
