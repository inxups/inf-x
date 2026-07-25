package com.pixulse.infx.item;

import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/** Resolves material-preserving bucket variants from held empty/water buckets. */
public final class R196BucketHelper {
    private R196BucketHelper() {}

    public static boolean isR196WaterBucket(ItemStack stack) {
        return stack.getItem() instanceof R196BucketItem bucket
                && bucket.contents() == R196BucketItem.Contents.WATER;
    }

    public static boolean isR196EmptyBucket(ItemStack stack) {
        return stack.getItem() instanceof R196BucketItem bucket
                && bucket.contents() == R196BucketItem.Contents.EMPTY;
    }

    public static @Nullable R196Material materialOf(ItemStack stack) {
        if (stack.getItem() instanceof R196BucketItem bucket) {
            return bucket.material();
        }
        if (stack.getItem() instanceof R196MobBucketItem mob) {
            return mob.material();
        }
        if (stack.getItem() instanceof R196SolidBucketItem solid) {
            return solid.material();
        }
        return null;
    }

    public static ItemStack emptyBucket(R196Material material) {
        return ModItems.bucket(material, R196BucketItem.Contents.EMPTY).toStack();
    }

    public static ItemStack waterBucket(R196Material material) {
        return ModItems.bucket(material, R196BucketItem.Contents.WATER).toStack();
    }

    public static ItemStack mobBucket(R196Material material, R196MobBucketKind kind) {
        return ModItems.mobBucket(material, kind).toStack();
    }

    public static ItemStack mobBucket(R196Material material, EntityType<?> type) {
        R196MobBucketKind kind = R196MobBucketKind.of(type);
        if (kind == null) {
            return ItemStack.EMPTY;
        }
        return mobBucket(material, kind);
    }

    public static ItemStack powderSnowBucket(R196Material material) {
        return ModItems.powderSnowBucket(material).toStack();
    }

    public static Item emptySuccessItem(ItemStack filled, net.minecraft.world.entity.player.Player player) {
        if (player.hasInfiniteMaterials()) {
            return filled.getItem();
        }
        if (filled.getItem() instanceof R196MobBucketItem mob) {
            return mob.emptyBucket();
        }
        if (filled.getItem() instanceof R196SolidBucketItem solid) {
            return solid.emptyBucket();
        }
        if (filled.getItem() instanceof R196BucketItem bucket) {
            return bucket.emptyBucket();
        }
        return Items.BUCKET;
    }
}
