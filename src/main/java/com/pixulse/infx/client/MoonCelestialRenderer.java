package com.pixulse.infx.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.world.MoonPhase;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4fc;

/** Renders MITE's colored moons, blood/blue halos, and moon-dog ring on the modern sky pipeline. */
@OnlyIn(Dist.CLIENT)
public final class MoonCelestialRenderer {
    private static final Identifier HALO_TEXTURE = InfiniteX.id("textures/environment/celestial/moon_halo.png");
    private static final Identifier RING_TEXTURE = InfiniteX.id("textures/environment/celestial/moon_ring.png");
    private static final float MOON_HEIGHT = 100.0F;
    private static final float HALO_SIZE = 160.0F;
    private static final float RING_SIZE = 80.0F;

    private static GpuBuffer haloBuffer;
    private static GpuBuffer ringBuffer;

    private MoonCelestialRenderer() {}

    /** Called from SkyRenderer's moon transform to preserve the MITE moon-face colors. */
    public static Vector4fc tintMoon(Vector4fc vanillaColor) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return vanillaColor;
        }
        MoonPhase phase = MoonPhase.at(level);
        return tintFor(phase, phase.isActiveInOverworldAtNight(level), vanillaColor);
    }

    static Vector4fc tintFor(MoonPhase phase, boolean phaseIsVisible, Vector4fc vanillaColor) {
        if (!phaseIsVisible) return vanillaColor;
        return switch (phase) {
            case BLOOD -> new Vector4f(0.6F, 0.2F, 0.1F, vanillaColor.w());
            case YELLOW -> new Vector4f(1.0F, 0.8F, 0.45F, vanillaColor.w());
            case BLUE -> new Vector4f(0.66F, 0.74F, 1.0F, vanillaColor.w());
            default -> vanillaColor;
        };
    }

    /** Called after vanilla's moon and before its stars, while its celestial transform is still active. */
    public static void renderEffects(
            PoseStack poseStack, float moonAngle, float rainBrightness, float starBrightness) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        MoonPhase phase = MoonPhase.at(level);
        if (!phase.isActiveInOverworldAtNight(level)) {
            return;
        }

        // Modern star brightness reaches 0.5 at full night; scaling it restores MITE's full
        // halo/ring opacity while still fading the effect naturally at dusk and dawn.
        float nightVisibility = Mth.clamp(starBrightness * 2.0F, 0.0F, 1.0F);
        if (nightVisibility <= 0.0F) {
            return;
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotation(moonAngle));
        try {
            switch (phase) {
                case BLOOD -> renderSprite(
                        HALO_TEXTURE,
                        haloBuffer(),
                        "INFX blood moon halo",
                        poseStack,
                        HALO_SIZE,
                        0.6F,
                        0.2F,
                        0.1F,
                        rainBrightness * 0.25F * nightVisibility);
                case BLUE -> renderSprite(
                        HALO_TEXTURE,
                        haloBuffer(),
                        "INFX blue moon halo",
                        poseStack,
                        HALO_SIZE,
                        0.33F,
                        0.37F,
                        0.8F,
                        rainBrightness * 0.25F * nightVisibility);
                case PHANTOM -> renderSprite(
                        RING_TEXTURE,
                        ringBuffer(),
                        "INFX moon dog ring",
                        poseStack,
                        RING_SIZE,
                        0.4975F,
                        0.6275F,
                        0.85F,
                        rainBrightness * 0.5F * nightVisibility);
                default -> {
                }
            }
        } finally {
            poseStack.popPose();
        }
    }

    /** Release custom geometry together with the vanilla SkyRenderer. */
    public static void close() {
        if (haloBuffer != null) {
            haloBuffer.close();
            haloBuffer = null;
        }
        if (ringBuffer != null) {
            ringBuffer.close();
            ringBuffer = null;
        }
    }

    private static GpuBuffer haloBuffer() {
        if (haloBuffer == null) {
            haloBuffer = buildQuad("INFX moon halo quad");
        }
        return haloBuffer;
    }

    private static GpuBuffer ringBuffer() {
        if (ringBuffer == null) {
            ringBuffer = buildQuad("INFX moon ring quad");
        }
        return ringBuffer;
    }

    private static GpuBuffer buildQuad(String name) {
        VertexFormat format = DefaultVertexFormat.POSITION_TEX;
        try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(4 * format.getVertexSize())) {
            BufferBuilder vertices = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, format);
            vertices.addVertex(-1.0F, 0.0F, -1.0F).setUv(0.0F, 0.0F);
            vertices.addVertex(1.0F, 0.0F, -1.0F).setUv(1.0F, 0.0F);
            vertices.addVertex(1.0F, 0.0F, 1.0F).setUv(1.0F, 1.0F);
            vertices.addVertex(-1.0F, 0.0F, 1.0F).setUv(0.0F, 1.0F);
            try (MeshData mesh = vertices.buildOrThrow()) {
                return RenderSystem.getDevice().createBuffer(() -> name, 32, mesh.vertexBuffer());
            }
        }
    }

    private static void renderSprite(
            Identifier textureId,
            GpuBuffer vertexBuffer,
            String passName,
            PoseStack poseStack,
            float size,
            float red,
            float green,
            float blue,
            float alpha) {
        if (alpha <= 0.0F) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        try {
            modelView.mul(poseStack.last().pose());
            modelView.translate(0.0F, MOON_HEIGHT, 0.0F);
            modelView.scale(size, 1.0F, size);
            GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(
                    modelView,
                    new Vector4f(red, green, blue, alpha),
                    new Vector3f(),
                    new Matrix4f());
            GpuTextureView color = minecraft.getMainRenderTarget().getColorTextureView();
            GpuTextureView depth = minecraft.getMainRenderTarget().getDepthTextureView();
            AbstractTexture texture = minecraft.getTextureManager().getTexture(textureId);
            var quadIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
            GpuBuffer indexBuffer = quadIndices.getBuffer(6);

            try (RenderPass renderPass = RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(() -> passName, color, OptionalInt.empty(), depth, OptionalDouble.empty())) {
                renderPass.setPipeline(RenderPipelines.CELESTIAL);
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setUniform("DynamicTransforms", transforms);
                renderPass.bindTexture("Sampler0", texture.getTextureView(), texture.getSampler());
                renderPass.setVertexBuffer(0, vertexBuffer);
                renderPass.setIndexBuffer(indexBuffer, quadIndices.type());
                renderPass.drawIndexed(0, 0, 6, 1);
            }
        } finally {
            modelView.popMatrix();
        }
    }
}
