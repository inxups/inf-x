package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.junit.jupiter.api.Test;

class InfXUnderworldBrownMushroomFeatureTest {
    private static final BlockPos ORIGIN = new BlockPos(0, Underworld.MIN_Y, 0);
    private static final BlockPos CANDIDATE = new BlockPos(8, Underworld.TERRAIN_MIN_Y, 8);
    private static final InfXUnderworldBrownMushroomFeature FEATURE =
            new InfXUnderworldBrownMushroomFeature(NoneFeatureConfiguration.CODEC);

    @Test
    void onlyTheZeroRollStartsTheSingleChunkCandidate() {
        assertAll(
                () -> assertTrue(InfXUnderworldBrownMushroomFeature.isBrownMushroomRoll(0)),
                () -> assertFalse(InfXUnderworldBrownMushroomFeature.isBrownMushroomRoll(1)),
                () -> assertFalse(InfXUnderworldBrownMushroomFeature.isBrownMushroomRoll(2)),
                () -> assertFalse(InfXUnderworldBrownMushroomFeature.isBrownMushroomRoll(3)));
    }

    @Test
    void placesTheCandidateOnlyInAirAboveTerrainStone() {
        UnderworldFeatureTestLevel level = new UnderworldFeatureTestLevel(0L);
        level.setRaw(CANDIDATE, Blocks.CAVE_AIR.defaultBlockState());

        assertTrue(place(level, new ZeroRandom()));
        assertEquals(Blocks.BROWN_MUSHROOM, level.blockAt(CANDIDATE).getBlock());
    }

    @Test
    void doesNotPlaceWhenTheOneInFourRollMisses() {
        UnderworldFeatureTestLevel level = new UnderworldFeatureTestLevel(0L);
        level.setRaw(CANDIDATE, Blocks.CAVE_AIR.defaultBlockState());

        assertFalse(place(level, new MissRandom()));
        assertTrue(level.blockAt(CANDIDATE).isAir());
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

    private static final class MissRandom extends ZeroRandom {
        @Override
        public int nextInt(int bound) {
            return bound == Underworld.BROWN_MUSHROOM_CHANCE ? 1 : 0;
        }
    }
}
