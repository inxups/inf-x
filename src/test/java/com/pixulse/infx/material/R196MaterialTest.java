package com.pixulse.infx.material;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.harvest.HarvestTier;
import java.util.List;
import org.junit.jupiter.api.Test;

class R196MaterialTest {
    private record Expected(
            R196Material material,
            String path,
            float durability,
            int enchantability,
            R196Quality quality,
            float damage,
            float efficiency,
            HarvestTier tier,
            float plateProtection,
            float horseProtection) {}

    private static final List<Expected> EXPECTED = List.of(
            new Expected(R196Material.LEATHER, "leather", 1, 10, R196Quality.FINE, 0, 0, null, 2, 0),
            new Expected(R196Material.WOOD, "wood", .5F, 10, R196Quality.FINE, 0, 1, null, 0, 0),
            new Expected(R196Material.FLINT, "flint", 1, 0, R196Quality.FINE, 1, 1.25F, HarvestTier.FLINT, 0, 0),
            new Expected(R196Material.OBSIDIAN, "obsidian", 2, 0, R196Quality.FINE, 2, 1.5F, HarvestTier.COPPER, 0, 0),
            new Expected(R196Material.GOLD, "gold", 4, 50, R196Quality.SUPERB, 2, 1.75F, HarvestTier.COPPER, 6, 3),
            new Expected(R196Material.COPPER, "copper", 4, 30, R196Quality.EXCELLENT, 3, 1.75F, HarvestTier.COPPER, 7, 4),
            new Expected(R196Material.SILVER, "silver", 4, 30, R196Quality.EXCELLENT, 3, 1.75F, HarvestTier.COPPER, 7, 4),
            new Expected(R196Material.RUSTED_IRON, "rusted_iron", 4, 0, R196Quality.POOR, 2, 1.25F, HarvestTier.COPPER, 6, 0),
            new Expected(R196Material.IRON, "iron", 8, 30, R196Quality.MASTERWORK, 4, 2, HarvestTier.IRON, 8, 5),
            new Expected(R196Material.ANCIENT_METAL, "ancient_metal", 16, 40, R196Quality.MASTERWORK, 4, 2, HarvestTier.ANCIENT_METAL, 8, 5),
            new Expected(R196Material.MITHRIL, "mithril", 64, 100, R196Quality.LEGENDARY, 5, 2.5F, HarvestTier.MITHRIL, 9, 6),
            new Expected(R196Material.ADAMANTIUM, "adamantium", 256, 40, R196Quality.LEGENDARY, 6, 3, HarvestTier.ADAMANTIUM, 10, 7));

    @Test
    void profilesMatchR196() {
        assertEquals(12, R196Material.values().length);
        for (Expected expected : EXPECTED) {
            R196Material actual = expected.material();
            assertAll(actual.path(),
                    () -> assertEquals(expected.path(), actual.path()),
                    () -> assertEquals(expected.durability(), actual.durabilityMultiplier()),
                    () -> assertEquals(expected.enchantability(), actual.enchantability()),
                    () -> assertEquals(expected.quality(), actual.maximumQuality()),
                    () -> assertEquals(expected.damage(), actual.materialDamage()),
                    () -> assertEquals(expected.efficiency(), actual.harvestEfficiency()),
                    () -> assertEquals(expected.tier(), actual.harvestTier().orElse(null)),
                    () -> assertEquals(expected.plateProtection(), actual.plateProtection()),
                    () -> assertEquals(expected.horseProtection(), actual.horseProtection()));
        }
    }

    @Test
    void flagsAndRepairPeersAreExplicit() {
        assertTrue(R196Material.FLINT.has(R196Material.Flag.ROCKY));
        assertTrue(R196Material.OBSIDIAN.has(R196Material.Flag.ROCKY));
        assertTrue(R196Material.SILVER.has(R196Material.Flag.SILVER));
        assertTrue(R196Material.RUSTED_IRON.has(R196Material.Flag.RUSTED));
        assertTrue(R196Material.ADAMANTIUM.has(R196Material.Flag.LAVA_SAFE));
        assertTrue(R196Material.COPPER.has(R196Material.Flag.METAL));
        assertFalse(R196Material.WOOD.has(R196Material.Flag.METAL));
        assertEquals("iron", R196Material.RUSTED_IRON.repairMaterialPath());
        assertEquals("flint", R196Material.FLINT.repairMaterialPath());
    }

    @Test
    void materialFormulasCoverLowMiddleAndHighTiers() {
        assertEquals(400, R196Material.FLINT.toolDurability(1));
        assertEquals(9_600, R196Material.IRON.toolDurability(3));
        assertEquals(512_000, R196Material.ADAMANTIUM.toolDurability(5));
        assertEquals(2.5F, R196Material.FLINT.miningSpeed(.5F));
        assertEquals(8.0F, R196Material.IRON.miningSpeed(1.0F));
        assertEquals(12.0F, R196Material.ADAMANTIUM.miningSpeed(1.0F));
        assertEquals(3.0F, R196Material.FLINT.meleeDamage(2.0F));
        assertEquals(6.0F, R196Material.IRON.meleeDamage(2.0F));
        assertEquals(8.0F, R196Material.ADAMANTIUM.meleeDamage(2.0F));
    }
}
