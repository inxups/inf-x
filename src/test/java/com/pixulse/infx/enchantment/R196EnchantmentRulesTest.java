package com.pixulse.infx.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class R196EnchantmentRulesTest {
    @Test
    void maximumLevelsAndDurabilityFollowR196Fractions() {
        assertEquals(5, R196EnchantmentRules.STANDARD_MAX_LEVEL);
        assertEquals(3, R196EnchantmentRules.BUTCHERING_MAX_LEVEL);
        assertEquals(3, R196EnchantmentRules.FORTUNE_MAX_LEVEL);
        assertEquals(4, R196EnchantmentRules.FREE_MOVEMENT_MAX_LEVEL);
        assertEquals(.15F, R196EnchantmentRules.durabilityNegationChance(1), .0001F);
        assertEquals(.75F, R196EnchantmentRules.durabilityNegationChance(5), .0001F);
    }

    @Test
    void bowRulesUseR196PullAndTrueFlightScaling() {
        assertEquals(20, R196EnchantmentRules.quicknessPullTicks(0));
        assertEquals(18, R196EnchantmentRules.quicknessPullTicks(1));
        assertEquals(10, R196EnchantmentRules.quicknessPullTicks(5));
        assertEquals(20, R196EnchantmentRules.quicknessAdjustedUseTicks(10, 5));
        assertEquals(8, R196EnchantmentRules.precisionArcheryLevelBonus(1));
        assertEquals(40, R196EnchantmentRules.precisionArcheryLevelBonus(5));
        assertTrue(R196EnchantmentRules.precisionUncertaintyMultiplier(0, 1) < .25F);
        assertEquals(5.0F, R196EnchantmentRules.trueFlightWander(0), .0001F);
    }

    @Test
    void combatEffectsUseSourceChancesAndMagnitudes() {
        assertEquals(.2F, R196EnchantmentRules.poisonChance(1), .0001F);
        assertEquals(208, R196EnchantmentRules.poisonDuration(1));
        assertEquals(400, R196EnchantmentRules.poisonDuration(5));
        assertEquals(.1F, R196EnchantmentRules.stunningChance(1), .0001F);
        assertEquals(.5F, R196EnchantmentRules.stunningChance(5), .0001F);
        assertEquals(250, R196EnchantmentRules.stunningDuration(5));
        assertEquals(25, R196EnchantmentRules.stunningAmplifier(5));
        assertEquals(.2F, R196EnchantmentRules.disarmingChance(1), .0001F);
        assertEquals(.5F, R196EnchantmentRules.vampirismChance(5), .0001F);
        assertEquals(1.0F, R196EnchantmentRules.slaughterDamageBonus(1), .0001F);
        assertEquals(5.0F, R196EnchantmentRules.slaughterDamageBonus(5), .0001F);
        assertEquals(1, R196EnchantmentRules.vampirismHealing(1.0F, 0.0F));
        assertEquals(2, R196EnchantmentRules.vampirismHealing(10.0F, .5F));
    }

    @Test
    void gatheringArmorAndFishingRulesUseR196Fractions() {
        assertEquals(.44F, R196EnchantmentRules.arrowRecoveryChance(.3F, 1), .0001F);
        assertEquals(1.0F, R196EnchantmentRules.arrowRecoveryChance(.3F, 5), .0001F);
        assertEquals(.1F, R196EnchantmentRules.harvestingBonusChance(1), .0001F);
        assertEquals(.5F, R196EnchantmentRules.harvestingBonusChance(5), .0001F);
        assertEquals(540, R196EnchantmentRules.baitingLureDelay(600, 1));
        assertEquals(353, R196EnchantmentRules.baitingLureDelay(600, 5));
        assertEquals(1.0F, R196EnchantmentRules.fertilityChance(5), .0001F);
        assertEquals(5, R196EnchantmentRules.treeFellingExtraLogs(5));
        assertEquals(5.0F, R196EnchantmentRules.penetrationPoints(5), .0001F);
        assertEquals(4.0F, R196EnchantmentRules.protectionBonus(8.0F, 4), .0001F);
        assertEquals(.8F, R196EnchantmentRules.freeMovementResistance(4), .0001F);
        assertEquals(-.2D, R196EnchantmentRules.freeMovementAdjustedImpairment(-1.0D, 4), .0001D);
        assertEquals(.82F, R196EnchantmentRules.reducedImpairmentMultiplier(.1F, 4), .0001F);
    }
}
