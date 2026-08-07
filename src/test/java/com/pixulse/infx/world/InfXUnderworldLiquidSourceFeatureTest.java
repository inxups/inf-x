package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class InfXUnderworldLiquidSourceFeatureTest {
    private static final BlockPos ORIGIN = new BlockPos(0, Underworld.MIN_Y, 0);
    private static final BlockPos SOURCE = new BlockPos(8, Underworld.TERRAIN_MIN_Y, 8);
    private static final InfXUnderworldLiquidSourceFeature FEATURE =
            new InfXUnderworldLiquidSourceFeature(NoneFeatureConfiguration.CODEC);

    @Test
    void samplesTheOriginalBottomWeightedLocalHeightRange() {
        RandomSource random = RandomSource.create(0x1A71D5L);
        boolean sawLowValue = false;
        boolean sawHighValue = false;

        for (int sample = 0; sample < 4_096; sample++) {
            int localY = InfXUnderworldLiquidSourceFeature.sampleLocalY(random);
            assertTrue(localY >= 0 && localY <= 126);
            sawLowValue |= localY < 8;
            sawHighValue |= localY > 96;
        }

        assertTrue(sawLowValue);
        assertTrue(sawHighValue);
    }

    @Test
    void usesTheWaterThresholdAtHigherLocalY() {
        assertAll(
                () -> assertFalse(InfXUnderworldLiquidSourceFeature.isUpperWaterAttempt(16, 0)),
                () -> assertTrue(InfXUnderworldLiquidSourceFeature.isUpperWaterAttempt(17, 0)),
                () -> assertFalse(InfXUnderworldLiquidSourceFeature.isUpperWaterAttempt(47, 31)),
                () -> assertTrue(InfXUnderworldLiquidSourceFeature.isUpperWaterAttempt(48, 31)));
    }

    @Test
    void enforcesTheMinimumWaterGenerationHeight() {
        assertAll(
                () -> assertFalse(InfXUnderworldLiquidSourceFeature.isWaterAllowedAtY(99)),
                () -> assertTrue(InfXUnderworldLiquidSourceFeature.isWaterAllowedAtY(100)),
                () -> assertTrue(InfXUnderworldLiquidSourceFeature.isWaterAllowedAtY(120)));
    }

    @Test
    void requiresExactlyThreeStoneNeighborsAndOneAirNeighbor() {
        UnderworldFeatureTestLevel level = new UnderworldFeatureTestLevel(0L);
        level.setRaw(SOURCE, Blocks.CAVE_AIR.defaultBlockState());
        level.setRaw(SOURCE.north(), Blocks.CAVE_AIR.defaultBlockState());

        assertTrue(InfXUnderworldLiquidSourceFeature.hasValidSourceGeometry(level.world(), SOURCE));
        level.setRaw(SOURCE.north(), Blocks.WATER.defaultBlockState());
        assertFalse(InfXUnderworldLiquidSourceFeature.hasValidSourceGeometry(level.world(), SOURCE));
    }

    @Test
    void neverUsesDeepSlateAsALiquidSourceHost() {
        UnderworldFeatureTestLevel level = new UnderworldFeatureTestLevel(0L);
        level.setRaw(SOURCE, Blocks.DEEPSLATE.defaultBlockState());
        level.setRaw(SOURCE.north(), Blocks.CAVE_AIR.defaultBlockState());

        assertFalse(InfXUnderworldLiquidSourceFeature.hasValidSourceGeometry(level.world(), SOURCE));
    }

    @Test
    void placesAndSchedulesOnlyTheConstrainedSource() {
        UnderworldFeatureTestLevel level = new UnderworldFeatureTestLevel(0L);
        level.setRaw(SOURCE, Blocks.CAVE_AIR.defaultBlockState());
        level.setRaw(SOURCE.north(), Blocks.CAVE_AIR.defaultBlockState());

        assertTrue(place(level, new ZeroRandom()));
        assertAll(
                () -> assertEquals(Blocks.LAVA, level.blockAt(SOURCE).getBlock()),
                () -> assertTrue(level.wasScheduled(SOURCE)),
                () -> assertEquals(Blocks.STONE, level.blockAt(SOURCE.below()).getBlock()));
    }

    private static boolean place(UnderworldFeatureTestLevel level, RandomSource random) {
        return FEATURE.place(new FeaturePlaceContext<>(
                Optional.empty(), level.world(), null, random, ORIGIN, NoneFeatureConfiguration.INSTANCE));
    }

    private static class ZeroRandom implements RandomSource {
        private final RandomSource delegate = RandomSource.create(0L);

        @Override
        public RandomSource fork() {
            return this.delegate.fork();
        }

        @Override
        public PositionalRandomFactory forkPositional() {
            return this.delegate.forkPositional();
        }

        @Override
        public void setSeed(long seed) {
            this.delegate.setSeed(seed);
        }

        @Override
        public int nextInt() {
            return 0;
        }

        @Override
        public int nextInt(int bound) {
            return 0;
        }

        @Override
        public long nextLong() {
            return 0L;
        }

        @Override
        public boolean nextBoolean() {
            return false;
        }

        @Override
        public float nextFloat() {
            return 0.0F;
        }

        @Override
        public double nextDouble() {
            return 0.0D;
        }

        @Override
        public double nextGaussian() {
            return 0.0D;
        }
    }
}
