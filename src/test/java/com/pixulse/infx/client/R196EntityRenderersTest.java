package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.entity.R196Slime;
import org.junit.jupiter.api.Test;

class R196EntityRenderersTest {
    @Test
    void gelatinousVariantsUseTheirMatchingMiteEntityTextures() {
        assertEquals(
                "infx:textures/entity/slime/slime.png",
                R196EntityRenderers.SlimeTexture.textureFor(R196Slime.Variant.SLIME).toString());
        assertEquals(
                "infx:textures/entity/slime/jelly.png",
                R196EntityRenderers.SlimeTexture.textureFor(R196Slime.Variant.JELLY).toString());
        assertEquals(
                "infx:textures/entity/slime/blob.png",
                R196EntityRenderers.SlimeTexture.textureFor(R196Slime.Variant.BLOB).toString());
        assertEquals(
                "infx:textures/entity/slime/ooze.png",
                R196EntityRenderers.SlimeTexture.textureFor(R196Slime.Variant.OOZE).toString());
        assertEquals(
                "infx:textures/entity/slime/pudding.png",
                R196EntityRenderers.SlimeTexture.textureFor(R196Slime.Variant.PUDDING).toString());
        assertEquals(
                "infx:textures/entity/slime/magmacube.png",
                R196EntityRenderers.MagmaCubeTexture.texture().toString());
    }
}
