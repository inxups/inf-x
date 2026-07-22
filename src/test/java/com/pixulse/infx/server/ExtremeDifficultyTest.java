package com.pixulse.infx.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import net.minecraft.world.Difficulty;
import org.junit.jupiter.api.Test;

class ExtremeDifficultyTest {
    @Test
    void vanillaDifficultiesAreNotExtreme() {
        assertFalse(ExtremeDifficulty.isActive(Difficulty.HARD, true));
        assertFalse(ExtremeDifficulty.isActive(Difficulty.NORMAL, true));
        assertFalse(ExtremeDifficulty.isExtreme(Difficulty.PEACEFUL));
    }

    @Test
    void treatsExtremeAsHardForGameplayValues() {
        assertSame(Difficulty.HARD, ExtremeDifficulty.gameplayDifficulty(ExtremeDifficulty.value()));
        assertSame(Difficulty.NORMAL, ExtremeDifficulty.gameplayDifficulty(Difficulty.NORMAL));
    }
}
