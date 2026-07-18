package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;
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
                                R196EquipmentType.SHEARS,
                                R196EquipmentType.SHOVEL,
                                R196EquipmentType.HOE,
                                R196EquipmentType.MATTOCK,
                                R196EquipmentType.DAGGER,
                                R196EquipmentType.SWORD,
                                R196EquipmentType.BATTLE_AXE,
                                R196EquipmentType.WAR_HAMMER)));
        output.accept(
                tableKey("abandoned_mineshaft"),
                mineshaftPool());
    }

    static ResourceKey<LootTable> tableKey(String structure) {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                InfiniteX.id("chests/rusted_iron/" + structure));
    }

    private static LootTable.Builder equipmentPool(int rolls, int emptyWeight, List<R196EquipmentType> types) {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(rolls))
                .add(EmptyLootItem.emptyItem().setWeight(emptyWeight));
        types.forEach(type -> pool.add(LootItem.lootTableItem(equipment(type)).setWeight(type == R196EquipmentType.SHEARS ? 3 : 2)));
        return LootTable.lootTable().withPool(pool);
    }

    private static LootTable.Builder mineshaftPool() {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(3.0F))
                .add(EmptyLootItem.emptyItem().setWeight(150))
                .add(LootItem.lootTableItem(ModItems.catalog().raw("rusted_iron_chain").holder())
                        .setWeight(5));
        List.of(
                        R196EquipmentType.SHOVEL,
                        R196EquipmentType.HATCHET,
                        R196EquipmentType.AXE,
                        R196EquipmentType.MATTOCK,
                        R196EquipmentType.PICKAXE,
                        R196EquipmentType.WAR_HAMMER)
                .forEach(type -> pool.add(LootItem.lootTableItem(equipment(type)).setWeight(2)));
        return LootTable.lootTable().withPool(pool);
    }

    private static ItemLike equipment(R196EquipmentType type) {
        return ModItems.catalog().equipment(R196Material.RUSTED_IRON, type).holder();
    }
}
