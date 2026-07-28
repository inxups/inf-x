package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.item.GelatinousSphereItem;
import org.junit.jupiter.api.Test;

class SlimeParticleTest {
    @Test
    void r196SlimeOverridesVanillaGreenLandingParticle() throws NoSuchMethodException {
        assertEquals(MiteSlime.class, MiteSlime.class.getDeclaredMethod("getParticleType").getDeclaringClass());
    }

    @Test
    void gelatinousVariantsUseTheirMatchingMiteLandingParticleColors() {
        assertEquals(GelatinousSphereItem.Color.GREEN, MiteSlime.Variant.SLIME.landingParticleColor());
        assertEquals(GelatinousSphereItem.Color.OCHRE, MiteSlime.Variant.JELLY.landingParticleColor());
        assertEquals(GelatinousSphereItem.Color.CRIMSON, MiteSlime.Variant.BLOB.landingParticleColor());
        assertEquals(GelatinousSphereItem.Color.GRAY, MiteSlime.Variant.OOZE.landingParticleColor());
        assertEquals(GelatinousSphereItem.Color.BLACK, MiteSlime.Variant.PUDDING.landingParticleColor());
    }
}
