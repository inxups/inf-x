package com.pixulse.infx.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * InfX's bottle o' enchanting always grants the fixed 200 XP worth of two
 * enchantment levels (Enchantment#getExperienceCost(2)) instead of the modern
 * random 3-11 XP. Both awardWithDirection call sites in onHit are redirected.
 */
@Mixin(ThrownExperienceBottle.class)
public abstract class ThrownExperienceBottleMixin {
    private static final int EXPERIENCE_BOTTLE_XP = 200;

    @Redirect(
            method = "onHit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ExperienceOrb;awardWithDirection(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;I)V"))
    private static void infx$awardExperience(
            ServerLevel level, Vec3 position, Vec3 direction, int value) {
        ExperienceOrb.awardWithDirection(level, position, direction, EXPERIENCE_BOTTLE_XP);
    }
}
