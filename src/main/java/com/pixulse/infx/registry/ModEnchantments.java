package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.enchantment.R196EnchantmentRules;
import com.pixulse.infx.tag.ModTags;
import java.util.List;
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

    private ModEnchantments() {}

    private static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, InfiniteX.id(path));
    }

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderGetter<Item> items = context.lookup(Registries.ITEM);
        register(context, items, DURABILITY, ModTags.Items.R196_DURABILITY_ENCHANTABLE, 8,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, 5, 8, EquipmentSlotGroup.ANY);
        register(context, items, DISARMING, ModTags.Items.R196_DISARMING_ENCHANTABLE, 3,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, 15, 12, EquipmentSlotGroup.MAINHAND);
        register(context, items, QUICKNESS, ItemTags.BOW_ENCHANTABLE, 5,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, 10, 10, EquipmentSlotGroup.MAINHAND);
        register(context, items, PRECISION, ItemTags.BOW_ENCHANTABLE, 5,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, 10, 10, EquipmentSlotGroup.MAINHAND);
        register(context, items, POISONING, ItemTags.BOW_ENCHANTABLE, 3,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, 18, 12, EquipmentSlotGroup.MAINHAND);
        register(context, items, BUTCHERING, ModTags.Items.R196_BUTCHERING_ENCHANTABLE, 4,
                R196EnchantmentRules.BUTCHERING_MAX_LEVEL, 12, 10, EquipmentSlotGroup.MAINHAND);
        register(context, items, STUNNING, ModTags.Items.R196_STUNNING_ENCHANTABLE, 3,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, 18, 12, EquipmentSlotGroup.MAINHAND);
        register(context, items, VAMPIRISM, ModTags.Items.R196_VAMPIRISM_ENCHANTABLE, 2,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, 25, 15, EquipmentSlotGroup.MAINHAND);
        register(context, items, RECOVERY, ItemTags.BOW_ENCHANTABLE, 5,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, 12, 10, EquipmentSlotGroup.MAINHAND);
        register(context, items, SLAUGHTER, ModTags.Items.R196_SLAUGHTER_ENCHANTABLE, 4,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, 10, 10, EquipmentSlotGroup.MAINHAND);
        register(context, items, CLEAVING, ModTags.Items.R196_CLEAVING_ENCHANTABLE, 4,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, 10, 10, EquipmentSlotGroup.MAINHAND);
        register(context, items, HARVESTING, ModTags.Items.R196_HARVESTING_ENCHANTABLE, 5,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, 8, 10, EquipmentSlotGroup.MAINHAND);
        register(context, items, PENETRATION, ModTags.Items.R196_PENETRATION_ENCHANTABLE, 4,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, 12, 10, EquipmentSlotGroup.MAINHAND);
        register(context, items, BAITING, ItemTags.FISHING_ENCHANTABLE, 5,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, 10, 10, EquipmentSlotGroup.MAINHAND);
        register(context, items, FERTILITY, ModTags.Items.R196_FERTILITY_ENCHANTABLE, 4,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, 12, 10, EquipmentSlotGroup.MAINHAND);
        register(context, items, TREE_FELLING, ModTags.Items.R196_TREE_FELLING_ENCHANTABLE, 3,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, 15, 12, EquipmentSlotGroup.MAINHAND);
        register(context, items, FORTUNE, ModTags.Items.R196_FORTUNE_ENCHANTABLE, 3,
                R196EnchantmentRules.FORTUNE_MAX_LEVEL, 18, 12, EquipmentSlotGroup.MAINHAND);
        register(context, items, FREE_MOVEMENT, ModTags.Items.R196_FREE_MOVEMENT_ENCHANTABLE, 4,
                R196EnchantmentRules.FREE_MOVEMENT_MAX_LEVEL, 12, 12, EquipmentSlotGroup.LEGS);
        register(context, items, REGENERATION, ModTags.Items.R196_CHEST_ARMOR_ENCHANTABLE, 2,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, 24, 16, EquipmentSlotGroup.CHEST);
        register(context, items, SPEED, ItemTags.FOOT_ARMOR_ENCHANTABLE, 4,
                R196EnchantmentRules.STANDARD_MAX_LEVEL, 12, 12, EquipmentSlotGroup.FEET);
        register(context, items, ENDURANCE, ModTags.Items.R196_CHEST_ARMOR_ENCHANTABLE, 4,
                R196EnchantmentRules.ENDURANCE_MAX_LEVEL, 12, 12, EquipmentSlotGroup.CHEST);
        register(context, items, PROTECTION, ItemTags.ARMOR_ENCHANTABLE, 6,
                R196EnchantmentRules.PROTECTION_MAX_LEVEL, 8, 8, EquipmentSlotGroup.ARMOR);
        register(context, items, CLUMSINESS, ItemTags.DURABILITY_ENCHANTABLE, 1, 1, 25, 25, EquipmentSlotGroup.ANY);
    }

    private static void register(
            BootstrapContext<Enchantment> context,
            HolderGetter<Item> items,
            ResourceKey<Enchantment> key,
            TagKey<Item> supported,
            int weight,
            int maximumLevel,
            int minimumCost,
            int costPerLevel,
            EquipmentSlotGroup slots) {
        HolderSet<Item> supportedItems = items.getOrThrow(supported);
        Enchantment.Builder builder = Enchantment.enchantment(Enchantment.definition(
                supportedItems,
                weight,
                maximumLevel,
                Enchantment.dynamicCost(minimumCost, costPerLevel),
                Enchantment.dynamicCost(minimumCost + 20, costPerLevel),
                8,
                slots));
        if (key.equals(DURABILITY)) {
            builder.withEffect(
                    EnchantmentEffectComponents.ITEM_DAMAGE,
                    new RemoveBinomial(LevelBasedValue.perLevel(0.15F)));
        }
        context.register(key, builder.build(key.identifier()));
    }
}
