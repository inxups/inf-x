package com.pixulse.infx.entity;

import com.pixulse.infx.registry.ModEntityTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** R196 pig replacement; reuses 26.2 pig model/textures. */
public final class R196Pig extends Pig {
    public R196Pig(EntityType<? extends Pig> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Pig.createAttributes().add(Attributes.MAX_HEALTH, 10.0);
    }

    @Override
    public @Nullable Pig getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ModEntityTypes.R196_PIG.get().create(level, EntitySpawnReason.BREEDING);
    }
}
