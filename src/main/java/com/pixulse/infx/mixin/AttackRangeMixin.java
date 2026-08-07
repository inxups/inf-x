package com.pixulse.infx.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * InfX melee reach grows when the target is lower than the attacker: reach increases by
 * half of (elevation difference - 0.5), capped at +1.0, and shrinks symmetrically for higher
 * targets. The vanilla eye-to-point distance check never accounts for vertical separation,
 * so a sword cannot hit a mob two blocks below; this restores the InfX height advantage for
 * any INFX weapon or tool carrying an attack-range component.
 */
@Mixin(AttackRange.class)
public abstract class AttackRangeMixin {
    @Shadow
    public abstract float effectiveMinRange(Entity entity);

    @Shadow
    public abstract float effectiveMaxRange(Entity entity);

    @Shadow
    public abstract float hitboxMargin();

    @Inject(method = "isInRange(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/Vec3;)Z", at = @At("HEAD"), cancellable = true)
    private void infx$HeightAdvantagePoint(
            LivingEntity attacker, Vec3 location, CallbackInfoReturnable<Boolean> callback) {
        if (!(attacker instanceof Player player) || !hasAttackRangeComponent(player)) {
            return;
        }
        // The client measures eye-to-hit-point distance, so the vertical separation is taken
        // from the eye; the server box check below keeps InfX's feet-to-feet measurement.
        float advantage = heightAdvantage((float) (player.getEyeY() - location.y));
        if (advantage == 0.0F) {
            return;
        }
        double distance = player.getEyePosition().distanceTo(location);
        callback.setReturnValue(distance >= effectiveMinRange(player) - hitboxMargin()
                && distance <= effectiveMaxRange(player) + hitboxMargin() + advantage);
    }

    @Inject(method = "isInRange(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/AABB;D)Z", at = @At("HEAD"), cancellable = true)
    private void infx$HeightAdvantageBox(
            LivingEntity attacker, AABB boundingBox, double extraBuffer, CallbackInfoReturnable<Boolean> callback) {
        if (!(attacker instanceof Player player) || !hasAttackRangeComponent(player)) {
            return;
        }
        // InfX measures elevation from feet to feet; the bounding box minY is the target's feet.
        float advantage = heightAdvantage((float) (player.getY() - boundingBox.minY));
        if (advantage == 0.0F) {
            return;
        }
        double distance = Math.sqrt(boundingBox.distanceToSqr(player.getEyePosition()));
        callback.setReturnValue(distance >= effectiveMinRange(player) - hitboxMargin() - extraBuffer
                && distance <= effectiveMaxRange(player) + hitboxMargin() + extraBuffer + advantage);
    }

    private static boolean hasAttackRangeComponent(Player player) {
        ItemStack held = player.getMainHandItem();
        return held.has(DataComponents.ATTACK_RANGE);
    }

    private static float heightAdvantage(float elevationDifference) {
        if (elevationDifference > 0.5F) {
            return Math.min(1.0F, (elevationDifference - 0.5F) * 0.5F);
        }
        if (elevationDifference < -0.5F) {
            return Math.max(-1.0F, (elevationDifference + 0.5F) * 0.5F);
        }
        return 0.0F;
    }
}
