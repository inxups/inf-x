package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.enchantment.EnchantmentRules;
import com.pixulse.infx.registry.tag.InfXItemTags;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.advancements.criterion.DamageSourcePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.EntityTypePredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.item.enchantment.effects.Ignite;
import net.minecraft.world.item.enchantment.effects.RemoveBinomial;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

/**
 * The 22 INFX enchantments, the 17 vanilla-derived MITE enchantments re-registered under their
 * {@code minecraft:} ids with MITE profiles, and the crafting-only clumsiness curse.
 */
public final class InfXEnchantments {
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

    /**
     * MITE enchantments that survive in 26.2 under their vanilla ids. Their definitions are
     * overridden via datagen so acquisition follows the same MITE table rules as the INFX set.
     */
    public static final ResourceKey<Enchantment> VANILLA_FIRE_PROTECTION = vanillaKey("fire_protection");
    public static final ResourceKey<Enchantment> VANILLA_FEATHER_FALLING = vanillaKey("feather_falling");
    public static final ResourceKey<Enchantment> VANILLA_BLAST_PROTECTION = vanillaKey("blast_protection");
    public static final ResourceKey<Enchantment> VANILLA_PROJECTILE_PROTECTION = vanillaKey("projectile_protection");
    public static final ResourceKey<Enchantment> VANILLA_RESPIRATION = vanillaKey("respiration");
    public static final ResourceKey<Enchantment> VANILLA_AQUA_AFFINITY = vanillaKey("aqua_affinity");
    public static final ResourceKey<Enchantment> VANILLA_THORNS = vanillaKey("thorns");
    public static final ResourceKey<Enchantment> VANILLA_SMITE = vanillaKey("smite");
    public static final ResourceKey<Enchantment> VANILLA_BANE_OF_ARTHROPODS = vanillaKey("bane_of_arthropods");
    public static final ResourceKey<Enchantment> VANILLA_KNOCKBACK = vanillaKey("knockback");
    public static final ResourceKey<Enchantment> VANILLA_FIRE_ASPECT = vanillaKey("fire_aspect");
    public static final ResourceKey<Enchantment> VANILLA_LOOTING = vanillaKey("looting");
    public static final ResourceKey<Enchantment> VANILLA_EFFICIENCY = vanillaKey("efficiency");
    public static final ResourceKey<Enchantment> VANILLA_SILK_TOUCH = vanillaKey("silk_touch");
    public static final ResourceKey<Enchantment> VANILLA_POWER = vanillaKey("power");
    public static final ResourceKey<Enchantment> VANILLA_PUNCH = vanillaKey("punch");
    public static final ResourceKey<Enchantment> VANILLA_FLAME = vanillaKey("flame");

    public static final List<ResourceKey<Enchantment>> INFX = List.of(
            DURABILITY, DISARMING, QUICKNESS, PRECISION, POISONING, BUTCHERING, STUNNING,
            VAMPIRISM, RECOVERY, SLAUGHTER, CLEAVING, HARVESTING, PENETRATION, BAITING,
            FERTILITY, TREE_FELLING, FORTUNE, FREE_MOVEMENT, REGENERATION, SPEED,
            ENDURANCE, PROTECTION);

    public static final List<ResourceKey<Enchantment>> VANILLA_R196 = List.of(
            VANILLA_FIRE_PROTECTION, VANILLA_FEATHER_FALLING, VANILLA_BLAST_PROTECTION,
            VANILLA_PROJECTILE_PROTECTION, VANILLA_RESPIRATION, VANILLA_AQUA_AFFINITY,
            VANILLA_THORNS, VANILLA_SMITE, VANILLA_BANE_OF_ARTHROPODS, VANILLA_KNOCKBACK,
            VANILLA_FIRE_ASPECT, VANILLA_LOOTING, VANILLA_EFFICIENCY, VANILLA_SILK_TOUCH,
            VANILLA_POWER, VANILLA_PUNCH, VANILLA_FLAME);

    /** Every enchantment served by the INFX tables, trades, loot and mob equipment. */
    public static final List<ResourceKey<Enchantment>> ALL =
            Stream.concat(INFX.stream(), VANILLA_R196.stream()).toList();

    private static final Map<ResourceKey<Enchantment>, EnchantmentProfile> INFX_PROFILES = Map.ofEntries(
            Map.entry(DURABILITY, profile(Rarity.UNCOMMON, 10)),
            Map.entry(DISARMING, profile(Rarity.RARE, 10)),
            Map.entry(QUICKNESS, profile(Rarity.UNCOMMON, 10)),
            Map.entry(PRECISION, profile(Rarity.COMMON, 10)),
            Map.entry(POISONING, profile(Rarity.RARE, 10)),
            Map.entry(BUTCHERING, profile(Rarity.UNCOMMON, 10)),
            Map.entry(STUNNING, profile(Rarity.UNCOMMON, 15)),
            Map.entry(VAMPIRISM, profile(Rarity.EPIC, 20)),
            Map.entry(RECOVERY, profile(Rarity.UNCOMMON, 10)),
            Map.entry(SLAUGHTER, profile(Rarity.COMMON, 10)),
            // INFX exposes cleaving and penetration as one rare piercing enchantment that merely
            // renames itself on axes, so the split registration must keep the rare weight.
            Map.entry(CLEAVING, profile(Rarity.RARE, 10)),
            Map.entry(HARVESTING, profile(Rarity.UNCOMMON, 10)),
            Map.entry(PENETRATION, profile(Rarity.RARE, 10)),
            Map.entry(BAITING, profile(Rarity.COMMON, 10)),
            Map.entry(FERTILITY, profile(Rarity.UNCOMMON, 10)),
            Map.entry(TREE_FELLING, profile(Rarity.UNCOMMON, 10)),
            Map.entry(FORTUNE, profile(Rarity.RARE, 10)),
            Map.entry(FREE_MOVEMENT, profile(Rarity.UNCOMMON, 10)),
            Map.entry(REGENERATION, profile(Rarity.RARE, 20)),
            Map.entry(SPEED, profile(Rarity.RARE, 10)),
            Map.entry(ENDURANCE, profile(Rarity.UNCOMMON, 10)),
            Map.entry(PROTECTION, profile(Rarity.COMMON, 10)),
            Map.entry(VANILLA_FIRE_PROTECTION, profile(Rarity.UNCOMMON, 10)),
            Map.entry(VANILLA_FEATHER_FALLING, profile(Rarity.UNCOMMON, 10)),
            Map.entry(VANILLA_BLAST_PROTECTION, profile(Rarity.UNCOMMON, 10)),
            Map.entry(VANILLA_PROJECTILE_PROTECTION, profile(Rarity.UNCOMMON, 10)),
            Map.entry(VANILLA_RESPIRATION, profile(Rarity.RARE, 10)),
            Map.entry(VANILLA_AQUA_AFFINITY, profile(Rarity.RARE, 10)),
            Map.entry(VANILLA_THORNS, profile(Rarity.RARE, 20)),
            Map.entry(VANILLA_SMITE, profile(Rarity.UNCOMMON, 10)),
            Map.entry(VANILLA_BANE_OF_ARTHROPODS, profile(Rarity.UNCOMMON, 10)),
            Map.entry(VANILLA_KNOCKBACK, profile(Rarity.UNCOMMON, 10)),
            Map.entry(VANILLA_FIRE_ASPECT, profile(Rarity.RARE, 20)),
            Map.entry(VANILLA_LOOTING, profile(Rarity.UNCOMMON, 10)),
            Map.entry(VANILLA_EFFICIENCY, profile(Rarity.COMMON, 10)),
            Map.entry(VANILLA_SILK_TOUCH, profile(Rarity.RARE, 10)),
            Map.entry(VANILLA_POWER, profile(Rarity.COMMON, 10)),
            Map.entry(VANILLA_PUNCH, profile(Rarity.UNCOMMON, 10)),
            Map.entry(VANILLA_FLAME, profile(Rarity.RARE, 20)));

    private InfXEnchantments() {}

    private static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, InfiniteX.id(path));
    }

    private static ResourceKey<Enchantment> vanillaKey(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.withDefaultNamespace(path));
    }

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderGetter<Item> items = context.lookup(Registries.ITEM);
        HolderGetter<Enchantment> enchantments = context.lookup(Registries.ENCHANTMENT);
        register(context, items, enchantments, DURABILITY, InfXItemTags.INFX_DURABILITY_ENCHANTABLE,
                EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.ANY);
        register(context, items, enchantments, DISARMING, InfXItemTags.INFX_DISARMING_ENCHANTABLE,
                EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, QUICKNESS, ItemTags.BOW_ENCHANTABLE,
                EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, PRECISION, ItemTags.BOW_ENCHANTABLE,
                EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, POISONING, ItemTags.BOW_ENCHANTABLE,
                EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, BUTCHERING, InfXItemTags.INFX_BUTCHERING_ENCHANTABLE,
                EnchantmentRules.BUTCHERING_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, STUNNING, InfXItemTags.INFX_STUNNING_ENCHANTABLE,
                EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, VAMPIRISM, InfXItemTags.INFX_VAMPIRISM_ENCHANTABLE,
                EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, RECOVERY, ItemTags.BOW_ENCHANTABLE,
                EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, SLAUGHTER, InfXItemTags.INFX_SLAUGHTER_ENCHANTABLE,
                EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, CLEAVING, InfXItemTags.INFX_CLEAVING_ENCHANTABLE,
                EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, HARVESTING, InfXItemTags.INFX_HARVESTING_ENCHANTABLE,
                EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, PENETRATION, InfXItemTags.INFX_PENETRATION_ENCHANTABLE,
                EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, BAITING, ItemTags.FISHING_ENCHANTABLE,
                EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, FERTILITY, InfXItemTags.INFX_FERTILITY_ENCHANTABLE,
                EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, TREE_FELLING, InfXItemTags.INFX_TREE_FELLING_ENCHANTABLE,
                EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, FORTUNE, InfXItemTags.INFX_FORTUNE_ENCHANTABLE,
                EnchantmentRules.FORTUNE_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, FREE_MOVEMENT, InfXItemTags.INFX_FREE_MOVEMENT_ENCHANTABLE,
                EnchantmentRules.FREE_MOVEMENT_MAX_LEVEL, EquipmentSlotGroup.LEGS);
        register(context, items, enchantments, REGENERATION, InfXItemTags.INFX_CHEST_ARMOR_ENCHANTABLE,
                EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.CHEST);
        register(context, items, enchantments, SPEED, ItemTags.FOOT_ARMOR_ENCHANTABLE,
                EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.FEET);
        register(context, items, enchantments, ENDURANCE, InfXItemTags.INFX_CHEST_ARMOR_ENCHANTABLE,
                EnchantmentRules.ENDURANCE_MAX_LEVEL, EquipmentSlotGroup.CHEST);
        register(context, items, enchantments, PROTECTION, ItemTags.ARMOR_ENCHANTABLE,
                EnchantmentRules.PROTECTION_MAX_LEVEL, EquipmentSlotGroup.ARMOR);
        bootstrapVanilla(context, items, enchantments);
        registerCraftingCurse(context, items, enchantments);
    }

    /**
     * Re-registers the vanilla-derived MITE enchantments with MITE rarity, difficulty windows,
     * level caps and item targets. Effects that MITE computes in armor, damage or drop code are
     * implemented in the INFX event pipeline instead of as data components.
     */
    private static void bootstrapVanilla(
            BootstrapContext<Enchantment> context,
            HolderGetter<Item> items,
            HolderGetter<Enchantment> enchantments) {
        HolderGetter<EntityType<?>> entityTypes = context.lookup(Registries.ENTITY_TYPE);
        register(context, items, enchantments, VANILLA_FIRE_PROTECTION, ItemTags.ARMOR_ENCHANTABLE,
                EnchantmentRules.PROTECTION_MAX_LEVEL, EquipmentSlotGroup.ARMOR,
                builder -> builder.withEffect(
                        EnchantmentEffectComponents.ATTRIBUTES,
                        new EnchantmentAttributeEffect(
                                Identifier.withDefaultNamespace("enchantment.fire_protection"),
                                Attributes.BURNING_TIME,
                                LevelBasedValue.perLevel(-EnchantmentRules.FIRE_PROTECTION_BURN_REDUCTION_PER_LEVEL),
                                AttributeModifier.Operation.ADD_MULTIPLIED_BASE)));
        register(context, items, enchantments, VANILLA_FEATHER_FALLING, ItemTags.FOOT_ARMOR_ENCHANTABLE,
                EnchantmentRules.PROTECTION_MAX_LEVEL, EquipmentSlotGroup.FEET);
        register(context, items, enchantments, VANILLA_BLAST_PROTECTION, InfXItemTags.INFX_SOLID_METAL_TORSO_ENCHANTABLE,
                EnchantmentRules.PROTECTION_MAX_LEVEL, EquipmentSlotGroup.ARMOR,
                builder -> builder.withEffect(
                        EnchantmentEffectComponents.ATTRIBUTES,
                        new EnchantmentAttributeEffect(
                                Identifier.withDefaultNamespace("enchantment.blast_protection"),
                                Attributes.EXPLOSION_KNOCKBACK_RESISTANCE,
                                LevelBasedValue.perLevel(EnchantmentRules.BLAST_PROTECTION_KNOCKBACK_REDUCTION_PER_LEVEL),
                                AttributeModifier.Operation.ADD_VALUE)));
        register(context, items, enchantments, VANILLA_PROJECTILE_PROTECTION, InfXItemTags.INFX_SOLID_METAL_TORSO_ENCHANTABLE,
                EnchantmentRules.PROTECTION_MAX_LEVEL, EquipmentSlotGroup.ARMOR);
        register(context, items, enchantments, VANILLA_RESPIRATION, ItemTags.HEAD_ARMOR_ENCHANTABLE,
                EnchantmentRules.RESPIRATION_MAX_LEVEL, EquipmentSlotGroup.HEAD,
                builder -> builder.withEffect(
                        EnchantmentEffectComponents.ATTRIBUTES,
                        new EnchantmentAttributeEffect(
                                Identifier.withDefaultNamespace("enchantment.respiration"),
                                Attributes.OXYGEN_BONUS,
                                LevelBasedValue.perLevel(1.0F),
                                AttributeModifier.Operation.ADD_VALUE)));
        register(context, items, enchantments, VANILLA_AQUA_AFFINITY, ItemTags.HEAD_ARMOR_ENCHANTABLE,
                1, EquipmentSlotGroup.HEAD,
                builder -> builder.withEffect(
                        EnchantmentEffectComponents.ATTRIBUTES,
                        new EnchantmentAttributeEffect(
                                Identifier.withDefaultNamespace("enchantment.aqua_affinity"),
                                Attributes.SUBMERGED_MINING_SPEED,
                                LevelBasedValue.perLevel(4.0F),
                                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)));
        register(context, items, enchantments, VANILLA_THORNS, InfXItemTags.INFX_THORNS_ENCHANTABLE,
                EnchantmentRules.THORNS_MAX_LEVEL, EquipmentSlotGroup.CHEST);
        register(context, items, enchantments, VANILLA_SMITE, InfXItemTags.INFX_SMITE_ENCHANTABLE,
                EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND,
                builder -> builder.withEffect(
                        EnchantmentEffectComponents.DAMAGE,
                        new AddValue(LevelBasedValue.perLevel(
                                EnchantmentRules.SMITE_DAMAGE_PER_LEVEL)),
                        LootItemEntityPropertyCondition.hasProperties(
                                LootContext.EntityTarget.THIS,
                                EntityPredicate.Builder.entity().entityType(
                                        EntityTypePredicate.of(entityTypes, EntityTypeTags.SENSITIVE_TO_SMITE)))));
        register(context, items, enchantments, VANILLA_BANE_OF_ARTHROPODS, InfXItemTags.INFX_SWORD_FAMILY_ENCHANTABLE,
                EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND,
                builder -> builder.withEffect(
                        EnchantmentEffectComponents.DAMAGE,
                        new AddValue(LevelBasedValue.perLevel(
                                EnchantmentRules.SMITE_DAMAGE_PER_LEVEL)),
                        LootItemEntityPropertyCondition.hasProperties(
                                LootContext.EntityTarget.THIS,
                                EntityPredicate.Builder.entity().entityType(
                                        EntityTypePredicate.of(entityTypes, EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)))));
        register(context, items, enchantments, VANILLA_KNOCKBACK, InfXItemTags.INFX_KNOCKBACK_ENCHANTABLE,
                EnchantmentRules.KNOCKBACK_MAX_LEVEL, EquipmentSlotGroup.MAINHAND,
                builder -> builder.withEffect(
                        EnchantmentEffectComponents.KNOCKBACK,
                        new AddValue(LevelBasedValue.perLevel(1.0F))));
        register(context, items, enchantments, VANILLA_FIRE_ASPECT, InfXItemTags.INFX_SWORD_FAMILY_ENCHANTABLE,
                EnchantmentRules.KNOCKBACK_MAX_LEVEL, EquipmentSlotGroup.MAINHAND,
                builder -> builder.withEffect(
                        EnchantmentEffectComponents.POST_ATTACK,
                        EnchantmentTarget.ATTACKER,
                        EnchantmentTarget.VICTIM,
                        // MITE ignites for a fixed second regardless of level; the level only
                        // marks the damage as fire-aspect for mob immunity checks.
                        new Ignite(LevelBasedValue.constant(1.0F)),
                        net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition.hasDamageSource(
                                DamageSourcePredicate.Builder.damageType().isDirect(true))));
        register(context, items, enchantments, VANILLA_LOOTING, InfXItemTags.INFX_LOOTING_ENCHANTABLE,
                EnchantmentRules.LOOTING_MAX_LEVEL, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, VANILLA_EFFICIENCY, InfXItemTags.INFX_EFFICIENCY_ENCHANTABLE,
                EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND,
                builder -> builder.withEffect(
                        EnchantmentEffectComponents.ATTRIBUTES,
                        new EnchantmentAttributeEffect(
                                Identifier.withDefaultNamespace("enchantment.efficiency"),
                                Attributes.MINING_EFFICIENCY,
                                new LevelBasedValue.LevelsSquared(1.0F),
                                AttributeModifier.Operation.ADD_VALUE)));
        register(context, items, enchantments, VANILLA_SILK_TOUCH, InfXItemTags.INFX_SILK_TOUCH_ENCHANTABLE,
                1, EquipmentSlotGroup.MAINHAND);
        register(context, items, enchantments, VANILLA_POWER, ItemTags.BOW_ENCHANTABLE,
                EnchantmentRules.STANDARD_MAX_LEVEL, EquipmentSlotGroup.MAINHAND,
                builder -> builder.withEffect(
                        EnchantmentEffectComponents.DAMAGE,
                        new AddValue(LevelBasedValue.perLevel(1.0F, 0.5F)),
                        LootItemEntityPropertyCondition.hasProperties(
                                LootContext.EntityTarget.DIRECT_ATTACKER,
                                EntityPredicate.Builder.entity().of(entityTypes, EntityTypeTags.ARROWS).build())));
        register(context, items, enchantments, VANILLA_PUNCH, ItemTags.BOW_ENCHANTABLE,
                EnchantmentRules.KNOCKBACK_MAX_LEVEL, EquipmentSlotGroup.MAINHAND,
                builder -> builder.withEffect(
                        EnchantmentEffectComponents.KNOCKBACK,
                        new AddValue(LevelBasedValue.perLevel(1.0F)),
                        LootItemEntityPropertyCondition.hasProperties(
                                LootContext.EntityTarget.DIRECT_ATTACKER,
                                EntityPredicate.Builder.entity().of(entityTypes, EntityTypeTags.ARROWS).build())));
        register(context, items, enchantments, VANILLA_FLAME, ItemTags.BOW_ENCHANTABLE,
                1, EquipmentSlotGroup.MAINHAND,
                builder -> builder.withEffect(
                        EnchantmentEffectComponents.PROJECTILE_SPAWNED,
                        new Ignite(LevelBasedValue.constant(100.0F))));
    }

    public static EnchantmentProfile profile(ResourceKey<Enchantment> key) {
        EnchantmentProfile profile = INFX_PROFILES.get(key);
        if (profile == null) {
            throw new IllegalArgumentException("Not an INFX enchantment: " + key.identifier());
        }
        return profile;
    }

    private static EnchantmentProfile profile(Rarity rarity, int difficulty) {
        return new EnchantmentProfile(rarity, difficulty);
    }

    private static void register(
            BootstrapContext<Enchantment> context,
            HolderGetter<Item> items,
            HolderGetter<Enchantment> enchantments,
            ResourceKey<Enchantment> key,
            TagKey<Item> supported,
            int maximumLevel,
            EquipmentSlotGroup slots) {
        register(context, items, enchantments, key, supported, maximumLevel, slots, builder -> {});
    }

    private static void register(
            BootstrapContext<Enchantment> context,
            HolderGetter<Item> items,
            HolderGetter<Enchantment> enchantments,
            ResourceKey<Enchantment> key,
            TagKey<Item> supported,
            int maximumLevel,
            EquipmentSlotGroup slots,
            java.util.function.Consumer<Enchantment.Builder> effects) {
        HolderSet<Item> supportedItems = items.getOrThrow(supported);
        EnchantmentProfile profile = profile(key);
        Enchantment.Builder builder = Enchantment.enchantment(Enchantment.definition(
                supportedItems,
                profile.weight(),
                maximumLevel,
                Enchantment.dynamicCost(profile.minimumCost(1), profile.difficulty()),
                Enchantment.dynamicCost(profile.maximumCost(1), profile.difficulty()),
                8,
                slots)).exclusiveWith(exclusiveSet(enchantments, key));
        if (key.equals(DURABILITY)) {
            builder.withEffect(
                    EnchantmentEffectComponents.ITEM_DAMAGE,
                    new RemoveBinomial(LevelBasedValue.perLevel(
                            EnchantmentRules.DURABILITY_NEGATION_PER_LEVEL)));
        }
        effects.accept(builder);
        context.register(key, builder.build(key.identifier()));
    }

    /**
     * MITE's canApplyTogether allows any two distinct enchantments except silk touch with
     * fortune, so every set contains the enchantment itself plus that one special pair.
     */
    private static HolderSet<Enchantment> exclusiveSet(
            HolderGetter<Enchantment> enchantments, ResourceKey<Enchantment> key) {
        if (key.equals(FORTUNE)) {
            return HolderSet.direct(
                    enchantments.getOrThrow(key), enchantments.getOrThrow(VANILLA_SILK_TOUCH));
        }
        if (key.equals(VANILLA_SILK_TOUCH)) {
            return HolderSet.direct(
                    enchantments.getOrThrow(key), enchantments.getOrThrow(FORTUNE));
        }
        return HolderSet.direct(enchantments.getOrThrow(key));
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

    public enum Rarity {
        COMMON(100),
        UNCOMMON(25),
        RARE(5),
        EPIC(1);

        private final int weight;

        Rarity(int weight) {
            this.weight = weight;
        }

        public int weight() {
            return weight;
        }
    }

    public record EnchantmentProfile(Rarity rarity, int difficulty) {
        public EnchantmentProfile {
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
