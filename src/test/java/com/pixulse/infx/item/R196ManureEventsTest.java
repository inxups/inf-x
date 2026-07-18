package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.entity.EntityTypes;
import org.junit.jupiter.api.Test;

class R196ManureEventsTest {
    @Test
    void livestockIntervalsMatchR196() {
        assertEquals(24_000, R196ManureEvents.interval(EntityTypes.COW));
        assertEquals(24_000, R196ManureEvents.interval(EntityTypes.MOOSHROOM));
        assertEquals(48_000, R196ManureEvents.interval(EntityTypes.PIG));
        assertEquals(48_000, R196ManureEvents.interval(EntityTypes.SHEEP));
        assertEquals(384_000, R196ManureEvents.interval(EntityTypes.CHICKEN));
        assertEquals(0, R196ManureEvents.interval(EntityTypes.WOLF));
    }
}
