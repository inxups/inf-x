package com.pixulse.infx.event;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.world.InfXMushroomGrowth;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AgricultureEventsTest {
    @Test
    void mushroomGrowthHasFourTiers() {
        assertTrue(
                InfXMushroomGrowth.GROWTH.getPossibleValues().containsAll(Set.of(0, 1, 2, 3)),
                "MITE mushroom growth tiers must span 0..3");
    }
}
