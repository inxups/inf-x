package com.pixulse.infx.entity;

import com.pixulse.infx.item.MobBucketKind;
import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.registry.InfinityXItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** InfiniteX tropical fish replacement retaining vanilla patterns and school data. */
public final class MiteTropicalFish extends TropicalFish implements MiteMob {
    public MiteTropicalFish(EntityType<? extends TropicalFish> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return AbstractFish.createAttributes();
    }

    @Override
    public ItemStack getBucketItemStack() {
        return InfinityXItems.mobBucket(MiteMaterial.IRON, MobBucketKind.TROPICAL).toStack();
    }
}
