package com.pixulse.infx.agriculture;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class R196AgricultureRulesTest {
    @Test
    void climateClustersFertilityAndMoistureAllAffectGrowth() {
        float isolatedDry = R196AgricultureEvents.growthMultiplier(
                0.8F, 0.7F, false, false, false, false);
        float tendedPatch = R196AgricultureEvents.growthMultiplier(
                0.8F, 0.7F, true, true, true, true);
        float cold = R196AgricultureEvents.growthMultiplier(
                0.0F, 0.4F, true, false, false, true);
        assertTrue(tendedPatch > isolatedDry);
        assertTrue(tendedPatch > cold);
        assertTrue(isolatedDry <= 0.6F);
    }

    @Test
    void sugarCaneAndVinesUseBiomeAndCoordinateRules() {
        assertEquals(0.0F, R196AgricultureEvents.sugarCaneGrowthChance(0.2F));
        assertTrue(R196AgricultureEvents.sugarCaneGrowthChance(1.0F) > 0.5F);
        int length = R196AgricultureEvents.maximumVineLength(new net.minecraft.core.BlockPos(123, 64, -456));
        assertTrue(length >= 3 && length <= 10);
    }
}
