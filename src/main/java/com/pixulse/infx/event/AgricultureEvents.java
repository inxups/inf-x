package com.pixulse.infx.event;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.InfxCropBlock;
import com.pixulse.infx.block.InfxFertileFarmlandBlock;
import com.pixulse.infx.data.agriculture.AgricultureData;
import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.core.BlockPos;
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
    /** Manure must be used on a mycelium-backed brown mushroom to grow a huge one. */
    private static final float BROWN_MUSHROOM_GROW_CHANCE = 1.0F / 3.0F;
    /** Red mushrooms keep the vanilla plant-anywhere rule but grow even less often. */
    private static final float RED_MUSHROOM_GROW_CHANCE = 1.0F / 5.0F;
    /** MITE brown mushroom light ceiling; planting legality itself is enforced by {@code MushroomBlock.canSurvive}. */
    private static final int MUSHROOM_PLANT_LIGHT_CEILING = 13;

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
        BlockState clicked = level.getBlockState(event.getPos());
        if (clicked.getBlock() instanceof MushroomBlock) {
            // The client cancels too so the interaction is consumed and always reaches the server.
            if (!(level instanceof ServerLevel serverLevel)) {
                cancelInteraction(event);
                return;
            }
            // Manure is always consumed by a mushroom interaction. Only brown mushrooms on
            // mycelium may grow, and both colors only grow on a reduced chance roll.
            boolean canGrow = !clicked.is(Blocks.BROWN_MUSHROOM)
                    || level.getBlockState(event.getPos().below()).is(Blocks.MYCELIUM);
            if (canGrow && serverLevel.getRandom().nextFloat() < mushroomGrowChance(clicked)) {
                MushroomBlock mushroom = (MushroomBlock) clicked.getBlock();
                mushroom.growMushroom(serverLevel, event.getPos(), clicked, serverLevel.getRandom());
            }
            if (!event.getEntity().hasInfiniteMaterials()) event.getItemStack().shrink(1);
            cancelInteraction(event);
            return;
        }

        BlockPos farmlandPos = clicked.getBlock() instanceof CropBlock ? event.getPos().below() : event.getPos();
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

    /** Chance that manure actually grows a huge mushroom: brown 1/3, red 1/5. */
    static float mushroomGrowChance(BlockState state) {
        return state.is(Blocks.BROWN_MUSHROOM) ? BROWN_MUSHROOM_GROW_CHANCE : RED_MUSHROOM_GROW_CHANCE;
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
        if (placed.is(Blocks.BROWN_MUSHROOM)) {
            // Convenience conversion: a brown mushroom on moist fertilized farmland turns the
            // soil into mycelium, the only soil manure can then use to grow a huge mushroom.
            // Planting legality itself is enforced before placement by MushroomBlock.canSurvive.
            BlockPos soil = pos.below();
            BlockState farmland = level.getBlockState(soil);
            if (farmland.is(InfXBlocks.FERTILE_FARMLAND)
                    && isMoistFarmland(farmland)
                    && level.getRawBrightness(pos, 0) < MUSHROOM_PLANT_LIGHT_CEILING) {
                level.setBlockAndUpdate(soil, Blocks.MYCELIUM.defaultBlockState());
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
        return state.getBlock() instanceof FarmlandBlock
                && state.hasProperty(FarmlandBlock.MOISTURE)
                && state.getValue(FarmlandBlock.MOISTURE) > 0;
    }
}
