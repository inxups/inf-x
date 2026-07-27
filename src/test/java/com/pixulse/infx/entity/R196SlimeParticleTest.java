package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.item.R196GelatinousSphereItem;
import org.junit.jupiter.api.Test;

class R196SlimeParticleTest {
    @Test
    void r196SlimeOverridesVanillaGreenLandingParticle() throws NoSuchMethodException {
        assertEquals(R196Slime.class, R196Slime.class.getDeclaredMethod("getParticleType").getDeclaringClass());
    }

    @Test
    void gelatinousVariantsUseTheirMatchingMiteLandingParticleColors() {
        assertEquals(R196GelatinousSphereItem.Color.GREEN, R196Slime.Variant.SLIME.landingParticleColor());
        assertEquals(R196GelatinousSphereItem.Color.OCHRE, R196Slime.Variant.JELLY.landingParticleColor());
        assertEquals(R196GelatinousSphereItem.Color.CRIMSON, R196Slime.Variant.BLOB.landingParticleColor());
        assertEquals(R196GelatinousSphereItem.Color.GRAY, R196Slime.Variant.OOZE.landingParticleColor());
        assertEquals(R196GelatinousSphereItem.Color.BLACK, R196Slime.Variant.PUDDING.landingParticleColor());
    }
}
