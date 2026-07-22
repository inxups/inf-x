package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.world.RunegateTeleportation;
import com.pixulse.infx.world.Underworld;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class RunegateAnimationTest {
    @Test
    void animationUsesMiteTwentyTickFadeAndThirtyTickExitCounter() {
        assertEquals(1, RunegateAnimation.nextCounter(true, 0));
        assertEquals(RunegateTeleportation.LOADING_TICKS, RunegateAnimation.nextCounter(true, 19));
        assertEquals(30, RunegateAnimation.nextCounter(true, RunegateTeleportation.LOADING_TICKS));
        assertEquals(29, RunegateAnimation.nextCounter(false, 30));
        assertEquals(0, RunegateAnimation.nextCounter(false, 0));
    }

    @Test
    void animationUsesMiteDimensionColorsAndOpacity() {
        assertEquals(0x80359FFF, RunegateAnimation.overlayColor(Level.OVERWORLD, 10));
        assertEquals(0xFF4401B4, RunegateAnimation.overlayColor(Underworld.LEVEL, 30));
        assertEquals(0xFFE47B4E, RunegateAnimation.overlayColor(Level.NETHER, 20));
        assertEquals(0xFFFFFFFF, RunegateAnimation.overlayColor(Level.END, 20));
    }
}
