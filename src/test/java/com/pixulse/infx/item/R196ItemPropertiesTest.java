package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.material.R196Material;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.junit.jupiter.api.Test;

class R196ItemPropertiesTest {
    private static R196EquipmentKey key(R196Material material, R196EquipmentType type) {
        return new R196EquipmentKey(material, type);
    }

    @Test
    void toolAttributesDisplayTheFinalR196Damage() {
        var flintHatchet =
                R196ItemProperties.toolAttributes(key(R196Material.FLINT, R196EquipmentType.HATCHET));
        var adamantiumHammer = R196ItemProperties.toolAttributes(
                key(R196Material.ADAMANTIUM, R196EquipmentType.WAR_HAMMER));
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
                R196ItemProperties.armorAttributes(key(R196Material.COPPER, R196EquipmentType.HELMET));
        var leggings = R196ItemProperties.armorAttributes(
                key(R196Material.COPPER, R196EquipmentType.CHAINMAIL_LEGGINGS));
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
        var armor = R196ItemProperties.armorAttributes(
                key(R196Material.ADAMANTIUM, R196EquipmentType.HORSE_ARMOR));
        assertEquals(7.0, armor.compute(Attributes.ARMOR, 0.0, EquipmentSlot.BODY));
    }

    @Test
    void toolAttackRangeStartsAtTheR196MeleeBaselineAndKeepsItsReachBonus() {
        assertEquals(1.5F, R196ItemProperties.attackRange(R196EquipmentType.FISHING_ROD).maxReach());
        assertEquals(2.5F, R196ItemProperties.attackRange(R196EquipmentType.SCYTHE).maxReach());
        assertEquals(1.75F, R196ItemProperties.attackRange(R196EquipmentType.KNIFE).maxReach());
    }
}
