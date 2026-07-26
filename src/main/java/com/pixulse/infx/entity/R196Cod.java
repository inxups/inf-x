package com.pixulse.infx.entity;

import com.pixulse.infx.item.R196MobBucketKind;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.fish.Cod;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** InfiniteX cod replacement retaining vanilla schooling and bucket persistence. */
public final class R196Cod extends Cod implements R196Mob {
    public R196Cod(EntityType<? extends Cod> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return AbstractFish.createAttributes();
    }

    @Override
    public ItemStack getBucketItemStack() {
        return ModItems.mobBucket(R196Material.IRON, R196MobBucketKind.COD).toStack();
    }
}
