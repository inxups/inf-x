package com.pixulse.infx.data;

import com.pixulse.infx.block.RuneStoneBlock;
import com.pixulse.infx.registry.InfinityXBlocks;
import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;

final class ModBlockLootSubProvider extends BlockLootSubProvider {
    ModBlockLootSubProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        InfinityXBlocks.WORKBENCHES.forEach(workbench -> dropSelf(workbench.get()));
        InfinityXBlocks.FURNACES.forEach(furnace -> dropSelf(furnace.get()));
        InfinityXBlocks.ORES.forEach(ore -> dropSelf(ore.get()));
        InfinityXBlocks.METAL_STORAGE_BLOCKS.forEach(block -> dropSelf(block.get()));
        InfinityXBlocks.METAL_ANVILS.forEach(anvil -> dropSelf(anvil.get()));
        InfinityXBlocks.ENCHANTING_TABLES.forEach(table -> dropSelf(table.get()));
        InfinityXBlocks.METAL_SAFES.forEach(safe -> dropSelf(safe.get()));
        add(InfinityXBlocks.SNOW_SLAB.get(), createSlabItemTable(InfinityXBlocks.SNOW_SLAB.get()));
        dropSelf(InfinityXBlocks.NETHER_GRAVEL.get());
        dropSelf(InfinityXBlocks.WITHERWOOD.get());
        dropRuneStone(InfinityXBlocks.MITHRIL_RUNE_STONE.get());
        dropRuneStone(InfinityXBlocks.ADAMANTIUM_RUNE_STONE.get());
        add(InfinityXBlocks.MANTLE.get(), noDrop());
        add(InfinityXBlocks.CORE.get(), noDrop());
    }

    private void dropRuneStone(RuneStoneBlock block) {
        add(block, createSingleItemTable(block).apply(CopyBlockState.copyState(block).copy(RuneStoneBlock.RUNE)));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Stream.of(
                        InfinityXBlocks.WORKBENCHES.stream().map(block -> (Block) block.get()),
                        InfinityXBlocks.FURNACES.stream().map(block -> (Block) block.get()),
                        InfinityXBlocks.ORES.stream().map(block -> (Block) block.get()),
                        InfinityXBlocks.METAL_STORAGE_BLOCKS.stream().map(block -> (Block) block.get()),
                        InfinityXBlocks.METAL_ANVILS.stream().map(block -> (Block) block.get()),
                        InfinityXBlocks.WORLD_BLOCKS.stream().map(block -> (Block) block.get()),
                        InfinityXBlocks.ENCHANTING_TABLES.stream().map(block -> (Block) block.get()),
                        InfinityXBlocks.METAL_SAFES.stream().map(block -> (Block) block.get()),
                        InfinityXBlocks.FULLTEXT_BLOCKS.stream().map(block -> (Block) block.get()),
                        InfinityXBlocks.MITE_RECIPE_BLOCKS.stream().map(block -> (Block) block.get()))
                .flatMap(stream -> stream)
                .toList();
    }
}
