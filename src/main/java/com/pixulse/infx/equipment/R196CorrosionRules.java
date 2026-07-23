package com.pixulse.infx.equipment;

import com.pixulse.infx.entity.R196GelatinousCubeRules;
import com.pixulse.infx.item.R196BucketItem;
import com.pixulse.infx.item.R196Catalog;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** R196 item damage rules shared by gelatinous cubes, their projectiles, and dropped items. */
public final class R196CorrosionRules {
    private static final float TOOL_DAMAGE_SCALE = 100.0F;
    private static final float ARMOR_DAMAGE_SCALE = 2.0F;

    private R196CorrosionRules() {}

    public static boolean isHarmedBy(R196Material material, R196CorrosionType type) {
        return switch (type) {
            case PEPSIN -> material == R196Material.LEATHER;
            case ACID -> switch (material) {
                case FLINT, OBSIDIAN, GOLD, MITHRIL -> false;
                default -> true;
            };
        };
    }

    public static boolean isHarmedBy(ItemStack stack, R196CorrosionType type) {
        if (stack.isEmpty()) {
            return false;
        }

        R196Catalog.EquipmentEntry equipment = ModItems.catalog().equipment(stack);
        if (equipment != null) {
            return isHarmedBy(equipment.key().material(), type);
        }
        R196Catalog.RawEntry raw = ModItems.catalog().raw(stack.getItem());
        if (raw != null && raw.definition().material().isPresent()) {
            return isHarmedBy(raw.definition().material().orElseThrow(), type);
        }
        if (stack.getItem() instanceof R196BucketItem bucket) {
            return isHarmedBy(bucket.material(), type);
        }

        if (type == R196CorrosionType.PEPSIN) {
            return isPepsinHarmedVanillaItem(stack);
        }
        return isAcidHarmedVanillaItem(stack);
    }

    public static int scaledDamage(ItemStack stack, float amount) {
        if (!stack.isDamageableItem() || amount <= 0.0F) {
            return 0;
        }
        R196Catalog.EquipmentEntry equipment = ModItems.catalog().equipment(stack);
        if (equipment == null) {
            return Math.max(1, Math.round(amount));
        }
        R196EquipmentType.ArmorForm armor = equipment.key().type().armorForm();
        float scale = armor == R196EquipmentType.ArmorForm.PLATE || armor == R196EquipmentType.ArmorForm.CHAIN
                ? ARMOR_DAMAGE_SCALE
                : TOOL_DAMAGE_SCALE;
        return Math.max(1, Math.round(amount * scale));
    }

    public static boolean damageHeldItem(ServerPlayer player, R196CorrosionType type, float amount) {
        if (player.hasInfiniteMaterials()) {
            return false;
        }
        return damageStack(player.level(), player, player.getMainHandItem(), type, amount, EquipmentSlot.MAINHAND);
    }

    public static boolean damageInventory(
            ServerPlayer player, R196CorrosionType type, float chancePerItem, float amount) {
        if (player.hasInfiniteMaterials() || chancePerItem <= 0.0F || amount <= 0.0F) {
            return false;
        }

        boolean damaged = false;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            damaged |= damageStackWithChance(player, stack, type, chancePerItem, amount, null);
        }
        for (EquipmentSlot slot : new EquipmentSlot[] {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        }) {
            damaged |= damageStackWithChance(player, player.getItemBySlot(slot), type, chancePerItem * 5.0F, amount, slot);
        }
        return damaged;
    }

    public static boolean damageItemEntity(ItemEntity entity, R196CorrosionType type, float amount) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return false;
        }
        ItemStack stack = entity.getItem();
        if (!isHarmedBy(stack, type) || amount <= 0.0F) {
            return false;
        }

        if (stack.isDamageableItem()) {
            int before = stack.getDamageValue();
            stack.hurtAndBreak(scaledDamage(stack, amount), level, null, ignored -> {});
            if (stack.isEmpty()) {
                entity.discard();
            } else {
                entity.setItem(stack);
            }
            return stack.isEmpty() || stack.getDamageValue() != before;
        }

        if (entity.getRandom().nextFloat() * 10.0F >= amount) {
            return false;
        }
        stack.shrink(1);
        if (stack.isEmpty()) {
            entity.discard();
        } else {
            entity.setItem(stack);
        }
        return true;
    }

    private static boolean damageStackWithChance(
            ServerPlayer player,
            ItemStack stack,
            R196CorrosionType type,
            float chancePerItem,
            float amount,
            EquipmentSlot slot) {
        if (stack.isEmpty() || !isHarmedBy(stack, type)) {
            return false;
        }

        boolean damaged = false;
        int itemCount = stack.getCount();
        for (int index = 0; index < itemCount && !stack.isEmpty(); index++) {
            if (player.getRandom().nextFloat() < chancePerItem) {
                damaged |= damageStack(player.level(), player, stack, type, amount, slot);
            }
        }
        return damaged;
    }

    private static boolean damageStack(
            ServerLevel level,
            ServerPlayer player,
            ItemStack stack,
            R196CorrosionType type,
            float amount,
            EquipmentSlot slot) {
        if (!isHarmedBy(stack, type) || amount <= 0.0F) {
            return false;
        }
        if (stack.isDamageableItem()) {
            int before = stack.getDamageValue();
            if (slot == null) {
                stack.hurtAndBreak(scaledDamage(stack, amount), level, player, ignored -> {});
            } else {
                stack.hurtAndBreak(scaledDamage(stack, amount), player, slot);
            }
            return stack.isEmpty() || stack.getDamageValue() != before;
        }
        if (player.getRandom().nextFloat() * 10.0F >= amount) {
            return false;
        }
        stack.shrink(1);
        return true;
    }

    private static boolean isPepsinHarmedVanillaItem(ItemStack stack) {
        return stack.is(ItemTags.WOOL)
                || stack.is(ItemTags.WOOL_CARPETS)
                || stack.has(DataComponents.FOOD)
                || stack.is(Items.LEATHER)
                || stack.is(Items.RABBIT_HIDE)
                || stack.is(Items.FEATHER)
                || stack.is(Items.PAPER)
                || stack.is(Items.BOOK)
                || stack.is(Items.WRITABLE_BOOK)
                || stack.is(Items.WRITTEN_BOOK)
                || stack.getItem() instanceof BlockItem blockItem
                        && R196GelatinousCubeRules.dissolvePeriod(
                                        blockItem.getBlock().defaultBlockState(), R196CorrosionType.PEPSIN)
                                >= 0;
    }

    private static boolean isAcidHarmedVanillaItem(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            return R196GelatinousCubeRules.dissolvePeriod(
                            blockItem.getBlock().defaultBlockState(), R196CorrosionType.ACID)
                    >= 0;
        }
        Item item = stack.getItem();
        return item != Items.FLINT
                && item != Items.OBSIDIAN
                && item != Items.QUARTZ
                && item != Items.DIAMOND
                && item != Items.EMERALD
                && item != Items.GOLD_INGOT
                && item != Items.GOLD_NUGGET
                && item != Items.NETHERITE_INGOT;
    }
}
