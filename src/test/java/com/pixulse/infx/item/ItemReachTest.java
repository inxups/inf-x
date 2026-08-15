package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.InfXAttributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.junit.jupiter.api.Test;

class ItemReachTest {
    @Test
    void heightAdjustmentUsesHalfTheExcessAndClampsToOneBlock() {
        assertEquals(0.0, ItemReach.heightAdjustment(0.5));
        assertEquals(0.00005, ItemReach.heightAdjustment(0.5001), 1.0E-9);
        assertEquals(0.25, ItemReach.heightAdjustment(1.0));
        assertEquals(1.0, ItemReach.heightAdjustment(2.5));
        assertEquals(1.0, ItemReach.heightAdjustment(4.0));
        assertEquals(0.0, ItemReach.heightAdjustment(-0.5));
        assertEquals(-0.00005, ItemReach.heightAdjustment(-0.5001), 1.0E-9);
        assertEquals(-0.25, ItemReach.heightAdjustment(-1.0));
        assertEquals(-1.0, ItemReach.heightAdjustment(-2.5));
        assertEquals(-1.0, ItemReach.heightAdjustment(-4.0));
    }

    @Test
    void blockTargetingRangeMirrorsTheServerPlacementBuffer() {
        assertEquals(1.0, ItemReach.BLOCK_TARGET_BUFFER, 1.0E-9);
        assertEquals(3.5, ItemReach.blockTargetingRange(ItemReach.BASE_RANGE), 1.0E-9);
        assertEquals(4.25, ItemReach.blockTargetingRange(3.25), 1.0E-9);
        assertEquals(6.0, ItemReach.blockTargetingRange(ItemReach.CREATIVE_RANGE), 1.0E-9);
    }

    @Test
    void positiveMeleeBonusRequiresAnInfxMainHandModifier() {
        assertFalse(ItemReach.hasPositiveMeleeBonus(ItemAttributeModifiers.EMPTY));
        assertFalse(ItemReach.hasPositiveMeleeBonus(modifiersWithMeleeBonus(EquipmentSlotGroup.OFFHAND)));
        assertTrue(ItemReach.hasPositiveMeleeBonus(modifiersWithMeleeBonus(EquipmentSlotGroup.MAINHAND)));
    }

    @Test
    void mainHandModifierCalculationClampsToTheAttributeBounds() {
        assertEquals(
                ItemReach.BASE_RANGE,
                ItemReach.applyMainHandModifiers(
                        modifiersWithMeleeBonus(EquipmentSlotGroup.OFFHAND),
                        InfXAttributes.ITEM_MELEE_RANGE,
                        ItemReach.BASE_RANGE));
        assertEquals(
                ItemReach.MAX_RANGE,
                ItemReach.applyMainHandModifiers(
                        modifiersWithMeleeBonus(EquipmentSlotGroup.MAINHAND, 100.0),
                        InfXAttributes.ITEM_MELEE_RANGE,
                        ItemReach.BASE_RANGE));
    }

    private static ItemAttributeModifiers modifiersWithMeleeBonus(EquipmentSlotGroup slot) {
        return modifiersWithMeleeBonus(slot, 0.5);
    }

    private static ItemAttributeModifiers modifiersWithMeleeBonus(EquipmentSlotGroup slot, double amount) {
        return ItemAttributeModifiers.builder()
                .add(
                        InfXAttributes.ITEM_MELEE_RANGE,
                        new AttributeModifier(
                                InfiniteX.id("test_melee_range"),
                                amount,
                                AttributeModifier.Operation.ADD_VALUE),
                        slot)
                .build();
    }
}
