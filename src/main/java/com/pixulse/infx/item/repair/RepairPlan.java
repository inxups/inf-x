package com.pixulse.infx.item.repair;

import com.pixulse.infx.item.Catalog;
import com.pixulse.infx.item.EquipmentCategory;
import com.pixulse.infx.item.EquipmentKey;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.equipment.QualitySystem;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXDataComponents;
import com.pixulse.infx.registry.InfXItems;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/** Pure, server-rechecked calculation for INFX metal anvils. */
public record RepairPlan(
        ItemStack output,
        int materialsUsed,
        int durabilityRestored,
        int anvilDamage,
        Operation operation,
        boolean consumesAdditional) {
    public static final int MAX_NAME_LENGTH = 40;

    public enum Operation {
        NONE,
        MATERIAL,
        COMBINATION,
        ENCHANTMENT,
        DISENCHANTMENT,
        RENAME
    }

    public static final RepairPlan EMPTY =
            new RepairPlan(ItemStack.EMPTY, 0, 0, 0, Operation.NONE, false);

    /** Keeps the old pure repair API available to callers and tests. */
    public RepairPlan(ItemStack output, int materialsUsed, int durabilityRestored, int anvilDamage) {
        this(
                output,
                materialsUsed,
                durabilityRestored,
                anvilDamage,
                output.isEmpty() ? Operation.NONE : Operation.MATERIAL,
                materialsUsed > 0);
    }

    public static RepairPlan create(
            InfxMaterial anvilMaterial, ItemStack damaged, ItemStack consumable) {
        return create(anvilMaterial, null, damaged, consumable, null);
    }

    public static RepairPlan create(
            InfxMaterial anvilMaterial,
            Player player,
            ItemStack first,
            ItemStack second,
            String itemName) {
        if (anvilMaterial == null || first.isEmpty() || first.getCount() != 1) {
            return EMPTY;
        }

        String validatedName = itemName == null ? null : normalizeName(itemName);
        if (itemName != null && validatedName == null) {
            return EMPTY;
        }

        RepairPlan primary = createPrimary(anvilMaterial, player, first, second);
        boolean hasSecondInput = !second.isEmpty();
        if (hasSecondInput && !primary.valid()) {
            return EMPTY;
        }

        ItemStack output = primary.valid() ? primary.output().copy() : first.copy();
        boolean renamed = applyName(output, first, validatedName);
        if (!primary.valid() && !renamed) {
            return EMPTY;
        }

        Operation operation = primary.valid() ? primary.operation() : Operation.RENAME;
        return new RepairPlan(
                output,
                primary.materialsUsed(),
                primary.durabilityRestored(),
                primary.anvilDamage(),
                operation,
                primary.consumesAdditional());
    }

    public boolean valid() {
        return !output.isEmpty() && operation != Operation.NONE;
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
        Catalog.EquipmentEntry entry = InfXItems.catalog().equipment(stack);
        return entry != null && supports(entry.key().type());
    }

    /** Matches the MITE second-slot rule for repair, enchantment and disenchantment inputs. */
    public static boolean isAdditionalItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (isRepairMaterial(stack.getItem())
                || stack.is(Items.ENCHANTED_BOOK)
                || stack.is(InfXItems.BOTTLE_OF_DISENCHANTING)) {
            return true;
        }
        return supportsType(stack)
                && stack.isDamageableItem()
                && !EnchantmentHelper.hasAnyEnchantments(stack);
    }

    public static boolean isRepairMaterial(Item item) {
        return item == Items.COPPER_NUGGET
                || item == Items.GOLD_NUGGET
                || item == Items.IRON_NUGGET
                || item == InfXItems.SILVER_NUGGET.get()
                || item == InfXItems.catalog().raw("ancient_metal_nugget").holder().get()
                || item == InfXItems.MITHRIL_NUGGET.get()
                || item == InfXItems.ADAMANTIUM_NUGGET.get();
    }

    public static Item repairItem(InfxMaterial material) {
        return switch (material) {
            case COPPER -> Items.COPPER_NUGGET;
            case SILVER -> InfXItems.SILVER_NUGGET.get();
            case GOLD -> Items.GOLD_NUGGET;
            case RUSTED_IRON, IRON -> Items.IRON_NUGGET;
            case ANCIENT_METAL -> InfXItems.catalog().raw("ancient_metal_nugget").holder().get();
            case MITHRIL -> InfXItems.MITHRIL_NUGGET.get();
            case ADAMANTIUM -> InfXItems.ADAMANTIUM_NUGGET.get();
            default -> Items.AIR;
        };
    }

    private static RepairPlan createPrimary(
            InfxMaterial anvilMaterial, Player player, ItemStack first, ItemStack second) {
        Catalog.EquipmentEntry entry = InfXItems.catalog().equipment(first);
        if (entry != null && supports(entry.key().type())) {
            EquipmentKey key = entry.key();
            RepairPlan material = createMaterial(anvilMaterial, player, first, second, key);
            if (material.valid()) {
                return material;
            }

            RepairPlan combination = createCombination(anvilMaterial, player, first, second, key);
            if (combination.valid()) {
                return combination;
            }
        }

        RepairPlan enchantment = createEnchantment(first, second);
        if (enchantment.valid()) {
            return enchantment;
        }

        return createDisenchantment(first, second);
    }

    private static RepairPlan createMaterial(
            InfxMaterial anvilMaterial,
            Player player,
            ItemStack first,
            ItemStack second,
            EquipmentKey key) {
        if (!first.isDamaged()
                || !key.material().has(InfxMaterial.Flag.METAL)
                || anvilMaterial.durabilityMultiplier() < key.material().durabilityMultiplier()
                || second.isEmpty()
                || second.getItem() != repairItem(key.material())) {
            return EMPTY;
        }
        if (player != null && !QualitySystem.canRepair(first, player)) {
            return EMPTY;
        }

        int fullRepairCost = fullRepairCost(key.type());
        if (fullRepairCost <= 0) {
            return EMPTY;
        }
        int repairPerItem = Math.max(1, first.getMaxDamage() / fullRepairCost);
        int damage = first.getDamageValue();
        long materialsNeeded = Math.max(1L, ((long) damage + repairPerItem - 1L) / repairPerItem);
        if (materialsNeeded > 1L && materialsNeeded * repairPerItem > damage) {
            materialsNeeded--;
        }
        int materialsUsed = Math.min(second.getCount(), (int) Math.min(Integer.MAX_VALUE, materialsNeeded));
        int outputDamage = (int) Math.max(0L, (long) damage - (long) materialsUsed * repairPerItem);
        int restored = damage - outputDamage;
        if (restored <= 0 || materialsUsed <= 0) {
            return EMPTY;
        }

        ItemStack output = first.copy();
        output.setDamageValue(outputDamage);
        return new RepairPlan(
                output,
                materialsUsed,
                restored,
                anvilDamageFor(key.type(), restored),
                Operation.MATERIAL,
                true);
    }

    private static RepairPlan createCombination(
            InfxMaterial anvilMaterial,
            Player player,
            ItemStack first,
            ItemStack second,
            EquipmentKey key) {
        if (!first.isDamaged()
                || second.isEmpty()
                || !sameCombinationItem(first, second)
                || EnchantmentHelper.hasAnyEnchantments(second)) {
            return EMPTY;
        }
        if (!EnchantmentHelper.hasAnyEnchantments(first)
                && (!first.isDamaged() || !second.isDamaged())) {
            return EMPTY;
        }
        if (key.material().has(InfxMaterial.Flag.METAL)
                && anvilMaterial.durabilityMultiplier() < key.material().durabilityMultiplier()) {
            return EMPTY;
        }
        if (player != null && !QualitySystem.canRepair(first, player)) {
            return EMPTY;
        }

        int maxDamage = first.getMaxDamage();
        int remaining = Math.min(
                maxDamage,
                (maxDamage - first.getDamageValue()) + (maxDamage - second.getDamageValue()));
        int outputDamage = maxDamage - remaining;
        if (outputDamage >= first.getDamageValue()) {
            return EMPTY;
        }

        ItemStack output = first.copy();
        output.setDamageValue(outputDamage);
        return new RepairPlan(output, 0, first.getDamageValue() - outputDamage, 0, Operation.COMBINATION, true);
    }

    private static RepairPlan createEnchantment(ItemStack first, ItemStack second) {
        if (second.isEmpty()
                || !second.is(Items.ENCHANTED_BOOK)
                || !EnchantmentHelper.canStoreEnchantments(first)
                || EnchantmentHelper.hasAnyEnchantments(first)) {
            return EMPTY;
        }

        ItemEnchantments bookEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(second);
        if (bookEnchantments.isEmpty()) {
            return EMPTY;
        }

        ItemStack output = first.copy();
        ItemEnchantments.Mutable enchantments =
                new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(output));
        boolean applied = false;
        for (var entry : bookEnchantments.entrySet()) {
            Holder<Enchantment> holder = entry.getKey();
            Enchantment enchantment = holder.value();
            if (!first.supportsEnchantment(holder)) {
                continue;
            }
            int level = Math.min(entry.getIntValue(), enchantment.getMaxLevel());
            if (level > 0) {
                enchantments.set(holder, level);
                applied = true;
            }
        }
        if (!applied) {
            return EMPTY;
        }

        EnchantmentHelper.setEnchantments(output, enchantments.toImmutable());
        return new RepairPlan(output, 0, 0, 0, Operation.ENCHANTMENT, true);
    }

    private static RepairPlan createDisenchantment(ItemStack first, ItemStack second) {
        if (second.isEmpty()
                || !second.is(InfXItems.BOTTLE_OF_DISENCHANTING)
                || first.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty()) {
            return EMPTY;
        }

        ItemStack output = first.copy();
        output.remove(DataComponents.ENCHANTMENTS);
        return new RepairPlan(output, 0, 0, 0, Operation.DISENCHANTMENT, true);
    }

    private static boolean sameCombinationItem(ItemStack first, ItemStack second) {
        return first.is(second.getItem())
                && Objects.equals(
                        first.get(InfXDataComponents.QUALITY.get()),
                        second.get(InfXDataComponents.QUALITY.get()));
    }

    private static boolean applyName(ItemStack output, ItemStack first, String itemName) {
        if (itemName == null) {
            return false;
        }
        if (StringUtil.isBlank(itemName)) {
            if (!first.has(DataComponents.CUSTOM_NAME)) {
                return false;
            }
            output.remove(DataComponents.CUSTOM_NAME);
            return true;
        }
        if (itemName.equals(first.getHoverName().getString())) {
            return false;
        }
        output.set(DataComponents.CUSTOM_NAME, Component.literal(itemName));
        return true;
    }

    /** Applies the same filtering, trimming, and length limit as the MITE anvil text field. */
    public static String normalizeName(String name) {
        String filtered = StringUtil.filterText(name == null ? "" : name).trim();
        return filtered.length() <= MAX_NAME_LENGTH ? filtered : null;
    }
}
