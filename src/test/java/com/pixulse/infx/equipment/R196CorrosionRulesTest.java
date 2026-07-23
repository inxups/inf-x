package com.pixulse.infx.equipment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.material.R196Material;
import org.junit.jupiter.api.Test;

class R196CorrosionRulesTest {
    @Test
    void pepsinOnlyHarmsLeatherEquipmentMaterial() {
        assertTrue(R196CorrosionRules.isHarmedBy(R196Material.LEATHER, R196CorrosionType.PEPSIN));
        assertFalse(R196CorrosionRules.isHarmedBy(R196Material.WOOD, R196CorrosionType.PEPSIN));
        assertFalse(R196CorrosionRules.isHarmedBy(R196Material.IRON, R196CorrosionType.PEPSIN));
    }

    @Test
    void acidRespectsMiteMaterialExceptions() {
        assertFalse(R196CorrosionRules.isHarmedBy(R196Material.FLINT, R196CorrosionType.ACID));
        assertFalse(R196CorrosionRules.isHarmedBy(R196Material.OBSIDIAN, R196CorrosionType.ACID));
        assertFalse(R196CorrosionRules.isHarmedBy(R196Material.GOLD, R196CorrosionType.ACID));
        assertFalse(R196CorrosionRules.isHarmedBy(R196Material.MITHRIL, R196CorrosionType.ACID));
        assertTrue(R196CorrosionRules.isHarmedBy(R196Material.IRON, R196CorrosionType.ACID));
        assertTrue(R196CorrosionRules.isHarmedBy(R196Material.ADAMANTIUM, R196CorrosionType.ACID));
    }
}
