package com.pixulse.infx.harvest;

public final class ToolWearCalculator {
    public static final float PICKAXE_DECAY = 1.0F;
    public static final float FLINT_HATCHET_DECAY = 4.0F / 3.0F;

    private ToolWearCalculator() {}

    public static int damageForBreaking(float blockHardness, float baseDecayRate) {
        if (blockHardness <= 0.0F) {
            return 0;
        }

        float scaledDecay = 100.0F * baseDecayRate;
        int hardnessDamage = (int) (blockHardness * scaledDecay);
        int minimumDamage = (int) (scaledDecay / 20.0F);
        return Math.max(Math.max(hardnessDamage, minimumDamage), 1);
    }
}
