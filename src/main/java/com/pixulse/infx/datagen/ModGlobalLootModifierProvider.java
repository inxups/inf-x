package com.pixulse.infx.datagen;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.loot.GravelLootModifier;
import com.pixulse.infx.loot.GlassShardLootModifier;
import com.pixulse.infx.loot.LegacyProgressionLootFilter;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
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
        add("glass_shards", new GlassShardLootModifier(new LootItemCondition[0], 2000));
        add("modern_progression_filter", new com.pixulse.infx.loot.ModernProgressionLootFilter(
                new LootItemCondition[0], 3000));
        add("creation_books", new com.pixulse.infx.loot.CreationBookLootModifier(
                new LootItemCondition[0], 2500));
        addHorseArmor("simple_dungeon");
        addHorseArmor("nether_bridge");
        addHorseArmor("desert_pyramid");
        addHorseArmor("jungle_temple");
        addHorseArmor("stronghold_corridor");
        addRustedIron("simple_dungeon");
        addRustedIron("abandoned_mineshaft");
        for (String structure : ModLegacyStructureLootSubProvider.TARGETS) {
            addStructure(structure);
        }
        add("progression_filter", new LegacyProgressionLootFilter(new LootItemCondition[0], 900));
        addPiglinBarterDrops();
    }

    /** InfX piglins barter through player kills: 25% chance of one barter payout per kill. */
    private void addPiglinBarterDrops() {
        LootItemCondition[] conditions = {
            LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/piglin")).build(),
            LootItemKilledByPlayerCondition.killedByPlayer().build(),
            LootItemRandomChanceCondition.randomChance(0.25F).build()
        };
        add("piglin_barter_drops", new com.pixulse.infx.loot.PiglinBarterDropLootModifier(conditions, 800));
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

    private void addRustedIron(String structure) {
        Identifier target = Identifier.withDefaultNamespace("chests/" + structure);
        LootItemCondition[] conditions = {
            LootTableIdCondition.builder(target).build()
        };
        add(
                "rusted_iron_" + structure,
                new AddTableLootModifier(
                        conditions,
                        1000,
                        ModRustedIronLootSubProvider.tableKey(structure)));
    }

    private void addStructure(String structure) {
        Identifier target = Identifier.withDefaultNamespace("chests/" + structure);
        LootItemCondition[] conditions = {
            LootTableIdCondition.builder(target).build()
        };
        add(
                "mite_structure_" + structure.replace('/', '_'),
                new AddTableLootModifier(
                        conditions,
                        1000,
                        ModLegacyStructureLootSubProvider.tableKey(structure)));
    }
}
