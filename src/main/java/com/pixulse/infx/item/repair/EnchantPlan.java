package com.pixulse.infx.item.repair;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Pure, server-rechecked enchanted-book merging for INFX metal anvils.
 *
 * <p>The merge follows the vanilla anvil rules: the same enchantment on both sides adds one
 * level (capped at the enchantment maximum), different levels keep the higher one, and
 * enchantments that do not fit the target or conflict with existing ones are skipped. Only
 * INFX equipment may receive enchantments; books can never be combined onto each other. INFX
 * charges no experience: the only cost is anvil wear from the halved book fee.
 */
public record EnchantPlan(ItemStack output, int wear) {
    public static final EnchantPlan EMPTY = new EnchantPlan(ItemStack.EMPTY, 0);

    /** Merges the enchantments of an enchanted book onto a single piece of INFX equipment. */
    public static EnchantPlan create(ItemStack target, ItemStack book) {
        if (target.isEmpty() || book.isEmpty() || !book.is(Items.ENCHANTED_BOOK)
                || !RepairPlan.supportsType(target)
                || target.getCount() != 1 || book.getCount() != 1) {
            return EMPTY;
        }
        ItemEnchantments additions = EnchantmentHelper.getEnchantmentsForCrafting(book);
        if (additions.isEmpty()) {
            return EMPTY;
        }
        ItemEnchantments.Mutable merged =
                new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(target));
        long wear = 0L;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : additions.entrySet()) {
            Holder<Enchantment> holder = entry.getKey();
            if (!target.supportsEnchantment(holder) || !compatibleWithExisting(holder, merged)) {
                continue;
            }
            int level = mergedLevel(
                    merged.getLevel(holder), entry.getIntValue(), holder.value().getMaxLevel());
            merged.set(holder, level);
            wear += enchantmentFee(holder.value().getAnvilCost(), level);
        }
        if (wear <= 0) {
            return EMPTY;
        }
        ItemStack output = target.copy();
        EnchantmentHelper.setEnchantments(output, merged.toImmutable());
        return new EnchantPlan(output, (int) Math.min(Integer.MAX_VALUE, wear));
    }

    public boolean valid() {
        return !output.isEmpty() && wear > 0;
    }

    /** Same level adds one; different levels keep the higher one; capped at the maximum. */
    public static int mergedLevel(int current, int addition, int maxLevel) {
        int level = current == addition ? addition + 1 : Math.max(current, addition);
        return Math.min(level, maxLevel);
    }

    /** Books pay half the anvil cost per level, mirroring the vanilla anvil fee. */
    public static int enchantmentFee(int anvilCost, int level) {
        return Math.max(1, anvilCost / 2) * level;
    }

    private static boolean compatibleWithExisting(
            Holder<Enchantment> candidate, ItemEnchantments.Mutable merged) {
        for (Holder<Enchantment> other : merged.keySet()) {
            if (!other.equals(candidate) && !Enchantment.areCompatible(candidate, other)) {
                return false;
            }
        }
        return true;
    }
}
