package com.pixulse.infx.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pixulse.infx.client.MoonCelestialRenderer;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * SkyRenderer has no public moon-tint or special-celestial hook. These three narrow injections
 * retain vanilla's data-driven phase geometry while adding InfX's visual-only treatment.
 */
@Mixin(SkyRenderer.class)
abstract class SkyRendererMixin {
    @ModifyArg(
            method = "renderMoon",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/DynamicUniforms;writeTransform(Lorg/joml/Matrix4fc;Lorg/joml/Vector4fc;Lorg/joml/Vector3fc;Lorg/joml/Matrix4fc;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"),
            index = 1)
    private Vector4fc tintMoon(Vector4fc vanillaColor) {
        return MoonCelestialRenderer.tintMoon(vanillaColor);
    }

    @Inject(
            method = "renderSunMoonAndStars",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SkyRenderer;renderStars(FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private void renderMoonEffects(
            PoseStack poseStack,
            float sunAngle,
            float moonAngle,
            float starAngle,
            MoonPhase moonPhase,
            float rainBrightness,
            float starBrightness,
            CallbackInfo callback) {
        MoonCelestialRenderer.renderEffects(poseStack, moonAngle, rainBrightness, starBrightness);
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void closeMoonEffects(CallbackInfo callback) {
        MoonCelestialRenderer.close();
    }
}
