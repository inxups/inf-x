package com.pixulse.infx.datagen;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXItems;
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
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

final class ModHorseArmorLootSubProvider implements LootTableSubProvider {
    ModHorseArmorLootSubProvider(HolderLookup.Provider registries) {}

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(
                tableKey("simple_dungeon"),
                table(
                        ConstantValue.exactly(8.0F),
                        147,
                        List.of(
                                new WeightedArmor(InfxMaterial.GOLD, 2),
                                new WeightedArmor(InfxMaterial.COPPER, 5),
                                new WeightedArmor(InfxMaterial.IRON, 1))));
        output.accept(
                tableKey("nether_bridge"),
                table(
                        UniformGenerator.between(2.0F, 5.0F),
                        50,
                        List.of(
                                new WeightedArmor(InfxMaterial.GOLD, 8),
                                new WeightedArmor(InfxMaterial.COPPER, 5),
                                new WeightedArmor(InfxMaterial.IRON, 3))));
        output.accept(
                tableKey("desert_pyramid"),
                table(
                        UniformGenerator.between(2.0F, 6.0F),
                        65,
                        List.of(
                                new WeightedArmor(InfxMaterial.IRON, 1),
                                new WeightedArmor(InfxMaterial.SILVER, 1),
                                new WeightedArmor(InfxMaterial.GOLD, 1))));
        output.accept(
                tableKey("jungle_temple"),
                table(
                        UniformGenerator.between(2.0F, 6.0F),
                        60,
                        List.of(
                                new WeightedArmor(InfxMaterial.IRON, 1),
                                new WeightedArmor(InfxMaterial.SILVER, 1),
                                new WeightedArmor(InfxMaterial.GOLD, 1))));
        output.accept(
                tableKey("stronghold_corridor"),
                table(
                        UniformGenerator.between(2.0F, 3.0F),
                        188,
                        List.of(
                                new WeightedArmor(InfxMaterial.COPPER, 1),
                                new WeightedArmor(InfxMaterial.IRON, 1))));
    }

    static ResourceKey<LootTable> tableKey(String structure) {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                InfiniteX.id("chests/horse_armor/" + structure));
    }

    private static LootTable.Builder table(
            NumberProvider rolls,
            int emptyWeight,
            List<WeightedArmor> armor) {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(rolls)
                .add(EmptyLootItem.emptyItem().setWeight(emptyWeight));
        armor.forEach(entry -> pool.add(LootItem.lootTableItem(horseArmor(entry.material()))
                .setWeight(entry.weight())));
        return LootTable.lootTable().withPool(pool);
    }

    private static ItemLike horseArmor(InfxMaterial material) {
        return InfXItems.catalog()
                .equipment(material, EquipmentType.HORSE_ARMOR)
                .holder();
    }

    private record WeightedArmor(InfxMaterial material, int weight) {}
}
