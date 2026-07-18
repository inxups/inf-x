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
        event.createDatapackRegistryObjects(ModWorldGen.builder());
        event.createProvider(ModBlockTagsProvider::new);
        event.createProvider(ModItemTagsProvider::new);
        event.createProvider(output -> new ModLanguageProvider(output, ModLanguageProvider.Locale.EN_US));
        event.createProvider(output -> new ModLanguageProvider(output, ModLanguageProvider.Locale.ZH_CN));
        event.createProvider(ModModelProvider::new);
        event.createProvider(ModEquipmentAssetProvider::new);
        event.createProvider(ModRecipeProvider.Runner::new);
        event.createProvider(DisabledVanillaRecipesProvider::new);
        event.createProvider((output, lookup) ->
                new AdvancementProvider(output, lookup, List.of(new ModAdvancementProvider())));
        event.createProvider((output, lookup) -> new LootTableProvider(
                output,
                Set.of(),
                List.of(
                        new LootTableProvider.SubProviderEntry(
                                ModBlockLootSubProvider::new, LootContextParamSets.BLOCK),
                        new LootTableProvider.SubProviderEntry(
                                ModHorseArmorLootSubProvider::new, LootContextParamSets.CHEST),
                        new LootTableProvider.SubProviderEntry(
                                ModRustedIronLootSubProvider::new, LootContextParamSets.CHEST),
                        new LootTableProvider.SubProviderEntry(
                                ModUnderworldLootSubProvider::new, LootContextParamSets.CHEST)),
                lookup));
        event.createProvider(ModGlobalLootModifierProvider::new);
    }
}
