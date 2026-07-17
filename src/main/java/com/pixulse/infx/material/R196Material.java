package com.pixulse.infx.material;

import com.pixulse.infx.harvest.HarvestTier;
import java.util.Optional;
import java.util.Set;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public enum R196Material {
    LEATHER("leather", "Leather", "Leather", "皮革", 1, 10, R196Quality.FINE, 0, 0, null,
            BlockTags.INCORRECT_FOR_WOODEN_TOOL, "leather", 2, 0),
    WOOD("wood", "Wood", "Wooden", "木", .5F, 10, R196Quality.FINE, 0, 1, null,
            BlockTags.INCORRECT_FOR_WOODEN_TOOL, "wood", 0, 0),
    FLINT("flint", "Flint", "Flint", "燧石", 1, 0, R196Quality.FINE, 1, 1.25F, HarvestTier.FLINT,
            BlockTags.INCORRECT_FOR_WOODEN_TOOL, "flint", 0, 0, Flag.ROCKY),
    OBSIDIAN("obsidian", "Obsidian", "Obsidian", "黑曜石", 2, 0, R196Quality.FINE, 2, 1.5F, HarvestTier.COPPER,
            BlockTags.INCORRECT_FOR_COPPER_TOOL, "obsidian", 0, 0, Flag.ROCKY),
    GOLD("gold", "Gold", "Golden", "金", 4, 50, R196Quality.SUPERB, 2, 1.75F, HarvestTier.COPPER,
            BlockTags.INCORRECT_FOR_COPPER_TOOL, "gold", 6, 3, Flag.METAL),
    COPPER("copper", "Copper", "Copper", "铜", 4, 30, R196Quality.EXCELLENT, 3, 1.75F, HarvestTier.COPPER,
            BlockTags.INCORRECT_FOR_COPPER_TOOL, "copper", 7, 4, Flag.METAL),
    SILVER("silver", "Silver", "Silver", "银", 4, 30, R196Quality.EXCELLENT, 3, 1.75F, HarvestTier.COPPER,
            BlockTags.INCORRECT_FOR_COPPER_TOOL, "silver", 7, 4, Flag.METAL, Flag.SILVER),
    RUSTED_IRON("rusted_iron", "Rusted Iron", "Rusted Iron", "锈铁", 4, 0, R196Quality.POOR, 2, 1.25F,
            HarvestTier.COPPER, BlockTags.INCORRECT_FOR_COPPER_TOOL, "iron", 6, 0, Flag.METAL, Flag.RUSTED),
    IRON("iron", "Iron", "Iron", "铁", 8, 30, R196Quality.MASTERWORK, 4, 2, HarvestTier.IRON,
            BlockTags.INCORRECT_FOR_IRON_TOOL, "iron", 8, 5, Flag.METAL),
    ANCIENT_METAL("ancient_metal", "Ancient Metal", "Ancient Metal", "远古金属", 16, 40, R196Quality.MASTERWORK, 4,
            2, HarvestTier.ANCIENT_METAL, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, "ancient_metal", 8, 5, Flag.METAL),
    MITHRIL("mithril", "Mithril", "Mithril", "秘银", 64, 100, R196Quality.LEGENDARY, 5, 2.5F, HarvestTier.MITHRIL,
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, "mithril", 9, 6, Flag.METAL),
    ADAMANTIUM("adamantium", "Adamantium", "Adamantium", "艾德曼", 256, 40, R196Quality.LEGENDARY, 6, 3,
            HarvestTier.ADAMANTIUM, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, "adamantium", 10, 7, Flag.METAL,
            Flag.LAVA_SAFE);

    private final String path;
    private final String englishNoun;
    private final String englishEquipmentPrefix;
    private final String chinesePrefix;
    private final float durabilityMultiplier;
    private final int enchantability;
    private final R196Quality maximumQuality;
    private final float materialDamage;
    private final float harvestEfficiency;
    private final Optional<HarvestTier> harvestTier;
    private final TagKey<Block> incorrectForDrops;
    private final String repairMaterialPath;
    private final float plateProtection;
    private final float horseProtection;
    private final Set<Flag> flags;

    R196Material(
            String path,
            String englishNoun,
            String englishEquipmentPrefix,
            String chinesePrefix,
            float durabilityMultiplier,
            int enchantability,
            R196Quality maximumQuality,
            float materialDamage,
            float harvestEfficiency,
            HarvestTier harvestTier,
            TagKey<Block> incorrectForDrops,
            String repairMaterialPath,
            float plateProtection,
            float horseProtection,
            Flag... flags) {
        this.path = path;
        this.englishNoun = englishNoun;
        this.englishEquipmentPrefix = englishEquipmentPrefix;
        this.chinesePrefix = chinesePrefix;
        this.durabilityMultiplier = durabilityMultiplier;
        this.enchantability = enchantability;
        this.maximumQuality = maximumQuality;
        this.materialDamage = materialDamage;
        this.harvestEfficiency = harvestEfficiency;
        this.harvestTier = Optional.ofNullable(harvestTier);
        this.incorrectForDrops = incorrectForDrops;
        this.repairMaterialPath = repairMaterialPath;
        this.plateProtection = plateProtection;
        this.horseProtection = horseProtection;
        this.flags = Set.of(flags);
    }

    public String path() { return path; }

    public String englishNoun() { return englishNoun; }

    public String englishEquipmentPrefix() { return englishEquipmentPrefix; }

    public String chinesePrefix() { return chinesePrefix; }

    public float durabilityMultiplier() { return durabilityMultiplier; }

    public int enchantability() { return enchantability; }

    public R196Quality maximumQuality() { return maximumQuality; }

    public float materialDamage() { return materialDamage; }

    public float harvestEfficiency() { return harvestEfficiency; }

    public Optional<HarvestTier> harvestTier() { return harvestTier; }

    public TagKey<Block> incorrectForDrops() { return incorrectForDrops; }

    public String repairMaterialPath() { return repairMaterialPath; }

    public float plateProtection() { return plateProtection; }

    public float horseProtection() { return horseProtection; }

    public boolean has(Flag flag) { return flags.contains(flag); }

    public int toolDurability(int components) {
        return (int) (4.0F * components * durabilityMultiplier * 100.0F);
    }

    public float miningSpeed(float typeMultiplier) {
        return 4.0F * typeMultiplier * harvestEfficiency;
    }

    public float meleeDamage(float typeBaseDamage) {
        return typeBaseDamage + materialDamage;
    }

    public enum Flag {
        METAL,
        ROCKY,
        SILVER,
        RUSTED,
        LAVA_SAFE
    }
}
