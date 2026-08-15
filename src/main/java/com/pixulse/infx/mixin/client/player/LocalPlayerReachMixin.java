package com.pixulse.infx.mixin.client.player;

import com.pixulse.infx.item.ItemReach;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Uses vanilla mount-safe picking with an INFX-owned scan range and final candidate filter. */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerReachMixin {
    @Redirect(
            method = "raycastHitResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object ignoreAttackRange(ItemStack stack, DataComponentType<?> type) {
        return type == DataComponents.ATTACK_RANGE ? null : stack.get(type);
    }

    @Redirect(
            method = "raycastHitResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;entityInteractionRange()D"))
    private double targetingRange(LocalPlayer player) {
        return ItemReach.targetingRange(player);
    }

    @Inject(method = "raycastHitResult", at = @At("RETURN"), cancellable = true)
    private void filterEntityCandidate(
            float partialTick, Entity cameraEntity, CallbackInfoReturnable<HitResult> callback) {
        HitResult result = callback.getReturnValue();
        LocalPlayer player = (LocalPlayer) (Object) this;
        if (!(result instanceof EntityHitResult)
                || ItemReach.isWithinTargetingRange(player, result.getLocation())) {
            return;
        }

        Vec3 from = player.getEyePosition(partialTick);
        Vec3 location = result.getLocation();
        Direction direction = Direction.getApproximateNearest(
                location.x - from.x, location.y - from.y, location.z - from.z);
        callback.setReturnValue(BlockHitResult.miss(location, direction, BlockPos.containing(location)));
    }
}
