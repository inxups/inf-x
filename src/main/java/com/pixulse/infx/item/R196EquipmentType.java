package com.pixulse.infx.item;

import com.pixulse.infx.material.R196Material;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.state.BlockState;

public enum R196EquipmentType {
    PICKAXE("pickaxe", "Pickaxe", "镐", R196EquipmentCategory.TOOL, metals(),
            3, 2, .75F, 1.0F, -2.8F, 1.0F, 1.0F,
            R196MiningFamily.PICKAXE, R196UseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    SHOVEL("shovel", "Shovel", "锹", R196EquipmentCategory.TOOL, shovelMaterials(),
            1, 1, .75F, 1.0F, -3.0F, .5F, 1.0F,
            R196MiningFamily.SHOVEL, R196UseAction.SHOVEL, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    HATCHET("hatchet", "Hatchet", "短斧", R196EquipmentCategory.TOOL, rockAndMetals(),
            1, 2, .5F, .5F, -3.2F, 4.0F / 3.0F, 4.0F / 3.0F,
            R196MiningFamily.AXE, R196UseAction.AXE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    AXE("axe", "Axe", "斧", R196EquipmentCategory.TOOL, rockAndMetals(),
            3, 3, .75F, 1.0F, -3.1F, 1.0F, 1.0F,
            R196MiningFamily.AXE, R196UseAction.AXE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    HOE("hoe", "Hoe", "锄", R196EquipmentCategory.TOOL, metals(),
            2, 1, .75F, .5F, -1.0F, 2.0F, 2.0F,
            R196MiningFamily.HOE, R196UseAction.HOE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    MATTOCK("mattock", "Mattock", "鹤嘴锄", R196EquipmentCategory.TOOL, metals(),
            4, 1, .75F, .75F, -3.0F, .4F, 1.0F,
            R196MiningFamily.SHOVEL, R196UseAction.MATTOCK, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    BATTLE_AXE("battle_axe", "Battle Axe", "战斧", R196EquipmentCategory.TOOL, metals(),
            4, 4, .75F, .75F, -3.1F, 1.25F, .75F,
            R196MiningFamily.AXE, R196UseAction.AXE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    WAR_HAMMER("war_hammer", "War Hammer", "战锤", R196EquipmentCategory.TOOL, metals(),
            5, 2, .75F, .75F, -3.4F, 2.0F / 3.0F, 2.0F / 3.0F,
            R196MiningFamily.PICKAXE, R196UseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    SCYTHE("scythe", "Scythe", "镰刀", R196EquipmentCategory.TOOL, metals(),
            2, 1, 1.0F, 1.0F, -1.0F, 2.0F, 4.0F,
            R196MiningFamily.SCYTHE, R196UseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    SHEARS("shears", "Shears", "剪刀", R196EquipmentCategory.TOOL, metals(),
            2, 0, .5F, 1.0F, Float.NaN, 1.0F, 2.0F,
            R196MiningFamily.SHEARS, R196UseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.SHEARS),
    FISHING_ROD("fishing_rod", "Fishing Rod", "钓鱼竿", R196EquipmentCategory.TOOL, fishingMaterials(),
            0, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            R196MiningFamily.NONE, R196UseAction.NONE, ModelFamily.FISHING_ROD, ArmorForm.NONE, null,
            FactoryKind.FISHING_ROD),
    CUDGEL("cudgel", "Cudgel", "短木棒", R196EquipmentCategory.WEAPON, materials(R196Material.WOOD),
            1, 1, .25F, .5F, -3.4F, .25F, .25F,
            R196MiningFamily.NONE, R196UseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    CLUB("club", "Club", "木棒", R196EquipmentCategory.WEAPON, materials(R196Material.WOOD),
            2, 2, .5F, .5F, -3.4F, .25F, .25F,
            R196MiningFamily.NONE, R196UseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    KNIFE("knife", "Knife", "小刀", R196EquipmentCategory.WEAPON,
            materials(R196Material.FLINT, R196Material.OBSIDIAN),
            1, 1, .25F, .5F, -2.4F, 1.0F, .5F,
            R196MiningFamily.SWORD, R196UseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    SWORD("sword", "Sword", "剑", R196EquipmentCategory.WEAPON, metals(),
            2, 4, .75F, .5F, -2.4F, 2.0F, .5F,
            R196MiningFamily.SWORD, R196UseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    DAGGER("dagger", "Dagger", "匕首", R196EquipmentCategory.WEAPON, metals(),
            1, 2, .5F, .5F, -2.4F, 2.0F, .5F,
            R196MiningFamily.SWORD, R196UseAction.NONE, ModelFamily.HANDHELD, ArmorForm.NONE, null,
            FactoryKind.ORDINARY),
    BOW("bow", "Bow", "弓", R196EquipmentCategory.WEAPON,
            materials(R196Material.WOOD, R196Material.ANCIENT_METAL, R196Material.MITHRIL),
            0, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            R196MiningFamily.NONE, R196UseAction.NONE, ModelFamily.BOW, ArmorForm.NONE, null,
            FactoryKind.BOW),
    ARROW("arrow", "Arrow", "箭", R196EquipmentCategory.WEAPON, rockAndMetals(),
            0, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            R196MiningFamily.NONE, R196UseAction.NONE, ModelFamily.GENERATED, ArmorForm.NONE, null,
            FactoryKind.ARROW),
    HELMET("helmet", "Helmet", "头盔", R196EquipmentCategory.PLATE_ARMOR, plateMaterials(),
            5, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            R196MiningFamily.NONE, R196UseAction.NONE, ModelFamily.GENERATED, ArmorForm.PLATE, ArmorType.HELMET,
            FactoryKind.PLAIN),
    CHESTPLATE("chestplate", "Chestplate", "胸甲", R196EquipmentCategory.PLATE_ARMOR, plateMaterials(),
            8, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            R196MiningFamily.NONE, R196UseAction.NONE, ModelFamily.GENERATED, ArmorForm.PLATE, ArmorType.CHESTPLATE,
            FactoryKind.PLAIN),
    LEGGINGS("leggings", "Leggings", "护腿", R196EquipmentCategory.PLATE_ARMOR, plateMaterials(),
            7, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            R196MiningFamily.NONE, R196UseAction.NONE, ModelFamily.GENERATED, ArmorForm.PLATE, ArmorType.LEGGINGS,
            FactoryKind.PLAIN),
    BOOTS("boots", "Boots", "靴子", R196EquipmentCategory.PLATE_ARMOR, plateMaterials(),
            4, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            R196MiningFamily.NONE, R196UseAction.NONE, ModelFamily.GENERATED, ArmorForm.PLATE, ArmorType.BOOTS,
            FactoryKind.PLAIN),
    CHAINMAIL_HELMET("chainmail_helmet", "Chain Helmet", "锁链头盔", R196EquipmentCategory.CHAIN_ARMOR, metals(),
            5, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            R196MiningFamily.NONE, R196UseAction.NONE, ModelFamily.GENERATED, ArmorForm.CHAIN, ArmorType.HELMET,
            FactoryKind.PLAIN),
    CHAINMAIL_CHESTPLATE("chainmail_chestplate", "Chain Chestplate", "锁链胸甲",
            R196EquipmentCategory.CHAIN_ARMOR, metals(),
            8, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            R196MiningFamily.NONE, R196UseAction.NONE, ModelFamily.GENERATED, ArmorForm.CHAIN, ArmorType.CHESTPLATE,
            FactoryKind.PLAIN),
    CHAINMAIL_LEGGINGS("chainmail_leggings", "Chain Leggings", "锁链护腿", R196EquipmentCategory.CHAIN_ARMOR, metals(),
            7, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            R196MiningFamily.NONE, R196UseAction.NONE, ModelFamily.GENERATED, ArmorForm.CHAIN, ArmorType.LEGGINGS,
            FactoryKind.PLAIN),
    CHAINMAIL_BOOTS("chainmail_boots", "Chain Boots", "锁链靴子", R196EquipmentCategory.CHAIN_ARMOR, metals(),
            4, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            R196MiningFamily.NONE, R196UseAction.NONE, ModelFamily.GENERATED, ArmorForm.CHAIN, ArmorType.BOOTS,
            FactoryKind.PLAIN),
    HORSE_ARMOR("horse_armor", "Horse Armor", "马铠", R196EquipmentCategory.HORSE_ARMOR, horseMaterials(),
            0, 0, 0.0F, 0.0F, Float.NaN, 0.0F, 0.0F,
            R196MiningFamily.NONE, R196UseAction.NONE, ModelFamily.GENERATED, ArmorForm.HORSE, null,
            FactoryKind.PLAIN);

    private final String path;
    private final String englishName;
    private final String chineseSuffix;
    private final R196EquipmentCategory category;
    private final EnumSet<R196Material> allowedMaterials;
    private final int durabilityComponents;
    private final float baseDamage;
    private final float reachBonus;
    private final float miningMultiplier;
    private final float attackSpeedModifier;
    private final float blockDecay;
    private final float attackDecay;
    private final R196MiningFamily miningFamily;
    private final R196UseAction useAction;
    private final ModelFamily modelFamily;
    private final ArmorForm armorForm;
    private final Optional<ArmorType> armorType;
    private final FactoryKind factoryKind;

    R196EquipmentType(
            String path,
            String englishName,
            String chineseSuffix,
            R196EquipmentCategory category,
            EnumSet<R196Material> allowedMaterials,
            int durabilityComponents,
            float baseDamage,
            float reachBonus,
            float miningMultiplier,
            float attackSpeedModifier,
            float blockDecay,
            float attackDecay,
            R196MiningFamily miningFamily,
            R196UseAction useAction,
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

    public R196EquipmentCategory category() { return category; }

    public Set<R196Material> allowedMaterials() { return Set.copyOf(allowedMaterials); }

    public boolean allows(R196Material material) { return allowedMaterials.contains(material); }

    public int durabilityComponents() { return durabilityComponents; }

    public float baseDamage() { return baseDamage; }

    public float reachBonus() { return reachBonus; }

    public float miningMultiplier() { return miningMultiplier; }

    public float attackSpeedModifier() { return attackSpeedModifier; }

    public boolean hasAttackSpeedModifier() { return !Float.isNaN(attackSpeedModifier); }

    public float blockDecay() { return blockDecay; }

    public float blockDecay(BlockState state) {
        if (this == SCYTHE && state.is(BlockTags.CROPS)) {
            return .5F;
        }
        if (this == KNIFE && state.is(BlockTags.SWORD_EFFICIENT)) {
            return .5F;
        }
        return blockDecay;
    }

    public float attackDecay() { return attackDecay; }

    public R196MiningFamily miningFamily() { return miningFamily; }

    public R196UseAction useAction() { return useAction; }

    public ModelFamily modelFamily() { return modelFamily; }

    public ArmorForm armorForm() { return armorForm; }

    public Optional<ArmorType> armorType() { return armorType; }

    public FactoryKind factoryKind() { return factoryKind; }

    public float disablesBlockingSeconds() {
        return this == HATCHET || this == AXE || this == BATTLE_AXE ? 5.0F : 0.0F;
    }

    public static List<R196EquipmentType> platePieces() {
        return List.of(HELMET, CHESTPLATE, LEGGINGS, BOOTS);
    }

    public static List<R196EquipmentType> chainPieces() {
        return List.of(CHAINMAIL_HELMET, CHAINMAIL_CHESTPLATE, CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS);
    }

    private static EnumSet<R196Material> materials(R196Material first, R196Material... rest) {
        return EnumSet.of(first, rest);
    }

    private static EnumSet<R196Material> metals() {
        return materials(
                R196Material.COPPER,
                R196Material.SILVER,
                R196Material.GOLD,
                R196Material.RUSTED_IRON,
                R196Material.IRON,
                R196Material.ANCIENT_METAL,
                R196Material.MITHRIL,
                R196Material.ADAMANTIUM);
    }

    private static EnumSet<R196Material> rockAndMetals() {
        EnumSet<R196Material> materials = metals();
        materials.add(R196Material.FLINT);
        materials.add(R196Material.OBSIDIAN);
        return materials;
    }

    private static EnumSet<R196Material> shovelMaterials() {
        EnumSet<R196Material> materials = rockAndMetals();
        materials.add(R196Material.WOOD);
        return materials;
    }

    private static EnumSet<R196Material> fishingMaterials() {
        return materials(
                R196Material.FLINT,
                R196Material.OBSIDIAN,
                R196Material.COPPER,
                R196Material.SILVER,
                R196Material.GOLD,
                R196Material.IRON,
                R196Material.ANCIENT_METAL,
                R196Material.MITHRIL,
                R196Material.ADAMANTIUM);
    }

    private static EnumSet<R196Material> plateMaterials() {
        EnumSet<R196Material> materials = metals();
        materials.add(R196Material.LEATHER);
        return materials;
    }

    private static EnumSet<R196Material> horseMaterials() {
        return materials(
                R196Material.COPPER,
                R196Material.SILVER,
                R196Material.GOLD,
                R196Material.IRON,
                R196Material.ANCIENT_METAL,
                R196Material.MITHRIL,
                R196Material.ADAMANTIUM);
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
