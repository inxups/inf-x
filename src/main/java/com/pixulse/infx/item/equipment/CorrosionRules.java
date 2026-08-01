package com.pixulse.infx.item.equipment;

import com.pixulse.infx.entity.GelatinousCubeRules;
import com.pixulse.infx.item.InfxBucketItem;
import com.pixulse.infx.item.Catalog;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXItems;
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

/** INFX item damage rules shared by gelatinous cubes, their projectiles, and dropped items. */
public final class CorrosionRules {
    private static final float TOOL_DAMAGE_SCALE = 100.0F;
    private static final float ARMOR_DAMAGE_SCALE = 2.0F;
    private static final String ITEM_ENTITY_CORROSION_HEALTH = "infx_mite_corrosion_health";

    private CorrosionRules() {}

    public static boolean isHarmedBy(InfxMaterial material, CorrosionType type) {
        return switch (type) {
            case PEPSIN -> material == InfxMaterial.LEATHER;
            case ACID -> switch (material) {
                case FLINT, OBSIDIAN, GOLD, MITHRIL, ADAMANTIUM -> false;
                default -> true;
            };
        };
    }

    public static boolean isHarmedBy(ItemStack stack, CorrosionType type) {
        if (stack.isEmpty()) {
            return false;
        }

        Catalog.EquipmentEntry equipment = InfXItems.catalog().equipment(stack);
        if (equipment != null) {
            return isHarmedBy(equipment, type);
        }
        Catalog.RawEntry raw = InfXItems.catalog().raw(stack.getItem());
        if (raw != null && raw.definition().material().isPresent()) {
            return isHarmedBy(raw.definition().material().orElseThrow(), type);
        }
        if (stack.getItem() instanceof InfxBucketItem bucket) {
            return isHarmedBy(bucket.material(), type);
        }

        if (type == CorrosionType.PEPSIN) {
            return isPepsinHarmedVanillaItem(stack);
        }
        return isAcidHarmedVanillaItem(stack);
    }

    /** MITE treats the material list as an OR: a wooden handle or shaft remains acid-sensitive. */
    private static boolean isHarmedBy(Catalog.EquipmentEntry equipment, CorrosionType type) {
        return isHarmedBy(equipment.key().material(), type)
                || hasMiteWoodenComponent(equipment.key().type()) && isHarmedBy(InfxMaterial.WOOD, type);
    }

    static boolean hasMiteWoodenComponent(EquipmentType type) {
        return type.armorForm() == EquipmentType.ArmorForm.NONE && type != EquipmentType.SHEARS;
    }

    public static int scaledDamage(ItemStack stack, float amount) {
        if (!stack.isDamageableItem() || amount <= 0.0F) {
            return 0;
        }
        Catalog.EquipmentEntry equipment = InfXItems.catalog().equipment(stack);
        if (equipment == null) {
            return Math.max(1, Math.round(amount));
        }
        EquipmentType type = equipment.key().type();
        EquipmentType.ArmorForm armor = type.armorForm();
        if (type == EquipmentType.BOW || type == EquipmentType.FISHING_ROD) {
            return Math.max(1, Math.round(amount));
        }
        float scale = armor == EquipmentType.ArmorForm.PLATE || armor == EquipmentType.ArmorForm.CHAIN
                ? ARMOR_DAMAGE_SCALE
                : TOOL_DAMAGE_SCALE;
        return Math.max(1, Math.round(amount * scale));
    }

    public static boolean damageHeldItem(ServerPlayer player, CorrosionType type, float amount) {
        if (player.hasInfiniteMaterials()) {
            return false;
        }
        return damageStack(player.level(), player, player.getMainHandItem(), type, amount, EquipmentSlot.MAINHAND);
    }

    public static boolean damageInventory(
            ServerPlayer player, CorrosionType type, float chancePerItem, float amount) {
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

    public static boolean damageItemEntity(ItemEntity entity, CorrosionType type, float amount) {
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

        int health = entity.getPersistentData().getIntOr(ITEM_ENTITY_CORROSION_HEALTH, 5);
        health -= Math.max(1, Math.round(amount));
        if (health <= 0) {
            entity.getPersistentData().remove(ITEM_ENTITY_CORROSION_HEALTH);
            entity.discard();
        } else {
            entity.getPersistentData().putInt(ITEM_ENTITY_CORROSION_HEALTH, health);
        }
        return true;
    }

    private static boolean damageStackWithChance(
            ServerPlayer player,
            ItemStack stack,
            CorrosionType type,
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
            CorrosionType type,
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
                        && GelatinousCubeRules.dissolvePeriod(
                                        blockItem.getBlock().defaultBlockState(), CorrosionType.PEPSIN)
                                >= 0;
    }

    private static boolean isAcidHarmedVanillaItem(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            return GelatinousCubeRules.dissolvePeriod(
                            blockItem.getBlock().defaultBlockState(), CorrosionType.ACID)
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
