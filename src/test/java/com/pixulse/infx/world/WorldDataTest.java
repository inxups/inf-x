package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class WorldDataTest {
    @Test
    void mansionExperienceMustComeFromOnePlayerRatherThanTheWholeServer() {
        WorldData data = new WorldData();
        UUID firstPlayer = UUID.randomUUID();
        UUID secondPlayer = UUID.randomUUID();

        assertFalse(data.recordMansionExperienceGain(firstPlayer, WorldData.MANSION_EXPERIENCE_REQUIREMENT - 1L));
        assertFalse(data.recordMansionExperienceGain(secondPlayer, 1L));
        assertTrue(data.recordMansionExperienceGain(firstPlayer, 1L));
        assertTrue(data.mansionExperienceEarned());
    }

    @Test
    void existingPlayerExperienceCanSatisfyTheMansionRequirementOnLogin() {
        WorldData data = new WorldData();
        UUID player = UUID.randomUUID();
        long nearlyComplete = WorldData.MANSION_EXPERIENCE_REQUIREMENT - 1L;

        assertFalse(data.observeMansionExperience(player, nearlyComplete));
        assertFalse(data.observeMansionExperience(player, nearlyComplete));
        assertTrue(data.recordMansionExperienceGain(player, 1L));
        assertTrue(data.mansionExperienceEarned());
    }
}
