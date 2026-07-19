package com.pixulse.infx.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.Difficulty;
import org.junit.jupiter.api.Test;

class ExtremeDifficultyTest {
    @Test
    void extremeUsesTheHighestVanillaDifficultyAndRequiresTheWorldLock() {
        assertTrue(ExtremeDifficulty.isActive(Difficulty.HARD, true));
        assertFalse(ExtremeDifficulty.isActive(Difficulty.HARD, false));
        assertFalse(ExtremeDifficulty.isActive(Difficulty.NORMAL, true));
    }
}
