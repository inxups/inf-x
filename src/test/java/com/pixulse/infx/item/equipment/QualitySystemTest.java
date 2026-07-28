package com.pixulse.infx.item.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import com.pixulse.infx.item.material.Quality;
import org.junit.jupiter.api.Test;

class QualitySystemTest {
    @Test
    void qualityCodecRoundTripsPersistentNames() {
        for (Quality quality : Quality.values()) {
            var encoded = Quality.CODEC.encodeStart(JsonOps.INSTANCE, quality).getOrThrow();
            assertEquals(new JsonPrimitive(quality.getSerializedName()), encoded);
            assertEquals(quality, Quality.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
        }
    }

    @Test
    void qualityDurabilityAndExperienceScaleMatchR196() {
        assertEquals(.5F, Quality.WRETCHED.durabilityMultiplier());
        assertEquals(.75F, Quality.POOR.durabilityMultiplier());
        assertEquals(1.5F, Quality.FINE.durabilityMultiplier());
        assertEquals(3.5F, Quality.LEGENDARY.durabilityMultiplier());
        assertEquals(40, QualitySystem.experienceCost(100.0F, Quality.FINE));
        assertEquals(80, QualitySystem.experienceCost(100.0F, Quality.EXCELLENT));
        assertEquals(640, QualitySystem.experienceCost(100.0F, Quality.LEGENDARY));
        assertEquals(80, QualitySystem.experienceCost(100.0F, Quality.FINE, true));
    }

    @Test
    void qualitySelectorExposesBothBelowAverageR196Grades() {
        assertEquals(
                Quality.FINE,
                QualitySystem.nextSelectableQuality(null, Quality.FINE, 40, 100.0F));
        assertEquals(
                Quality.WRETCHED,
                QualitySystem.nextSelectableQuality(Quality.FINE, Quality.FINE, 40, 100.0F));
        assertEquals(
                Quality.POOR,
                QualitySystem.nextSelectableQuality(Quality.WRETCHED, Quality.FINE, 0, 100.0F));
        assertNull(QualitySystem.nextSelectableQuality(
                Quality.POOR, Quality.FINE, 0, 100.0F));
        assertEquals(
                Quality.WRETCHED,
                QualitySystem.nextSelectableQuality(
                        null, Quality.FINE, 40, 100.0F, true));
    }

    @Test
    void clumsinessUsesMitesOriginalNegativeIntegerDivisionForMinimumQuality() {
        assertEquals(
                QualitySystem.toCode(Quality.WRETCHED),
                QualitySystem.clumsyFallbackCode(0, true));
        assertEquals(
                QualitySystem.toCode(Quality.POOR),
                QualitySystem.clumsyFallbackCode(1, true));
        assertEquals(
                QualitySystem.toCode(Quality.POOR),
                QualitySystem.clumsyFallbackCode(10, true));
        assertEquals(QualitySystem.AVERAGE_CODE, QualitySystem.clumsyFallbackCode(11, true));
        assertEquals(QualitySystem.AVERAGE_CODE, QualitySystem.clumsyFallbackCode(20, true));
        assertEquals(QualitySystem.AVERAGE_CODE, QualitySystem.clumsyFallbackCode(0, false));
    }
}
