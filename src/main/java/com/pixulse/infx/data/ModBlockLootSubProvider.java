package com.pixulse.infx.data;

import com.pixulse.infx.registry.ModBlocks;
import java.util.List;
import java.util.Set;

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
        dropSelf(ModBlocks.FLINT_WORKBENCH.get());
        dropSelf(ModBlocks.COPPER_WORKBENCH.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return List.of(ModBlocks.FLINT_WORKBENCH.get(), ModBlocks.COPPER_WORKBENCH.get());
    }
}
