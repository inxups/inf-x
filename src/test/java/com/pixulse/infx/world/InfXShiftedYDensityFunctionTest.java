package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.junit.jupiter.api.Test;

class InfXShiftedYDensityFunctionTest {
    @Test
    void samplesTheWrappedFunctionAtTheOffsetY() {
        DensityFunction y = DensityFunctions.yClampedGradient(-1_000, 1_000, -1_000, 1_000);
        DensityFunction shifted = new InfXShiftedYDensityFunction(y, 120);

        assertEquals(0.0, shifted.compute(new DensityFunction.SinglePointContext(3, 120, 7)));
        assertEquals(9.0, shifted.compute(new DensityFunction.SinglePointContext(3, 129, 7)), 1.0E-12);
    }

    @Test
    void mapAllVisitsTheWrappedFunctionBeforeTheWrapper() {
        DensityFunction input = DensityFunctions.constant(2.0);
        DensityFunction shifted = new InfXShiftedYDensityFunction(input, 120);
        DensityFunction mapped = shifted.mapAll(function ->
                function == input ? DensityFunctions.constant(5.0) : function);

        assertEquals(5.0, mapped.compute(new DensityFunction.SinglePointContext(0, 120, 0)));
    }
}
