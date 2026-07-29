package com.pixulse.infx.event;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.MiteCropBlock;
import com.pixulse.infx.data.agriculture.AgricultureData;
import com.pixulse.infx.player.ProgressionEvents;
import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;

/** MITE crop-family rules that do not belong to a specific custom crop block. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class AgricultureEvents {
    private AgricultureEvents() {}

    @SubscribeEvent
    public static void beforeCropGrowth(CropGrowEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        MiteCropBlock replacement = InfXBlocks.miteCropForVanilla(event.getState().getBlock());
        if (replacement != null) {
            level.setBlock(event.getPos(), replacement.stateFromVanilla(event.getState()), Block.UPDATE_CLIENTS);
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
            return;
        }
        if (event.getState().getBlock() instanceof MiteCropBlock) {
            return;
        }
        if (event.getState().is(Blocks.SUGAR_CANE)) {
            float temperature = level.getBiome(event.getPos()).value().getBaseTemperature();
            if (temperature <= 0.3F || level.getRandom().nextFloat() > sugarCaneGrowthChance(temperature)) {
                event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
            }
            return;
        }
        if (event.getState().is(Blocks.VINE) && vineLength(level, event.getPos()) >= maximumVineLength(event.getPos())) {
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
        }
    }

    /**
     * Converts legacy vanilla row crops before a bone-meal attempt. MITE white dye cures blight
     * only, so a healthy converted crop intentionally does not consume or use the bone meal.
     */
    @SubscribeEvent
    public static void onBonemeal(BonemealEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        MiteCropBlock replacement = InfXBlocks.miteCropForVanilla(event.getState().getBlock());
        if (replacement == null) {
            return;
        }
        level.setBlock(event.getPos(), replacement.stateFromVanilla(event.getState()), Block.UPDATE_CLIENTS);
        event.setSuccessful(false);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !event.getItemStack().is(InfXItems.catalog().raw("manure").holder())) {
            return;
        }
        BlockState clicked = level.getBlockState(event.getPos());
        if (clicked.getBlock() instanceof MushroomBlock mushroom) {
            if (mushroom.growMushroom(level, event.getPos(), clicked, level.getRandom())) {
                if (!event.getEntity().hasInfiniteMaterials()) event.getItemStack().shrink(1);
                if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                    ProgressionEvents.award(player, "supersize_me", "grew_giant_mushroom");
                }
                cancelInteraction(event);
            }
            return;
        }

        BlockPos farmlandPos = clicked.getBlock() instanceof CropBlock ? event.getPos().below() : event.getPos();
        if (!level.getBlockState(farmlandPos).is(Blocks.FARMLAND)) {
            return;
        }
        boolean fresh = AgricultureData.get(level).fertilize(farmlandPos, level.getGameTime());
        if (!event.getEntity().hasInfiniteMaterials()) event.getItemStack().shrink(1);
        if (fresh && event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            ProgressionEvents.award(player, "soil_enrichment", "fertilized_soil");
        }
        cancelInteraction(event);
    }

    private static void cancelInteraction(PlayerInteractEvent.RightClickBlock event) {
        event.setUseBlock(TriState.FALSE);
        event.setUseItem(TriState.FALSE);
        event.setCancellationResult(InteractionResult.SUCCESS_SERVER);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockState placed = event.getPlacedBlock();
        BlockPos pos = event.getPos();
        MiteCropBlock replacement = InfXBlocks.miteCropForVanilla(placed.getBlock());
        if (replacement != null) {
            level.setBlock(pos, replacement.stateFromVanilla(placed), Block.UPDATE_CLIENTS);
            return;
        }
        AgricultureData data = AgricultureData.get(level);
        if (placed.is(BlockTags.LOGS) && event.getEntity() instanceof Player) {
            data.markArtificialLog(pos, level.getGameTime());
        }
        if (placed.is(Blocks.COCOA) && !validCocoaSite(level, pos, placed, data)) {
            event.setCanceled(true);
            return;
        }
        if (placed.is(Blocks.BROWN_MUSHROOM)) {
            BlockPos soil = pos.below();
            BlockState farmland = level.getBlockState(soil);
            if (data.isFertile(soil) && isMoistFarmland(farmland) && level.getRawBrightness(pos, 0) < 8) {
                level.setBlockAndUpdate(soil, Blocks.MYCELIUM.defaultBlockState());
                if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                    ProgressionEvents.award(player, "make_mycelium", "made_mycelium");
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBroken(BreakBlockEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getState().is(BlockTags.LOGS)) {
            AgricultureData.get(level).removeArtificialLog(event.getPos());
        }
    }

    static boolean validCocoaSite(ServerLevel level, BlockPos cocoa, BlockState state, AgricultureData data) {
        if (!level.getBiome(cocoa).is(BiomeTags.IS_JUNGLE) || !state.hasProperty(CocoaBlock.FACING)) return false;
        BlockPos support = cocoa.relative(state.getValue(CocoaBlock.FACING));
        if (data.isArtificialLog(support)) return false;
        for (BlockPos pos : BlockPos.betweenClosed(support.offset(-3, 0, -3), support.offset(3, 5, 3))) {
            if (level.getBlockState(pos).is(BlockTags.LEAVES) && pos.getY() >= support.getY() + 2) return true;
        }
        return false;
    }

    public static float sugarCaneGrowthChance(float temperature) {
        if (temperature <= 0.3F) return 0.0F;
        return Math.clamp((temperature - 0.3F) / 0.9F, 0.1F, 1.0F);
    }

    public static int maximumVineLength(BlockPos root) {
        long mixed = root.getX() * 341873128712L + root.getZ() * 132897987541L;
        return 3 + Math.floorMod(Long.hashCode(mixed), 8);
    }

    private static int vineLength(ServerLevel level, BlockPos pos) {
        int length = 1;
        while (length < 32 && level.getBlockState(pos.below(length)).is(Blocks.VINE)) length++;
        return length;
    }

    private static boolean isMoistFarmland(BlockState state) {
        return state.is(Blocks.FARMLAND)
                && state.hasProperty(FarmlandBlock.MOISTURE)
                && state.getValue(FarmlandBlock.MOISTURE) > 0;
    }
}
