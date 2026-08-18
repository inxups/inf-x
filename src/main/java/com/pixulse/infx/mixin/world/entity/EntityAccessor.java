package com.pixulse.infx.mixin.world.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Accessor for the protected {@code stuckSpeedMultiplier} field used by Free Movement. */
@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("stuckSpeedMultiplier")
    Vec3 infx$getStuckSpeedMultiplier();

    @Accessor("stuckSpeedMultiplier")
    void infx$setStuckSpeedMultiplier(Vec3 value);
}
