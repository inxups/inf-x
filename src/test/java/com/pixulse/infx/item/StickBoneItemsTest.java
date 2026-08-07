package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class StickBoneItemsTest {
    @Test
    void restoresStackLimitAndMeleeReach() {
        assertEquals(32, StickBoneItems.stackLimit(Items.STICK, 64));
        assertEquals(16, StickBoneItems.stackLimit(Items.BONE, 64));

        var reach = StickBoneItems.meleeAttackRange();
        assertEquals(0.0F, reach.minReach());
        assertEquals(2.0F, reach.maxReach());
        assertEquals(0.0F, reach.minCreativeReach());
        assertEquals(5.0F, reach.maxCreativeReach());
        assertEquals(0.0F, reach.hitboxMargin());
        assertEquals(1.0F, reach.mobFactor());
    }

    @Test
    void restoresPerHitBreakChances() {
        assertEquals(50, StickBoneItems.breakDenominator(Items.STICK));
        assertEquals(100, StickBoneItems.breakDenominator(Items.BONE));
        assertEquals(0, StickBoneItems.breakDenominator(Items.FLINT));
    }
}
