package com.pixulse.infx.agriculture;

import com.pixulse.infx.progression.ProgressionEvents;
import com.pixulse.infx.registry.ModItems;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** R196 crop climate, clustering, fertility, disease, wilting and offline rules. */
public final class R196AgricultureEvents {
    private static final long WILT_TICKS = 7L * 24_000L;
    private static final long OFFLINE_STAGE_MILLIS = 30L * 60L * 1000L;

    private R196AgricultureEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(R196AgricultureEvents::beforeCropGrowth);
        gameBus.addListener(R196AgricultureEvents::onBonemeal);
        gameBus.addListener(R196AgricultureEvents::onRightClickBlock);
        gameBus.addListener(R196AgricultureEvents::onLevelTick);
        gameBus.addListener(R196AgricultureEvents::onBlockPlaced);
        gameBus.addListener(R196AgricultureEvents::onBlockBroken);
    }

    private static void beforeCropGrowth(CropGrowEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (event.getState().is(Blocks.SUGAR_CANE)) {
            float temperature = level.getBiome(event.getPos()).value().getBaseTemperature();
            if (temperature <= 0.3F || level.getRandom().nextFloat() > sugarCaneGrowthChance(temperature)) {
                event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
            }
            return;
        }
        if (event.getState().is(Blocks.VINE)) {
            if (vineLength(level, event.getPos()) >= maximumVineLength(event.getPos())) {
                event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
            }
            return;
        }
        if (!isCrop(event.getState())) return;
        BlockPos pos = event.getPos();
        R196AgricultureData data = R196AgricultureData.get(level);
        data.track(pos, level.getGameTime());

        spreadOrCreateDisease(level, pos, data);
        updateDrought(level, pos, data);
        if (shouldWilt(level, pos, data)) {
            level.setBlockAndUpdate(pos, Blocks.DEAD_BUSH.defaultBlockState());
            data.remove(pos);
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
            return;
        }

        if (level.getBrightness(LightLayer.SKY, pos.above()) < 8) {
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
            return;
        }
        if (data.isInfected(pos) && level.getRandom().nextFloat() < 0.75F) {
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
            return;
        }

        float multiplier = growthMultiplier(
                level.getBiome(pos).value().getBaseTemperature(),
                level.getBiome(pos).value().getModifiedClimateSettings().downfall(),
                hasSameCropNeighbor(level, pos, event.getState()),
                level.getBrightness(LightLayer.BLOCK, pos.above()) >= 9,
                data.isFertile(pos.below()),
                isMoistFarmland(level.getBlockState(pos.below())));
        multiplier *= geologyFactor(level.getBlockState(pos.below(2)));
        if (multiplier < 1.0F && level.getRandom().nextFloat() > multiplier) {
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
        } else if (multiplier > 1.0F && level.getRandom().nextFloat() < multiplier - 1.0F) {
            event.setResult(CropGrowEvent.Pre.Result.GROW);
        }
    }

    public static float growthMultiplier(
            float temperature,
            float downfall,
            boolean clustered,
            boolean artificialLight,
            boolean fertile,
            boolean moist) {
        float temperatureFactor = Math.clamp(1.0F - Math.abs(temperature - 0.8F) * 0.75F, 0.2F, 1.0F);
        float moistureFactor = Math.clamp(0.35F + downfall, 0.35F, 1.15F);
        float result = temperatureFactor * moistureFactor;
        result *= clustered ? 1.25F : 0.5F;
        if (artificialLight) result += 0.15F;
        if (fertile) result += 0.5F;
        if (moist) result += 0.25F;
        return Math.clamp(result, 0.05F, 2.0F);
    }

    private static void onBonemeal(BonemealEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isCrop(event.getState())) {
            return;
        }
        R196AgricultureData data = R196AgricultureData.get(level);
        if (!event.getStack().is(Items.BONE_MEAL)) {
            return;
        }
        if (data.cure(event.getPos())) {
            if (event.getPlayer() != null && !event.getPlayer().hasInfiniteMaterials()) {
                event.getStack().shrink(1);
            }
            level.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    event.getPos().getX() + 0.5,
                    event.getPos().getY() + 0.5,
                    event.getPos().getZ() + 0.5,
                    8,
                    0.25,
                    0.25,
                    0.25,
                    0.0);
            if (event.getPlayer() instanceof net.minecraft.server.level.ServerPlayer player) {
                ProgressionEvents.award(player, "plant_doctor", "cured_crop");
            }
            event.setSuccessful(true);
        } else {
            event.setSuccessful(false);
        }
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !event.getItemStack().is(ModItems.catalog().raw("manure").holder())) {
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
        if (!clicked.is(Blocks.FARMLAND)) return;
        R196AgricultureData data = R196AgricultureData.get(level);
        boolean fresh = data.fertilize(event.getPos(), level.getGameTime());
        BlockState farmland = level.getBlockState(event.getPos());
        if (farmland.hasProperty(FarmlandBlock.MOISTURE)) {
            level.setBlockAndUpdate(event.getPos(), farmland.setValue(FarmlandBlock.MOISTURE, 7));
        }
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

    private static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        R196AgricultureData data = R196AgricultureData.get(level);
        BlockState placed = event.getPlacedBlock();
        BlockPos pos = event.getPos();
        if (isCrop(placed)) {
            data.track(pos, level.getGameTime());
        }
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
            if (data.isFertile(soil)
                    && isMoistFarmland(farmland)
                    && level.getRawBrightness(pos, 0) < 8) {
                level.setBlockAndUpdate(soil, Blocks.MYCELIUM.defaultBlockState());
                if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                    ProgressionEvents.award(player, "make_mycelium", "made_mycelium");
                }
            }
        }
    }

    private static void onBlockBroken(BreakBlockEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        R196AgricultureData data = R196AgricultureData.get(level);
        if (event.getState().is(BlockTags.LOGS)) data.removeArtificialLog(event.getPos());
        if (isCrop(event.getState())) data.remove(event.getPos());
    }

    static boolean validCocoaSite(
            ServerLevel level,
            BlockPos cocoa,
            BlockState state,
            R196AgricultureData data) {
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
        return 3 + Math.floorMod((int) (mixed ^ mixed >>> 32), 8);
    }

    private static int vineLength(ServerLevel level, BlockPos pos) {
        int length = 1;
        while (length < 32 && level.getBlockState(pos.below(length)).is(Blocks.VINE)) length++;
        return length;
    }

    private static float geologyFactor(BlockState state) {
        if (state.is(BlockTags.SAND) || state.is(Blocks.CLAY)) return 0.7F;
        if (state.is(BlockTags.BASE_STONE_OVERWORLD)) return 0.8F;
        return 1.0F;
    }

    private static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % 200 != 0) {
            return;
        }
        R196AgricultureData data = R196AgricultureData.get(level);
        if (level.getGameTime() % 6_000 == 0) data.checkpointWallClock();
        for (String encoded : new ArrayList<>(data.tracked().keySet())) {
            BlockPos pos = BlockPos.of(Long.parseLong(encoded));
            if (!level.isLoaded(pos)) continue;
            BlockState state = level.getBlockState(pos);
            if (!isCrop(state)) {
                data.remove(pos);
                continue;
            }
            int offlineStages = data.consumeOfflineStages(
                    pos, level.getServer().isSingleplayer(), OFFLINE_STAGE_MILLIS, 7);
            if (offlineStages > 0 && !data.isInfected(pos)) {
                level.setBlockAndUpdate(pos, advanceAge(state, offlineStages));
            }
            if (shouldWilt(level, pos, data)) {
                level.setBlockAndUpdate(pos, Blocks.DEAD_BUSH.defaultBlockState());
                data.remove(pos);
            }
        }
    }

    private static void spreadOrCreateDisease(ServerLevel level, BlockPos pos, R196AgricultureData data) {
        if (!data.isInfected(pos) && level.getRandom().nextInt(4096) == 0) {
            data.infect(pos, level.getGameTime());
        }
        for (BlockPos neighbor : horizontalNeighbors(pos)) {
            if (data.isInfected(neighbor) && level.getRandom().nextInt(8) == 0) {
                data.infect(pos, level.getGameTime());
                return;
            }
        }
    }

    private static void updateDrought(ServerLevel level, BlockPos pos, R196AgricultureData data) {
        if (isMoistFarmland(level.getBlockState(pos.below())) || level.isRainingAt(pos.above())) {
            data.clearDry(pos);
        } else {
            data.setDry(pos, level.getGameTime());
        }
    }

    private static boolean shouldWilt(ServerLevel level, BlockPos pos, R196AgricultureData data) {
        long now = level.getGameTime();
        return data.isInfected(pos) && now - data.infectedSince(pos) >= WILT_TICKS
                || data.drySince(pos) > 0L && now - data.drySince(pos) >= WILT_TICKS;
    }

    private static boolean isCrop(BlockState state) {
        return state.is(BlockTags.CROPS);
    }

    private static boolean isMoistFarmland(BlockState state) {
        return state.is(Blocks.FARMLAND)
                && state.hasProperty(FarmlandBlock.MOISTURE)
                && state.getValue(FarmlandBlock.MOISTURE) > 0;
    }

    private static boolean hasSameCropNeighbor(ServerLevel level, BlockPos pos, BlockState state) {
        for (BlockPos neighbor : horizontalNeighbors(pos)) {
            if (level.getBlockState(neighbor).is(state.getBlock())) return true;
        }
        return false;
    }

    private static BlockPos[] horizontalNeighbors(BlockPos pos) {
        return new BlockPos[]{pos.north(), pos.south(), pos.east(), pos.west()};
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static BlockState advanceAge(BlockState state, int stages) {
        for (var property : state.getProperties()) {
            if (property instanceof IntegerProperty age && age.getName().equals("age")) {
                int maximum = age.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
                int current = state.getValue(age);
                return state.setValue(age, Math.min(maximum, current + stages));
            }
        }
        return state;
    }
}
