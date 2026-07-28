package com.pixulse.infx.util;

import com.pixulse.infx.item.MiteBucketItem;
import com.pixulse.infx.item.MiteMobBucketItem;
import com.pixulse.infx.item.MiteSolidBucketItem;
import com.pixulse.infx.item.MobBucketKind;
import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.registry.InfinityXItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/** Resolves material-preserving bucket variants from held empty/water buckets. */
public final class BucketHelper {
    private BucketHelper() {}

    public static boolean isR196WaterBucket(ItemStack stack) {
        return stack.getItem() instanceof MiteBucketItem bucket
                && bucket.contents() == MiteBucketItem.Contents.WATER;
    }

    public static boolean isR196EmptyBucket(ItemStack stack) {
        return stack.getItem() instanceof MiteBucketItem bucket
                && bucket.contents() == MiteBucketItem.Contents.EMPTY;
    }

    public static @Nullable MiteMaterial materialOf(ItemStack stack) {
        if (stack.getItem() instanceof MiteBucketItem bucket) {
            return bucket.material();
        }
        if (stack.getItem() instanceof MiteMobBucketItem mob) {
            return mob.material();
        }
        if (stack.getItem() instanceof MiteSolidBucketItem solid) {
            return solid.material();
        }
        return null;
    }

    public static ItemStack emptyBucket(MiteMaterial material) {
        return InfinityXItems.bucket(material, MiteBucketItem.Contents.EMPTY).toStack();
    }

    public static ItemStack waterBucket(MiteMaterial material) {
        return InfinityXItems.bucket(material, MiteBucketItem.Contents.WATER).toStack();
    }

    public static ItemStack mobBucket(MiteMaterial material, MobBucketKind kind) {
        return InfinityXItems.mobBucket(material, kind).toStack();
    }

    public static ItemStack mobBucket(MiteMaterial material, EntityType<?> type) {
        MobBucketKind kind = MobBucketKind.of(type);
        if (kind == null) {
            return ItemStack.EMPTY;
        }
        return mobBucket(material, kind);
    }

    public static ItemStack powderSnowBucket(MiteMaterial material) {
        return InfinityXItems.powderSnowBucket(material).toStack();
    }

    public static Item emptySuccessItem(ItemStack filled, net.minecraft.world.entity.player.Player player) {
        if (player.hasInfiniteMaterials()) {
            return filled.getItem();
        }
        if (filled.getItem() instanceof MiteMobBucketItem mob) {
            return mob.emptyBucket();
        }
        if (filled.getItem() instanceof MiteSolidBucketItem solid) {
            return solid.emptyBucket();
        }
        if (filled.getItem() instanceof MiteBucketItem bucket) {
            return bucket.emptyBucket();
        }
        return Items.BUCKET;
    }
}
