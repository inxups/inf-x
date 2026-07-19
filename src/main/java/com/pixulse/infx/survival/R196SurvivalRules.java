package com.pixulse.infx.survival;

import com.pixulse.infx.progression.R196Experience;

public final class R196SurvivalRules {
    public static final double INITIAL_CAP = 6.0D;

    private R196SurvivalRules() {}

    public static double healthCap(int level) {
        return INITIAL_CAP + 2.0D * Math.floorDiv(Math.max(0, Math.min(level, R196Experience.MAX_DISPLAY_LEVEL)), 5);
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
        double rate = 0.00045D;
        if (moving) rate += 0.00025D;
        if (sprinting) rate += 0.00110D;
        if (swimming) rate += 0.00130D;
        if (jumping) rate += 0.00125D;
        if (rowing) rate += 0.00050D;
        if (wet) rate *= 1.75D;
        if (wet && cold) rate *= 1.75D;
        if (malnourished) rate *= 1.5D;
        return rate;
    }

    public static double recoveryPerTick(boolean sleeping, boolean malnourished, int regenerationLevel) {
        double rate = 1.0D / 1_250.0D;
        if (sleeping) rate *= 4.0D;
        if (malnourished) rate *= 0.25D;
        rate *= 1.0D + Math.max(0, regenerationLevel) * 0.5D;
        return rate;
    }
}
