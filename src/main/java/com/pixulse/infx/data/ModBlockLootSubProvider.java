package com.pixulse.infx.data;

import com.pixulse.infx.registry.ModBlocks;
import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

final class ModBlockLootSubProvider extends BlockLootSubProvider {
    ModBlockLootSubProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        ModBlocks.WORKBENCHES.forEach(workbench -> dropSelf(workbench.get()));
        ModBlocks.FURNACES.forEach(furnace -> dropSelf(furnace.get()));
        ModBlocks.ORES.forEach(ore -> dropSelf(ore.get()));
        ModBlocks.METAL_STORAGE_BLOCKS.forEach(block -> dropSelf(block.get()));
        ModBlocks.METAL_ANVILS.forEach(anvil -> dropSelf(anvil.get()));
        ModBlocks.ENCHANTING_TABLES.forEach(table -> dropSelf(table.get()));
        ModBlocks.METAL_SAFES.forEach(safe -> dropSelf(safe.get()));
        ModBlocks.R196_FLOWERS.forEach(flower -> dropSelf(flower.get()));
        dropSelf(ModBlocks.NETHER_GRAVEL.get());
        dropSelf(ModBlocks.WITHERWOOD.get());
        dropSelf(ModBlocks.MITHRIL_RUNE_STONE.get());
        dropSelf(ModBlocks.ADAMANTIUM_RUNE_STONE.get());
        add(ModBlocks.MANTLE.get(), noDrop());
        add(ModBlocks.CORE.get(), noDrop());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Stream.of(
                        ModBlocks.WORKBENCHES.stream().map(block -> (Block) block.get()),
                        ModBlocks.FURNACES.stream().map(block -> (Block) block.get()),
                        ModBlocks.ORES.stream().map(block -> (Block) block.get()),
                        ModBlocks.METAL_STORAGE_BLOCKS.stream().map(block -> (Block) block.get()),
                        ModBlocks.METAL_ANVILS.stream().map(block -> (Block) block.get()),
                        ModBlocks.WORLD_BLOCKS.stream().map(block -> (Block) block.get()),
                        ModBlocks.ENCHANTING_TABLES.stream().map(block -> (Block) block.get()),
                        ModBlocks.METAL_SAFES.stream().map(block -> (Block) block.get()),
                        ModBlocks.R196_FLOWERS.stream().map(block -> (Block) block.get()),
                        ModBlocks.FULLTEXT_BLOCKS.stream().map(block -> (Block) block.get()))
                .flatMap(stream -> stream)
                .toList();
    }
}
