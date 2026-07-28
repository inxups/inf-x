package com.pixulse.infx.item.equipment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.item.material.MiteMaterial;
import org.junit.jupiter.api.Test;

class CorrosionRulesTest {
    @Test
    void pepsinOnlyHarmsLeatherEquipmentMaterial() {
        assertTrue(CorrosionRules.isHarmedBy(MiteMaterial.LEATHER, CorrosionType.PEPSIN));
        assertFalse(CorrosionRules.isHarmedBy(MiteMaterial.WOOD, CorrosionType.PEPSIN));
        assertFalse(CorrosionRules.isHarmedBy(MiteMaterial.IRON, CorrosionType.PEPSIN));
    }

    @Test
    void acidRespectsMiteMaterialExceptions() {
        assertFalse(CorrosionRules.isHarmedBy(MiteMaterial.FLINT, CorrosionType.ACID));
        assertFalse(CorrosionRules.isHarmedBy(MiteMaterial.OBSIDIAN, CorrosionType.ACID));
        assertFalse(CorrosionRules.isHarmedBy(MiteMaterial.GOLD, CorrosionType.ACID));
        assertFalse(CorrosionRules.isHarmedBy(MiteMaterial.MITHRIL, CorrosionType.ACID));
        assertTrue(CorrosionRules.isHarmedBy(MiteMaterial.IRON, CorrosionType.ACID));
        assertTrue(CorrosionRules.isHarmedBy(MiteMaterial.ADAMANTIUM, CorrosionType.ACID));
    }
}
