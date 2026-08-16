package com.pixulse.infx.mixin.world.entity.monster;

import com.pixulse.infx.entity.InfxCreeper;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * MITE creeper explosions destroy blocks at 0.715× their power while keeping the 1.1× entity
 * radius (the entity radius is handled separately by ExplosionRanges).
 */
@Mixin(Creeper.class)
public abstract class CreeperExplosionBlockRadiusMixin {
    @ModifyArg(
            method = "explodeCreeper",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)V"),
            index = 4)
    private float infx$creeperBlockRadius(float radius) {
        return ((Object) this) instanceof InfxCreeper ? radius * 0.715F : radius;
    }
}
