package com.pixulse.infx.entity;

import com.pixulse.infx.registry.ModEntityTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** R196 sheep replacement; reuses 26.2 sheep model/textures. */
public final class R196Sheep extends Sheep {
    public R196Sheep(EntityType<? extends Sheep> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Sheep.createAttributes().add(Attributes.MAX_HEALTH, 8.0);
    }

    @Override
    public @Nullable Sheep getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ModEntityTypes.R196_SHEEP.get().create(level, EntitySpawnReason.BREEDING);
    }
}
