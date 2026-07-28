package com.pixulse.infx.mixin;

import com.pixulse.infx.util.BucketHelper;
import com.pixulse.infx.item.MiteBucketItem;
import com.pixulse.infx.item.MobBucketKind;
import com.pixulse.infx.item.material.MiteMaterial;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Bucketable;
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
    @Inject(method = "bucketMobPickup", at = @At("HEAD"), cancellable = true)
    private static <T extends LivingEntity & Bucketable> void infx$materialPreservingPickup(
            Player player,
            InteractionHand hand,
            T pickupEntity,
            CallbackInfoReturnable<Optional<InteractionResult>> callback) {
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof MiteBucketItem bucket)
                || bucket.contents() != MiteBucketItem.Contents.WATER
                || !pickupEntity.isAlive()) {
            return;
        }
        MobBucketKind kind = MobBucketKind.of(pickupEntity.getType());
        if (kind == null) {
            return;
        }
        MiteMaterial material = bucket.material();
        pickupEntity.playSound(pickupEntity.getPickupSound(), 1.0F, 1.0F);
        ItemStack filled = BucketHelper.mobBucket(material, kind);
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
