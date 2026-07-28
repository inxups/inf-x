package com.pixulse.infx.item;

import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.registry.tag.ModTags;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.state.BlockState;

public enum EquipmentType {
    PICKAXE("pickaxe", "Pickaxe", "镐", EquipmentCategory.TOOL, metals(),
            3, 2, .75F, 1.0F, -2.8F, 1.0F, 1.0F,
            MiningFamily.PICKAXE, MiteUseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    SHOVEL("shovel", "Shovel", "锹", EquipmentCategory.TOOL, shovelMaterials(),
            1, 1, .75F, 1.0F, -3.0F, .5F, 1.0F,
            MiningFamily.SHOVEL, MiteUseAction.SHOVEL, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    HATCHET("hatchet", "Hatchet", "短斧", EquipmentCategory.TOOL, rockAndMetals(),
            1, 2, .5F, .5F, -3.2F, 4.0F / 3.0F, 4.0F / 3.0F,
            MiningFamily.AXE, MiteUseAction.AXE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    AXE("axe", "Axe", "斧", EquipmentCategory.TOOL, rockAndMetals(),
            3, 3, .75F, 1.0F, -3.1F, 1.0F, 1.0F,
            MiningFamily.AXE, MiteUseAction.AXE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    HOE("hoe", "Hoe", "锄", EquipmentCategory.TOOL, metals(),
            2, 1, .75F, .5F, -1.0F, 2.0F, 2.0F,
            MiningFamily.HOE, MiteUseAction.HOE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    MATTOCK("mattock", "Mattock", "鹤嘴锄", EquipmentCategory.TOOL, metals(),
            4, 1, .75F, .75F, -3.0F, .4F, 1.0F,
            MiningFamily.SHOVEL, MiteUseAction.MATTOCK, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    BATTLE_AXE("battle_axe", "Battle Axe", "战斧", EquipmentCategory.TOOL, metals(),
            4, 4, .75F, .75F, -3.1F, 1.25F, .75F,
            MiningFamily.AXE, MiteUseAction.AXE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    WAR_HAMMER("war_hammer", "War Hammer", "战锤", EquipmentCategory.TOOL, metals(),
            5, 2, .75F, .75F, -3.4F, 2.0F / 3.0F, 2.0F / 3.0F,
            MiningFamily.PICKAXE, MiteUseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    SCYTHE("scythe", "Scythe", "镰刀", EquipmentCategory.TOOL, metals(),
            2, 1, 1.0F, 1.0F, -1.0F, 2.0F, 4.0F,
            MiningFamily.SCYTHE, MiteUseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    SHEARS("shears", "Shears", "剪刀", EquipmentCategory.TOOL, metals(),
            2, 0, .5F, 1.0F, Float.NaN, 1.0F, 2.0F,
            MiningFamily.SHEARS, MiteUseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.SHEARS),
    FISHING_ROD("fishing_rod", "Fishing Rod", "钓鱼竿", EquipmentCategory.TOOL, fishingMaterials(),
            0, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, MiteUseAction.NONE, ModelFamily.FISHING_ROD, ArmorForm.NONE, null,
            FactoryKind.FISHING_ROD),
    CUDGEL("cudgel", "Cudgel", "短木棒", EquipmentCategory.WEAPON, materials(MiteMaterial.WOOD),
            1, 1, .25F, .5F, -3.4F, .25F, .25F,
            MiningFamily.CUDGEL, MiteUseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    CLUB("club", "Club", "木棒", EquipmentCategory.WEAPON, materials(MiteMaterial.WOOD),
            2, 2, .5F, .5F, -3.4F, .25F, .25F,
            MiningFamily.CUDGEL, MiteUseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    KNIFE("knife", "Knife", "小刀", EquipmentCategory.WEAPON,
            materials(MiteMaterial.FLINT, MiteMaterial.OBSIDIAN),
            1, 1, .25F, .5F, -2.4F, 1.0F, .5F,
            MiningFamily.SWORD, MiteUseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    SWORD("sword", "Sword", "剑", EquipmentCategory.WEAPON, metals(),
            2, 4, .75F, .5F, -2.4F, 2.0F, .5F,
            MiningFamily.SWORD, MiteUseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    DAGGER("dagger", "Dagger", "匕首", EquipmentCategory.WEAPON, metals(),
            1, 2, .5F, .5F, -2.4F, 2.0F, .5F,
            MiningFamily.SWORD, MiteUseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    BOW("bow", "Bow", "弓", EquipmentCategory.WEAPON,
            materials(MiteMaterial.WOOD, MiteMaterial.ANCIENT_METAL, MiteMaterial.MITHRIL),
            0, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, MiteUseAction.NONE, ModelFamily.BOW, ArmorForm.NONE, null,
            FactoryKind.BOW),
    ARROW("arrow", "Arrow", "箭", EquipmentCategory.WEAPON, rockAndMetals(),
            0, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, MiteUseAction.NONE, ModelFamily.GENERATED, ArmorForm.NONE, null,
            FactoryKind.ARROW),
    HELMET("helmet", "Helmet", "头盔", EquipmentCategory.PLATE_ARMOR, plateMaterials(),
            5, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, MiteUseAction.NONE, ModelFamily.GENERATED, ArmorForm.PLATE, ArmorType.HELMET,
            FactoryKind.PLAIN),
    CHESTPLATE("chestplate", "Chestplate", "胸甲", EquipmentCategory.PLATE_ARMOR, plateMaterials(),
            8, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, MiteUseAction.NONE, ModelFamily.GENERATED, ArmorForm.PLATE, ArmorType.CHESTPLATE,
            FactoryKind.PLAIN),
    LEGGINGS("leggings", "Leggings", "护腿", EquipmentCategory.PLATE_ARMOR, plateMaterials(),
            7, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, MiteUseAction.NONE, ModelFamily.GENERATED, ArmorForm.PLATE, ArmorType.LEGGINGS,
            FactoryKind.PLAIN),
    BOOTS("boots", "Boots", "靴子", EquipmentCategory.PLATE_ARMOR, plateMaterials(),
            4, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, MiteUseAction.NONE, ModelFamily.GENERATED, ArmorForm.PLATE, ArmorType.BOOTS,
            FactoryKind.PLAIN),
    CHAINMAIL_HELMET("chainmail_helmet", "Chain Helmet", "锁链头盔", EquipmentCategory.CHAIN_ARMOR, metals(),
            5, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, MiteUseAction.NONE, ModelFamily.GENERATED, ArmorForm.CHAIN, ArmorType.HELMET,
            FactoryKind.PLAIN),
    CHAINMAIL_CHESTPLATE("chainmail_chestplate", "Chain Chestplate", "锁链胸甲",
            EquipmentCategory.CHAIN_ARMOR, metals(),
            8, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, MiteUseAction.NONE, ModelFamily.GENERATED, ArmorForm.CHAIN, ArmorType.CHESTPLATE,
            FactoryKind.PLAIN),
    CHAINMAIL_LEGGINGS("chainmail_leggings", "Chain Leggings", "锁链护腿", EquipmentCategory.CHAIN_ARMOR, metals(),
            7, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, MiteUseAction.NONE, ModelFamily.GENERATED, ArmorForm.CHAIN, ArmorType.LEGGINGS,
            FactoryKind.PLAIN),
    CHAINMAIL_BOOTS("chainmail_boots", "Chain Boots", "锁链靴子", EquipmentCategory.CHAIN_ARMOR, metals(),
            4, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, MiteUseAction.NONE, ModelFamily.GENERATED, ArmorForm.CHAIN, ArmorType.BOOTS,
            FactoryKind.PLAIN),
    HORSE_ARMOR("horse_armor", "Horse Armor", "马铠", EquipmentCategory.HORSE_ARMOR, horseMaterials(),
            0, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, MiteUseAction.NONE, ModelFamily.GENERATED, ArmorForm.HORSE, null,
            FactoryKind.PLAIN);

    private final String path;
    private final String englishName;
    private final String chineseSuffix;
    private final EquipmentCategory category;
    private final EnumSet<MiteMaterial> allowedMaterials;
    private final int durabilityComponents;
    private final float baseDamage;
    private final float reachBonus;
    private final float miningMultiplier;
    private final float attackSpeedModifier;
    private final float blockDecay;
    private final float attackDecay;
    private final MiningFamily miningFamily;
    private final MiteUseAction useAction;
    private final ModelFamily modelFamily;
    private final ArmorForm armorForm;
    private final Optional<ArmorType> armorType;
    private final FactoryKind factoryKind;

    EquipmentType(
            String path,
            String englishName,
            String chineseSuffix,
            EquipmentCategory category,
            EnumSet<MiteMaterial> allowedMaterials,
            int durabilityComponents,
            float baseDamage,
            float reachBonus,
            float miningMultiplier,
            float attackSpeedModifier,
            float blockDecay,
            float attackDecay,
            MiningFamily miningFamily,
            MiteUseAction useAction,
            ModelFamily modelFamily,
            ArmorForm armorForm,
            ArmorType armorType,
            FactoryKind factoryKind) {
        this.path = path;
        this.englishName = englishName;
        this.chineseSuffix = chineseSuffix;
        this.category = category;
        this.allowedMaterials = EnumSet.copyOf(allowedMaterials);
        this.durabilityComponents = durabilityComponents;
        this.baseDamage = baseDamage;
        this.reachBonus = reachBonus;
        this.miningMultiplier = miningMultiplier;
        this.attackSpeedModifier = attackSpeedModifier;
        this.blockDecay = blockDecay;
        this.attackDecay = attackDecay;
        this.miningFamily = miningFamily;
        this.useAction = useAction;
        this.modelFamily = modelFamily;
        this.armorForm = armorForm;
        this.armorType = Optional.ofNullable(armorType);
        this.factoryKind = factoryKind;
    }

    public String path() { return path; }

    public String englishName() { return englishName; }

    public String chineseSuffix() { return chineseSuffix; }

    public EquipmentCategory category() { return category; }

    public Set<MiteMaterial> allowedMaterials() { return Set.copyOf(allowedMaterials); }

    public boolean allows(MiteMaterial material) { return allowedMaterials.contains(material); }

    public int durabilityComponents() { return durabilityComponents; }

    public float baseDamage() { return baseDamage; }

    public float reachBonus() { return reachBonus; }

    public float miningMultiplier() { return miningMultiplier; }

    public float attackSpeedModifier() { return attackSpeedModifier; }

    public boolean hasAttackSpeedModifier() { return !Float.isNaN(attackSpeedModifier); }

    public float blockDecay() { return blockDecay; }

    public float blockDecay(BlockState state) {
        if (this == SCYTHE && state.is(ModTags.Blocks.effectiveWith(MiningFamily.SCYTHE))) {
            return .5F;
        }
        if (this == KNIFE && state.is(BlockTags.SWORD_EFFICIENT)) {
            return .5F;
        }
        return blockDecay;
    }

    public float attackDecay() { return attackDecay; }

    public MiningFamily miningFamily() { return miningFamily; }

    public MiteUseAction useAction() { return useAction; }

    public ModelFamily modelFamily() { return modelFamily; }

    public ArmorForm armorForm() { return armorForm; }

    public Optional<ArmorType> armorType() { return armorType; }

    public FactoryKind factoryKind() { return factoryKind; }

    public float disablesBlockingSeconds() {
        return this == HATCHET || this == AXE || this == BATTLE_AXE ? 5.0F : 0.0F;
    }

    public static List<EquipmentType> platePieces() {
        return List.of(HELMET, CHESTPLATE, LEGGINGS, BOOTS);
    }

    public static List<EquipmentType> chainPieces() {
        return List.of(CHAINMAIL_HELMET, CHAINMAIL_CHESTPLATE, CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS);
    }

    private static EnumSet<MiteMaterial> materials(MiteMaterial first, MiteMaterial... rest) {
        return EnumSet.of(first, rest);
    }

    private static EnumSet<MiteMaterial> metals() {
        return materials(
                MiteMaterial.COPPER,
                MiteMaterial.SILVER,
                MiteMaterial.GOLD,
                MiteMaterial.RUSTED_IRON,
                MiteMaterial.IRON,
                MiteMaterial.ANCIENT_METAL,
                MiteMaterial.MITHRIL,
                MiteMaterial.ADAMANTIUM);
    }

    private static EnumSet<MiteMaterial> rockAndMetals() {
        EnumSet<MiteMaterial> materials = metals();
        materials.add(MiteMaterial.FLINT);
        materials.add(MiteMaterial.OBSIDIAN);
        return materials;
    }

    private static EnumSet<MiteMaterial> shovelMaterials() {
        EnumSet<MiteMaterial> materials = rockAndMetals();
        materials.add(MiteMaterial.WOOD);
        return materials;
    }

    private static EnumSet<MiteMaterial> fishingMaterials() {
        return materials(
                MiteMaterial.FLINT,
                MiteMaterial.OBSIDIAN,
                MiteMaterial.COPPER,
                MiteMaterial.SILVER,
                MiteMaterial.GOLD,
                MiteMaterial.IRON,
                MiteMaterial.ANCIENT_METAL,
                MiteMaterial.MITHRIL,
                MiteMaterial.ADAMANTIUM);
    }

    private static EnumSet<MiteMaterial> plateMaterials() {
        EnumSet<MiteMaterial> materials = metals();
        materials.add(MiteMaterial.LEATHER);
        return materials;
    }

    private static EnumSet<MiteMaterial> horseMaterials() {
        return materials(
                MiteMaterial.COPPER,
                MiteMaterial.SILVER,
                MiteMaterial.GOLD,
                MiteMaterial.IRON,
                MiteMaterial.ANCIENT_METAL,
                MiteMaterial.MITHRIL,
                MiteMaterial.ADAMANTIUM);
    }

    public enum ModelFamily {
        GENERATED,
        HANDHELD,
        FISHING_ROD,
        BOW
    }

    public enum ArmorForm {
        NONE,
        PLATE,
        CHAIN,
        HORSE
    }

    public enum FactoryKind {
        ORDINARY,
        SHEARS,
        FISHING_ROD,
        BOW,
        ARROW,
        PLAIN
    }
}
