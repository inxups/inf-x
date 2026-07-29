package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.item.material.MiteMaterial;
import net.minecraft.world.entity.EntitySpawnReason;
import org.junit.jupiter.api.Test;

class MonsterTacticsTest {
    @Test
    void worldAgeRaisesEquipmentTierAndEnchantChance() {
        assertEquals(MiteMaterial.COPPER, MonsterTactics.maximumGearMaterial(1));
        assertEquals(MiteMaterial.IRON, MonsterTactics.maximumGearMaterial(32));
        assertEquals(MiteMaterial.ANCIENT_METAL, MonsterTactics.maximumGearMaterial(64));
        assertEquals(MiteMaterial.MITHRIL, MonsterTactics.maximumGearMaterial(128));
        assertEquals(MiteMaterial.ADAMANTIUM, MonsterTactics.maximumGearMaterial(256));
        assertTrue(MonsterTactics.equipmentChance(256) > MonsterTactics.equipmentChance(1));
        assertTrue(MonsterTactics.enchantmentChance(256) > MonsterTactics.enchantmentChance(16));
    }

    @Test
    void ordinarySpawnersBurnOutAfterFifteenPlayerAttributedDeaths() {
        assertFalse(SpawnerBurnout.isExhausted(SpawnerBurnout.KILL_LIMIT - 1));
        assertTrue(SpawnerBurnout.isExhausted(SpawnerBurnout.KILL_LIMIT));
        assertEquals(1_100L, SpawnerBurnout.playerDamageExpiresAt(1_000L));
        assertTrue(SpawnerBurnout.hasActivePlayerDamageCredit(1_100L, 1_100L));
        assertFalse(SpawnerBurnout.hasActivePlayerDamageCredit(1_101L, 1_100L));
    }

    @Test
    void ordinarySpawnersBypassTorchLightButKeepMiteSunlightAndPlacementGates() {
        assertTrue(MonsterTactics.allowsMiteSpawnerLightBypass(
                EntitySpawnReason.SPAWNER, false, true, false));
        assertFalse(MonsterTactics.allowsMiteSpawnerLightBypass(
                EntitySpawnReason.SPAWNER, false, true, true));
        assertFalse(MonsterTactics.allowsMiteSpawnerLightBypass(
                EntitySpawnReason.SPAWNER, false, false, false));
        assertFalse(MonsterTactics.allowsMiteSpawnerLightBypass(
                EntitySpawnReason.TRIAL_SPAWNER, false, true, false));
    }

    @Test
    void cooperationAssignsStableDistinctFlanks() {
        assertEquals(4.0, MonsterTactics.flankOffset(0, 4.0).x, 1.0E-6);
        assertEquals(4.0, MonsterTactics.flankOffset(2, 4.0).z, 1.0E-6);
    }
}
