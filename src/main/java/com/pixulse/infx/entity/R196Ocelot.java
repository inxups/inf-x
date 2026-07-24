package com.pixulse.infx.entity;

import com.pixulse.infx.registry.ModEntityTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** R196 ocelot replacement; reuses 26.2 ocelot model/textures. */
public final class R196Ocelot extends Ocelot {
    public R196Ocelot(EntityType<? extends Ocelot> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Ocelot.createAttributes();
    }

    @Override
    public @Nullable Ocelot getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ModEntityTypes.R196_OCELOT.get().create(level, EntitySpawnReason.BREEDING);
    }
}
