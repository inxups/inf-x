package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.world.R196RiverBiomes;
import com.pixulse.infx.world.Underworld;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;

/** Moves the shallow Overworld's subterranean structures into the Underworld biome. */
final class ModBiomeTagsProvider extends TagsProvider<Biome> {
    ModBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.BIOME, lookupProvider, InfiniteX.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(BiomeTags.IS_OVERWORLD)
                .add(R196RiverBiomes.DESERT_RIVER)
                .add(R196RiverBiomes.JUNGLE_RIVER)
                .add(R196RiverBiomes.SWAMP_RIVER);
        tag(BiomeTags.IS_RIVER)
                .add(R196RiverBiomes.DESERT_RIVER)
                .add(R196RiverBiomes.JUNGLE_RIVER)
                .add(R196RiverBiomes.SWAMP_RIVER);
        tag(BiomeTags.IS_JUNGLE).add(R196RiverBiomes.JUNGLE_RIVER);
        tag(BiomeTags.HAS_ANCIENT_CITY, true).add(Underworld.BIOME);
        tag(BiomeTags.HAS_BURIED_TREASURE, true).add(Underworld.BIOME);
        tag(BiomeTags.HAS_MINESHAFT, true).add(Underworld.BIOME);
        tag(BiomeTags.HAS_MINESHAFT_MESA, true).add(Underworld.BIOME);
        tag(BiomeTags.HAS_STRONGHOLD).addTag(BiomeTags.IS_OVERWORLD);
        tag(BiomeTags.HAS_TRAIL_RUINS, true).add(Underworld.BIOME);
        tag(BiomeTags.HAS_TRIAL_CHAMBERS, true).add(Underworld.BIOME);
    }
}
