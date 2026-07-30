package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CraftingProgressSmootherTest {
    private static final long TICK = CraftingProgressSmoother.INTERPOLATION_NANOS;

    @Test
    void interpolatesBetweenServerTicksAndStopsWhenProgressStops() {
        CraftingProgressSmoother smoother = new CraftingProgressSmoother();

        assertEquals(0.0F, smoother.sample(0, 100, true, 0, 0L), 0.0001F);
        assertEquals(0.0F, smoother.sample(1, 100, true, 0, TICK), 0.0001F);
        assertEquals(0.005F, smoother.sample(1, 100, true, 0, TICK + TICK / 2), 0.0001F);
        assertEquals(0.01F, smoother.sample(1, 100, true, 0, TICK * 2), 0.0001F);
        assertEquals(0.01F, smoother.sample(1, 100, true, 0, TICK * 3), 0.0001F);
    }

    @Test
    void cancellationClearsDisplayImmediately() {
        CraftingProgressSmoother smoother = new CraftingProgressSmoother();
        smoother.sample(50, 100, true, 0, 0L);

        assertEquals(0.0F, smoother.sample(0, 0, false, 0, TICK), 0.0001F);
    }

    @Test
    void completedCycleReachesFullBeforeResetting() {
        CraftingProgressSmoother smoother = new CraftingProgressSmoother();
        assertEquals(0.99F, smoother.sample(99, 100, true, 0, 0L), 0.0001F);

        assertEquals(0.99F, smoother.sample(0, 0, false, 1, TICK), 0.0001F);
        assertEquals(0.995F, smoother.sample(0, 0, false, 1, TICK + TICK / 2), 0.0001F);
        assertEquals(1.0F, smoother.sample(0, 0, false, 1, TICK * 2), 0.0001F);
        assertEquals(1.0F, smoother.sample(0, 0, false, 1, TICK * 2 + TICK / 2), 0.0001F);
        assertEquals(0.0F, smoother.sample(0, 0, false, 1, TICK * 3), 0.0001F);
    }

    @Test
    void pixelSplitUsesAlphaForTheNextLogicalPixel() {
        CraftingProgressSmoother.PixelFill fill = CraftingProgressSmoother.splitPixels(0.52F, 24);

        assertEquals(12, fill.fullPixels());
        assertEquals(0.48F, fill.nextPixelAlpha(), 0.0001F);
        assertEquals(24, CraftingProgressSmoother.splitPixels(1.0F, 24).fullPixels());
        assertEquals(0.0F, CraftingProgressSmoother.splitPixels(1.0F, 24).nextPixelAlpha(), 0.0001F);
    }
}
