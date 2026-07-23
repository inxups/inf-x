package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.enchantment.R196EnchantmentRules;
import com.pixulse.infx.tag.ModTags;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.RemoveBinomial;

/** The 22 R196 enchantments plus the crafting-only clumsiness curse. */
public final class ModEnchantments {
    public static final ResourceKey<Enchantment> DURABILITY = key("durability");
    public static final ResourceKey<Enchantment> DISARMING = key("disarming");
    public static final ResourceKey<Enchantment> QUICKNESS = key("quickness");
    public static final ResourceKey<Enchantment> PRECISION = key("precision");
    public static final ResourceKey<Enchantment> POISONING = key("poisoning");
    public static final ResourceKey<Enchantment> BUTCHERING = key("butchering");
    public static final ResourceKey<Enchantment> STUNNING = key("stunning");
    public static final ResourceKey<Enchantment> VAMPIRISM = key("vampirism");
    public static final ResourceKey<Enchantment> RECOVERY = key("recovery");
    public static final ResourceKey<Enchantment> SLAUGHTER = key("slaughter");
    public static final ResourceKey<Enchantment> CLEAVING = key("cleaving");
    public static final ResourceKey<Enchantment> HARVESTING = key("harvesting");
    public static final ResourceKey<Enchantment> PENETRATION = key("penetration");
    public static final ResourceKey<Enchantment> BAITING = key("baiting");
    public static final ResourceKey<Enchantment> FERTILITY = key("fertility");
    public static final ResourceKey<Enchantment> TREE_FELLING = key("tree_felling");
    public static final ResourceKey<Enchantment> FORTUNE = key("fortune");
    public static final ResourceKey<Enchantment> FREE_MOVEMENT = key("free_movement");
    public static final ResourceKey<Enchantment> REGENERATION = key("regeneration");
    public static final ResourceKey<Enchantment> SPEED = key("speed");
    public static final ResourceKey<Enchantment> ENDURANCE = key("endurance");
    public static final ResourceKey<Enchantment> PROTECTION = key("protection");
    public static final ResourceKey<Enchantment> CLUMSINESS = key("clumsiness");

    public static final List<ResourceKey<Enchantment>> R196 = List.of(
            DURABILITY, DISARMING, QUICKNESS, PRECISION, POISONING, BUTCHERING, STUNNING,
            VAMPIRISM, RECOVERY, SLAUGHTER, CLEAVING, HARVESTING, PENETRATION, BAITING,
            FERTILITY, TREE_FELLING, FORTUNE, FREE_MOVEMENT, REGENERATION, SPEED,
            ENDURANCE, PROTECTION);

    private static final Map<ResourceKey<Enchantment>, R196EnchantmentProfile> R196_PROFILES = Map.ofEntries(
            Map.entry(DURABILITY, profile(R196Rarity.UNCOMMON, 10)),
            Map.entry(DISARMING, profile(R196Rarity.RARE, 10)),
            Map.entry(QUICKNESS, profile(R196Rarity.UNCOMMON, 10)),
            Map.entry(PRECISION, profile(R196Rarity.COMMON, 10)),
            Map.entry(POISONING, profile(R196Rarity.RARE, 10)),
            Map.entry(BUTCHERING, profile(R196Rarity.UNCOMMON, 10)),
            Map.entry(STUNNING, profile(R196Rarity.UNCOMMON, 15)),
            Map.entry(VAMPIRISM, profile(R196Rarity.EPIC, 20)),
            Map.entry(RECOVERY, profile(R196Rarity.UNCOMMON, 10)),
            Map.entry(SLAUGHTER, profile(R196Rarity.COMMON, 10)),
            Map.entry(CLEAVING, profile(R196Rarity.UNCOMMON, 10)),
            Map.entry(HARVESTING, profile(R196Rarity.UNCOMMON, 10)),
            Map.entry(PENETRATION, profile(R196Rarity.RARE, 10)),
            Map.entry(BAITING, profile(R196Rarity.COMMON, 10)),
            Map.entry(FERTILITY, profile(R196Rarity.UNCOMMON, 10)),
            Map.entry(TREE_FELLING, profile(R196Rarity.UNCOMMON, 10)),
            Map.entry(FORTUNE, profile(R196Rarity.RARE, 10)),
            Map.entry(FREE_MOVEMENT, profile(R196Rarity.UNCOMMON, 10)),
            Map.entry(REGENERATION, profile(R196Rarity.RARE, 20)),
            Map.entry(SPEED, profile(R196Rarity.RARE, 10)),
            Map.entry(ENDURANCE, profile(R196Rarity.UNCOMMON, 10)),
            Map.entry(PROTECTION, profile(R196Rarity.COMMON, 10)));

    private ModEnchantments() {}

    private static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, InfiniteX.id(path));
    }

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderGetter<Item> items = context.lookup(Registries.ITEM);
        HolderGetter<Enchantment> enchantments = context.lookup(Registries.ENCHANTMENT);
        register(context, items, enchantments, DURABILITY, ModTags.Items.R196_DURABILITY_ENCHANTABLE,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.ANY);
        register(context, items, enchantments, DISARMING, ModTags.Items.R196_DISARMING_ENCHANTABLE,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, QUICKNESS, ItemTags.BOW_ENCHANTABLE,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, PRECISION, ItemTags.BOW_ENCHANTABLE,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, POISONING, ItemTags.BOW_ENCHANTABLE,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, BUTCHERING, ModTags.Items.R196_BUTCHERING_ENCHANTABLE,
                R196EnchantmentRules.BUTCHERING_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, STUNNING, ModTags.Items.R196_STUNNING_ENCHANTABLE,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, VAMPIRISM, ModTags.Items.R196_VAMPIRISM_ENCHANTABLE,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, RECOVERY, ItemTags.BOW_ENCHANTABLE,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, SLAUGHTER, ModTags.Items.R196_SLAUGHTER_ENCHANTABLE,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, CLEAVING, ModTags.Items.R196_CLEAVING_ENCHANTABLE,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, HARVESTING, ModTags.Items.R196_HARVESTING_ENCHANTABLE,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, PENETRATION, ModTags.Items.R196_PENETRATION_ENCHANTABLE,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, BAITING, ItemTags.FISHING_ENCHANTABLE,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, FERTILITY, ModTags.Items.R196_FERTILITY_ENCHANTABLE,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, TREE_FELLING, ModTags.Items.R196_TREE_FELLING_ENCHANTABLE,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, FORTUNE, ModTags.Items.R196_FORTUNE_ENCHANTABLE,
                R196EnchantmentRules.FORTUNE_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, FREE_MOVEMENT, ModTags.Items.R196_FREE_MOVEMENT_ENCHANTABLE,
                R196EnchantmentRules.FREE_MOVEMENT_MAX_LEVEL, EquipmentSlotGroup.LEGS);
        register(context, items, enchantments, REGENERATION, ModTags.Items.R196_CHEST_ARMOR_ENCHANTABLE,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.CHEST);
        register(context, items, enchantments, SPEED, ItemTags.FOOT_ARMOR_ENCHANTABLE,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.FEET);
        register(context, items, enchantments, ENDURANCE, ModTags.Items.R196_CHEST_ARMOR_ENCHANTABLE,
                R196EnchantmentRules.ENDURANCE_MAX_LEVEL, EquipmentSlotGroup.CHEST);
        register(context, items, enchantments, PROTECTION, ItemTags.ARMOR_ENCHANTABLE,
                R196EnchantmentRules.PROTECTION_MAX_LEVEL, EquipmentSlotGroup.ARMOR);
        registerCraftingCurse(context, items, enchantments);
    }

    public static R196EnchantmentProfile profile(ResourceKey<Enchantment> key) {
        R196EnchantmentProfile profile = R196_PROFILES.get(key);
        if (profile == null) {
            throw new IllegalArgumentException("Not an R196 enchantment: " + key.identifier());
        }
        return profile;
    }

    private static R196EnchantmentProfile profile(R196Rarity rarity, int difficulty) {
        return new R196EnchantmentProfile(rarity, difficulty);
    }

    private static void register(
            BootstrapContext<Enchantment> context,
            HolderGetter<Item> items,
            HolderGetter<Enchantment> enchantments,
            ResourceKey<Enchantment> key,
            TagKey<Item> supported,
            int maximumLevel,
            EquipmentSlotGroup slots) {
        HolderSet<Item> supportedItems = items.getOrThrow(supported);
        R196EnchantmentProfile profile = profile(key);
        Enchantment.Builder builder = Enchantment.enchantment(Enchantment.definition(
                supportedItems,
                profile.weight(),
                maximumLevel,
                Enchantment.dynamicCost(profile.minimumCost(1), profile.difficulty()),
                Enchantment.dynamicCost(profile.maximumCost(1), profile.difficulty()),
                8,
                slots)).exclusiveWith(HolderSet.direct(enchantments.getOrThrow(key)));
        if (key.equals(DURABILITY)) {
            builder.withEffect(
                    EnchantmentEffectComponents.ITEM_DAMAGE,
                    new RemoveBinomial(LevelBasedValue.perLevel(0.15F)));
        }
        context.register(key, builder.build(key.identifier()));
    }

    private static void registerCraftingCurse(
            BootstrapContext<Enchantment> context,
            HolderGetter<Item> items,
            HolderGetter<Enchantment> enchantments) {
        Enchantment.Builder builder = Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.DURABILITY_ENCHANTABLE),
                1,
                1,
                Enchantment.dynamicCost(25, 25),
                Enchantment.dynamicCost(45, 25),
                8,
                EquipmentSlotGroup.ANY)).exclusiveWith(HolderSet.direct(enchantments.getOrThrow(CLUMSINESS)));
        context.register(CLUMSINESS, builder.build(CLUMSINESS.identifier()));
    }

    public enum R196Rarity {
        COMMON(100),
        UNCOMMON(25),
        RARE(5),
        EPIC(1);

        private final int weight;

        R196Rarity(int weight) {
            this.weight = weight;
        }

        public int weight() {
            return weight;
        }
    }

    public record R196EnchantmentProfile(R196Rarity rarity, int difficulty) {
        public R196EnchantmentProfile {
            if (difficulty <= 0) {
                throw new IllegalArgumentException("Difficulty must be positive");
            }
        }

        public int weight() {
            return rarity.weight();
        }

        public int minimumCost(int level) {
            if (level < 1) {
                throw new IllegalArgumentException("Level must be positive");
            }
            return Math.max(difficulty - 10, 0) + difficulty * (level - 1) + 1;
        }

        public int maximumCost(int level) {
            return minimumCost(level) + difficulty - 1;
        }
    }
}
