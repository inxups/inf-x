package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.item.material.InfxMaterial;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.junit.jupiter.api.Test;

class ItemPropertiesTest {
    private static EquipmentKey key(InfxMaterial material, EquipmentType type) {
        return new EquipmentKey(material, type);
    }

    @Test
    void toolAttributesDisplayTheFinalR196Damage() {
        var flintHatchet =
                ItemProperties.toolAttributes(key(InfxMaterial.FLINT, EquipmentType.HATCHET));
        var adamantiumHammer = ItemProperties.toolAttributes(
                key(InfxMaterial.ADAMANTIUM, EquipmentType.WAR_HAMMER));
        assertEquals(3.0, flintHatchet.compute(Attributes.ATTACK_DAMAGE, 1.0, EquipmentSlot.MAINHAND));
        assertEquals(8.0, adamantiumHammer.compute(Attributes.ATTACK_DAMAGE, 1.0, EquipmentSlot.MAINHAND));
        assertEquals(
                .8,
                flintHatchet.compute(Attributes.ATTACK_SPEED, 4.0, EquipmentSlot.MAINHAND),
                1.0E-6);
    }

    @Test
    void fractionalArmorSurvivesComponentConstruction() {
        var helmet =
                ItemProperties.armorAttributes(key(InfxMaterial.COPPER, EquipmentType.HELMET));
        var leggings = ItemProperties.armorAttributes(
                key(InfxMaterial.COPPER, EquipmentType.CHAINMAIL_LEGGINGS));
        assertEquals(
                35.0 / 24.0,
                helmet.compute(Attributes.ARMOR, 0.0, EquipmentSlot.HEAD),
                1.0E-6);
        assertEquals(
                35.0 / 24.0,
                leggings.compute(Attributes.ARMOR, 0.0, EquipmentSlot.LEGS),
                1.0E-6);
    }

    @Test
    void horseArmorUsesBodyAttributes() {
        var armor = ItemProperties.armorAttributes(
                key(InfxMaterial.ADAMANTIUM, EquipmentType.HORSE_ARMOR));
        assertEquals(7.0, armor.compute(Attributes.ARMOR, 0.0, EquipmentSlot.BODY));
    }

    @Test
    void toolAttackRangeStartsAtTheR196MeleeBaselineAndKeepsItsReachBonus() {
        assertEquals(1.5F, ItemProperties.attackRange(EquipmentType.FISHING_ROD).maxReach());
        assertEquals(2.5F, ItemProperties.attackRange(EquipmentType.SCYTHE).maxReach());
        assertEquals(1.75F, ItemProperties.attackRange(EquipmentType.KNIFE).maxReach());
        assertEquals(5.0F, ItemProperties.attackRange(EquipmentType.FISHING_ROD).maxCreativeReach());
        assertEquals(6.0F, ItemProperties.attackRange(EquipmentType.SCYTHE).maxCreativeReach());
        assertEquals(0.6F, ItemProperties.attackRange(EquipmentType.SWORD).mobFactor());
        assertEquals(0.6F, ItemProperties.attackRange(EquipmentType.KNIFE).mobFactor());
        assertEquals(0.6F, ItemProperties.attackRange(EquipmentType.SCYTHE).mobFactor());
    }

}
