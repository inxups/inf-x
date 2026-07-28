package com.pixulse.infx.agriculture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GrassTramplingTest {
    @Test
    void tramplingEffectMatchesMiteCurve() {
        assertEquals(0.0F, GrassTrampling.tramplingEffect(0), 1.0e-6F);
        assertEquals(0.0F, GrassTrampling.tramplingEffect(3), 1.0e-6F);
        assertEquals(0.05F, GrassTrampling.tramplingEffect(4), 1.0e-6F);
        assertEquals(0.5F, GrassTrampling.tramplingEffect(15), 1.0e-6F);
        assertEquals(0.5F, GrassTrampling.tramplingEffect(20), 1.0e-6F);
    }

    @Test
    void blendColorPullsBiomeGreenTowardManureBrown() {
        int lush = 0xFF55AA33;
        int blended = GrassTrampling.blendColor(lush, 15);
        int r = (blended >> 16) & 0xFF;
        int g = (blended >> 8) & 0xFF;
        int b = blended & 0xFF;
        assertTrue(r > ((lush >> 16) & 0xFF) || Math.abs(r - GrassTrampling.MANURE_RED) < 40);
        assertTrue(Math.abs(r - GrassTrampling.MANURE_RED) < Math.abs(((lush >> 16) & 0xFF) - GrassTrampling.MANURE_RED)
                || r == GrassTrampling.MANURE_RED);
        // Half-blend at max effect 0.5
        assertEquals((int) (0x55 * 0.5F + 134 * 0.5F), r);
        assertEquals((int) (0xAA * 0.5F + 96 * 0.5F), g);
        assertEquals((int) (0x33 * 0.5F + 67 * 0.5F), b);
    }

    @Test
    void zeroTramplingKeepsBiomeColor() {
        assertEquals(0xFF112233, GrassTrampling.blendColor(0xFF112233, 0));
        assertEquals(0xFF112233, GrassTrampling.blendColor(0xFF112233, 3));
    }
}
