package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BoneMealItem;
import org.junit.jupiter.api.Test;

class ManureEventsTest {
    @Test
    void livestockIntervalsMatchR196() {
        assertEquals(24_000, ManureEvents.interval(EntityType.COW));
        assertEquals(24_000, ManureEvents.interval(EntityType.MOOSHROOM));
        assertEquals(48_000, ManureEvents.interval(EntityType.PIG));
        assertEquals(48_000, ManureEvents.interval(EntityType.SHEEP));
        assertEquals(384_000, ManureEvents.interval(EntityType.CHICKEN));
        assertEquals(0, ManureEvents.interval(EntityType.WOLF));
    }

    @Test
    void manureIsNotABoneMealGrowthItem() {
        assertFalse(BoneMealItem.class.isAssignableFrom(ManureItem.class));
    }
}
