package com.pixulse.infx.material;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pixulse.infx.harvest.HarvestTier;
import com.pixulse.infx.harvest.MiteMiningRules;
import java.util.List;
import java.util.Set;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.junit.jupiter.api.Test;

class R196MaterialTest {
    private record Expected(
            R196Material material,
            String path,
            String englishNoun,
            String englishEquipmentPrefix,
            String chinesePrefix,
            float durability,
            int enchantability,
            R196Quality quality,
            float damage,
            float efficiency,
            HarvestTier tier,
            TagKey<Block> incorrectForDrops,
            String repairMaterialPath,
            float plateProtection,
            float horseProtection,
            Set<R196Material.Flag> flags) {}

    private static final List<Expected> EXPECTED = List.of(
            new Expected(R196Material.LEATHER, "leather", "Leather", "Leather", "皮革", 1, 10, R196Quality.FINE, 0,
                    0, null, BlockTags.INCORRECT_FOR_WOODEN_TOOL, "leather", 2, 0, Set.of()),
            new Expected(R196Material.WOOD, "wood", "Wood", "Wooden", "木", .5F, 10, R196Quality.FINE, 0, 1, null,
                    BlockTags.INCORRECT_FOR_WOODEN_TOOL, "wood", 0, 0, Set.of()),
            new Expected(R196Material.FLINT, "flint", "Flint", "Flint", "燧石", 1, 0, R196Quality.FINE, 1, 1.25F,
                    HarvestTier.FLINT, BlockTags.INCORRECT_FOR_WOODEN_TOOL, "flint", 0, 0,
                    Set.of(R196Material.Flag.ROCKY)),
            new Expected(R196Material.OBSIDIAN, "obsidian", "Obsidian", "Obsidian", "黑曜石", 2, 0, R196Quality.FINE,
                    2, 1.5F, HarvestTier.COPPER, BlockTags.INCORRECT_FOR_COPPER_TOOL, "obsidian", 0, 0,
                    Set.of(R196Material.Flag.ROCKY)),
            new Expected(R196Material.GOLD, "gold", "Gold", "Golden", "金", 4, 50, R196Quality.SUPERB, 2, 1.75F,
                    HarvestTier.COPPER, BlockTags.INCORRECT_FOR_COPPER_TOOL, "gold", 6, 3,
                    Set.of(R196Material.Flag.METAL)),
            new Expected(R196Material.COPPER, "copper", "Copper", "Copper", "铜", 4, 30, R196Quality.EXCELLENT, 3,
                    1.75F, HarvestTier.COPPER, BlockTags.INCORRECT_FOR_COPPER_TOOL, "copper", 7, 4,
                    Set.of(R196Material.Flag.METAL)),
            new Expected(R196Material.SILVER, "silver", "Silver", "Silver", "银", 4, 30, R196Quality.EXCELLENT, 3,
                    1.75F, HarvestTier.COPPER, BlockTags.INCORRECT_FOR_COPPER_TOOL, "silver", 7, 4,
                    Set.of(R196Material.Flag.METAL, R196Material.Flag.SILVER)),
            new Expected(R196Material.RUSTED_IRON, "rusted_iron", "Rusted Iron", "Rusted Iron", "锈铁", 4, 0,
                    R196Quality.POOR, 2, 1.25F, HarvestTier.COPPER, BlockTags.INCORRECT_FOR_COPPER_TOOL, "iron", 6, 0,
                    Set.of(R196Material.Flag.METAL, R196Material.Flag.RUSTED)),
            new Expected(R196Material.IRON, "iron", "Iron", "Iron", "铁", 8, 30, R196Quality.MASTERWORK, 4, 2,
                    HarvestTier.IRON, BlockTags.INCORRECT_FOR_IRON_TOOL, "iron", 8, 5,
                    Set.of(R196Material.Flag.METAL)),
            new Expected(R196Material.ANCIENT_METAL, "ancient_metal", "Ancient Metal", "Ancient Metal", "远古金属",
                    16, 40, R196Quality.MASTERWORK, 4, 2, HarvestTier.ANCIENT_METAL,
                    BlockTags.INCORRECT_FOR_NETHERITE_TOOL, "ancient_metal", 8, 5, Set.of(R196Material.Flag.METAL)),
            new Expected(R196Material.MITHRIL, "mithril", "Mithril", "Mithril", "秘银", 64, 100,
                    R196Quality.LEGENDARY, 5, 2.5F, HarvestTier.MITHRIL, BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
                    "mithril", 9, 6, Set.of(R196Material.Flag.METAL)),
            new Expected(R196Material.ADAMANTIUM, "adamantium", "Adamantium", "Adamantium", "艾德曼", 256, 40,
                    R196Quality.LEGENDARY, 6, 3, HarvestTier.ADAMANTIUM, BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
                    "adamantium", 10, 7, Set.of(R196Material.Flag.METAL, R196Material.Flag.LAVA_SAFE)));

    @Test
    void qualityOrderMatchesR196() {
        assertEquals(List.of(
                R196Quality.WRETCHED,
                R196Quality.POOR,
                R196Quality.FINE,
                R196Quality.EXCELLENT,
                R196Quality.SUPERB,
                R196Quality.MASTERWORK,
                R196Quality.LEGENDARY), List.of(R196Quality.values()));
    }

    @Test
    void profilesMatchR196() {
        assertEquals(List.of(
                R196Material.LEATHER,
                R196Material.WOOD,
                R196Material.FLINT,
                R196Material.OBSIDIAN,
                R196Material.GOLD,
                R196Material.COPPER,
                R196Material.SILVER,
                R196Material.RUSTED_IRON,
                R196Material.IRON,
                R196Material.ANCIENT_METAL,
                R196Material.MITHRIL,
                R196Material.ADAMANTIUM), List.of(R196Material.values()));
        for (Expected expected : EXPECTED) {
            R196Material actual = expected.material();
            assertAll(actual.path(),
                    () -> assertEquals(expected.path(), actual.path()),
                    () -> assertEquals(expected.englishNoun(), actual.englishNoun()),
                    () -> assertEquals(expected.englishEquipmentPrefix(), actual.englishEquipmentPrefix()),
                    () -> assertEquals(expected.chinesePrefix(), actual.chinesePrefix()),
                    () -> assertEquals(expected.durability(), actual.durabilityMultiplier()),
                    () -> assertEquals(expected.enchantability(), actual.enchantability()),
                    () -> assertEquals(expected.quality(), actual.maximumQuality()),
                    () -> assertEquals(expected.damage(), actual.materialDamage()),
                    () -> assertEquals(expected.efficiency(), actual.harvestEfficiency()),
                    () -> assertEquals(expected.tier(), actual.harvestTier().orElse(null)),
                    () -> assertEquals(expected.incorrectForDrops(), actual.incorrectForDrops()),
                    () -> assertEquals(expected.repairMaterialPath(), actual.repairMaterialPath()),
                    () -> assertEquals(expected.plateProtection(), actual.plateProtection()),
                    () -> assertEquals(expected.horseProtection(), actual.horseProtection()),
                    () -> assertEquals(expected.flags(), actual.flags()));
        }
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
        assertEquals(0, MiteMiningRules.harvestLevel(R196Material.WOOD));
        assertEquals(1, MiteMiningRules.harvestLevel(R196Material.FLINT));
        assertEquals(2, MiteMiningRules.harvestLevel(R196Material.OBSIDIAN));
        assertEquals(3, MiteMiningRules.harvestLevel(R196Material.IRON));
        assertEquals(3, MiteMiningRules.harvestLevel(R196Material.ANCIENT_METAL));
        assertEquals(4, MiteMiningRules.harvestLevel(R196Material.MITHRIL));
        assertEquals(5, MiteMiningRules.harvestLevel(R196Material.ADAMANTIUM));
    }

    @Test
    void flagsAreImmutable() {
        assertThrows(UnsupportedOperationException.class,
                () -> R196Material.SILVER.flags().add(R196Material.Flag.ROCKY));
    }
}
