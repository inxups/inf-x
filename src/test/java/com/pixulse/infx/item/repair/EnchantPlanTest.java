package com.pixulse.infx.item.repair;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EnchantPlanTest {
    @Test
    void sameLevelMergesToTheNextLevel() {
        assertEquals(2, EnchantPlan.mergedLevel(1, 1, 5));
        assertEquals(3, EnchantPlan.mergedLevel(2, 2, 5));
    }

    @Test
    void differentLevelsKeepTheHigherOne() {
        assertEquals(4, EnchantPlan.mergedLevel(2, 4, 5));
        assertEquals(4, EnchantPlan.mergedLevel(4, 2, 5));
    }

    @Test
    void mergeCapsAtTheEnchantmentMaximum() {
        assertEquals(5, EnchantPlan.mergedLevel(5, 5, 5));
        assertEquals(5, EnchantPlan.mergedLevel(5, 1, 5));
    }

    @Test
    void booksPayHalfTheAnvilCostPerLevel() {
        assertEquals(4, EnchantPlan.enchantmentFee(8, 1));
        assertEquals(8, EnchantPlan.enchantmentFee(8, 2));
        assertEquals(1, EnchantPlan.enchantmentFee(1, 1));
    }
}
