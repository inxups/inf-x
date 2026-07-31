package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.junit.jupiter.api.Test;

class InfXUnderworldFluidPickerTest {
    private static final double ANCIENT_CITY_ADJUSTED_DENSITY = -0.18656130456018377;
    private static final Aquifer AQUIFER = Aquifer.createDisabled(
            InfXUnderworldFluidPicker.create(settings()));

    @Test
    void negativeDensityHonorsTheWaterFloorAndSeaLevel() {
        assertAll(
                () -> assertEquals(Blocks.AIR.defaultBlockState(), substanceAt(99, -1.0)),
                () -> assertEquals(Blocks.WATER.defaultBlockState(), substanceAt(100, -1.0)),
                () -> assertEquals(Blocks.WATER.defaultBlockState(), substanceAt(143, -1.0)),
                () -> assertEquals(Blocks.AIR.defaultBlockState(), substanceAt(144, -1.0)));
    }

    @Test
    void keepsTheExistingLowLavaBoundary() {
        assertAll(
                () -> assertEquals(Blocks.LAVA.defaultBlockState(), substanceAt(-55, -1.0)),
                () -> assertEquals(Blocks.AIR.defaultBlockState(), substanceAt(-54, -1.0)));
    }

    @Test
    void ancientCityTerrainAdjustmentCreatesDryAirBelowTheWaterFloor() {
        assertEquals(Blocks.AIR.defaultBlockState(), substanceAt(-51, ANCIENT_CITY_ADJUSTED_DENSITY));
    }

    @Test
    void positiveDensityStillFallsBackToTheDefaultSolidBlock() {
        assertNull(substanceAt(99, 1.0));
    }

    private static net.minecraft.world.level.block.state.BlockState substanceAt(int y, double density) {
        return AQUIFER.computeSubstance(new DensityFunction.SinglePointContext(0, y, 0), density);
    }

    private static NoiseGeneratorSettings settings() {
        return new NoiseGeneratorSettings(
                NoiseSettings.create(Underworld.MIN_Y, Underworld.HEIGHT, 1, 2),
                Blocks.STONE.defaultBlockState(),
                Blocks.WATER.defaultBlockState(),
                NoiseRouterData.none(),
                SurfaceRules.state(Blocks.STONE.defaultBlockState()),
                List.of(),
                Underworld.WATER_LEVEL,
                false,
                false,
                false,
                true);
    }
}
