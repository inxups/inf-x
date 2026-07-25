package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.pixulse.infx.entity.R196Bat;
import com.pixulse.infx.entity.R196Creeper;
import com.pixulse.infx.entity.R196Silverfish;
import com.pixulse.infx.entity.R196Skeleton;
import com.pixulse.infx.entity.R196Slime;
import com.pixulse.infx.entity.R196Spider;
import com.pixulse.infx.entity.R196Wolf;
import com.pixulse.infx.entity.R196Zombie;
import org.junit.jupiter.api.Test;

class R196EntityRenderersTest {
    /**
     * createRenderState clears render data right after extractRenderState, so isWell set during
     * extraction never reaches getTextureLocation and every animal stays on its healthy skin.
     * The flag must come from a registered render state modifier instead.
     */
    @Test
    void livestockRenderersDoNotSetWellDuringExtraction() {
        for (Class<?> renderer : new Class<?>[] {
            R196EntityRenderers.CowTexture.class,
            R196EntityRenderers.ChickenTexture.class,
            R196EntityRenderers.PigTexture.class,
            R196EntityRenderers.SheepTexture.class
        }) {
            for (java.lang.reflect.Method method : renderer.getDeclaredMethods()) {
                assertNotEquals(
                        "extractRenderState",
                        method.getName(),
                        renderer.getSimpleName()
                                + " sets render data during extraction; it is wiped before the texture"
                                + " lookup. Register a render state modifier instead.");
            }
        }
    }

    /** Guards the modifier type parameters, which NeoForge validates at registration time. */
    @Test
    void livestockRenderStateModifiersRegisterForEachSickSkinRenderer() {
        assertDoesNotThrow(() -> R196EntityRenderers.registerRenderStateModifiers(
                new net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent()));
    }

    @Test
    void sickLivestockUseDerived26_2LayoutTextures() {
        assertEquals(
                "infx:textures/entity/cow/cow_temperate_sick.png",
                R196EntityRenderers.CowTexture.sickTexture().toString());
        assertEquals(
                "infx:textures/entity/chicken/chicken_temperate_sick.png",
                R196EntityRenderers.ChickenTexture.sickTexture().toString());
        assertEquals(
                "infx:textures/entity/pig/pig_temperate_sick.png",
                R196EntityRenderers.PigTexture.sickTexture().toString());
        assertEquals(
                "infx:textures/entity/sheep/sheep_sick.png",
                R196EntityRenderers.SheepTexture.sickTexture().toString());
        assertEquals(
                "infx:textures/entity/cow/cow_warm_sick.png",
                R196EntityRenderers.sickTextureFor(
                                net.minecraft.resources.Identifier.withDefaultNamespace(
                                        "textures/entity/cow/cow_warm.png"))
                        .toString());
        assertEquals(
                "infx:textures/entity/pig/pig_cold_sick_baby.png",
                R196EntityRenderers.sickTextureFor(
                                net.minecraft.resources.Identifier.withDefaultNamespace(
                                        "textures/entity/pig/pig_cold_baby.png"))
                        .toString());
        assertEquals(
                "infx:textures/entity/sheep/sheep_sick_baby.png",
                R196EntityRenderers.sickTextureFor(
                                net.minecraft.resources.Identifier.withDefaultNamespace(
                                        "textures/entity/sheep/sheep_baby.png"))
                        .toString());
    }

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

    @Test
    void newMonsterVariantsUseAuthorizedMiteEntityTextures() {
        assertEquals(
                "infx:textures/entity/ghoul.png",
                R196EntityRenderers.ZombieTexture.textureFor(R196Zombie.Variant.GHOUL).toString());
        assertEquals(
                "infx:textures/entity/shadow.png",
                R196EntityRenderers.ZombieTexture.textureFor(R196Zombie.Variant.SHADOW).toString());
        assertEquals(
                "infx:textures/entity/wight.png",
                R196EntityRenderers.ZombieTexture.textureFor(R196Zombie.Variant.WIGHT).toString());
        assertEquals(
                "infx:textures/entity/zombie/revenant.png",
                R196EntityRenderers.ZombieTexture.textureFor(R196Zombie.Variant.REVENANT).toString());
        assertEquals(
                "infx:textures/entity/skeleton/longdead.png",
                R196EntityRenderers.SkeletonTexture.textureFor(R196Skeleton.Variant.LONGDEAD).toString());
        assertEquals(
                "infx:textures/entity/skeleton/bone_lord.png",
                R196EntityRenderers.SkeletonTexture.textureFor(R196Skeleton.Variant.BONE_LORD).toString());
        assertEquals(
                "infx:textures/entity/skeleton/longdead_guardian.png",
                R196EntityRenderers.SkeletonTexture.textureFor(R196Skeleton.Variant.ANCIENT_BONE_LORD)
                        .toString());
        assertEquals(
                "infx:textures/entity/spider/black_widow.png",
                R196EntityRenderers.SpiderTexture.textureFor(R196Spider.Variant.BLACK_WIDOW).toString());
        assertEquals(
                "infx:textures/entity/spider/demon_spider.png",
                R196EntityRenderers.SpiderTexture.textureFor(R196Spider.Variant.DEMON).toString());
        assertEquals(
                "infx:textures/entity/spider/wood_spider.png",
                R196EntityRenderers.SpiderTexture.textureFor(R196Spider.Variant.WOOD).toString());
        assertEquals(
                "infx:textures/entity/spider/phase_spider.png",
                R196EntityRenderers.SpiderTexture.textureFor(R196Spider.Variant.PHASE).toString());
        assertEquals(
                "infx:textures/entity/spider/cave_spider.png",
                R196EntityRenderers.SpiderTexture.textureFor(R196Spider.Variant.CAVE_SPIDER).toString());
        assertEquals(
                "infx:textures/entity/creeper/infernal_creeper.png",
                R196EntityRenderers.CreeperTexture.textureFor(R196Creeper.Variant.INFERNAL).toString());
        assertEquals(
                "infx:textures/entity/fire_elemental.png",
                R196EntityRenderers.FireElementalTexture.texture().toString());
        assertEquals(
                "infx:textures/entity/earth_elemental/stone.png",
                R196EntityRenderers.EarthElementalTexture.texture().toString());
        assertEquals(
                "infx:textures/entity/silverfish/netherspawn.png",
                R196EntityRenderers.SilverfishTexture.textureFor(R196Silverfish.Variant.NETHERSPAWN)
                        .toString());
        assertEquals(
                "infx:textures/entity/silverfish/copperspine.png",
                R196EntityRenderers.SilverfishTexture.textureFor(R196Silverfish.Variant.COPPERSPINE)
                        .toString());
        assertEquals(
                "infx:textures/entity/silverfish/hoary.png",
                R196EntityRenderers.SilverfishTexture.textureFor(R196Silverfish.Variant.HOARY).toString());
        assertEquals(
                "infx:textures/entity/bat/vampire.png",
                R196EntityRenderers.BatTexture.textureFor(R196Bat.Variant.VAMPIRE).toString());
        assertEquals(
                "infx:textures/entity/bat/nightwing.png",
                R196EntityRenderers.BatTexture.textureFor(R196Bat.Variant.NIGHTWING).toString());
        assertEquals(
                "infx:textures/entity/hellhound/hellhound.png",
                R196EntityRenderers.WolfTexture.textureFor(R196Wolf.Variant.HELLHOUND, false, false)
                        .toString());
        assertEquals(
                "infx:textures/entity/dire_wolf/neutral.png",
                R196EntityRenderers.WolfTexture.textureFor(R196Wolf.Variant.DIRE_WOLF, false, false)
                        .toString());
        assertEquals(
                "infx:textures/entity/dire_wolf/tame.png",
                R196EntityRenderers.WolfTexture.textureFor(R196Wolf.Variant.DIRE_WOLF, true, false)
                        .toString());
        assertEquals(
                "infx:textures/entity/dire_wolf/angry.png",
                R196EntityRenderers.WolfTexture.textureFor(R196Wolf.Variant.DIRE_WOLF, false, true)
                        .toString());
    }
}
