package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.pixulse.infx.server.ExtremeDifficulty;
import net.minecraft.world.Difficulty;
import org.junit.jupiter.api.Test;

class WorldCreationDifficultyPolicyTest {
    @Test
    void keepsHardcoreWorldCreationOnExtremeDifficulty() {
        assertSame(ExtremeDifficulty.value(), WorldCreationDifficultyPolicy.resolve(Difficulty.HARD, true));
    }

    @Test
    void leavesNonHardcoreDifficultyUntouched() {
        assertSame(Difficulty.NORMAL, WorldCreationDifficultyPolicy.resolve(Difficulty.NORMAL, false));
    }
}
