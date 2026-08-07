package com.pixulse.infx.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class FireCookingEventsTest {
    @Test
    void mapsOnlyOpenFireCookingPairs() {
        assertEquals(Items.COOKED_BEEF, FireCookingEvents.cookedResult(Items.BEEF));
        assertEquals(Items.COOKED_MUTTON, FireCookingEvents.cookedResult(Items.MUTTON));
        assertEquals(Items.COOKED_COD, FireCookingEvents.cookedResult(Items.COD));
        assertEquals(Items.COOKED_SALMON, FireCookingEvents.cookedResult(Items.SALMON));
        assertEquals(Items.BAKED_POTATO, FireCookingEvents.cookedResult(Items.POTATO));
        assertNull(FireCookingEvents.cookedResult(Items.RABBIT));
        assertFalse(FireCookingEvents.isCooked(Items.COOKED_RABBIT));
    }

    @Test
    void keepsCookingExperienceValues() {
        assertEquals(4, FireCookingEvents.cookingExperience(Items.COOKED_BEEF));
        assertEquals(3, FireCookingEvents.cookingExperience(Items.COOKED_COD));
        assertEquals(4, FireCookingEvents.cookingExperience(Items.COOKED_SALMON));
        assertEquals(2, FireCookingEvents.cookingExperience(Items.COOKED_MUTTON));
        assertEquals(0, FireCookingEvents.cookingExperience(Items.BAKED_POTATO));
        assertEquals(0, FireCookingEvents.cookingExperience(Items.BREAD));
    }

    @Test
    void addsProgressPerFireDamage() {
        assertEquals(3.0F, FireCookingEvents.addCookingProgress(0.0F, 1.0F));
        assertEquals(100.0F, FireCookingEvents.addCookingProgress(97.0F, 1.0F));
        assertTrue(FireCookingEvents.addCookingProgress(99.0F, 1.0F) >= 100.0F);
    }

    @Test
    void usesExponentialBulkCookingExtinguishChance() {
        assertEquals(0.0F, FireCookingEvents.extinguishChance(1));
        assertEquals(0.04F, FireCookingEvents.extinguishChance(2));
        assertEquals(0.32F, FireCookingEvents.extinguishChance(5));
        assertEquals(0.64F, FireCookingEvents.extinguishChance(6));
        assertEquals(1.0F, FireCookingEvents.extinguishChance(7));
        assertEquals(1.0F, FireCookingEvents.extinguishChance(16));
    }
}
