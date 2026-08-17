package com.pixulse.infx.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.InfiniteX;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Per-dimension ownership and protection deadlines for bone-lord summon slots. */
public final class BoneLordSummonRegistry extends SavedData {
    public static final int MAX_TROOPS_PER_LORD = 6;

    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);
    private static final Codec<Map<String, List<Troop>>> ROSTERS =
            Codec.unboundedMap(Codec.STRING, Troop.CODEC.listOf());
    private static final Codec<BoneLordSummonRegistry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ROSTERS.optionalFieldOf("rosters", Map.of()).forGetter(data -> data.rosters))
            .apply(instance, BoneLordSummonRegistry::new));
    public static final SavedDataType<BoneLordSummonRegistry> TYPE =
            new SavedDataType<>(InfiniteX.id("infx_bone_lord_summons"), BoneLordSummonRegistry::new, CODEC);

    private final Map<String, List<Troop>> rosters;

    public BoneLordSummonRegistry() {
        this(Map.of());
    }

    private BoneLordSummonRegistry(Map<String, List<Troop>> rosters) {
        this.rosters = new HashMap<>();
        rosters.forEach((lord, troops) -> this.rosters.put(lord, new ArrayList<>(troops)));
    }

    public static BoneLordSummonRegistry get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public int count(UUID lord) {
        return rosters.getOrDefault(lord.toString(), List.of()).size();
    }

    public boolean hasCapacity(UUID lord) {
        return count(lord) < MAX_TROOPS_PER_LORD;
    }

    public List<Troop> troops(UUID lord) {
        return List.copyOf(rosters.getOrDefault(lord.toString(), List.of()));
    }

    public void register(UUID lord, UUID troop, long protectedUntil) {
        releaseTroop(troop);
        List<Troop> roster = rosters.computeIfAbsent(lord.toString(), ignored -> new ArrayList<>());
        if (roster.size() >= MAX_TROOPS_PER_LORD) {
            return;
        }
        roster.add(new Troop(troop, protectedUntil));
        setDirty();
    }

    public boolean isTracked(UUID troop) {
        return lordFor(troop).isPresent();
    }

    public boolean isProtected(UUID troop, long gameTime) {
        return rosters.values().stream()
                .flatMap(List::stream)
                .anyMatch(entry -> entry.troop().equals(troop) && entry.protectedUntil() > gameTime);
    }

    public Optional<UUID> lordFor(UUID troop) {
        return rosters.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(record -> record.troop().equals(troop)))
                .map(entry -> UUID.fromString(entry.getKey()))
                .findFirst();
    }

    public void release(UUID lord, UUID troop) {
        String lordKey = lord.toString();
        List<Troop> troops = rosters.get(lordKey);
        if (troops == null || !troops.removeIf(entry -> entry.troop().equals(troop))) {
            return;
        }
        if (troops.isEmpty()) {
            rosters.remove(lordKey);
        }
        setDirty();
    }

    public void releaseTroop(UUID troop) {
        boolean changed = false;
        for (var iterator = rosters.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String, List<Troop>> entry = iterator.next();
            if (entry.getValue().removeIf(record -> record.troop().equals(troop))) {
                changed = true;
            }
            if (entry.getValue().isEmpty()) {
                iterator.remove();
            }
        }
        if (changed) {
            setDirty();
        }
    }

    /** Drops every slot owned by a destroyed bone lord without removing its surviving troops. */
    public void releaseLord(UUID lord) {
        if (rosters.remove(lord.toString()) != null) {
            setDirty();
        }
    }

    /** Removes only entities that are presently loaded and conclusively gone; unloaded troops retain their slot. */
    public void removeDestroyedLoaded(ServerLevel level, UUID lord) {
        for (Troop troop : troops(lord)) {
            Entity entity = level.getEntity(troop.troop());
            if (entity != null && (!entity.isAlive() || entity.isRemoved())) {
                release(lord, troop.troop());
            }
        }
    }

    public record Troop(UUID troop, long protectedUntil) {
        static final Codec<Troop> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        UUID_CODEC.fieldOf("troop").forGetter(Troop::troop),
                        Codec.LONG.fieldOf("protected_until").forGetter(Troop::protectedUntil))
                .apply(instance, Troop::new));
    }
}
