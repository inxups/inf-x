package com.pixulse.infx.entity;

import com.pixulse.infx.registry.ModEntityTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** R196 cow replacement; reuses 26.2 cow model/textures with livestock rules applied by events. */
public final class R196Cow extends Cow {
    public R196Cow(EntityType<? extends Cow> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return AbstractCow.createAttributes().add(Attributes.MAX_HEALTH, 20.0);
    }

    @Override
    public @Nullable Cow getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ModEntityTypes.R196_COW.get().create(level, EntitySpawnReason.BREEDING);
    }
}
