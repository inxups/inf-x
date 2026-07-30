package com.pixulse.infx.entity;

import com.pixulse.infx.registry.InfXEntityTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** INFX ocelot replacement; reuses 26.2 ocelot model/textures (no livestock needs). */
public final class InfxOcelot extends Ocelot {
    public InfxOcelot(EntityType<? extends Ocelot> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Ocelot.createAttributes();
    }

    @Override
    public @Nullable Ocelot getBreedOffspring(@NonNull ServerLevel level, @NonNull AgeableMob partner) {
        return InfXEntityTypes.INFX_OCELOT.get().create(level, EntitySpawnReason.BREEDING);
    }
}
