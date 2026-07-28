package com.pixulse.infx.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.entity.MiteBat;
import com.pixulse.infx.entity.MiteCreeper;
import com.pixulse.infx.entity.Livestock;
import com.pixulse.infx.entity.MiteSilverfish;
import com.pixulse.infx.entity.MiteSkeleton;
import com.pixulse.infx.entity.MiteSlime;
import com.pixulse.infx.entity.MiteSpider;
import com.pixulse.infx.entity.MiteWolf;
import com.pixulse.infx.entity.MiteZombie;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.BatRenderer;
import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MagmaCubeRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.client.renderer.entity.SilverfishRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.BatRenderState;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.client.renderer.entity.state.CowRenderState;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.client.renderer.entity.state.GhastRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;

/**
 * Vanilla-model renderers that bind authorized MITE entity textures for R196 variants.
 *
 * <p>Base monsters keep vanilla texture ids wherever the authorized MITE pack is pixel-identical
 * to vanilla 26.2 (zombie, skeleton, creeper + armor, enderman + eyes, witch, spider eyes). Only
 * the audited divergences bind {@code infx:} sheets: spider, blaze, ghast, and the humanoid
 * zombie pigman that replaces the modern piglin-model look.
 */
final class EntityRenderers {
    private static final ContextKey<Boolean> LIVESTOCK_WELL =
            new ContextKey<>(InfiniteX.id("livestock_well"));
    private static final ContextKey<Boolean> VILLAGER_ZOMBIE =
            new ContextKey<>(InfiniteX.id("villager_zombie"));

    private EntityRenderers() {}

    /**
     * Attach isWell to livestock render states.
     *
     * <p>Must be registered as a render state modifier rather than set from
     * {@code extractRenderState}: {@code EntityRenderer#createRenderState} calls
     * {@code RenderStateExtensions#onUpdateEntityRenderState} right after extraction, and that
     * starts by calling {@code resetRenderData()}. Data attached during extraction is therefore
     * wiped before {@code getTextureLocation} runs, leaving every animal on its healthy skin.
     * Registered modifiers run after the reset, so the flag survives to the texture lookup.
     */
    static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(
                CowTexture.class, (Cow entity, CowRenderState state) -> extractWell(entity, state));
        event.registerEntityModifier(
                ChickenTexture.class, (Chicken entity, ChickenRenderState state) -> extractWell(entity, state));
        event.registerEntityModifier(
                PigTexture.class, (Pig entity, PigRenderState state) -> extractWell(entity, state));
        event.registerEntityModifier(
                SheepTexture.class, (Sheep entity, SheepRenderState state) -> extractWell(entity, state));
        event.registerEntityModifier(
                ZombieTexture.class,
                (net.minecraft.world.entity.monster.zombie.Zombie entity, ZombieRenderState state) ->
                        state.setRenderData(
                                VILLAGER_ZOMBIE,
                                entity instanceof MiteZombie zombie && zombie.isVillagerZombie()));
    }

    private static void extractWell(Animal animal, LivingEntityRenderState state) {
        state.setRenderData(LIVESTOCK_WELL, Livestock.isWell(animal));
    }

    private static boolean isWell(LivingEntityRenderState state) {
        Boolean well = state.getRenderData(LIVESTOCK_WELL);
        return well == null || well;
    }

    /**
     * Map a 26.2 healthy livestock texture id to the derived sick skin.
     * e.g. minecraft:textures/entity/cow/cow_temperate.png
     *   -> infx:textures/entity/cow/cow_temperate_sick.png
     *      minecraft:textures/entity/cow/cow_temperate_baby.png
     *   -> infx:textures/entity/cow/cow_temperate_sick_baby.png
     */
    static Identifier sickTextureFor(Identifier healthy) {
        String path = healthy.getPath();
        if (!path.startsWith("textures/entity/") || !path.endsWith(".png")) {
            return healthy;
        }
        String withoutExt = path.substring(0, path.length() - 4);
        String sickPath;
        if (withoutExt.endsWith("_baby")) {
            sickPath = withoutExt.substring(0, withoutExt.length() - 5) + "_sick_baby.png";
        } else {
            sickPath = withoutExt + "_sick.png";
        }
        // sheep healthy is sheep.png / sheep_baby.png -> sheep_sick.png / sheep_sick_baby.png
        if (withoutExt.endsWith("/sheep")) {
            sickPath = "textures/entity/sheep/sheep_sick.png";
        } else if (withoutExt.endsWith("/sheep_baby")) {
            sickPath = "textures/entity/sheep/sheep_sick_baby.png";
        }
        return mite(sickPath);
    }

    /** 26.2 UV sick cow skins (temperate/warm/cold + baby) when !isWell(). */
    static final class CowTexture extends CowRenderer {
        CowTexture(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public Identifier getTextureLocation(CowRenderState state) {
            Identifier healthy = super.getTextureLocation(state);
            return isWell(state) ? healthy : sickTextureFor(healthy);
        }

        static Identifier sickTexture() {
            return mite("textures/entity/cow/cow_temperate_sick.png");
        }
    }

    /** 26.2 UV sick chicken skins when !isWell(). */
    static final class ChickenTexture extends ChickenRenderer {
        ChickenTexture(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public Identifier getTextureLocation(ChickenRenderState state) {
            Identifier healthy = super.getTextureLocation(state);
            return isWell(state) ? healthy : sickTextureFor(healthy);
        }

        static Identifier sickTexture() {
            return mite("textures/entity/chicken/chicken_temperate_sick.png");
        }
    }

    /** 26.2 UV sick pig skins when !isWell(). */
    static final class PigTexture extends PigRenderer {
        PigTexture(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public Identifier getTextureLocation(PigRenderState state) {
            Identifier healthy = super.getTextureLocation(state);
            return isWell(state) ? healthy : sickTextureFor(healthy);
        }

        static Identifier sickTexture() {
            return mite("textures/entity/pig/pig_temperate_sick.png");
        }
    }

    /** 26.2 UV sick sheep body skins when !isWell(); wool layers stay vanilla. */
    static final class SheepTexture extends SheepRenderer {
        SheepTexture(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public Identifier getTextureLocation(SheepRenderState state) {
            Identifier healthy = super.getTextureLocation(state);
            return isWell(state) ? healthy : sickTextureFor(healthy);
        }

        static Identifier sickTexture() {
            return mite("textures/entity/sheep/sheep_sick.png");
        }
    }

    static final class ZombieTexture extends ZombieRenderer {
        private final MiteZombie.Variant variant;
        private final Identifier texture;
        private final Identifier babyTexture;

        ZombieTexture(EntityRendererProvider.Context context, MiteZombie.Variant variant) {
            super(context);
            this.variant = variant;
            this.texture = textureFor(variant);
            this.babyTexture = babyTextureFor(variant);
        }

        @Override
        public Identifier getTextureLocation(ZombieRenderState state) {
            if (variant == MiteZombie.Variant.ZOMBIE && Boolean.TRUE.equals(state.getRenderData(VILLAGER_ZOMBIE))) {
                return villagerTexture();
            }
            return state.isBaby ? babyTexture : texture;
        }

        static Identifier textureFor(MiteZombie.Variant variant) {
            return switch (variant) {
                case GHOUL -> mite("textures/entity/ghoul.png");
                case SHADOW -> mite("textures/entity/shadow.png");
                case WIGHT -> mite("textures/entity/wight.png");
                case REVENANT -> mite("textures/entity/zombie/revenant.png");
                case INVISIBLE_STALKER, ZOMBIE -> Identifier.withDefaultNamespace("textures/entity/zombie/zombie.png");
            };
        }

        /** 26.2 babies render with BabyZombieModel's own UV sheet, so adult sheets cannot be reused. */
        static Identifier babyTextureFor(MiteZombie.Variant variant) {
            return switch (variant) {
                case GHOUL -> mite("textures/entity/ghoul_baby.png");
                case SHADOW -> mite("textures/entity/shadow_baby.png");
                case WIGHT -> mite("textures/entity/wight_baby.png");
                case REVENANT -> mite("textures/entity/zombie/revenant_baby.png");
                case INVISIBLE_STALKER, ZOMBIE ->
                        Identifier.withDefaultNamespace("textures/entity/zombie/zombie_baby.png");
            };
        }

        static Identifier villagerTexture() {
            return mite("textures/entity/zombie/zombie_villager.png");
        }
    }

    /** MITE zombie pigmen are humanoid zombies with the pack's 64x64 sheet, not modern piglin models. */
    static final class ZombiePigmanTexture extends ZombieRenderer {
        ZombiePigmanTexture(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public Identifier getTextureLocation(ZombieRenderState state) {
            return state.isBaby ? babyTexture() : texture();
        }

        static Identifier texture() {
            return mite("textures/entity/zombie_pigman.png");
        }

        static Identifier babyTexture() {
            return mite("textures/entity/zombie_pigman_baby.png");
        }
    }

    static final class SkeletonTexture extends SkeletonRenderer {
        private final Identifier texture;

        SkeletonTexture(EntityRendererProvider.Context context, MiteSkeleton.Variant variant) {
            super(context);
            this.texture = textureFor(variant);
        }

        @Override
        public Identifier getTextureLocation(SkeletonRenderState state) {
            return texture;
        }

        static Identifier textureFor(MiteSkeleton.Variant variant) {
            return switch (variant) {
                case LONGDEAD -> mite("textures/entity/skeleton/longdead.png");
                case BONE_LORD -> mite("textures/entity/skeleton/bone_lord.png");
                case ANCIENT_BONE_LORD -> mite("textures/entity/skeleton/longdead_guardian.png");
                case SKELETON -> Identifier.withDefaultNamespace("textures/entity/skeleton/skeleton.png");
            };
        }
    }

    static final class SpiderTexture extends SpiderRenderer<MiteSpider> {
        private final Identifier texture;
        private final float renderScale;

        SpiderTexture(EntityRendererProvider.Context context, MiteSpider.Variant variant) {
            this(context, variant, 1.0F);
        }

        SpiderTexture(EntityRendererProvider.Context context, MiteSpider.Variant variant, float renderScale) {
            super(context);
            this.texture = textureFor(variant);
            this.renderScale = renderScale;
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return texture;
        }

        @Override
        public void extractRenderState(MiteSpider entity, LivingEntityRenderState state, float partialTicks) {
            super.extractRenderState(entity, state, partialTicks);
            state.scale *= renderScale;
        }

        static Identifier textureFor(MiteSpider.Variant variant) {
            return switch (variant) {
                case CAVE_SPIDER -> mite("textures/entity/spider/cave_spider.png");
                case BLACK_WIDOW -> mite("textures/entity/spider/black_widow.png");
                case DEMON -> mite("textures/entity/spider/demon_spider.png");
                case WOOD -> mite("textures/entity/spider/wood_spider.png");
                case PHASE -> mite("textures/entity/spider/phase_spider.png");
                case SPIDER -> mite("textures/entity/spider/spider.png");
            };
        }
    }

    static final class CreeperTexture extends CreeperRenderer {
        private final Identifier texture;

        CreeperTexture(EntityRendererProvider.Context context, MiteCreeper.Variant variant) {
            super(context);
            this.texture = textureFor(variant);
        }

        @Override
        public Identifier getTextureLocation(CreeperRenderState state) {
            return texture;
        }

        static Identifier textureFor(MiteCreeper.Variant variant) {
            return switch (variant) {
                case INFERNAL -> mite("textures/entity/creeper/infernal_creeper.png");
                case CREEPER -> Identifier.withDefaultNamespace("textures/entity/creeper/creeper.png");
            };
        }
    }

    /**
     * SlimeRenderer's built-in outer layer always uses the vanilla slime texture, even when this
     * renderer overrides {@link #getTextureLocation(SlimeRenderState)}. Rebuild the small renderer
     * with a texture-aware outer layer so both shells use the authorized MITE sheet.
     */
    static final class SlimeTexture extends MobRenderer<MiteSlime, SlimeRenderState, SlimeModel> {
        private final Identifier texture;

        SlimeTexture(EntityRendererProvider.Context context, MiteSlime.Variant variant) {
            super(context, new SlimeModel(context.bakeLayer(ModelLayers.SLIME)), 0.25F);
            this.texture = textureFor(variant);
            addLayer(new MiteSlimeOuterLayer(this, context.getModelSet(), texture));
        }

        @Override
        protected void scale(SlimeRenderState state, PoseStack poseStack) {
            poseStack.scale(0.999F, 0.999F, 0.999F);
            poseStack.translate(0.0F, 0.001F, 0.0F);
            float size = state.size;
            float squish = state.squish / (size * 0.5F + 1.0F);
            float width = 1.0F / (squish + 1.0F);
            poseStack.scale(width * size, 1.0F / width * size, width * size);
        }

        @Override
        protected float getShadowRadius(SlimeRenderState state) {
            return state.size * 0.25F;
        }

        @Override
        public Identifier getTextureLocation(SlimeRenderState state) {
            return texture;
        }

        @Override
        public SlimeRenderState createRenderState() {
            return new SlimeRenderState();
        }

        @Override
        public void extractRenderState(MiteSlime entity, SlimeRenderState state, float partialTicks) {
            super.extractRenderState(entity, state, partialTicks);
            state.squish = Mth.lerp(partialTicks, entity.oSquish, entity.squish);
            state.size = entity.getSize();
        }

        static Identifier textureFor(MiteSlime.Variant variant) {
            return switch (variant) {
                case SLIME -> mite("textures/entity/slime/slime.png");
                case JELLY -> mite("textures/entity/slime/jelly.png");
                case BLOB -> mite("textures/entity/slime/blob.png");
                case OOZE -> mite("textures/entity/slime/ooze.png");
                case PUDDING -> mite("textures/entity/slime/pudding.png");
            };
        }
    }

    /** Texture-aware replacement for the outer shell hard-coded by the vanilla SlimeRenderer. */
    private static final class MiteSlimeOuterLayer extends RenderLayer<SlimeRenderState, SlimeModel> {
        private final SlimeModel model;
        private final Identifier texture;

        private MiteSlimeOuterLayer(
                RenderLayerParent<SlimeRenderState, SlimeModel> renderer,
                EntityModelSet modelSet,
                Identifier texture) {
            super(renderer);
            this.model = new SlimeModel(modelSet.bakeLayer(ModelLayers.SLIME_OUTER));
            this.texture = texture;
        }

        @Override
        public void submit(
                PoseStack poseStack,
                SubmitNodeCollector submitNodeCollector,
                int lightCoords,
                SlimeRenderState state,
                float yRot,
                float xRot) {
            boolean appearsGlowingWithInvisibility = state.appearsGlowing() && state.isInvisible;
            if (!state.isInvisible || appearsGlowingWithInvisibility) {
                int overlayCoords = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
                if (appearsGlowingWithInvisibility) {
                    submitNodeCollector.order(1).submitModel(
                            model,
                            state,
                            poseStack,
                            RenderTypes.outline(texture),
                            lightCoords,
                            overlayCoords,
                            state.outlineColor,
                            null);
                } else {
                    submitNodeCollector.order(1).submitModel(
                            model,
                            state,
                            poseStack,
                            RenderTypes.entityTranslucent(texture),
                            lightCoords,
                            overlayCoords,
                            state.outlineColor,
                            null);
                }
            }
        }
    }

    static final class MagmaCubeTexture extends MagmaCubeRenderer {
        MagmaCubeTexture(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public Identifier getTextureLocation(SlimeRenderState state) {
            return texture();
        }

        static Identifier texture() {
            return mite("textures/entity/slime/magmacube.png");
        }
    }

    static final class SilverfishTexture extends SilverfishRenderer {
        private final Identifier texture;

        SilverfishTexture(EntityRendererProvider.Context context, MiteSilverfish.Variant variant) {
            super(context);
            this.texture = textureFor(variant);
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return texture;
        }

        static Identifier textureFor(MiteSilverfish.Variant variant) {
            return switch (variant) {
                case NETHERSPAWN -> mite("textures/entity/silverfish/netherspawn.png");
                case COPPERSPINE -> mite("textures/entity/silverfish/copperspine.png");
                case HOARY -> mite("textures/entity/silverfish/hoary.png");
            };
        }
    }

    static final class BatTexture extends BatRenderer {
        private final Identifier texture;
        private final float renderScale;

        BatTexture(EntityRendererProvider.Context context, MiteBat.Variant variant) {
            this(context, variant, 1.0F);
        }

        BatTexture(EntityRendererProvider.Context context, MiteBat.Variant variant, float renderScale) {
            super(context);
            this.texture = textureFor(variant);
            this.renderScale = renderScale;
        }

        @Override
        public Identifier getTextureLocation(BatRenderState state) {
            return texture;
        }

        @Override
        public void extractRenderState(Bat entity, BatRenderState state, float partialTicks) {
            super.extractRenderState(entity, state, partialTicks);
            state.scale *= renderScale;
        }

        static Identifier textureFor(MiteBat.Variant variant) {
            return switch (variant) {
                case VAMPIRE, GIANT_VAMPIRE -> mite("textures/entity/bat/vampire.png");
                case NIGHTWING -> mite("textures/entity/bat/nightwing.png");
            };
        }
    }

    static final class WolfTexture extends WolfRenderer {
        private final Identifier wild;
        private final Identifier tame;
        private final Identifier angry;

        WolfTexture(EntityRendererProvider.Context context, MiteWolf.Variant variant) {
            super(context);
            this.wild = textureFor(variant, false, false);
            this.tame = textureFor(variant, true, false);
            this.angry = textureFor(variant, false, true);
        }

        @Override
        public void extractRenderState(Wolf entity, WolfRenderState state, float partialTicks) {
            super.extractRenderState(entity, state, partialTicks);
            if (!(entity instanceof MiteWolf wolf)) {
                return;
            }
            if (wolf.variant() == MiteWolf.Variant.HELLHOUND) {
                state.texture = wild;
                return;
            }
            if (entity.isTame()) {
                state.texture = tame;
            } else if (entity.isAngry()) {
                state.texture = angry;
            } else {
                state.texture = wild;
            }
        }

        static Identifier textureFor(MiteWolf.Variant variant, boolean tame, boolean angry) {
            if (variant == MiteWolf.Variant.HELLHOUND) {
                return mite("textures/entity/hellhound/hellhound.png");
            }
            if (tame) {
                return mite("textures/entity/dire_wolf/tame.png");
            }
            if (angry) {
                return mite("textures/entity/dire_wolf/angry.png");
            }
            return mite("textures/entity/dire_wolf/neutral.png");
        }
    }

    static final class FireElementalTexture extends BlazeRenderer {
        FireElementalTexture(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return texture();
        }

        static Identifier texture() {
            return mite("textures/entity/fire_elemental.png");
        }
    }

    /** MITE blaze sheet: brighter rod pixels than vanilla 26.2. */
    static final class BlazeTexture extends BlazeRenderer {
        BlazeTexture(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return texture();
        }

        static Identifier texture() {
            return mite("textures/entity/blaze.png");
        }
    }

    /** MITE ghast face art; 64x32 matches the ghast model's declared UV size. */
    static final class GhastTexture extends GhastRenderer {
        GhastTexture(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public Identifier getTextureLocation(GhastRenderState state) {
            return texture(state.isCharging);
        }

        static Identifier texture(boolean charging) {
            return charging
                    ? mite("textures/entity/ghast/ghast_shooting.png")
                    : mite("textures/entity/ghast/ghast.png");
        }
    }

    private static Identifier mite(String path) {
        return InfiniteX.id(path);
    }
}
