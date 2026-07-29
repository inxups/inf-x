package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.data.agriculture.AgricultureData;
import com.pixulse.infx.world.MoonPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * MITE row crop implementation. Disease and death are synchronized block-state properties so
 * their supplied textures represent the actual server state rather than hidden saved data.
 */
public final class MiteCropBlock extends CropBlock {
    public static final BooleanProperty BLIGHTED = BooleanProperty.create("blighted");
    public static final BooleanProperty DEAD = BooleanProperty.create("dead");

    private static final float GLOBAL_GROWTH_RATE = 0.25F;
    private static final float DROUGHT_DEATH_CHANCE = 0.05F;
    private static final int FERTILITY_CONSUMPTION_CHANCE = 256;

    private final MiteCropType type;
    private final MapCodec<MiteCropBlock> codec;

    public MiteCropBlock(MiteCropType type, BlockBehaviour.Properties properties) {
        super(properties);
        this.type = type;
        this.codec = simpleCodec(codecProperties -> new MiteCropBlock(type, codecProperties));
        registerDefaultState(defaultBlockState().setValue(BLIGHTED, false).setValue(DEAD, false));
    }

    @Override
    public MapCodec<MiteCropBlock> codec() {
        return codec;
    }

    @Override
    public int getMaxAge() {
        return type.maxAge();
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return type.seed();
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        // MITE keeps mature crops ticking so they can blight; dead crops never tick again.
        return !state.getValue(DEAD);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(DEAD)) {
            return;
        }

        float growthRate = growthRate(level, pos);
        if (growthRate == 0.0F && !hasNearbyWater(level, pos.below())) {
            if (random.nextFloat() < DROUGHT_DEATH_CHANCE) {
                if (isMaxAge(state)) {
                    Block.dropResources(state, level, pos);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                } else {
                    makeDead(level, pos, state, getAge(state));
                }
            }
            return;
        }

        if (state.getValue(BLIGHTED)) {
            tickBlightedCrop(level, pos, state, random);
            return;
        }

        int light = level.getRawBrightness(pos.above(), 0);
        if (isBloodMoonNight(level) && !level.getServer().isDedicatedServer() && level.canSeeSky(pos.above())
                && random.nextFloat() < 0.25F) {
            setBlighted(level, pos, state);
            return;
        }

        float blightChance = type.blightChance()
                * blightChanceModifier(
                        level.getBiome(pos).value().getBaseTemperature(),
                        level.getBiome(pos).value().getModifiedClimateSettings().downfall() >= 0.85F)
                * (1.0F - light / 16.0F);
        if (random.nextFloat() < blightChance) {
            setBlighted(level, pos, state);
            return;
        }

        if (light != 15 || growthRate == 0.0F || isMaxAge(state)) {
            return;
        }

        if (random.nextInt(growthTickInterval(growthRate)) != 0) {
            return;
        }

        int nextAge = Math.min(getMaxAge(), getAge(state) + 1);
        BlockState grown = state.setValue(getAgeProperty(), nextAge);
        level.setBlock(pos, grown, Block.UPDATE_CLIENTS);
        if (random.nextInt(FERTILITY_CONSUMPTION_CHANCE) == 0) {
            AgricultureData.get(level).consumeFertility(pos.below());
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        // White dye in MITE cures blight; it does not accelerate healthy row crops.
        return !state.getValue(DEAD) && state.getValue(BLIGHTED);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return isValidBonemealTarget(level, pos, state);
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        cureBlight(level, pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BLIGHTED, DEAD);
    }

    public MiteCropType type() {
        return type;
    }

    public BlockState stateForAge(int age) {
        return defaultBlockState().setValue(getAgeProperty(), Math.clamp(age, 0, getMaxAge()));
    }

    public BlockState stateFromVanilla(BlockState vanillaState) {
        if (vanillaState.getBlock() instanceof CropBlock crop) {
            return stateForAge(type.ageFromVanilla(crop.getAge(vanillaState)));
        }
        return defaultBlockState();
    }

    public int textureStage(BlockState state) {
        return type.textureStage(getAge(state));
    }

    public int deadTextureStage(BlockState state) {
        return type.deadTextureStage(getAge(state));
    }

    public boolean isDead(BlockState state) {
        return state.getValue(DEAD);
    }

    public boolean isBlighted(BlockState state) {
        return state.getValue(BLIGHTED);
    }

    public boolean canYield(BlockState state) {
        return !isDead(state) && !isBlighted(state) && isMaxAge(state);
    }

    public boolean cureBlight(ServerLevel level, BlockPos pos, BlockState state) {
        if (!isValidBonemealTarget(level, pos, state)) {
            return false;
        }
        level.setBlock(pos, state.setValue(BLIGHTED, false), Block.UPDATE_CLIENTS);
        return true;
    }

    public float growthRate(ServerLevel level, BlockPos pos) {
        BlockState farmland = level.getBlockState(pos.below());
        if (!isMoistFarmland(farmland)) {
            return 0.0F;
        }

        float rate = 1.0F;
        BlockPos farmlandPos = pos.below();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                float contribution = farmlandContribution(level.getBlockState(farmlandPos.offset(x, 0, z)));
                if (x != 0 || z != 0) {
                    contribution /= 4.0F;
                }
                rate += contribution;
            }
        }

        boolean northSouth = sameLivingCrop(level, pos.north()) || sameLivingCrop(level, pos.south());
        boolean eastWest = sameLivingCrop(level, pos.east()) || sameLivingCrop(level, pos.west());
        boolean diagonal = sameLivingCrop(level, pos.north().west())
                || sameLivingCrop(level, pos.north().east())
                || sameLivingCrop(level, pos.south().west())
                || sameLivingCrop(level, pos.south().east());
        if (diagonal || northSouth && eastWest) {
            rate /= 2.0F;
        }

        if (AgricultureData.get(level).isFertile(farmlandPos)) {
            rate *= 1.5F;
        }

        float temperature = level.getBiome(pos).value().getBaseTemperature();
        rate *= temperatureGrowthRateModifier(temperature);
        rate *= GLOBAL_GROWTH_RATE;
        rate *= proximityGrowthRateModifier(level, pos);
        return rate;
    }

    public static float temperatureGrowthRateModifier(float temperature) {
        float delta;
        if (temperature < 0.8F) {
            delta = 0.8F - temperature;
        } else if (temperature > 1.2F) {
            delta = temperature - 1.2F;
        } else {
            return 1.0F;
        }
        return Math.max(1.0F - delta, 0.0F);
    }

    public static float blightChanceModifier(float temperature, boolean highHumidity) {
        float delta;
        if (temperature < 1.0F) {
            delta = 1.0F - temperature;
        } else if (temperature > 1.2F) {
            delta = temperature - 1.2F;
        } else {
            delta = 0.0F;
        }
        return Math.max((1.0F - delta) * (highHumidity ? 1.5F : 1.0F), 0.0F);
    }

    public static int growthTickInterval(float growthRate) {
        if (growthRate <= 0.0F) {
            return Integer.MAX_VALUE;
        }
        return Math.max(1, (int) (25.0F / growthRate) + 1);
    }

    private void tickBlightedCrop(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (random.nextInt(64) == 0) {
            int deadAge = isMaxAge(state) ? getMaxAge() - 1 : getAge(state);
            makeDead(level, pos, state, deadAge);
            return;
        }
        if (random.nextBoolean()) {
            return;
        }

        BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
        if (target.equals(pos) || !level.isLoaded(target)) {
            return;
        }
        BlockState targetState = level.getBlockState(target);
        if (targetState.getBlock() instanceof MiteCropBlock targetCrop
                && !targetCrop.isDead(targetState)
                && !targetCrop.isBlighted(targetState)) {
            targetCrop.setBlighted(level, target, targetState);
        }
    }

    private void setBlighted(ServerLevel level, BlockPos pos, BlockState state) {
        if (!state.getValue(DEAD) && !state.getValue(BLIGHTED)) {
            level.setBlock(pos, state.setValue(BLIGHTED, true), Block.UPDATE_CLIENTS);
        }
    }

    private void makeDead(ServerLevel level, BlockPos pos, BlockState state, int age) {
        BlockState dead = state.setValue(getAgeProperty(), Math.clamp(age, 0, getMaxAge()))
                .setValue(BLIGHTED, false)
                .setValue(DEAD, true);
        level.setBlock(pos, dead, Block.UPDATE_CLIENTS);
    }

    private float proximityGrowthRateModifier(BlockGetter level, BlockPos pos) {
        boolean north = sameLivingCrop(level, pos.north());
        boolean east = sameLivingCrop(level, pos.east());
        boolean south = sameLivingCrop(level, pos.south());
        boolean west = sameLivingCrop(level, pos.west());
        int neighbors = (north ? 1 : 0) + (east ? 1 : 0) + (south ? 1 : 0) + (west ? 1 : 0);
        if (neighbors > 1) {
            return 1.0F;
        }
        if (neighbors == 0) {
            return 0.5F;
        }
        if (north) {
            return sameLivingCrop(level, pos.north().north())
                            || sameLivingCrop(level, pos.north().east())
                            || sameLivingCrop(level, pos.north().west())
                    ? 1.0F
                    : 0.75F;
        }
        if (east) {
            return sameLivingCrop(level, pos.east().north())
                            || sameLivingCrop(level, pos.east().east())
                            || sameLivingCrop(level, pos.east().south())
                    ? 1.0F
                    : 0.75F;
        }
        if (south) {
            return sameLivingCrop(level, pos.south().east())
                            || sameLivingCrop(level, pos.south().south())
                            || sameLivingCrop(level, pos.south().west())
                    ? 1.0F
                    : 0.75F;
        }
        return sameLivingCrop(level, pos.west().north())
                        || sameLivingCrop(level, pos.west().south())
                        || sameLivingCrop(level, pos.west().west())
                ? 1.0F
                : 0.75F;
    }

    private boolean sameLivingCrop(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(this) && !state.getValue(DEAD);
    }

    private static float farmlandContribution(BlockState state) {
        if (!state.is(Blocks.FARMLAND)) {
            return 0.0F;
        }
        return isMoistFarmland(state) ? 3.0F : 1.0F;
    }

    private static boolean isMoistFarmland(BlockState state) {
        return state.is(Blocks.FARMLAND) && state.getValue(FarmlandBlock.MOISTURE) > 0;
    }

    private static boolean hasNearbyWater(ServerLevel level, BlockPos farmland) {
        for (BlockPos nearby : BlockPos.betweenClosed(
                farmland.offset(-4, 0, -4), farmland.offset(4, 1, 4))) {
            if (level.getFluidState(nearby).is(FluidTags.WATER) || level.getBlockState(nearby).is(Blocks.ICE)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBloodMoonNight(ServerLevel level) {
        return MoonPhase.at(level) == MoonPhase.BLOOD
                && Math.floorMod(level.getOverworldClockTime(), 24_000L) >= 12_000L;
    }
}
