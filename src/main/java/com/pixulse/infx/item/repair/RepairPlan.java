package com.pixulse.infx.item.repair;

import com.pixulse.infx.item.EquipmentCategory;
import com.pixulse.infx.item.Catalog;
import com.pixulse.infx.item.EquipmentKey;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Pure, server-rechecked repair calculation for R196 metal anvils. */
public record RepairPlan(ItemStack output, int materialsUsed, int durabilityRestored, int anvilDamage) {
    public static final RepairPlan EMPTY = new RepairPlan(ItemStack.EMPTY, 0, 0, 0);

    public static RepairPlan create(
            MiteMaterial anvilMaterial, ItemStack damaged, ItemStack consumable) {
        Catalog.EquipmentEntry entry = ModItems.catalog().equipment(damaged);
        if (entry == null || damaged.getCount() != 1 || !damaged.isDamaged() || consumable.isEmpty()) {
            return EMPTY;
        }
        EquipmentKey key = entry.key();
        if (!supports(key.type())
                || !key.material().has(MiteMaterial.Flag.METAL)
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
        return new RepairPlan(output, materialsUsed, restored, anvilDamageFor(key.type(), restored));
    }

    public boolean valid() {
        return !output.isEmpty() && materialsUsed > 0 && durabilityRestored > 0 && anvilDamage > 0;
    }

    public static int anvilDamageFor(EquipmentType type, int durabilityRestored) {
        if (durabilityRestored <= 0) {
            return 0;
        }
        if (type == EquipmentType.FISHING_ROD) {
            return (int) Math.min(Integer.MAX_VALUE, (long) durabilityRestored * 22L);
        }
        return type.armorForm() == EquipmentType.ArmorForm.PLATE
                        || type.armorForm() == EquipmentType.ArmorForm.CHAIN
                        || type == EquipmentType.BOW
                ? (int) Math.min(Integer.MAX_VALUE, (long) durabilityRestored * 200L)
                : durabilityRestored;
    }

    public static int fullRepairCost(EquipmentType type) {
        if (type == EquipmentType.FISHING_ROD) {
            return 1;
        }
        if (type == EquipmentType.BOW) {
            return 2;
        }
        if (type.armorForm() == EquipmentType.ArmorForm.CHAIN) {
            return type.durabilityComponents();
        }
        return type.durabilityComponents() * 2;
    }

    public static boolean supports(EquipmentType type) {
        return type == EquipmentType.BOW
                || type.armorForm() == EquipmentType.ArmorForm.PLATE
                || type.armorForm() == EquipmentType.ArmorForm.CHAIN
                || type.category() == EquipmentCategory.TOOL
                || (type.category() == EquipmentCategory.WEAPON
                        && type != EquipmentType.ARROW);
    }

    public static boolean supportsType(ItemStack stack) {
        Catalog.EquipmentEntry entry = ModItems.catalog().equipment(stack);
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

    public static Item repairItem(MiteMaterial material) {
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
