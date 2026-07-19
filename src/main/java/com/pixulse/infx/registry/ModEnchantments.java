package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
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
        register(context, items, DURABILITY, ItemTags.DURABILITY_ENCHANTABLE, 8, 3, 5, 8, EquipmentSlotGroup.ANY);
        register(context, items, DISARMING, ItemTags.MELEE_WEAPON_ENCHANTABLE, 3, 3, 15, 12, EquipmentSlotGroup.MAINHAND);
        register(context, items, QUICKNESS, ItemTags.BOW_ENCHANTABLE, 5, 3, 10, 10, EquipmentSlotGroup.MAINHAND);
        register(context, items, PRECISION, ItemTags.BOW_ENCHANTABLE, 5, 3, 10, 10, EquipmentSlotGroup.MAINHAND);
        register(context, items, POISONING, ItemTags.WEAPON_ENCHANTABLE, 3, 3, 18, 12, EquipmentSlotGroup.MAINHAND);
        register(context, items, BUTCHERING, ItemTags.MELEE_WEAPON_ENCHANTABLE, 4, 3, 12, 10, EquipmentSlotGroup.MAINHAND);
        register(context, items, STUNNING, ItemTags.MELEE_WEAPON_ENCHANTABLE, 3, 3, 18, 12, EquipmentSlotGroup.MAINHAND);
        register(context, items, VAMPIRISM, ItemTags.MELEE_WEAPON_ENCHANTABLE, 2, 3, 25, 15, EquipmentSlotGroup.MAINHAND);
        register(context, items, RECOVERY, ItemTags.BOW_ENCHANTABLE, 5, 3, 12, 10, EquipmentSlotGroup.MAINHAND);
        register(context, items, SLAUGHTER, ItemTags.MELEE_WEAPON_ENCHANTABLE, 4, 4, 10, 10, EquipmentSlotGroup.MAINHAND);
        register(context, items, CLEAVING, ItemTags.MELEE_WEAPON_ENCHANTABLE, 4, 4, 10, 10, EquipmentSlotGroup.MAINHAND);
        register(context, items, HARVESTING, ItemTags.MINING_ENCHANTABLE, 5, 3, 8, 10, EquipmentSlotGroup.MAINHAND);
        register(context, items, PENETRATION, ItemTags.MINING_ENCHANTABLE, 4, 4, 12, 10, EquipmentSlotGroup.MAINHAND);
        register(context, items, BAITING, ItemTags.FISHING_ENCHANTABLE, 5, 3, 10, 10, EquipmentSlotGroup.MAINHAND);
        register(context, items, FERTILITY, ItemTags.HOES, 4, 3, 12, 10, EquipmentSlotGroup.MAINHAND);
        register(context, items, TREE_FELLING, ItemTags.AXES, 3, 4, 15, 12, EquipmentSlotGroup.MAINHAND);
        register(context, items, FORTUNE, ItemTags.MINING_ENCHANTABLE, 3, 3, 18, 12, EquipmentSlotGroup.MAINHAND);
        register(context, items, FREE_MOVEMENT, ItemTags.ARMOR_ENCHANTABLE, 4, 3, 12, 12, EquipmentSlotGroup.ARMOR);
        register(context, items, REGENERATION, ItemTags.ARMOR_ENCHANTABLE, 2, 3, 24, 16, EquipmentSlotGroup.ARMOR);
        register(context, items, SPEED, ItemTags.FOOT_ARMOR_ENCHANTABLE, 4, 3, 12, 12, EquipmentSlotGroup.FEET);
        register(context, items, ENDURANCE, ItemTags.ARMOR_ENCHANTABLE, 4, 3, 12, 12, EquipmentSlotGroup.ARMOR);
        register(context, items, PROTECTION, ItemTags.ARMOR_ENCHANTABLE, 6, 4, 8, 8, EquipmentSlotGroup.ARMOR);
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
                    new RemoveBinomial(new LevelBasedValue.Fraction(
                            LevelBasedValue.perLevel(1.0F),
                            LevelBasedValue.perLevel(2.0F, 1.0F))));
        }
        context.register(key, builder.build(key.identifier()));
    }
}
