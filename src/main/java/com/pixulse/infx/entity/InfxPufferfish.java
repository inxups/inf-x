package com.pixulse.infx.entity;

import com.pixulse.infx.item.MobBucketKind;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.fish.Pufferfish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

/** InfiniteX pufferfish replacement retaining vanilla inflation and poison behavior. */
public final class InfxPufferfish extends Pufferfish implements InfxMob {
    public InfxPufferfish(EntityType<? extends Pufferfish> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return AbstractFish.createAttributes();
    }

    @Override
    public @NonNull ItemStack getBucketItemStack() {
        return InfXItems.mobBucket(InfxMaterial.IRON, MobBucketKind.PUFFERFISH).toStack();
    }
}
