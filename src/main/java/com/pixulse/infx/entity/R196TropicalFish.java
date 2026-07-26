package com.pixulse.infx.entity;

import com.pixulse.infx.item.R196MobBucketKind;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** InfiniteX tropical fish replacement retaining vanilla patterns and school data. */
public final class R196TropicalFish extends TropicalFish implements R196Mob {
    public R196TropicalFish(EntityType<? extends TropicalFish> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return AbstractFish.createAttributes();
    }

    @Override
    public ItemStack getBucketItemStack() {
        return ModItems.mobBucket(R196Material.IRON, R196MobBucketKind.TROPICAL).toStack();
    }
}
