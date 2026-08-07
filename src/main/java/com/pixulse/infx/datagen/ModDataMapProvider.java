package com.pixulse.infx.datagen;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

/** MITE torches burn as fuel (800 ticks), matching the MITE torch burn time. */
public final class ModDataMapProvider extends DataMapProvider {
    public ModDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        builder(NeoForgeDataMaps.FURNACE_FUELS).add(Items.TORCH.builtInRegistryHolder(), new FurnaceFuel(800), false);
        builder(NeoForgeDataMaps.FURNACE_FUELS).add(Items.SOUL_TORCH.builtInRegistryHolder(), new FurnaceFuel(800), false);
        builder(NeoForgeDataMaps.FURNACE_FUELS).add(Items.REDSTONE_TORCH.builtInRegistryHolder(), new FurnaceFuel(800), false);
        builder(NeoForgeDataMaps.FURNACE_FUELS).add(Items.COPPER_TORCH.builtInRegistryHolder(), new FurnaceFuel(800), false);
    }
}
