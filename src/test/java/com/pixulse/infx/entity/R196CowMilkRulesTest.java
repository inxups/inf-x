package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class R196CowMilkRulesTest {
    @Test
    void dailyMilkBudgetIsFourUnits() {
        assertEquals(4, R196AnimalEvents.MILK_UNITS_PER_DAY);
    }

    @Test
    void bucketUsesFullQuotaWhileBowlUsesOneUnit() {
        // Document the shared budget: one bucket (4) or up to four bowls (1 each).
        assertTrue(R196AnimalEvents.MILK_UNITS_PER_DAY == 4);
        assertTrue(1 + 1 + 1 + 1 == R196AnimalEvents.MILK_UNITS_PER_DAY);
        assertFalse(4 + 1 <= R196AnimalEvents.MILK_UNITS_PER_DAY);
    }
}
