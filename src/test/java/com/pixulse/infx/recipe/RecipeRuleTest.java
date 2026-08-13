package com.pixulse.infx.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.world.item.crafting.Recipe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class RecipeRuleTest {
    private static final Identifier COPPER_PICKAXE = Identifier.fromNamespaceAndPath("infx", "copper_pickaxe");
    private static final Identifier IRON_SWORD = Identifier.fromNamespaceAndPath("infx", "iron_sword");

    @AfterEach
    void clearInstalledRules() {
        RecipeRules.installServerRules(RecipeRules.LoadedRules.EMPTY);
        RecipeRules.clearClientRules();
    }

    @Test
    void parsesSingleTargetRule() {
        RecipeRule rule = parse("""
                {"target": "infx:copper_pickaxe", "difficulty": 1250.0, "workbench_tier": "copper"}
                """);
        assertEquals(1, rule.targets().size());
        assertTrue(rule.targets().getFirst() instanceof RecipeRule.IdTarget);
        assertEquals(Optional.of(1250.0F), rule.difficulty());
        assertEquals(Optional.of(BenchTier.COPPER), rule.workbenchTier());
    }

    @Test
    void parsesArrayAndTagTargets() {
        RecipeRule rule = parse("""
                {"target": ["infx:a", "#infx:metal_tools"], "difficulty": 400.0}
                """);
        assertEquals(2, rule.targets().size());
        assertTrue(rule.targets().getFirst() instanceof RecipeRule.IdTarget);
        assertTrue(rule.targets().get(1) instanceof RecipeRule.TagTarget tag
                && tag.tag().location().equals(Identifier.fromNamespaceAndPath("infx", "metal_tools")));
        assertEquals(Optional.empty(), rule.workbenchTier());
    }

    @Test
    void resolvesTagsAndPrefersExactTargetsOverTags() {
        Map<Identifier, TagFile> tags = Map.of(
                Identifier.fromNamespaceAndPath("infx", "metal_tools"),
                new TagFile(List.of(
                        TagEntry.element(COPPER_PICKAXE),
                        TagEntry.tag(Identifier.fromNamespaceAndPath("infx", "all_swords"))),
                        false,
                        List.of()),
                Identifier.fromNamespaceAndPath("infx", "all_swords"),
                new TagFile(List.of(TagEntry.element(IRON_SWORD)), false, List.of()));

        RecipeRule taggedRule = rule("zz_tagged", List.of("#infx:metal_tools"), 300.0F, null);
        RecipeRule exactRule = rule("aa_exact", List.of("infx:iron_sword"), 500.0F, null);
        RecipeRules.LoadedRules loaded = RecipeRules.build(
                Map.of(
                        Identifier.fromNamespaceAndPath("infx", "zz_tagged"), taggedRule,
                        Identifier.fromNamespaceAndPath("infx", "aa_exact"), exactRule),
                tags);

        // The exact-ID rule wins over the tag rule regardless of rule ID order.
        RecipeRule.Resolved winner = loaded.find(key(IRON_SWORD)).orElseThrow();
        assertEquals(exactRule.ruleId(), winner.ruleId());
        assertEquals(Optional.of(500.0F), winner.difficulty());

        // Copper pickaxe is only reachable through the tag.
        RecipeRule.Resolved taggedWinner = loaded.find(key(COPPER_PICKAXE)).orElseThrow();
        assertEquals(taggedRule.ruleId(), taggedWinner.ruleId());
        assertTrue(taggedWinner.tagResolvedTargets().contains(COPPER_PICKAXE));

        // A recipe outside every target resolves to nothing.
        assertTrue(loaded.find(key(Identifier.fromNamespaceAndPath("minecraft", "torch"))).isEmpty());
    }

    @Test
    void laterRuleIdWinsAmongRulesOfTheSameKind() {
        RecipeRule first = rule("a_first", List.of("infx:iron_sword"), 100.0F, null);
        RecipeRule second = rule("b_second", List.of("infx:iron_sword"), 900.0F, null);
        RecipeRules.LoadedRules loaded = RecipeRules.build(
                Map.of(
                        Identifier.fromNamespaceAndPath("infx", "a_first"), first,
                        Identifier.fromNamespaceAndPath("infx", "b_second"), second),
                Map.of());
        assertEquals(second.ruleId(), loaded.find(key(IRON_SWORD)).orElseThrow().ruleId());
    }

    @Test
    void resolvesRuleTagCyclesWithoutRecursingForever() {
        Identifier cycle = Identifier.fromNamespaceAndPath("infx", "cycle");
        TagFile cycleFile = new TagFile(List.of(TagEntry.tag(cycle)), false, List.of());
        RecipeRules.LoadedRules loaded = RecipeRules.build(
                Map.of(),
                Map.of(cycle, cycleFile));
        assertTrue(loaded.rules().isEmpty());
    }

    @Test
    void clientRuleLifecycleCannotOverwriteServerRules() {
        Identifier recipeId = Identifier.fromNamespaceAndPath("infx", "test_flint_workbench");
        ResourceKey<Recipe<?>> recipeKey = key(recipeId);
        RecipeRule.Resolved serverRule = resolvedRule(recipeId, BenchTier.HAND);
        RecipeRule.Resolved clientRule = resolvedRule(recipeId, BenchTier.COPPER);
        CraftingProfile inferred = new CraftingProfile(BenchTier.FLINT, 325.0F, false);

        RecipeRules.installServerRules(new RecipeRules.LoadedRules(List.of(serverRule)));

        assertEquals(BenchTier.HAND, RecipeRules.applyServerRule(recipeKey, inferred).requiredBench());
        assertEquals(BenchTier.FLINT, RecipeRules.applyClientRule(recipeKey, inferred).requiredBench());

        RecipeRules.setClientRules(List.of(clientRule));
        assertEquals(BenchTier.COPPER, RecipeRules.applyClientRule(recipeKey, inferred).requiredBench());
        assertEquals(BenchTier.HAND, RecipeRules.applyServerRule(recipeKey, inferred).requiredBench());
        assertEquals(List.of(serverRule), RecipeRules.serverResolvedRules());

        RecipeRules.clearClientRules();
        assertEquals(BenchTier.FLINT, RecipeRules.applyClientRule(recipeKey, inferred).requiredBench());
        assertEquals(BenchTier.HAND, RecipeRules.applyServerRule(recipeKey, inferred).requiredBench());
        assertEquals(List.of(serverRule), RecipeRules.serverResolvedRules());
    }

    @Test
    void difficultyOnlyAndTierOnlyRulesAreAccepted() {
        assertTrue(parse("{\"target\": \"infx:a\", \"difficulty\": 25.0}").workbenchTier().isEmpty());
        assertTrue(parse("{\"target\": \"infx:a\", \"workbench_tier\": \"iron\"}").difficulty().isEmpty());
    }

    @Test
    void rejectsRulesWithoutAnyValue() {
        assertTrue(parseError("{\"target\": \"infx:a\"}").contains("difficulty"));
    }

    @Test
    void rejectsUnknownTierAndMalformedTargets() {
        assertTrue(parseError("{\"target\": \"infx:a\", \"workbench_tier\": \"bedrock\"}").contains("tier"));
        assertTrue(parseError("{\"target\": \"not an id\", \"difficulty\": 25.0}").contains("target"));
    }

    private static RecipeRule parse(String json) {
        return RecipeRule.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    }

    private static String parseError(String json) {
        try {
            return RecipeRule.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json))
                    .error()
                    .orElseThrow()
                    .message();
        } catch (IllegalArgumentException validation) {
            // The compact constructor validates difficulty/workbench_tier pairs.
            return validation.getMessage();
        }
    }

    private static RecipeRule rule(String ruleId, List<String> targets, float difficulty, BenchTier tier) {
        List<RecipeRule.Target> parsed = targets.stream()
                .map(text -> RecipeRule.Target.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("\"" + text + "\""))
                        .getOrThrow())
                .toList();
        return new RecipeRule(
                Identifier.fromNamespaceAndPath("infx", ruleId),
                parsed,
                Optional.of(difficulty),
                Optional.ofNullable(tier));
    }

    private static RecipeRule.Resolved resolvedRule(Identifier recipeId, BenchTier tier) {
        return new RecipeRule.Resolved(
                recipeId,
                List.of(recipeId),
                List.of(),
                Optional.of(270.0F),
                Optional.of(tier));
    }

    private static ResourceKey<Recipe<?>> key(Identifier id) {
        return ResourceKey.create(Registries.RECIPE, id);
    }
}
