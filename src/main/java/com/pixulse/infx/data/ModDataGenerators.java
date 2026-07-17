package com.pixulse.infx.data;

import java.util.List;
import java.util.Set;

import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class ModDataGenerators {
    private ModDataGenerators() {}

    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(ModBlockTagsProvider::new);
        event.createProvider(ModItemTagsProvider::new);
        event.createProvider(ModRecipeProvider.Runner::new);
        event.createProvider(DisabledVanillaRecipesProvider::new);
        event.createProvider((output, lookup) ->
                new AdvancementProvider(output, lookup, List.of(new ModAdvancementProvider())));
        event.createProvider((output, lookup) -> new LootTableProvider(
                output,
                Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(
                        ModBlockLootSubProvider::new, LootContextParamSets.BLOCK)),
                lookup));
        event.createProvider(ModGlobalLootModifierProvider::new);
    }
}
