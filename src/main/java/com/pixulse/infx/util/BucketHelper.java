package com.pixulse.infx.util;

import com.pixulse.infx.item.InfxBucketItem;
import com.pixulse.infx.item.InfxMobBucketItem;
import com.pixulse.infx.item.InfxSolidBucketItem;
import com.pixulse.infx.item.MobBucketKind;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/** Resolves material-preserving bucket variants from held empty/water buckets. */
public final class BucketHelper {
    private BucketHelper() {}

    public static boolean isR196WaterBucket(ItemStack stack) {
        return stack.getItem() instanceof InfxBucketItem bucket
                && bucket.contents() == InfxBucketItem.Contents.WATER;
    }

    public static boolean isR196EmptyBucket(ItemStack stack) {
        return stack.getItem() instanceof InfxBucketItem bucket
                && bucket.contents() == InfxBucketItem.Contents.EMPTY;
    }

    public static @Nullable InfxMaterial materialOf(ItemStack stack) {
        if (stack.getItem() instanceof InfxBucketItem bucket) {
            return bucket.material();
        }
        if (stack.getItem() instanceof InfxMobBucketItem mob) {
            return mob.material();
        }
        if (stack.getItem() instanceof InfxSolidBucketItem solid) {
            return solid.material();
        }
        return null;
    }

    public static ItemStack emptyBucket(InfxMaterial material) {
        return InfXItems.bucket(material, InfxBucketItem.Contents.EMPTY).toStack();
    }

    public static ItemStack waterBucket(InfxMaterial material) {
        return InfXItems.bucket(material, InfxBucketItem.Contents.WATER).toStack();
    }

    public static ItemStack mobBucket(InfxMaterial material, MobBucketKind kind) {
        return InfXItems.mobBucket(material, kind).toStack();
    }

    public static ItemStack mobBucket(InfxMaterial material, EntityType<?> type) {
        MobBucketKind kind = MobBucketKind.of(type);
        if (kind == null) {
            return ItemStack.EMPTY;
        }
        return mobBucket(material, kind);
    }

    public static ItemStack powderSnowBucket(InfxMaterial material) {
        return InfXItems.powderSnowBucket(material).toStack();
    }

    public static Item emptySuccessItem(ItemStack filled, net.minecraft.world.entity.player.Player player) {
        if (player.hasInfiniteMaterials()) {
            return filled.getItem();
        }
        if (filled.getItem() instanceof InfxMobBucketItem mob) {
            return mob.emptyBucket();
        }
        if (filled.getItem() instanceof InfxSolidBucketItem solid) {
            return solid.emptyBucket();
        }
        if (filled.getItem() instanceof InfxBucketItem bucket) {
            return bucket.emptyBucket();
        }
        return Items.BUCKET;
    }
}
