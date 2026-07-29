package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.event.server.ExtremeDifficulty;
import net.minecraft.world.level.GameType;
import org.junit.jupiter.api.Test;

class WorldCreationLockProfileTest {
    @Test
    void exposesTheRequestedSinglePlayerProfile() {
        assertEquals(GameType.SURVIVAL, WorldCreationLockProfile.GAME_TYPE);
        assertTrue(ExtremeDifficulty.isExtreme(WorldCreationLockProfile.difficulty()));
        assertFalse(WorldCreationLockProfile.ALLOW_COMMANDS);
        assertFalse(WorldCreationLockProfile.BONUS_CHEST);
        assertFalse(WorldCreationLockProfile.ALLOW_ADVANCED_CONFIGURATION);
    }
}
