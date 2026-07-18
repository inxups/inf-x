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
        dropSelf(ModBlocks.MITHRIL_RUNE_STONE.get());
        dropSelf(ModBlocks.ADAMANTIUM_RUNE_STONE.get());
        add(ModBlocks.MANTLE.get(), noDrop());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Stream.concat(
                        Stream.concat(
                                ModBlocks.WORKBENCHES.stream().map(workbench -> (Block) workbench.get()),
                                ModBlocks.FURNACES.stream().map(furnace -> (Block) furnace.get())),
                        Stream.concat(
                                ModBlocks.ORES.stream().map(ore -> (Block) ore.get()),
                                Stream.concat(
                                        ModBlocks.METAL_STORAGE_BLOCKS.stream().map(block -> (Block) block.get()),
                                        Stream.concat(
                                                ModBlocks.METAL_ANVILS.stream().map(anvil -> (Block) anvil.get()),
                                                ModBlocks.WORLD_BLOCKS.stream().map(block -> (Block) block.get())))))
                .toList();
    }
}
