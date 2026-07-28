package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.entity.EntityType;
import org.junit.jupiter.api.Test;

class R196ManureEventsTest {
    @Test
    void livestockIntervalsMatchR196() {
        assertEquals(24_000, R196ManureEvents.interval(EntityType.COW));
        assertEquals(24_000, R196ManureEvents.interval(EntityType.MOOSHROOM));
        assertEquals(48_000, R196ManureEvents.interval(EntityType.PIG));
        assertEquals(48_000, R196ManureEvents.interval(EntityType.SHEEP));
        assertEquals(384_000, R196ManureEvents.interval(EntityType.CHICKEN));
        assertEquals(0, R196ManureEvents.interval(EntityType.WOLF));
    }
}
