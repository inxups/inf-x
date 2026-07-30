package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.item.GelatinousSphereItem;
import org.junit.jupiter.api.Test;

class SlimeParticleTest {
    @Test
    void r196SlimeOverridesVanillaGreenLandingParticle() throws NoSuchMethodException {
        assertEquals(InfxSlime.class, InfxSlime.class.getDeclaredMethod("getParticleType").getDeclaringClass());
    }

    @Test
    void gelatinousVariantsUseTheirMatchingMiteLandingParticleColors() {
        assertEquals(GelatinousSphereItem.Color.GREEN, InfxSlime.Variant.SLIME.landingParticleColor());
        assertEquals(GelatinousSphereItem.Color.OCHRE, InfxSlime.Variant.JELLY.landingParticleColor());
        assertEquals(GelatinousSphereItem.Color.CRIMSON, InfxSlime.Variant.BLOB.landingParticleColor());
        assertEquals(GelatinousSphereItem.Color.GRAY, InfxSlime.Variant.OOZE.landingParticleColor());
        assertEquals(GelatinousSphereItem.Color.BLACK, InfxSlime.Variant.PUDDING.landingParticleColor());
    }
}
