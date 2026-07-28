package com.pixulse.infx.entity;

import com.pixulse.infx.item.MobBucketKind;
import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.fish.Salmon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** InfiniteX salmon replacement retaining vanilla school and size variants. */
public final class MiteSalmon extends Salmon implements MiteMob {
    public MiteSalmon(EntityType<? extends Salmon> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return AbstractFish.createAttributes();
    }

    @Override
    public ItemStack getBucketItemStack() {
        return InfXItems.mobBucket(MiteMaterial.IRON, MobBucketKind.SALMON).toStack();
    }
}
