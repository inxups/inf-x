package com.pixulse.infx.player;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PlayerProgressionEventsTest {
    @Test
    void meleeUsesR196PositiveBonusAndNegativeDebtPenalty() {
        assertEquals(1.0F, PlayerProgressionEvents.meleeMultiplier(0), 0.0001F);
        assertEquals(1.05F, PlayerProgressionEvents.meleeMultiplier(10), 0.0001F);
        assertEquals(2.0F, PlayerProgressionEvents.meleeMultiplier(200), 0.0001F);
        assertEquals(0.98F, PlayerProgressionEvents.meleeMultiplier(-1), 0.0001F);
        assertEquals(0.2F, PlayerProgressionEvents.meleeMultiplier(-40), 0.0001F);
    }
}
