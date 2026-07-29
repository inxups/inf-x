package com.pixulse.infx.datagen;

import com.pixulse.infx.block.BlueberryBushBlock;
import com.pixulse.infx.block.MiteCropBlock;
import com.pixulse.infx.block.MiteCropType;
import com.pixulse.infx.block.RuneStoneBlock;
import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.registry.InfXItems;
import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.NonNull;

final class ModBlockLootSubProvider extends BlockLootSubProvider {
    ModBlockLootSubProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        InfXBlocks.WORKBENCHES.forEach(workbench -> dropSelf(workbench.get()));
        InfXBlocks.FURNACES.forEach(furnace -> dropSelf(furnace.get()));
        InfXBlocks.ORES.forEach(ore -> dropSelf(ore.get()));
        InfXBlocks.METAL_STORAGE_BLOCKS.forEach(block -> dropSelf(block.get()));
        InfXBlocks.METAL_ANVILS.forEach(anvil -> dropSelf(anvil.get()));
        InfXBlocks.ENCHANTING_TABLES.forEach(table -> dropSelf(table.get()));
        InfXBlocks.METAL_SAFES.forEach(safe -> dropSelf(safe.get()));
        add(InfXBlocks.SNOW_SLAB.get(), createSlabItemTable(InfXBlocks.SNOW_SLAB.get()));
        dropSelf(InfXBlocks.NETHER_GRAVEL.get());
        dropSelf(InfXBlocks.WITHERWOOD.get());
        add(InfXBlocks.BLUEBERRY_BUSH.get(), blueberryBushDrops(InfXBlocks.BLUEBERRY_BUSH.get()));
        InfXBlocks.MITE_CROPS.forEach(crop -> add(crop.get(), miteCropDrops(crop.get())));
        dropRuneStone(InfXBlocks.MITHRIL_RUNE_STONE.get());
        dropRuneStone(InfXBlocks.ADAMANTIUM_RUNE_STONE.get());
        add(InfXBlocks.MANTLE.get(), noDrop());
        add(InfXBlocks.CORE.get(), noDrop());
    }

    private void dropRuneStone(RuneStoneBlock block) {
        add(block, createSingleItemTable(block).apply(CopyBlockState.copyState(block).copy(RuneStoneBlock.RUNE)));
    }

    private LootTable.Builder blueberryBushDrops(BlueberryBushBlock bush) {
        var matureBush = LootItemBlockStatePropertyCondition.hasBlockStateProperties(bush)
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(SweetBerryBushBlock.AGE, SweetBerryBushBlock.MAX_AGE));
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .when(matureBush)
                        .add(LootItem.lootTableItem(InfXItems.BLUEBERRIES)))
                .withPool(LootPool.lootPool()
                        .when(hasSilkTouch())
                        .add(LootItem.lootTableItem(bush)));
    }

    private LootTable.Builder miteCropDrops(MiteCropBlock crop) {
        var planted = cropState(crop, 0);
        var mature = cropState(crop, crop.getMaxAge());
        LootTable.Builder table = LootTable.lootTable()
                .withPool(LootPool.lootPool().add(LootItem.lootTableItem(crop.type().seed()).when(planted)))
                .withPool(matureCropProductPool(crop, mature));
        if (crop.type() == MiteCropType.POTATOES) {
            table.withPool(potatoPoisonPool(crop, planted, mature));
        }
        if (crop.type() == MiteCropType.BEETROOTS) {
            table.withPool(LootPool.lootPool().add(LootItem.lootTableItem(crop.type().seed())
                    .when(mature)
                    .when(LootItemRandomChanceCondition.randomChance(crop.type().bonusYieldChance()))));
        }
        return table;
    }

    private static LootPool.Builder matureCropProductPool(
            MiteCropBlock crop, LootItemBlockStatePropertyCondition.Builder mature) {
        MiteCropType type = crop.type();
        if (type.matureYield() == 1) {
            return LootPool.lootPool().add(LootItem.lootTableItem(type.product()).when(mature));
        }
        return LootPool.lootPool().add(AlternativesEntry.alternatives(
                LootItem.lootTableItem(type.product())
                        .when(mature)
                        .when(LootItemRandomChanceCondition.randomChance(type.bonusYieldChance()))
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(type.matureYield() + 1))),
                LootItem.lootTableItem(type.product())
                        .when(mature)
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(type.matureYield())))));
    }

    private static LootPool.Builder potatoPoisonPool(
            MiteCropBlock crop,
            LootItemBlockStatePropertyCondition.Builder planted,
            LootItemBlockStatePropertyCondition.Builder mature) {
        return LootPool.lootPool().add(LootItem.lootTableItem(Items.POISONOUS_POTATO)
                .when(livingCropState(crop))
                .when(InvertedLootItemCondition.invert(AnyOfCondition.anyOf(planted, mature))));
    }

    private static LootItemBlockStatePropertyCondition.Builder cropState(MiteCropBlock crop, int age) {
        return LootItemBlockStatePropertyCondition.hasBlockStateProperties(crop)
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(CropBlock.AGE, age)
                        .hasProperty(MiteCropBlock.BLIGHTED, false)
                        .hasProperty(MiteCropBlock.DEAD, false));
    }

    private static LootItemBlockStatePropertyCondition.Builder livingCropState(MiteCropBlock crop) {
        return LootItemBlockStatePropertyCondition.hasBlockStateProperties(crop)
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(MiteCropBlock.DEAD, false));
    }

    @Override
    protected @NonNull Iterable<Block> getKnownBlocks() {
        return Stream.of(
                        InfXBlocks.WORKBENCHES.stream().map(block -> (Block) block.get()),
                        InfXBlocks.FURNACES.stream().map(block -> (Block) block.get()),
                        InfXBlocks.ORES.stream().map(DeferredHolder::get),
                        InfXBlocks.METAL_STORAGE_BLOCKS.stream().map(DeferredHolder::get),
                        InfXBlocks.METAL_ANVILS.stream().map(block -> (Block) block.get()),
                        InfXBlocks.WORLD_BLOCKS.stream().map(block -> (Block) block.get()),
                        InfXBlocks.ENCHANTING_TABLES.stream().map(block -> (Block) block.get()),
                        InfXBlocks.METAL_SAFES.stream().map(block -> (Block) block.get()),
                        InfXBlocks.FULLTEXT_BLOCKS.stream().map(block -> (Block) block.get()),
                        InfXBlocks.MITE_RECIPE_BLOCKS.stream().map(block -> (Block) block.get()),
                        InfXBlocks.MITE_CROPS.stream().map(block -> (Block) block.get()))
                .flatMap(stream -> stream)
                .toList();
    }
}
