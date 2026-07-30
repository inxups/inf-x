package com.pixulse.infx.entity;

import com.pixulse.infx.item.MobBucketKind;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

/** InfiniteX tropical fish replacement retaining vanilla patterns and school data. */
public final class InfxTropicalFish extends TropicalFish implements InfxMob {
    public InfxTropicalFish(EntityType<? extends TropicalFish> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return AbstractFish.createAttributes();
    }

    @Override
    public @NonNull ItemStack getBucketItemStack() {
        return InfXItems.mobBucket(InfxMaterial.IRON, MobBucketKind.TROPICAL).toStack();
    }
}
