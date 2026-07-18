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
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Stream.concat(
                        ModBlocks.WORKBENCHES.stream().map(workbench -> (Block) workbench.get()),
                        ModBlocks.FURNACES.stream().map(furnace -> (Block) furnace.get()))
                .toList();
    }
}
