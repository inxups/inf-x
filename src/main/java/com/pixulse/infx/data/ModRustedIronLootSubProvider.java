package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.registry.ModItems;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/** R196's non-craftable rusted-iron finds in ordinary dungeons and abandoned mineshafts. */
final class ModRustedIronLootSubProvider implements LootTableSubProvider {
    ModRustedIronLootSubProvider(HolderLookup.Provider registries) {}

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(
                tableKey("simple_dungeon"),
                equipmentPool(
                        8,
                        130,
                        List.of(
                                EquipmentType.SHEARS,
                                EquipmentType.SHOVEL,
                                EquipmentType.HOE,
                                EquipmentType.MATTOCK,
                                EquipmentType.DAGGER,
                                EquipmentType.SWORD,
                                EquipmentType.BATTLE_AXE,
                                EquipmentType.WAR_HAMMER)));
        output.accept(
                tableKey("abandoned_mineshaft"),
                mineshaftPool());
    }

    static ResourceKey<LootTable> tableKey(String structure) {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                InfiniteX.id("chests/rusted_iron/" + structure));
    }

    private static LootTable.Builder equipmentPool(int rolls, int emptyWeight, List<EquipmentType> types) {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(rolls))
                .add(EmptyLootItem.emptyItem().setWeight(emptyWeight));
        types.forEach(type -> pool.add(LootItem.lootTableItem(equipment(type)).setWeight(type == EquipmentType.SHEARS ? 3 : 2)));
        return LootTable.lootTable().withPool(pool);
    }

    private static LootTable.Builder mineshaftPool() {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(3.0F))
                .add(EmptyLootItem.emptyItem().setWeight(150))
                .add(LootItem.lootTableItem(ModItems.catalog().raw("rusted_iron_chain").holder())
                        .setWeight(5));
        List.of(
                        EquipmentType.SHOVEL,
                        EquipmentType.HATCHET,
                        EquipmentType.AXE,
                        EquipmentType.MATTOCK,
                        EquipmentType.PICKAXE,
                        EquipmentType.WAR_HAMMER)
                .forEach(type -> pool.add(LootItem.lootTableItem(equipment(type)).setWeight(2)));
        return LootTable.lootTable().withPool(pool);
    }

    private static ItemLike equipment(EquipmentType type) {
        return ModItems.catalog().equipment(MiteMaterial.RUSTED_IRON, type).holder();
    }
}
