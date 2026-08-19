package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class MonsterTacticsTest {
    @Test
    void tensionRaisesEquipmentTierAndEnchantChance() {
        assertEquals(InfxMaterial.COPPER, MonsterTactics.maximumGearMaterial(0.0F));
        assertEquals(InfxMaterial.IRON, MonsterTactics.maximumGearMaterial(0.2F));
        assertEquals(InfxMaterial.ANCIENT_METAL, MonsterTactics.maximumGearMaterial(0.4F));
        assertEquals(InfxMaterial.MITHRIL, MonsterTactics.maximumGearMaterial(0.6F));
        assertEquals(InfxMaterial.ADAMANTIUM, MonsterTactics.maximumGearMaterial(0.8F));
        assertTrue(MonsterTactics.equipmentChance(1.5F) > MonsterTactics.equipmentChance(0.1F));
        assertTrue(MonsterTactics.enchantmentChance(1.5F) > MonsterTactics.enchantmentChance(0.1F));
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

    @Test
    void frenzyDamageBonusStacksPerSource() {
        assertEquals(0.0F, MonsterEvents.frenzyDamageBonus(false, false));
        assertEquals(0.5F, MonsterEvents.frenzyDamageBonus(true, false));
        assertEquals(0.5F, MonsterEvents.frenzyDamageBonus(false, true));
        assertEquals(1.0F, MonsterEvents.frenzyDamageBonus(true, true));
    }

    @Test
    void zombieRareDropsMatchMite() {
        assertEquals(0.025F, InfxZombie.rareDropChance(0), 1.0E-6F);
        assertEquals(0.035F, InfxZombie.rareDropChance(1), 1.0E-6F);
        assertEquals(0.055F, InfxZombie.rareDropChance(3), 1.0E-6F);
        for (int i = 0; i < 32; i++) {
            var nugget = InfxZombie.randomNugget(RandomSource.create(i));
            assertTrue(
                    nugget == Items.COPPER_NUGGET
                            || nugget == Items.GOLD_NUGGET
                            || nugget == Items.IRON_NUGGET
                            || nugget == InfXItems.SILVER_NUGGET.get(),
                    "zombie rare drops must be one of the four metal nuggets");
        }
    }
}
