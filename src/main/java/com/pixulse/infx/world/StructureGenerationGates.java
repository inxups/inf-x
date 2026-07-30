package com.pixulse.infx.world;

import com.pixulse.infx.InfiniteX;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Server-authoritative gates for structures generated after a world progression milestone.
 *
 * <p>Chunk generation can run away from the server thread, so it only reads the immutable
 * snapshot published here. Add future gates to {@link #RULES}; matching gates are combined with
 * logical AND.</p>
 */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class StructureGenerationGates {
    public static final int MANSION_EXPERIENCE_REQUIREMENT = 100_000;
    public static final Identifier VILLAGE_RULE = InfiniteX.id("village");
    public static final Identifier PILLAGER_OUTPOST_RULE = InfiniteX.id("pillager_outpost");
    public static final Identifier MANSION_RULE = InfiniteX.id("mansion");
    public static final Identifier MONUMENT_RULE = InfiniteX.id("monument");
    public static final Identifier OVERWORLD_RUINED_PORTALS_RULE = InfiniteX.id("overworld_ruined_portals");
    public static final Identifier SHIPWRECK_RULE = InfiniteX.id("shipwreck");
    public static final Identifier ANCIENT_CITY_RULE = InfiniteX.id("ancient_city");
    public static final Identifier TRIAL_CHAMBERS_RULE = InfiniteX.id("trial_chambers");

    private static final GateCondition VILLAGE_REQUIREMENTS = Conditions.allOf(
            Conditions.afterDay(VillageProgression.VILLAGE_DAY),
            Conditions.milestone(WorldMilestone.IRON_TOOL_CRAFTED));
    private static final List<StructureGate> RULES = List.of(
            new StructureGate(
                    VILLAGE_RULE,
                    Set.of(Level.OVERWORLD),
                    StructureSelector.tag(StructureTags.VILLAGE),
                    VILLAGE_REQUIREMENTS),
            new StructureGate(
                    PILLAGER_OUTPOST_RULE,
                    Set.of(Level.OVERWORLD),
                    StructureSelector.key(BuiltinStructures.PILLAGER_OUTPOST),
                    VILLAGE_REQUIREMENTS),
            new StructureGate(
                    MANSION_RULE,
                    Set.of(Level.OVERWORLD),
                    StructureSelector.key(BuiltinStructures.WOODLAND_MANSION),
                    Conditions.milestone(WorldMilestone.MANSION_EXPERIENCE_HELD)),
            new StructureGate(
                    MONUMENT_RULE,
                    Set.of(Level.OVERWORLD),
                    StructureSelector.key(BuiltinStructures.OCEAN_MONUMENT),
                    Conditions.milestone(WorldMilestone.NETHER_FORTRESS_ENTERED)),
            new StructureGate(
                    OVERWORLD_RUINED_PORTALS_RULE,
                    Set.of(Level.OVERWORLD),
                    StructureSelector.tag(StructureTags.RUINED_PORTAL),
                    Conditions.milestone(WorldMilestone.NETHER_ENTERED)),
            new StructureGate(
                    SHIPWRECK_RULE,
                    Set.of(Level.OVERWORLD),
                    StructureSelector.tag(StructureTags.SHIPWRECK),
                    Conditions.milestone(WorldMilestone.MONUMENT_GUARDIAN_KILLED)),
            new StructureGate(
                    ANCIENT_CITY_RULE,
                    Set.of(Level.OVERWORLD),
                    StructureSelector.key(BuiltinStructures.ANCIENT_CITY),
                    Conditions.never()),
            new StructureGate(
                    TRIAL_CHAMBERS_RULE,
                    Set.of(Level.OVERWORLD),
                    StructureSelector.key(BuiltinStructures.TRIAL_CHAMBERS),
                    Conditions.never()));
    private static final Map<Identifier, StructureGate> RULES_BY_ID = RULES.stream()
            .collect(Collectors.toUnmodifiableMap(StructureGate::id, Function.identity()));

    private static volatile WorldProgressSnapshot snapshot = WorldProgressSnapshot.locked();

    private StructureGenerationGates() {}

    /** Returns the immutable built-in rule registry. */
    public static List<StructureGate> rules() {
        return RULES;
    }

    /**
     * Returns whether a candidate structure may start generating in this dimension.
     *
     * <p>Rules that do not match the dimension or structure do not restrict it. Every matching
     * rule must be unlocked.</p>
     */
    public static boolean allows(ResourceKey<Level> dimension, Holder<Structure> structure) {
        return allows(dimension, structure, snapshot, RULES);
    }

    static boolean allows(
            ResourceKey<Level> dimension,
            Holder<Structure> structure,
            WorldProgressSnapshot progress,
            List<StructureGate> rules) {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(structure, "structure");
        Objects.requireNonNull(progress, "progress");
        Objects.requireNonNull(rules, "rules");
        return rules.stream()
                .filter(rule -> rule.appliesTo(dimension, structure))
                .allMatch(rule -> rule.condition().test(progress));
    }

    /** Returns the published status of a named rule without touching live world state. */
    public static boolean isUnlocked(Identifier ruleId) {
        return rule(ruleId).condition().test(snapshot);
    }

    /** Evaluates a named rule against the current server-thread world state. */
    public static boolean isUnlocked(Identifier ruleId, ServerLevel level) {
        return rule(ruleId).condition().test(snapshot(level.getServer().overworld()));
    }

    /** Publishes the current shared-world progression after a server-thread state change. */
    public static void refresh(ServerLevel level) {
        snapshot = snapshot(level.getServer().overworld());
    }

    /** Returns the shared survival day used by world progression rules. */
    public static long day(ServerLevel level) {
        return Math.max(1L, level.getOverworldClockTime() / 24_000L + 1L);
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        snapshot = WorldProgressSnapshot.locked();
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        refresh(event.getServer().overworld());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        refresh(event.getServer().overworld());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        snapshot = WorldProgressSnapshot.locked();
    }

    private static StructureGate rule(Identifier ruleId) {
        StructureGate rule = RULES_BY_ID.get(ruleId);
        if (rule == null) throw new IllegalArgumentException("Unknown structure generation rule: " + ruleId);
        return rule;
    }

    private static WorldProgressSnapshot snapshot(ServerLevel level) {
        WorldData data = WorldData.get(level);
        EnumSet<WorldMilestone> milestones = EnumSet.noneOf(WorldMilestone.class);
        if (data.ironToolCrafted()) milestones.add(WorldMilestone.IRON_TOOL_CRAFTED);
        if (data.endConquered()) milestones.add(WorldMilestone.END_CONQUERED);
        if (hasMansionExperience(level.getServer().getPlayerList().getPlayers().stream()
                .filter(player -> player.isAlive())
                .mapToInt(player -> player.totalExperience))) {
            milestones.add(WorldMilestone.MANSION_EXPERIENCE_HELD);
        }
        if (data.netherEntered()) milestones.add(WorldMilestone.NETHER_ENTERED);
        if (data.netherFortressEntered()) milestones.add(WorldMilestone.NETHER_FORTRESS_ENTERED);
        if (data.monumentGuardianKilled()) milestones.add(WorldMilestone.MONUMENT_GUARDIAN_KILLED);
        return new WorldProgressSnapshot(day(level), milestones, data.firstCompletions().keySet());
    }

    static boolean hasMansionExperience(int totalExperience) {
        return totalExperience >= MANSION_EXPERIENCE_REQUIREMENT;
    }

    static boolean hasMansionExperience(IntStream totalExperiences) {
        return totalExperiences.anyMatch(StructureGenerationGates::hasMansionExperience);
    }

    /** A single named rule, scoped to dimensions and a structure ID or tag selector. */
    public record StructureGate(
            Identifier id,
            Set<ResourceKey<Level>> dimensions,
            StructureSelector selector,
            GateCondition condition) {
        public StructureGate {
            Objects.requireNonNull(id, "id");
            dimensions = Set.copyOf(dimensions);
            if (dimensions.isEmpty()) throw new IllegalArgumentException("A structure gate needs at least one dimension");
            Objects.requireNonNull(selector, "selector");
            Objects.requireNonNull(condition, "condition");
        }

        boolean appliesTo(ResourceKey<Level> dimension, Holder<Structure> structure) {
            return dimensions.contains(dimension) && selector.matches(structure);
        }
    }

    /** Selects a generated structure by exact registry key or a loaded structure tag. */
    @FunctionalInterface
    public interface StructureSelector {
        boolean matches(Holder<Structure> structure);

        static StructureSelector key(ResourceKey<Structure> key) {
            Objects.requireNonNull(key, "key");
            return structure -> structure.is(key);
        }

        static StructureSelector tag(TagKey<Structure> tag) {
            Objects.requireNonNull(tag, "tag");
            return structure -> structure.is(tag);
        }
    }

    /** A condition evaluated solely against a published immutable world-progress snapshot. */
    @FunctionalInterface
    public interface GateCondition {
        boolean test(WorldProgressSnapshot progress);
    }

    /** Built-in, composable conditions for structure generation rules. */
    public static final class Conditions {
        private static final GateCondition NEVER = progress -> false;

        private Conditions() {}

        /** A condition used to permanently suppress a structure's future generation. */
        public static GateCondition never() {
            return NEVER;
        }

        public static GateCondition afterDay(long day) {
            if (day < 1L) throw new IllegalArgumentException("Minimum day must be positive");
            return progress -> progress.day() >= day;
        }

        public static GateCondition milestone(WorldMilestone milestone) {
            Objects.requireNonNull(milestone, "milestone");
            return progress -> progress.hasMilestone(milestone);
        }

        public static GateCondition firstCompletion(String advancement) {
            if (advancement == null || advancement.isBlank()) {
                throw new IllegalArgumentException("World advancement must not be blank");
            }
            return progress -> progress.hasFirstCompletion(advancement);
        }

        public static GateCondition allOf(GateCondition... conditions) {
            List<GateCondition> all = conditions(conditions);
            return progress -> all.stream().allMatch(condition -> condition.test(progress));
        }

        public static GateCondition anyOf(GateCondition... conditions) {
            List<GateCondition> any = conditions(conditions);
            return progress -> any.stream().anyMatch(condition -> condition.test(progress));
        }

        private static List<GateCondition> conditions(GateCondition[] conditions) {
            if (conditions.length == 0) throw new IllegalArgumentException("A composite condition must not be empty");
            return List.of(conditions);
        }
    }

    /** Shared world-progress milestones exposed to structure-gate conditions. */
    public enum WorldMilestone {
        IRON_TOOL_CRAFTED,
        END_CONQUERED,
        MANSION_EXPERIENCE_HELD,
        NETHER_ENTERED,
        NETHER_FORTRESS_ENTERED,
        MONUMENT_GUARDIAN_KILLED
    }

    /** Immutable, thread-safe projection of world data and player state used by gates. */
    public record WorldProgressSnapshot(
            long day,
            Set<WorldMilestone> milestones,
            Set<String> firstCompletions) {
        public WorldProgressSnapshot {
            if (day < 0L) throw new IllegalArgumentException("Day must not be negative");
            milestones = Set.copyOf(milestones);
            firstCompletions = Set.copyOf(firstCompletions);
        }

        static WorldProgressSnapshot locked() {
            return new WorldProgressSnapshot(0L, Set.of(), Set.of());
        }

        public boolean hasMilestone(WorldMilestone milestone) {
            return milestones.contains(milestone);
        }

        public boolean hasFirstCompletion(String advancement) {
            return firstCompletions.contains(advancement);
        }
    }
}
