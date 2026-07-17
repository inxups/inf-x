package com.pixulse.infx.harvest;

import java.util.function.IntConsumer;

public final class ToolWearApplication {
    private ToolWearApplication() {}

    public static void afterHarvestSnapshot(float blockHardness, float baseDecayRate, IntConsumer damageTool) {
        int damage = ToolWearCalculator.damageForBreaking(blockHardness, baseDecayRate);
        if (damage > 0) {
            damageTool.accept(damage);
        }
    }
}
