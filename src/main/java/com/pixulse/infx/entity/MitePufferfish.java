package com.pixulse.infx.entity;

import com.pixulse.infx.item.MobBucketKind;
import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.fish.Pufferfish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** InfiniteX pufferfish replacement retaining vanilla inflation and poison behavior. */
public final class MitePufferfish extends Pufferfish implements MiteMob {
    public MitePufferfish(EntityType<? extends Pufferfish> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return AbstractFish.createAttributes();
    }

    @Override
    public ItemStack getBucketItemStack() {
        return InfXItems.mobBucket(MiteMaterial.IRON, MobBucketKind.PUFFERFISH).toStack();
    }
}
