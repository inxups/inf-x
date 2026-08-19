package com.pixulse.infx.datagen;

import java.util.concurrent.CompletableFuture;

import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.MonsterRoomMob;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

/** InfX torches burn as fuel (800 ticks), matching the InfX torch burn time. */
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
        builder(NeoForgeDataMaps.FURNACE_FUELS).add(Items.LEAF_LITTER.builtInRegistryHolder(), new FurnaceFuel(20), false);
        builder(NeoForgeDataMaps.FURNACE_FUELS).add(InfXItems.catalog().raw("manure").holder(), new FurnaceFuel(60), false);

        // MITE dungeon depth roster. Vanilla keeps skeleton/spider/zombie weights; the InfX depth
        // mobs join the shared weighted table so any dungeon can host them, with the depth mixin
        // on top applying the per-y tiers.
        addMonsterRoomMob(InfXEntityTypes.INFX_ZOMBIE.get(), 200);
        addMonsterRoomMob(InfXEntityTypes.INFX_SKELETON.get(), 100);
        addMonsterRoomMob(InfXEntityTypes.INFX_SPIDER.get(), 100);
        addMonsterRoomMob(InfXEntityTypes.GHOUL.get(), 80);
        addMonsterRoomMob(InfXEntityTypes.WIGHT.get(), 60);
        addMonsterRoomMob(InfXEntityTypes.DEMON_SPIDER.get(), 50);
        addMonsterRoomMob(InfXEntityTypes.HELLHOUND.get(), 40);
    }

    private void addMonsterRoomMob(EntityType<?> type, int weight) {
        builder(NeoForgeDataMaps.MONSTER_ROOM_MOBS).add(type.builtInRegistryHolder(), new MonsterRoomMob(weight), false);
    }
}
