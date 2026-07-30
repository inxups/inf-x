package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.entity.InfxZombie;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.NonNull;

/** Renders the MITE stalker as a faint wight-textured silhouette rather than a potion-invisible zombie. */
public final class InvisibleStalkerRenderer
        extends MobRenderer<InfxZombie, ZombieRenderState, InvisibleStalkerModel> {
    static final float OPACITY = 0.05F;
    private static final Identifier TEXTURE = InfiniteX.id("textures/entity/wight.png");

    public InvisibleStalkerRenderer(EntityRendererProvider.Context context) {
        super(context, new InvisibleStalkerModel(context.bakeLayer(InvisibleStalkerModel.LAYER)), 0.5F);
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    @Override
    public void extractRenderState(InfxZombie entity, ZombieRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTicks, itemModelResolver);
        state.isAggressive = entity.isAggressive();
        state.isConverting = false;
    }

    @Override
    public @NonNull Identifier getTextureLocation(ZombieRenderState state) {
        return TEXTURE;
    }

    @Override
    protected RenderType getRenderType(
            ZombieRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearsGlowing) {
        return RenderTypes.entityTranslucent(TEXTURE);
    }

    @Override
    protected int getModelTint(ZombieRenderState state) {
        return modelTint();
    }

    static Identifier texture() {
        return TEXTURE;
    }

    static int modelTint() {
        return ARGB.white(OPACITY);
    }
}
