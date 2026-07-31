package com.pixulse.infx.datagen;

import com.pixulse.infx.item.InfxBucketItem;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.world.Underworld;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
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
    ModUnderworldLootSubProvider(HolderLookup.Provider registries) {}

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(8.0F))
                .add(EmptyLootItem.emptyItem().setWeight(54))
                .add(LootItem.lootTableItem(InfXItems.catalog().raw("ancient_metal_nugget").holder())
                        .setWeight(10)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F))))
                .add(LootItem.lootTableItem(InfXItems.ANCIENT_METAL_INGOT)
                        .setWeight(10)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F))))
                .add(LootItem.lootTableItem(InfXItems.catalog().raw("ancient_metal_coin").holder()).setWeight(5))
                .add(LootItem.lootTableItem(InfXItems.bucket(
                                InfxMaterial.ANCIENT_METAL,
                                InfxBucketItem.Contents.EMPTY))
                        .setWeight(2))
                .add(LootItem.lootTableItem(InfXItems.RECORD_UNDERWORLD).setWeight(1))
                .add(LootItem.lootTableItem(InfXItems.RECORD_DESCENT).setWeight(1))
                .add(LootItem.lootTableItem(InfXItems.RECORD_WANDERER).setWeight(1))
                .add(LootItem.lootTableItem(InfXItems.RECORD_LEGENDS).setWeight(1))
                .add(LootItem.lootTableItem(equipment(EquipmentType.HORSE_ARMOR)).setWeight(5));
        List.of(
                        EquipmentType.PICKAXE,
                        EquipmentType.SHOVEL,
                        EquipmentType.AXE,
                        EquipmentType.SWORD,
                        EquipmentType.WAR_HAMMER,
                        EquipmentType.BOW,
                        EquipmentType.CHAINMAIL_HELMET,
                        EquipmentType.CHAINMAIL_CHESTPLATE,
                        EquipmentType.CHAINMAIL_LEGGINGS,
                        EquipmentType.CHAINMAIL_BOOTS)
                .forEach(type -> pool.add(LootItem.lootTableItem(equipment(type)).setWeight(1)));
        output.accept(Underworld.DUNGEON_LOOT, LootTable.lootTable().withPool(pool));
    }

    private static ItemLike equipment(EquipmentType type) {
        return InfXItems.catalog().equipment(InfxMaterial.ANCIENT_METAL, type).holder();
    }
}
