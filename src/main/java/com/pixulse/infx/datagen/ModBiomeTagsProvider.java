package com.pixulse.infx.datagen;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.world.RiverBiomes;
import java.util.concurrent.CompletableFuture;

import com.pixulse.infx.world.Underworld;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import org.jspecify.annotations.NonNull;

/** Moves selected shallow Overworld subterranean structures into the Underworld while retaining vanilla mineshafts. */
/** Supplies biome tags for custom rivers and the restored stronghold progression. */
final class ModBiomeTagsProvider extends KeyTagsProvider<Biome> {
    ModBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.BIOME, lookupProvider, InfiniteX.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider registries) {
        tag(BiomeTags.IS_OVERWORLD)
                .add(RiverBiomes.DESERT_RIVER)
                .add(RiverBiomes.JUNGLE_RIVER)
                .add(RiverBiomes.SWAMP_RIVER);
        tag(BiomeTags.IS_RIVER)
                .add(RiverBiomes.DESERT_RIVER)
                .add(RiverBiomes.JUNGLE_RIVER)
                .add(RiverBiomes.SWAMP_RIVER);
        tag(BiomeTags.IS_JUNGLE).add(RiverBiomes.JUNGLE_RIVER);
        tag(BiomeTags.HAS_STRONGHOLD).addTag(BiomeTags.IS_OVERWORLD);
    }
}
