package com.pixulse.infx.mixin.world.entity.animal.equine;

import com.pixulse.infx.registry.InfXEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.SkeletonTrapGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * InfX skeleton-horse traps field INFX skeleton riders instead of the vanilla
 * skeleton: the trap spawns its riders with EntitySpawnReason.TRIGGERED, a
 * reason the global EntityJoinLevelEvent replacement deliberately skips, so the
 * rider entity type is swapped right at the trap's creation point.
 */
@Mixin(SkeletonTrapGoal.class)
public abstract class SkeletonTrapGoalMixin {
    @Redirect(
            method = "createSkeleton",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/EntityType;SKELETON:Lnet/minecraft/world/entity/EntityType;"))
    private static EntityType<?> infx$trapRiderType() {
        return InfXEntityTypes.INFX_SKELETON.get();
    }
}
