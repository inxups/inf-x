package com.pixulse.infx.entity;

import com.pixulse.infx.item.R196MobBucketKind;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.fish.Salmon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** InfiniteX salmon replacement retaining vanilla school and size variants. */
public final class R196Salmon extends Salmon implements R196Mob {
    public R196Salmon(EntityType<? extends Salmon> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return AbstractFish.createAttributes();
    }

    @Override
    public ItemStack getBucketItemStack() {
        return ModItems.mobBucket(R196Material.IRON, R196MobBucketKind.SALMON).toStack();
    }
}
