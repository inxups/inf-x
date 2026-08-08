package com.pixulse.infx.mixin.world.entity.animal;

import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * InfX removes the vanilla "feed a baby to speed up its growth" behavior: babies only grow by
 * waiting. The golden dandelion age-lock interaction lives in {@code AgeableMob.mobInteract},
 * which is reached through the super call after this branch, so it stays intact.
 */
@Mixin(Animal.class)
public abstract class AnimalBabyFeedMixin {
    @Redirect(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Animal;canAgeUp()Z"))
    private boolean infx$disableBabyGrowthFeed(Animal animal) {
        return false;
    }
}
