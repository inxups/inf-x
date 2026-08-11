package com.pixulse.infx.world.agriculture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.block.InfxCropBlock;
import com.pixulse.infx.block.InfxCropType;
import com.pixulse.infx.event.AgricultureEvents;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class AgricultureRulesTest {
    @Test
    void climateAndTickIntervalFollowGrowingPlantRules() {
        assertEquals(1.0F, InfxCropBlock.temperatureGrowthRateModifier(0.8F));
        assertEquals(1.0F, InfxCropBlock.temperatureGrowthRateModifier(1.2F));
        assertEquals(0.2F, InfxCropBlock.temperatureGrowthRateModifier(0.0F), 1.0E-6F);
        assertEquals(0.0F, InfxCropBlock.temperatureGrowthRateModifier(-0.2F));
        assertEquals(1.5F, InfxCropBlock.blightChanceModifier(1.1F, true));
        assertEquals(0.0F, InfxCropBlock.blightChanceModifier(2.2F, false));
        assertEquals(26, InfxCropBlock.growthTickInterval(1.0F));
        assertEquals(Integer.MAX_VALUE, InfxCropBlock.growthTickInterval(0.0F));
    }

    @Test
    void cropTexturesUseStageMappings() {
        assertEquals(8, InfxCropType.WHEAT.textureStages());
        assertEquals(7, InfxCropType.WHEAT.deadTextureStages());
        assertEquals(0, InfxCropType.CARROTS.textureStage(0));
        assertEquals(0, InfxCropType.CARROTS.textureStage(1));
        assertEquals(1, InfxCropType.CARROTS.textureStage(3));
        assertEquals(2, InfxCropType.CARROTS.textureStage(6));
        assertEquals(3, InfxCropType.CARROTS.textureStage(7));
        assertEquals(2, InfxCropType.POTATOES.deadTextureStage(7));
        assertEquals(7, InfxCropType.BEETROOTS.maxAge());
        assertEquals(0, InfxCropType.BEETROOTS.textureStage(1));
        assertEquals(1, InfxCropType.BEETROOTS.textureStage(3));
        assertEquals(2, InfxCropType.BEETROOTS.textureStage(6));
        assertEquals(3, InfxCropType.BEETROOTS.deadTextureStage(7));
        assertEquals(4, InfxCropType.BEETROOTS.ageFromVanilla(2));
        assertEquals(7, InfxCropType.BEETROOTS.ageFromVanilla(3));
        assertEquals(0.25F, InfxCropType.CARROTS.bonusYieldChance());
        assertEquals(0.5F, InfxCropType.BEETROOTS.bonusYieldChance());
    }

    @Test
    void sugarCaneAndVinesUseBiomeAndCoordinateRules() {
        assertEquals(0.0F, AgricultureEvents.sugarCaneGrowthChance(0.2F));
        assertTrue(AgricultureEvents.sugarCaneGrowthChance(1.0F) > 0.5F);
        int length = AgricultureEvents.maximumVineLength(new BlockPos(123, 64, -456));
        assertTrue(length >= 3 && length <= 10);
    }
}
