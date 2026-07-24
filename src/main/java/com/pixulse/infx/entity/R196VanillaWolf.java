package com.pixulse.infx.entity;

import com.pixulse.infx.registry.ModEntityTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * R196 vanilla-wolf replacement (separate from hellhound/dire wolf).
 * Reuses 26.2 wolf model/textures; blood/blue moon rules apply via events.
 */
public final class R196VanillaWolf extends Wolf {
    public R196VanillaWolf(EntityType<? extends Wolf> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Wolf.createAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.40)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    public @Nullable Wolf getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ModEntityTypes.R196_WOLF.get().create(level, EntitySpawnReason.BREEDING);
    }
}
