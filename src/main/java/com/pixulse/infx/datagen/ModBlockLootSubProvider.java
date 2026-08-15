package com.pixulse.infx.datagen;

import com.pixulse.infx.block.BlueberryBushBlock;
import com.pixulse.infx.block.InfxCropBlock;
import com.pixulse.infx.block.InfxCropType;
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
import net.minecraft.world.item.Item;
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
        InfXBlocks.ORES.forEach(ore -> add(ore.get(), createOreDrop(ore.get(), oreRawItem(ore))));
        InfXBlocks.METAL_STORAGE_BLOCKS.forEach(block -> dropSelf(block.get()));
        InfXBlocks.METAL_ANVILS.forEach(anvil -> dropSelf(anvil.get()));
        InfXBlocks.ENCHANTING_TABLES.forEach(table -> dropSelf(table.get()));
        InfXBlocks.METAL_SAFES.forEach(safe -> dropSelf(safe.get()));
        add(InfXBlocks.SNOW_SLAB.get(), createSlabItemTable(InfXBlocks.SNOW_SLAB.get()));
        dropSelf(InfXBlocks.GRAVEL.get());
        dropSelf(InfXBlocks.NETHER_GRAVEL.get());
        dropSelf(InfXBlocks.WITHERWOOD.get());
        add(InfXBlocks.BLUEBERRY_BUSH.get(), blueberryBushDrops(InfXBlocks.BLUEBERRY_BUSH.get()));
        InfXBlocks.INFX_CROPS.forEach(crop -> add(crop.get(), infxCropDrops(crop.get())));
        dropOther(InfXBlocks.FERTILE_FARMLAND.get(), Items.DIRT);
        dropRuneStone(InfXBlocks.MITHRIL_RUNE_STONE.get());
        dropRuneStone(InfXBlocks.ADAMANTIUM_RUNE_STONE.get());
        add(InfXBlocks.MANTLE.get(), noDrop());
        add(InfXBlocks.CORE.get(), noDrop());
    }

    private void dropRuneStone(RuneStoneBlock block) {
        add(block, createSingleItemTable(block).apply(CopyBlockState.copyState(block).copy(RuneStoneBlock.RUNE)));
    }

    /** Ore blocks drop one raw chunk with fortune bonus; silk touch still yields the block. */
    private static Item oreRawItem(DeferredHolder<Block, ? extends Block> ore) {
        if (ore == InfXBlocks.SILVER_ORE || ore == InfXBlocks.DEEPSLATE_SILVER_ORE) {
            return InfXItems.RAW_SILVER.get();
        }
        if (ore == InfXBlocks.MITHRIL_ORE || ore == InfXBlocks.DEEPSLATE_MITHRIL_ORE) {
            return InfXItems.RAW_MITHRIL.get();
        }
        return InfXItems.RAW_ADAMANTIUM.get();
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

    private LootTable.Builder infxCropDrops(InfxCropBlock crop) {
        var planted = cropState(crop, 0);
        var mature = cropState(crop, crop.getMaxAge());
        LootTable.Builder table = LootTable.lootTable()
                .withPool(LootPool.lootPool().add(LootItem.lootTableItem(crop.type().seed()).when(planted)))
                .withPool(matureCropProductPool(crop, mature));
        if (crop.type() == InfxCropType.POTATOES) {
            table.withPool(potatoPoisonPool(crop, planted, mature));
        }
        if (crop.type() == InfxCropType.BEETROOTS) {
            table.withPool(LootPool.lootPool().add(LootItem.lootTableItem(crop.type().seed())
                    .when(mature)
                    .when(LootItemRandomChanceCondition.randomChance(crop.type().bonusYieldChance()))));
        }
        return table;
    }

    private static LootPool.Builder matureCropProductPool(
            InfxCropBlock crop, LootItemBlockStatePropertyCondition.Builder mature) {
        InfxCropType type = crop.type();
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
            InfxCropBlock crop,
            LootItemBlockStatePropertyCondition.Builder planted,
            LootItemBlockStatePropertyCondition.Builder mature) {
        return LootPool.lootPool().add(LootItem.lootTableItem(Items.POISONOUS_POTATO)
                .when(livingCropState(crop))
                .when(InvertedLootItemCondition.invert(AnyOfCondition.anyOf(planted, mature))));
    }

    private static LootItemBlockStatePropertyCondition.Builder cropState(InfxCropBlock crop, int age) {
        return LootItemBlockStatePropertyCondition.hasBlockStateProperties(crop)
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(CropBlock.AGE, age)
                        .hasProperty(InfxCropBlock.BLIGHTED, false)
                        .hasProperty(InfxCropBlock.DEAD, false));
    }

    private static LootItemBlockStatePropertyCondition.Builder livingCropState(InfxCropBlock crop) {
        return LootItemBlockStatePropertyCondition.hasBlockStateProperties(crop)
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(InfxCropBlock.DEAD, false));
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
                        InfXBlocks.INFX_RECIPE_BLOCKS.stream().map(block -> (Block) block.get()),
                        InfXBlocks.INFX_CROPS.stream().map(block -> (Block) block.get()),
                        Stream.of((Block) InfXBlocks.FERTILE_FARMLAND.get()))
                .flatMap(stream -> stream)
                .toList();
    }
}
