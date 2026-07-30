package com.pixulse.infx.datagen;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.world.RiverBiomes;
import com.pixulse.infx.world.Underworld;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import org.jspecify.annotations.NonNull;

/** Moves the shallow Overworld's subterranean structures into the Underworld biome. */
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
        tag(BiomeTags.HAS_ANCIENT_CITY, true).add(Underworld.BIOME);
        tag(BiomeTags.HAS_BURIED_TREASURE, true).add(Underworld.BIOME);
        tag(BiomeTags.HAS_MINESHAFT, true).add(Underworld.BIOME);
        tag(BiomeTags.HAS_MINESHAFT_MESA, true).add(Underworld.BIOME);
        tag(BiomeTags.HAS_STRONGHOLD).addTag(BiomeTags.IS_OVERWORLD);
        tag(BiomeTags.HAS_TRAIL_RUINS, true).add(Underworld.BIOME);
        tag(BiomeTags.HAS_TRIAL_CHAMBERS, true).add(Underworld.BIOME);
    }
}
