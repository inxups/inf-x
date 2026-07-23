package com.pixulse.infx.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.registry.ModEnchantments;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class R196EnchantmentRulesTest {
    @Test
    void enchantingPowerIsPaidAsRawExperience() {
        assertEquals(0, R196EnchantmentRules.experienceCost(-1));
        assertEquals(0, R196EnchantmentRules.experienceCost(0));
        assertEquals(100, R196EnchantmentRules.experienceCost(1));
        assertEquals(3_300, R196EnchantmentRules.experienceCost(33));
        assertEquals(10_000, R196EnchantmentRules.experienceCost(100));
    }

    @Test
    void tablePowerUsesMiteTiersAndBookshelfLimit() {
        assertEquals(2, R196EnchantmentRules.enchantingTablePower(0, 2, 50));
        assertEquals(4, R196EnchantmentRules.enchantingTablePower(0, 4, 100));
        assertEquals(50, R196EnchantmentRules.enchantingTablePower(24, 2, 50));
        assertEquals(100, R196EnchantmentRules.enchantingTablePower(24, 4, 100));
        assertEquals(50, R196EnchantmentRules.enchantingTablePower(30, 2, 50));
        assertEquals(100, R196EnchantmentRules.enchantingTablePower(30, 4, 100));
    }

    @Test
    void itemEnchantabilityUsesMiteDiminishingReturns() {
        assertEquals(0, R196EnchantmentRules.effectiveEnchantmentPower(100, 0));
        assertEquals(10, R196EnchantmentRules.effectiveEnchantmentPower(10, 10));
        assertEquals(15, R196EnchantmentRules.effectiveEnchantmentPower(20, 10));
        assertEquals(18, R196EnchantmentRules.effectiveEnchantmentPower(30, 10));
        assertEquals(18, R196EnchantmentRules.effectiveEnchantmentPower(100, 10));
        assertEquals(40, R196EnchantmentRules.effectiveEnchantmentPower(50, 30));
        assertEquals(53, R196EnchantmentRules.effectiveEnchantmentPower(100, 30));
        assertEquals(45, R196EnchantmentRules.effectiveEnchantmentPower(50, 40));
        assertEquals(65, R196EnchantmentRules.effectiveEnchantmentPower(100, 40));
        assertEquals(50, R196EnchantmentRules.effectiveEnchantmentPower(50, 50));
        assertEquals(75, R196EnchantmentRules.effectiveEnchantmentPower(100, 50));
        assertEquals(50, R196EnchantmentRules.effectiveEnchantmentPower(50, 100));
        assertEquals(100, R196EnchantmentRules.effectiveEnchantmentPower(100, 100));
    }

    @Test
    void optionPowerUsesMiteFractionsAndRandomness() {
        assertEquals(23, R196EnchantmentRules.enchantmentOptionPower(100, 0, 0.0F));
        assertEquals(33, R196EnchantmentRules.enchantmentOptionPower(100, 0, 0.5F));
        assertEquals(43, R196EnchantmentRules.enchantmentOptionPower(100, 0, 1.0F));
        assertEquals(57, R196EnchantmentRules.enchantmentOptionPower(100, 1, 0.0F));
        assertEquals(67, R196EnchantmentRules.enchantmentOptionPower(100, 1, 0.5F));
        assertEquals(77, R196EnchantmentRules.enchantmentOptionPower(100, 1, 1.0F));
        assertEquals(100, R196EnchantmentRules.enchantmentOptionPower(100, 2, 0.0F));
        assertEquals(0, R196EnchantmentRules.enchantmentOptionPower(0, 2, 0.5F));
        assertEquals(1, R196EnchantmentRules.enchantmentOptionPower(1, 0, 0.0F));
    }

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

    @Test
    void selectorUsesOnlyTheFixedR196CandidatePool() {
        assertSame(ModEnchantments.R196, R196EnchantmentSelector.candidateKeys());
        assertEquals(22, R196EnchantmentSelector.candidateKeys().size());
        assertFalse(R196EnchantmentSelector.candidateKeys().contains(ModEnchantments.CLUMSINESS));
    }

    @Test
    void butcheringAndFortuneHelpersKeepTheirTargetedMiteRanges() {
        assertEquals(0, R196EnchantmentRules.butcheringExtraCount(0, RandomSource.create(1L)));
        int horseBeef = R196EnchantmentRules.horseButcheringBeefCount(3, RandomSource.create(1L));
        assertTrue(horseBeef >= 1 && horseBeef <= 5);
        assertEquals(.0F, R196EnchantmentRules.fortuneOreBonusChance(0), .0001F);
        assertEquals(.1F, R196EnchantmentRules.fortuneOreBonusChance(1), .0001F);
        assertEquals(.3F, R196EnchantmentRules.fortuneOreBonusChance(3), .0001F);
        assertEquals(.3F, R196EnchantmentRules.fortuneOreBonusChance(99), .0001F);
        assertEquals(16, R196EnchantmentRules.grassWormDenominator(0, false));
        assertEquals(13, R196EnchantmentRules.grassWormDenominator(3, false));
        assertEquals(4, R196EnchantmentRules.grassWormDenominator(0, true));
        assertEquals(2, R196EnchantmentRules.grassWormDenominator(3, true));
        int netherWartBonus = R196EnchantmentRules.netherWartFortuneBonus(3, RandomSource.create(2L));
        assertTrue(netherWartBonus >= 0 && netherWartBonus <= 3);
    }
}
