package com.pixulse.infx.datagen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.NonNull;

/**
 * Writes the {@code data/infx/recipe_rules/*.json} files for every INFX
 * recipe. The recipe provider re-runs its (cheap, in-memory) recipe build
 * with a discarding output so that the rules can never drift from the
 * recipes themselves.
 */
final class ModRecipeRulesProvider implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    ModRecipeRulesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.output = output;
        this.registries = registries;
    }

    @Override
    public @NonNull CompletableFuture<?> run(@NonNull CachedOutput cache) {
        return registries.thenCompose(registries -> {
            ModRecipeProvider provider = new ModRecipeProvider(registries, DiscardingRecipeOutput.INSTANCE);
            provider.buildRecipes();
            var pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe_rules");
            return CompletableFuture.allOf(provider.recipeRules().entrySet().stream()
                    .map(entry -> DataProvider.saveStable(
                            cache,
                            com.pixulse.infx.recipe.RecipeRule.CODEC,
                            entry.getValue(),
                            pathProvider.json(entry.getKey().identifier())))
                    .toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public @NonNull String getName() {
        return "InfiniteX recipe rules";
    }

    private enum DiscardingRecipeOutput implements RecipeOutput {
        INSTANCE;

        @Override
        public void accept(
                ResourceKey<Recipe<?>> id,
                Recipe<?> recipe,
                AdvancementHolder advancementHolder,
                net.neoforged.neoforge.common.conditions.ICondition... conditions) {
            // Recipes are emitted by ModRecipeProvider.Runner; only the rule
            // collection matters here.
        }

        @Override
        public Advancement.Builder advancement() {
            return Advancement.Builder.recipeAdvancement();
        }

        @Override
        public void includeRootAdvancement() {
            // Not needed for rule generation.
        }
    }
}
