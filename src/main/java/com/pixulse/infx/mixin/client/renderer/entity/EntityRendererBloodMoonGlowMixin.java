package com.pixulse.infx.mixin.client.renderer.entity;

import com.pixulse.infx.world.SpawnGate;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MITE: frenzied mobs glow red (frenzy glow color 8527390). Modern MC draws the coloured glow
 * from {@code EntityRenderState.outlineColor}, which is also what {@code appearsGlowing} reads.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererBloodMoonGlowMixin {
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void infx$bloodMoonRedGlow(
            Entity entity, EntityRenderState state, float partialTicks, CallbackInfo ci) {
        if (entity instanceof Enemy && SpawnGate.isBloodMoonFrenzied(entity.level())) {
            state.outlineColor = ARGB.opaque(0x821E1E);
        }
    }
}
