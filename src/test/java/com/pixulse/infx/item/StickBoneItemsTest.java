package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.registry.InfXAttributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class StickBoneItemsTest {
    @Test
    void restoresStackLimitAndBothReachModifiers() {
        assertEquals(32, StickBoneItems.stackLimit(Items.STICK, 64));
        assertEquals(16, StickBoneItems.stackLimit(Items.BONE, 64));

        var attributes = StickBoneItems.reachAttributes(ItemAttributeModifiers.EMPTY);
        assertEquals(
                3.0,
                attributes.compute(
                        InfXAttributes.ITEM_INTERACTION_RANGE,
                        ItemReach.BASE_RANGE,
                        EquipmentSlot.MAINHAND));
        assertEquals(
                3.0,
                attributes.compute(
                        InfXAttributes.ITEM_MELEE_RANGE,
                        ItemReach.BASE_RANGE,
                        EquipmentSlot.MAINHAND));
        assertEquals(
                ItemReach.BASE_RANGE,
                attributes.compute(
                        InfXAttributes.ITEM_INTERACTION_RANGE,
                        ItemReach.BASE_RANGE,
                        EquipmentSlot.OFFHAND));
        assertEquals(
                ItemReach.BASE_RANGE,
                attributes.compute(
                        InfXAttributes.ITEM_MELEE_RANGE,
                        ItemReach.BASE_RANGE,
                        EquipmentSlot.OFFHAND));
    }

    @Test
    void restoresPerHitBreakChances() {
        assertEquals(50, StickBoneItems.breakDenominator(Items.STICK));
        assertEquals(100, StickBoneItems.breakDenominator(Items.BONE));
        assertEquals(0, StickBoneItems.breakDenominator(Items.FLINT));
    }
}
