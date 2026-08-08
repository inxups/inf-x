package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PatrolRulesTest {
    @Test
    void patrolsNeedTheDayFloorAndTheVillageUnlock() {
        assertFalse(PatrolRules.maySpawn(PatrolRules.MINIMUM_PATROL_DAY - 1L, true),
                "patrols must not spawn before day 32 even with the village unlocked");
        assertFalse(PatrolRules.maySpawn(PatrolRules.MINIMUM_PATROL_DAY, false),
                "patrols must not spawn before the village condition is met");
        assertTrue(PatrolRules.maySpawn(PatrolRules.MINIMUM_PATROL_DAY, true),
                "patrols may spawn once both gates pass");
        assertTrue(PatrolRules.maySpawn(1_000L, true),
                "patrols stay allowed on later days");
    }
}
