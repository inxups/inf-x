package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.datafixers.util.Either;
import com.pixulse.infx.InfiniteX;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.junit.jupiter.api.Test;

class StructureGenerationGatesTest {
    private static final ResourceKey<Structure> VILLAGE = BuiltinStructures.VILLAGE_PLAINS;
    private static final ResourceKey<Structure> OUTPOST = BuiltinStructures.PILLAGER_OUTPOST;
    private static final ResourceKey<Structure> MANSION = BuiltinStructures.WOODLAND_MANSION;
    private static final ResourceKey<Structure> MONUMENT = BuiltinStructures.OCEAN_MONUMENT;
    private static final ResourceKey<Structure> RUINED_PORTAL = BuiltinStructures.RUINED_PORTAL_STANDARD;
    private static final ResourceKey<Structure> RUINED_PORTAL_DESERT = BuiltinStructures.RUINED_PORTAL_DESERT;
    private static final ResourceKey<Structure> RUINED_PORTAL_JUNGLE = BuiltinStructures.RUINED_PORTAL_JUNGLE;
    private static final ResourceKey<Structure> RUINED_PORTAL_MOUNTAIN = BuiltinStructures.RUINED_PORTAL_MOUNTAIN;
    private static final ResourceKey<Structure> RUINED_PORTAL_OCEAN = BuiltinStructures.RUINED_PORTAL_OCEAN;
    private static final ResourceKey<Structure> RUINED_PORTAL_SWAMP = BuiltinStructures.RUINED_PORTAL_SWAMP;
    private static final ResourceKey<Structure> RUINED_PORTAL_NETHER = BuiltinStructures.RUINED_PORTAL_NETHER;
    private static final ResourceKey<Structure> SHIPWRECK = BuiltinStructures.SHIPWRECK;
    private static final ResourceKey<Structure> SHIPWRECK_BEACHED = BuiltinStructures.SHIPWRECK_BEACHED;
    private static final ResourceKey<Structure> ANCIENT_CITY = BuiltinStructures.ANCIENT_CITY;
    private static final ResourceKey<Structure> TRIAL_CHAMBERS = BuiltinStructures.TRIAL_CHAMBERS;
    private static final TagKey<Structure> TEST_TAG =
            TagKey.create(Registries.STRUCTURE, InfiniteX.id("test_structure_gate"));

    @Test
    void villageRuleRequiresItsExactDayAndWorldMilestone() {
        Holder<Structure> village = holder(VILLAGE, StructureTags.VILLAGE);

        assertFalse(StructureGenerationGates.allows(
                Level.OVERWORLD,
                village,
                progress(VillageProgression.VILLAGE_DAY - 1L),
                StructureGenerationGates.rules()));
        assertFalse(StructureGenerationGates.allows(
                Level.OVERWORLD,
                village,
                progress(VillageProgression.VILLAGE_DAY),
                StructureGenerationGates.rules()));
        assertTrue(StructureGenerationGates.allows(
                Level.OVERWORLD,
                village,
                progress(VillageProgression.VILLAGE_DAY, StructureGenerationGates.WorldMilestone.IRON_TOOL_CRAFTED),
                StructureGenerationGates.rules()));
    }

    @Test
    void ancientCityAndTrialChambersArePermanentlySuppressedInTheOverworld() {
        StructureGenerationGates.WorldProgressSnapshot fullyUnlocked = progress(
                Long.MAX_VALUE,
                StructureGenerationGates.WorldMilestone.IRON_TOOL_CRAFTED,
                StructureGenerationGates.WorldMilestone.END_CONQUERED);

        assertFalse(StructureGenerationGates.allows(
                Level.OVERWORLD, holder(ANCIENT_CITY), fullyUnlocked, StructureGenerationGates.rules()));
        assertFalse(StructureGenerationGates.allows(
                Level.OVERWORLD, holder(TRIAL_CHAMBERS), fullyUnlocked, StructureGenerationGates.rules()));
        assertTrue(StructureGenerationGates.allows(
                Level.NETHER, holder(ANCIENT_CITY), fullyUnlocked, StructureGenerationGates.rules()));
        assertTrue(StructureGenerationGates.allows(
                Level.NETHER, holder(TRIAL_CHAMBERS), fullyUnlocked, StructureGenerationGates.rules()));
        assertTrue(StructureGenerationGates.allows(
                Underworld.LEVEL,
                holder(Underworld.ANCIENT_CITY),
                fullyUnlocked,
                StructureGenerationGates.rules()));
    }

    @Test
    void progressionGatesRequireTheirSpecifiedWorldMilestones() {
        assertFalse(StructureGenerationGates.allows(
                Level.OVERWORLD,
                holder(OUTPOST),
                progress(VillageProgression.VILLAGE_DAY - 1L, StructureGenerationGates.WorldMilestone.IRON_TOOL_CRAFTED),
                StructureGenerationGates.rules()));
        assertFalse(StructureGenerationGates.allows(
                Level.OVERWORLD,
                holder(OUTPOST),
                progress(VillageProgression.VILLAGE_DAY),
                StructureGenerationGates.rules()));
        assertTrue(StructureGenerationGates.allows(
                Level.OVERWORLD,
                holder(OUTPOST),
                progress(VillageProgression.VILLAGE_DAY, StructureGenerationGates.WorldMilestone.IRON_TOOL_CRAFTED),
                StructureGenerationGates.rules()));

        assertFalse(StructureGenerationGates.allows(
                Level.OVERWORLD, holder(MANSION), progress(1L), StructureGenerationGates.rules()));
        assertTrue(StructureGenerationGates.allows(
                Level.OVERWORLD,
                holder(MANSION),
                progress(1L, StructureGenerationGates.WorldMilestone.MANSION_EXPERIENCE_HELD),
                StructureGenerationGates.rules()));

        assertFalse(StructureGenerationGates.allows(
                Level.OVERWORLD, holder(MONUMENT), progress(1L), StructureGenerationGates.rules()));
        assertTrue(StructureGenerationGates.allows(
                Level.OVERWORLD,
                holder(MONUMENT),
                progress(1L, StructureGenerationGates.WorldMilestone.NETHER_FORTRESS_ENTERED),
                StructureGenerationGates.rules()));

        for (ResourceKey<Structure> portal : List.of(
                RUINED_PORTAL,
                RUINED_PORTAL_DESERT,
                RUINED_PORTAL_JUNGLE,
                RUINED_PORTAL_MOUNTAIN,
                RUINED_PORTAL_OCEAN,
                RUINED_PORTAL_SWAMP)) {
            assertFalse(StructureGenerationGates.allows(
                    Level.OVERWORLD,
                    holder(portal, StructureTags.RUINED_PORTAL),
                    progress(1L),
                    StructureGenerationGates.rules()));
            assertTrue(StructureGenerationGates.allows(
                    Level.OVERWORLD,
                    holder(portal, StructureTags.RUINED_PORTAL),
                    progress(1L, StructureGenerationGates.WorldMilestone.NETHER_ENTERED),
                    StructureGenerationGates.rules()));
        }
        assertTrue(StructureGenerationGates.allows(
                Level.NETHER,
                holder(RUINED_PORTAL_NETHER, StructureTags.RUINED_PORTAL),
                progress(1L),
                StructureGenerationGates.rules()));

        for (ResourceKey<Structure> shipwreck : List.of(SHIPWRECK, SHIPWRECK_BEACHED)) {
            assertFalse(StructureGenerationGates.allows(
                    Level.OVERWORLD,
                    holder(shipwreck, StructureTags.SHIPWRECK),
                    progress(1L),
                    StructureGenerationGates.rules()));
            assertTrue(StructureGenerationGates.allows(
                    Level.OVERWORLD,
                    holder(shipwreck, StructureTags.SHIPWRECK),
                    progress(1L, StructureGenerationGates.WorldMilestone.MONUMENT_GUARDIAN_KILLED),
                    StructureGenerationGates.rules()));
        }
    }

    @Test
    void mansionGateRequiresAnOnlinePlayerToCurrentlyHoldOneHundredThousandExperience() {
        int threshold = StructureGenerationGates.MANSION_EXPERIENCE_REQUIREMENT;

        assertFalse(StructureGenerationGates.hasMansionExperience(IntStream.of(threshold - 1, 1)));
        assertTrue(StructureGenerationGates.hasMansionExperience(IntStream.of(threshold)));
        assertTrue(StructureGenerationGates.hasMansionExperience(IntStream.of(threshold + 1)));
        assertFalse(StructureGenerationGates.hasMansionExperience(IntStream.of(threshold - 1)));
    }

    @Test
    void selectorsSupportExactKeysAndLoadedTags() {
        Holder<Structure> taggedOutpost = holder(OUTPOST, TEST_TAG);
        Holder<Structure> untaggedOutpost = holder(OUTPOST);
        List<StructureGenerationGates.StructureGate> rules = List.of(
                new StructureGenerationGates.StructureGate(
                        InfiniteX.id("outpost_by_key"),
                        Set.of(Level.OVERWORLD),
                        StructureGenerationGates.StructureSelector.key(OUTPOST),
                        StructureGenerationGates.Conditions.afterDay(5L)),
                new StructureGenerationGates.StructureGate(
                        InfiniteX.id("outpost_by_tag"),
                        Set.of(Level.OVERWORLD),
                        StructureGenerationGates.StructureSelector.tag(TEST_TAG),
                        StructureGenerationGates.Conditions.milestone(
                                StructureGenerationGates.WorldMilestone.END_CONQUERED)));

        assertFalse(StructureGenerationGates.allows(Level.OVERWORLD, taggedOutpost, progress(4L), rules));
        assertFalse(StructureGenerationGates.allows(Level.OVERWORLD, taggedOutpost, progress(5L), rules));
        assertTrue(StructureGenerationGates.allows(
                Level.OVERWORLD,
                taggedOutpost,
                progress(5L, StructureGenerationGates.WorldMilestone.END_CONQUERED),
                rules));
        assertTrue(StructureGenerationGates.allows(Level.OVERWORLD, untaggedOutpost, progress(5L), rules));
    }

    @Test
    void conditionsComposeAgainstTheImmutableWorldSnapshot() {
        StructureGenerationGates.GateCondition all = StructureGenerationGates.Conditions.allOf(
                StructureGenerationGates.Conditions.afterDay(10L),
                StructureGenerationGates.Conditions.firstCompletion("bookcase"));
        StructureGenerationGates.GateCondition any = StructureGenerationGates.Conditions.anyOf(
                StructureGenerationGates.Conditions.firstCompletion("bookcase"),
                StructureGenerationGates.Conditions.milestone(
                        StructureGenerationGates.WorldMilestone.END_CONQUERED));

        assertFalse(all.test(progress(10L)));
        assertTrue(all.test(progress(10L, Set.of("bookcase"))));
        assertFalse(any.test(progress(1L)));
        assertTrue(any.test(progress(1L, Set.of("bookcase"))));
        assertTrue(any.test(progress(1L, StructureGenerationGates.WorldMilestone.END_CONQUERED)));
    }

    @Test
    void dimensionMismatchAndMissingRulesNeverBlockGeneration() {
        Holder<Structure> village = holder(VILLAGE, StructureTags.VILLAGE);

        assertTrue(StructureGenerationGates.allows(
                Level.NETHER,
                village,
                StructureGenerationGates.WorldProgressSnapshot.locked(),
                StructureGenerationGates.rules()));
        assertTrue(StructureGenerationGates.allows(
                Level.OVERWORLD,
                holder(OUTPOST),
                StructureGenerationGates.WorldProgressSnapshot.locked(),
                List.of()));
    }

    @Test
    void everyOverlappingRuleMustBeUnlocked() {
        Holder<Structure> taggedOutpost = holder(OUTPOST, TEST_TAG);
        List<StructureGenerationGates.StructureGate> rules = List.of(
                new StructureGenerationGates.StructureGate(
                        InfiniteX.id("all_outposts"),
                        Set.of(Level.OVERWORLD),
                        StructureGenerationGates.StructureSelector.tag(TEST_TAG),
                        StructureGenerationGates.Conditions.afterDay(10L)),
                new StructureGenerationGates.StructureGate(
                        InfiniteX.id("special_outpost"),
                        Set.of(Level.OVERWORLD),
                        StructureGenerationGates.StructureSelector.key(OUTPOST),
                        StructureGenerationGates.Conditions.milestone(
                                StructureGenerationGates.WorldMilestone.END_CONQUERED)));

        assertFalse(StructureGenerationGates.allows(
                Level.OVERWORLD,
                taggedOutpost,
                progress(9L, StructureGenerationGates.WorldMilestone.END_CONQUERED),
                rules));
        assertFalse(StructureGenerationGates.allows(Level.OVERWORLD, taggedOutpost, progress(10L), rules));
        assertTrue(StructureGenerationGates.allows(
                Level.OVERWORLD,
                taggedOutpost,
                progress(10L, StructureGenerationGates.WorldMilestone.END_CONQUERED),
                rules));
    }

    @Test
    void chunkGeneratorMixinRemainsRegistered() throws Exception {
        try (InputStream stream = StructureGenerationGatesTest.class
                .getClassLoader()
                .getResourceAsStream("infx.mixins.json")) {
            assertNotNull(stream);
            String mixins = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(mixins.contains("\"ChunkGeneratorMixin\""));
        }
    }

    private static StructureGenerationGates.WorldProgressSnapshot progress(
            long day, StructureGenerationGates.WorldMilestone... milestones) {
        return progress(day, Set.of(), milestones);
    }

    private static StructureGenerationGates.WorldProgressSnapshot progress(long day, Set<String> firstCompletions) {
        return progress(day, firstCompletions, new StructureGenerationGates.WorldMilestone[0]);
    }

    private static StructureGenerationGates.WorldProgressSnapshot progress(
            long day,
            Set<String> firstCompletions,
            StructureGenerationGates.WorldMilestone... milestones) {
        EnumSet<StructureGenerationGates.WorldMilestone> completed =
                EnumSet.noneOf(StructureGenerationGates.WorldMilestone.class);
        Collections.addAll(completed, milestones);
        return new StructureGenerationGates.WorldProgressSnapshot(day, completed, firstCompletions);
    }

    private static Holder<Structure> holder(ResourceKey<Structure> key, TagKey<Structure>... tags) {
        Set<TagKey<Structure>> boundTags = Set.copyOf(Arrays.asList(tags));
        return new Holder<>() {
            @Override
            public Structure value() {
                throw new UnsupportedOperationException("The structure value is irrelevant to selector tests");
            }

            @Override
            public boolean isBound() {
                return true;
            }

            @Override
            public boolean areComponentsBound() {
                return true;
            }

            @Override
            public boolean is(Identifier identifier) {
                return key.identifier().equals(identifier);
            }

            @Override
            public boolean is(ResourceKey<Structure> candidate) {
                return key.equals(candidate);
            }

            @Override
            public boolean is(Predicate<ResourceKey<Structure>> predicate) {
                return predicate.test(key);
            }

            @Override
            public boolean is(TagKey<Structure> tag) {
                return boundTags.contains(tag);
            }

            @Override
            public boolean is(Holder<Structure> other) {
                return this == other;
            }

            @Override
            public Stream<TagKey<Structure>> tags() {
                return boundTags.stream();
            }

            @Override
            public DataComponentMap components() {
                return DataComponentMap.EMPTY;
            }

            @Override
            public Either<ResourceKey<Structure>, Structure> unwrap() {
                return Either.left(key);
            }

            @Override
            public Optional<ResourceKey<Structure>> unwrapKey() {
                return Optional.of(key);
            }

            @Override
            public Holder.Kind kind() {
                return Holder.Kind.REFERENCE;
            }

            @Override
            public boolean canSerializeIn(HolderOwner<Structure> owner) {
                return true;
            }
        };
    }
}
