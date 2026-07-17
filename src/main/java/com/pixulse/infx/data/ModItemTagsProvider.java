package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.harvest.HarvestTier;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.tag.ModTags;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

final class ModItemTagsProvider extends ItemTagsProvider {
    ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, InfiniteX.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(ModTags.Items.BINDINGS)
                .add(BuiltInRegistries.ITEM.getResourceKey(Items.STRING).orElseThrow())
                .add(ModItems.SINEW.getKey());
        tag(ModTags.Items.toolTier(HarvestTier.FLINT)).add(ModItems.FLINT_HATCHET.getKey());
        tag(ModTags.Items.toolTier(HarvestTier.COPPER)).add(ModItems.COPPER_PICKAXE.getKey());
    }
}
