package com.pixulse.infx.recipe;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.pixulse.infx.InfiniteX;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

/**
 * Data-driven crafting rules (recipe_rules datapack).
 *
 * <p>Rules override the {@link InfxCraftingRules} inference with explicit
 * difficulty and workbench-tier values. Rule files live in
 * {@code data/<namespace>/recipe_rules/<name>.json}; recipe tags referenced
 * by {@code target} entries are loaded from
 * {@code data/<namespace>/tags/recipe/<name>.json} with vanilla tag
 * semantics.</p>
 *
 * <p>The server parses the rules during every datapack reload and applies
 * them while matching recipes in {@link TimedCraftingEngine}. The same rules
 * are sent to clients with the recipe sync so that JEI can group and label
 * recipes with the exact server values.</p>
 *
 * <p>Matching precedence: rules whose target is an exact recipe ID win over
 * rules that only reach the recipe through a tag; inside each pass the rule
 * with the lexicographically largest rule ID wins, so a later rule file can
 * override an earlier one. When several recipes match one crafting grid, a
 * recipe covered by a rule wins over a recipe without one, which keeps the
 * rule-backed INFX items craftable next to recipes that rely on inference.</p>
 */
public final class RecipeRules {
    private static final FileToIdConverter RULE_LISTER = FileToIdConverter.json("recipe_rules");
    private static final FileToIdConverter TAG_LISTER = FileToIdConverter.json("tags/recipe");

    private static volatile LoadedRules active = LoadedRules.EMPTY;

    private RecipeRules() {}

    /** Registers the server-side reload listener. */
    public static void registerServerListener() {
        NeoForge.EVENT_BUS.addListener((AddServerReloadListenersEvent event) ->
                event.addListener(InfiniteX.id("recipe_rules"), new RuleReloadListener()));
    }

    /** Applies rules on top of the inferred profile for a concrete grid. */
    public static CraftingProfile profile(RecipeHolder<CraftingRecipe> holder, CraftingInput input) {
        return applyRule(holder.id(), InfxCraftingRules.profile(holder.value(), input));
    }

    /** Applies rules on top of the inferred profile for JEI displays. */
    public static CraftingProfile displayProfile(RecipeHolder<CraftingRecipe> holder) {
        return applyRule(holder.id(), InfxCraftingRules.displayProfile(holder.value()));
    }

    /** The active rules in client-sync form; used by the network payload. */
    public static List<RecipeRule.Resolved> resolvedRules() {
        return active.rules();
    }

    /** Whether an explicit rule targets the given recipe (used for match precedence). */
    public static Optional<RecipeRule.Resolved> ruleFor(ResourceKey<Recipe<?>> recipeKey) {
        return active.find(recipeKey);
    }

    /** Installs the rules received from the server on the client. */
    public static void setClientRules(List<RecipeRule.Resolved> rules) {
        active = new LoadedRules(List.copyOf(rules));
    }

    /** Drops client rules when the client logs out. */
    public static void clearClientRules() {
        active = LoadedRules.EMPTY;
    }

    private static CraftingProfile applyRule(ResourceKey<Recipe<?>> recipeKey, CraftingProfile inferred) {
        Optional<RecipeRule.Resolved> rule = active.find(recipeKey);
        if (rule.isEmpty()) {
            return inferred;
        }
        RecipeRule.Resolved resolved = rule.orElseThrow();
        float difficulty = resolved.difficulty().orElse(inferred.difficulty());
        BenchTier tier = resolved.workbenchTier().orElse(inferred.requiredBench());
        boolean materialGated = resolved.workbenchTier().isPresent()
                ? tier.materialGatedTier()
                : inferred.materialGated();
        return new CraftingProfile(tier, difficulty, materialGated);
    }

    /** Builds the lookup structure from parsed rule and tag files (testable without a reload). */
    static LoadedRules build(Map<Identifier, RecipeRule> rules, Map<Identifier, TagFile> tagFiles) {
        Map<TagKey<Recipe<?>>, Set<Identifier>> resolvedTags = resolveTags(tagFiles);
        List<RecipeRule.Resolved> resolved = rules.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    RecipeRule rule = entry.getValue();
                    List<Identifier> exact = rule.targets().stream()
                            .filter(target -> target instanceof RecipeRule.IdTarget)
                            .map(target -> ((RecipeRule.IdTarget) target).recipeId())
                            .toList();
                    List<Identifier> tagTargets = rule.targets().stream()
                            .filter(target -> target instanceof RecipeRule.TagTarget)
                            .flatMap(target ->
                                    resolvedTags.getOrDefault(
                                                    ((RecipeRule.TagTarget) target).tag(), Set.of())
                                            .stream())
                            .distinct()
                            .toList();
                    return new RecipeRule.Resolved(
                            entry.getKey(), exact, tagTargets, rule.difficulty(), rule.workbenchTier());
                })
                .toList();
        return new LoadedRules(resolved);
    }

    private static Map<TagKey<Recipe<?>>, Set<Identifier>> resolveTags(Map<Identifier, TagFile> tagFiles) {
        Map<TagKey<Recipe<?>>, Set<Identifier>> result = new HashMap<>();
        for (Identifier tagId : tagFiles.keySet()) {
            Set<Identifier> members = new HashSet<>();
            resolveTag(TagKey.create(Registries.RECIPE, tagId), tagFiles, members, new ArrayDeque<>());
            result.put(TagKey.create(Registries.RECIPE, tagId), Set.copyOf(members));
        }
        return result;
    }

    private static void resolveTag(
            TagKey<Recipe<?>> key,
            Map<Identifier, TagFile> tagFiles,
            Set<Identifier> members,
            Deque<Identifier> stack) {
        if (stack.contains(key.location())) {
            return;
        }
        TagFile file = tagFiles.get(key.location());
        if (file == null) {
            return;
        }
        stack.push(key.location());
        for (TagEntry entry : file.entries()) {
            if (entry.isTag()) {
                resolveTag(TagKey.create(Registries.RECIPE, entry.getId()), tagFiles, members, stack);
            } else {
                members.add(entry.getId());
            }
        }
        stack.pop();
    }

    /** The merged, tag-resolved rule set used for lookups on both sides. */
    record LoadedRules(List<RecipeRule.Resolved> rules) {
        static final LoadedRules EMPTY = new LoadedRules(List.of());

        Optional<RecipeRule.Resolved> find(ResourceKey<Recipe<?>> recipeKey) {
            Identifier id = recipeKey.identifier();
            RecipeRule.Resolved exact = rules.stream()
                    .filter(rule -> rule.exactTargets().contains(id))
                    .max(Comparator.comparing(RecipeRule.Resolved::ruleId))
                    .orElse(null);
            if (exact != null) {
                return Optional.of(exact);
            }
            return rules.stream()
                    .filter(rule -> rule.tagResolvedTargets().contains(id))
                    .max(Comparator.comparing(RecipeRule.Resolved::ruleId));
        }
    }

    private static final class RuleReloadListener extends SimplePreparableReloadListener<LoadedRules> {
        @Override
        protected LoadedRules prepare(ResourceManager manager, ProfilerFiller profiler) {
            Map<Identifier, RecipeRule> rules = new HashMap<>();
            SimpleJsonResourceReloadListener.scanDirectory(
                    manager, RULE_LISTER, JsonOps.INSTANCE, RecipeRule.CODEC, rules);
            Map<Identifier, TagFile> tagFiles = new HashMap<>();
            SimpleJsonResourceReloadListener.scanDirectory(
                    manager, TAG_LISTER, JsonOps.INSTANCE, TagFile.CODEC, tagFiles);
            return build(rules, tagFiles);
        }

        @Override
        protected void apply(LoadedRules data, ResourceManager manager, ProfilerFiller profiler) {
            active = data;
            InfiniteX.LOGGER.info("Loaded {} recipe rules", data.rules().size());
        }
    }
}
