package com.pixulse.infx.item.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.registry.InfXEnchantments;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class EnchantmentRulesTest {
    @Test
    void enchantingPowerIsPaidAsRawExperience() {
        assertEquals(0, EnchantmentRules.experienceCost(-1));
        assertEquals(0, EnchantmentRules.experienceCost(0));
        assertEquals(100, EnchantmentRules.experienceCost(1));
        assertEquals(3_300, EnchantmentRules.experienceCost(33));
        assertEquals(10_000, EnchantmentRules.experienceCost(100));
    }

    @Test
    void tablePowerUsesMiteTiersAndBookshelfLimit() {
        assertEquals(2, EnchantmentRules.enchantingTablePower(0, 2, 50));
        assertEquals(4, EnchantmentRules.enchantingTablePower(0, 4, 100));
        assertEquals(50, EnchantmentRules.enchantingTablePower(24, 2, 50));
        assertEquals(100, EnchantmentRules.enchantingTablePower(24, 4, 100));
        assertEquals(50, EnchantmentRules.enchantingTablePower(30, 2, 50));
        assertEquals(100, EnchantmentRules.enchantingTablePower(30, 4, 100));
    }

    @Test
    void itemEnchantabilityUsesMiteDiminishingReturns() {
        assertEquals(0, EnchantmentRules.effectiveEnchantmentPower(100, 0));
        assertEquals(10, EnchantmentRules.effectiveEnchantmentPower(10, 10));
        assertEquals(15, EnchantmentRules.effectiveEnchantmentPower(20, 10));
        assertEquals(18, EnchantmentRules.effectiveEnchantmentPower(30, 10));
        assertEquals(18, EnchantmentRules.effectiveEnchantmentPower(100, 10));
        assertEquals(40, EnchantmentRules.effectiveEnchantmentPower(50, 30));
        assertEquals(53, EnchantmentRules.effectiveEnchantmentPower(100, 30));
        assertEquals(45, EnchantmentRules.effectiveEnchantmentPower(50, 40));
        assertEquals(65, EnchantmentRules.effectiveEnchantmentPower(100, 40));
        assertEquals(50, EnchantmentRules.effectiveEnchantmentPower(50, 50));
        assertEquals(75, EnchantmentRules.effectiveEnchantmentPower(100, 50));
        assertEquals(50, EnchantmentRules.effectiveEnchantmentPower(50, 100));
        assertEquals(100, EnchantmentRules.effectiveEnchantmentPower(100, 100));
    }

    @Test
    void optionPowerUsesMiteFractionsAndRandomness() {
        assertEquals(23, EnchantmentRules.enchantmentOptionPower(100, 0, 0.0F));
        assertEquals(33, EnchantmentRules.enchantmentOptionPower(100, 0, 0.5F));
        assertEquals(43, EnchantmentRules.enchantmentOptionPower(100, 0, 1.0F));
        assertEquals(57, EnchantmentRules.enchantmentOptionPower(100, 1, 0.0F));
        assertEquals(67, EnchantmentRules.enchantmentOptionPower(100, 1, 0.5F));
        assertEquals(77, EnchantmentRules.enchantmentOptionPower(100, 1, 1.0F));
        assertEquals(100, EnchantmentRules.enchantmentOptionPower(100, 2, 0.0F));
        assertEquals(0, EnchantmentRules.enchantmentOptionPower(0, 2, 0.5F));
        assertEquals(1, EnchantmentRules.enchantmentOptionPower(1, 0, 0.0F));
    }

    @Test
    void maximumLevelsAndDurabilityFollowR196Fractions() {
        assertEquals(5, EnchantmentRules.STANDARD_MAX_LEVEL);
        assertEquals(3, EnchantmentRules.BUTCHERING_MAX_LEVEL);
        assertEquals(3, EnchantmentRules.FORTUNE_MAX_LEVEL);
        assertEquals(4, EnchantmentRules.FREE_MOVEMENT_MAX_LEVEL);
        assertEquals(.15F, EnchantmentRules.durabilityNegationChance(1), .0001F);
        assertEquals(.75F, EnchantmentRules.durabilityNegationChance(5), .0001F);
    }

    /** The registered item_damage effect is built from this constant, so both must stay in step. */
    @Test
    void durabilityNegationScalesLinearlyFromThePerLevelConstant() {
        assertEquals(.15F, EnchantmentRules.DURABILITY_NEGATION_PER_LEVEL, .0001F);
        for (int level = 0; level <= EnchantmentRules.STANDARD_MAX_LEVEL; level++) {
            assertEquals(
                    level * EnchantmentRules.DURABILITY_NEGATION_PER_LEVEL,
                    EnchantmentRules.durabilityNegationChance(level),
                    .0001F);
        }
    }

    @Test
    void bowRulesUseR196PullAndTrueFlightScaling() {
        assertEquals(20, EnchantmentRules.quicknessPullTicks(0));
        assertEquals(18, EnchantmentRules.quicknessPullTicks(1));
        assertEquals(10, EnchantmentRules.quicknessPullTicks(5));
        assertEquals(20, EnchantmentRules.quicknessAdjustedUseTicks(10, 5));
        assertEquals(8, EnchantmentRules.precisionArcheryLevelBonus(1));
        assertEquals(40, EnchantmentRules.precisionArcheryLevelBonus(5));
        assertTrue(EnchantmentRules.precisionUncertaintyMultiplier(0, 1) < .25F);
        assertEquals(5.0F, EnchantmentRules.trueFlightWander(0), .0001F);
    }

    @Test
    void combatEffectsUseSourceChancesAndMagnitudes() {
        assertEquals(.2F, EnchantmentRules.poisonChance(1), .0001F);
        assertEquals(208, EnchantmentRules.poisonDuration(1));
        assertEquals(400, EnchantmentRules.poisonDuration(5));
        assertEquals(.1F, EnchantmentRules.stunningChance(1), .0001F);
        assertEquals(.5F, EnchantmentRules.stunningChance(5), .0001F);
        assertEquals(250, EnchantmentRules.stunningDuration(5));
        assertEquals(25, EnchantmentRules.stunningAmplifier(5));
        assertEquals(.2F, EnchantmentRules.disarmingChance(1), .0001F);
        assertEquals(.5F, EnchantmentRules.vampirismChance(5), .0001F);
        assertEquals(1.0F, EnchantmentRules.slaughterDamageBonus(1), .0001F);
        assertEquals(5.0F, EnchantmentRules.slaughterDamageBonus(5), .0001F);
        assertEquals(1, EnchantmentRules.vampirismHealing(1.0F, 0.0F));
        assertEquals(2, EnchantmentRules.vampirismHealing(10.0F, .5F));
    }

    @Test
    void gatheringArmorAndFishingRulesUseR196Fractions() {
        assertEquals(.44F, EnchantmentRules.arrowRecoveryChance(.3F, 1), .0001F);
        assertEquals(1.0F, EnchantmentRules.arrowRecoveryChance(.3F, 5), .0001F);
        assertEquals(.1F, EnchantmentRules.harvestingBonusChance(1), .0001F);
        assertEquals(.5F, EnchantmentRules.harvestingBonusChance(5), .0001F);
        assertEquals(540, EnchantmentRules.baitingLureDelay(600, 1));
        assertEquals(353, EnchantmentRules.baitingLureDelay(600, 5));
        assertEquals(1.0F, EnchantmentRules.fertilityChance(5), .0001F);
        assertEquals(5, EnchantmentRules.treeFellingExtraLogs(5));
        assertEquals(5.0F, EnchantmentRules.penetrationPoints(5), .0001F);
        assertEquals(4.0F, EnchantmentRules.protectionBonus(8.0F, 4), .0001F);
        assertEquals(.8F, EnchantmentRules.freeMovementResistance(4), .0001F);
        assertEquals(-.2D, EnchantmentRules.freeMovementAdjustedImpairment(-1.0D, 4), .0001D);
        assertEquals(.82F, EnchantmentRules.reducedImpairmentMultiplier(.1F, 4), .0001F);
    }

    @Test
    void selectorUsesOnlyTheFixedR196CandidatePool() {
        assertSame(InfXEnchantments.ALL, EnchantmentSelector.candidateKeys());
        assertEquals(39, EnchantmentSelector.candidateKeys().size());
        assertTrue(EnchantmentSelector.candidateKeys().containsAll(InfXEnchantments.INFX));
        assertTrue(EnchantmentSelector.candidateKeys().containsAll(InfXEnchantments.VANILLA_R196));
        assertFalse(EnchantmentSelector.candidateKeys().contains(InfXEnchantments.CLUMSINESS));
    }

    @Test
    void vanillaMiteRulesFollowTheOriginalFormulas() {
        assertEquals(0.15F, EnchantmentRules.FIRE_PROTECTION_BURN_REDUCTION_PER_LEVEL, .0001F);
        assertEquals(2.0F, EnchantmentRules.SMITE_DAMAGE_PER_LEVEL, .0001F);
        assertEquals(2.0F, EnchantmentRules.typedProtectionPoints(8.0F, 1), .0001F);
        assertEquals(8.0F, EnchantmentRules.typedProtectionPoints(8.0F, 4), .0001F);
        assertEquals(0.0F, EnchantmentRules.typedProtectionPoints(8.0F, 0), .0001F);
        assertEquals(15.0F, EnchantmentRules.featherFallingPoints(4, 1.0F), .0001F);
        assertEquals(3.75F, EnchantmentRules.featherFallingPoints(1, 1.0F), .0001F);
        assertEquals(7.5F, EnchantmentRules.featherFallingPoints(4, 0.5F), .0001F);
        assertEquals(0.15F, EnchantmentRules.thornsChance(1), .0001F);
        assertEquals(0.45F, EnchantmentRules.thornsChance(3), .0001F);
        assertEquals(0.45F, EnchantmentRules.thornsChance(9), .0001F);
        RandomSource random = RandomSource.create(1L);
        for (int roll = 0; roll < 20; roll++) {
            int damage = EnchantmentRules.thornsDamage(3, random);
            assertTrue(damage >= 1 && damage <= 4, "thorns damage " + damage);
        }
        assertEquals(11, EnchantmentRules.thornsDamage(21, random));
        assertEquals(3, EnchantmentRules.thornsArmorWear(true));
        assertEquals(1, EnchantmentRules.thornsArmorWear(false));
    }

    @Test
    void butcheringAndFortuneHelpersKeepTheirTargetedMiteRanges() {
        assertEquals(0, EnchantmentRules.butcheringExtraCount(0, RandomSource.create(1L)));
        int horseBeef = EnchantmentRules.horseButcheringBeefCount(3, RandomSource.create(1L));
        assertTrue(horseBeef >= 1 && horseBeef <= 5);
        assertEquals(.0F, EnchantmentRules.fortuneOreBonusChance(0), .0001F);
        assertEquals(.1F, EnchantmentRules.fortuneOreBonusChance(1), .0001F);
        assertEquals(.3F, EnchantmentRules.fortuneOreBonusChance(3), .0001F);
        assertEquals(.3F, EnchantmentRules.fortuneOreBonusChance(99), .0001F);
        assertEquals(16, EnchantmentRules.grassWormDenominator(0, false));
        assertEquals(13, EnchantmentRules.grassWormDenominator(3, false));
        assertEquals(4, EnchantmentRules.grassWormDenominator(0, true));
        assertEquals(2, EnchantmentRules.grassWormDenominator(3, true));
        int netherWartBonus = EnchantmentRules.netherWartFortuneBonus(3, RandomSource.create(2L));
        assertTrue(netherWartBonus >= 0 && netherWartBonus <= 3);
    }
}
