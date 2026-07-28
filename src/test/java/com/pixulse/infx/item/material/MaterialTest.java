package com.pixulse.infx.item.material;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pixulse.infx.data.harvest.HarvestTier;
import com.pixulse.infx.data.harvest.MiteMiningRules;
import java.util.List;
import java.util.Set;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.junit.jupiter.api.Test;

class MaterialTest {
    private record Expected(
            MiteMaterial material,
            String path,
            String englishNoun,
            String englishEquipmentPrefix,
            String chinesePrefix,
            float durability,
            int enchantability,
            Quality quality,
            float damage,
            float efficiency,
            HarvestTier tier,
            TagKey<Block> incorrectForDrops,
            String repairMaterialPath,
            float plateProtection,
            float horseProtection,
            Set<MiteMaterial.Flag> flags) {}

    private static final List<Expected> EXPECTED = List.of(
            new Expected(MiteMaterial.LEATHER, "leather", "Leather", "Leather", "皮革", 1, 10, Quality.FINE, 0,
                    0, null, BlockTags.INCORRECT_FOR_WOODEN_TOOL, "leather", 2, 0, Set.of()),
            new Expected(MiteMaterial.WOOD, "wood", "Wood", "Wooden", "木", .5F, 10, Quality.FINE, 0, 1, null,
                    BlockTags.INCORRECT_FOR_WOODEN_TOOL, "wood", 0, 0, Set.of()),
            new Expected(MiteMaterial.FLINT, "flint", "Flint", "Flint", "燧石", 1, 0, Quality.FINE, 1, 1.25F,
                    HarvestTier.FLINT, BlockTags.INCORRECT_FOR_WOODEN_TOOL, "flint", 0, 0,
                    Set.of(MiteMaterial.Flag.ROCKY)),
            new Expected(MiteMaterial.OBSIDIAN, "obsidian", "Obsidian", "Obsidian", "黑曜石", 2, 0, Quality.FINE,
                    2, 1.5F, HarvestTier.COPPER, BlockTags.INCORRECT_FOR_COPPER_TOOL, "obsidian", 0, 0,
                    Set.of(MiteMaterial.Flag.ROCKY)),
            new Expected(MiteMaterial.GOLD, "gold", "Gold", "Golden", "金", 4, 50, Quality.SUPERB, 2, 1.75F,
                    HarvestTier.COPPER, BlockTags.INCORRECT_FOR_COPPER_TOOL, "gold", 6, 3,
                    Set.of(MiteMaterial.Flag.METAL)),
            new Expected(MiteMaterial.COPPER, "copper", "Copper", "Copper", "铜", 4, 30, Quality.EXCELLENT, 3,
                    1.75F, HarvestTier.COPPER, BlockTags.INCORRECT_FOR_COPPER_TOOL, "copper", 7, 4,
                    Set.of(MiteMaterial.Flag.METAL)),
            new Expected(MiteMaterial.SILVER, "silver", "Silver", "Silver", "银", 4, 30, Quality.EXCELLENT, 3,
                    1.75F, HarvestTier.COPPER, BlockTags.INCORRECT_FOR_COPPER_TOOL, "silver", 7, 4,
                    Set.of(MiteMaterial.Flag.METAL, MiteMaterial.Flag.SILVER)),
            new Expected(MiteMaterial.RUSTED_IRON, "rusted_iron", "Rusted Iron", "Rusted Iron", "锈铁", 4, 0,
                    Quality.POOR, 2, 1.25F, HarvestTier.COPPER, BlockTags.INCORRECT_FOR_COPPER_TOOL, "iron", 6, 0,
                    Set.of(MiteMaterial.Flag.METAL, MiteMaterial.Flag.RUSTED)),
            new Expected(MiteMaterial.IRON, "iron", "Iron", "Iron", "铁", 8, 30, Quality.MASTERWORK, 4, 2,
                    HarvestTier.IRON, BlockTags.INCORRECT_FOR_IRON_TOOL, "iron", 8, 5,
                    Set.of(MiteMaterial.Flag.METAL)),
            new Expected(MiteMaterial.ANCIENT_METAL, "ancient_metal", "Ancient Metal", "Ancient Metal", "远古金属",
                    16, 40, Quality.MASTERWORK, 4, 2, HarvestTier.ANCIENT_METAL,
                    BlockTags.INCORRECT_FOR_NETHERITE_TOOL, "ancient_metal", 8, 5, Set.of(MiteMaterial.Flag.METAL)),
            new Expected(MiteMaterial.MITHRIL, "mithril", "Mithril", "Mithril", "秘银", 64, 100,
                    Quality.LEGENDARY, 5, 2.5F, HarvestTier.MITHRIL, BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
                    "mithril", 9, 6, Set.of(MiteMaterial.Flag.METAL)),
            new Expected(MiteMaterial.ADAMANTIUM, "adamantium", "Adamantium", "Adamantium", "艾德曼", 256, 40,
                    Quality.LEGENDARY, 6, 3, HarvestTier.ADAMANTIUM, BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
                    "adamantium", 10, 7, Set.of(MiteMaterial.Flag.METAL, MiteMaterial.Flag.LAVA_SAFE)));

    @Test
    void qualityOrderMatchesR196() {
        assertEquals(List.of(
                Quality.WRETCHED,
                Quality.POOR,
                Quality.FINE,
                Quality.EXCELLENT,
                Quality.SUPERB,
                Quality.MASTERWORK,
                Quality.LEGENDARY), List.of(Quality.values()));
    }

    @Test
    void profilesMatchR196() {
        assertEquals(List.of(
                MiteMaterial.LEATHER,
                MiteMaterial.WOOD,
                MiteMaterial.FLINT,
                MiteMaterial.OBSIDIAN,
                MiteMaterial.GOLD,
                MiteMaterial.COPPER,
                MiteMaterial.SILVER,
                MiteMaterial.RUSTED_IRON,
                MiteMaterial.IRON,
                MiteMaterial.ANCIENT_METAL,
                MiteMaterial.MITHRIL,
                MiteMaterial.ADAMANTIUM), List.of(MiteMaterial.values()));
        for (Expected expected : EXPECTED) {
            MiteMaterial actual = expected.material();
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
        assertEquals(400, MiteMaterial.FLINT.toolDurability(1));
        assertEquals(9_600, MiteMaterial.IRON.toolDurability(3));
        assertEquals(512_000, MiteMaterial.ADAMANTIUM.toolDurability(5));
        assertEquals(2.5F, MiteMaterial.FLINT.miningSpeed(.5F));
        assertEquals(8.0F, MiteMaterial.IRON.miningSpeed(1.0F));
        assertEquals(12.0F, MiteMaterial.ADAMANTIUM.miningSpeed(1.0F));
        assertEquals(3.0F, MiteMaterial.FLINT.meleeDamage(2.0F));
        assertEquals(6.0F, MiteMaterial.IRON.meleeDamage(2.0F));
        assertEquals(8.0F, MiteMaterial.ADAMANTIUM.meleeDamage(2.0F));
        assertEquals(0, MiteMiningRules.harvestLevel(MiteMaterial.WOOD));
        assertEquals(1, MiteMiningRules.harvestLevel(MiteMaterial.FLINT));
        assertEquals(2, MiteMiningRules.harvestLevel(MiteMaterial.OBSIDIAN));
        assertEquals(3, MiteMiningRules.harvestLevel(MiteMaterial.IRON));
        assertEquals(3, MiteMiningRules.harvestLevel(MiteMaterial.ANCIENT_METAL));
        assertEquals(4, MiteMiningRules.harvestLevel(MiteMaterial.MITHRIL));
        assertEquals(5, MiteMiningRules.harvestLevel(MiteMaterial.ADAMANTIUM));
    }

    @Test
    void flagsAreImmutable() {
        assertThrows(UnsupportedOperationException.class,
                () -> MiteMaterial.SILVER.flags().add(MiteMaterial.Flag.ROCKY));
    }
}
