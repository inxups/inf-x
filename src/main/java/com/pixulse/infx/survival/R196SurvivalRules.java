package com.pixulse.infx.survival;

import com.pixulse.infx.progression.R196Experience;

public final class R196SurvivalRules {
    public static final double INITIAL_CAP = 6.0D;
    public static final double MAX_CAP = 20.0D;
    public static final double BASE_METABOLISM_PER_TICK = 1.0D / 2_000.0D;
    public static final double NUTRITION_METABOLISM_PER_TICK = BASE_METABOLISM_PER_TICK * 0.25D;
    public static final double HUNGER_EFFECT_METABOLISM_PER_TICK = 0.025D / 4.0D;
    public static final double HEALING_METABOLISM = 0.25D;

    private R196SurvivalRules() {}

    public static double healthCap(int level) {
        double scaled = INITIAL_CAP
                + 2.0D * Math.floorDiv(Math.max(0, Math.min(level, R196Experience.MAX_DISPLAY_LEVEL)), 5);
        return Math.min(MAX_CAP, scaled);
    }

    public static double foodCap(int level) {
        return healthCap(level);
    }

    public static double metabolism(
            boolean moving,
            boolean sprinting,
            boolean swimming,
            boolean jumping,
            boolean rowing,
            boolean wet,
            boolean cold,
            boolean malnourished) {
        return baselineMetabolism(wet, cold, malnourished)
                + activityMetabolism(moving, sprinting, swimming, jumping, rowing);
    }

    public static double baselineMetabolism(boolean wet, boolean cold, boolean malnourished) {
        double multiplier = 1.0D;
        if (wet) multiplier += cold ? 0.5D : 0.25D;
        if (malnourished) multiplier += 0.5D;
        return BASE_METABOLISM_PER_TICK * multiplier;
    }

    public static double activityMetabolism(
            boolean moving, boolean sprinting, boolean swimming, boolean jumping, boolean rowing) {
        double rate = 0.0D;
        if (moving) rate += 0.00025D;
        if (sprinting) rate += 0.00110D;
        if (swimming) rate += 0.00130D;
        if (jumping) rate += 0.00125D;
        if (rowing) rate += 0.00050D;
        return rate;
    }

    public static double enduranceModifier(int enduranceLevel) {
        return Math.max(0.2D, 1.0D - Math.max(0, enduranceLevel) * 0.2D);
    }

    public static double hungerEffectMetabolism(int effectLevel) {
        return HUNGER_EFFECT_METABOLISM_PER_TICK * Math.max(0, effectLevel);
    }

    public static double recoveryPerTick(
            double nutrition, boolean sleeping, boolean malnourished, int regenerationLevel) {
        double rate = 4.0E-4D + Math.max(0.0D, nutrition) * 2.0E-5D;
        if (sleeping) rate *= 4.0D;
        if (malnourished) rate *= 0.25D;
        rate *= 1.0D + Math.max(0, regenerationLevel) * 0.5D;
        return rate;
    }
}
