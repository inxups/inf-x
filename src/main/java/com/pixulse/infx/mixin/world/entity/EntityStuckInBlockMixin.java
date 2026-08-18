package com.pixulse.infx.mixin.world.entity;

import com.pixulse.infx.item.enchantment.EnchantmentRules;
import com.pixulse.infx.item.enchantment.Enchantments;
import com.pixulse.infx.registry.InfXEnchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MITE's Free Action resists web (and other stuck-block) slow-down the same way it resists
 * Slowness/Paralysis: the slow-down factor is multiplied by {@code 1 - resistance}. Modern
 * {@link Entity#makeStuckInBlock} writes the multiplier straight to {@code stuckSpeedMultiplier}
 * without going through the MOVEMENT_SPEED attribute, so this injects at the stuck-block entry
 * and softens each multiplier component toward 1.0 by the Free Movement resistance.
 */
@Mixin(Entity.class)
abstract class EntityStuckInBlockMixin {
    @Inject(method = "makeStuckInBlock", at = @At("HEAD"), cancellable = true)
    private void infx$applyFreeMovement(BlockState state, Vec3 multiplier, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof LivingEntity living)) {
            return;
        }
        int level = Enchantments.maxArmorLevel(living, InfXEnchantments.FREE_MOVEMENT);
        if (level <= 0) {
            return;
        }
        float resistance = EnchantmentRules.freeMovementResistance(level);
        Vec3 reduced = new Vec3(
                reduceComponent(multiplier.x(), resistance),
                reduceComponent(multiplier.y(), resistance),
                reduceComponent(multiplier.z(), resistance));
        self.resetFallDistance();
        ((EntityAccessor) self).infx$setStuckSpeedMultiplier(reduced);
        ci.cancel();
    }

    /** MITE {@code getSpeedBoostVsSlowDown}: factor = {@code base + (1 - base) * resistance}. */
    private static double reduceComponent(double base, float resistance) {
        double clamped = Math.clamp(base, 0.0, 1.0);
        return clamped + (1.0 - clamped) * resistance;
    }
}
