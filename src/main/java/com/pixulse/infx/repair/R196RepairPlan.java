package com.pixulse.infx.repair;

import com.pixulse.infx.item.R196Catalog;
import com.pixulse.infx.item.R196EquipmentKey;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Pure, server-rechecked repair calculation for R196 metal anvils. */
public record R196RepairPlan(ItemStack output, int materialsUsed, int durabilityRestored, int anvilDamage) {
    public static final R196RepairPlan EMPTY = new R196RepairPlan(ItemStack.EMPTY, 0, 0, 0);

    public static R196RepairPlan create(
            R196Material anvilMaterial, ItemStack damaged, ItemStack consumable) {
        R196Catalog.EquipmentEntry entry = ModItems.catalog().equipment(damaged);
        if (entry == null || damaged.getCount() != 1 || !damaged.isDamaged() || consumable.isEmpty()) {
            return EMPTY;
        }
        R196EquipmentKey key = entry.key();
        if (!supports(key.type())
                || !key.material().has(R196Material.Flag.METAL)
                || anvilMaterial.durabilityMultiplier() < key.material().durabilityMultiplier()
                || consumable.getItem() != repairItem(key.material())) {
            return EMPTY;
        }

        int fullRepairCost = fullRepairCost(key.type());
        if (fullRepairCost <= 0) {
            return EMPTY;
        }
        int repairPerItem = Math.max(1, damaged.getMaxDamage() / fullRepairCost);
        long materialsNeeded = Math.max(
                1L, ((long) damaged.getDamageValue() + repairPerItem - 1L) / repairPerItem);
        int materialsUsed = Math.min(
                consumable.getCount(), (int) Math.min(Integer.MAX_VALUE, materialsNeeded));
        int outputDamage = (int) Math.max(
                0L, (long) damaged.getDamageValue() - (long) materialsUsed * repairPerItem);
        int restored = damaged.getDamageValue() - outputDamage;
        if (restored <= 0) {
            return EMPTY;
        }

        ItemStack output = damaged.copy();
        output.setDamageValue(outputDamage);
        return new R196RepairPlan(output, materialsUsed, restored, anvilDamageFor(key.type(), restored));
    }

    public boolean valid() {
        return !output.isEmpty() && materialsUsed > 0 && durabilityRestored > 0 && anvilDamage > 0;
    }

    public static int anvilDamageFor(R196EquipmentType type, int durabilityRestored) {
        if (durabilityRestored <= 0) {
            return 0;
        }
        if (type == R196EquipmentType.FISHING_ROD) {
            return (int) Math.min(Integer.MAX_VALUE, (long) durabilityRestored * 22L);
        }
        return type.armorForm() == R196EquipmentType.ArmorForm.PLATE
                        || type.armorForm() == R196EquipmentType.ArmorForm.CHAIN
                        || type == R196EquipmentType.BOW
                ? (int) Math.min(Integer.MAX_VALUE, (long) durabilityRestored * 200L)
                : durabilityRestored;
    }

    public static int fullRepairCost(R196EquipmentType type) {
        if (type == R196EquipmentType.FISHING_ROD) {
            return 1;
        }
        if (type == R196EquipmentType.BOW) {
            return 2;
        }
        if (type.armorForm() == R196EquipmentType.ArmorForm.CHAIN) {
            return type.durabilityComponents();
        }
        return type.durabilityComponents() * 2;
    }

    public static boolean supports(R196EquipmentType type) {
        return type == R196EquipmentType.BOW
                || type.armorForm() == R196EquipmentType.ArmorForm.PLATE
                || type.armorForm() == R196EquipmentType.ArmorForm.CHAIN
                || type.category() == com.pixulse.infx.item.R196EquipmentCategory.TOOL
                || (type.category() == com.pixulse.infx.item.R196EquipmentCategory.WEAPON
                        && type != R196EquipmentType.ARROW);
    }

    public static boolean supportsType(ItemStack stack) {
        R196Catalog.EquipmentEntry entry = ModItems.catalog().equipment(stack);
        return entry != null && supports(entry.key().type());
    }

    public static boolean isRepairMaterial(Item item) {
        return item == Items.COPPER_NUGGET
                || item == Items.GOLD_NUGGET
                || item == Items.IRON_NUGGET
                || item == ModItems.SILVER_NUGGET.get()
                || item == ModItems.catalog().raw("ancient_metal_nugget").holder().get()
                || item == ModItems.MITHRIL_NUGGET.get()
                || item == ModItems.ADAMANTIUM_NUGGET.get();
    }

    public static Item repairItem(R196Material material) {
        return switch (material) {
            case COPPER -> Items.COPPER_NUGGET;
            case SILVER -> ModItems.SILVER_NUGGET.get();
            case GOLD -> Items.GOLD_NUGGET;
            case RUSTED_IRON, IRON -> Items.IRON_NUGGET;
            case ANCIENT_METAL -> ModItems.catalog().raw("ancient_metal_nugget").holder().get();
            case MITHRIL -> ModItems.MITHRIL_NUGGET.get();
            case ADAMANTIUM -> ModItems.ADAMANTIUM_NUGGET.get();
            default -> Items.AIR;
        };
    }
}
