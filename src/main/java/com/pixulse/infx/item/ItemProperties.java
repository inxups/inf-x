package com.pixulse.infx.item;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.config.InfXConfig;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.tag.InfXItemTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.component.BlocksAttacks;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.equipment.Equippable;
import com.pixulse.infx.item.material.Quality;
import com.pixulse.infx.registry.InfXDataComponents;

public final class ItemProperties {
    private ItemProperties() {}

    public static Item.Properties forEquipment(EquipmentKey key, Item.Properties properties) {
        if (key.material().has(InfxMaterial.Flag.LAVA_SAFE)) {
            properties.fireResistant();
        }
        if (key.material() == InfxMaterial.RUSTED_IRON
                && key.durability() > 0) {
            properties.component(InfXDataComponents.QUALITY.get(), Quality.POOR);
        }
        return switch (key.type().armorForm()) {
            case PLATE, CHAIN -> armor(key, properties);
            case HORSE -> horseArmor(key, properties);
            case NONE -> switch (key.type()) {
                case BOW, FISHING_ROD -> commonDamageable(key, properties);
                case ARROW -> properties.stacksTo(16);
                default -> tool(key, properties);
            };
        };
    }

    private static Item.Properties tool(EquipmentKey key, Item.Properties properties) {
        var p = commonDamageable(key, properties)
                .component(DataComponents.TOOL, key.type().miningFamily().createTool(key))
                .attributes(toolAttributes(key))
                .component(
                        DataComponents.WEAPON,
                        new Weapon(key.attackWear(), key.type().disablesBlockingSeconds()));
        // Shears do not block attacks; they use the normal weapon component for left-click melee.
        // The INFX toolsBlockAttacks config removes the blocking component entirely.
        if (key.type() != EquipmentType.SHEARS && key.type() != EquipmentType.HOE && InfXConfig.toolsBlockAttacks()) {
            p.component(DataComponents.BLOCKS_ATTACKS, toolBlocking());
        }
        return p;
    }

    private static BlocksAttacks toolBlocking() {
        return new BlocksAttacks(
                0.1F,
                1.0F,
                List.of(new BlocksAttacks.DamageReduction(100.0F, Optional.empty(), 0.0F, 0.5F)),
                new BlocksAttacks.ItemDamageFunction(0.0F, 0.0F, 0.5F),
                Optional.empty(),
                Optional.of(SoundEvents.SHIELD_BLOCK),
                Optional.of(SoundEvents.SHIELD_BREAK));
    }

    static ItemAttributeModifiers toolAttributes(EquipmentKey key) {
        EquipmentType type = key.type();
        ItemAttributeModifiers.Builder attributes = ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                Item.BASE_ATTACK_DAMAGE_ID,
                                key.meleeDamage() - 1.0F,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND);
        if (type.hasAttackSpeedModifier()) {
            attributes.add(
                    Attributes.ATTACK_SPEED,
                    new AttributeModifier(
                            Item.BASE_ATTACK_SPEED_ID,
                            type.attackSpeedModifier(),
                            AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }
        if (type.reachBonus() != 0.0F) {
            Identifier reachId = InfiniteX.id("tool_reach");
            attributes.add(
                    Attributes.BLOCK_INTERACTION_RANGE,
                    new AttributeModifier(reachId, type.reachBonus(), AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
            attributes.add(
                    Attributes.ENTITY_INTERACTION_RANGE,
                    new AttributeModifier(reachId, type.reachBonus(), AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }
        return attributes.build();
    }

    private static Item.Properties armor(EquipmentKey key, Item.Properties properties) {
        EquipmentSlot slot = key.type().armorType().orElseThrow().getSlot();
        Equippable equippable = Equippable.builder(slot)
                .setEquipSound(SoundEvents.ARMOR_EQUIP_GENERIC)
                .setAsset(key.equipmentAsset())
                .build();
        return commonDamageable(key, properties)
                .attributes(armorAttributes(key))
                .component(DataComponents.EQUIPPABLE, equippable);
    }

    private static Item.Properties horseArmor(EquipmentKey key, Item.Properties properties) {
        HolderGetter<EntityType<?>> entities =
                BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.ENTITY_TYPE);
        Equippable equippable = Equippable.builder(EquipmentSlot.BODY)
                .setEquipSound(SoundEvents.HORSE_ARMOR)
                .setAsset(key.equipmentAsset())
                .setAllowedEntities(entities.getOrThrow(EntityTypeTags.CAN_WEAR_HORSE_ARMOR))
                .setDamageOnHurt(false)
                .setCanBeSheared(true)
                .setShearingSound(SoundEvents.HORSE_ARMOR_UNEQUIP)
                .build();
        return properties
                .stacksTo(1)
                .attributes(armorAttributes(key))
                .component(DataComponents.EQUIPPABLE, equippable);
    }

    static ItemAttributeModifiers armorAttributes(EquipmentKey key) {
        EquipmentSlot slot = key.type().armorForm() == EquipmentType.ArmorForm.HORSE
                ? EquipmentSlot.BODY
                : key.type().armorType().orElseThrow().getSlot();
        Identifier armorId = InfiniteX.id("armor." + key.type().path());
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ARMOR,
                        new AttributeModifier(
                                armorId, key.armorProtection(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.bySlot(slot))
                .build();
    }

    private static Item.Properties commonDamageable(EquipmentKey key, Item.Properties properties) {
        int durability = key.material() == InfxMaterial.RUSTED_IRON
                ? Math.max(1, Math.round(key.durability() * Quality.POOR.durabilityMultiplier()))
                : key.durability();
        properties.durability(durability).repairable(InfXItemTags.repairMaterial(key.material()));
        if (key.material().enchantability() > 0) {
            properties.enchantable(key.material().enchantability());
        }
        return properties;
    }
}
