package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;

/** Removes subterranean structures from every Overworld biome until they move to the Underworld. */
final class ModBiomeTagsProvider extends TagsProvider<Biome> {
    ModBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.BIOME, lookupProvider, InfiniteX.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(BiomeTags.HAS_ANCIENT_CITY, true);
        tag(BiomeTags.HAS_BURIED_TREASURE, true);
        tag(BiomeTags.HAS_MINESHAFT, true);
        tag(BiomeTags.HAS_MINESHAFT_MESA, true);
        tag(BiomeTags.HAS_STRONGHOLD).addTag(BiomeTags.IS_OVERWORLD);
        tag(BiomeTags.HAS_TRAIL_RUINS, true);
        tag(BiomeTags.HAS_TRIAL_CHAMBERS, true);
    }
}
