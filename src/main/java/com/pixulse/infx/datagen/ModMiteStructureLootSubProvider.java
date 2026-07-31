package com.pixulse.infx.datagen;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.InfxBucketItem;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXItems;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;

/** MITE structure rewards, mapped to the 26.1.2 InfiniteX item catalog. */
final class ModMiteStructureLootSubProvider implements LootTableSubProvider {
    static final List<String> TARGETS = List.of(
            "simple_dungeon",
            "abandoned_mineshaft",
            "nether_bridge",
            "desert_pyramid",
            "jungle_temple",
            "stronghold_corridor",
            "stronghold_crossing",
            "stronghold_library",
            "ancient_city",
            "ancient_city_ice_box",
            "bastion_bridge",
            "bastion_hoglin_stable",
            "bastion_other",
            "bastion_treasure",
            "buried_treasure",
            "end_city_treasure",
            "igloo_chest",
            "pillager_outpost",
            "ruined_portal",
            "shipwreck_map",
            "shipwreck_supply",
            "shipwreck_treasure",
            "underwater_ruin_big",
            "underwater_ruin_small",
            "woodland_mansion",
            "village/village_armorer",
            "village/village_butcher",
            "village/village_cartographer",
            "village/village_desert_house",
            "village/village_fisher",
            "village/village_fletcher",
            "village/village_mason",
            "village/village_plains_house",
            "village/village_savanna_house",
            "village/village_shepherd",
            "village/village_snowy_house",
            "village/village_taiga_house",
            "village/village_tannery",
            "village/village_temple",
            "village/village_toolsmith",
            "village/village_weaponsmith",
            "trial_chambers/corridor",
            "trial_chambers/entrance",
            "trial_chambers/intersection",
            "trial_chambers/intersection_barrel",
            "trial_chambers/reward",
            "trial_chambers/reward_ominous",
            "trial_chambers/supply");

    ModMiteStructureLootSubProvider(HolderLookup.Provider registries) {}

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        for (String target : TARGETS) {
            output.accept(tableKey(target), tableFor(target));
        }
    }

    static ResourceKey<LootTable> tableKey(String structure) {
        return ResourceKey.create(Registries.LOOT_TABLE, InfiniteX.id("chests/mite/" + structure));
    }

    private static LootTable.Builder tableFor(String structure) {
        if (structure.equals("simple_dungeon")) return dungeonPool();
        if (structure.equals("abandoned_mineshaft")) return mineshaftPool();
        if (structure.equals("nether_bridge")) return netherBridgePool();
        if (structure.equals("desert_pyramid")) return desertTemplePool();
        if (structure.equals("jungle_temple")) return jungleTemplePool();
        if (structure.equals("stronghold_corridor")) return strongholdCorridorPool();
        if (structure.equals("stronghold_crossing")) return strongholdCrossingPool();
        if (structure.equals("stronghold_library")) return strongholdLibraryPool();
        if (structure.startsWith("village/")) return villagePool(structure.substring("village/".length()));
        if (structure.startsWith("bastion_") || structure.equals("ruined_portal")) return netherBridgePool();
        if (structure.equals("buried_treasure") || structure.equals("end_city_treasure")) {
            return strongholdCorridorPool();
        }
        if (structure.equals("ancient_city")) return mineshaftPool();
        if (structure.equals("woodland_mansion")
                || structure.equals("trial_chambers/reward_ominous")) return strongholdCrossingPool();
        if (structure.equals("trial_chambers/reward")) return trialRewardPool();
        if (structure.startsWith("trial_chambers/")) return dungeonPool();
        if (structure.startsWith("shipwreck_") || structure.equals("pillager_outpost")) {
            return mineshaftPool();
        }
        return dungeonPool();
    }

    private static LootTable.Builder dungeonPool() {
        return table(
                ConstantValue.exactly(8.0F),
                List.of(
                        item(Items.BREAD, 1, 1, 10),
                        item(Items.CARROT, 1, 1, 5),
                        item(Items.POTATO, 1, 1, 5),
                        item(InfXItems.ONION, 1, 1, 5),
                        item(Items.WHEAT, 1, 4, 10),
                        item(Items.GOLDEN_APPLE, 1, 1, 1),
                        item(raw("copper_coin"), 1, 4, 10),
                        item(raw("silver_coin"), 1, 2, 2),
                        item(raw("gold_coin"), 1, 1, 1),
                        item(Items.SADDLE, 1, 1, 10),
                        item(Items.GUNPOWDER, 1, 4, 10),
                        item(Items.STRING, 1, 4, 10),
                        item(Items.BOWL, 1, 1, 3),
                        item(bucket(InfxMaterial.COPPER), 1, 1, 5),
                        item(bucket(InfxMaterial.IRON), 1, 1, 3),
                        item(Items.MUSIC_DISC_13, 1, 1, 5),
                        item(Items.MUSIC_DISC_CAT, 1, 1, 5),
                        item(Items.NAME_TAG, 1, 1, 10),
                        item(fishingRod(InfxMaterial.FLINT), 1, 1, 5),
                        item(fishingRod(InfxMaterial.COPPER), 1, 1, 3),
                        item(fishingRod(InfxMaterial.ANCIENT_METAL), 1, 1, 2),
                        item(equipment(InfxMaterial.WOOD, EquipmentType.SHOVEL), 1, 1, 2),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.SHOVEL), 1, 1, 1),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.HOE), 1, 1, 1),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.MATTOCK), 1, 1, 1),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.DAGGER), 1, 1, 1),
                        item(equipment(InfxMaterial.SILVER, EquipmentType.DAGGER), 1, 1, 1),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.SWORD), 1, 1, 1),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.BATTLE_AXE), 1, 1, 1),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.WAR_HAMMER), 1, 1, 1)));
    }

    private static LootTable.Builder mineshaftPool() {
        return table(
                UniformGenerator.between(3.0F, 6.0F),
                List.of(
                        item(Items.APPLE, 1, 3, 3),
                        item(Items.BREAD, 1, 3, 15),
                        item(Items.CARROT, 1, 2, 2),
                        item(Items.POTATO, 1, 2, 2),
                        item(InfXItems.ONION, 1, 2, 2),
                        item(InfXItems.CHEESE, 1, 3, 3),
                        item(Items.BROWN_MUSHROOM, 1, 3, 5),
                        item(Items.RED_MUSHROOM, 1, 3, 5),
                        item(Items.BOWL, 1, 1, 3),
                        item(raw("copper_coin"), 1, 5, 5),
                        item(raw("silver_nugget"), 1, 5, 5),
                        item(raw("gold_coin"), 1, 5, 5),
                        item(raw("copper_coin"), 1, 5, 5),
                        item(raw("silver_ingot"), 1, 3, 3),
                        item(raw("gold_coin"), 1, 3, 2),
                        item(raw("silver_coin"), 1, 5, 5),
                        item(Items.LAPIS_LAZULI, 2, 5, 5),
                        item(Items.REDSTONE, 2, 5, 5),
                        item(raw("emerald_shard"), 1, 6, 5),
                        item(raw("diamond_shard"), 1, 4, 3),
                        item(raw("emerald_shard"), 1, 5, 3),
                        item(raw("diamond_shard"), 1, 5, 1),
                        item(Items.COAL, 2, 5, 10),
                        item(Items.LEATHER_BOOTS, 1, 1, 1),
                        item(Items.SADDLE, 1, 1, 3),
                        item(Items.TORCH, 1, 6, 10),
                        item(Items.FLINT_AND_STEEL, 1, 1, 5),
                        item(Items.TNT, 1, 3, 5),
                        item(bucket(InfxMaterial.COPPER), 1, 1, 2),
                        item(bucket(InfxMaterial.IRON), 1, 1, 1),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.SHOVEL), 1, 1, 2),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.SHOVEL), 1, 1, 1),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.HATCHET), 1, 1, 2),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.HATCHET), 1, 1, 1),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.AXE), 1, 1, 2),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.AXE), 1, 1, 1),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.MATTOCK), 1, 1, 2),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.MATTOCK), 1, 1, 1),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.PICKAXE), 1, 1, 2),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.PICKAXE), 1, 1, 1),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.WAR_HAMMER), 1, 1, 2),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.WAR_HAMMER), 1, 1, 1),
                        item(Items.RAIL, 2, 5, 1)));
    }

    private static LootTable.Builder netherBridgePool() {
        return table(
                UniformGenerator.between(2.0F, 5.0F),
                List.of(
                        item(raw("diamond_shard"), 1, 3, 5),
                        item(raw("gold_coin"), 1, 3, 15),
                        item(equipment(InfxMaterial.GOLD, EquipmentType.SWORD), 1, 1, 5),
                        item(equipment(InfxMaterial.GOLD, EquipmentType.CHESTPLATE), 1, 1, 5),
                        item(Items.FLINT_AND_STEEL, 1, 1, 5),
                        item(Items.NETHER_WART, 3, 7, 5),
                        item(Items.SADDLE, 1, 1, 10)));
    }

    private static LootTable.Builder desertTemplePool() {
        return table(
                UniformGenerator.between(2.0F, 6.0F),
                List.of(
                        item(raw("diamond_shard"), 1, 3, 3),
                        item(raw("gold_coin"), 2, 7, 15),
                        item(raw("emerald_shard"), 1, 3, 2),
                        item(Items.BONE, 4, 6, 20),
                        item(Items.ROTTEN_FLESH, 3, 7, 16),
                        item(Items.SADDLE, 1, 1, 3),
                        item(Items.BOWL, 1, 1, 3),
                        item(Items.PAPER, 1, 1, 1),
                        item(Items.FLOWER_POT, 1, 1, 1)));
    }

    private static LootTable.Builder jungleTemplePool() {
        return table(
                UniformGenerator.between(2.0F, 6.0F),
                List.of(
                        item(raw("diamond_shard"), 1, 3, 3),
                        item(raw("gold_coin"), 2, 7, 15),
                        item(raw("emerald_shard"), 1, 3, 2),
                        item(Items.BONE, 4, 6, 20),
                        item(Items.ROTTEN_FLESH, 3, 7, 16),
                        item(Items.SADDLE, 1, 1, 3)));
    }

    private static LootTable.Builder strongholdCorridorPool() {
        return table(
                UniformGenerator.between(2.0F, 3.0F),
                List.of(
                        item(Items.BREAD, 1, 3, 15),
                        item(Items.APPLE, 1, 3, 15),
                        item(Items.CARROT, 1, 1, 5),
                        item(Items.POTATO, 1, 1, 5),
                        item(InfXItems.ONION, 1, 1, 5),
                        item(Items.GOLDEN_APPLE, 1, 1, 1),
                        item(Items.ENDER_PEARL, 1, 1, 10),
                        item(raw("emerald_shard"), 1, 5, 3),
                        item(raw("diamond_shard"), 1, 5, 2),
                        item(raw("copper_coin"), 1, 5, 5),
                        item(raw("silver_ingot"), 1, 3, 3),
                        item(raw("gold_coin"), 1, 3, 2),
                        item(raw("silver_coin"), 1, 5, 5),
                        item(Items.REDSTONE, 3, 5, 5),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.PICKAXE), 1, 1, 5),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.PICKAXE), 1, 1, 5),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.DAGGER), 1, 1, 3),
                        item(equipment(InfxMaterial.SILVER, EquipmentType.DAGGER), 1, 1, 3),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.DAGGER), 1, 1, 3),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.SWORD), 1, 1, 5),
                        item(equipment(InfxMaterial.SILVER, EquipmentType.SWORD), 1, 1, 5),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.SWORD), 1, 1, 5),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.BATTLE_AXE), 1, 1, 2),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.BATTLE_AXE), 1, 1, 1),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.WAR_HAMMER), 1, 1, 2),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.WAR_HAMMER), 1, 1, 1),
                        chain(InfxMaterial.COPPER, EquipmentType.CHAINMAIL_CHESTPLATE, 5),
                        chain(InfxMaterial.COPPER, EquipmentType.CHAINMAIL_HELMET, 5),
                        chain(InfxMaterial.COPPER, EquipmentType.CHAINMAIL_LEGGINGS, 5),
                        chain(InfxMaterial.COPPER, EquipmentType.CHAINMAIL_BOOTS, 5),
                        chain(InfxMaterial.ANCIENT_METAL, EquipmentType.CHAINMAIL_CHESTPLATE, 3),
                        chain(InfxMaterial.ANCIENT_METAL, EquipmentType.CHAINMAIL_HELMET, 3),
                        chain(InfxMaterial.ANCIENT_METAL, EquipmentType.CHAINMAIL_LEGGINGS, 3),
                        chain(InfxMaterial.ANCIENT_METAL, EquipmentType.CHAINMAIL_BOOTS, 3),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.CHESTPLATE), 1, 1, 5),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.HELMET), 1, 1, 5),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.LEGGINGS), 1, 1, 5),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.BOOTS), 1, 1, 5),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.CHESTPLATE), 1, 1, 3),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.HELMET), 1, 1, 3),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.LEGGINGS), 1, 1, 3),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.BOOTS), 1, 1, 3),
                        item(Items.SADDLE, 1, 1, 1),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.SHEARS), 1, 1, 1)));
    }

    private static LootTable.Builder strongholdCrossingPool() {
        return table(
                UniformGenerator.between(1.0F, 4.0F),
                List.of(
                        item(raw("silver_coin"), 1, 5, 10),
                        item(raw("gold_coin"), 1, 3, 5),
                        item(Items.REDSTONE, 4, 9, 5),
                        item(Items.COAL, 3, 8, 10),
                        item(Items.BREAD, 1, 3, 15),
                        item(Items.APPLE, 1, 3, 15),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.PICKAXE), 1, 1, 1)));
    }

    private static LootTable.Builder strongholdLibraryPool() {
        return table(
                UniformGenerator.between(1.0F, 4.0F),
                List.of(
                        item(Items.BOOK, 1, 3, 20),
                        item(Items.PAPER, 2, 7, 20),
                        item(Items.MAP, 1, 1, 1),
                        item(Items.COMPASS, 1, 1, 1)));
    }

    private static LootTable.Builder trialRewardPool() {
        return table(
                UniformGenerator.between(1.0F, 4.0F),
                List.of(
                        item(Items.BOOK, 1, 3, 20),
                        item(Items.PAPER, 2, 7, 20),
                        item(Items.MAP, 1, 1, 1),
                        item(Items.COMPASS, 1, 1, 1),
                        item(InfXItems.ONION, 1, 2, 5),
                        item(raw("emerald_shard"), 1, 5, 4),
                        item(raw("diamond_shard"), 1, 3, 2),
                        item(raw("silver_coin"), 1, 5, 3),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.SWORD), 1, 1, 2),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.CHAINMAIL_CHESTPLATE), 1, 1, 1)));
    }

    private static LootTable.Builder villagePool(String profession) {
        return switch (profession) {
            case "village_armorer", "village_toolsmith", "village_weaponsmith" -> villageBlacksmithPool();
            case "village_cartographer" -> strongholdLibraryPool();
            case "village_fletcher" -> villageFletcherPool();
            case "village_mason" -> mineshaftPool();
            case "village_butcher", "village_fisher", "village_shepherd", "village_tannery" ->
                    villageFoodPool();
            default -> dungeonPool();
        };
    }

    private static LootTable.Builder villageFoodPool() {
        return table(
                UniformGenerator.between(3.0F, 8.0F),
                List.of(
                        item(Items.BREAD, 1, 3, 15),
                        item(Items.APPLE, 1, 3, 15),
                        item(Items.CARROT, 1, 2, 8),
                        item(Items.POTATO, 1, 2, 8),
                        item(InfXItems.ONION, 1, 2, 8),
                        item(InfXItems.CHEESE, 1, 3, 8),
                        item(Items.WHEAT, 1, 4, 10),
                        item(Items.STRING, 1, 4, 8),
                        item(Items.BOWL, 1, 1, 3),
                        item(Items.LEATHER, 1, 3, 5),
                        item(raw("copper_coin"), 1, 5, 5),
                        item(raw("silver_coin"), 1, 3, 3)));
    }

    private static LootTable.Builder villageFletcherPool() {
        return table(
                UniformGenerator.between(3.0F, 8.0F),
                List.of(
                        item(Items.STICK, 1, 8, 15),
                        item(Items.FEATHER, 1, 5, 10),
                        item(Items.STRING, 1, 5, 10),
                        item(Items.FLINT, 1, 5, 5),
                        item(Items.ARROW, 1, 8, 8),
                        item(equipment(InfxMaterial.WOOD, EquipmentType.BOW), 1, 1, 3),
                        item(raw("copper_coin"), 1, 5, 5),
                        item(raw("silver_coin"), 1, 3, 2)));
    }

    private static LootTable.Builder villageBlacksmithPool() {
        List<LootEntry> entries = new ArrayList<>(List.of(
                        item(raw("emerald_shard"), 1, 3, 2),
                        item(raw("diamond_shard"), 1, 3, 1),
                        item(raw("copper_coin"), 1, 5, 5),
                        item(raw("silver_nugget"), 1, 5, 5),
                        item(raw("gold_coin"), 1, 8, 5),
                        item(raw("silver_coin"), 1, 5, 5),
                        item(raw("copper_coin"), 1, 3, 3),
                        item(raw("silver_coin"), 1, 3, 3),
                        item(raw("copper_coin"), 1, 20, 15),
                        item(raw("silver_coin"), 1, 10, 10),
                        item(raw("gold_coin"), 1, 3, 5),
                        item(Items.BREAD, 1, 3, 15),
                        item(Items.APPLE, 1, 3, 15),
                        item(Items.LEATHER_CHESTPLATE, 1, 1, 5),
                        item(Items.LEATHER_HELMET, 1, 1, 5),
                        item(Items.LEATHER_LEGGINGS, 1, 1, 5),
                        item(Items.LEATHER_BOOTS, 1, 1, 5),
                        item(Items.SADDLE, 1, 1, 3)));
        entries.addAll(armorSet(InfxMaterial.COPPER, 5));
        entries.addAll(chainSet(InfxMaterial.COPPER, 5));
        entries.addAll(armorSet(InfxMaterial.ANCIENT_METAL, 5));
        entries.addAll(chainSet(InfxMaterial.ANCIENT_METAL, 5));
        entries.addAll(List.of(
                        item(equipment(InfxMaterial.COPPER, EquipmentType.SHOVEL), 1, 1, 5),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.SHOVEL), 1, 1, 5),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.HOE), 1, 1, 5),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.HOE), 1, 1, 5),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.MATTOCK), 1, 1, 5),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.MATTOCK), 1, 1, 5),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.HATCHET), 1, 1, 5),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.HATCHET), 1, 1, 5),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.AXE), 1, 1, 5),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.AXE), 1, 1, 5),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.SHEARS), 1, 1, 5),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.SHEARS), 1, 1, 5),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.SCYTHE), 1, 1, 5),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.PICKAXE), 1, 1, 5),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.DAGGER), 1, 1, 5),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.DAGGER), 1, 1, 5),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.SWORD), 1, 1, 5),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.SWORD), 1, 1, 5),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.WAR_HAMMER), 1, 1, 5),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.WAR_HAMMER), 1, 1, 5),
                        item(equipment(InfxMaterial.COPPER, EquipmentType.BATTLE_AXE), 1, 1, 5),
                        item(equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.BATTLE_AXE), 1, 1, 5)));
        return table(UniformGenerator.between(3.0F, 8.0F), entries);
    }

    private static LootTable.Builder table(NumberProvider rolls, List<LootEntry> entries) {
        LootPool.Builder pool = LootPool.lootPool().setRolls(rolls);
        entries.forEach(entry -> pool.add(entry.builder()));
        return LootTable.lootTable().withPool(pool);
    }

    private static LootEntry item(ItemLike item, int min, int max, int weight) {
        return new LootEntry(item, min, max, weight);
    }

    private static LootEntry chain(InfxMaterial material, EquipmentType type, int weight) {
        return item(equipment(material, type), 1, 1, weight);
    }

    private static List<LootEntry> armorSet(InfxMaterial material, int weight) {
        return List.of(
                item(equipment(material, EquipmentType.HELMET), 1, 1, weight),
                item(equipment(material, EquipmentType.CHESTPLATE), 1, 1, weight),
                item(equipment(material, EquipmentType.LEGGINGS), 1, 1, weight),
                item(equipment(material, EquipmentType.BOOTS), 1, 1, weight));
    }

    private static List<LootEntry> chainSet(InfxMaterial material, int weight) {
        return List.of(
                item(equipment(material, EquipmentType.CHAINMAIL_HELMET), 1, 1, weight),
                item(equipment(material, EquipmentType.CHAINMAIL_CHESTPLATE), 1, 1, weight),
                item(equipment(material, EquipmentType.CHAINMAIL_LEGGINGS), 1, 1, weight),
                item(equipment(material, EquipmentType.CHAINMAIL_BOOTS), 1, 1, weight));
    }

    private static ItemLike equipment(InfxMaterial material, EquipmentType type) {
        return InfXItems.catalog().equipment(material, type).holder();
    }

    private static ItemLike fishingRod(InfxMaterial material) {
        return equipment(material, EquipmentType.FISHING_ROD);
    }

    private static ItemLike bucket(InfxMaterial material) {
        return InfXItems.bucket(material, InfxBucketItem.Contents.EMPTY);
    }

    private static ItemLike raw(String path) {
        return InfXItems.catalog().raw(path).holder();
    }

    private record LootEntry(ItemLike item, int min, int max, int weight) {
        LootPoolSingletonContainer.Builder<?> builder() {
            var builder = LootItem.lootTableItem(item).setWeight(weight);
            if (min != 1 || max != 1) {
                builder.apply(SetItemCountFunction.setCount(UniformGenerator.between((float) min, (float) max)));
            }
            return builder;
        }
    }
}
