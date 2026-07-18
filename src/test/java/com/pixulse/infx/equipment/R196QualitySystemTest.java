package com.pixulse.infx.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import com.pixulse.infx.material.R196Quality;
import org.junit.jupiter.api.Test;

class R196QualitySystemTest {
    @Test
    void qualityCodecRoundTripsPersistentNames() {
        for (R196Quality quality : R196Quality.values()) {
            var encoded = R196Quality.CODEC.encodeStart(JsonOps.INSTANCE, quality).getOrThrow();
            assertEquals(new JsonPrimitive(quality.getSerializedName()), encoded);
            assertEquals(quality, R196Quality.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
        }
    }

    @Test
    void qualityDurabilityAndExperienceScaleMatchR196() {
        assertEquals(.75F, R196Quality.POOR.durabilityMultiplier());
        assertEquals(1.5F, R196Quality.FINE.durabilityMultiplier());
        assertEquals(3.5F, R196Quality.LEGENDARY.durabilityMultiplier());
        assertEquals(40, R196QualitySystem.experienceCost(100.0F, R196Quality.FINE));
        assertEquals(80, R196QualitySystem.experienceCost(100.0F, R196Quality.EXCELLENT));
        assertEquals(640, R196QualitySystem.experienceCost(100.0F, R196Quality.LEGENDARY));
    }
}
