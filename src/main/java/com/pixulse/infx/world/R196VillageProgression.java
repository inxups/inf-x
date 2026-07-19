package com.pixulse.infx.world;

import com.pixulse.infx.harvest.HarvestTier;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.block.entity.R196SafeBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Day/tool village gate plus R164's villager-free, withered farms. */
public final class R196VillageProgression {
    public static final long VILLAGE_DAY = 60L;
    private static volatile boolean generationUnlocked;

    private R196VillageProgression() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(R196VillageProgression::onItemCrafted);
        gameBus.addListener(R196VillageProgression::onServerAboutToStart);
        gameBus.addListener(R196VillageProgression::onServerTick);
        gameBus.addListener(R196VillageProgression::onChunkLoad);
    }

    private static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        var equipment = ModItems.catalog().equipment(event.getCrafting());
        if (equipment == null
                || equipment.key().type() != R196EquipmentType.PICKAXE
                        && equipment.key().type() != R196EquipmentType.WAR_HAMMER
                || equipment.key().material().harvestTier()
                        .map(tier -> !tier.satisfies(HarvestTier.IRON))
                        .orElse(true)) {
            return;
        }
        R196WorldData.get(level).markIronToolCrafted();
        refresh(level);
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 20 == 0) refresh(event.getServer().overworld());
    }

    private static void onServerAboutToStart(ServerAboutToStartEvent event) {
        generationUnlocked = false;
    }

    private static void refresh(ServerLevel level) {
        generationUnlocked = day(level) >= VILLAGE_DAY && R196WorldData.get(level).ironToolCrafted();
    }

    public static boolean generationUnlocked() {
        return generationUnlocked;
    }

    public static boolean generationUnlocked(ServerLevel level) {
        return day(level) >= VILLAGE_DAY && R196WorldData.get(level).ironToolCrafted();
    }

    public static long day(ServerLevel level) {
        return Math.max(1L, level.getOverworldClockTime() / 24_000L + 1L);
    }

    private static void onChunkLoad(ChunkEvent.Load event) {
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
                        level.setBlockAndUpdate(cursor, ModBlocks.IRON_SAFE.get().defaultBlockState());
                        if (level.getBlockEntity(cursor) instanceof R196SafeBlockEntity safe) {
                            safe.setItem(0, new ItemStack(Items.IRON_NUGGET, 4 + level.getRandom().nextInt(9)));
                            safe.setItem(1, new ItemStack(Items.COPPER_NUGGET, 8 + level.getRandom().nextInt(13)));
                            safe.setItem(2, ModItems.SILVER_NUGGET.toStack(2 + level.getRandom().nextInt(7)));
                            safe.setItem(3, ModItems.catalog().raw("copper_coin").holder().toStack());
                        }
                        placedSafe = true;
                    }
                }
            }
        }
    }
}
