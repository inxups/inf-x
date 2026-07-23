package com.pixulse.infx.enchantment;

import com.pixulse.infx.registry.ModEnchantments;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/** Selects table enchantments with R196's unbounded-strength rules. */
public final class R196EnchantmentSelector {
    private static final int FOLLOW_UP_ENCHANTMENT_COST = 5;
    private static final int MAX_ENCHANTMENTS_PER_OPTION = 3;

    private R196EnchantmentSelector() {}

    /**
     * MITE's table uses a strength floor for an enchantment level, rather than vanilla's bounded
     * level range. This keeps a 100-strength option eligible for its highest applicable levels.
     */
    public static List<EnchantmentInstance> select(
            RandomSource random, RegistryAccess registryAccess, ItemStack stack, int enchantmentPower) {
        if (enchantmentPower <= 0 || !stack.isEnchantable()) return List.of();

        int remainingPower = Math.max(
                1,
                (int) (enchantmentPower * (1.0F + (random.nextFloat() - 0.5F) * 0.5F)));
        boolean book = stack.is(Items.BOOK);
        List<Holder<Enchantment>> candidates = new ArrayList<>();
        HolderGetter<Enchantment> enchantments = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
        candidateKeys().forEach(key -> candidates.add(enchantments.getOrThrow(key)));
        List<EnchantmentInstance> selected = new ArrayList<>();

        while (remainingPower > 0) {
            List<EnchantmentInstance> available = availableEnchantments(candidates, stack, book, remainingPower);
            if (available.isEmpty()) break;

            EnchantmentInstance choice = WeightedRandom
                    .getRandomItem(random, available, instance -> instance.enchantment().value().getWeight())
                    .orElseThrow();
            if (selected.size() < 2 && available.size() > 1 && choice.level() > 1 && random.nextInt(2) == 0) {
                choice = new EnchantmentInstance(choice.enchantment(), random.nextInt(choice.level()) + 1);
            }
            selected.add(choice);
            remainingPower -= choice.enchantment().value().getMinCost(choice.level());
            remainingPower -= FOLLOW_UP_ENCHANTMENT_COST;
            if (remainingPower < FOLLOW_UP_ENCHANTMENT_COST || selected.size() >= MAX_ENCHANTMENTS_PER_OPTION) break;

            EnchantmentInstance selectedChoice = choice;
            candidates.removeIf(candidate -> candidate.equals(selectedChoice.enchantment())
                    || !Enchantment.areCompatible(selectedChoice.enchantment(), candidate));
        }

        shuffle(random, selected);
        return book && !selected.isEmpty()
                ? List.of(selected.get(random.nextInt(selected.size())))
                : List.copyOf(selected);
    }

    private static List<EnchantmentInstance> availableEnchantments(
            List<Holder<Enchantment>> candidates, ItemStack stack, boolean book, int remainingPower) {
        List<EnchantmentInstance> available = new ArrayList<>();
        for (Holder<Enchantment> candidate : candidates) {
            Enchantment enchantment = candidate.value();
            if (!book && !enchantment.isSupportedItem(stack)) continue;
            for (int level = enchantment.getMaxLevel(); level >= enchantment.getMinLevel(); level--) {
                if (enchantment.getMinCost(level) <= remainingPower) {
                    available.add(new EnchantmentInstance(candidate, level));
                    break;
                }
            }
        }
        return available;
    }

    static List<ResourceKey<Enchantment>> candidateKeys() {
        return ModEnchantments.R196;
    }

    private static void shuffle(RandomSource random, List<EnchantmentInstance> enchantments) {
        for (int index = enchantments.size(); index > 1; index--) {
            int selectedIndex = random.nextInt(index);
            EnchantmentInstance selected = enchantments.get(index - 1);
            enchantments.set(index - 1, enchantments.get(selectedIndex));
            enchantments.set(selectedIndex, selected);
        }
    }
}
