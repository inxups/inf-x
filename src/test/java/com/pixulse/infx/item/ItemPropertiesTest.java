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
    void toolReachBonusStillDifferentiatesTheEntityInteractionRange() {
        // INFX tools carry no ATTACK_RANGE component, so melee reach follows the shared
        // component-less 1.5-block rule; the per-type reach bonus keeps differentiating
        // the entity interaction range instead.
        var sword = ItemProperties.toolAttributes(key(InfxMaterial.IRON, EquipmentType.SWORD));
        var scythe = ItemProperties.toolAttributes(key(InfxMaterial.COPPER, EquipmentType.SCYTHE));
        var knife = ItemProperties.toolAttributes(key(InfxMaterial.FLINT, EquipmentType.KNIFE));
        assertEquals(
                2.25,
                sword.compute(Attributes.ENTITY_INTERACTION_RANGE, 1.5, EquipmentSlot.MAINHAND),
                1.0E-6);
        assertEquals(
                2.5,
                scythe.compute(Attributes.ENTITY_INTERACTION_RANGE, 1.5, EquipmentSlot.MAINHAND),
                1.0E-6);
        assertEquals(
                1.75,
                knife.compute(Attributes.ENTITY_INTERACTION_RANGE, 1.5, EquipmentSlot.MAINHAND),
                1.0E-6);
    }

}
