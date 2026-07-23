package com.pixulse.infx.enchantment;

import net.minecraft.util.RandomSource;

/**
 * Numeric R196 enchantment rules kept independent from the registry and game-event APIs.
 *
 * <p>The original implementation expresses most effects as a fraction of an enchantment's
 * maximum level. Keeping those fractions here prevents registration changes from silently
 * changing combat, harvesting, or survival behavior.</p>
 */
public final class R196EnchantmentRules {
    private static final int EXPERIENCE_PER_ENCHANTMENT_POWER = 100;
    private static final int MAX_ACCESSIBLE_BOOKSHELVES = 24;
    public static final int BOOK_ENCHANTABILITY = 30;
    public static final int STANDARD_MAX_LEVEL = 5;
    public static final int BUTCHERING_MAX_LEVEL = 3;
    public static final int FORTUNE_MAX_LEVEL = 3;
    public static final int FREE_MOVEMENT_MAX_LEVEL = 4;
    public static final int ENDURANCE_MAX_LEVEL = 4;
    public static final int PROTECTION_MAX_LEVEL = 4;

    private R196EnchantmentRules() {}

    public static int experienceCost(int enchantmentPower) {
        long cost = Math.max(0L, enchantmentPower) * EXPERIENCE_PER_ENCHANTMENT_POWER;
        return (int) Math.min(Integer.MAX_VALUE, cost);
    }

    public static int enchantingTablePower(int bookshelfCount, int powerPerShelf, int maximumPower) {
        if (powerPerShelf <= 0 || maximumPower <= 0) return 0;
        int shelves = Math.clamp(bookshelfCount, 0, MAX_ACCESSIBLE_BOOKSHELVES);
        long power = (long) (shelves + 1) * powerPerShelf;
        return (int) Math.min(maximumPower, power);
    }

    public static int effectiveEnchantmentPower(int tablePower, int itemEnchantability) {
        if (tablePower <= 0 || itemEnchantability <= 0) return 0;
        if (tablePower <= itemEnchantability) return tablePower;

        int halfPowerLevels = Math.min(tablePower, itemEnchantability * 2) - itemEnchantability;
        int quarterPowerLevels = Math.max(
                0,
                Math.min(tablePower, itemEnchantability * 3) - itemEnchantability * 2);
        return Math.round(itemEnchantability + halfPowerLevels * 0.5F + quarterPowerLevels * 0.25F);
    }

    public static int enchantmentOptionPower(int effectivePower, int slot, float randomFraction) {
        if (slot < 0 || slot > 2) throw new IllegalArgumentException("Enchantment slot must be between 0 and 2");
        if (effectivePower <= 0) return 0;
        float fraction = (slot + 1) / 3.0F;
        if (slot < 2) {
            fraction += (randomFraction - 0.5F) * 0.2F;
        }
        return Math.max(Math.round(effectivePower * fraction), 1);
    }

    public static float levelFraction(int level, int maximumLevel) {
        if (maximumLevel <= 0) return 0.0F;
        return Math.clamp(level, 0, maximumLevel) / (float) maximumLevel;
    }

    public static float durabilityNegationChance(int level) {
        return levelFraction(level, STANDARD_MAX_LEVEL) * 0.75F;
    }

    public static int quicknessPullTicks(int level) {
        return 20 - Math.round(10.0F * levelFraction(level, STANDARD_MAX_LEVEL));
    }

    public static int quicknessAdjustedUseTicks(int usedTicks, int level) {
        if (usedTicks <= 0) return 0;
        return Math.round(usedTicks * 20.0F / quicknessPullTicks(level));
    }

    public static int precisionArcheryLevelBonus(int level) {
        return Math.round(40.0F * levelFraction(level, STANDARD_MAX_LEVEL));
    }

    public static float precisionUncertaintyMultiplier(int experienceLevel, int precisionLevel) {
        if (precisionLevel <= 0) return 1.0F;
        float baseline = trueFlightWander(experienceLevel);
        return baseline <= 0.0F
                ? 1.0F
                : trueFlightWander(experienceLevel + precisionArcheryLevelBonus(precisionLevel)) / baseline;
    }

    public static float trueFlightWander(int effectiveArcheryLevel) {
        if (effectiveArcheryLevel < 0) return 5.0F - effectiveArcheryLevel * 0.5F;
        float denominator = 0.8F + (effectiveArcheryLevel + 1) / 5.0F;
        return (float) (0.5D + 4.5D / (denominator * denominator));
    }

    public static float poisonChance(int level) {
        return levelFraction(level, STANDARD_MAX_LEVEL);
    }

    public static int poisonDuration(int level) {
        return 160 + Math.round(240.0F * levelFraction(level, STANDARD_MAX_LEVEL));
    }

    public static float stunningChance(int level) {
        return levelFraction(level, STANDARD_MAX_LEVEL) * 0.5F;
    }

    public static int stunningDuration(int level) {
        return Math.max(0, level) * 50;
    }

    public static int stunningAmplifier(int level) {
        return Math.max(0, level) * 5;
    }

    public static float disarmingChance(int level) {
        return levelFraction(level, STANDARD_MAX_LEVEL);
    }

    public static float vampirismChance(int level) {
        return levelFraction(level, STANDARD_MAX_LEVEL) * 0.5F;
    }

    public static float slaughterDamageBonus(int level) {
        return Math.clamp(level, 0, STANDARD_MAX_LEVEL);
    }

    public static int vampirismHealing(float inflictedDamage, float randomFraction) {
        if (inflictedDamage <= 0.0F) return 0;
        return Math.max(1, (int) (inflictedDamage * 0.5F * Math.clamp(randomFraction, 0.0F, 1.0F)));
    }

    public static float arrowRecoveryChance(float materialChance, int recoveryLevel) {
        float enchantmentChance = levelFraction(recoveryLevel, STANDARD_MAX_LEVEL);
        return enchantmentChance + Math.clamp(materialChance, 0.0F, 1.0F) * (1.0F - enchantmentChance);
    }

    public static float harvestingBonusChance(int level) {
        return levelFraction(level, STANDARD_MAX_LEVEL) * 0.5F;
    }

    public static int harvestingBonusCount(int existingCount, int level, RandomSource random) {
        int extra = 0;
        float chance = harvestingBonusChance(level);
        for (int unit = 0; unit < Math.max(0, existingCount); unit++) {
            if (random.nextFloat() < chance) {
                extra++;
            }
        }
        return extra;
    }

    public static int butcheringExtraCount(int level, RandomSource random) {
        int cappedLevel = Math.clamp(level, 0, BUTCHERING_MAX_LEVEL);
        return cappedLevel == 0 ? 0 : random.nextInt(cappedLevel + 1);
    }

    /** Horses do not have vanilla meat drops, so retain MITE's complete horse-beef roll. */
    public static int horseButcheringBeefCount(int level, RandomSource random) {
        int cappedLevel = Math.clamp(level, 0, BUTCHERING_MAX_LEVEL);
        return 1 + random.nextInt(cappedLevel + 1) + random.nextInt(2);
    }

    public static boolean butcheringAddsSpiderEye(int level, RandomSource random) {
        int cappedLevel = Math.clamp(level, 0, BUTCHERING_MAX_LEVEL);
        return cappedLevel > 0 && random.nextInt(cappedLevel + 1) > 0;
    }

    public static int fortuneOreBonusCount(int existingCount, int level, RandomSource random) {
        int extra = 0;
        float chance = fortuneOreBonusChance(level);
        for (int unit = 0; unit < Math.max(0, existingCount); unit++) {
            if (random.nextFloat() < chance) {
                extra++;
            }
        }
        return extra;
    }

    public static float fortuneOreBonusChance(int level) {
        return Math.clamp(level, 0, FORTUNE_MAX_LEVEL) * 0.1F;
    }

    public static int grassWormDenominator(int fortuneLevel, boolean raining) {
        int fortune = Math.clamp(fortuneLevel, 0, FORTUNE_MAX_LEVEL);
        if (raining) {
            fortune += 12;
        }
        return 16 - Math.min(fortune, 14);
    }

    public static int netherWartFortuneBonus(int level, RandomSource random) {
        int cappedLevel = Math.clamp(level, 0, FORTUNE_MAX_LEVEL);
        return cappedLevel == 0 ? 0 : random.nextInt(cappedLevel + 1);
    }

    public static int baitingLureDelay(int delay, int level) {
        int adjusted = Math.max(1, delay);
        for (int index = 0; index < Math.clamp(level, 0, STANDARD_MAX_LEVEL); index++) {
            adjusted = Math.max(1, adjusted * 9 / 10);
        }
        return adjusted;
    }

    public static float fertilityChance(int level) {
        return levelFraction(level, STANDARD_MAX_LEVEL);
    }

    public static int treeFellingExtraLogs(int level) {
        return Math.clamp(level, 0, STANDARD_MAX_LEVEL);
    }

    public static float penetrationPoints(int level) {
        return levelFraction(level, STANDARD_MAX_LEVEL) * 5.0F;
    }

    public static float protectionBonus(float armorProtection, int level) {
        return Math.max(0.0F, armorProtection)
                * levelFraction(level, PROTECTION_MAX_LEVEL)
                * 0.5F;
    }

    public static float freeMovementResistance(int level) {
        return levelFraction(level, FREE_MOVEMENT_MAX_LEVEL) * 0.8F;
    }

    public static double freeMovementAdjustedImpairment(double baseAmount, int level) {
        return baseAmount * (1.0D - freeMovementResistance(level));
    }

    public static float reducedImpairmentMultiplier(float baseMultiplier, int freeMovementLevel) {
        float base = Math.clamp(baseMultiplier, 0.0F, 1.0F);
        return base + (1.0F - base) * freeMovementResistance(freeMovementLevel);
    }
}
