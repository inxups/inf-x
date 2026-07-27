package com.pixulse.infx.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.entity.R196EarthElemental;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/** R196 material-body renderer shared by ordinary earth elementals and clay golems. */
final class R196EarthElementalRenderer<T extends R196EarthElemental>
        extends MobRenderer<T, R196EarthElementalRenderState, R196EarthElementalModel> {
    private static final Identifier STONE_NORMAL = texture("stone/earth_elemental_stone.png");
    private static final Identifier STONE_MAGMA = texture("stone/earth_elemental_stone_magma.png");
    private static final Identifier OBSIDIAN_NORMAL = texture("obsidian/earth_elemental_obsidian.png");
    private static final Identifier OBSIDIAN_MAGMA = texture("obsidian/earth_elemental_obsidian_magma.png");
    private static final Identifier NETHERRACK_NORMAL = texture("netherrack/earth_elemental_netherrack.png");
    private static final Identifier NETHERRACK_MAGMA = texture("netherrack/earth_elemental_netherrack_magma.png");
    private static final Identifier END_STONE_NORMAL = texture("end_stone/earth_elemental_end_stone.png");
    private static final Identifier END_STONE_MAGMA = texture("end_stone/earth_elemental_end_stone_magma.png");
    private static final Identifier CLAY_NORMAL = texture("clay/earth_elemental_clay.png");
    private static final Identifier CLAY_HARDENED = texture("clay/earth_elemental_clay_hardened.png");
    private static final Identifier NORMAL_GLOW = texture("earth_elemental_glow.png");
    private static final Identifier MAGMA_GLOW = texture("earth_elemental_magma_glow.png");

    R196EarthElementalRenderer(EntityRendererProvider.Context context) {
        super(context, new R196EarthElementalModel(context.bakeLayer(R196EarthElementalModel.LAYER)), 0.5F);
        addLayer(new GlowLayer(this));
    }

    @Override
    public R196EarthElementalRenderState createRenderState() {
        return new R196EarthElementalRenderState();
    }

    @Override
    public void extractRenderState(T entity, R196EarthElementalRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTicks, itemModelResolver);
        state.form = entity.form();
    }

    @Override
    public Identifier getTextureLocation(R196EarthElementalRenderState state) {
        return textureFor(state.form);
    }

    /** MITE magma elementals keep a faint light level even in total darkness. */
    @Override
    protected int getBlockLightLevel(T entity, BlockPos blockPos) {
        return entity.isMagma() ? Math.max(5, super.getBlockLightLevel(entity, blockPos)) : super.getBlockLightLevel(entity, blockPos);
    }

    @Override
    protected int getSkyLightLevel(T entity, BlockPos blockPos) {
        return entity.isMagma() ? Math.max(5, super.getSkyLightLevel(entity, blockPos)) : super.getSkyLightLevel(entity, blockPos);
    }

    static Identifier textureFor(R196EarthElemental.Form form) {
        return switch (form) {
            case STONE_NORMAL -> STONE_NORMAL;
            case STONE_MAGMA -> STONE_MAGMA;
            case OBSIDIAN_NORMAL -> OBSIDIAN_NORMAL;
            case OBSIDIAN_MAGMA -> OBSIDIAN_MAGMA;
            case NETHERRACK_NORMAL -> NETHERRACK_NORMAL;
            case NETHERRACK_MAGMA -> NETHERRACK_MAGMA;
            case END_STONE_NORMAL -> END_STONE_NORMAL;
            case END_STONE_MAGMA -> END_STONE_MAGMA;
            case CLAY_NORMAL -> CLAY_NORMAL;
            case CLAY_HARDENED -> CLAY_HARDENED;
        };
    }

    static Identifier glowTextureFor(R196EarthElemental.Form form) {
        return form.isMagmaForm() ? MAGMA_GLOW : NORMAL_GLOW;
    }

    private static Identifier texture(String filename) {
        return InfiniteX.id("textures/entity/earth_elemental/" + filename);
    }

    private static final class GlowLayer
            extends RenderLayer<R196EarthElementalRenderState, R196EarthElementalModel> {
        private GlowLayer(RenderLayerParent<R196EarthElementalRenderState, R196EarthElementalModel> renderer) {
            super(renderer);
        }

        @Override
        public void submit(
                PoseStack poseStack,
                SubmitNodeCollector submitNodeCollector,
                int lightCoords,
                R196EarthElementalRenderState state,
                float yRot,
                float xRot) {
            submitNodeCollector.order(1).submitModel(
                    getParentModel(),
                    state,
                    poseStack,
                    RenderTypes.eyes(glowTextureFor(state.form)),
                    lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    state.outlineColor,
                    null);
        }
    }
}
