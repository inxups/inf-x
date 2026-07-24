package com.pixulse.infx.entity;

import com.pixulse.infx.registry.ModEntityTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** R196 chicken replacement; reuses 26.2 chicken model/textures. */
public final class R196Chicken extends Chicken {
    public R196Chicken(EntityType<? extends Chicken> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Chicken.createAttributes().add(Attributes.MAX_HEALTH, 4.0);
    }

    @Override
    public @Nullable Chicken getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ModEntityTypes.R196_CHICKEN.get().create(level, EntitySpawnReason.BREEDING);
    }
}
