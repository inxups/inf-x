package com.pixulse.infx.world.agriculture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.block.MiteCropBlock;
import com.pixulse.infx.block.MiteCropType;
import com.pixulse.infx.data.agriculture.AgricultureData;
import com.pixulse.infx.event.AgricultureEvents;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class AgricultureRulesTest {
    @Test
    void miteClimateAndTickIntervalFollowGrowingPlantRules() {
        assertEquals(1.0F, MiteCropBlock.temperatureGrowthRateModifier(0.8F));
        assertEquals(1.0F, MiteCropBlock.temperatureGrowthRateModifier(1.2F));
        assertEquals(0.2F, MiteCropBlock.temperatureGrowthRateModifier(0.0F), 1.0E-6F);
        assertEquals(0.0F, MiteCropBlock.temperatureGrowthRateModifier(-0.2F));
        assertEquals(1.5F, MiteCropBlock.blightChanceModifier(1.1F, true));
        assertEquals(0.0F, MiteCropBlock.blightChanceModifier(2.2F, false));
        assertEquals(26, MiteCropBlock.growthTickInterval(1.0F));
        assertEquals(Integer.MAX_VALUE, MiteCropBlock.growthTickInterval(0.0F));
    }

    @Test
    void cropTexturesUseMiteStageMappings() {
        assertEquals(8, MiteCropType.WHEAT.textureStages());
        assertEquals(7, MiteCropType.WHEAT.deadTextureStages());
        assertEquals(0, MiteCropType.CARROTS.textureStage(0));
        assertEquals(0, MiteCropType.CARROTS.textureStage(1));
        assertEquals(1, MiteCropType.CARROTS.textureStage(3));
        assertEquals(2, MiteCropType.CARROTS.textureStage(6));
        assertEquals(3, MiteCropType.CARROTS.textureStage(7));
        assertEquals(2, MiteCropType.POTATOES.deadTextureStage(7));
        assertEquals(7, MiteCropType.BEETROOTS.maxAge());
        assertEquals(0, MiteCropType.BEETROOTS.textureStage(1));
        assertEquals(1, MiteCropType.BEETROOTS.textureStage(3));
        assertEquals(2, MiteCropType.BEETROOTS.textureStage(6));
        assertEquals(3, MiteCropType.BEETROOTS.deadTextureStage(7));
        assertEquals(4, MiteCropType.BEETROOTS.ageFromVanilla(2));
        assertEquals(7, MiteCropType.BEETROOTS.ageFromVanilla(3));
        assertEquals(0.25F, MiteCropType.CARROTS.bonusYieldChance());
        assertEquals(0.5F, MiteCropType.BEETROOTS.bonusYieldChance());
    }

    @Test
    void fertilityPersistsUntilTheCropConsumesIt() {
        AgricultureData data = new AgricultureData();
        BlockPos farmland = new BlockPos(3, 64, -7);
        assertTrue(data.fertilize(farmland, 0L));
        assertTrue(data.isFertile(farmland));
        assertTrue(data.consumeFertility(farmland));
        assertFalse(data.isFertile(farmland));
        assertFalse(data.consumeFertility(farmland));
    }

    @Test
    void sugarCaneAndVinesUseBiomeAndCoordinateRules() {
        assertEquals(0.0F, AgricultureEvents.sugarCaneGrowthChance(0.2F));
        assertTrue(AgricultureEvents.sugarCaneGrowthChance(1.0F) > 0.5F);
        int length = AgricultureEvents.maximumVineLength(new BlockPos(123, 64, -456));
        assertTrue(length >= 3 && length <= 10);
    }
}
