package com.pixulse.infx.world;

import net.minecraft.SharedConstants;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

/** Keeps structure-adjusted cavities dry below the Underworld's minimum water height. */
final class InfXUnderworldFluidPicker {
    private static final int LAVA_LEVEL = -54;
    private static final Aquifer.FluidStatus LAVA =
            new Aquifer.FluidStatus(LAVA_LEVEL, Blocks.LAVA.defaultBlockState());
    private static final Aquifer.FluidStatus EMPTY =
            new Aquifer.FluidStatus(DimensionType.MIN_Y * 2, Blocks.AIR.defaultBlockState());

    private InfXUnderworldFluidPicker() {
    }

    static Aquifer.FluidPicker create(NoiseGeneratorSettings settings) {
        int seaLevel = settings.seaLevel();
        Aquifer.FluidStatus sea = new Aquifer.FluidStatus(seaLevel, settings.defaultFluid());
        return (x, y, z) -> {
            if (SharedConstants.DEBUG_DISABLE_FLUID_GENERATION) {
                return EMPTY;
            }
            if (y < Math.min(LAVA_LEVEL, seaLevel)) {
                return LAVA;
            }
            return y < Underworld.WATER_MIN_Y ? EMPTY : sea;
        };
    }
}
