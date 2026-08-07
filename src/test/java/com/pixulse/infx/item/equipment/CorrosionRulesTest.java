package com.pixulse.infx.item.equipment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.InfxMaterial;
import org.junit.jupiter.api.Test;

class CorrosionRulesTest {
    @Test
    void pepsinOnlyHarmsLeatherEquipmentMaterial() {
        assertTrue(CorrosionRules.isHarmedBy(InfxMaterial.LEATHER, CorrosionType.PEPSIN));
        assertFalse(CorrosionRules.isHarmedBy(InfxMaterial.WOOD, CorrosionType.PEPSIN));
        assertFalse(CorrosionRules.isHarmedBy(InfxMaterial.IRON, CorrosionType.PEPSIN));
    }

    @Test
    void acidRespectsMaterialExceptions() {
        assertFalse(CorrosionRules.isHarmedBy(InfxMaterial.FLINT, CorrosionType.ACID));
        assertFalse(CorrosionRules.isHarmedBy(InfxMaterial.OBSIDIAN, CorrosionType.ACID));
        assertFalse(CorrosionRules.isHarmedBy(InfxMaterial.GOLD, CorrosionType.ACID));
        assertFalse(CorrosionRules.isHarmedBy(InfxMaterial.MITHRIL, CorrosionType.ACID));
        assertFalse(CorrosionRules.isHarmedBy(InfxMaterial.ADAMANTIUM, CorrosionType.ACID));
        assertTrue(CorrosionRules.isHarmedBy(InfxMaterial.IRON, CorrosionType.ACID));
    }

    @Test
    void compositeEquipmentKeepsItsWoodenComponents() {
        assertTrue(CorrosionRules.hasWoodenComponent(EquipmentType.PICKAXE));
        assertTrue(CorrosionRules.hasWoodenComponent(EquipmentType.BOW));
        assertTrue(CorrosionRules.hasWoodenComponent(EquipmentType.ARROW));
        assertFalse(CorrosionRules.hasWoodenComponent(EquipmentType.SHEARS));
        assertFalse(CorrosionRules.hasWoodenComponent(EquipmentType.HELMET));
    }
}
