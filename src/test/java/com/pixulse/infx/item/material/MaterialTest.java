package com.pixulse.infx.item.material;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pixulse.infx.data.harvest.HarvestTier;
import com.pixulse.infx.data.harvest.InfxMiningRules;
import java.util.List;
import java.util.Set;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.junit.jupiter.api.Test;

class MaterialTest {
    private record Expected(
            InfxMaterial material,
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
            Set<InfxMaterial.Flag> flags) {}

    private static final List<Expected> EXPECTED = List.of(
            new Expected(InfxMaterial.LEATHER, "leather", "Leather", "Leather", "皮革", 1, 10, Quality.FINE, 0,
                    0, null, BlockTags.INCORRECT_FOR_WOODEN_TOOL, "leather", 2, 0, Set.of()),
            new Expected(InfxMaterial.WOOD, "wood", "Wood", "Wooden", "木", .5F, 10, Quality.FINE, 0, 1, null,
                    BlockTags.INCORRECT_FOR_WOODEN_TOOL, "wood", 0, 0, Set.of()),
            new Expected(InfxMaterial.FLINT, "flint", "Flint", "Flint", "燧石", 1, 0, Quality.FINE, 1, 1.25F,
                    HarvestTier.FLINT, BlockTags.INCORRECT_FOR_WOODEN_TOOL, "flint", 0, 0,
                    Set.of(InfxMaterial.Flag.ROCKY)),
            new Expected(InfxMaterial.OBSIDIAN, "obsidian", "Obsidian", "Obsidian", "黑曜石", 2, 0, Quality.FINE,
                    2, 1.5F, HarvestTier.COPPER, BlockTags.INCORRECT_FOR_COPPER_TOOL, "obsidian", 0, 0,
                    Set.of(InfxMaterial.Flag.ROCKY)),
            new Expected(InfxMaterial.GOLD, "gold", "Gold", "Golden", "金", 4, 50, Quality.SUPERB, 2, 1.75F,
                    HarvestTier.COPPER, BlockTags.INCORRECT_FOR_COPPER_TOOL, "gold", 6, 3,
                    Set.of(InfxMaterial.Flag.METAL)),
            new Expected(InfxMaterial.COPPER, "copper", "Copper", "Copper", "铜", 4, 30, Quality.EXCELLENT, 3,
                    1.75F, HarvestTier.COPPER, BlockTags.INCORRECT_FOR_COPPER_TOOL, "copper", 7, 4,
                    Set.of(InfxMaterial.Flag.METAL)),
            new Expected(InfxMaterial.SILVER, "silver", "Silver", "Silver", "银", 4, 30, Quality.EXCELLENT, 3,
                    1.75F, HarvestTier.COPPER, BlockTags.INCORRECT_FOR_COPPER_TOOL, "silver", 7, 4,
                    Set.of(InfxMaterial.Flag.METAL, InfxMaterial.Flag.SILVER)),
            new Expected(InfxMaterial.RUSTED_IRON, "rusted_iron", "Rusted Iron", "Rusted Iron", "锈铁", 4, 0,
                    Quality.POOR, 2, 1.25F, HarvestTier.COPPER, BlockTags.INCORRECT_FOR_COPPER_TOOL, "iron", 6, 0,
                    Set.of(InfxMaterial.Flag.METAL, InfxMaterial.Flag.RUSTED)),
            new Expected(InfxMaterial.IRON, "iron", "Iron", "Iron", "铁", 8, 30, Quality.MASTERWORK, 4, 2,
                    HarvestTier.IRON, BlockTags.INCORRECT_FOR_IRON_TOOL, "iron", 8, 5,
                    Set.of(InfxMaterial.Flag.METAL)),
            new Expected(InfxMaterial.ANCIENT_METAL, "ancient_metal", "Ancient Metal", "Ancient Metal", "远古金属",
                    16, 40, Quality.MASTERWORK, 4, 2, HarvestTier.ANCIENT_METAL,
                    BlockTags.INCORRECT_FOR_NETHERITE_TOOL, "ancient_metal", 8, 5, Set.of(InfxMaterial.Flag.METAL)),
            new Expected(InfxMaterial.MITHRIL, "mithril", "Mithril", "Mithril", "秘银", 64, 100,
                    Quality.LEGENDARY, 5, 2.5F, HarvestTier.MITHRIL, BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
                    "mithril", 9, 6, Set.of(InfxMaterial.Flag.METAL)),
            new Expected(InfxMaterial.ADAMANTIUM, "adamantium", "Adamantium", "Adamantium", "艾德曼", 256, 40,
                    Quality.LEGENDARY, 6, 3, HarvestTier.ADAMANTIUM, BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
                    "adamantium", 10, 7, Set.of(InfxMaterial.Flag.METAL, InfxMaterial.Flag.LAVA_SAFE)));

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
                InfxMaterial.LEATHER,
                InfxMaterial.WOOD,
                InfxMaterial.FLINT,
                InfxMaterial.OBSIDIAN,
                InfxMaterial.GOLD,
                InfxMaterial.COPPER,
                InfxMaterial.SILVER,
                InfxMaterial.RUSTED_IRON,
                InfxMaterial.IRON,
                InfxMaterial.ANCIENT_METAL,
                InfxMaterial.MITHRIL,
                InfxMaterial.ADAMANTIUM), List.of(InfxMaterial.values()));
        for (Expected expected : EXPECTED) {
            InfxMaterial actual = expected.material();
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
        assertEquals(400, InfxMaterial.FLINT.toolDurability(1));
        assertEquals(9_600, InfxMaterial.IRON.toolDurability(3));
        assertEquals(512_000, InfxMaterial.ADAMANTIUM.toolDurability(5));
        assertEquals(2.5F, InfxMaterial.FLINT.miningSpeed(.5F));
        assertEquals(8.0F, InfxMaterial.IRON.miningSpeed(1.0F));
        assertEquals(12.0F, InfxMaterial.ADAMANTIUM.miningSpeed(1.0F));
        assertEquals(3.0F, InfxMaterial.FLINT.meleeDamage(2.0F));
        assertEquals(6.0F, InfxMaterial.IRON.meleeDamage(2.0F));
        assertEquals(8.0F, InfxMaterial.ADAMANTIUM.meleeDamage(2.0F));
        assertEquals(0, InfxMiningRules.harvestLevel(InfxMaterial.WOOD));
        assertEquals(1, InfxMiningRules.harvestLevel(InfxMaterial.FLINT));
        assertEquals(2, InfxMiningRules.harvestLevel(InfxMaterial.OBSIDIAN));
        assertEquals(3, InfxMiningRules.harvestLevel(InfxMaterial.IRON));
        assertEquals(3, InfxMiningRules.harvestLevel(InfxMaterial.ANCIENT_METAL));
        assertEquals(4, InfxMiningRules.harvestLevel(InfxMaterial.MITHRIL));
        assertEquals(5, InfxMiningRules.harvestLevel(InfxMaterial.ADAMANTIUM));
    }

    @Test
    void flagsAreImmutable() {
        assertThrows(UnsupportedOperationException.class,
                () -> InfxMaterial.SILVER.flags().add(InfxMaterial.Flag.ROCKY));
    }
}
