package com.pixulse.infx.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.InfiniteX;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** World-wide R196 progression: first achievements, village gate and unique books. */
public final class R196WorldData extends SavedData {
    private static final Codec<Map<String, FirstCompletion>> COMPLETIONS =
            Codec.unboundedMap(Codec.STRING, FirstCompletion.CODEC);
    private static final Codec<R196WorldData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    COMPLETIONS.optionalFieldOf("first_completions", Map.of()).forGetter(data -> data.firstCompletions),
                    Codec.INT.optionalFieldOf("creation_book_mask", 0).forGetter(data -> data.creationBookMask),
                    Codec.STRING.listOf().optionalFieldOf("creation_book_components", java.util.List.of())
                            .forGetter(data -> java.util.List.copyOf(data.creationBookComponents)),
                    Codec.BOOL.optionalFieldOf("iron_tool_crafted", false).forGetter(data -> data.ironToolCrafted),
                    Codec.BOOL.optionalFieldOf("end_conquered", false).forGetter(data -> data.endConquered))
            .apply(instance, R196WorldData::new));
    public static final SavedDataType<R196WorldData> TYPE = new SavedDataType<>(
            InfiniteX.id("r196_world_progress"), R196WorldData::new, CODEC);

    private final Map<String, FirstCompletion> firstCompletions;
    private int creationBookMask;
    private final Set<String> creationBookComponents;
    private boolean ironToolCrafted;
    private boolean endConquered;

    public R196WorldData() {
        this(Map.of(), 0, java.util.List.of(), false, false);
    }

    private R196WorldData(
            Map<String, FirstCompletion> firstCompletions,
            int creationBookMask,
            java.util.List<String> creationBookComponents,
            boolean ironToolCrafted,
            boolean endConquered) {
        this.firstCompletions = new HashMap<>(firstCompletions);
        this.creationBookMask = creationBookMask;
        this.creationBookComponents = new HashSet<>(creationBookComponents);
        this.ironToolCrafted = ironToolCrafted;
        this.endConquered = endConquered;
    }

    public static R196WorldData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public synchronized boolean recordFirst(String advancement, String player, long day) {
        if (firstCompletions.putIfAbsent(advancement, new FirstCompletion(player, day)) != null) return false;
        setDirty();
        return true;
    }

    public Optional<FirstCompletion> firstCompletion(String advancement) {
        return Optional.ofNullable(firstCompletions.get(advancement));
    }

    public Map<String, FirstCompletion> firstCompletions() {
        return Map.copyOf(firstCompletions);
    }

    public boolean hasWorldAdvancement(String advancement) {
        return firstCompletions.containsKey(advancement);
    }

    public boolean ironToolCrafted() {
        return ironToolCrafted;
    }

    public void markIronToolCrafted() {
        if (!ironToolCrafted) {
            ironToolCrafted = true;
            setDirty();
        }
    }

    public synchronized boolean beginCreationBookComponent(String componentKey) {
        if (!creationBookComponents.add(componentKey)) return false;
        setDirty();
        return true;
    }

    public synchronized int claimCreationBook(net.minecraft.util.RandomSource random) {
        int remaining = R196CreationBooks.TITLES.size() - Integer.bitCount(creationBookMask);
        if (remaining <= 0) return -1;
        int selected = random.nextInt(remaining);
        for (int index = 0; index < R196CreationBooks.TITLES.size(); index++) {
            if ((creationBookMask & 1 << index) != 0) continue;
            if (selected-- > 0) continue;
            creationBookMask |= 1 << index;
            setDirty();
            return index;
        }
        return -1;
    }

    public int creationBookMask() {
        return creationBookMask;
    }

    public boolean endConquered() {
        return endConquered;
    }

    public void markEndConquered() {
        if (!endConquered) {
            endConquered = true;
            setDirty();
        }
    }

    public record FirstCompletion(String player, long day) {
        public static final Codec<FirstCompletion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Codec.STRING.fieldOf("player").forGetter(FirstCompletion::player),
                        Codec.LONG.fieldOf("day").forGetter(FirstCompletion::day))
                .apply(instance, FirstCompletion::new));
    }
}
