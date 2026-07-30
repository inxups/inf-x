package com.pixulse.infx.item.equipment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void acidRespectsMiteMaterialExceptions() {
        assertFalse(CorrosionRules.isHarmedBy(InfxMaterial.FLINT, CorrosionType.ACID));
        assertFalse(CorrosionRules.isHarmedBy(InfxMaterial.OBSIDIAN, CorrosionType.ACID));
        assertFalse(CorrosionRules.isHarmedBy(InfxMaterial.GOLD, CorrosionType.ACID));
        assertFalse(CorrosionRules.isHarmedBy(InfxMaterial.MITHRIL, CorrosionType.ACID));
        assertTrue(CorrosionRules.isHarmedBy(InfxMaterial.IRON, CorrosionType.ACID));
        assertTrue(CorrosionRules.isHarmedBy(InfxMaterial.ADAMANTIUM, CorrosionType.ACID));
    }
}
