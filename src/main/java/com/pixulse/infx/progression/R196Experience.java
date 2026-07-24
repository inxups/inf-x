package com.pixulse.infx.progression;

import net.minecraft.world.entity.player.Player;

/** Pure R196 experience-curve and debt calculations. */
public final class R196Experience {
    public static final int MAX_DISPLAY_LEVEL = 200;
    public static final int MAX_DEBT_LEVEL = 40;
    public static final int XP_AT_DISPLAY_CAP = cumulativeForLevel(MAX_DISPLAY_LEVEL);
    public static final int MIN_TOTAL_EXPERIENCE = -MAX_DEBT_LEVEL * 20;

    private R196Experience() {}

    public static int cumulativeForLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        long value = 5L * level * level + 15L * level;
        return (int) Math.min(Integer.MAX_VALUE, value);
    }

    public static int pointsToNextLevel(int level) {
        if (level < 0) {
            return 20;
        }
        if (level >= MAX_DISPLAY_LEVEL) {
            return 1;
        }
        return 10 * level + 20;
    }

    public static int levelForTotal(int totalExperience) {
        if (totalExperience < 0) {
            return Math.max(-MAX_DEBT_LEVEL, Math.floorDiv(totalExperience, 20));
        }
        if (totalExperience >= XP_AT_DISPLAY_CAP) {
            return MAX_DISPLAY_LEVEL;
        }
        double root = (-15.0 + Math.sqrt(225.0 + 20.0 * totalExperience)) / 10.0;
        int estimate = Math.max(0, (int) Math.floor(root));
        while (estimate < MAX_DISPLAY_LEVEL
                && cumulativeForLevel(estimate + 1) <= totalExperience) {
            estimate++;
        }
        while (estimate > 0 && cumulativeForLevel(estimate) > totalExperience) {
            estimate--;
        }
        return estimate;
    }

    public static float progressForTotal(int totalExperience, int level) {
        if (totalExperience < 0 || level < 0) {
            int debtRemainder = Math.floorMod(-totalExperience, 20);
            return debtRemainder == 0 ? 0.0F : (20 - debtRemainder) / 20.0F;
        }
        if (level >= MAX_DISPLAY_LEVEL) {
            return 1.0F;
        }
        int base = cumulativeForLevel(level);
        return Math.clamp((totalExperience - base) / (float) pointsToNextLevel(level), 0.0F, 0.999999F);
    }

    public static void setTotal(Player player, int requestedTotal) {
        int total = Math.max(MIN_TOTAL_EXPERIENCE, requestedTotal);
        int level = levelForTotal(total);
        player.totalExperience = total;
        player.experienceLevel = level;
        player.experienceProgress = progressForTotal(total, level);
    }

    public static void add(Player player, int amount) {
        long total = (long) player.totalExperience + amount;
        setTotal(player, (int) Math.clamp(total, MIN_TOTAL_EXPERIENCE, Integer.MAX_VALUE));
    }

    public static void addLevels(Player player, int amount) {
        int current = player.experienceLevel;
        int target = Math.clamp(current + amount, -MAX_DEBT_LEVEL, MAX_DISPLAY_LEVEL);
        if (target < 0) {
            setTotal(player, target * 20);
            return;
        }
        int progressPoints = current >= 0 && current < MAX_DISPLAY_LEVEL
                ? Math.round(player.experienceProgress * pointsToNextLevel(current))
                : 0;
        setTotal(player, cumulativeForLevel(target) + Math.min(progressPoints, pointsToNextLevel(target) - 1));
    }

    public static int deathTotal(int previousTotal) {
        if (previousTotal >= cumulativeForLevel(1)) {
            return 0;
        }
        return Math.max(MIN_TOTAL_EXPERIENCE, previousTotal - 20);
    }

    public static int droppedOnDeath(int totalExperience) {
        return Math.max(0, totalExperience) / 3;
    }

    /**
     * R196 {@code EnumLevelBonus.HARVESTING}/{@code CRAFTING}: {@code level × 0.02},
     * including negative debt levels.
     */
    public static float harvestOrCraftLevelBonus(int level) {
        return clampedLevel(level) * 0.02F;
    }

    /**
     * R196 {@code EnumLevelBonus.MELEE_DAMAGE}: {@code level × 0.005} while positive,
     * otherwise the same {@code level × 0.02} debt penalty as harvest/craft.
     */
    public static float meleeLevelBonus(int level) {
        int capped = clampedLevel(level);
        return capped > 0 ? capped * 0.005F : capped * 0.02F;
    }

    public static float harvestOrCraftMultiplier(int level) {
        return 1.0F + harvestOrCraftLevelBonus(level);
    }

    public static float meleeMultiplier(int level) {
        return 1.0F + meleeLevelBonus(level);
    }

    private static int clampedLevel(int level) {
        return Math.clamp(level, -MAX_DEBT_LEVEL, MAX_DISPLAY_LEVEL);
    }
}
