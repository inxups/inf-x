package com.pixulse.infx.datagen;

import com.pixulse.infx.block.BlueberryBushBlock;
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
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
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
                        InfXBlocks.MITE_RECIPE_BLOCKS.stream().map(block -> (Block) block.get()))
                .flatMap(stream -> stream)
                .toList();
    }
}
