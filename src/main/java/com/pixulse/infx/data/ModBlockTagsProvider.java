package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.harvest.HarvestTier;
import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.tag.ModTags;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

final class ModBlockTagsProvider extends BlockTagsProvider {
    ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, InfiniteX.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.HARDENED_CLAY_FURNACE.getKey())
                .add(ModBlocks.OBSIDIAN_FURNACE.getKey())
                .add(ModBlocks.NETHERRACK_FURNACE.getKey())
                .add(ModBlocks.SILVER_ORE.getKey())
                .add(ModBlocks.MITHRIL_ORE.getKey())
                .add(ModBlocks.ADAMANTIUM_ORE.getKey());
        tag(ModTags.Blocks.RESTRICTED_HARVEST)
                .addTag(BlockTags.LOGS)
                .addTag(BlockTags.MINEABLE_WITH_PICKAXE);
        tag(ModTags.Blocks.requiredTier(HarvestTier.FLINT)).addTag(BlockTags.LOGS);
        tag(ModTags.Blocks.requiredTier(HarvestTier.COPPER)).addTag(BlockTags.MINEABLE_WITH_PICKAXE);
        tag(ModTags.Blocks.requiredTier(HarvestTier.IRON))
                .add(ModBlocks.OBSIDIAN_FURNACE.getKey())
                .add(ModBlocks.MITHRIL_ORE.getKey());
        tag(ModTags.Blocks.requiredTier(HarvestTier.MITHRIL)).add(ModBlocks.ADAMANTIUM_ORE.getKey());
    }
}
