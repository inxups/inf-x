package com.pixulse.infx.item;

import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.tag.InfXBlockTags;

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
            MiningFamily.PICKAXE, InfxUseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    SHOVEL("shovel", "Shovel", "锹", EquipmentCategory.TOOL, shovelMaterials(),
            1, 1, .75F, 1.0F, -3.0F, .5F, 1.0F,
            MiningFamily.SHOVEL, InfxUseAction.SHOVEL, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    HATCHET("hatchet", "Hatchet", "短斧", EquipmentCategory.TOOL, rockAndMetals(),
            1, 2, .5F, .5F, -2.8F, 4.0F / 3.0F, 4.0F / 3.0F,
            MiningFamily.AXE, InfxUseAction.AXE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    AXE("axe", "Axe", "斧", EquipmentCategory.TOOL, rockAndMetals(),
            3, 3, .75F, 1.0F, -3.1F, 1.0F, 1.0F,
            MiningFamily.AXE, InfxUseAction.AXE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    HOE("hoe", "Hoe", "锄", EquipmentCategory.TOOL, metals(),
            2, 1, .75F, .5F, -1.0F, 2.0F, 2.0F,
            MiningFamily.HOE, InfxUseAction.HOE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    MATTOCK("mattock", "Mattock", "鹤嘴锄", EquipmentCategory.TOOL, metals(),
            4, 1, .75F, .75F, -3.0F, .4F, 1.0F,
            MiningFamily.SHOVEL, InfxUseAction.MATTOCK, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    BATTLE_AXE("battle_axe", "Battle Axe", "战斧", EquipmentCategory.TOOL, metals(),
            4, 4, .75F, .75F, -3.1F, 1.25F, .75F,
            MiningFamily.AXE, InfxUseAction.AXE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    WAR_HAMMER("war_hammer", "War Hammer", "战锤", EquipmentCategory.TOOL, metals(),
            5, 2, .75F, .75F, -3.0F, 2.0F / 3.0F, 2.0F / 3.0F,
            MiningFamily.PICKAXE, InfxUseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    SCYTHE("scythe", "Scythe", "镰刀", EquipmentCategory.TOOL, metals(),
            2, 1, 1.0F, 1.0F, -1.0F, 2.0F, 4.0F,
            MiningFamily.SCYTHE, InfxUseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    SHEARS("shears", "Shears", "剪刀", EquipmentCategory.TOOL, metals(),
            2, 0, .5F, 1.0F, Float.NaN, 1.0F, 2.0F,
            MiningFamily.SHEARS, InfxUseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.SHEARS),
    FISHING_ROD("fishing_rod", "Fishing Rod", "钓鱼竿", EquipmentCategory.TOOL, fishingMaterials(),
            0, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, InfxUseAction.NONE, ModelFamily.FISHING_ROD, ArmorForm.NONE, null,
            FactoryKind.FISHING_ROD),
    CUDGEL("cudgel", "Cudgel", "短木棒", EquipmentCategory.WEAPON, materials(InfxMaterial.WOOD),
            1, 1, .25F, .5F, -1.5F, .25F, .25F,
            MiningFamily.CUDGEL, InfxUseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    CLUB("club", "Club", "木棒", EquipmentCategory.WEAPON, materials(InfxMaterial.WOOD),
            2, 2, .5F, .5F, -2.4F, .25F, .25F,
            MiningFamily.CUDGEL, InfxUseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    KNIFE("knife", "Knife", "小刀", EquipmentCategory.WEAPON,
            materials(InfxMaterial.FLINT, InfxMaterial.OBSIDIAN),
            1, 1, .25F, .5F, -1.0F, 1.0F, .5F,
            MiningFamily.SWORD, InfxUseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    SWORD("sword", "Sword", "剑", EquipmentCategory.WEAPON, metals(),
            2, 4, .75F, .5F, -2.4F, 2.0F, .5F,
            MiningFamily.SWORD, InfxUseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    DAGGER("dagger", "Dagger", "匕首", EquipmentCategory.WEAPON, metals(),
            1, 2, .5F, .5F, -1.5F, 2.0F, .5F,
            MiningFamily.SWORD, InfxUseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    BOW("bow", "Bow", "弓", EquipmentCategory.WEAPON,
            materials(InfxMaterial.WOOD, InfxMaterial.ANCIENT_METAL, InfxMaterial.MITHRIL),
            0, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, InfxUseAction.NONE, ModelFamily.BOW, ArmorForm.NONE, null,
            FactoryKind.BOW),
    ARROW("arrow", "Arrow", "箭", EquipmentCategory.WEAPON, rockAndMetals(),
            0, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, InfxUseAction.NONE, ModelFamily.GENERATED, ArmorForm.NONE, null,
            FactoryKind.ARROW),
    SHIELD("shield", "Shield", "盾", EquipmentCategory.WEAPON, shieldMaterials(),
            2, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, InfxUseAction.NONE, ModelFamily.SHIELD, ArmorForm.NONE, null,
            FactoryKind.SHIELD),
    HELMET("helmet", "Helmet", "头盔", EquipmentCategory.PLATE_ARMOR, plateMaterials(),
            5, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, InfxUseAction.NONE, ModelFamily.GENERATED, ArmorForm.PLATE, ArmorType.HELMET,
            FactoryKind.PLAIN),
    CHESTPLATE("chestplate", "Chestplate", "胸甲", EquipmentCategory.PLATE_ARMOR, plateMaterials(),
            8, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, InfxUseAction.NONE, ModelFamily.GENERATED, ArmorForm.PLATE, ArmorType.CHESTPLATE,
            FactoryKind.PLAIN),
    LEGGINGS("leggings", "Leggings", "护腿", EquipmentCategory.PLATE_ARMOR, plateMaterials(),
            7, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, InfxUseAction.NONE, ModelFamily.GENERATED, ArmorForm.PLATE, ArmorType.LEGGINGS,
            FactoryKind.PLAIN),
    BOOTS("boots", "Boots", "靴子", EquipmentCategory.PLATE_ARMOR, plateMaterials(),
            4, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, InfxUseAction.NONE, ModelFamily.GENERATED, ArmorForm.PLATE, ArmorType.BOOTS,
            FactoryKind.PLAIN),
    CHAINMAIL_HELMET("chainmail_helmet", "Chain Helmet", "锁链头盔", EquipmentCategory.CHAIN_ARMOR, metals(),
            5, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, InfxUseAction.NONE, ModelFamily.GENERATED, ArmorForm.CHAIN, ArmorType.HELMET,
            FactoryKind.PLAIN),
    CHAINMAIL_CHESTPLATE("chainmail_chestplate", "Chain Chestplate", "锁链胸甲",
            EquipmentCategory.CHAIN_ARMOR, metals(),
            8, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, InfxUseAction.NONE, ModelFamily.GENERATED, ArmorForm.CHAIN, ArmorType.CHESTPLATE,
            FactoryKind.PLAIN),
    CHAINMAIL_LEGGINGS("chainmail_leggings", "Chain Leggings", "锁链护腿", EquipmentCategory.CHAIN_ARMOR, metals(),
            7, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, InfxUseAction.NONE, ModelFamily.GENERATED, ArmorForm.CHAIN, ArmorType.LEGGINGS,
            FactoryKind.PLAIN),
    CHAINMAIL_BOOTS("chainmail_boots", "Chain Boots", "锁链靴子", EquipmentCategory.CHAIN_ARMOR, metals(),
            4, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, InfxUseAction.NONE, ModelFamily.GENERATED, ArmorForm.CHAIN, ArmorType.BOOTS,
            FactoryKind.PLAIN),
    HORSE_ARMOR("horse_armor", "Horse Armor", "马铠", EquipmentCategory.HORSE_ARMOR, horseMaterials(),
            0, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            MiningFamily.NONE, InfxUseAction.NONE, ModelFamily.GENERATED, ArmorForm.HORSE, null,
            FactoryKind.PLAIN);

    private final String path;
    private final String englishName;
    private final String chineseSuffix;
    private final EquipmentCategory category;
    private final EnumSet<InfxMaterial> allowedMaterials;
    private final int durabilityComponents;
    private final float baseDamage;
    private final float reachBonus;
    private final float miningMultiplier;
    private final float attackSpeedModifier;
    private final float blockDecay;
    private final float attackDecay;
    private final MiningFamily miningFamily;
    private final InfxUseAction useAction;
    private final ModelFamily modelFamily;
    private final ArmorForm armorForm;
    private final Optional<ArmorType> armorType;
    private final FactoryKind factoryKind;

    EquipmentType(
            String path,
            String englishName,
            String chineseSuffix,
            EquipmentCategory category,
            EnumSet<InfxMaterial> allowedMaterials,
            int durabilityComponents,
            float baseDamage,
            float reachBonus,
            float miningMultiplier,
            float attackSpeedModifier,
            float blockDecay,
            float attackDecay,
            MiningFamily miningFamily,
            InfxUseAction useAction,
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

    public Set<InfxMaterial> allowedMaterials() { return Set.copyOf(allowedMaterials); }

    public boolean allows(InfxMaterial material) { return allowedMaterials.contains(material); }

    public int durabilityComponents() { return durabilityComponents; }

    public float baseDamage() { return baseDamage; }

    public float reachBonus() { return reachBonus; }

    public ReachMode reachMode() {
        return switch (this) {
            case PICKAXE, SHOVEL, HOE, MATTOCK, SHEARS -> ReachMode.INTERACTION;
            case CUDGEL, CLUB, KNIFE, SWORD, DAGGER -> ReachMode.MELEE;
            case HATCHET, AXE, BATTLE_AXE, WAR_HAMMER, SCYTHE -> ReachMode.BOTH;
            case FISHING_ROD,
                    BOW,
                    ARROW,
                    SHIELD,
                    HELMET,
                    CHESTPLATE,
                    LEGGINGS,
                    BOOTS,
                    CHAINMAIL_HELMET,
                    CHAINMAIL_CHESTPLATE,
                    CHAINMAIL_LEGGINGS,
                    CHAINMAIL_BOOTS,
                    HORSE_ARMOR -> ReachMode.NONE;
        };
    }

    public float miningMultiplier() { return miningMultiplier; }

    public float attackSpeedModifier() { return attackSpeedModifier; }

    public boolean hasAttackSpeedModifier() { return !Float.isNaN(attackSpeedModifier); }

    public float blockDecay() { return blockDecay; }

    public float blockDecay(BlockState state) {
        if (this == SCYTHE && state.is(InfXBlockTags.effectiveWith(MiningFamily.SCYTHE))) {
            return .5F;
        }
        if (this == KNIFE && state.is(BlockTags.SWORD_EFFICIENT)) {
            return .5F;
        }
        return blockDecay;
    }

    public float attackDecay() { return attackDecay; }

    public MiningFamily miningFamily() { return miningFamily; }

    public InfxUseAction useAction() { return useAction; }

    public boolean supportsSweepAttack() {
        return this == SCYTHE || this == SWORD;
    }

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

    private static EnumSet<InfxMaterial> materials(InfxMaterial first, InfxMaterial... rest) {
        return EnumSet.of(first, rest);
    }

    private static EnumSet<InfxMaterial> metals() {
        return materials(
                InfxMaterial.COPPER,
                InfxMaterial.SILVER,
                InfxMaterial.GOLD,
                InfxMaterial.RUSTED_IRON,
                InfxMaterial.IRON,
                InfxMaterial.ANCIENT_METAL,
                InfxMaterial.MITHRIL,
                InfxMaterial.ADAMANTIUM);
    }

    private static EnumSet<InfxMaterial> rockAndMetals() {
        EnumSet<InfxMaterial> materials = metals();
        materials.add(InfxMaterial.FLINT);
        materials.add(InfxMaterial.OBSIDIAN);
        return materials;
    }

    private static EnumSet<InfxMaterial> shovelMaterials() {
        EnumSet<InfxMaterial> materials = rockAndMetals();
        materials.add(InfxMaterial.WOOD);
        return materials;
    }

    // Three-tier shield line: wood (vanilla look) → ancient_metal → adamantium.
    private static EnumSet<InfxMaterial> shieldMaterials() {
        return materials(InfxMaterial.WOOD, InfxMaterial.ANCIENT_METAL, InfxMaterial.ADAMANTIUM);
    }

    private static EnumSet<InfxMaterial> fishingMaterials() {
        return materials(
                InfxMaterial.FLINT,
                InfxMaterial.OBSIDIAN,
                InfxMaterial.COPPER,
                InfxMaterial.SILVER,
                InfxMaterial.GOLD,
                InfxMaterial.IRON,
                InfxMaterial.ANCIENT_METAL,
                InfxMaterial.MITHRIL,
                InfxMaterial.ADAMANTIUM);
    }

    private static EnumSet<InfxMaterial> plateMaterials() {
        EnumSet<InfxMaterial> materials = metals();
        materials.add(InfxMaterial.LEATHER);
        return materials;
    }

    private static EnumSet<InfxMaterial> horseMaterials() {
        return materials(
                InfxMaterial.COPPER,
                InfxMaterial.SILVER,
                InfxMaterial.GOLD,
                InfxMaterial.IRON,
                InfxMaterial.ANCIENT_METAL,
                InfxMaterial.MITHRIL,
                InfxMaterial.ADAMANTIUM);
    }

    public enum ModelFamily {
        GENERATED,
        HANDHELD,
        FISHING_ROD,
        BOW,
        SHIELD
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
        PLAIN,
        SHIELD
    }
}
