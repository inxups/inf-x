package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.pixulse.infx.world.MoonPhase;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.junit.jupiter.api.Test;

class MoonCelestialRendererTest {
    @Test
    void specialMoonTintsMatchMiteAndPreserveRainAlpha() {
        Vector4fc source = new Vector4f(1.0F, 1.0F, 1.0F, 0.65F);
        assertColor(MoonCelestialRenderer.tintFor(MoonPhase.BLOOD, true, source), 0.6F, 0.2F, 0.1F, 0.65F);
        assertColor(MoonCelestialRenderer.tintFor(MoonPhase.YELLOW, true, source), 1.0F, 0.8F, 0.45F, 0.65F);
        assertColor(MoonCelestialRenderer.tintFor(MoonPhase.BLUE, true, source), 0.66F, 0.74F, 1.0F, 0.65F);
        assertSame(source, MoonCelestialRenderer.tintFor(MoonPhase.BLOOD, false, source));
        assertSame(source, MoonCelestialRenderer.tintFor(MoonPhase.PHANTOM, true, source));
    }

    private static void assertColor(Vector4fc actual, float red, float green, float blue, float alpha) {
        assertEquals(red, actual.x(), 1.0E-6F);
        assertEquals(green, actual.y(), 1.0E-6F);
        assertEquals(blue, actual.z(), 1.0E-6F);
        assertEquals(alpha, actual.w(), 1.0E-6F);
    }
}
