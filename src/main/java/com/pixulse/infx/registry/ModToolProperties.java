package com.pixulse.infx.registry;

import java.util.List;

import com.pixulse.infx.InfiniteX;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.block.Block;

final class ModToolProperties {
    private ModToolProperties() {}

    static Item.Properties flintHatchet(Item.Properties properties) {
        return tool(
                properties,
                BlockTags.INCORRECT_FOR_WOODEN_TOOL,
                BlockTags.MINEABLE_WITH_AXE,
                400,
                2.5F,
                5.0F,
                -3.2F,
                0.5F,
                5.0F);
    }

    static Item.Properties copperPickaxe(Item.Properties properties) {
        return tool(
                        properties,
                        BlockTags.INCORRECT_FOR_COPPER_TOOL,
                        BlockTags.MINEABLE_WITH_PICKAXE,
                        4800,
                        7.0F,
                        2.0F,
                        -2.8F,
                        0.75F,
                        0.0F)
                .repairable(ItemTags.COPPER_TOOL_MATERIALS)
                .enchantable(30);
    }

    private static Item.Properties tool(
            Item.Properties properties,
            TagKey<Block> incorrectForDrops,
            TagKey<Block> minesEfficiently,
            int durability,
            float miningSpeed,
            float attackDamage,
            float attackSpeed,
            float reachBonus,
            float disableBlockingSeconds) {
        HolderGetter<Block> blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        Tool tool = new Tool(
                List.of(
                        Tool.Rule.deniesDrops(blocks.getOrThrow(incorrectForDrops)),
                        Tool.Rule.minesAndDrops(blocks.getOrThrow(minesEfficiently), miningSpeed)),
                1.0F,
                0,
                true);

        Identifier reachId = InfiniteX.id("tool_reach");
        ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(
                        Attributes.BLOCK_INTERACTION_RANGE,
                        new AttributeModifier(reachId, reachBonus, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(
                        Attributes.ENTITY_INTERACTION_RANGE,
                        new AttributeModifier(reachId, reachBonus, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();

        return properties
                .durability(durability)
                .component(DataComponents.TOOL, tool)
                .attributes(attributes)
                .component(DataComponents.WEAPON, new Weapon(2, disableBlockingSeconds));
    }
}
