package com.pixulse.infx.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class ItemEventsTest {
    @Test
    void gemExperienceMapsTheFourRedeemableItems() {
        assertEquals(500, ItemEvents.gemExperience(Items.DIAMOND));
        assertEquals(250, ItemEvents.gemExperience(Items.EMERALD));
        assertEquals(50, ItemEvents.gemExperience(Items.LAPIS_LAZULI));
        assertEquals(25, ItemEvents.gemExperience(Items.QUARTZ));
    }

    @Test
    void otherItemsCannotBeRedeemed() {
        assertEquals(0, ItemEvents.gemExperience(Items.IRON_INGOT));
        assertEquals(0, ItemEvents.gemExperience(Items.QUARTZ_BLOCK));
        assertEquals(0, ItemEvents.gemExperience(Items.COAL));
    }
}
