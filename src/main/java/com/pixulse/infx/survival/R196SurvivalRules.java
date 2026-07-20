package com.pixulse.infx.survival;

import com.pixulse.infx.progression.R196Experience;

public final class R196SurvivalRules {
    public static final double INITIAL_CAP = 6.0D;
    public static final double MAX_CAP = 20.0D;
    public static final double BASE_METABOLISM_PER_TICK = 1.0D / 2_000.0D;
    public static final double NUTRITION_METABOLISM_PER_TICK = BASE_METABOLISM_PER_TICK * 0.25D;
    public static final double HUNGER_EFFECT_METABOLISM_PER_TICK = 0.025D / 4.0D;
    public static final double WALK_METABOLISM_PER_BLOCK = 0.01D / 4.0D;
    public static final double SPRINT_METABOLISM_PER_BLOCK = 0.05D / 4.0D;
    public static final double SWIM_METABOLISM_PER_BLOCK = 0.015D / 4.0D;
    public static final double CLIMB_METABOLISM_PER_BLOCK = 0.1D / 4.0D;
    public static final double JUMP_METABOLISM = 0.2D / 4.0D;
    public static final double SPRINT_JUMP_METABOLISM = 0.8D / 4.0D;
    public static final double ROW_METABOLISM_PER_TICK = 0.01D / 4.0D;
    public static final double ATTACK_METABOLISM = 0.3D / 4.0D;
    public static final double MINING_METABOLISM_PER_TICK = 0.01D / 4.0D;
    public static final double BOW_DRAW_METABOLISM_PER_TICK = 0.01D / 4.0D;
    public static final double DAMAGE_METABOLISM = 0.3D / 4.0D;
    public static final double HEALING_METABOLISM = 0.25D;
    private static final double MAX_PLACEMENT_HARDNESS = 20.0D;

    private R196SurvivalRules() {}

    public static double healthCap(int level) {
        double scaled = INITIAL_CAP
                + 2.0D * Math.floorDiv(Math.max(0, Math.min(level, R196Experience.MAX_DISPLAY_LEVEL)), 5);
        return Math.min(MAX_CAP, scaled);
    }

    public static double foodCap(int level) {
        return healthCap(level);
    }

    public static double baselineMetabolism(boolean wet, boolean cold, boolean malnourished) {
        double multiplier = 1.0D;
        if (wet) multiplier += cold ? 0.5D : 0.25D;
        if (malnourished) multiplier += 0.5D;
        return BASE_METABOLISM_PER_TICK * multiplier;
    }

    public static double movementMetabolism(
            int walkCentimeters,
            int crouchCentimeters,
            int sprintCentimeters,
            int swimCentimeters,
            int underwaterCentimeters,
            int onWaterCentimeters,
            int climbCentimeters) {
        double walkingBlocks = centimeters(walkCentimeters) + centimeters(crouchCentimeters);
        double swimmingBlocks = centimeters(swimCentimeters)
                + centimeters(underwaterCentimeters)
                + centimeters(onWaterCentimeters);
        return walkingBlocks * WALK_METABOLISM_PER_BLOCK
                + centimeters(sprintCentimeters) * SPRINT_METABOLISM_PER_BLOCK
                + swimmingBlocks * SWIM_METABOLISM_PER_BLOCK
                + centimeters(climbCentimeters) * CLIMB_METABOLISM_PER_BLOCK;
    }

    public static double jumpMetabolism(boolean sprinting) {
        return sprinting ? SPRINT_JUMP_METABOLISM : JUMP_METABOLISM;
    }

    public static double placementMetabolism(double hardness) {
        return Math.min(Math.max(0.0D, hardness), MAX_PLACEMENT_HARDNESS) / 4.0D;
    }

    public static double tillingMetabolism(double hardness) {
        return Math.max(0.0D, hardness) / 8.0D;
    }

    public static double enduranceModifier(int enduranceLevel) {
        return Math.max(0.2D, 1.0D - Math.max(0, enduranceLevel) * 0.2D);
    }

    public static double hungerEffectMetabolism(int effectLevel) {
        return HUNGER_EFFECT_METABOLISM_PER_TICK * Math.max(0, effectLevel);
    }

    private static double centimeters(int value) {
        return Math.max(0, value) / 100.0D;
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
