package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.material.R196Material;
import org.junit.jupiter.api.Test;

class R196MonsterTacticsTest {
    @Test
    void worldAgeRaisesEquipmentTierAndEnchantChance() {
        assertEquals(R196Material.COPPER, R196MonsterTactics.maximumGearMaterial(1));
        assertEquals(R196Material.IRON, R196MonsterTactics.maximumGearMaterial(32));
        assertEquals(R196Material.ANCIENT_METAL, R196MonsterTactics.maximumGearMaterial(64));
        assertEquals(R196Material.MITHRIL, R196MonsterTactics.maximumGearMaterial(128));
        assertEquals(R196Material.ADAMANTIUM, R196MonsterTactics.maximumGearMaterial(256));
        assertTrue(R196MonsterTactics.equipmentChance(256) > R196MonsterTactics.equipmentChance(1));
        assertTrue(R196MonsterTactics.enchantmentChance(256) > R196MonsterTactics.enchantmentChance(16));
    }

    @Test
    void spawnersStopAtTwentyMatchingMobs() {
        assertFalse(R196MonsterTactics.spawnerAtCap(19));
        assertTrue(R196MonsterTactics.spawnerAtCap(20));
    }

    @Test
    void cooperationAssignsStableDistinctFlanks() {
        assertEquals(4.0, R196MonsterTactics.flankOffset(0, 4.0).x, 1.0E-6);
        assertEquals(4.0, R196MonsterTactics.flankOffset(2, 4.0).z, 1.0E-6);
    }
}
