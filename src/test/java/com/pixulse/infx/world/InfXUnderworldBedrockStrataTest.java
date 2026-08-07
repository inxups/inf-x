package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.junit.jupiter.api.Test;

class InfXUnderworldBedrockStrataTest {
    private static final long TEST_SEED = 0x51A7A5L;

    @Test
    void firstStratumConnectsDownwardFromTheLowestCenter() {
        var values = values(1.0, 0.0, 0.0, 0.0, 0.0);

        assertEquals(1, InfXUnderworldBedrockStrata.selectedStratum(-128, values, y -> false));
        assertEquals(1, InfXUnderworldBedrockStrata.selectedStratum(-113, values, y -> false));
        assertEquals(0, InfXUnderworldBedrockStrata.selectedStratum(-112, values, y -> false));
        assertEquals(0, InfXUnderworldBedrockStrata.selectedStratum(-129, values, y -> false));
        assertEquals(0, InfXUnderworldBedrockStrata.selectedStratum(120, values, y -> false));
    }

    @Test
    void higherStrataUseTheConfiguredCentersAndPriorityOrder() {
        var second = values(-1.0, -1.0, -0.75, -1.0, -1.0);
        var third = values(-1.0, -1.0, -10.0, 1.0, 0.0);
        var fourth = values(-1.0, -1.0, -10.0, 0.0, 1.0);
        var overlapping = values(1.0, 0.0, 50.0, -10.0, -10.0);

        assertEquals(2, InfXUnderworldBedrockStrata.selectedStratum(-72, second, y -> false));
        assertEquals(3, InfXUnderworldBedrockStrata.selectedStratum(-16, third, y -> false));
        assertEquals(4, InfXUnderworldBedrockStrata.selectedStratum(99, fourth, y -> false));
        assertEquals(1, InfXUnderworldBedrockStrata.selectedStratum(-120, overlapping, y -> false));
    }

    @Test
    void lowerFacesUseAStableOneBlockDither() {
        var values = values(-1.0, -1.0, -0.75, -1.0, -1.0);

        assertFalse(InfXUnderworldBedrockStrata.selectedStratum(-74, values, y -> false) == 2);
        assertEquals(2, InfXUnderworldBedrockStrata.selectedStratum(-74, values, y -> true));
    }

    @Test
    void onlyDeepslateIsReplacedByInternalBedrock() {
        var values = values(1.0, 0.0, 0.0, 0.0, 0.0);

        assertTrue(InfXUnderworldBedrockStrata.replacementFor(
                        Blocks.DEEPSLATE.defaultBlockState(), -120, values, y -> false)
                .is(Blocks.BEDROCK));
        assertTrue(InfXUnderworldBedrockStrata.replacementFor(
                        Blocks.STONE.defaultBlockState(), -120, values, y -> false)
                .is(Blocks.STONE));
        assertTrue(InfXUnderworldBedrockStrata.replacementFor(
                        Blocks.BEDROCK.defaultBlockState(), -120, values, y -> false)
                .is(Blocks.BEDROCK));
        assertTrue(InfXUnderworldBedrockStrata.replacementFor(
                        Blocks.WATER.defaultBlockState(), -120, values, y -> false)
                .is(Blocks.WATER));
        assertTrue(InfXUnderworldBedrockStrata.replacementFor(
                        Blocks.AIR.defaultBlockState(), -120, values, y -> false)
                .isAir());
    }

    @Test
    void seededNoiseSamplingIsDeterministicAndVariesBySeed() {
        long first = strataHash(noiseSet(TEST_SEED));
        long repeated = strataHash(noiseSet(TEST_SEED));
        long different = strataHash(noiseSet(TEST_SEED + 1));

        assertEquals(first, repeated);
        assertNotEquals(first, different);
    }

    private static long strataHash(InfXUnderworldBedrockStrata.NoiseSet noises) {
        long hash = 1_125_899_906_842_597L;
        for (int x = -128; x <= 128; x += 16) {
            for (int z = -128; z <= 128; z += 16) {
                var values = noises.sample(x, z);
                for (int y = Underworld.INTERNAL_BEDROCK_MIN_Y;
                        y < Underworld.INTERNAL_BEDROCK_MAX_Y_EXCLUSIVE;
                        y += 2) {
                    int stratum = InfXUnderworldBedrockStrata.selectedStratum(y, values, lowerDither(TEST_SEED, x, z));
                    hash = hash * 31 + stratum;
                }
            }
        }
        return hash;
    }

    private static InfXUnderworldBedrockStrata.LowerFaceDither lowerDither(long seed, int x, int z) {
        return y -> ((seed + x * 341873128712L + y * 132897987541L + z * 42317861L) & 1L) == 0L;
    }

    private static InfXUnderworldBedrockStrata.NoiseSet noiseSet(long seed) {
        var random = new LegacyRandomSource(seed);
        return new InfXUnderworldBedrockStrata.NoiseSet(
                noise(random), noise(random), noise(random), noise(random), noise(random), noise(random), noise(random),
                noise(random), noise(random), noise(random), noise(random));
    }

    private static NormalNoise noise(LegacyRandomSource random) {
        return NormalNoise.create(random, -3, 1.0, 1.0, 1.0, 1.0);
    }

    private static InfXUnderworldBedrockStrata.NoiseValues values(
            double firstA, double firstB, double second, double third, double fourth) {
        return new InfXUnderworldBedrockStrata.NoiseValues(
                firstA, firstB, second, third, fourth, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }
}
