package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.harvest.HarvestTier;
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
        tag(ModTags.Blocks.RESTRICTED_HARVEST)
                .addTag(BlockTags.LOGS)
                .addTag(BlockTags.MINEABLE_WITH_PICKAXE);
        tag(ModTags.Blocks.requiredTier(HarvestTier.FLINT)).addTag(BlockTags.LOGS);
        tag(ModTags.Blocks.requiredTier(HarvestTier.COPPER)).addTag(BlockTags.MINEABLE_WITH_PICKAXE);
    }
}
