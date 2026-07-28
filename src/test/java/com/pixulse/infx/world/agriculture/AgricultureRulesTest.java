package com.pixulse.infx.world.agriculture;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.event.AgricultureEvents;
import com.pixulse.infx.world.MoonPhase;

import org.junit.jupiter.api.Test;

class AgricultureRulesTest {
    @Test
    void climateClustersFertilityAndMoistureAllAffectGrowth() {
        float isolatedDry = AgricultureEvents.growthMultiplier(
                0.8F, 0.7F, false, false, false, false);
        float tendedPatch = AgricultureEvents.growthMultiplier(
                0.8F, 0.7F, true, true, true, true);
        float cold = AgricultureEvents.growthMultiplier(
                0.0F, 0.4F, true, false, false, true);
        assertTrue(tendedPatch > isolatedDry);
        assertTrue(tendedPatch > cold);
        assertTrue(isolatedDry <= 0.6F);
    }

    @Test
    void sugarCaneAndVinesUseBiomeAndCoordinateRules() {
        assertEquals(0.0F, AgricultureEvents.sugarCaneGrowthChance(0.2F));
        assertTrue(AgricultureEvents.sugarCaneGrowthChance(1.0F) > 0.5F);
        int length = AgricultureEvents.maximumVineLength(new net.minecraft.core.BlockPos(123, 64, -456));
        assertTrue(length >= 3 && length <= 10);
    }

    @Test
    void offlineGrowthAndBloodMoonDiseaseDifferBetweenIntegratedAndDedicatedServers() {
        long twoStages = 2L * 30L * 60L * 1_000L;
        assertEquals(2, AgricultureData.offlineStages(twoStages, true, 30L * 60L * 1_000L, 7));
        assertEquals(0, AgricultureData.offlineStages(twoStages, false, 30L * 60L * 1_000L, 7));
        assertEquals(512, AgricultureEvents.diseaseChance(MoonPhase.BLOOD, false));
        assertEquals(4096, AgricultureEvents.diseaseChance(MoonPhase.BLOOD, true));
        assertEquals(4096, AgricultureEvents.diseaseChance(MoonPhase.FULL, false));
    }
}
