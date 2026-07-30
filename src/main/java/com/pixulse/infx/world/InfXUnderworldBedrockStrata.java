package com.pixulse.infx.world;

import com.pixulse.infx.InfiniteX;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

/** MITE's lower internal bedrock strata adapted to 26.1.2's seeded normal noises. */
public final class InfXUnderworldBedrockStrata {
    private static final int FOUR_OCTAVE_FIRST_OCTAVE = -3;
    private static final List<Double> FOUR_OCTAVE_AMPLITUDES = List.of(1.0, 1.0, 1.0, 1.0);
    private static final double MITE_OCTAVE_GAIN = 8.0;
    private static final double STRATA_ONE_SCALE = 1.0 / 32.0;
    private static final double STRATA_OTHER_SCALE = 1.0 / 16.0;
    private static final double BUMP_ONE_A_SCALE = 1.0 / 8.0;
    private static final double BUMP_ONE_B_SCALE = 1.0 / 4.0;
    private static final double BUMP_ONE_C_SCALE = 1.0 / 2.0;
    private static final double BUMP_OTHER_SCALE = 1.0;
    private static final Identifier LOWER_FACE_DITHER = InfiniteX.id("underworld_bedrock_strata_dither");

    private static final ResourceKey<NormalNoise.NoiseParameters> STRATA_ONE_A = noiseKey("underworld_bedrock_strata_1a");
    private static final ResourceKey<NormalNoise.NoiseParameters> STRATA_ONE_B = noiseKey("underworld_bedrock_strata_1b");
    private static final ResourceKey<NormalNoise.NoiseParameters> STRATA_TWO = noiseKey("underworld_bedrock_strata_2");
    private static final ResourceKey<NormalNoise.NoiseParameters> STRATA_THREE = noiseKey("underworld_bedrock_strata_3");
    private static final ResourceKey<NormalNoise.NoiseParameters> STRATA_FOUR = noiseKey("underworld_bedrock_strata_4");
    private static final ResourceKey<NormalNoise.NoiseParameters> STRATA_ONE_A_BUMP = noiseKey("underworld_bedrock_strata_1a_bump");
    private static final ResourceKey<NormalNoise.NoiseParameters> STRATA_ONE_B_BUMP = noiseKey("underworld_bedrock_strata_1b_bump");
    private static final ResourceKey<NormalNoise.NoiseParameters> STRATA_ONE_C_BUMP = noiseKey("underworld_bedrock_strata_1c_bump");
    private static final ResourceKey<NormalNoise.NoiseParameters> STRATA_TWO_BUMP = noiseKey("underworld_bedrock_strata_2_bump");
    private static final ResourceKey<NormalNoise.NoiseParameters> STRATA_THREE_BUMP = noiseKey("underworld_bedrock_strata_3_bump");
    private static final ResourceKey<NormalNoise.NoiseParameters> STRATA_FOUR_BUMP = noiseKey("underworld_bedrock_strata_4_bump");

    private InfXUnderworldBedrockStrata() {}

    public static void bootstrapNoiseParameters(BootstrapContext<NormalNoise.NoiseParameters> context) {
        for (ResourceKey<NormalNoise.NoiseParameters> key : List.of(
                STRATA_ONE_A,
                STRATA_ONE_B,
                STRATA_TWO,
                STRATA_THREE,
                STRATA_FOUR,
                STRATA_ONE_A_BUMP,
                STRATA_ONE_B_BUMP,
                STRATA_ONE_C_BUMP,
                STRATA_TWO_BUMP,
                STRATA_THREE_BUMP,
                STRATA_FOUR_BUMP)) {
            context.register(key, new NormalNoise.NoiseParameters(FOUR_OCTAVE_FIRST_OCTAVE, FOUR_OCTAVE_AMPLITUDES));
        }
    }

    public static void apply(ChunkAccess chunk, RandomState randomState) {
        NoiseSet noises = NoiseSet.from(randomState);
        PositionalRandomFactory ditherFactory = randomState.getOrCreateRandomFactory(LOWER_FACE_DITHER);
        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minY = Math.max(chunk.getMinY(), Underworld.INTERNAL_BEDROCK_MIN_Y);
        int maxYExclusive = Math.min(chunk.getMaxY(), Underworld.INTERNAL_BEDROCK_MAX_Y_EXCLUSIVE);

        for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
            for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                int worldX = x;
                int worldZ = z;
                NoiseValues values = noises.sample(worldX, worldZ);
                LowerFaceDither dither = y -> ditherFactory.at(worldX, y, worldZ).nextBoolean();
                for (int y = minY; y < maxYExclusive; y++) {
                    pos.set(worldX, y, worldZ);
                    BlockState state = chunk.getBlockState(pos);
                    BlockState replacement = replacementFor(state, y, values, dither);
                    if (replacement != state) {
                        chunk.setBlockState(pos, replacement, 0);
                    }
                }
            }
        }
    }

    static BlockState replacementFor(BlockState state, int y, NoiseValues values, LowerFaceDither dither) {
        return state.is(Blocks.DEEPSLATE) && selectedStratum(y, values, dither) != 0
                ? Blocks.BEDROCK.defaultBlockState()
                : state;
    }

    static int selectedStratum(int y, NoiseValues values, LowerFaceDither dither) {
        if (y < Underworld.INTERNAL_BEDROCK_MIN_Y || y >= Underworld.INTERNAL_BEDROCK_MAX_Y_EXCLUSIVE) {
            return 0;
        }

        double firstDensity = firstDensity(values);
        if (firstDensity > 0.0 && y - Underworld.BEDROCK_STRATUM_ONE_CENTER_Y <= firstDensity * 7.0) {
            return 1;
        }

        double secondDensity = values.stratumTwo() - firstDensity * 1.5;
        if (isCenteredStratum(y, Underworld.BEDROCK_STRATUM_TWO_CENTER_Y, secondDensity, values.stratumTwoBump(), dither)) {
            return 2;
        }

        double thirdDensity = values.stratumThree() - values.stratumFour() * 0.375 + 0.5;
        if (isCenteredStratum(y, Underworld.BEDROCK_STRATUM_THREE_CENTER_Y, thirdDensity, values.stratumThreeBump(), dither)) {
            return 3;
        }

        double fourthDensity = values.stratumFour() - values.stratumThree() * 0.375 + 0.5;
        return isCenteredStratum(y, Underworld.BEDROCK_STRATUM_FOUR_CENTER_Y, fourthDensity, values.stratumFourBump(), dither)
                ? 4
                : 0;
    }

    private static double firstDensity(NoiseValues values) {
        double density = Math.max(values.stratumOneA(), values.stratumOneB());
        density = addPositiveBump(density, values.stratumOneABump(), 0.25, 0.0);
        density = addPositiveBump(density, values.stratumOneBBump(), 0.125, 0.0);
        density = addPositiveBump(density, values.stratumOneCBump(), 0.125, 0.0);
        return addPositiveBump(density, values.stratumFourBump(), 0.09375, 0.125);
    }

    private static double addPositiveBump(double density, double bump, double scale, double offset) {
        return bump > 0.0 ? density + bump * scale + offset : density;
    }

    private static boolean isCenteredStratum(
            int y, int centerY, double density, double upperBump, LowerFaceDither dither) {
        if (density <= 0.0) {
            return false;
        }

        int displacement = y - centerY;
        if (displacement > 0) {
            density = addPositiveBump(density, upperBump, 0.25, 0.25);
        } else if (displacement < 0) {
            if (dither.narrowLowerFace(y)) {
                displacement++;
            }
            displacement = -displacement;
        }
        return displacement <= density * 2.0;
    }

    private static ResourceKey<NormalNoise.NoiseParameters> noiseKey(String path) {
        return ResourceKey.create(Registries.NOISE, InfiniteX.id(path));
    }

    @FunctionalInterface
    interface LowerFaceDither {
        boolean narrowLowerFace(int y);
    }

    record NoiseValues(
            double stratumOneA,
            double stratumOneB,
            double stratumTwo,
            double stratumThree,
            double stratumFour,
            double stratumOneABump,
            double stratumOneBBump,
            double stratumOneCBump,
            double stratumTwoBump,
            double stratumThreeBump,
            double stratumFourBump) {}

    record NoiseSet(
            NormalNoise stratumOneA,
            NormalNoise stratumOneB,
            NormalNoise stratumTwo,
            NormalNoise stratumThree,
            NormalNoise stratumFour,
            NormalNoise stratumOneABump,
            NormalNoise stratumOneBBump,
            NormalNoise stratumOneCBump,
            NormalNoise stratumTwoBump,
            NormalNoise stratumThreeBump,
            NormalNoise stratumFourBump) {
        static NoiseSet from(RandomState randomState) {
            return new NoiseSet(
                    randomState.getOrCreateNoise(STRATA_ONE_A),
                    randomState.getOrCreateNoise(STRATA_ONE_B),
                    randomState.getOrCreateNoise(STRATA_TWO),
                    randomState.getOrCreateNoise(STRATA_THREE),
                    randomState.getOrCreateNoise(STRATA_FOUR),
                    randomState.getOrCreateNoise(STRATA_ONE_A_BUMP),
                    randomState.getOrCreateNoise(STRATA_ONE_B_BUMP),
                    randomState.getOrCreateNoise(STRATA_ONE_C_BUMP),
                    randomState.getOrCreateNoise(STRATA_TWO_BUMP),
                    randomState.getOrCreateNoise(STRATA_THREE_BUMP),
                    randomState.getOrCreateNoise(STRATA_FOUR_BUMP));
        }

        NoiseValues sample(int x, int z) {
            return new NoiseValues(
                    sample(stratumOneA, x, z, STRATA_ONE_SCALE),
                    sample(stratumOneB, x, z, STRATA_ONE_SCALE),
                    sample(stratumTwo, x, z, STRATA_OTHER_SCALE),
                    sample(stratumThree, x, z, STRATA_OTHER_SCALE),
                    sample(stratumFour, x, z, STRATA_OTHER_SCALE),
                    sample(stratumOneABump, x, z, BUMP_ONE_A_SCALE),
                    sample(stratumOneBBump, x, z, BUMP_ONE_B_SCALE),
                    sample(stratumOneCBump, x, z, BUMP_ONE_C_SCALE),
                    sample(stratumTwoBump, x, z, BUMP_OTHER_SCALE),
                    sample(stratumThreeBump, x, z, BUMP_OTHER_SCALE),
                    sample(stratumFourBump, x, z, BUMP_OTHER_SCALE));
        }

        private static double sample(NormalNoise noise, int x, int z, double scale) {
            return noise.getValue(x * scale, 0.0, z * scale) * MITE_OCTAVE_GAIN;
        }
    }
}
