package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.item.material.InfxMaterial;
import net.minecraft.world.entity.EntitySpawnReason;
import org.junit.jupiter.api.Test;

class MonsterTacticsTest {
    @Test
    void worldAgeRaisesEquipmentTierAndEnchantChance() {
        assertEquals(InfxMaterial.COPPER, MonsterTactics.maximumGearMaterial(1));
        assertEquals(InfxMaterial.IRON, MonsterTactics.maximumGearMaterial(32));
        assertEquals(InfxMaterial.ANCIENT_METAL, MonsterTactics.maximumGearMaterial(64));
        assertEquals(InfxMaterial.MITHRIL, MonsterTactics.maximumGearMaterial(128));
        assertEquals(InfxMaterial.ADAMANTIUM, MonsterTactics.maximumGearMaterial(256));
        assertTrue(MonsterTactics.equipmentChance(256) > MonsterTactics.equipmentChance(1));
        assertTrue(MonsterTactics.enchantmentChance(256) > MonsterTactics.enchantmentChance(16));
    }

    @Test
    void spawnersStopAtTwentyMatchingMobs() {
        assertFalse(MonsterTactics.spawnerAtCap(19));
        assertTrue(MonsterTactics.spawnerAtCap(20));
    }

    @Test
    void ordinarySpawnersBypassTorchLightButKeepSunlightAndPlacementGates() {
        assertTrue(MonsterTactics.allowsSpawnerLightBypass(
                EntitySpawnReason.SPAWNER, false, true, false));
        assertFalse(MonsterTactics.allowsSpawnerLightBypass(
                EntitySpawnReason.SPAWNER, false, true, true));
        assertFalse(MonsterTactics.allowsSpawnerLightBypass(
                EntitySpawnReason.SPAWNER, false, false, false));
        assertFalse(MonsterTactics.allowsSpawnerLightBypass(
                EntitySpawnReason.TRIAL_SPAWNER, false, true, false));
    }

    @Test
    void cooperationAssignsStableDistinctFlanks() {
        assertEquals(4.0, MonsterTactics.flankOffset(0, 4.0).x, 1.0E-6);
        assertEquals(4.0, MonsterTactics.flankOffset(2, 4.0).z, 1.0E-6);
    }
}
