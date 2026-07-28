package com.pixulse.infx.entity;

import com.pixulse.infx.item.MobBucketKind;
import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.registry.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.fish.Cod;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** InfiniteX cod replacement retaining vanilla schooling and bucket persistence. */
public final class MiteCod extends Cod implements MiteMob {
    public MiteCod(EntityType<? extends Cod> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return AbstractFish.createAttributes();
    }

    @Override
    public ItemStack getBucketItemStack() {
        return ModItems.mobBucket(MiteMaterial.IRON, MobBucketKind.COD).toStack();
    }
}
