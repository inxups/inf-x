package com.pixulse.infx.mixin;

import com.pixulse.infx.item.R196BucketHelper;
import com.pixulse.infx.item.R196BucketItem;
import com.pixulse.infx.item.R196MobBucketKind;
import com.pixulse.infx.material.R196Material;
import java.util.Optional;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Bucketable;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla {@link Bucketable} only accepts {@code Items.WATER_BUCKET}. R196 water buckets must also
 * capture fish/axolotl/tadpole while preserving the bucket material.
 */
@Mixin(Bucketable.class)
interface BucketableMixin {
    @Inject(method = "canBePickedUpWithBucket", at = @At("HEAD"), cancellable = true)
    private void infx$acceptR196WaterBuckets(ItemStack stack, CallbackInfoReturnable<Boolean> callback) {
        if (R196BucketHelper.isR196WaterBucket(stack)) {
            callback.setReturnValue(true);
        }
    }

    @Inject(method = "bucketMobPickup", at = @At("HEAD"), cancellable = true)
    private static <T extends LivingEntity & Bucketable> void infx$materialPreservingPickup(
            Player player,
            InteractionHand hand,
            T pickupEntity,
            CallbackInfoReturnable<Optional<InteractionResult>> callback) {
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof R196BucketItem bucket)
                || bucket.contents() != R196BucketItem.Contents.WATER
                || !pickupEntity.isAlive()) {
            return;
        }
        if (!pickupEntity.canBePickedUpWithBucket(held)) {
            return;
        }
        R196MobBucketKind kind = R196MobBucketKind.of(pickupEntity.getType());
        if (kind == null) {
            return;
        }
        R196Material material = bucket.material();
        pickupEntity.playSound(pickupEntity.getPickupSound(), 1.0F, 1.0F);
        ItemStack filled = R196BucketHelper.mobBucket(material, kind);
        pickupEntity.saveToBucketTag(filled);
        ItemStack result = ItemUtils.createFilledResult(held, player, filled, false);
        player.setItemInHand(hand, result);
        Level level = pickupEntity.level();
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.FILLED_BUCKET.trigger(serverPlayer, filled);
        }
        if (pickupEntity instanceof Leashable leashable) {
            leashable.dropLeash();
        }
        pickupEntity.discard();
        callback.setReturnValue(Optional.of(InteractionResult.SUCCESS));
    }
}
