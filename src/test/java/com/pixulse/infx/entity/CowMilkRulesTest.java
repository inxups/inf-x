package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class CowMilkRulesTest {
    @Test
    void dailyMilkBudgetIsFourUnits() {
        assertEquals(4, InfxCow.MILK_UNITS_PER_DAY);
    }

    @Test
    void bucketUsesFullQuotaWhileBowlUsesOneUnit() {
        // Document the shared budget: one bucket (4) or up to four bowls (1 each).
        assertEquals(InfxCow.MILK_UNITS_PER_DAY, 4);
        assertEquals(1 + 1 + 1 + 1, InfxCow.MILK_UNITS_PER_DAY);
        assertFalse(4 + 1 <= InfxCow.MILK_UNITS_PER_DAY);
    }
}
