package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.entity.R196Slime;
import com.pixulse.infx.entity.R196Spider;
import net.minecraft.client.renderer.entity.BatRenderer;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.MagmaCubeRenderer;
import net.minecraft.client.renderer.entity.SilverfishRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.BatRenderState;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.monster.Creeper;

/** Vanilla-model renderers with the approved MITE textures for supported R196 variants. */
final class R196EntityRenderers {
    private R196EntityRenderers() {}

    static final class ZombieTint extends ZombieRenderer {
        private final int tint;

        ZombieTint(EntityRendererProvider.Context context, int tint) {
            super(context);
            this.tint = tint;
        }

        @Override
        protected int getModelTint(ZombieRenderState state) {
            return tint;
        }
    }

    static final class SkeletonTint extends SkeletonRenderer {
        private final int tint;

        SkeletonTint(EntityRendererProvider.Context context, int tint) {
            super(context);
            this.tint = tint;
        }

        @Override
        protected int getModelTint(SkeletonRenderState state) {
            return tint;
        }
    }

    static final class SpiderTint extends SpiderRenderer<R196Spider> {
        private final int tint;
        private final float renderScale;

        SpiderTint(EntityRendererProvider.Context context, int tint) {
            this(context, tint, 1.0F);
        }

        SpiderTint(EntityRendererProvider.Context context, int tint, float renderScale) {
            super(context);
            this.tint = tint;
            this.renderScale = renderScale;
        }

        @Override
        protected int getModelTint(LivingEntityRenderState state) {
            return tint;
        }

        @Override
        public void extractRenderState(R196Spider entity, LivingEntityRenderState state, float partialTicks) {
            super.extractRenderState(entity, state, partialTicks);
            state.scale *= renderScale;
        }
    }

    static final class CreeperTint extends CreeperRenderer {
        private final int tint;
        private final float renderScale;

        CreeperTint(EntityRendererProvider.Context context, int tint) {
            this(context, tint, 1.0F);
        }

        CreeperTint(EntityRendererProvider.Context context, int tint, float renderScale) {
            super(context);
            this.tint = tint;
            this.renderScale = renderScale;
        }

        @Override
        protected int getModelTint(CreeperRenderState state) {
            return tint;
        }

        @Override
        public void extractRenderState(Creeper entity, CreeperRenderState state, float partialTicks) {
            super.extractRenderState(entity, state, partialTicks);
            state.scale *= renderScale;
        }
    }

    static final class SlimeTexture extends SlimeRenderer {
        private final Identifier texture;

        SlimeTexture(EntityRendererProvider.Context context, R196Slime.Variant variant) {
            super(context);
            this.texture = textureFor(variant);
        }

        @Override
        public Identifier getTextureLocation(SlimeRenderState state) {
            return texture;
        }

        static Identifier textureFor(R196Slime.Variant variant) {
            return switch (variant) {
                case SLIME -> miteGelatinousTexture("slime");
                case JELLY -> miteGelatinousTexture("jelly");
                case BLOB -> miteGelatinousTexture("blob");
                case OOZE -> miteGelatinousTexture("ooze");
                case PUDDING -> miteGelatinousTexture("pudding");
            };
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
            return miteGelatinousTexture("magmacube");
        }
    }

    private static Identifier miteGelatinousTexture(String name) {
        return InfiniteX.id("textures/entity/slime/" + name + ".png");
    }

    static final class SilverfishTint extends SilverfishRenderer {
        private final int tint;

        SilverfishTint(EntityRendererProvider.Context context, int tint) {
            super(context);
            this.tint = tint;
        }

        @Override
        protected int getModelTint(LivingEntityRenderState state) {
            return tint;
        }
    }

    static final class BatTint extends BatRenderer {
        private final int tint;
        private final float renderScale;

        BatTint(EntityRendererProvider.Context context, int tint) {
            this(context, tint, 1.0F);
        }

        BatTint(EntityRendererProvider.Context context, int tint, float renderScale) {
            super(context);
            this.tint = tint;
            this.renderScale = renderScale;
        }

        @Override
        protected int getModelTint(BatRenderState state) {
            return tint;
        }

        @Override
        public void extractRenderState(Bat entity, BatRenderState state, float partialTicks) {
            super.extractRenderState(entity, state, partialTicks);
            state.scale *= renderScale;
        }
    }

    static final class WolfTint extends WolfRenderer {
        private final int tint;

        WolfTint(EntityRendererProvider.Context context, int tint) {
            super(context);
            this.tint = tint;
        }

        @Override
        protected int getModelTint(WolfRenderState state) {
            return tint;
        }
    }

    static final class EarthTint extends IronGolemRenderer {
        private final int tint;

        EarthTint(EntityRendererProvider.Context context, int tint) {
            super(context);
            this.tint = tint;
        }

        @Override
        protected int getModelTint(IronGolemRenderState state) {
            return tint;
        }
    }
}
