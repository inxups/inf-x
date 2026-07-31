package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.pixulse.infx.entity.InfxBat;
import com.pixulse.infx.entity.InfxCreeper;
import com.pixulse.infx.entity.EarthElemental;
import com.pixulse.infx.entity.InfxSilverfish;
import com.pixulse.infx.entity.InfxSkeleton;
import com.pixulse.infx.entity.InfxSlime;
import com.pixulse.infx.entity.InfxSpider;
import com.pixulse.infx.entity.InfxWolf;
import com.pixulse.infx.entity.InfxZombie;
import org.junit.jupiter.api.Test;

class EntityRenderersTest {
    /**
     * createRenderState clears render data right after extractRenderState, so isWell set during
     * extraction never reaches getTextureLocation and every animal stays on its healthy skin.
     * The flag must come from a registered render state modifier instead.
     */
    @Test
    void livestockRenderersDoNotSetWellDuringExtraction() {
        for (Class<?> renderer : new Class<?>[] {
            EntityRenderers.CowTexture.class,
            EntityRenderers.ChickenTexture.class,
            EntityRenderers.PigTexture.class,
            EntityRenderers.SheepTexture.class
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
        assertDoesNotThrow(() -> EntityRenderers.registerRenderStateModifiers(
                new net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent()));
    }

    @Test
    void sickLivestockUseDerived26_2LayoutTextures() {
        assertEquals(
                "infx:textures/entity/cow/cow_temperate_sick.png",
                EntityRenderers.CowTexture.sickTexture().toString());
        assertEquals(
                "infx:textures/entity/chicken/chicken_temperate_sick.png",
                EntityRenderers.ChickenTexture.sickTexture().toString());
        assertEquals(
                "infx:textures/entity/pig/pig_temperate_sick.png",
                EntityRenderers.PigTexture.sickTexture().toString());
        assertEquals(
                "infx:textures/entity/sheep/sheep_sick.png",
                EntityRenderers.SheepTexture.sickTexture().toString());
        assertEquals(
                "infx:textures/entity/cow/cow_warm_sick.png",
                EntityRenderers.sickTextureFor(
                                net.minecraft.resources.Identifier.withDefaultNamespace(
                                        "textures/entity/cow/cow_warm.png"))
                        .toString());
        assertEquals(
                "infx:textures/entity/pig/pig_cold_sick_baby.png",
                EntityRenderers.sickTextureFor(
                                net.minecraft.resources.Identifier.withDefaultNamespace(
                                        "textures/entity/pig/pig_cold_baby.png"))
                        .toString());
        assertEquals(
                "infx:textures/entity/sheep/sheep_sick_baby.png",
                EntityRenderers.sickTextureFor(
                                net.minecraft.resources.Identifier.withDefaultNamespace(
                                        "textures/entity/sheep/sheep_baby.png"))
                        .toString());
    }

    @Test
    void gelatinousVariantsUseTheirMatchingMiteEntityTextures() {
        assertEquals(
                "infx:textures/entity/slime/slime.png",
                EntityRenderers.SlimeTexture.textureFor(InfxSlime.Variant.SLIME).toString());
        assertEquals(
                "infx:textures/entity/slime/jelly.png",
                EntityRenderers.SlimeTexture.textureFor(InfxSlime.Variant.JELLY).toString());
        assertEquals(
                "infx:textures/entity/slime/blob.png",
                EntityRenderers.SlimeTexture.textureFor(InfxSlime.Variant.BLOB).toString());
        assertEquals(
                "infx:textures/entity/slime/ooze.png",
                EntityRenderers.SlimeTexture.textureFor(InfxSlime.Variant.OOZE).toString());
        assertEquals(
                "infx:textures/entity/slime/pudding.png",
                EntityRenderers.SlimeTexture.textureFor(InfxSlime.Variant.PUDDING).toString());
        assertEquals(
                "infx:textures/entity/slime/magmacube.png",
                EntityRenderers.MagmaCubeTexture.texture().toString());
    }

    /**
     * Vanilla SlimeRenderer owns an outer layer that unconditionally binds
     * minecraft:textures/entity/slime/slime.png. The INFX renderer must own both layers so the
     * supplied MITE sheet is applied consistently.
     */
    @Test
    void gelatinousRenderersDoNotInheritVanillaOuterLayer() {
        assertEquals(
                net.minecraft.client.renderer.entity.MobRenderer.class,
                EntityRenderers.SlimeTexture.class.getSuperclass());
    }

    @Test
    void newMonsterVariantsUseAuthorizedMiteEntityTextures() {
        assertEquals(
                "infx:textures/entity/wight.png", InvisibleStalkerRenderer.texture().toString());
        assertEquals(
                net.minecraft.util.ARGB.white(InvisibleStalkerRenderer.OPACITY),
                InvisibleStalkerRenderer.modelTint());
        assertEquals(
                "infx:textures/entity/ghoul.png",
                EntityRenderers.ZombieTexture.textureFor(InfxZombie.Variant.GHOUL).toString());
        assertEquals(
                "infx:textures/entity/shadow.png",
                EntityRenderers.ZombieTexture.textureFor(InfxZombie.Variant.SHADOW).toString());
        assertEquals(
                "infx:textures/entity/wight.png",
                EntityRenderers.ZombieTexture.textureFor(InfxZombie.Variant.WIGHT).toString());
        assertEquals(
                "infx:textures/entity/zombie/revenant.png",
                EntityRenderers.ZombieTexture.textureFor(InfxZombie.Variant.REVENANT).toString());
        assertEquals(
                "infx:textures/entity/zombie/zombie_villager.png",
                EntityRenderers.ZombieTexture.villagerTexture().toString());
        assertEquals(
                "infx:textures/entity/skeleton/longdead.png",
                EntityRenderers.SkeletonTexture.textureFor(InfxSkeleton.Variant.LONGDEAD).toString());
        assertEquals(
                "infx:textures/entity/skeleton/longdead_guardian.png",
                EntityRenderers.SkeletonTexture.textureFor(InfxSkeleton.Variant.LONGDEAD_GUARDIAN).toString());
        assertEquals(
                "infx:textures/entity/skeleton/bone_lord.png",
                EntityRenderers.SkeletonTexture.textureFor(InfxSkeleton.Variant.BONE_LORD).toString());
        assertEquals(
                "infx:textures/entity/skeleton/longdead_guardian.png",
                EntityRenderers.SkeletonTexture.textureFor(InfxSkeleton.Variant.ANCIENT_BONE_LORD)
                        .toString());
        assertEquals(
                "infx:textures/entity/spider/black_widow.png",
                EntityRenderers.SpiderTexture.textureFor(InfxSpider.Variant.BLACK_WIDOW).toString());
        assertEquals(
                "infx:textures/entity/spider/demon_spider.png",
                EntityRenderers.SpiderTexture.textureFor(InfxSpider.Variant.DEMON).toString());
        assertEquals(
                "infx:textures/entity/spider/wood_spider.png",
                EntityRenderers.SpiderTexture.textureFor(InfxSpider.Variant.WOOD).toString());
        assertEquals(
                "infx:textures/entity/spider/phase_spider.png",
                EntityRenderers.SpiderTexture.textureFor(InfxSpider.Variant.PHASE).toString());
        assertEquals(
                "infx:textures/entity/spider/cave_spider.png",
                EntityRenderers.SpiderTexture.textureFor(InfxSpider.Variant.CAVE_SPIDER).toString());
        assertEquals(
                "infx:textures/entity/creeper/infernal_creeper.png",
                EntityRenderers.CreeperTexture.textureFor(InfxCreeper.Variant.INFERNAL).toString());
        assertEquals(
                "infx:textures/entity/fire_elemental.png",
                EntityRenderers.FireElementalTexture.texture().toString());
        assertEquals(
                "infx:textures/entity/silverfish/netherspawn.png",
                EntityRenderers.SilverfishTexture.textureFor(InfxSilverfish.Variant.NETHERSPAWN)
                        .toString());
        assertEquals(
                "infx:textures/entity/silverfish/copperspine.png",
                EntityRenderers.SilverfishTexture.textureFor(InfxSilverfish.Variant.COPPERSPINE)
                        .toString());
        assertEquals(
                "infx:textures/entity/silverfish/hoary.png",
                EntityRenderers.SilverfishTexture.textureFor(InfxSilverfish.Variant.HOARY).toString());
        assertEquals(
                "infx:textures/entity/bat.png",
                EntityRenderers.BatTexture.textureFor(InfxBat.Variant.NORMAL).toString());
        assertEquals(
                "infx:textures/entity/bat/vampire.png",
                EntityRenderers.BatTexture.textureFor(InfxBat.Variant.VAMPIRE).toString());
        assertEquals(
                "infx:textures/entity/bat/nightwing.png",
                EntityRenderers.BatTexture.textureFor(InfxBat.Variant.NIGHTWING).toString());
        assertEquals(
                "infx:textures/entity/hellhound/hellhound.png",
                EntityRenderers.WolfTexture.textureFor(InfxWolf.Variant.HELLHOUND, false, false)
                        .toString());
        assertEquals(
                "infx:textures/entity/dire_wolf/neutral.png",
                EntityRenderers.WolfTexture.textureFor(InfxWolf.Variant.DIRE_WOLF, false, false)
                        .toString());
        assertEquals(
                "infx:textures/entity/dire_wolf/tame.png",
                EntityRenderers.WolfTexture.textureFor(InfxWolf.Variant.DIRE_WOLF, true, false)
                        .toString());
        assertEquals(
                "infx:textures/entity/dire_wolf/angry.png",
                EntityRenderers.WolfTexture.textureFor(InfxWolf.Variant.DIRE_WOLF, false, true)
                        .toString());
    }

    @Test
    void earthElementalFormsUseTheirMatchingMaterialAndGlowTextures() {
        assertEarthTexture(
                EarthElemental.Form.STONE_NORMAL,
                "infx:textures/entity/earth_elemental/stone/earth_elemental_stone.png",
                "infx:textures/entity/earth_elemental/earth_elemental_glow.png");
        assertEarthTexture(
                EarthElemental.Form.STONE_MAGMA,
                "infx:textures/entity/earth_elemental/stone/earth_elemental_stone_magma.png",
                "infx:textures/entity/earth_elemental/earth_elemental_magma_glow.png");
        assertEarthTexture(
                EarthElemental.Form.OBSIDIAN_NORMAL,
                "infx:textures/entity/earth_elemental/obsidian/earth_elemental_obsidian.png",
                "infx:textures/entity/earth_elemental/earth_elemental_glow.png");
        assertEarthTexture(
                EarthElemental.Form.OBSIDIAN_MAGMA,
                "infx:textures/entity/earth_elemental/obsidian/earth_elemental_obsidian_magma.png",
                "infx:textures/entity/earth_elemental/earth_elemental_magma_glow.png");
        assertEarthTexture(
                EarthElemental.Form.NETHERRACK_NORMAL,
                "infx:textures/entity/earth_elemental/netherrack/earth_elemental_netherrack.png",
                "infx:textures/entity/earth_elemental/earth_elemental_glow.png");
        assertEarthTexture(
                EarthElemental.Form.NETHERRACK_MAGMA,
                "infx:textures/entity/earth_elemental/netherrack/earth_elemental_netherrack_magma.png",
                "infx:textures/entity/earth_elemental/earth_elemental_magma_glow.png");
        assertEarthTexture(
                EarthElemental.Form.END_STONE_NORMAL,
                "infx:textures/entity/earth_elemental/end_stone/earth_elemental_end_stone.png",
                "infx:textures/entity/earth_elemental/earth_elemental_glow.png");
        assertEarthTexture(
                EarthElemental.Form.END_STONE_MAGMA,
                "infx:textures/entity/earth_elemental/end_stone/earth_elemental_end_stone_magma.png",
                "infx:textures/entity/earth_elemental/earth_elemental_magma_glow.png");
        assertEarthTexture(
                EarthElemental.Form.CLAY_NORMAL,
                "infx:textures/entity/earth_elemental/clay/earth_elemental_clay.png",
                "infx:textures/entity/earth_elemental/earth_elemental_glow.png");
        assertEarthTexture(
                EarthElemental.Form.CLAY_HARDENED,
                "infx:textures/entity/earth_elemental/clay/earth_elemental_clay_hardened.png",
                "infx:textures/entity/earth_elemental/earth_elemental_glow.png");
    }

    private static void assertEarthTexture(
            EarthElemental.Form form, String expectedTexture, String expectedGlowTexture) {
        assertEquals(expectedTexture, EarthElementalRenderer.textureFor(form).toString(), form.name());
        assertEquals(expectedGlowTexture, EarthElementalRenderer.glowTextureFor(form).toString(), form.name());
    }

    /**
     * Pixel audit vs minecraft_26.2_client.jar: the MITE pack's zombie, skeleton, creeper(+armor),
     * enderman(+eyes), witch, and spider-eyes sheets are identical to vanilla 26.2, so those stay
     * on vanilla ids. Only the audited divergences bind infx sheets.
     */
    @Test
    void baseMonstersBindOnlyTheAuditedMiteDivergences() {
        assertEquals(
                "infx:textures/entity/spider/spider.png",
                EntityRenderers.SpiderTexture.textureFor(InfxSpider.Variant.SPIDER).toString());
        assertEquals(
                "infx:textures/entity/blaze.png", EntityRenderers.BlazeTexture.texture().toString());
        assertEquals(
                "infx:textures/entity/ghast/ghast.png",
                EntityRenderers.GhastTexture.texture(false).toString());
        assertEquals(
                "infx:textures/entity/ghast/ghast_shooting.png",
                EntityRenderers.GhastTexture.texture(true).toString());
        assertEquals(
                "infx:textures/entity/zombie_pigman.png",
                EntityRenderers.ZombiePigmanTexture.texture().toString());
        assertEquals(
                "infx:textures/entity/zombie_pigman_baby.png",
                EntityRenderers.ZombiePigmanTexture.babyTexture().toString());
        assertEquals(
                "minecraft:textures/entity/zombie/zombie.png",
                EntityRenderers.ZombieTexture.textureFor(InfxZombie.Variant.ZOMBIE).toString());
        assertEquals(
                "minecraft:textures/entity/creeper/creeper.png",
                EntityRenderers.CreeperTexture.textureFor(InfxCreeper.Variant.CREEPER).toString());
    }

    /** 26.2 renders babies with BabyZombieModel's chibi UV sheet, never the adult layout. */
    @Test
    void zombieLineBabiesUseDerivedBabyUvSheets() {
        assertEquals(
                "infx:textures/entity/ghoul_baby.png",
                EntityRenderers.ZombieTexture.babyTextureFor(InfxZombie.Variant.GHOUL).toString());
        assertEquals(
                "infx:textures/entity/shadow_baby.png",
                EntityRenderers.ZombieTexture.babyTextureFor(InfxZombie.Variant.SHADOW).toString());
        assertEquals(
                "infx:textures/entity/wight_baby.png",
                EntityRenderers.ZombieTexture.babyTextureFor(InfxZombie.Variant.WIGHT).toString());
        assertEquals(
                "infx:textures/entity/zombie/revenant_baby.png",
                EntityRenderers.ZombieTexture.babyTextureFor(InfxZombie.Variant.REVENANT).toString());
        assertEquals(
                "minecraft:textures/entity/zombie/zombie_baby.png",
                EntityRenderers.ZombieTexture.babyTextureFor(InfxZombie.Variant.ZOMBIE).toString());
    }
}
