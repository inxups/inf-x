package com.pixulse.infx.world;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;

import com.pixulse.infx.data.harvest.HarvestTier;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.block.entity.SafeBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

/** Village milestone tracking plus R164's villager-free, withered farms. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class VillageProgression {
    public static final long VILLAGE_DAY = 60L;

    private VillageProgression() {}

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        var equipment = InfXItems.catalog().equipment(event.getCrafting());
        if (equipment == null
                || equipment.key().type() != EquipmentType.PICKAXE
                        && equipment.key().type() != EquipmentType.WAR_HAMMER
                || equipment.key().material().harvestTier()
                        .map(tier -> !tier.satisfies(HarvestTier.IRON))
                        .orElse(true)) {
            return;
        }
        WorldData.get(level).markIronToolCrafted();
        StructureGenerationGates.refresh(level);
    }

    public static boolean generationUnlocked() {
        return StructureGenerationGates.isUnlocked(StructureGenerationGates.VILLAGE_RULE);
    }

    public static boolean generationUnlocked(ServerLevel level) {
        return StructureGenerationGates.isUnlocked(StructureGenerationGates.VILLAGE_RULE, level);
    }

    public static long day(ServerLevel level) {
        return StructureGenerationGates.day(level);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level)) return;
        var structures = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        boolean village = event.getChunk().getAllStarts().keySet().stream()
                .map(structures::getKey)
                .anyMatch(id -> id != null && id.getPath().startsWith("village_"));
        if (!village) return;
        level.getServer().execute(() -> witherVillageFarm(level, event.getChunk().getPos()));
    }

    private static void witherVillageFarm(ServerLevel level, net.minecraft.world.level.ChunkPos chunk) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean placedSafe = false;
        int minX = chunk.getMinBlockX();
        int minZ = chunk.getMinBlockZ();
        for (int y = level.getMinY(); y < level.getMaxY(); y++) {
            for (int x = minX; x < minX + 16; x++) {
                for (int z = minZ; z < minZ + 16; z++) {
                    cursor.set(x, y, z);
                    var state = level.getBlockState(cursor);
                    if (state.is(Blocks.FARMLAND)) {
                        level.setBlock(cursor, Blocks.DIRT.defaultBlockState(), 2);
                    } else if (state.is(BlockTags.CROPS)) {
                        level.removeBlock(cursor, false);
                    } else if (!placedSafe && (state.is(Blocks.CHEST) || state.is(Blocks.BARREL))) {
                        level.setBlockAndUpdate(cursor, InfXBlocks.IRON_SAFE.get().defaultBlockState());
                        if (level.getBlockEntity(cursor) instanceof SafeBlockEntity safe) {
                            safe.setItem(0, new ItemStack(Items.IRON_NUGGET, 4 + level.getRandom().nextInt(9)));
                            safe.setItem(1, new ItemStack(Items.COPPER_NUGGET, 8 + level.getRandom().nextInt(13)));
                            safe.setItem(2, InfXItems.SILVER_NUGGET.toStack(2 + level.getRandom().nextInt(7)));
                            safe.setItem(3, InfXItems.catalog().raw("copper_coin").holder().toStack());
                        }
                        placedSafe = true;
                    }
                }
            }
        }
    }
}
