package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.loot.GravelLootModifier;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

final class ModGlobalLootModifierProvider extends GlobalLootModifierProvider {
    ModGlobalLootModifierProvider(
            PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, InfiniteX.MOD_ID);
    }

    @Override
    protected void start() {
        add("gravel", new GravelLootModifier(new LootItemCondition[0], 2000));
        addHorseArmor("simple_dungeon");
        addHorseArmor("nether_bridge");
        addHorseArmor("desert_pyramid");
        addHorseArmor("jungle_temple");
        addHorseArmor("stronghold_corridor");
    }

    private void addHorseArmor(String structure) {
        Identifier target = Identifier.withDefaultNamespace("chests/" + structure);
        LootItemCondition[] conditions = {
            LootTableIdCondition.builder(target).build()
        };
        add(
                "horse_armor_" + structure,
                new AddTableLootModifier(
                        conditions,
                        1000,
                        ModHorseArmorLootSubProvider.tableKey(structure)));
    }
}
