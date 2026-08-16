package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class StickBoneItemsTest {
    @Test
    void restoresPerHitBreakChances() {
        assertEquals(50, StickBoneItems.breakDenominator(Items.STICK));
        assertEquals(100, StickBoneItems.breakDenominator(Items.BONE));
        assertEquals(0, StickBoneItems.breakDenominator(Items.FLINT));
    }
}
