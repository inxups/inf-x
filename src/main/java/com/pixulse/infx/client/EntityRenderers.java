package com.pixulse.infx.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.config.InfXClientConfig;
import com.pixulse.infx.entity.InfxBat;
import com.pixulse.infx.entity.InfxCreeper;
import com.pixulse.infx.entity.Livestock;
import com.pixulse.infx.entity.InfxSilverfish;
import com.pixulse.infx.entity.InfxSkeleton;
import com.pixulse.infx.entity.InfxSlime;
import com.pixulse.infx.entity.InfxSpider;
import com.pixulse.infx.entity.InfxWolf;
import com.pixulse.infx.registry.InfXEntityTypes;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.model.monster.spider.SpiderModel;
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
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.SpiderEyesLayer;
import net.minecraft.client.renderer.entity.state.BatRenderState;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.jspecify.annotations.NonNull;

/**
 * Vanilla-model renderers that bind authorized imported entity textures for INFX variants.
 *
 * <p>Base monsters keep vanilla texture ids wherever the imported pack is pixel-identical
 * to vanilla 26.2 (zombie, skeleton, creeper + armor, enderman + eyes, witch, spider eyes). Only
 * the audited divergences bind {@code infx:} sheets: spider, blaze, ghast, and the humanoid
 * zombie pigman that replaces the modern piglin-model look.
 */
public final class EntityRenderers {
    private static final ContextKey<Boolean> LIVESTOCK_WELL =
            new ContextKey<>(InfiniteX.id("livestock_well"));

    private EntityRenderers() {
    }

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
    public static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(
                CowTexture.class, EntityRenderers::extractWell);
        event.registerEntityModifier(
                ChickenTexture.class, EntityRenderers::extractWell);
        event.registerEntityModifier(
                PigTexture.class, EntityRenderers::extractWell);
        event.registerEntityModifier(
                SheepTexture.class, EntityRenderers::extractWell);
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
     * -> infx:textures/entity/cow/cow_temperate_sick.png
     * minecraft:textures/entity/cow/cow_temperate_baby.png
     * -> infx:textures/entity/cow/cow_temperate_sick_baby.png
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
        return of(sickPath);
    }

    /**
     * 26.2 UV sick cow skins (temperate/warm/cold + baby) when !isWell().
     */
    public static final class CowTexture extends CowRenderer {
        public CowTexture(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public @NonNull Identifier getTextureLocation(@NonNull CowRenderState state) {
            Identifier healthy = super.getTextureLocation(state);
            return isWell(state) ? healthy : sickTextureFor(healthy);
        }

        static Identifier sickTexture() {
            return of("textures/entity/cow/cow_temperate_sick.png");
        }
    }

    /**
     * 26.2 UV sick chicken skins when !isWell().
     */
    public static final class ChickenTexture extends ChickenRenderer {
        public ChickenTexture(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public @NonNull Identifier getTextureLocation(@NonNull ChickenRenderState state) {
            Identifier healthy = super.getTextureLocation(state);
            return isWell(state) ? healthy : sickTextureFor(healthy);
        }

        static Identifier sickTexture() {
            return of("textures/entity/chicken/chicken_temperate_sick.png");
        }
    }

    /**
     * 26.2 UV sick pig skins when !isWell().
     */
    public static final class PigTexture extends PigRenderer {
        public PigTexture(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public @NonNull Identifier getTextureLocation(@NonNull PigRenderState state) {
            Identifier healthy = super.getTextureLocation(state);
            return isWell(state) ? healthy : sickTextureFor(healthy);
        }

        static Identifier sickTexture() {
            return of("textures/entity/pig/pig_temperate_sick.png");
        }
    }

    /**
     * 26.2 UV sick sheep body skins when !isWell(); wool layers stay vanilla.
     */
    public static final class SheepTexture extends SheepRenderer {
        public SheepTexture(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public @NonNull Identifier getTextureLocation(@NonNull SheepRenderState state) {
            Identifier healthy = super.getTextureLocation(state);
            return isWell(state) ? healthy : sickTextureFor(healthy);
        }

        static Identifier sickTexture() {
            return of("textures/entity/sheep/sheep_sick.png");
        }
    }

    public static final class ZombieTexture extends ZombieRenderer {
        private final Identifier texture;
        private final Identifier babyTexture;

        public ZombieTexture(EntityRendererProvider.Context context, EntityType<?> type) {
            super(context);
            this.texture = textureFor(type);
            this.babyTexture = babyTextureFor(type);
            if (type == InfXEntityTypes.REVENANT.get()) {
                this.addLayer(new EliteEyesLayer<>(this, of("textures/entity/zombie/revenant_eyes.png")));
            }
        }

        @Override
        public @NonNull Identifier getTextureLocation(ZombieRenderState state) {
            return state.isBaby ? babyTexture : texture;
        }

        static Identifier textureFor(EntityType<?> type) {
            if (type == InfXEntityTypes.GHOUL.get()) return of("textures/entity/ghoul.png");
            if (type == InfXEntityTypes.SHADOW.get()) return of("textures/entity/shadow.png");
            if (type == InfXEntityTypes.WIGHT.get()) return of("textures/entity/wight.png");
            if (type == InfXEntityTypes.REVENANT.get()) return of("textures/entity/zombie/revenant.png");
            return Identifier.withDefaultNamespace("textures/entity/zombie/zombie.png");
        }

        /**
         * 26.2 babies render with BabyZombieModel's own UV sheet, so adult sheets cannot be reused.
         */
        static Identifier babyTextureFor(EntityType<?> type) {
            if (type == InfXEntityTypes.GHOUL.get()) return of("textures/entity/ghoul_baby.png");
            if (type == InfXEntityTypes.SHADOW.get()) return of("textures/entity/shadow_baby.png");
            if (type == InfXEntityTypes.WIGHT.get()) return of("textures/entity/wight_baby.png");
            if (type == InfXEntityTypes.REVENANT.get()) return of("textures/entity/zombie/revenant_baby.png");
            return Identifier.withDefaultNamespace("textures/entity/zombie/zombie_baby.png");
        }
    }

    public static final class SkeletonTexture extends SkeletonRenderer {
        private final Identifier texture;

        public SkeletonTexture(EntityRendererProvider.Context context, InfxSkeleton.Variant variant) {
            super(context);
            this.texture = textureFor(variant);
            if (variant == InfxSkeleton.Variant.BONE_LORD) {
                this.addLayer(new EliteEyesLayer<>(this, of("textures/entity/skeleton/bone_lord_eyes.png")));
            }
        }

        @Override
        public @NonNull Identifier getTextureLocation(@NonNull SkeletonRenderState state) {
            return texture;
        }

        static Identifier textureFor(InfxSkeleton.Variant variant) {
            return switch (variant) {
                case LONGDEAD -> of("textures/entity/skeleton/longdead.png");
                case LONGDEAD_GUARDIAN -> of("textures/entity/skeleton/longdead_guardian.png");
                case BONE_LORD -> of("textures/entity/skeleton/bone_lord.png");
                case ANCIENT_BONE_LORD -> of("textures/entity/skeleton/longdead_guardian.png");
                case SKELETON -> Identifier.withDefaultNamespace("textures/entity/skeleton/skeleton.png");
            };
        }
    }

    /** Green emissive eyes for the phase spider, mirroring the vanilla red spider eyes layer. */
    public static final class PhaseSpiderEyesLayer extends SpiderEyesLayer<SpiderModel> {
        private static final RenderType GREEN_EYES =
                RenderTypes.eyes(InfiniteX.id("textures/entity/spider/phase_spider_eyes.png"));

        public PhaseSpiderEyesLayer(RenderLayerParent<LivingEntityRenderState, SpiderModel> renderer) {
            super(renderer);
        }

        @Override
        public RenderType renderType() {
            return GREEN_EYES;
        }
    }

    /**
     * Emissive eye overlay for elite mobs (hellhound / revenant / bone lord): a transparent
     * canvas with bright red at the body sheet's eye UVs, drawn with {@link RenderTypes#eyes}
     * so the eyes glow fullbright regardless of light, moon phase, or dimension. Gated by the
     * client {@code eliteEyeGlow} switch so the whole effect can be turned off.
     */
    public static final class EliteEyesLayer<S extends LivingEntityRenderState, M extends EntityModel<S>>
            extends EyesLayer<S, M> {
        private final RenderType eyeRenderType;

        public EliteEyesLayer(RenderLayerParent<S, M> renderer, Identifier eyeTexture) {
            super(renderer);
            this.eyeRenderType = RenderTypes.eyes(eyeTexture);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                           int lightCoords, S state, float yRot, float xRot) {
            if (!InfXClientConfig.INSTANCE.eliteEyeGlow.getValue()) {
                return;
            }
            super.submit(poseStack, submitNodeCollector, lightCoords, state, yRot, xRot);
        }

        @Override
        public RenderType renderType() {
            return eyeRenderType;
        }
    }

    public static final class SpiderTexture extends SpiderRenderer<InfxSpider> {
        private final Identifier texture;
        private final float renderScale;

        public SpiderTexture(EntityRendererProvider.Context context, InfxSpider.Variant variant) {
            this(context, variant, 1.0F);
        }

        public SpiderTexture(EntityRendererProvider.Context context, InfxSpider.Variant variant, float renderScale) {
            super(context);
            this.texture = textureFor(variant);
            this.renderScale = renderScale;
            // Glow textures: phase spiders glow green, everything else red. The phase body sheet
            // paints green eyes, so it needs a green eyes glow layer instead of the vanilla red one.
            if (variant == InfxSpider.Variant.PHASE) {
                this.layers.removeIf(layer -> layer instanceof SpiderEyesLayer<?>);
                this.addLayer(new PhaseSpiderEyesLayer(this));
            }
        }

        @Override
        public @NonNull Identifier getTextureLocation(@NonNull LivingEntityRenderState state) {
            return texture;
        }

        @Override
        public void extractRenderState(InfxSpider entity, @NonNull LivingEntityRenderState state, float partialTicks) {
            super.extractRenderState(entity, state, partialTicks);
            state.scale *= renderScale;
        }

        static Identifier textureFor(InfxSpider.Variant variant) {
            return switch (variant) {
                case CAVE_SPIDER -> of("textures/entity/spider/cave_spider.png");
                case BLACK_WIDOW -> of("textures/entity/spider/black_widow.png");
                case DEMON -> of("textures/entity/spider/demon_spider.png");
                case WOOD -> of("textures/entity/spider/wood_spider.png");
                case PHASE -> of("textures/entity/spider/phase_spider.png");
                case SPIDER -> of("textures/entity/spider/spider.png");
            };
        }
    }

    public static final class CreeperTexture extends CreeperRenderer {
        private final Identifier texture;

        public CreeperTexture(EntityRendererProvider.Context context, InfxCreeper.Variant variant) {
            super(context);
            this.texture = textureFor(variant);
        }

        @Override
        public @NonNull Identifier getTextureLocation(@NonNull CreeperRenderState state) {
            return texture;
        }

        static Identifier textureFor(InfxCreeper.Variant variant) {
            return switch (variant) {
                case INFERNAL -> of("textures/entity/creeper/infernal_creeper.png");
                case CREEPER -> Identifier.withDefaultNamespace("textures/entity/creeper/creeper.png");
            };
        }
    }

    /**
     * SlimeRenderer's built-in outer layer always uses the vanilla slime texture, even when this
     * renderer overrides {@link #getTextureLocation(SlimeRenderState)}. Rebuild the small renderer
     * with a texture-aware outer layer so both shells use the authorized imported sheet.
     */
    public static final class SlimeTexture extends MobRenderer<InfxSlime, SlimeRenderState, SlimeModel> {
        private final Identifier texture;

        public SlimeTexture(EntityRendererProvider.Context context, InfxSlime.Variant variant) {
            super(context, new SlimeModel(context.bakeLayer(ModelLayers.SLIME)), 0.25F);
            this.texture = textureFor(variant);
            addLayer(new InfxSlimeOuterLayer(this, context.getModelSet(), texture));
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
        public @NonNull Identifier getTextureLocation(SlimeRenderState state) {
            return texture;
        }

        @Override
        public SlimeRenderState createRenderState() {
            return new SlimeRenderState();
        }

        @Override
        public void extractRenderState(InfxSlime entity, SlimeRenderState state, float partialTicks) {
            super.extractRenderState(entity, state, partialTicks);
            state.squish = Mth.lerp(partialTicks, entity.oSquish, entity.squish);
            state.size = entity.getSize();
        }

        static Identifier textureFor(InfxSlime.Variant variant) {
            return switch (variant) {
                case SLIME -> of("textures/entity/slime/slime.png");
                case JELLY -> of("textures/entity/slime/jelly.png");
                case BLOB -> of("textures/entity/slime/blob.png");
                case OOZE -> of("textures/entity/slime/ooze.png");
                case PUDDING -> of("textures/entity/slime/pudding.png");
            };
        }
    }

    /**
     * Texture-aware replacement for the outer shell hard-coded by the vanilla SlimeRenderer.
     */
    private static final class InfxSlimeOuterLayer extends RenderLayer<SlimeRenderState, SlimeModel> {
        private final SlimeModel model;
        private final Identifier texture;

        private InfxSlimeOuterLayer(
                RenderLayerParent<SlimeRenderState, SlimeModel> renderer,
                EntityModelSet modelSet,
                Identifier texture) {
            super(renderer);
            this.model = new SlimeModel(modelSet.bakeLayer(ModelLayers.SLIME_OUTER));
            this.texture = texture;
        }

        @Override
        public void submit(
                @NonNull PoseStack poseStack,
                @NonNull SubmitNodeCollector submitNodeCollector,
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

    public static final class MagmaCubeTexture extends MagmaCubeRenderer {
        public MagmaCubeTexture(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public @NonNull Identifier getTextureLocation(@NonNull SlimeRenderState state) {
            return texture();
        }

        static Identifier texture() {
            return of("textures/entity/slime/magmacube.png");
        }
    }

    public static final class SilverfishTexture extends SilverfishRenderer {
        private final Identifier texture;

        public SilverfishTexture(EntityRendererProvider.Context context, InfxSilverfish.Variant variant) {
            super(context);
            this.texture = textureFor(variant);
        }

        @Override
        public @NonNull Identifier getTextureLocation(@NonNull LivingEntityRenderState state) {
            return texture;
        }

        static Identifier textureFor(InfxSilverfish.Variant variant) {
            return switch (variant) {
                case NETHERSPAWN -> of("textures/entity/silverfish/netherspawn.png");
                case COPPERSPINE -> of("textures/entity/silverfish/copperspine.png");
                case HOARY -> of("textures/entity/silverfish/hoary.png");
            };
        }
    }

    public static final class BatTexture extends BatRenderer {
        private final Identifier texture;
        private final float renderScale;

        public BatTexture(EntityRendererProvider.Context context, InfxBat.Variant variant) {
            this(context, variant, 1.0F);
        }

        public BatTexture(EntityRendererProvider.Context context, InfxBat.Variant variant, float renderScale) {
            super(context);
            this.texture = textureFor(variant);
            this.renderScale = renderScale;
        }

        @Override
        public @NonNull Identifier getTextureLocation(@NonNull BatRenderState state) {
            return texture;
        }

        @Override
        public void extractRenderState(@NonNull Bat entity, @NonNull BatRenderState state, float partialTicks) {
            super.extractRenderState(entity, state, partialTicks);
            state.scale *= renderScale;
        }

        static Identifier textureFor(InfxBat.Variant variant) {
            return switch (variant) {
                case NORMAL -> of("textures/entity/bat.png");
                case VAMPIRE, GIANT_VAMPIRE -> of("textures/entity/bat/vampire.png");
                case NIGHTWING -> of("textures/entity/bat/nightwing.png");
            };
        }
    }

    public static final class WolfTexture extends WolfRenderer {
        private final Identifier wild;
        private final Identifier tame;
        private final Identifier angry;

        public WolfTexture(EntityRendererProvider.Context context, InfxWolf.Variant variant) {
            super(context);
            this.wild = textureFor(variant, false, false);
            this.tame = textureFor(variant, true, false);
            this.angry = textureFor(variant, false, true);
            if (variant == InfxWolf.Variant.HELLHOUND) {
                this.addLayer(new EliteEyesLayer<>(this, of("textures/entity/hellhound/hellhound_eyes.png")));
            }
        }

        @Override
        public void extractRenderState(@NonNull Wolf entity, @NonNull WolfRenderState state, float partialTicks) {
            super.extractRenderState(entity, state, partialTicks);
            if (!(entity instanceof InfxWolf wolf)) {
                return;
            }
            if (wolf.variant() == InfxWolf.Variant.HELLHOUND) {
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

        static Identifier textureFor(InfxWolf.Variant variant, boolean tame, boolean angry) {
            if (variant == InfxWolf.Variant.HELLHOUND) {
                return of("textures/entity/hellhound/hellhound.png");
            }
            if (tame) {
                return of("textures/entity/dire_wolf/tame.png");
            }
            if (angry) {
                return of("textures/entity/dire_wolf/angry.png");
            }
            return of("textures/entity/dire_wolf/neutral.png");
        }
    }

    public static final class FireElementalTexture extends BlazeRenderer {
        public FireElementalTexture(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public @NonNull Identifier getTextureLocation(@NonNull LivingEntityRenderState state) {
            return texture();
        }

        static Identifier texture() {
            return of("textures/entity/fire_elemental.png");
        }
    }

    /**
     * Blaze sheet: brighter rod pixels than vanilla 26.2.
     */
    public static final class BlazeTexture extends BlazeRenderer {
        public BlazeTexture(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public @NonNull Identifier getTextureLocation(@NonNull LivingEntityRenderState state) {
            return texture();
        }

        static Identifier texture() {
            return of("textures/entity/blaze.png");
        }
    }

    /**
     * Ghast face art; 64x32 matches the ghast model's declared UV size.
     */
    public static final class GhastTexture extends GhastRenderer {
        public GhastTexture(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public @NonNull Identifier getTextureLocation(GhastRenderState state) {
            return texture(state.isCharging);
        }

        static Identifier texture(boolean charging) {
            return charging
                    ? of("textures/entity/ghast/ghast_shooting.png")
                    : of("textures/entity/ghast/ghast.png");
        }
    }

    private static Identifier of(String path) {
        return InfiniteX.id(path);
    }
}
