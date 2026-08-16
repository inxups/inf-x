package com.pixulse.infx.event;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.InfxCropBlock;
import com.pixulse.infx.block.InfxFertileFarmlandBlock;
import com.pixulse.infx.data.agriculture.AgricultureData;
import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.world.BlightTracker;
import com.pixulse.infx.world.InfXMushroomGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;

/** InfX crop-family rules that do not belong to a specific custom crop block. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class AgricultureEvents {
    /** MITE manure grows one mushroom tier half of the time; a failed roll still consumes. */
    private static final float MANURE_GROW_CHANCE = 0.5F;

    private AgricultureEvents() {}

    @SubscribeEvent
    public static void beforeCropGrowth(CropGrowEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        InfxCropBlock replacement = InfXBlocks.infxCropForVanilla(event.getState().getBlock());
        if (replacement != null) {
            level.setBlock(event.getPos(), replacement.stateFromVanilla(event.getState()), Block.UPDATE_CLIENTS);
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
            return;
        }
        if (event.getState().getBlock() instanceof InfxCropBlock) {
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
     * Converts legacy vanilla row crops before a bone-meal attempt. InfX white dye cures blight
     * only, so a healthy converted crop intentionally does not consume or use the bone meal.
     * Saplings never grow from bone meal either; grass blocks, water plants and decorative
     * plants keep their vanilla uses.
     */
    @SubscribeEvent
    public static void onBonemeal(BonemealEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (event.getState().getBlock() instanceof SaplingBlock) {
            // InfX bone meal ignores saplings entirely: no growth, no consumption.
            event.setSuccessful(false);
            event.setCanceled(true);
            return;
        }
        if (event.getState().is(Blocks.BROWN_MUSHROOM) || event.getState().is(Blocks.RED_MUSHROOM)) {
            // MITE: only manure grows mushrooms; bone meal is a no-op on them.
            event.setSuccessful(false);
            event.setCanceled(true);
            return;
        }
        InfxCropBlock replacement = InfXBlocks.infxCropForVanilla(event.getState().getBlock());
        if (replacement == null) {
            return;
        }
        level.setBlock(event.getPos(), replacement.stateFromVanilla(event.getState()), Block.UPDATE_CLIENTS);
        event.setSuccessful(false);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getItemStack().is(InfXItems.catalog().raw("manure").holder())) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState clicked = level.getBlockState(pos);
        if (clicked.getBlock() instanceof MushroomBlock) {
            BlockState below = level.getBlockState(pos.below());
            if (below.getBlock() instanceof FarmlandBlock) {
                // MITE redirect trap: manure on a mushroom growing on farmland only fertilizes the
                // soil below (already-fertilized soil is a no-op that does not consume).
                if (!(level instanceof ServerLevel serverLevel)) {
                    cancelInteraction(event);
                    return;
                }
                fertilizeFarmlandByHand(serverLevel, pos.below(), event);
                cancelInteraction(event);
                return;
            }
            // MITE: an illegal mushroom (brown not on mycelium, red not on grass) is not consumed.
            if (!InfXMushroomGrowth.isGrowableAt(level, pos, clicked)) {
                return;
            }
            manureGrowMushroom(event, level, pos, clicked);
            return;
        }
        // MITE: manure on a mycelium/grass block forwards to the mushroom growing on top.
        if (event.getFace() == Direction.UP
                && (clicked.is(Blocks.MYCELIUM) || clicked.is(Blocks.GRASS_BLOCK))
                && level.getBlockState(pos.above()).getBlock() instanceof MushroomBlock) {
            BlockState mushroom = level.getBlockState(pos.above());
            if (!InfXMushroomGrowth.isGrowableAt(level, pos.above(), mushroom)) {
                return;
            }
            manureGrowMushroom(event, level, pos.above(), mushroom);
            return;
        }

        BlockPos farmlandPos = clicked.getBlock() instanceof CropBlock ? pos.below() : pos;
        if (!(level.getBlockState(farmlandPos).getBlock() instanceof FarmlandBlock)) {
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            cancelInteraction(event);
            return;
        }
        fertilizeFarmlandByHand(serverLevel, farmlandPos, event);
        cancelInteraction(event);
    }

    /** MITE manure: 50% chance to grow one tier; the manure is consumed even on a failed roll. */
    private static void manureGrowMushroom(PlayerInteractEvent.RightClickBlock event, Level level, BlockPos pos, BlockState mushroom) {
        // The client cancels too so the interaction is consumed and always reaches the server.
        if (!(level instanceof ServerLevel serverLevel)) {
            cancelInteraction(event);
            return;
        }
        if (serverLevel.getRandom().nextFloat() < MANURE_GROW_CHANCE) {
            InfXMushroomGrowth.tryGrowGiantMushroom(serverLevel, pos, mushroom, serverLevel.getRandom());
        }
        if (!event.getEntity().hasInfiniteMaterials()) event.getItemStack().shrink(1);
        cancelInteraction(event);
    }

    private static void fertilizeFarmlandByHand(ServerLevel level, BlockPos farmlandPos, PlayerInteractEvent.RightClickBlock event) {
        if (level.getBlockState(farmlandPos).getBlock() instanceof InfxFertileFarmlandBlock) return;

        if (!event.getEntity().hasInfiniteMaterials())
            event.getItemStack().shrink(1);
        fertilizeFarmland(level, farmlandPos);
    }


    public static void fertilizeFarmland(ServerLevel level, BlockPos farmlandPos) {
        BlockState state = level.getBlockState(farmlandPos);
        if (state.is(Blocks.FARMLAND)) {
            level.setBlockAndUpdate(
                    farmlandPos,
                    InfXBlocks.FERTILE_FARMLAND.get().defaultBlockState()
                            .setValue(FarmlandBlock.MOISTURE, state.getValue(FarmlandBlock.MOISTURE)));
        }
        level.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                farmlandPos.getX() + 0.5,
                farmlandPos.getY() + 1.0,
                farmlandPos.getZ() + 0.5,
                10,
                0.4,
                0.2,
                0.4,
                0.05);
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
        InfxCropBlock replacement = InfXBlocks.infxCropForVanilla(placed.getBlock());
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
    }

    @SubscribeEvent
    public static void onBlockBroken(BreakBlockEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (event.getState().is(BlockTags.LOGS)) {
            AgricultureData.get(level).removeArtificialLog(event.getPos());
        }
        if (event.getState().getBlock() instanceof CropBlock) {
            BlightTracker.get(level).cure(event.getPos());
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
}
