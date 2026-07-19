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
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

final class ModUnderworldLootSubProvider implements LootTableSubProvider {
    static final ResourceKey<LootTable> DUNGEON = ResourceKey.create(
            Registries.LOOT_TABLE, InfiniteX.id("chests/underworld_dungeon"));

    ModUnderworldLootSubProvider(HolderLookup.Provider registries) {}

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(8.0F))
                .add(EmptyLootItem.emptyItem().setWeight(54))
                .add(LootItem.lootTableItem(ModItems.catalog().raw("ancient_metal_nugget").holder())
                        .setWeight(10)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F))))
                .add(LootItem.lootTableItem(ModItems.ANCIENT_METAL_INGOT)
                        .setWeight(10)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F))))
                .add(LootItem.lootTableItem(ModItems.catalog().raw("ancient_metal_coin").holder()).setWeight(5))
                .add(LootItem.lootTableItem(ModItems.bucket(
                                R196Material.ANCIENT_METAL,
                                com.pixulse.infx.item.R196BucketItem.Contents.EMPTY))
                        .setWeight(2))
                .add(LootItem.lootTableItem(ModItems.RECORD_UNDERWORLD).setWeight(1))
                .add(LootItem.lootTableItem(ModItems.RECORD_DESCENT).setWeight(1))
                .add(LootItem.lootTableItem(ModItems.RECORD_WANDERER).setWeight(1))
                .add(LootItem.lootTableItem(ModItems.RECORD_LEGENDS).setWeight(1))
                .add(LootItem.lootTableItem(equipment(R196EquipmentType.HORSE_ARMOR)).setWeight(5));
        List.of(
                        R196EquipmentType.PICKAXE,
                        R196EquipmentType.SHOVEL,
                        R196EquipmentType.AXE,
                        R196EquipmentType.SWORD,
                        R196EquipmentType.WAR_HAMMER,
                        R196EquipmentType.BOW,
                        R196EquipmentType.CHAINMAIL_HELMET,
                        R196EquipmentType.CHAINMAIL_CHESTPLATE,
                        R196EquipmentType.CHAINMAIL_LEGGINGS,
                        R196EquipmentType.CHAINMAIL_BOOTS)
                .forEach(type -> pool.add(LootItem.lootTableItem(equipment(type)).setWeight(1)));
        output.accept(DUNGEON, LootTable.lootTable().withPool(pool));
    }

    private static ItemLike equipment(R196EquipmentType type) {
        return ModItems.catalog().equipment(R196Material.ANCIENT_METAL, type).holder();
    }
}
